package finalproj.fchm;


public class FCHM{
	ThreadLocal<Record> rec;
	
	public FCHM(){
		rec = new ThreadLocal<Record>(){
			protected Record initialValue(){
				return new Record();
			}
		};
	}

}
