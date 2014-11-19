package map;


import java.util.concurrent.atomic.AtomicLong;

import finalproj.fchm.FCHM;

public class HM_TB {
	static final int NUM_THREADS = 4;
	
	
	public static void main(String args[]){
		FCHM<Integer, String> map= new FCHM<Integer, String>();
		
		AtomicLong throughput = new AtomicLong(0);
		HMThread[] threads = new HMThread[NUM_THREADS];
		for(int i = 0; i < NUM_THREADS; i++){
			threads[i] = new HMThread(map);
			threads[i].start();
		}
		
		for(HMThread thread : threads){
			try {
				thread.join();
			} catch (InterruptedException e) {
				System.err.println("Thread interrupted.");
				e.printStackTrace();
			}
		}
		
		System.out.println("Threads finished with: " + throughput.get());
	}
}
