/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : Java Native Lock implementation for NUMA Locks
 */

package finalproj.numa;

import java.util.concurrent.locks.*;

public class NativeLock implements MyLock{
	
	Lock Nlock;
	
	public NativeLock(int c){
		Nlock = new ReentrantLock();
	}
	
	public void Lock(){
		Nlock.lock();
		return;
	}
	
	public void UnLock(){
		Nlock.unlock();
		return;
	}
}
