package finalproj.numa;

import javax.swing.Timer;
import java.util.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class TestThread extends Thread{
	private static int ID_GEN = 0;
	private int id;
	private int cluster_id;
	private MyLock lock;
	private boolean timer10;
	private long count;
	private long count_enq;
	private long count_deq;
	private long count_cnt;
	
	public TestThread(int cluster, MyLock Lock) {
		id = ID_GEN++;
		cluster_id = cluster;
		lock = Lock;
		timer10 = false;
		count = 0;
		count_enq = 0;
		count_deq = 0;
		count_cnt = 0;
	}
	
	public int getThreadId(){
		return id;
	}
	
	public int getClusterId(){
		return cluster_id;
	}
	
	@Override
	public void run() {

		  ActionListener al1 = new ActionListener() {
			  @Override 
			  public void actionPerformed(ActionEvent event) {
				  timer10 = true;
				  count = 0;
				  count_enq = 0;
				  count_deq = 0;
				  count_cnt = 0;
			  }
		  };
		  
		  
		  ActionListener al2 = new ActionListener() {
			  @Override 
			  public void actionPerformed(ActionEvent event) {
				  timer10 = false;
				  //System.out.println("thread id: "+id+" execution count : "+ (count_enq+count_deq));
				  System.out.println("thread id: "+id+" execution count : "+ count);//(count_enq+count_deq));
			  }
		  };
		  
		  Timer timer1 = new Timer(10000, al1);
		  timer1.setRepeats(false);
		  Timer timer2 = new Timer(20000, al2);	
		  timer2.setRepeats(false);
		  timer1.start();
		  timer2.start();
		
//		  Random r_n = new Random();
//		  int rand_i = 0;
		while(true){
			
			lock.lock();
			try{
				count++;
			}finally{
				lock.unlock();
			}
			/*
			rand_i= r_n.nextInt();
			
			if (rand_i%2 == 0){
				lock.enqueue(rand_i);
				count_enq++;
			}else{
				lock.dequeue();
				count_deq++;
			}*/
			
		}
	}
}
