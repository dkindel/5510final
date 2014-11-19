package map;

public interface HM<K,V> {
	public V put(K key, V val);
	public V remove(K key);
	public V get(K key);
}
