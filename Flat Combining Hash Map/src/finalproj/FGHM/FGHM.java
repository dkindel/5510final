package finalproj.FGHM;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;

import finalproj.map.HM;


/**
 * Implements a fine grained hash map
 * @author dave
 */
public class FGHM<K,V> implements HM<K,V>{

	protected ReentrantLock[] lock; 
	protected HashMap<K,V>[] table; 
	private static int largestbucketever = 0; //This keeps track of the largest bucket
									//that has ever been in this map.  Don't need 
									//current since this is only for resizing and 
									//it will never "downsize"
	
	@SuppressWarnings("unchecked")
	public FGHM(int capacity) {
		table = (HashMap<K,V>[]) new HashMap[capacity]; 
		lock = new ReentrantLock[capacity]; 
		for (int i = 0; i < capacity; i++) { 
			lock[i] = new ReentrantLock(); 
			table[i] = new HashMap<K,V>(); 
		}
	}
	 
	@Override
	public V put(K key, V val) {
		V retval = null;
		
		int keyhash = hash(key) % lock.length;
		if(keyhash < 0) {
			System.out.println("keyhash was " + keyhash);
		}
		lock[keyhash].lock();
		try{
			int tabHash = hash(key) % table.length; 
			retval = table[tabHash].put(key, val);
			if(largestbucketever < table[tabHash].size()){
				largestbucketever = table[tabHash].size();
			}
		}finally{
			lock[keyhash].unlock();
		}

		if(largestbucketever > 20)
			resize();
		return retval;
	}

	@Override
	public V remove(K key) {
		int keyhash = hash(key) % lock.length;
		lock[keyhash].lock();
		try{
			int tabHash = hash(key) % table.length; 
			return table[tabHash].remove(key);
		}finally{
			lock[keyhash].unlock();
		}
	}

	@Override
	public V get(K key) {
		int keyhash = hash(key) % lock.length;
		lock[keyhash].lock();
		try{
			int tabHash = hash(key) % table.length; 
			return table[tabHash].get(key);
		}finally{
			lock[keyhash].unlock();
		}
	}
	
	private void resize(){
		resize(0, table);
	}

	private void resize(int depth, HashMap<K,V>[] oldTab){
		lock[depth].lock();
		try{
			if (oldTab == table){
				int next = depth + 1;
				if (next < lock.length){
					resize (next, oldTab);
				}
				else{
					sequentialResize();
				}
			}
		}finally{
			lock[depth].unlock();
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
			hash = hash*-1;
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
