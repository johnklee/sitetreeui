package modules.server.utils;

import java.io.File;
import java.util.Random;

import ntu.sd.utils.SiTree;
import demo.MyCrawler;
import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;
import flib.util.TimeStr;

public class CrawlerMediator implements Runnable{	
	public static int 		NumberOfCrawlers = 7;
	public String 			url;
	public int 				stat=1;/*1:Under Crawling, 2:Under Indexing, 3:Under eval, -1:Error, 0:Done*/
	public String 			errMsg;
	Random	 				rdm = new Random();
	public SiTree 			siTree=null;
	
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
	        config.setPolitenessDelay(1000);
	        
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
	        controller.shutdown();
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
			Thread.sleep((int)(rdm.nextFloat()*3000));
			return true;
		}
		catch(Exception e){return false;}
	}
	
	public boolean perf()
	{
		try
		{
			Thread.sleep((int)(rdm.nextFloat()*2000));
			return true;
		}
		catch(Exception e){return false;}
	}

	@Override
	public void run() {
		stat=1;
		System.out.printf("\t[Test] Crawling...\n");
		if(!crawl()){stat=-1; errMsg="crawl fail!"; return;}
		stat=2;
		System.out.printf("\t[Test] Indexing...\n");
		if(!index()){stat=-1; errMsg="index fail!"; return;}		
		stat=3;
		System.out.printf("\t[Test] Perfing...\n");
		if(!perf()){stat=-1; errMsg="perf fail!"; return;}
		stat=0;
	}
}
