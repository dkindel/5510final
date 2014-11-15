package finalproj.fchm;



public class Record<T> {
	public Record<T> next;
	public long age;
	public boolean active;
	public Request req;
	public boolean added; //added to the list
	
	class Request{
		public int op; //0 for add
				//1 for remove
				//2 for contains
		public T param;
		public boolean done;
		public boolean retval;
		
		public Request(){
			done = true;		//true at first so the combiner doesn't pick it up
			retval = false;
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
