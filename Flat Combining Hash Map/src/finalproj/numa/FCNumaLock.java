/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : FC-NUMA Lock Implementation for NUMA Locks
 */

package finalproj.numa;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class FCNumaLock implements MyLock{
	
	final int numClusters;
	volatile int current_timestamp;
	
	List<AtomicReference<FCQueue>>localFCQueues;
	
	AtomicReference<FCNode> globalQueueTail;
	
    //AtomicLong[] FCLock;
	
	private ThreadLocal myFCNode = new ThreadLocal() 
    {
        @Override
        protected Object initialValue() {
            return new FCNode();
        }
    };
    
	public FCNumaLock(int c){
		numClusters = c;
		current_timestamp = 0;
		
//		localPLQueues = new ArrayList<AtomicReference<FCNode>>(c);
//		for (int i = 0; i < c; i++){
//			localPLQueues.add(new AtomicReference <FCNode>());
//		}
		
		localFCQueues = new ArrayList<AtomicReference<FCQueue>>(c);
		for (int i = 0; i < c; i++){
			FCQueue temp = new FCQueue();
			localFCQueues.add(new AtomicReference <FCQueue>(temp));
		}
		
		FCNode temp = new FCNode();
//		globalQueueHead = new AtomicReference<FCNode>(temp);
		globalQueueTail = new AtomicReference<FCNode>(null);
	}

	public void Lock(){
		//FCNode myNode = (FCNode)myFCNode.get();
		
		FCNode MyFCNode = (FCNode)myFCNode.get();
		MyFCNode.canBeGlobalTail = false;
		//MyFCNode.isOwner = false;
		MyFCNode.isOwner.set(false);
		MyFCNode.requestReady = true;
		
		int myCluster = ((TestThread)Thread.currentThread()).getClusterId();
		int ThresHold = 1000;
		AtomicReference<FCQueue> myFCQ = localFCQueues.get(myCluster);
		FCQueue myFCQueue = myFCQ.get();
		
		//FCNode localHead = myFCQueue.LocalHead;
		//FCNode localTail = myFCQueue.LocalTail;
		
		while(true){
			if(!MyFCNode.isActive){
				//insert my item in local pl
				MyFCNode.isActive = true;
				MyFCNode.requestReady = true;
				AddNodeToPL(MyFCNode,myCluster);
				//System.out.println("added thread: "+ ((TestThread)Thread.currentThread()).getThreadId() );
			}
			
			if(myFCQueue.FCLock.get() == 0){
				if (myFCQueue.FCLock.compareAndSet(0, 1)){
					if(MyFCNode.requestReady){
						//for(int k = 0; k<10000;k++){}
						//System.out.println("combiner thread: "+ ((TestThread)Thread.currentThread()).getThreadId() );
						myFCQueue.count++; //increment passing count
						for (int i = 0; i<32 ; i++){ //for max combining iterations
							FCNode temp = (localFCQueues.get(myCluster).get()).FCHead.get();//get local pl
							FCNode tempPred = temp;
							boolean flag = false;
							while (temp!= null){
								if((temp.requestReady) && (temp != MyFCNode)){
									if (myFCQueue.LocalHead == null){
										myFCQueue.LocalHead = temp;
										myFCQueue.LocalTail = temp;
										temp.GLNext = null; //the first node in the localFClist
									}else{
										//System.out.println("combiner thread: "+ ((TestThread)Thread.currentThread()).getThreadId() );
										myFCQueue.LocalTail.GLNext = temp;
										myFCQueue.LocalTail = temp;
									}
									
									temp.age = myFCQueue.count;
									temp.requestReady = false;
								}else{
									if(myFCQueue.count - temp.age > ThresHold){
										if ((temp != MyFCNode))// & (temp != myFCQueue.FCHead))
										{
											//System.out.println("cleaner thread: "+ myFCNode +((TestThread)Thread.currentThread()).getThreadId() + " : "+myFCQueue.count+" : " + temp+ temp.age);
											temp.isActive = false;
											tempPred = temp.PLNext;
											flag = true;
										}
									}
								}
								if (!flag)tempPred = temp;
								else flag = false;
								temp = temp.PLNext;
							}
						}
						if (myFCQueue.LocalHead == null){
							myFCQueue.LocalHead = MyFCNode;
							myFCQueue.LocalTail = MyFCNode;
						}else{
							myFCQueue.LocalTail.GLNext = MyFCNode;
							myFCQueue.LocalTail = MyFCNode;
						}
						
						MyFCNode.canBeGlobalTail = true;
						MyFCNode.requestReady = false;
						
						FCNode prevTail = globalQueueTail.getAndSet(myFCQueue.LocalTail);
						if(prevTail != null){
							prevTail.canBeGlobalTail = false;
							prevTail.GLNext = myFCQueue.LocalHead;
						}else{
							//myFCQueue.LocalHead.isOwner = true;
							//combiner is local and global head in a lot of cases	
							((localFCQueues.get(myCluster)).get()).LocalHead.isOwner.set(true);
							//myFCQueue.LocalHead.isOwner.set(true);// = true;
						}
					}
					
					myFCQueue.FCLock.set(0);
				}
			}
			if (MyFCNode.requestReady == false){
				//System.out.println("ready thread: "+ ((TestThread)Thread.currentThread()).getThreadId() );
				break;
			}
		}
		while(!MyFCNode.isOwner.get()){}//{if (!MyFCNode.isActive) {AddNodeToPL(MyFCNode,myCluster);}}
		return;
	}
		
	private void AddNodeToPL(FCNode myNode, int myCluster){
		
		AtomicReference<FCQueue> myFCQ = localFCQueues.get(myCluster);
		FCQueue myFCQueue = myFCQ.get();
		
		while(true){
			//FCNode Curr_Head = CurHead.get();
			FCNode Curr_Head = myFCQueue.FCHead.get();
			/*while(Curr_Head != null){
				if(Curr_Head == myNode){
					//node already exists
					Curr_Head.isActive = true;
					Curr_Head.requestReady = true;
					return;
				}
				Curr_Head = Curr_Head.PLNext;
			}*/
			//need to create new node in the 
			Curr_Head = myFCQueue.FCHead.get();
			myNode.PLNext = Curr_Head;
			if (Curr_Head  == myFCQueue.FCHead.get()){
				if(myFCQueue.FCHead.compareAndSet(myNode.PLNext, myNode))
					return;
				//if (CurHead.compareAndSet(myNode.PLNext, myNode)){
				//if (myFCQueue.FCHead_updater.compareAndSet(myFCQueue, myNode.PLNext, myNode))
				//	return;
				}
			}
		}
	
    public void UnLock(){
    	FCNode myNode = (FCNode)myFCNode.get();
    	
    	if (myNode.canBeGlobalTail){
    		while(true){
    			if (globalQueueTail.get() == myNode){
    				//System.out.println("1 t: "+ ((TestThread)Thread.currentThread()).getThreadId());
    				if(globalQueueTail.compareAndSet(myNode,null)){
    					myNode.isOwner.set(false);
    					myNode.canBeGlobalTail = false;
    					myNode.GLNext = null;
    					break;
    				}
    			}else{
    				if(myNode.GLNext != null){
    					//System.out.println("2 t: "+ ((TestThread)Thread.currentThread()).getThreadId());
    					myNode.GLNext.isOwner.set(true);// = true;
    					myNode.isOwner.set(false);
    					myNode.canBeGlobalTail = false;
    					myNode.GLNext = null;
    					break;
    				}
    			}
    		}
    	}else{
    		//System.out.println("3 t: "+ ((TestThread)Thread.currentThread()).getThreadId());
    		myNode.GLNext.isOwner.set(true);// = true;
    		myNode.isOwner.set(false);
    		myNode.GLNext = null;
    	}
    	return;
    }
    
    private class FCNode{
    	FCNode PLNext;
    	FCNode GLNext;
    	boolean isActive;
    	AtomicBoolean isOwner;
    	boolean requestReady;
    	int age;
    	boolean canBeGlobalTail;
    	
    	public FCNode(){
    		PLNext = null;
    		GLNext = null;
    		isOwner = new AtomicBoolean(false);
    		isActive = false;
    		requestReady = false;
    		age = 0;
    	}
    }
    
    private class FCQueue{
    	int count;
    	
    	AtomicInteger FCLock;
    	//volatile FCNode FCHead;
    	AtomicReference<FCNode> FCHead;
    	
    	FCNode LocalHead;
    	FCNode LocalTail;
    	
    	//final private AtomicReferenceFieldUpdater FCHead_updater = 
        //        AtomicReferenceFieldUpdater.newUpdater(FCQueue.class,FCNode.class, "FCHead");
    	   	
    	public FCQueue(){
    		count = 0;
    		FCLock = new AtomicInteger();
    		//FCHead = null;
    		FCHead = new AtomicReference<FCNode>(null);
    		
    		LocalHead = null;
    		LocalTail = null;
    		    	}
    }
    
 }
