package finalproj.locks;

import java.util.concurrent.atomic.AtomicReference;

public class Record {
	public AtomicReference<Record> next;
	public int value;
	public long age;
	
	public Record(int val){
		value = val;
		age = System.currentTimeMillis(); //sets the age 
	}
	
	public Record(){
		age = System.currentTimeMillis(); //sets the age 
	}
	
}
