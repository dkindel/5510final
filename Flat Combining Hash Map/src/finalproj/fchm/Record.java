package finalproj.fchm;



public class Record<K,V> {
	public Record<K,V> next;
	public long age;
	public boolean active;
	public Request req;
	public boolean added;
	
	class Request{
		public int op; //0 for add
				//1 for remove
				//2 for contains
		public K key;
		public V value;
		public boolean done;
		public V retval;
		
		public Request(){
			done = true;		//true at first so the combiner doesn't pick it up
			retval = null;
			op = -1;
		}
	}
	
	public Record(){
		age = 0;
		next = null;
		active = false;
		req = new Request();
	}
	
}
