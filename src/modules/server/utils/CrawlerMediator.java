package modules.server.utils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ntu.sd.index.Indexer;
import ntu.sd.performance.PerformanceHandler;
import ntu.sd.performance.strategy.AnalysisStrategy;
import ntu.sd.performance.strategy.yslow.FakeYslowStrategy;
import ntu.sd.performance.strategy.yslow.YslowStrategy;
import ntu.sd.performance.util.Result;
import ntu.sd.utils.SiTree;
import ntu.sd.utils.SiTree.Node;
import demo.MyCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import flib.util.TimeStr;

public class CrawlerMediator implements Runnable{	
	public static boolean		isInCache=false;
	public static int 			NumberOfCrawlers = 7;
	public String 				url;
	public int 					stat=1;/*1:Under Crawling, 2:Under Indexing, 3:Under eval, -1:Error, 0:Done*/
	public String 				errMsg;
	Random	 					rdm = new Random();
	public SiTree 				siTree=null;
	public Map<Integer,Result>	aRstMap = new HashMap<Integer,Result>();
	
	public CrawlerMediator(String url){this.url = url;}
	
	public boolean crawl(){
		try
		{
			long st = System.currentTimeMillis();
			CrawlController.CheckOthersWait=CrawlController.CleanUpWait=CrawlController.DConfirmWait=1;
			File tmpDir = new File(System.getProperty("java.io.tmpdir"));
			File tmpCMDir = new File(tmpDir, "crawler4j");
			tmpCMDir.mkdirs();
			CrawlConfig config = new CrawlConfig();
			config.setCrawlStorageFolder(tmpCMDir.getAbsolutePath());
			/*
	         * Be polite: Make sure that we don't send more than 1 request per
	         * second (1000 milliseconds between requests).
	         */
	        config.setPolitenessDelay(0);
	        config.setConnectionTimeout(1000);
	        config.setSocketTimeout(1000);
	        
	        /*
	         * You can set the maximum number of pages to crawl. The default value
	         * is -1 for unlimited number of pages
	         */
	        config.setMaxPagesToFetch(1000);
	        
	        /*
	         * This config parameter can be used to set your crawl to be resumable
	         * (meaning that you can resume the crawl from a previously
	         * interrupted/crashed crawl). Note: if you enable resuming feature and
	         * want to start a fresh crawl, you need to delete the contents of
	         * rootFolder manually.
	         */
	        config.setResumableCrawling(false);
	        config.setIncludeBinaryContentInCrawling(true);
	        config.setMaxDownloadSize(10*config.MB);
	        
	        /*
	         * Instantiate the controller for this crawl.
	         */
	        PageFetcher pageFetcher = new PageFetcher(config);
	        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
	        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
	        CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
	        
	        /*
	         * For each crawl, you need to add some seed urls. These are the first
	         * URLs that are fetched and then the crawler starts following links
	         * which are found in these pages
	         */
	        controller.addSeed(url);
	        System.out.printf("\t[Info] Starting Crawler...\n");
	        siTree = new SiTree();
	        controller.addObserver(siTree);       
	        controller.start(MyCrawler.class, NumberOfCrawlers);
	        System.out.printf("\t[Info] Done! %s\n", TimeStr.ToStringFrom(st));
	        controller.deleteObserver(siTree);
	        System.out.printf("\t[Info] Shutdown Controller...\n");
	        controller.shutdown();
	        //controller.getFrontier().close();
	        Thread.sleep(500);
	        System.out.printf("\t[Info] Delete tmp directory...\n");
	        for(File f:tmpCMDir.listFiles()) f.delete();
	        tmpCMDir.delete();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			errMsg = e.toString();
			return false;
		}
	}
	
	public boolean index()
	{
		try
		{
			Indexer indexer = new Indexer(url);
			for (Node node : siTree.nodeMap.values())
			{
				if(node.isValid) indexer.processPage(node.page);
			}
			indexer.close();
			return true;
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
	public boolean perf()
	{
		try
		{
			System.out.println("[Perf]Start!");
			AnalysisStrategy as = new YslowStrategy();
			PerformanceHandler ph = new PerformanceHandler();
			ph.setStrategy(as);
			for (Node node : siTree.nodeMap.values())
			{
				
				String url;
				int id;
				if(node.isValid) {
					url = node.url.getURL();
					id = node.url.getDocid();
					ph.newAnalyze(id, url);
					
				}
				
				//else url = node.pageFetchResult.getOriginalURL();

			}
			
			//wait for done
			while(!ph.isDone());
			
			for (Result rl:ph.getResultList())
			{
				aRstMap.put(rl.getId(),rl);
			}
			
			System.out.println("[Perf]Done!");
			
			
			return true;
			
			
			
			/*AnalysisStrategy as = new FakeYslowStrategy();
			for (Node node : siTree.nodeMap.values())
			{
				String url;
				if(node.isValid) {
					url = node.url.getURL();
					aRstMap.put(node.url.getDocid(), as.analyze(url));
				}
				else url = node.pageFetchResult.getOriginalURL();
				
			}
			return true;*/
			
		}
		catch(Exception e){
			e.printStackTrace();
			return false;			
		}
	}

	@Override
	public void run() {
		if(!isInCache)
		{
			stat=1;
			System.out.printf("\t[Test] Crawling...\n");
			if(!crawl()){stat=-1; errMsg="crawl fail!"; return;}
			stat=2;
			System.out.printf("\t[Test] Indexing...\n");
			if(!index()){stat=-1; errMsg="index fail!"; return;}		
			stat=3;
			System.out.printf("\t[Test] Perfing...\n");
			if(!perf()){stat=-1; errMsg="perf fail!"; return;}
		}
		stat=0;		
	}
}
