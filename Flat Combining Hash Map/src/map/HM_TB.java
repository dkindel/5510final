package map;


import java.util.concurrent.atomic.AtomicLong;
import finalproj.FGHM.FGHM;

import finalproj.fchm.FCHM;

public class HM_TB {
	
	public static void main(String args[]){
		int NUM_THREADS = Integer.parseInt(args[0]);
		
		FCHM<Integer, String> map= new FCHM<Integer, String>(50);
		//FGHM<Integer, String> map= new FGHM<Integer, String>(50);
		
		AtomicLong throughput = new AtomicLong(0);
		HMThread[] threads = new HMThread[NUM_THREADS];
		for(int i = 0; i < NUM_THREADS; i++){
			threads[i] = new HMThread(map, throughput);
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
