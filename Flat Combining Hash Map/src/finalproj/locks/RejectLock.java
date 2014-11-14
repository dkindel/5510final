package finalproj.locks;

import java.util.concurrent.atomic.AtomicInteger;
import finalproj.fchm.FCHMThread;

public class RejectLock implements Lock {
	private AtomicInteger lockholder = new AtomicInteger(-1);
	
	/**
	 * Lock attempts to change lock holder from -1 to the thread id.  
	 * 
	 * Since it's done in a single atomic CAS, whatever lockholder is
	 * set to is the thread id number of the thread holding the lock
	 * ...if that makes sense
	 */
	@Override
	public boolean lock() {
		int threadid = ((FCHMThread) Thread.currentThread()).getThreadId();
		return lockholder.compareAndSet(-1, threadid);
	}

	/**
	 * Only unlocks if it's the thread holding the lock attempting the unlock.  
	 */
	@Override
	public boolean unlock() {
		int threadid = ((FCHMThread) Thread.currentThread()).getThreadId();
		return lockholder.compareAndSet(threadid, -1);
	}

}
