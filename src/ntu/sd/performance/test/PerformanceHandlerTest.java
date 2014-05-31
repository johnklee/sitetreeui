package ntu.sd.performance.test;

import static org.junit.Assert.*;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import ntu.sd.performance.PerformanceHandler;

public class PerformanceHandlerTest {
	PerformanceHandler ph;
	
	@Before
	public void setUp() throws Exception {
		System.out.println("Test Start");
		ph=new PerformanceHandler();
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Test End");
	
		ph=null;
	}

	@Test
	public void testUpdate() {

		String dir =System.getProperty("user.dir");
		System.out.println(dir);
		//################
		WebURL url=new WebURL();
		url.setURL("http://www.yahoo.com/");
		url.setDocid(0);
		PageFetchResult pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//####################
		url=new WebURL();
		url.setURL("http://www.google.com/");
		url.setDocid(1);
		pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//####################
		
		
		//####################
		
		while(!ph.IsDone()){
			//System.out.println("nd");
		}
		for(int j = 0 ; j < ph.getResultList().size() ; j++) {
			for(int i = 0 ; i < ph.getResultList().get(j).getRuleResult().size();i++){
				System.out.println(i);
				ph.getResultList().get(j).getRuleResult().get(i).print();
			}
		}
		
		//####################
	}
	
	
	


}
