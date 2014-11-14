package finalproj.fchm;


public class FCHMThread extends Thread {
	public static int ID_GEN;
	private int id;
	private FCHM<Integer> hashmap;
	
	public FCHMThread(FCHM<Integer> map){
		id = ID_GEN++;
		hashmap = map;
	}

	public void run(){
		hashmap.add(1);
	}
	
	public int getThreadId(){
		return id;
	}
}
