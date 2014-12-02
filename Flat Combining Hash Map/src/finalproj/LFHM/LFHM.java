package finalproj.LFHM;

import java.util.concurrent.atomic.AtomicInteger;

import finalproj.map.HM;

public class LFHM<K,V> implements HM<K, V> {
	

	private BucketList<K,V>[] bucket;
	private AtomicInteger bucketSize;
	private AtomicInteger setSize;
	
	@SuppressWarnings("unchecked")
	public LFHM(int initcap){
		bucket = (BucketList<K,V>[]) new BucketList[initcap];
		bucket[0] = new BucketList<K,V>();
		bucketSize = new AtomicInteger(2);
		setSize = new AtomicInteger(0);
	}

	@Override
	public V put(K key, V val) {
		int myBucket = hash(key) % bucketSize.get();
		BucketList<K,V> b = getBucketList(myBucket);
		if(!b.add(key, val)){
			return null;
		}
		int setSizeNow = setSize.getAndIncrement();
		int bucketSizeNow = bucketSize.get();
		if(setSizeNow/bucketSizeNow > 4)
			bucketSize.compareAndSet(bucketSizeNow, 2* bucketSizeNow);
		return val;
	}

	@Override
	public V remove(K key) {
		int myBucket = hash(key) % bucketSize.get();
		BucketList<K,V> b = getBucketList(myBucket);
		V val;
		if((val = b.remove(key)) == null){
			return null;
		}
		setSize.getAndDecrement();
		return val;
	}

	@Override
	public V get(K key) {
		int myBucket = hash(key) % bucketSize.get();
		BucketList<K,V> b = getBucketList(myBucket);
		return b.get(key);
	}

	@Override
	public void print() {
		// TODO Auto-generated method stub
		
	}
	
	private BucketList<K,V> getBucketList(int myBucket){
		if (bucket[myBucket] == null){
			initializeBucket(myBucket);
		}
		return bucket[myBucket];
	}
	
	private void initializeBucket(int myBucket){
		int parent = getParent(myBucket);
		if(bucket[parent] == null)
			initializeBucket(parent);
		BucketList<K,V> b = bucket[parent].getSentinel(myBucket);
		if(b != null){
			bucket[myBucket] = b;
		}
	}

	private int getParent(int myBucket) {
		int parent = bucketSize.get();
		do{
			parent = parent >> 1;
		} while(parent > myBucket);
		parent = myBucket - parent;
		return parent;
	}
	


	private int hash(K key){
		int hash = key.hashCode();
		if (hash < 0) 
			hash = hash*-1;
		return hash;
	}
	
}
