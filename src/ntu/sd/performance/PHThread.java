package ntu.sd.performance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import ntu.sd.performance.strategy.yslow.YslowTranslator;
import ntu.sd.performance.util.Result;
import ntu.sd.performance.strategy.AnalysisStrategy;

public class PHThread extends Thread {
	private int id;
	private String URL;
    ArrayList<Result> ResList;
    AnalysisStrategy strategy;
    
	public  PHThread(ThreadGroup tg, String tstr){
		super(tg,tstr);
	}
	
	public void setPHThread(int n_id,String n_URL,AnalysisStrategy n_strategy,ArrayList<Result> n_ResList){
		id=n_id;
		URL=n_URL;
		strategy=n_strategy;
		ResList=n_ResList;
	}
	
	@Override
	public void run(){
		Result r = strategy.analyze(URL);
		r.setId(id);
		ResList.add(r);
	}
	
	
	public void yslow(){
		try {
		    // Execute command
			YslowTranslator yt = new YslowTranslator();
			String dir =System.getProperty("user.dir");
		    String command = "phantomjs\\phantomjs phantomjs\\yslow.js --info grade ";
		    //URL="http://www.yahoo.com/";
		    Process child = Runtime.getRuntime().exec(command +URL);
		    Result res;
		    
		    // Get output stream to write from it		    		         		    		    
		    OutputStream out = child.getOutputStream();
		    BufferedReader stdInput = new BufferedReader(new InputStreamReader(child.getInputStream()));
		    String s  = stdInput.readLine();

		    res=new Result(id,23,yt.ResParse(s));
		    ResList.add(res);
		    
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
	}

}
