package finalproj.fchm;

import java.util.concurrent.atomic.AtomicReference;

import finalproj.locks.RejectLock;


public class FCHM<T>{
	private ThreadLocal<Record<T>> rec = null;
	private AtomicReference<Record<T>> head = new AtomicReference<Record<T>>();
	private RejectLock lock = new RejectLock();
	
	public FCHM(){
		rec = new ThreadLocal<Record<T>>(){
			protected Record<T> initialValue(){
				return new Record<T>();
			}
		};
	}
	
	public void add(T val){
		runFunc(0, val);
	}
	
	public void remove(T val){
		runFunc(1, val);
	}

	public boolean contains(T val){
		return runFunc(2, val);
	}
	
	
	private boolean runFunc(int op, T val){
		while(true){
			if(rec.get().added && rec.get().active){
				return active(op, val);
			}else{
				set_active();
			}
		}
	}
	
	private boolean active(int op, T val){
		//ReturnClass ret = new ReturnClass(); //set this up to return
		rec.get().req.op = op;
		rec.get().req.param = val;
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
				if(rec.get().req.done){ //managed to finish before inactive
					return rec.get().req.retval;
				}
				else{ //just inactive. flip the bit
					set_active();
					continue;
				}
			}
			//if lockholder, won't enter.  if came unlocked, and spinng, we try to grab it
			else if(!locked){
				continue;
			}
			else{//returned
				return rec.get().req.retval;
			}
		}
	}
	
	private void amLockholder(){
		scanCombineApply();
	}
	
	private void scanCombineApply() {
		
	}

	private void set_active(){
		if(!rec.get().added){
			rec.get().added = true;
			rec.get().active = true;
			while(true){
				Record<T> headref = head.get();
				rec.get().next = headref;
				if(head.compareAndSet(headref, rec.get()))
					break;
			}
		}else{
			rec.get().active = true;
		}
	}
	
}
