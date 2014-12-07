package finalproj.map;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;


/**
 * The Thread class.  Runs the test case for a bit to optimize for the JIT
 * and then runs the actual tests.  
 * 
 * @author dave
 *
 */
public class HMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private HM<Integer, String> hashmap;
	private AtomicLong tput;
	private int load;
	
	//used to block at the end so printing doesn't mess with other threads operations
	private CyclicBarrier bar;
	
	/**
	 * Constructor for the HMThread.  Sets the id and other global variables for use 
	 * either between threads or alone in this one
	 * 
	 * @param map the hash map DS
	 * @param throughput used to count the throughput between threads
	 * @param bar used to block at the end of a threads operation so nothing is 
	 * 				overrun between them
	 * @param load specifies what type of load is used in testing.  possibilities are:
	 * 				90% get, 5% add, 5% remove
	 * 				34% get, 33% add, 5% remove
	 */
	public HMThread(HM<Integer, String> map, AtomicLong throughput, 
			CyclicBarrier bar, int load){
		id = ID_GEN++;
		hashmap = map;
		tput = throughput;
		this.bar = bar;
		this.load = load;
	}

	/**
	 * This method runs the tests for each and every thread and counts how many 
	 * operations are actually run
	 */
	public void run(){
		if(load == 0){

			long t= System.currentTimeMillis();
			long end = t+10000;
			Random rand = new Random();
			int count = 0;
			while(System.currentTimeMillis() < end) {
				int next = rand.nextInt();
				int op = count % 100;
				if(op < 5){
					hashmap.put(next, new Integer(next).toString());
				}
				else if(op < 10){
					hashmap.remove(next);
				}
				else{
					hashmap.get(next);
				}
				count++;
			}
			//System.out.println("Thread " + id + " has started.");


			int throughput = 0;
			//run for 5s getting measurements
			t= System.currentTimeMillis();
			end = t+5000;
			while(System.currentTimeMillis() < end) {
				int next = rand.nextInt() & 0x3FFFFFFF;
				int op = throughput % 100;
				if(op < 5){
					String val = hashmap.put(next, new Integer(next).toString());
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in put:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				else if(op < 10){
					String val = hashmap.remove(next);
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in remove:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				else{
					String val = hashmap.get(next);
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in get:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				throughput++;
			}
			long finishedIn = System.currentTimeMillis() - t;
			
			try{
				bar.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tput.addAndGet(throughput/5000);
			System.out.println("Thread " + id + " has finished for time " + finishedIn);
		}
		else{
			long t= System.currentTimeMillis();
			long end = t+10000;
			Random rand = new Random();
			int count = 0;
			while(System.currentTimeMillis() < end) {
				int next = rand.nextInt();
				int op = count % 100;
				if(op % 3 == 0){
					hashmap.put(next, new Integer(next).toString());
				}
				else if(op % 3 == 1){
					hashmap.remove(next);
				}
				else{
					hashmap.get(next);
				}
				count++;
			}
			System.out.println("Thread " + id + " has started.");


			int throughput = 0;
			//run for 5s getting measurements
			t= System.currentTimeMillis();
			end = t+5000;
			while(System.currentTimeMillis() < end) {
				int next = rand.nextInt() & 0x3FFFFFFF;
				int op = throughput % 100;
				if(op % 3 == 0){
					String val = hashmap.put(next, new Integer(next).toString());
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in put:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				else if(op % 3 == 1){
					String val = hashmap.remove(next);
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in remove:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				else{
					String val = hashmap.get(next);
					if(val != null){
						if(Integer.parseInt(val) != next)
							System.out.println("error in get:  requested: '" + next + "'. received:  '" + val + "'.");
					}
				}
				throughput++;
			}
			long finishedIn = System.currentTimeMillis() - t;
			
			try{
				bar.await();
			} catch (InterruptedException | BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			tput.addAndGet(throughput/5000);
			System.out.println("Thread " + id + " has finished for time " + finishedIn);
		}
		//the following is just an example of a small scale test used to test correctness
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
			//hashmap.remove(12);
			//hashmap.printList();
			hashmap.print();
		}
		System.out.println("done thread " + id);*/
	}
	
	/**
	 * returns the thread id for this particular thread
	 * @return the thread id
	 */
	public int getThreadId(){
		return id;
	}
}
