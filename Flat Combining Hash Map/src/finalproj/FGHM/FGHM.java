package finalproj.FGHM;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Iterator;

import map.HM;

/**
 * Implements a fine grained hash map
 * @author dave
 */
public class FGHM<K,V> implements HM<K,V>{

	protected Object[] lock; 
	protected HashMap<K,V>[] table; 
	static int largestbucketever = 0; //This keeps track of the largest bucket
									//that has ever been in this map.  Don't need 
									//current since this is only for resizing and 
									//it will never "downsize"
	
	@SuppressWarnings("unchecked")
	public FGHM(int capacity) {
		table = (HashMap<K,V>[]) new HashMap[capacity]; 
		lock = new Object[capacity]; 
		for (int i = 0; i < capacity; i++) { 
			lock[i] = new Object(); 
			table[i] = new HashMap<K,V>(); 
		}
	}
	 
	@Override
	public V put(K key, V val) {
		V retval = null;
		boolean resize = false;
		
		int keyhash = hash(key) % lock.length;
		if(keyhash < 0) {
			System.out.println("keyhash was " + keyhash);
		}
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			retval = table[tabHash].put(key, val);
			if(largestbucketever < table[tabHash].size()){
				largestbucketever = table[tabHash].size();
				resize = true;
			}
		}

		if(resize)
			resize();
		return retval;
	}

	@Override
	public V remove(K key) {
		int keyhash = hash(key) % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			return table[tabHash].remove(key);
		}
	}

	@Override
	public V get(K key) {
		int keyhash = hash(key) % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = hash(key) % table.length; 
			return table[tabHash].get(key);
		}
	}
	
	private void resize(){
		resize(0, table);
	}

	private void resize(int depth, HashMap<K,V>[] oldTab){
		synchronized (lock[depth]) {
			if (oldTab == table){
				int next = depth + 1;
				if (next < lock.length){
					resize (next, oldTab);
				}
				else{
					sequentialResize();
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
			hash = hash*-1;
		return hash;
	}

}
