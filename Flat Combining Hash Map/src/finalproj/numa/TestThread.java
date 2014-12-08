/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : Thread Execution for NUMA Locks
 */

package finalproj.numa;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.BrokenBarrierException;

import javax.swing.Timer;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestThread extends Thread{
	public static int ID_GEN = 0;
	
	private int id;
	private int clusterID;
	private MyLock mylock;
	private boolean timer5;
	private int CSType;
	private int count;
	private SharedCounter Counter;
	
	private AtomicLong throughput;
	private CyclicBarrier barrier;
	
	public TestThread(int clusterid, MyLock lockid, int cs, CyclicBarrier bar, AtomicLong l, SharedCounter c){
		id = ID_GEN++;
		clusterID = clusterid;
		this.mylock = lockid;
		timer5 = true;
		CSType = cs;
		throughput = l;
		this.barrier = bar;
		count = 0;
		this.Counter = c;
	}
	
	public int getThreadId(){
		return id;
	}
	
	public int getClusterId(){
		return clusterID;
	}
	
	@Override
	public void run() {
		
		ActionListener al1 = new ActionListener() {
			  @Override 
			  public void actionPerformed(ActionEvent event) {
				  count = 0;
			  }
		  };
		  
		  ActionListener al2 = new ActionListener() {
			  @Override 
			  public void actionPerformed(ActionEvent event) {
				  timer5 = false;
				  System.out.println("thread id: "+id+" execution count : "+ count);
				  //System.out.println(count);//(count_enq+count_deq));
			  }
		  };
		
		  Timer timer1 = new Timer(10000, al1);
		  timer1.setRepeats(false);
		  Timer timer2 = new Timer(5000, al2);	
		  timer2.setRepeats(false);
		  timer1.start();
		  timer2.start();

		if (CSType == 0){
			while(true){
				if(!timer5) break;
				mylock.Lock();
				try{
					
					//empty CS
				}finally{
					mylock.UnLock();
				}
				count++;
			}
		}else if (CSType == 1){
			while(true){
				if(!timer5) break;
				mylock.Lock();
				try{
					//increment counter inside CS;
					Counter.getAndIncrement();
				}finally{
					mylock.UnLock();
				}
				count++;
			}
		}else{
			while(true){
				if(!timer5) break;
				mylock.Lock();
				try{
					//delay inside CS
					for(int i = 0; i < 100 ; i++){}
				}finally{
					mylock.UnLock();
				}
				count++;
			}

		}
		
		try{
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
		
		throughput.addAndGet(count/5000);
		//System.out.println("Thread " + id + " has finished");
		
	}
	
}
