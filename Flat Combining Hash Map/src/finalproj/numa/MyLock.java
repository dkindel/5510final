package finalproj.numa;

public interface MyLock {
	public void lock();
	public void unlock();
	public boolean enqueue(Object e);
	public Object dequeue();
	public boolean _contains(Object e);
	public void printqueue();
}