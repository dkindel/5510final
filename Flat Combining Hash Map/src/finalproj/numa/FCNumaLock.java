package finalproj.numa;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;


public class FCNumaLock implements MyLock{
	//for each cluster create a PL
	//for each cluster, threads will try and acquire cluster lock
	//the thread that gets the lock becomes combiner
	//the combiner will now create the local queue, and set readyrequests to 0
	//the combiner will add itself to the tail of the local queue
	//the combiner will set its globaltail flag to true
	//the combiner will now splice the local queue to global queue
	//the combiner will spin on isOwner
	
	//how to create the PL
	//array of FClock
	//how to create global queue
	//how to splice
	
	final int numClusters;
	final int delay;	
	//volatile int current_timestamp;
	
	List<AtomicReference<CombiningNodeList>> localPLQueues; 	//local publishing lists						
	
	//AtomicReference<CombiningNode> globalQueueHead;		//global MCS queue
	AtomicReference<CombiningNode> globalQueueTail;			//
	
	private ThreadLocal<CombiningNode> combining_node;
	
	//AtomicInteger FCLock[];
	
	
	FCNumaLock(int c, int d){
		numClusters = c;
		delay = d;
		//current_timestamp = 0;
		
		localPLQueues = new ArrayList<AtomicReference<CombiningNodeList>>(numClusters);
		for (int i = 0; i < numClusters; i++) {					
			localPLQueues.add(new AtomicReference<CombiningNodeList>());//(new CombiningNode()));
		}
		
		combining_node = new ThreadLocal<CombiningNode>(){
			@Override
	        protected CombiningNode initialValue() {
				return new CombiningNode();
			}
		};
		
		//FCLock = new AtomicInteger[numClusters];
		
		CombiningNode head = new CombiningNode();
		globalQueueTail = new AtomicReference<CombiningNode>(head); 
		//globalQueueTail = globalQueueHead;
	}
	
    public void lock(){
        CombiningNode comb_node = (CombiningNode)combining_node.get(); 	//thread local
        comb_node.requestReady = true;								//ready to be combined
        int cluster = ((TestThread)Thread.currentThread()).getClusterId();
        wait_until_fulfilled(comb_node , cluster);
    }

    public void unlock(){
    	CombiningNode comb_node = (CombiningNode)combining_node.get(); 	//thread local
    	if (comb_node.canBeGlobalTail == true) {
    		while (true) {
    			if (globalQueueTail.get() == comb_node) {
    				if (globalQueueTail.compareAndSet(comb_node, null) == true) {
    					// cleanup CAS succeeded
    					break;
    				}
    			} else {
    				// lock handoff
    				if (comb_node.FCnext != null) {
    					comb_node.FCnext.isOwner = true;
    					break;
    				}
    			}
    		}
    	} else {
    		// lock handoff
    		comb_node.FCnext.isOwner = true;
    	}
    }
    
    final int NUM_ROUNDS_IS_LINKED_CHECK_FREQUENCY = 100;
  
    void wait_until_fulfilled(CombiningNode comb_node, int cluster){
    	int rounds = 0;
    	AtomicReference<CombiningNodeList> localQueuePL = localPLQueues.get(cluster);	//get local queue for this cluster
    	CombiningNodeList localQueue = localQueuePL.get();								
    	
    	while (true)
        {
            // check if PR is in PL
            if ((rounds % NUM_ROUNDS_IS_LINKED_CHECK_FREQUENCY == 0) && (!comb_node.is_linked)){
                comb_node.is_linked = true;
                comb_node.requestReady = true;
                link_in_combining(comb_node,cluster);
                
            }
            if (localQueue.FC_Lock.get() == 0){
            	if (localQueue.FC_Lock.compareAndSet(0, 1)){
            		doFlatCombining(comb_node,cluster);
            		localQueue.FC_Lock.set(0);
            		while (comb_node.isOwner == false){}
            		comb_node.requestReady = false;
            	}
            }
            if (!comb_node.requestReady){
            	if(comb_node.isOwner){
            		return;
            	}
            }
        }
    }
    
    final int COMBINING_NODE_TIMEOUT = 10000;
    final int COMBINING_NODE_TIMEOUT_CHECK_FREQUENCY = 100; 
    final int MAX_COMBINING_ROUNDS = 32;
    
    private void doFlatCombining(CombiningNode comb_node, int cluster) {
    	
    	int combining_rounds = 0;
       // int num_pushed_items = 0;
        
        CombiningNode cur_comb_node = null;
        CombiningNode last_combining_node =  null;
        
        CombiningNode localTail = null;
        CombiningNode localHead = null;
        
        AtomicReference<CombiningNodeList> localQueuePL = localPLQueues.get(cluster);	//get local queue for this cluster
    	CombiningNodeList localQueue = localQueuePL.get();	
        
    	int local_current_timestamp = ++localQueue.current_timestamp;
        
        boolean check_timestamps = (local_current_timestamp % COMBINING_NODE_TIMEOUT_CHECK_FREQUENCY == 0);
        boolean have_work = false;

        while (true){
        
       // 	num_pushed_items = 0;
        	cur_comb_node = localQueue.comb_list_head;	
        	last_combining_node = cur_comb_node;			
            have_work = false;
            
            while (cur_comb_node != null){					//while i am not null
            
                if (!cur_comb_node.requestReady){		//if my request is not valid 
                
                    CombiningNode next_node = cur_comb_node.PLnext;			//next node in PL pointed by me
                    // take the node out if its not the first one
                    if ((check_timestamps) &&								//cleanup the local FC
                        (cur_comb_node != localQueue.comb_list_head) &&		//if i am not head
                        (local_current_timestamp - cur_comb_node.last_request_timestamp > COMBINING_NODE_TIMEOUT)){
                    
                        last_combining_node.PLnext = next_node;
                        cur_comb_node.is_linked = false;
                    }
                    cur_comb_node = next_node;
                    continue;
                }

                have_work = true;
                
                cur_comb_node.last_request_timestamp = local_current_timestamp;
                
                if((cur_comb_node.requestReady) && (cur_comb_node != comb_node)) {
                	if (localHead == null){
                		localHead = cur_comb_node;	
                		localTail = cur_comb_node;
                	}else{
                		localTail.FCnext = cur_comb_node;
                		localTail = cur_comb_node;
                	}
                	//cur_comb_node.last_request_timestamp = local_current_timestamp;
                	cur_comb_node.requestReady = false;
                	last_combining_node = cur_comb_node;
                    cur_comb_node = cur_comb_node.PLnext;
                }
            }
                
            combining_rounds++;

            //finished with combining
            if ((!have_work) || (combining_rounds >= MAX_COMBINING_ROUNDS)){
            	
            	//add self to end of local FC queue
            	localTail.FCnext = comb_node;
				localTail = comb_node;
				
				comb_node.canBeGlobalTail = true;
				comb_node.requestReady = false;
				
				//splice into global queue
				
				CombiningNode prevTail;// = globalQueueTail.get();				
				
				do{
					prevTail = globalQueueTail.get();
				}while (!globalQueueTail.compareAndSet(prevTail, localTail));
            	
				if(prevTail != null){
					prevTail.FCnext = localHead;
				}else{
					localHead.isOwner = true;
				}

            }
        }
    }
    
	private void link_in_combining(CombiningNode cn,int cluster){
		AtomicReference<CombiningNodeList> localQueuePL = localPLQueues.get(cluster);	//get local queue for this cluster
		//AtomicReference<CombiningNode> localQueue = localPLQueues.get(clusterID);
		CombiningNodeList localQueue = localQueuePL.get();
        while (true){
        	//get current head
        	//try adding pr to pl
        	//repeat till successful
        	
            // snapshot the list head
            CombiningNode cur_head = localQueue.comb_list_head;
            cn.PLnext = cur_head;
            
            // try to insert the node
            if (localQueue.comb_list_head == cur_head){
                if (localQueue.comb_list_head_updater.compareAndSet(this, cn.PLnext, cn)){
                    return;
                }
            }
        }
    } 

    static class CombiningNode
    {
        volatile boolean is_linked; 		// whether the PR is in the PL
        int last_request_timestamp; 		//age of PR
        CombiningNode PLnext;				//for PL
        CombiningNode FCnext;
        volatile boolean requestReady;		//to be combined
        volatile boolean isOwner;			//has the global lock and can execute CS
        volatile boolean canBeGlobalTail;
        CombiningNode()
        {
            is_linked = false;
            PLnext = null;
            FCnext = null;
            requestReady = false;
            last_request_timestamp = 0;
            isOwner = false;
            canBeGlobalTail = false;
        }
    }
  
    static class CombiningNodeList{
    	AtomicInteger FC_Lock;
    	volatile int current_timestamp;
    	//AtomicReference<CombiningNode> PLHead;
    	volatile CombiningNode comb_list_head;
        
        // For compareAndSet on the _req_list_head
        final private static AtomicReferenceFieldUpdater comb_list_head_updater =
                AtomicReferenceFieldUpdater.newUpdater(CombiningNodeList.class,CombiningNode.class, "comb_list_head");
    	CombiningNodeList(){
    		FC_Lock.set(0);
    		current_timestamp = 0;
    		//PLHead = null;
    		comb_list_head = new CombiningNode();
    	}
    }
    
    
	//public void lock(){throw new UnsupportedOperationException();}
	//public void unlock(){throw new UnsupportedOperationException();}
	public boolean enqueue(Object e){throw new UnsupportedOperationException();}
	public Object dequeue(){throw new UnsupportedOperationException();}
	public boolean _contains(Object e){throw new UnsupportedOperationException();}
	public void printqueue(){throw new UnsupportedOperationException();}
}	
