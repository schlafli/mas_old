package env;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class LogOut {

	private static LogOut out = new LogOut();
	private boolean go=false;
	
	private File outFile;
	private PrintWriter outWriter;
	
	private LogOut(){
		
	}
	public void close(){
		if(go){
			outWriter.flush();
			outWriter.close();
			go=false;
			
		}
	}
	
	public void openFile(String prefix){
		if(!go){
			outFile = new File("./out/"+prefix+"-"+System.currentTimeMillis()+".txt");
			try {
				outWriter= new PrintWriter(outFile);
				go=true;
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void log(String s){
		if(go){
			outWriter.println(s);
			outWriter.flush();
		}
	}
	
	public static LogOut getInstance(){
		return out;
	}
	
}
