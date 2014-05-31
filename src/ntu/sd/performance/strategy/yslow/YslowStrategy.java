package ntu.sd.performance.strategy.yslow;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;

import ntu.sd.performance.util.Result;
import ntu.sd.performance.strategy.AnalysisStrategy;

public class YslowStrategy implements AnalysisStrategy {

	@Override
	public Result analyze(String URL) {
		// TODO Auto-generated method stub
		
		
		return yslow(URL);
	}

	
	
	public Result yslow( String URL ){
		try {
		    // Execute command
			YslowTranslator yt = new YslowTranslator();
			String dir =System.getProperty("user.dir");
			//System.out.println(dir);
		    String command = "phantomjs\\phantomjs phantomjs\\yslow.js --info grade ";
		    //URL="http://www.yahoo.com/";
		    Process child = Runtime.getRuntime().exec(command +URL);
		    Result res;
		    // Get output stream to write from it
		    
		         
		    
		    
		    OutputStream out = child.getOutputStream();
		    BufferedReader stdInput = new BufferedReader(new InputStreamReader(child.getInputStream()));
		    String s  = stdInput.readLine();

		    res=new Result(-1,23,yt.ResParse(s));
		    return res;
		    
		    /*
			while ((s = stdInput.readLine()) != null) {
		            System.out.println(s);
		    }
		    */
		    
			//System.out.println(s);
			//System.out.println(yt.ResParse(s));
			//ResList.add(yt.ResParse(s));
			/*
			for(int i = 0 ; i < ResList.size();i++)
				ResList.get(i).print();;
			 */
			
		} catch (IOException e) {
		}
		return null;
	}
}
