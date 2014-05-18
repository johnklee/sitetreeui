package demo;

import java.io.File;
import java.util.List;

import org.apache.lucene.queryparser.classic.ParseException;

import ntu.sd.index.Indexer;
import ntu.sd.search.RelevantPage;
import ntu.sd.search.Searcher;
import ntu.sd.utils.SiTree;
import unitest.TestCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;

public class CrawlAndIndexTest {
	public static void main(String []args){
		String storageFolder = "C:/Users/user/temp2/";
	    CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(storageFolder);
        
        
        
        config.setPolitenessDelay(50);
        
        
        config.setMaxPagesToFetch(1000);
        config.setMaxDownloadSize(1000*CrawlConfig.MB);
        
        
        
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        
        
        PageFetcher pageFetcher = new PageFetcher(config);
        
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        String rootUrl = "http://homepage.ntu.edu.tw/~b99609035/";
        try {
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.getConfig().setMaxDownloadSize(1000*CrawlConfig.MB);
			controller.addSeed(rootUrl);
			Indexer indexer = new Indexer(storageFolder,"testing",rootUrl);
			SiTree siTree = new SiTree();
			controller.addObserver(indexer);
			controller.addObserver(siTree);
			controller.startNonBlocking(TestCrawler.class, 5);
			long before = System.currentTimeMillis();
			while(!controller.isFinished())
			{
				
				try{Thread.sleep(1000);} catch(Exception e){e.printStackTrace();}
			}
			
			long after = System.currentTimeMillis();
			System.out.println(after-before);
			siTree.outputTo(new File(storageFolder));
			controller.shutdown();
    		controller.deleteObservers();
    		pageFetcher.shutDown();
    		TestCrawler.Clear();
    		controller = null;
    		indexer.close();
    		
    		String indexDirectoryPath = indexer.getIndexDirectoryPath();
    		List <RelevantPage> searchResultList = null;
    		String keyword = "providing";
    		searchResultList = (new Searcher()).search(indexDirectoryPath, Indexer.FIELD_BODY, keyword);
    		
    		if (searchResultList != null) {
    			for (RelevantPage searchResult : searchResultList) {
    				System.out.println("<div >"+searchResult.getScore() + searchResult.getUrl() +"</div>");
    			}
    			
    		}
    		
    		
    		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        

		
	}

}
