package finalproj.fchm;

import java.util.concurrent.atomic.AtomicReference;

import finalproj.locks.RejectLock;


public class FCHM<T>{
	private ThreadLocal<Record<T>> rec = null;
	private AtomicReference<Record<T>> head;
	private RejectLock lock;
	
	public FCHM(){
		/*rec = new ThreadLocal<Record>(){
			protected Record initialValue(){
				return new Record();
			}
		};*/
		
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
	
	class ReturnClass{
		boolean retval = false;
		boolean is_now_inactive = false;
	}
	
	private boolean runFunc(int op, T val){
		while(true){
			if((rec == null) && (rec.get().active)){
				ReturnClass ret = active(op, val);
				if(ret.is_now_inactive)
					continue; //it's now inactive, so we must set it active and continue
				else{
					return ret.retval;
				}
			}else{
				set_active();
			}
		}
	}
	
	private ReturnClass active(int op, T val){
		ReturnClass ret = new ReturnClass(); //set this up to return
		rec.get().req.op = op;
		rec.get().req.param = val;
		rec.get().req.done = false;
		if(!lock.lock()){ //NOT the combiner
			
		}
		else{//AM the combiner
			amLockholder(ret);
		}
		return ret;
	}
	
	private void amLockholder(ReturnClass ret){
		scanCombineApply();
		ret.is_now_inactive = !rec.get().req.done && !rec.get().active; //doesn't matter if inactive
		ret.retval = rec.get().req.retval;
	}
	
	private void scanCombineApply() {
		
	}

	private void set_active(){
		if(rec == null){
			rec = new ThreadLocal<Record<T>>(){
				protected Record<T> initialValue(){
					return new Record<T>();
				}
			};
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
