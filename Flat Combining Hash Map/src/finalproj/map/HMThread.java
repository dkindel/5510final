package finalproj.map;

import java.util.Random;
/*import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;*/
import java.util.concurrent.atomic.AtomicLong;



public class HMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private HM<Integer, String> hashmap;
	AtomicLong tput;
	
	//private static CyclicBarrier bar = new CyclicBarrier(4);
	
	public HMThread(HM<Integer, String> map, AtomicLong throughput){
		id = ID_GEN++;
		hashmap = map;
		tput = throughput;
	}

	public void run(){
		
		long t= System.currentTimeMillis();
		long end = t+5000;
		Random rand = new Random();
		int count = 0;
		while(System.currentTimeMillis() < end) {
			int next = rand.nextInt();
			int op = count % 100;
			if(op < 10){
				hashmap.put(next, new Integer(next).toString());
			}
			else if(op == 10){
				hashmap.remove(next);
			}
			else{
				hashmap.get(next);
			}
			count++;
		}
		System.out.println("Thread " + id + " has started.");


		int throughput = 0;
		//run for 2s getting measurements
		t= System.currentTimeMillis();
		end = t+2000;
		while(System.currentTimeMillis() < end) {
			int next = rand.nextInt();
			int op = throughput % 100;
			if(op < 10){
				hashmap.put(next, new Integer(next).toString());
			}
			else if(op == 10){
				hashmap.remove(next);
			}
			else{
				hashmap.get(next);
			}
			throughput++;
		}
		
		tput.addAndGet(throughput);
		
		System.out.println("Thread " + id + " has finished.");
		
/*
		System.out.println("starting thread " + id);
		if(id == 0){
			hashmap.put(0, "Zero");
			hashmap.put(1, "One");
			hashmap.put(2, "Two");
			hashmap.put(3, "Three");
		}
		else if(id == 1){
			hashmap.put(4, "Four");
			hashmap.put(5, "Five");
			hashmap.put(6, "Six");
			hashmap.put(7, "Seven");
		}
		else if(id == 2){
			hashmap.put(208, "Two Hundred Eight");
			hashmap.put(909, "Nine Hundred and Nine");
			hashmap.put(1000, "One Thousand");
			hashmap.put(67, "Sixty Seven");
		}
		else if(id == 3){
			hashmap.put(89, "Eighty Nine");
			hashmap.put(2, "Two");
			hashmap.put(12, "Twelve");
			hashmap.put(59, "Fifty Nine");
		}
		try{
			bar.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(id == 0) {
			hashmap.remove(12);
			//hashmap.printList();
			hashmap.print();
		}
		System.out.println("done thread " + id);*/
	}
	
	public int getThreadId(){
		return id;
	}
}
