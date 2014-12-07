package finalproj.map;

/**
 * General interface for Hash Maps.  They need at least these 4 functions
 * @author dave
 *
 * @param <K> The type for the key
 * @param <V> The type for the value
 */
public interface HM<K,V> {
	public V put(K key, V val);
	public V remove(K key);
	public V get(K key);
	public void print();
}
