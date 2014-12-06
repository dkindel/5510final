/*
 * This file implements the native Java Queue : ConcurrentLinkedQueue 
*/
package finalproj.numa;

import java.util.concurrent.*;

public class NativeQueue implements MyLock{
    ConcurrentLinkedQueue<Object> nativeQueue;// = new ConcurrentLinkedQueue<String>();	
	//BlockingQueue<Object> nativeQueue;
	
    public NativeQueue(){
    	nativeQueue = new ConcurrentLinkedQueue<Object>();
    	//nativeQueue = new LinkedBlockingQueue<Object>();
    }
    
    public boolean enqueue(Object e){
    	return nativeQueue.add(e);
    }
    
    public Object dequeue(){
    	return nativeQueue.poll();
    }
    
    public boolean _contains(Object e){
    	return nativeQueue.contains(e);
    }
    
    public void lock(){
    	throw new UnsupportedOperationException();
    }
	public void unlock(){
		throw new UnsupportedOperationException();
	}
	public void printqueue(){
		throw new UnsupportedOperationException();
	}

}