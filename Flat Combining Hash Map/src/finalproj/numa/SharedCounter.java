/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : Shared Counter Incremented by the CS for NUMA Locks
 */

package finalproj.numa;

public class SharedCounter {

	private int value;
	
	public SharedCounter(){
		value = 0;
	}
	
	public int getAndIncrement(){
		int temp = value;
		value = temp + 1;
		return temp;
	}
}
