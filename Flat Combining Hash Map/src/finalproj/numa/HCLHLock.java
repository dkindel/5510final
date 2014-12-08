/* ECE 5510 Final Project
 * Authors : Ekta Bindlish, Dave Kindel
 * File Description : HCLH Lock Implementation for NUMA Locks
 */

package finalproj.numa;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;

public class HCLHLock implements MyLock{
	
	int MAX_CLUSTERS;												
	//int delay;													
	
	List<AtomicReference<QNode>> localQueues;						
	AtomicReference<QNode> globalQueue;								
	
	ThreadLocal<QNode> currNode = new ThreadLocal<QNode>() {		
		protected QNode initialValue() { 
			return new QNode(); 
			};
		};
	
	ThreadLocal<QNode> predNode = new ThreadLocal<QNode>() {		
		protected QNode initialValue() { 
			return null; 
		};
	};
  
	public HCLHLock(int clusters) {							 
		MAX_CLUSTERS = clusters;					
		localQueues = new ArrayList<AtomicReference<QNode>>(MAX_CLUSTERS);
		for (int i = 0; i < MAX_CLUSTERS; i++) {					
			localQueues.add(new AtomicReference <QNode>());
		}
		QNode head = new QNode();
		globalQueue = new AtomicReference<QNode>(head);
		//delay = d;
	}
  
	public void Lock() {
		QNode myNode = currNode.get();								//get a new qnode
		AtomicReference<QNode> localQueue = localQueues.get(((TestThread)Thread.currentThread()).getClusterId());

		QNode myPred = null;
		do {
			myPred = localQueue.get();
		} while (!localQueue.compareAndSet(myPred, myNode));
		
		if (myPred != null) {
			boolean iOwnLock = myPred.waitForGrantOrClusterMaster();
			if (iOwnLock) {
				for(int i = 0;i<10;i++){}
				predNode.set(myPred);
				return;
			}
		}
		// I am the cluster master: splice local queue into global queue.
		
		//	System.out.println("cluster master: "+ ((TestThread)Thread.currentThread()).getThreadId());
		for(int i = 0;i<10;i++){}

		QNode localTail = null;
		do {
			myPred = globalQueue.get();
			localTail = localQueue.get();
		} while(!globalQueue.compareAndSet(myPred, localTail));
		
		// inform successor it is the new master
		localTail.setTailWhenSpliced(true);
		
		// wait for predecessor to release lock
		while (myPred.isSuccessorMustWait()) {};
		//for(int i = 0;i<10;i++){}
		// I have the lock. Save QNode just released by previous leader
		predNode.set(myPred);
		return;
	}
  
	public void UnLock() {
		QNode myNode = currNode.get();
		myNode.setSuccessorMustWait(false);
		//for(int i = 0;i<10;i++){}
		QNode node = predNode.get();
		node.unlock();
		currNode.set(node);
	}
  
  
	private static class QNode {
		// tailWhenSpliced
		private static final int TWS_MASK = 0x80000000;
		// successorMustWait
		private static final int SMW_MASK = 0x40000000;
		// clusterID
		private static final int CLUSTER_MASK = 0x3FFFFFFF;
		AtomicInteger state;
		public QNode() {
			state = new AtomicInteger(0);
		}
    
		boolean waitForGrantOrClusterMaster() {
			int myCluster = (((TestThread)Thread.currentThread()).getClusterId());
			while(true) {
				if (getClusterID() == myCluster && !isTailWhenSpliced() && !isSuccessorMustWait()) {
					return true;
				} else if (getClusterID() != myCluster || isTailWhenSpliced()) {
					return false;
				}
			}
		}
    
		public void unlock() {
			int oldState = 0;
			int newState = (((TestThread)Thread.currentThread()).getClusterId());
			// successorMustWait = true;
			newState |= SMW_MASK;
			// tailWhenSpliced = false;
			newState &= (~TWS_MASK);
			do {
				oldState = state.get();
			} while (! state.compareAndSet(oldState, newState));
		}
    
		public int getClusterID() {
			return state.get() & CLUSTER_MASK;
		}
    
		public void setClusterID(int clusterID) {
			int oldState, newState;
			do {
				oldState = state.get();
				newState = (oldState & ~CLUSTER_MASK) | clusterID;
			} while (! state.compareAndSet(oldState, newState));
		}
    
		public boolean isSuccessorMustWait() {
			return (state.get() & SMW_MASK) != 0;
		}
    
		public void setSuccessorMustWait(boolean successorMustWait) {
			int oldState, newState;
			do {
				oldState = state.get();
				if (successorMustWait) {
					newState = oldState | SMW_MASK;
				} else {
					newState = oldState & ~SMW_MASK;
				}
			} while (! state.compareAndSet(oldState, newState));
		}
    
		public boolean isTailWhenSpliced() {
			return (state.get() & TWS_MASK) != 0;
		}
    
		public void setTailWhenSpliced(boolean tailWhenSpliced) {
			int oldState, newState;
			do {
				oldState = state.get();
				if (tailWhenSpliced) {
					newState = oldState | TWS_MASK;
				} else {
					newState = oldState & ~TWS_MASK;
				}
			} while (! state.compareAndSet(oldState, newState));
		}
	} 

}
