package finalproj.map;


import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;

import finalproj.map.CHM.CHM;
import finalproj.map.FCHM.FCHM;
import finalproj.map.FGHM.FGHM;
import finalproj.map.LFHM.LFHM;
import finalproj.map.NBHM.NonBlockingHashMap;


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
		else if (map_type == 2){
			System.out.println("Running ConcurrentHashMap");
			map= new CHM<Integer, String>(init_capacity);
		}
		else if (map_type == 3){
			System.out.println("Running a Non Blocking map (by Cliff Click)");
			map= new NonBlockingHashMap<Integer, String>(init_capacity);
		}
		else{
			System.out.println("Running a Lock Free map (Split Order)");
			map= new LFHM<Integer, String>(init_capacity);
		}
			
		
		//FCHM<Integer, String> map= new FCHM<Integer, String>(50);
		//FGHM<Integer, String> map= new FGHM<Integer, String>(50);
		
		AtomicLong throughput = new AtomicLong(0);
		CyclicBarrier bar = new CyclicBarrier(NUM_THREADS);
		
		HMThread[] threads = new HMThread[NUM_THREADS];
		long start = System.currentTimeMillis();
		for(int i = 0; i < NUM_THREADS; i++){
			threads[i] = new HMThread(map, throughput, bar);
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
