package demo;

import java.io.File;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.queryparser.classic.ParseException;

import ntu.sd.index.IndexRegistry;
import ntu.sd.index.Indexer;
import ntu.sd.search.RelevantPage;
import ntu.sd.search.Searcher;
import unitest.TestCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class IndexRegistryTest {
	public static void main(String []args){
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
		
		// Logger initiate
		File log4j = new File(tmpDirPath + File.separator + "log4j.properties");
		if(log4j.exists()) {
			System.out.println("configure log4j settings"); 
			PropertyConfigurator.configure(log4j.getAbsolutePath()); 
		}
		else {
			BasicConfigurator.configure();  
		}
		//if(true) return;
		
		// Configurations
		String storageFolderPath = tmpDirPath + File.separator + "crawlerStorage";
	    
		CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(storageFolderPath);
        config.setPolitenessDelay(50);
        config.setMaxPagesToFetch(1000);
        config.setMaxDownloadSize(1000*CrawlConfig.MB);
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        PageFetcher pageFetcher = new PageFetcher(config);
        
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        String rootUrl = "http://homepage.ntu.edu.tw/~b99609035/";
    	String keyword = "providing";
    	
    	IndexRegistry reg = new IndexRegistry();
        
        // Crawl and Index
        try {
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.getConfig().setMaxDownloadSize(1000*CrawlConfig.MB);
			controller.addSeed(rootUrl);
			reg.inquire(rootUrl);
			reg.register(controller, rootUrl, false);
			controller.startNonBlocking(TestCrawler.class, 5);
			long before = System.currentTimeMillis();
			while(!controller.isFinished())
			{
				try{Thread.sleep(1000);} catch(Exception e){e.printStackTrace();}
			}
    		reg.shutdown();
			
			long after = System.currentTimeMillis();
			System.out.println(after-before);
			controller.shutdown();
    		controller.deleteObservers();
    		pageFetcher.shutDown();
    		TestCrawler.Clear();
    		controller = null;
    		
    		//String indexDirectoryPath = reg.urltoDirPath(rootUrl);
    		String indexDirectoryPath = Indexer.getDefaultIndexDirectoryPath(rootUrl);
    		List <RelevantPage> searchResultList = null;
    		searchResultList = (new Searcher()).search(indexDirectoryPath, Indexer.FIELD_BODY, keyword);
    		
    		if (searchResultList != null) {
    			for (RelevantPage searchResult : searchResultList) {
    				System.out.println("<div >" + searchResult.getScore() + " "
    						+ searchResult.getUrl() + " "
    						+ searchResult.getId() + " "
    						+ "</div>");
    			}
    		}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        reg.list();
        //reg.flush();
	}
}
