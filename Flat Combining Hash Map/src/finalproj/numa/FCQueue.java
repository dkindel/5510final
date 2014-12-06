package finalproj.numa;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

public class FCQueue implements MyLock{
    // Maximum participating threads
    final int MAX_THREADS = 64;
    final int delay;
    //this is the publication record
    static class CombiningNode
    {
        volatile boolean is_linked; 		// whether the PR is in the PL
        int last_request_timestamp; 		//age of PR
        // comb_list_head CAS will perform the write for this 
        CombiningNode next;					//for PL
        volatile boolean is_request_valid;	
        // membar on item and is_consumer is committed by a write to is_request_valid
        boolean is_consumer;				// F - enq, T - deq
        Object item;						// item to be enqueued or dequeued

        CombiningNode()
        {
            is_linked = false;
            next = null;
            is_request_valid = false;
        }
    }
    
    //shared lock - all threads will perform a CAS on this
    AtomicInteger fc_lock;
    
    // used to gather combined enqueued items at combiner level
    Object[] combined_pushed_items;

    //current timestamp - count
    volatile int current_timestamp = 0;
    
    //the PR for each thread is created now
    private ThreadLocal combining_node = new ThreadLocal() 
    {
        @Override
        protected Object initialValue() {
            return new CombiningNode();
        }
    };

    //the head of the combining list - this is never deleted/cleanedup
    //this is trea
    volatile CombiningNode comb_list_head;
    
    // For compareAndSet on the _req_list_head
    final private static AtomicReferenceFieldUpdater comb_list_head_updater =
            AtomicReferenceFieldUpdater.newUpdater(FCQueue.class,CombiningNode.class, "comb_list_head");
    
    //these form the data structure D
    static class QueueFatNode
    {
        Object items[];
        int items_left;
        QueueFatNode next;
    }

    // this is the data structure D 
    volatile QueueFatNode queue_head, queue_tail;

    //constructor
    FCQueue(int c, int d)
    {
        combined_pushed_items = new Object[MAX_THREADS];
        // lock is initailized to free
        fc_lock = new AtomicInteger(0);
        //qh points to D
        queue_head = new QueueFatNode();
        queue_tail = queue_head;
        queue_head.next = null;
        queue_head.items_left = 0;
        delay = d;
    }

    final int COMBINING_NODE_TIMEOUT = 10000;
    final int COMBINING_NODE_TIMEOUT_CHECK_FREQUENCY = 100; 
    final int MAX_COMBINING_ROUNDS = 32;

    void doFlatCombining(CombiningNode combiner_thread_node)
    {
        int combining_rounds = 0;
        int num_pushed_items = 0;
        CombiningNode cur_comb_node = null;
        CombiningNode last_combining_node =  null;

        // advance timestamp and sample volatile variables to local variables for reading speed
        int local_current_timestamp = ++current_timestamp;	//increment count
        QueueFatNode local_queue_head = queue_head;			//head of queue of fat nodes

        boolean check_timestamps = (local_current_timestamp % COMBINING_NODE_TIMEOUT_CHECK_FREQUENCY == 0);
        boolean have_work = false;
        //System.out.println("fc: thread: "+ ((TestThread)Thread.currentThread()).getClusterId());
        while (true)
        {
            // initialize for a new round
            num_pushed_items = 0;
            cur_comb_node = comb_list_head;					//head of the PL
            last_combining_node = cur_comb_node;			
            have_work = false;

            while (cur_comb_node != null)
            {
                if (!cur_comb_node.is_request_valid)
                {
                    // after manipulating is_linked the owner thread can change next so we need to save it first
                    CombiningNode next_node = cur_comb_node.next;

                    // take the node out if its not the first one
                    // (we're letting the first one go to avoid CASes)
                    if ((check_timestamps) &&
                        (cur_comb_node != comb_list_head) &&
                        (local_current_timestamp - cur_comb_node.last_request_timestamp > COMBINING_NODE_TIMEOUT))
                    {
                        last_combining_node.next = next_node;
                        cur_comb_node.is_linked = false;
                    }
                    cur_comb_node = next_node;
                    continue;
                }

                have_work = true;

                // update combining node last use timestamp
                cur_comb_node.last_request_timestamp = local_current_timestamp;
                
                
                //if dequeue
                if (cur_comb_node.is_consumer)										
                {
                    boolean consumer_satisfied = false;
                    // check queue first
                    while ((local_queue_head.next != null) && !consumer_satisfied)
                    {
                        QueueFatNode head_next = local_queue_head.next;
                        if (head_next.items_left == 0)
                        {
                            local_queue_head = head_next;
                        }
                        else
                        {
                            head_next.items_left--;
                            cur_comb_node.item = head_next.items[head_next.items_left];
                            consumer_satisfied = true;
                        }
                    }
                    
                    // if queue is empty, check current pass
                    if ((!consumer_satisfied) && (num_pushed_items > 0))
                    {
                        num_pushed_items--;
                        cur_comb_node.item = combined_pushed_items[num_pushed_items];
                        consumer_satisfied = true;
                    }

                    if (!consumer_satisfied)
                    {
                        // queue empty
                        cur_comb_node.item = null;
                    }
                }
                else
                {
                    combined_pushed_items[num_pushed_items] = cur_comb_node.item;
                    num_pushed_items++;
                }

                // requesting thread is released
                cur_comb_node.is_request_valid = false;

                // next node
                last_combining_node = cur_comb_node;
                cur_comb_node = cur_comb_node.next;
            }

            // enqueue pushed items into D
            if (num_pushed_items > 0)
            {
                QueueFatNode new_node = new QueueFatNode();
                new_node.items_left = num_pushed_items;
                new_node.items = new Object[num_pushed_items];
                System.arraycopy(combined_pushed_items, 0, new_node.items, 0, num_pushed_items);
                new_node.next = null;
                queue_tail.next = new_node;
                queue_tail = new_node;//??
            }            

            combining_rounds++;

            if ((!have_work) || (combining_rounds >= MAX_COMBINING_ROUNDS))
            {
                // no more rounds needed
                // Update queue_head.
                // This membar flushes write queue so it also finalize changes made to the queue nodes
                queue_head = local_queue_head;
                
                return;
            }
        }
    }

    private void link_in_combining(CombiningNode cn)
    {
    	//System.out.println("comb: thread: "+ ((TestThread)Thread.currentThread()).getClusterId());
        while (true)
        {
        	//get current head
        	//try adding pr to pl
        	//repeat till successful
        	
            // snapshot the list head
            CombiningNode cur_head = comb_list_head;
            cn.next = cur_head;
            
            // try to insert the node
            if (comb_list_head == cur_head)
            {
                if (comb_list_head_updater.compareAndSet(this, cn.next, cn))
                {
                    return;
                }
            }
        }
    }

    final int NUM_ROUNDS_IS_LINKED_CHECK_FREQUENCY = 100;
    
    //add or create a PR and wait till the combiner writes the reply
    private void wait_until_fulfilled(CombiningNode comb_node)
    {
    	//check if pr is valid
    	//try to become combiner
    	//repeat till is_request_valid becomes false
    	
    	//initialize number of passes
    	
    	//System.out.println("wait: thread: "+ ((TestThread)Thread.currentThread()).getClusterId());
        int rounds = 0;

        while (true)
        {
            // check if PR is in PL
            if ((rounds % NUM_ROUNDS_IS_LINKED_CHECK_FREQUENCY == 0) &&
                (!comb_node.is_linked))
            {
                comb_node.is_linked = true;
                //add PR to PL
                link_in_combining(comb_node);
            }
            
            //check if the shared lock is free
            if (fc_lock.get() == 0)
            {	//the shared lock is free, try to become combiner
                if (fc_lock.compareAndSet(0, 1))
                {
                    //successfully became combiner
                	//for (int i=0;i<)
                    doFlatCombining(comb_node);
                    //release global lock
                    fc_lock.set(0);
                }
            }
            
            //PR request has been serviced
            if (!comb_node.is_request_valid)
            {
                return;
            }
            
            //increment the number of passes
            rounds++;
        }
    }

    public boolean enqueue(Object value){
        CombiningNode comb_node = (CombiningNode)combining_node.get(); 	//thread local
        comb_node.is_consumer = false;									//opcode - enqueue
        comb_node.item = value;											//data - item to be enqueued

        comb_node.is_request_valid = true;								//ready to be combined

        wait_until_fulfilled(comb_node);
        return true;													//assumes that enq has been done by the combiner
    }

    public Object dequeue(){
        CombiningNode comb_node = (CombiningNode)combining_node.get(); 	//thread local
        comb_node.is_consumer = true;        							//opcode - dequeue
        																//data - dont write the value of item	s
        comb_node.is_request_valid = true;								//ready to be combined
        
        wait_until_fulfilled(comb_node);
        return comb_node.item;											//combiner will write the value of the item
    }
    
  
	public void unlock(){
		throw new UnsupportedOperationException();
	}
	
	public boolean _contains(Object e){
		throw new UnsupportedOperationException();
	}
	
	public void printqueue(){
		QueueFatNode new1 = queue_head;
		String a = "thread 1: ";
		while(new1 != null){
			for (int i = 0; i<new1.items.length; i++){
				a = a + new1.items[i];
				
			}
			System.out.println(a);
			new1 = new1.next;
		}
	}
	
   
    public void lock(){
    	throw new UnsupportedOperationException();
    }

}

