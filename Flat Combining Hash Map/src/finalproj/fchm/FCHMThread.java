package finalproj.fchm;


public class FCHMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private FCHM<Integer, String> hashmap;
	
	public FCHMThread(FCHM<Integer, String> map){
		id = ID_GEN++;
		hashmap = map;
	}

	public void run(){
		hashmap.put(id, new Integer(id).toString());
	}
	
	public int getThreadId(){
		return id;
	}
}
