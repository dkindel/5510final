package finalproj.fchm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicReference;

import finalproj.locks.RejectLock;


public class FCHM<K,V>{
	private ThreadLocal<Record<K,V>> rec = null;
	private AtomicReference<Record<K,V>> head;
	private RejectLock lock;
	private long count = 0;
	
	private static final int CLEANUP_COUNT = 20;
	
	
	
	private HashMap<K,V> map = new HashMap<K,V>(50);
	
	public FCHM(){
		rec = new ThreadLocal<Record<K,V>>(){
			protected Record<K,V> initialValue(){
				return new Record<K,V>();
			}
		};
		lock = new RejectLock();
		head  = new AtomicReference<Record<K,V>>();
	}
	
	public V put(K key, V val){
		return runFunc(0, key, val);
	}
	
	public V remove(K key){
		return runFunc(1, key);
	}

	public V get(K key){
		return runFunc(2, key);
	}
	
	public void print(){
		runFunc(3);
	}
	
	private V runFunc(int op){
		return runFunc(op, null, null);
	}
	
	private V runFunc(int op, K key){
		return runFunc(op, key, null);
	}
	
	
	private V runFunc(int op, K key,V val){
		//System.out.println("running op " + op +" with "+ key + " and " + val);
		while(true){
			rec.get().req.op = op;
			rec.get().req.key = key;
			rec.get().req.value = val;
			if(rec.get().active){
				V ret = active();
				//System.out.println("success " + op +" with "+ key + " and " + val);
				return ret;
			}else{
				set_active();
			}
		}
	}
	
	private V active(){
		//ReturnClass ret = new ReturnClass(); //set this up to return
		rec.get().req.done = false;
		while(true){
			boolean locked = true;
			//int tid = ((FCHMThread) Thread.currentThread()).getThreadId();
			if(!lock.lock()){ //NOT the combiner
				//System.out.println("Thread " + tid + " not it");
				while(!rec.get().req.done &&
						rec.get().active &&
						(locked = lock.isLocked()));
			}
			else{//AM the combiner
				//System.out.println("Thread " + tid + " it");
				amLockholder();
				/*try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}*/
				lock.unlock();
				//don't worry about locked. 
			}
			if(!rec.get().active){
				//System.out.println("returning because inactive");
				set_active();
				continue;
			}
			//if lockholder, won't enter.  if came unlocked, and spinning, 
			//we try to grab it
			else if(!locked){
				//System.out.println("returning because not locked");
				continue;
			}
			else{//returned
				return rec.get().req.retval;
			}
		}
	}
	
	private void amLockholder(){
		count++;
		scanCombineApply();
		removeOldRecords();
	}
	
	private void removeOldRecords(){
		Record<K,V> curr = head.get();
		while(curr != null){
			//if(curr.age < )
			curr = curr.next;
		}
	}
	
	private void scanCombineApply() {
		Record<K,V> curr = head.get();
		while(curr != null){
			if(!curr.req.done){ //means we need to execute
				switch(curr.req.op){
				case 0:
					//System.out.println("putting " + curr.req.key + " and " + curr.req.value);
					curr.req.retval = map.put(curr.req.key, curr.req.value);
					break;
				case 1:
					curr.req.retval = map.remove(curr.req.key);
					break;
				case 2: 
					curr.req.retval = map.get(curr.req.key);
					break;
				case 3:
				    Iterator<Entry<K, V>> it = map.entrySet().iterator();
				    while (it.hasNext()) {
				        Map.Entry<K,V> pairs = it.next();
				        System.out.println(pairs.getKey() + " = " + pairs.getValue());
				        it.remove(); // avoids a ConcurrentModificationException
				    }
					break;
				default:
					System.err.println("I have no idea how this happened but a " +
							"bad op was passed.  We'll pass over this one.");
					break;
				}
				curr.age = count;
				curr.req.done = true;
			}
			curr = curr.next;
		}
	}

	private void set_active(){
		while(!rec.get().added){
			Record<K,V> headref = head.get();
			rec.get().next = headref;
			rec.get().added = head.compareAndSet(headref, rec.get());
		}
		rec.get().active = true;
	}

	public synchronized void printList(){
		Record<K,V> curr = head.get();
		while(curr != null){
			System.out.println("node: " + curr);
			curr = curr.next;
		}
	}
}
