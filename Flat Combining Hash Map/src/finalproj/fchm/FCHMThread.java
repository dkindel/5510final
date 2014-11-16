package finalproj.fchm;

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;


public class FCHMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private FCHM<Integer, String> hashmap;
	private static CyclicBarrier bar = new CyclicBarrier(4);
	
	public FCHMThread(FCHM<Integer, String> map){
		id = ID_GEN++;
		hashmap = map;
	}

	public void run(){
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
		try {
			bar.await();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(id == 0) {
			hashmap.remove(12);
			hashmap.printList();
			hashmap.print();
		}
	}
	
	public int getThreadId(){
		return id;
	}
}
