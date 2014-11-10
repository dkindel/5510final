package finalproj.locks;

import java.util.concurrent.atomic.AtomicReference;

public class LockFreeList {
	
	public AtomicReference<Record> head;
	
	public void add(Record rec){
		Record headrec = head.get();
		rec.next.set(headrec);
		while(!head.compareAndSet(headrec, rec)){ //if can't cas the head anymore, have to retry
			headrec = head.get();
			rec.next.set(headrec);
		}
	}
	
	public void remove(Record rec){
		
	}
	
	public void printList(){
		AtomicReference<Record> node;
		AtomicReference<Record> prev = head;
		while((node = prev.get().next) != null){
			System.out.print(node.get().value + " ");
		}
		System.out.println("");
	}

}
