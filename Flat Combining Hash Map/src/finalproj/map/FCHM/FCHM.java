package finalproj.map.FCHM;

import finalproj.map.HM;

/**
 * The Flat Combining implementation class
 * @author dave
 *
 * @param <K> the key type 
 * @param <V> the value type
 */
public class FCHM<K,V> implements HM<K,V> {
	private int capacity = 0;
	
	//This is an array of flat combining implementations
	//This is used because it decreases general contention
	//on the data structure
	private SubFCHM<K,V>[] map;
	
	/**
	 * Constructor.  This specifies the initial capacity and creates all of the 
	 * sub fchms to the same capacity.  
	 * @param initialcapacity
	 */
	@SuppressWarnings("unchecked")
	public FCHM(int initialcapacity){
		capacity = initialcapacity;
		map = (SubFCHM<K, V>[]) new SubFCHM[initialcapacity];
		for(int i = 0; i < initialcapacity; i++){
			map[i] = new SubFCHM<K,V>(initialcapacity);
		}
	}

	/**
	 * Puts the key and value pair into the hash map
	 */
	@Override
	public V put(K key, V val) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].put(key, val);
	}

	/**
	 * removes the key and value associated with the provided key
	 */
	@Override
	public V remove(K key) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].remove(key);
	}

	/**
	 * returns the value associated with the key from the hashmap
	 */
	@Override
	public V get(K key) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].get(key);
	}

	/**
	 * prints out the entire hashmap
	 */
	@Override
	public void print() {
		for(int i = 0; i < map.length; i++){
			map[i].print();
		}
	}

	/**
	 * hash function for the key.  
	 * @param key the key to hash for
	 * @return the associated hash value
	 */
	private int hash(K key){
		int hash = key.hashCode();
		if (hash < 0) 
			hash = hash*-1;
		return hash;
	}
}
