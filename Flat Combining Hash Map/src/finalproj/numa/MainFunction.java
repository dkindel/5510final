package finalproj.numa;

public class MainFunction {
	private static final int THREAD_COUNT  = 8;
	private static final int CLUSTER_COUNT = 2;
	private static final String HCLH   = "HCLHLock";
	private static final String NATIVE = "NativeQueue";
	private static final String FCQ    = "FCQueue";
	private static final String FCNUMA = "FCNumaLock";
	
	public static void numa_main(String[] args) throws InstantiationException, IllegalAccessException, ClassNotFoundException{
		
		String lockClass = (args.length==0 ? HCLH : args[0]);
		int numThreads = (args.length<2 ? THREAD_COUNT : Integer.parseInt(args[1]));
		int numClusters = (args.length<3 ? CLUSTER_COUNT : Integer.parseInt(args[2]));
		MyLock tryLock;
		int delay = (args.length<4 ? 0 : Integer.parseInt(args[3]));
		
		int threadsPerCluster = numThreads/numClusters;	
		
		System.out.println("Alg: "+lockClass);
		/*
		if (lockClass.contains(HCLH)){
			tryLock = new HCLHLock(numClusters, delay);
		}else{
			tryLock = (MyLock)Class.forName("NUMA." + lockClass).newInstance();
		}*/
		
		tryLock = new FCNumaLock(numClusters,delay);
				
		for (int i = 0; i<numClusters;i++){
			for(int t=0; t<threadsPerCluster; t++){
				new TestThread((i),(MyLock)tryLock).start();
		    }	
		}
	}
}
