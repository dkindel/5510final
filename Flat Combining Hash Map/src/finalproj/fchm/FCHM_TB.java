package finalproj.fchm;


import java.util.concurrent.atomic.AtomicLong;

public class FCHM_TB {
	static final int NUM_THREADS = 2;
	
	
	public static void main(String args[]){
		FCHM<Integer> map= new FCHM<Integer>();
		
		AtomicLong throughput = new AtomicLong(0);
		FCHMThread[] threads = new FCHMThread[NUM_THREADS];
		for(int i = 0; i < NUM_THREADS; i++){
			threads[i] = new FCHMThread(map);
			threads[i].start();
		}
		
		for(FCHMThread thread : threads){
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
