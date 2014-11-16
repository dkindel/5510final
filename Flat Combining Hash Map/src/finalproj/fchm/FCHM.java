package finalproj.fchm;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import finalproj.locks.RejectLock;


public class FCHM<K,V>{
	private ThreadLocal<Record<K,V>> rec = null;
	private AtomicReference<Record<K,V>> head = new AtomicReference<Record<K,V>>();
	private RejectLock lock = new RejectLock();
	private long count = 0;
	
	HashMap<K,V> map = new HashMap<K,V>(50);
	
	public FCHM(){
		rec = new ThreadLocal<Record<K,V>>(){
			protected Record<K,V> initialValue(){
				return new Record<K,V>();
			}
		};
	}
	
	public V put(K key, V val){
		return runFunc(0, key, val);
	}
	
	public V remove(K key){
		return runFunc(1, key, null);
	}

	public V get(K key){
		return runFunc(2, key, null);
	}
	
	
	private V runFunc(int op, K key,V val){
		while(true){
			rec.get().req.op = op;
			rec.get().req.key = key;
			rec.get().req.value = val;
			if(rec.get().active){
				return active();
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
			if(!lock.lock()){ //NOT the combiner
				while(!rec.get().req.done &&
						rec.get().active &&
						(locked = lock.isLocked()));
			}
			else{//AM the combiner
				amLockholder();
				lock.unlock();
				//don't worry about locked. 
			}
			if(!rec.get().active){
				//if(rec.get().req.done){ //managed to finish before inactive
				//	return rec.get().req.retval;
				//}
				//else{ //just inactive. flip the bit
					set_active();
					continue;
				//}
			}
			//if lockholder, won't enter.  if came unlocked, and spinng, 
			//we try to grab it
			else if(!locked){
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
	}
	
	private void scanCombineApply() {
		Record<K,V> curr = head.get();
		while((curr = curr.next) != null){
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
				default:
					System.err.println("I have no idea how this happened but a " +
							"bad op was passed.  We'll pass over this one.");
					break;
				}
			}
			curr.age = count;
		}
	}

	private void set_active(){
		while(true){
			Record<K,V> headref = head.get();
			rec.get().next = headref;
			if(head.compareAndSet(headref, rec.get()))
				break;
		}
		rec.get().active = true;
	}
	
}
