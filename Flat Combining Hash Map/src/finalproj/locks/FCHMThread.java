package finalproj.locks;

public class FCHMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private FCHM hashmap;
	
	public FCHMThread(FCHM map){
		id = ID_GEN++;
		hashmap = map;
	}

	public void run(){
		Record rec = new Record();
		rec.value = id;
	}
	
	public int getThreadId(){
		return id;
	}
}
