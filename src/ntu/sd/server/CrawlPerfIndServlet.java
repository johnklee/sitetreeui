package ntu.sd.server;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.ParseException;
import org.json.simple.JSONObject;

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


@WebServlet("/url")
public class CrawlPerfIndServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String storageFolder = "C:/Users/user/temp/";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrawlPerfIndServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// Nothing TODO
	    
	}
	
	private CrawlController createCrawlController(String personalStorageFolder) throws Exception {
		
		CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(personalStorageFolder);
        
        config.setPolitenessDelay(20);
        
        config.setMaxPagesToFetch(1000);
        config.setMaxDownloadSize(1000*CrawlConfig.MB);
        
        config.setResumableCrawling(false);
        config.setIncludeBinaryContentInCrawling(true);
        
        
        PageFetcher pageFetcher = new PageFetcher(config);
        
        RobotstxtConfig robotstxtConfig = new RobotstxtConfig();
        RobotstxtServer robotstxtServer = new RobotstxtServer(robotstxtConfig, pageFetcher);
		return new CrawlController(config, pageFetcher, robotstxtServer);
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.println("click");
		String rootUrl = request.getParameter("url");  
	    if(rootUrl == null) {  
	        return;  
	    }
	    
		try {
			Indexer indexer = new Indexer(storageFolder,request.getSession().getId(),rootUrl);
			
			CrawlController controller = createCrawlController(indexer.getIndexDirectoryPath());
			controller.addSeed(rootUrl);
			
			
			SiTree siTree = new SiTree();
			
			controller.addObserver(indexer);
			controller.addObserver(siTree);
			controller.start(TestCrawler.class, 10);
			
			while (!controller.isFinished()) {
				
			}
			
			controller.deleteObservers();
			controller.shutdown();		
			TestCrawler.Clear();
			controller = null;
			siTree.close();
			request.getSession().setAttribute(rootUrl, indexer.getIndexDirectoryPath());
			indexer.close();

		    
	        response.setContentType("text/html; charset=UTF-8");
			response.setCharacterEncoding("UTF-8");
			
			JSONObject obj = new JSONObject();
			obj.put("error", 1);
			obj.put("message", "hello");
		    response.getWriter().write(obj.toJSONString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
}