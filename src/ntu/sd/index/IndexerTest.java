package ntu.sd.index;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.omg.PortableInterceptor.SUCCESSFUL;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.fetcher.PageFetchResult;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.parser.ParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class IndexerTest {
	@Before
	public void setUp() throws Exception {
		System.out.println("Test Start");
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Test End");
	
		
	}

	@Test
	public void test(){
		String rootUrl = "http://nkfly.github.io/";
		try {
			URL yahoo = new URL(rootUrl);
		    URLConnection yc = yahoo.openConnection();
		    BufferedReader in = new BufferedReader(new InputStreamReader(yc.getInputStream()));
		    String wholeHtml = "";
		    String inputLine;
		    while ((inputLine = in.readLine()) != null) wholeHtml += (inputLine + "\n");
		    in.close();
		    // we have get the html text and content type
		    // I will make a fake Page now
		    WebURL url = new WebURL();
		    url.setURL(rootUrl);
		    Page page = new Page(url);
		    page.setContentType("text/html");
		    HtmlParseData parseData = new HtmlParseData();
		    parseData.setText(wholeHtml);
		    page.setParseData(parseData);
		    
		    // start indexer
		    Indexer indexer = new Indexer(rootUrl);
		    assertNotNull(indexer.getDefaultIndexDirectoryPath(rootUrl));
		    indexer.processPage(page);
		    indexer.close();
		    
		
			
		} catch (Exception  e) {
			e.printStackTrace();
			fail("exception in indexer");
		}
		
		
	}

}
