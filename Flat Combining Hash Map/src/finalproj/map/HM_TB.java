package finalproj.map;


import java.util.concurrent.atomic.AtomicLong;
import finalproj.FGHM.FGHM;
import finalproj.LFHM.LFHM;

import finalproj.fchm.FCHM;

public class HM_TB {
	
	public static void main(String args[]){
		int NUM_THREADS = Integer.parseInt(args[0]);
		int map_type = Integer.parseInt(args[1]);
		int init_capacity = Integer.parseInt(args[2]);
		System.out.println("Running with " + NUM_THREADS + " threads.");
		System.out.println("Running with " + init_capacity + " capacity.");
		
		
		HM<Integer, String> map;
		if(map_type == 0){
			System.out.println("Running a Flat-Combining map");
			map = new FCHM<Integer, String>(init_capacity);
		}
		else if (map_type == 1){
			System.out.println("Running a Fine Grained map");
			map= new FGHM<Integer, String>(init_capacity);
		}
		else{
			System.out.println("Running a Lock Free map");
			map= new LFHM<Integer, String>(init_capacity);
		}
			
		
		//FCHM<Integer, String> map= new FCHM<Integer, String>(50);
		//FGHM<Integer, String> map= new FGHM<Integer, String>(50);
		
		AtomicLong throughput = new AtomicLong(0);
		HMThread[] threads = new HMThread[NUM_THREADS];
		long start = System.currentTimeMillis();
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
		long end = System.currentTimeMillis();
		
		System.out.println("Threads finished with: " + throughput.get() + " in " + (end - start));
	}
}
