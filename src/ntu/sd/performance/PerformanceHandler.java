package ntu.sd.performance;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import ntu.sd.performance.strategy.yslow.YslowStrategy;
import ntu.sd.performance.util.Result;
import ntu.sd.performance.strategy.AnalysisStrategy;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class PerformanceHandler implements Observer {
	private int count;
	private AnalysisStrategy strategy;
	ArrayList<Result> ResultList;
	ThreadGroup threadGroup;
	


	
	public ThreadGroup getThreadGroup() {
		return threadGroup;
	}


	public void setThreadGroup(ThreadGroup threadGroup) {
		this.threadGroup = threadGroup;
	}


	public PerformanceHandler(){
		count=0;
		strategy = new YslowStrategy();
		ResultList = new ArrayList<Result>();
		threadGroup= new ThreadGroup("group");
	}
	
	
	@Override
	public void update(Observable o, Object obj) {
		// TODO Auto-generated method stub
				Tuple rt = (Tuple)obj;	
				if(rt.getBoolean(0))
				{
					/*Page Done*/	
					
					WebURL page = (WebURL)rt.get(1);
					
					int id = page.getDocid();
					String URL = page.getURL();
					newAnalyze(id,URL);


				}
				else
				{
					/*Page Fail*/
					//System.out.print("page fail");

				}

	}
	
	
	

	public void newAnalyze(int id,String URL) {
		// TODO Auto-generated method stub
					PHThread thread=new PHThread(threadGroup,"group");
					thread.setPHThread(id, URL, strategy, ResultList);
					thread.start();
	}
	
	public boolean isDone() {
		if(threadGroup.activeCount()==0)
			return true;
		return false;
	}
	
	public AnalysisStrategy getStrategy() {
		return strategy;
	}
	public void setStrategy(AnalysisStrategy strategy) {
		this.strategy = strategy;
	}
	public ArrayList<Result> getResultList() {
		return ResultList;
	}
	public void setResultList(ArrayList<Result> resultList) {
		ResultList = resultList;
	}


}
