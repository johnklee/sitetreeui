package demo;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.lucene.queryparser.classic.ParseException;

import ntu.sd.index.Indexer;
import ntu.sd.search.SearchResult;
import ntu.sd.search.Searcher;
import ntu.sd.utils.SiTree;

import unitest.TestCrawler;

import edu.uci.ics.crawler4j.crawler.CrawlConfig;
import edu.uci.ics.crawler4j.crawler.CrawlController;
import edu.uci.ics.crawler4j.fetcher.PageFetcher;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtConfig;
import edu.uci.ics.crawler4j.robotstxt.RobotstxtServer;


@WebServlet("/CrawlAndIndex")
public class CrawlAndIndexServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final String SESSION_INDEX_DIRECTORY = "indexDirectory";
       
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
	    String storageFolder = "C:/Users/user/temp/";
	    CrawlConfig config = new CrawlConfig();        
        config.setCrawlStorageFolder(storageFolder);
        
        
        
        config.setPolitenessDelay(20);
        
        
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
			Indexer indexer = new Indexer(storageFolder,request.getSession().getId(),rootUrl);
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
    		request.getSession().setAttribute(SESSION_INDEX_DIRECTORY, indexer.getIndexDirectoryPath());
    		indexer.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	      
	        response.getWriter().write("<form method='post'><input type='text' name='keyword'/><input type='submit'/></form>");  
	    
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String keyword = request.getParameter("keyword");
		String indexDirectoryPath = (String)request.getSession().getAttribute(SESSION_INDEX_DIRECTORY);
		List <SearchResult> searchResultList = null;
		try {
			searchResultList = Searcher.search(indexDirectoryPath, Indexer.FIELD_BODY, keyword);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (searchResultList != null) {
			for (SearchResult searchResult : searchResultList) {
				response.getWriter().write("<div >"+searchResult.getScore() + searchResult.getUrl() +"</div>");
			}
			
		}
	}
}
