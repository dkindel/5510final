package finalproj.map.FCHM;


/**
 * The Record class defines the object that gets added to a publication list.
 * The contents include details about the record itself along with a Request
 * that details the operation a thread wants perform and any parameters 
 * associated with it.  
 * @author dave
 *
 * @param <K>
 * @param <V>
 */
public class Record<K,V> {
	public Record<K,V> next;
	public long age;
	public boolean active;
	public Request req;
	public boolean added;
	
	/**
	 * Provides details about the request a thread asks the combiner to perform.
	 * This includes parameters and return values along with a status variable.  
	 * @author dave
	 *
	 */
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
