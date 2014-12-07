package finalproj.map.locks;

import java.util.concurrent.atomic.AtomicInteger;

import finalproj.map.HMThread;

/**
 * This is a very simple lock, similar to a TAS lock but not quite.
 * If a thread cannot get a lock, it simply is rejected and boots out of the method.
 * Each thread MUST check the return status of the lock function call to see
 * if it's the lock holder or not due to this.  
 * 
 * If the lockHolder == -1, that means that nobody holds the lock.  Otherwise, it's 
 * set to the thread id.  In my use, the thread id is guaranteed to be greater than 
 * or equal to 0. 
 * @author dave
 *
 */
public class RejectLock implements Lock {
	private AtomicInteger lockholder;
	
	
	public RejectLock(){
		lockholder = new AtomicInteger(-1);
	}
	/**
	 * Lock attempts to change lock holder from -1 to the thread id.  
	 * 
	 * Since it's done in a single atomic CAS, whatever lockholder is
	 * set to is the thread id number of the thread holding the lock
	 * ...if that makes sense
	 */
	@Override
	public boolean lock() {
		int threadid = ((HMThread) Thread.currentThread()).getThreadId();
		//System.out.println("threadid: " + threadid + " and lockholder " + lockholder.get());
		return lockholder.compareAndSet(-1, threadid);
	}

	/**
	 * Only unlocks if it's the thread holding the lock attempting the unlock.  
	 */
	@Override
	public boolean unlock() {
		int threadid = ((HMThread) Thread.currentThread()).getThreadId();
		return lockholder.compareAndSet(threadid, -1);
	}
	
	/**
	 * simply return a boolean to check if the lockholder is -1
	 * @return
	 */
	public boolean isLocked(){
		return !lockholder.compareAndSet(-1, -1);
	}
	
	/**
	 * just read the hash code of the lock holder.  Used for debugging
	 * @return
	 */
	public int readHash(){
		return lockholder.hashCode();
	}

}
