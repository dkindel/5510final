package finalproj.map.CHM;

import java.util.concurrent.ConcurrentHashMap;

import finalproj.map.HM;


/**
 * This is just a wrapper for the java std concurrent hash map
 * 
 * The reason for this is that we need it to implement my custom HM interface
 * @author dave
 *
 * @param <K> the key type
 * @param <V> the value type
 */
public class CHM<K,V> implements HM<K, V> {

	private ConcurrentHashMap<K,V> map;
	
	public CHM(int cap){
		map = new ConcurrentHashMap<K,V>(cap);
	}
	
	
	@Override
	public V put(K key, V value) {
		return map.put(key, value);
	}

	@Override
	public V remove(K key) {
		return map.remove(key);
	}

	@Override
	public V get(K key) {
		return map.get(key);
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}

}
