package finalproj.fchm;

import finalproj.map.HM;

public class FCHM<K,V> implements HM<K,V> {
	private int capacity = 0;
	
	private SubFCHM<K,V>[] map;
	
	@SuppressWarnings("unchecked")
	public FCHM(int initialcapacity){
		capacity = initialcapacity;
		map = (SubFCHM<K, V>[]) new SubFCHM[initialcapacity];
		for(int i = 0; i < initialcapacity; i++){
			map[i] = new SubFCHM<K,V>(initialcapacity);
		}
	}

	@Override
	public V put(K key, V val) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].put(key, val);
	}

	@Override
	public V remove(K key) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].remove(key);
	}

	@Override
	public V get(K key) {
		int keyhash = hash(key) % capacity;
		return map[keyhash].get(key);
	}

	@Override
	public void print() {
		for(int i = 0; i < map.length; i++){
			map[i].print();
		}
	}

	private int hash(K key){
		int hash = key.hashCode();
		if (hash < 0) 
			hash = hash*-1;
		return hash;
	}
}
