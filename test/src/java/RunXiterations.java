import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;



public class RunXiterations {
	

	public static void main(String [] args){
		   String s = null;

		try {
			Process p = Runtime.getRuntime().exec("java jason.mas2j.parser.mas2j /home/schlafli/workspaces/masworkspace/test/test.mas2j run");
			BufferedReader stdInput = new BufferedReader(new 
	                 InputStreamReader(p.getInputStream()));

	            BufferedReader stdError = new BufferedReader(new 
	                 InputStreamReader(p.getErrorStream()));

	            // read the output from the command
	            System.out.println("Here is the standard output of the command:\n");
	            while ((s = stdInput.readLine()) != null) {
	                System.out.println(s);
	            }
	            
	            // read any errors from the attempted command
	            System.out.println("Here is the standard error of the command (if any):\n");
	            while ((s = stdError.readLine()) != null) {
	                System.out.println(s);
	            }

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

	}
}
