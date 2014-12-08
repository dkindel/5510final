/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : Main Function for NUMA Locks. Refer to README for more details
 */

package finalproj.numa;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

public class MainFunction {
	
	private static final int THREAD_COUNT   = 8;
	private static final int CLUSTER_COUNT  = 2;
	private static final int LOCK_TYPE_NUMA	= 10;
	private static final int EMPTY_CS		= 0;
	
	public static void main(String args[]){
		int lockType 	= (args.length ==0 ? LOCK_TYPE_NUMA : Integer.parseInt(args[0]));
		int numThreads 	= (args.length < 2 ? THREAD_COUNT 	: Integer.parseInt(args[1]));
		int numClusters = (args.length < 3 ? CLUSTER_COUNT 	: Integer.parseInt(args[2]));
		int CSType		= (args.length < 3 ? EMPTY_CS 		: Integer.parseInt(args[3]));
		
		
		
		String locktype;
		String cstype;
		
		int threadsPerCluster = numThreads/numClusters;	
		
		if (lockType == 10){
			locktype = "Running a NUMA Lock";
		}else if (lockType == 11){
			locktype = "Running a HCLH lock";
		}else{
			locktype = "Running a Java Native Lock";
		}
		
		if (CSType == 0){
			cstype = "Critical Section is Empty";
		}else if (CSType == 1){
			cstype = "Critical Section is a shared counter increment";
		}else {
			cstype = "Critical Section is a 5000ms delay";
		}
		
		System.out.println("Algorithm: NUMA Locks");
		System.out.println("Running with "+numThreads + " threads");
		System.out.println("Running with "+numClusters + " clusters");
		System.out.println("Running with "+ cstype);
		System.out.println(locktype);
		
		MyLock tryLock;
		if (lockType == 10){
			tryLock = new FCNumaLock(numClusters);
		}else if(lockType == 11){
			tryLock = new HCLHLock(numClusters);
		}else{
			tryLock = new NativeLock(numClusters);
		}
		
		SharedCounter Counter = new SharedCounter();
		AtomicLong throughput = new AtomicLong(0);
		CyclicBarrier barrier = new CyclicBarrier(numThreads);
		
		TestThread[] threads = new TestThread[numThreads]; 
		int threadid = 0;
		for (int i = 0; i<numClusters;i++){
			for(int t=0; t<threadsPerCluster; t++){
				threads[threadid] = new TestThread(i,(MyLock)tryLock,CSType,barrier,throughput,Counter);
				threads[threadid].start();
				threadid++;
				//new TestThread((t),(MyLock)tryLock,CSType,bar,throughput ).start();
		    }	
		}
		
		for(TestThread thread : threads){
			try{
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Thread interrupted.");
				e.printStackTrace();
			}
		}
		
		System.out.println("Threads finished with: " + throughput.get());
		System.out.println();
			
	}

}
