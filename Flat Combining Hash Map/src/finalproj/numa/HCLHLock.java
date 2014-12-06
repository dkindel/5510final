package finalproj.numa;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.ArrayList;
import java.util.List;

public class HCLHLock implements MyLock {
  
	int MAX_CLUSTERS;												//Max number of clusters
	int delay;														//for testing
	
	List<AtomicReference<QNode>> localQueues;						//List of local queues, one per cluster
	AtomicReference<QNode> globalQueue;								//global queue
	
	ThreadLocal<QNode> currNode = new ThreadLocal<QNode>() {		//current QNode - thread local - CLH
		protected QNode initialValue() { 
			return new QNode(); 
			};
		};
	
	ThreadLocal<QNode> predNode = new ThreadLocal<QNode>() {		//predecessor QNode - thread local CLH
		protected QNode initialValue() { 
			return null; 
		};
	};
  
	public HCLHLock(int clusters, int d) {							//constructor 
		MAX_CLUSTERS = clusters;					
		localQueues = new ArrayList<AtomicReference<QNode>>(MAX_CLUSTERS);
		for (int i = 0; i < MAX_CLUSTERS; i++) {					//add a node for each cluster
			localQueues.add(new AtomicReference <QNode>());
		}
		QNode head = new QNode();
		globalQueue = new AtomicReference<QNode>(head);
		delay = d;
	}
  
	public void lock() {
		QNode myNode = currNode.get();								//get a new qnode
		//fetch my local queue based on my cluster id
		AtomicReference<QNode> localQueue = localQueues.get(((TestThread)Thread.currentThread()).getClusterId());
		// splice my QNode into my local queue at tail
		QNode myPred = null;
		do {
			myPred = localQueue.get();
		} while (!localQueue.compareAndSet(myPred, myNode));
		
		if (myPred != null) {
			boolean iOwnLock = myPred.waitForGrantOrClusterMaster();
			if (iOwnLock) {
				// I am at head of global queue and have the lock. Save QNode just released by previous leader
				predNode.set(myPred);
				return;
			}
		}
		// I am the local cluster master.
	//	System.out.println("cluster master: "+ ((TestThread)Thread.currentThread()).getThreadId());
		if ( delay>0){for(int i = 0;i<delay;i++){}}
		
		// Splice local queue into global queue.
		QNode localTail = null;
		do {
			myPred = globalQueue.get();
			localTail = localQueue.get();
		} while(!globalQueue.compareAndSet(myPred, localTail));
		// inform successor it is the new master
		localTail.setTailWhenSpliced(true);
		// wait for predecessor to release lock
		while (myPred.isSuccessorMustWait()) {};
		// I have the lock. Save QNode just released by previous leader
		predNode.set(myPred);
		return;
	}
  
	public void unlock() {
		QNode myNode = currNode.get();
		//set my state to false
		myNode.setSuccessorMustWait(false);
		// promote pred node to current
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
	} // end QNode
  
	// superfluous declarations needed to satisfy lock interface
/*	public void lockInterruptibly() throws InterruptedException {
		throw new UnsupportedOperationException();
	}
  
	public boolean tryLock() {
		throw new UnsupportedOperationException();
	}
  
	public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
		throw new UnsupportedOperationException();
	}
  
	public Condition newCondition() {
		throw new UnsupportedOperationException();
	}
 */  
	public boolean enqueue(Object e){
		throw new UnsupportedOperationException();
	}
  
	public Object dequeue(){
		throw new UnsupportedOperationException();
	}
	
	public boolean _contains(Object e){
		throw new UnsupportedOperationException();
	}
	
	public void printqueue(){
		throw new UnsupportedOperationException();
	}
	
}
