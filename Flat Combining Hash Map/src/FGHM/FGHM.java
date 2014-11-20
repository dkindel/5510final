package FGHM;

import java.util.LinkedList;

import map.HM;

/**
 * Implements a fine grained hash map
 * @author dave
 */
public class FGHM<K,V> implements HM<K,V>{

	protected Object[] lock; 
	protected LinkedList<V>[] table; 
	static int largestbucket = 0;
	
	@SuppressWarnings("unchecked")
	public FGHM(int capacity) {
		table = (LinkedList<V>[]) new LinkedList[capacity]; 
		lock = new Object[capacity]; 
		for (int i = 0; i < capacity; i++) { 
			lock[i] = new Object(); 
			table[i] = new LinkedList<V>(); 
		}
	}
	 
	@Override
	public V put(K key, V val) {
		V retval = null;
		boolean resize = false;
		int keyhash = key.hashCode() % lock.length;
		synchronized(lock[keyhash]){
			int tabHash = key.hashCode() % table.length; 
			if(table[tabHash].add(val)){
				if(largestbucket < table[tabHash].size()){
					largestbucket = table[tabHash].size();
					resize = true;
				}
				retval = val;
			}
		}

		if(resize)
			resize();
		return retval;
	}

	@Override
	public V remove(K key) {
		return null;
	}

	@Override
	public V get(K key) {
		return null;
	}
	
	private void resize(){
		resize(0, table);
	}

	private void resize(int depth, LinkedList<V>[] oldTab){
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
		LinkedList<V>[] oldTable = table;
		table = (LinkedList<V>[]) new LinkedList[newCapacity];
		for (int i = 0; i < newCapacity; i++){
			table[i] = new LinkedList<V>();
		}
		largestbucket = 0;
		for (LinkedList<V> bucket : oldTable) {
			for (V x : bucket) {
				int myBucket = x.hashCode() % table.length;
				table[myBucket].add(x); 
				if(largestbucket < table[myBucket].size()){
					largestbucket = table[myBucket].size();
				}
			}
		}
	}

}
