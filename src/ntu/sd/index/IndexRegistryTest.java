package ntu.sd.index;

import static org.junit.Assert.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;

public class IndexRegistryTest {
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
		// Current Directory
		String workingDir = System.getProperty("user.dir");
		System.out.println("Current working directory : " + workingDir);
		
		// Initiate tmp directory
		String tmpDirPath = workingDir + File.separator + "tmp";
		File tmpDir = new File(tmpDirPath);
		if (!tmpDir.exists()) {
			System.out.println("creating directory: " + tmpDirPath);
			if(tmpDir.mkdir()) {    
				System.out.println("DIR created");  
			}
		}
		
		// url
		String content = "This_is_the_url_string";
		 
		File file = new File(tmpDirPath + File.separator + "reg");

		// if file doesnt exists, then create it
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
	
			FileWriter fw = new FileWriter(file.getAbsoluteFile());
			BufferedWriter bw = new BufferedWriter(fw);
			bw.write(content);
			bw.write("\n");
			bw.close();
			
			IndexRegistry reg = new IndexRegistry(tmpDirPath);
			
			// list
			reg.list();
			
			// inquire
			assertTrue("Can't find history registry!", reg.inquire(content));
			
			// register test - exist
			assertFalse("record exist, shouldn't add again!", reg.add(content, false));
			
			// register test - force
			assertTrue("record exist, shouldn't add again!", reg.add(content, true));
			
			// delete
			reg.delete(content);
			assertFalse("content is not deleted!", reg.inquire(content));
			
			// flush
			reg.flush();
			tmpDir.delete();
			
		} catch (IOException e) {
			e.printStackTrace();
			fail("exception in indexer");
		}
		
	}
}
