package finalproj.locks;

public class FCHM implements Lock{
	
	LockFreeList list;
	
	public FCHM(){
		list = new LockFreeList();
	}
	
	@Override
	public void lock() {
		
	}

	@Override
	public void unlock() {
		
	}

}
