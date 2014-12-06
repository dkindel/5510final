package finalproj.map.FGHM;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import finalproj.map.HMThread;
//import java.util.concurrent.locks.ReentrantLock;

import finalproj.map.HM;


/**
 * Implements a fine grained hash map
 * @author dave
 */
public class FGHM<K,V> implements HM<K,V>{

	protected Object[] lock; 
	protected HashMap<K,V>[] table; 
	private static int largestbucketever = 0; //This keeps track of the largest bucket
									//that has ever been in this map.  Don't need 
									//current since this is only for resizing and 
									//it will never "downsize"
	
	@SuppressWarnings("unchecked")
	public FGHM(int capacity) {
		table = (HashMap<K,V>[]) new HashMap[capacity]; 
		lock = new Object[capacity]; 
		for (int i = 0; i < capacity; i++) { 
			lock[i] = new Object(); 
			table[i] = new HashMap<K,V>(capacity); 
		}
	}
	 
	@Override
	public V put(K key, V val) {
		V retval = null;
		int keyhash = hash(key) % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			long t = System.currentTimeMillis();
			retval = table[tabHash].put(key, val);
			/*if(largestbucketever < table[tabHash].size()){
				largestbucketever = table[tabHash].size();
			}*/
			/*long time;
			if((time = System.currentTimeMillis()) > t+2000){
				System.out.println("error in put! Thread " + ((HMThread)Thread.currentThread()).getThreadId() 
						+ " went over by " + (time - t) + " in bucket " + keyhash);
			}*/
			return retval;
		}

		/*if(largestbucketever > 500)
			resize();*/
	}

	@Override
	public V remove(K key) {
		int keyhash = hash(key) % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			long t = System.currentTimeMillis();
			V val = table[tabHash].remove(key);
			/*long time;
			if((time = System.currentTimeMillis()) > t+2000){
				System.out.println("error in remove! Thread " + ((HMThread)Thread.currentThread()).getThreadId() 
						+ " went over by " + (time - t) + " on key " + keyhash);			
			}*/
			return val;
		}
	}

	@Override
	public V get(K key) {
		int keyhash = hash(key) % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			long t = System.currentTimeMillis();
			V val = table[tabHash].get(key);
			/*long time;
			if((time = System.currentTimeMillis()) > t+2000){
				System.out.println("error in get! Thread " + ((HMThread)Thread.currentThread()).getThreadId() 
						+ " went over by " + (time - t) + " in bucket " + keyhash);
			}*/
			return val;
		}
	}
	
	private void resize(){
		resize(0, table);
	}

	private void resize(int depth, HashMap<K,V>[] oldTab){
		synchronized(lock[depth]){
			if (oldTab == table){
				int next = depth + 1;
				if (next < lock.length){
					resize (next, oldTab);
				}
				else{
					System.out.println("resize start");
					long start = System.currentTimeMillis();
					sequentialResize();
					System.out.println("resize end for " + (System.currentTimeMillis() - start));
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	private void sequentialResize() {
		int oldCapacity = table.length;
		int newCapacity = 2 * oldCapacity;
		HashMap<K,V>[] oldTable = table;
		table = (HashMap<K,V>[]) new HashMap[newCapacity];
		for (int i = 0; i < newCapacity; i++){
			table[i] = new HashMap<K,V>();
		}
		largestbucketever = 0;
		for (HashMap<K,V> bucket : oldTable) {
			Iterator<Entry<K, V>> it = bucket.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<K,V> pairs = it.next();

				int myBucket = hash(pairs.getKey()) % table.length;
				table[myBucket].put(pairs.getKey(), pairs.getValue()); 
				if(largestbucketever < table[myBucket].size()){
					largestbucketever = table[myBucket].size();
				}
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		}
	}
	
	private int hash(K key){
		int hash = key.hashCode();
		if (hash < 0) 
			hash *= -1;
		return hash;
	}
	
	public void print(){
		for (HashMap<K,V> bucket : table) {
			Iterator<Entry<K, V>> it = bucket.entrySet().iterator();
		    while (it.hasNext()) {
		        Map.Entry<K,V> pairs = it.next();
		        System.out.println("Key: " + pairs.getKey() + ". Val: " + pairs.getValue());
		        it.remove(); // avoids a ConcurrentModificationException
		    }
		}
	}

}
