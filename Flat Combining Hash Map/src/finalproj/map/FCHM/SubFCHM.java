package finalproj.map.FCHM;


import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;
import finalproj.map.HM;
import finalproj.map.locks.RejectLock;

/**
 * This class contains the implementation details for Flat Combining
 * @author dave
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class SubFCHM<K,V> implements HM<K, V>{
	private ThreadLocal<Record<K,V>> rec = null;
	private AtomicReference<Record<K,V>> head;
	private RejectLock lock;
	private long count = 0;
	private long cleanedupat = 0;
	
	private static final int CLEANUP_COUNT = 1000;
	private static final int AGE_DIFFERENCE = 1000;
	
	
	
	private HashMap<K,V> map;
	
	/**
	 * The constructor sets up the Flat Combining Hashm Map to use a sequential
	 * hash map for the combiner to use
	 * @param capacity
	 */
	public SubFCHM(int capacity){
		map = new HashMap<K,V>(capacity);
		rec = new ThreadLocal<Record<K,V>>(){
			protected Record<K,V> initialValue(){
				return new Record<K,V>();
			}
		};
		lock = new RejectLock();
		head  = new AtomicReference<Record<K,V>>();
	}
	
	/**
	 * Put the key and value into the hash map as either the combiner 
	 * or the waiter thread
	 */
	public V put(K key, V val){
		return runFunc(0, key, val);
	}

	/**
	 * Removes the key and value associated with the provided key from the 
	 * hash map as either the combiner or the waiter thread
	 */
	public V remove(K key){
		return runFunc(1, key);
	}

	/**
	 * Gets the value associated with the key from the hash map as either 
	 * the combiner or the waiter thread
	 */
	public V get(K key){
		return runFunc(2, key);
	}
	
	/**
	 * Prints of the hash map
	 */
	public void print(){
		runFunc(3);
	}
	
	/**
	 * helper thread to run the function with some null parameters
	 * @param op the operation to perform
	 * @return the return value from the operation
	 */
	private V runFunc(int op){
		return runFunc(op, null, null);
	}

	/**
	 * helper thread to run the function with some null parameters
	 * @param op the operation to perform
	 * @param key the key to run with
	 * @return the return value from the operation
	 */
	private V runFunc(int op, K key){
		return runFunc(op, key, null);
	}
	
	/**
	 * FC entry point for the operation op and key and value
	 * @param op the operation to be performed
	 * @param key the key to operate with/on
	 * @param val the value to operate with
	 * @return the return valu from running the operation
	 */
	private V runFunc(int op, K key,V val){
		//System.out.println("running op " + op +" with "+ key + " and " + val);
		while(true){
			//set all of the relevent values
			rec.get().req.op = op;
			rec.get().req.key = key;
			rec.get().req.value = val;
			//if it's active run the active function
			if(rec.get().active){
				V ret = active();
				//System.out.println("success " + op +" with "+ key + " and " + val);
				return ret;
			}else{
				//if it's not active, we need to make the 
				//record for this thread active
				set_active();
			}
		}
	}
	
	//Record is active, we must enter and run
	private V active(){
		//we know at the start, we clearly aren't done
		//and we also know the params are also set
		rec.get().req.done = false;
		while(true){
			boolean locked = true;
			if(!lock.lock()){ //NOT the combiner
				while(!rec.get().req.done &&
						rec.get().active &&
						(locked = lock.isLocked()));
			}
			else{//AM the combiner
				//run through the publication list and complete all the operations
				amLockholder();
				lock.unlock();
				//don't worry about locked. 
			}
			//if we've been set inactive, we need to fix that and continue again
			if(!rec.get().active){
				set_active();
				continue;
			}
			//if lockholder, won't enter.  if came unlocked, and spinning, 
			//we try to grab it
			else if(!locked){
				continue;
			}
			else{//returned
				return rec.get().req.retval;
			}
		}
	}
	
	/**
	 * I am the lockholder!  increase the age and run scanCombineApply()
	 * Possibly need to remove older records as well, if it's time to do so
	 */
	private void amLockholder(){
		count++;
		scanCombineApply();
		if((count - cleanedupat) >= CLEANUP_COUNT){
			removeOldRecords();
		}
	}
	
	/**
	 * to remove old record, we run through the entire publication list and read 
	 * everybody's age, comparing it to our threshold.  A physical remove may be
	 * necessary
	 */
	private void removeOldRecords(){
		Record<K,V> curr = head.get();
		Record<K,V> pred;
		
		while(curr != null){
			pred = curr;
			curr = curr.next; //skip first node
			
			//perform the physical deletion
			while((curr != null) && curr.age < (count - AGE_DIFFERENCE)){
				pred.next = curr.next;
				curr.next = null;
				curr.added = false;
				curr.active = false;
				curr = curr.next;
			}
		}
	}
	
	/**
	 * Run scanCombineApply. This is the bulk of the work that the combiner performs. 
	 * This is where the sequential HM is used and all operations are performed.
	 * To make this more efficient, here is where I could run through the list, 
	 * gathering work and making operation judgments before completing any operations
	 * but that simply isn't done for this hash map.  Operations are run as soon as 
	 * they're seen
	 */
	private void scanCombineApply() {
		Record<K,V> curr = head.get();
		//start at head and run through the publication list
		while(curr != null){
			//check if it's necessary to operate
			if(!curr.req.done){ //means we need to execute
				switch(curr.req.op){
				case 0:
					curr.req.retval = map.put(curr.req.key, curr.req.value);
					break;
				case 1:
					curr.req.retval = map.remove(curr.req.key);
					break;
				case 2: 
					curr.req.retval = map.get(curr.req.key);
					break;
				case 3:
					//map.print();
					print();
					break;
				default:
					System.err.println("I have no idea how this happened but a " +
							"bad op was passed.  We'll pass over this one.");
					break;
				}
				//set the new age and set done to true!
				curr.age = count;
				curr.req.done = true;
			}
			curr = curr.next;
		}
	}

	/**
	 * Sets the record as active.  
	 * If the record isn't in the publication list, we add it in
	 */
	private void set_active(){
		while(!rec.get().added){
			Record<K,V> headref = head.get();
			rec.get().next = headref;
			rec.get().added = head.compareAndSet(headref, rec.get());
		}
		rec.get().active = true;
	}

	/**
	 * prints the entire publication list.  This is just a helper function
	 * for debugging.
	 */
	public synchronized void printList(){
		Record<K,V> curr = head.get();
		while(curr != null){
			System.out.println("node: " + curr);
			curr = curr.next;
		}
	}
}
