package finalproj;

import finalproj.map.HM_TB;
import finalproj.numa.MainFunction;

public class MainWrapper {
	public static void main(String args[]){
		int test = Integer.parseInt(args[0]);
		String[] newArgs = new String[10];
		//copy over arguments to new arg array
		for(int i = 1; i < args.length; i++){
			newArgs[i-1] = args[i];
		}
		//check to make sure the right number of params was used
		if(args.length != 5){
			System.err.println("Invalid number of parameters!");
			System.err.println("The program received " + args.length + " and expected 5");
			System.exit(1);
		}
		if(test == 0){
			System.out.println("Running a test on Numa-aware lock");
			try {
				MainFunction.numa_main(newArgs);
			} catch (InstantiationException | IllegalAccessException
					| ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else{
			HM_TB.hm_main(newArgs);
		}
	}
}
