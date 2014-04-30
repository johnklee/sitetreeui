package demo;

import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ntu.sd.index.Indexer;

import unitest.TestCrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


@WebServlet("/CrawlAndIndex")
public class CrawlAndIndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrawlAndIndexServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType("text/html; charset=UTF-8");
		response.setCharacterEncoding("UTF-8");
		String rootUrl = request.getParameter("url");  
	    if(rootUrl==null) {  
	        response.getWriter().write("[['Illegal Param']]");  
	        return;  
	    }
	    String crawlStorageFolder = "C:/Users/user/temp/";
	    CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(crawlStorageFolder);
        
        
        config.setPolitenessDelay(50);
        
        
        config.setMaxPagesToFetch(100);
        config.setMaxDownloadSize(100*CrawlConfig.MB);
        
        
        
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        
        PageFetcher pageFetcher = new PageFetcher(config);
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
        try {
			CrawlController controller = new CrawlController(config, pageFetcher, robotstxtServer);
			controller.getConfig().setMaxDownloadSize(100*CrawlConfig.MB);
			controller.addSeed(rootUrl);
			Indexer indexer = new Indexer(getServletContext().getRealPath("/"),request.getSession().getId(),rootUrl);
			controller.addObserver(indexer);
			controller.startNonBlocking(TestCrawler.class, 5);
			while(!controller.isFinished())
			{
				System.out.println(indexer.getIndexDirectoryPath());
				try{Thread.sleep(1000);} catch(Exception e){e.printStackTrace();}
			}
			controller.shutdown();
    		controller.deleteObservers();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	        response.getWriter().write("finish");  
	    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		  
	}
}
