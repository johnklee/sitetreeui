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
		url.setURL("http://140.112.31.76/FF/crawlme/");
		url.setDocid(0);
		PageFetchResult pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		url=new WebURL();
		url.setURL("http://140.112.31.76/FF/css/test.css");
		url.setDocid(1);
		pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		 url=new WebURL();
		url.setURL("http://140.112.31.76/FF/crawlme/test.html");
		url.setDocid(2);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		
		//################
		url=new WebURL();
		url.setURL("URL=http://140.112.31.76/FF/crawlme/test2.html");
		url.setDocid(3);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		 url=new WebURL();
		url.setURL("URL=http://140.112.31.76/FF/crawlme/");
		url.setDocid(4);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		 url=new WebURL();
		url.setURL("http://140.122.64.191/SchoolSrh/Demo/schoolSearch.html");
		url.setDocid(5);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		 url=new WebURL();
		url.setURL("http://140.112.31.76/FF/mp3/test.mp3");
		url.setDocid(6);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		//################
		 url=new WebURL();
		url.setURL("http://140.122.64.191/SchoolSrh/js/ext-3.4.0/resources/css/ext-all.css");
		url.setDocid(7);
		 pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//################
		
		
		/*
		//####################
		 * http://140.112.31.76/FF/crawlme/
		 * URL=http://140.112.31.76/FF/css/test.css
		 * URL=http://140.112.31.76/FF/crawlme/test.html
		 * URL=http://140.112.31.76/FF/crawlme/test2.html:
		 * http://140.112.31.76/FF/crawlme/
		 * http://140.122.64.191/SchoolSrh/Demo/schoolSearch.html:
		 * http://140.112.31.76/FF/mp3/test.mp3:
		 * http://140.122.64.191/SchoolSrh/js/ext-3.4.0/resources/css/ext-all.css:
		url=new WebURL();
		url.setURL("http://www.google.com/");
		url.setDocid(1);
		pr= new PageFetchResult();
		ph.update(null, new Tuple(true, url, pr));
		//####################
		*/
		
		//####################
		
		while(!ph.isDone()){
			//System.out.println("nd");
		}
		
		System.out.println(ph.getResultList().size());
		for(int j = 0 ; j < ph.getResultList().size() ; j++) {
			for(int i = 0 ; i < ph.getResultList().get(j).getRuleResult().size();i++){
				System.out.println(i);
				ph.getResultList().get(j).getRuleResult().get(i).print();
			}
		}
		
		//####################
	}
	
	
	


}
