package ntu.sd.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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


@WebServlet("/search")
public class KeywordSearchServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KeywordSearchServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response){
		String rootUrl = request.getParameter("url");
		String keyword = request.getParameter("keyword");
		
		JSONObject jsonResult = new JSONObject();
		PrintWriter writer = null;
		try {
			writer = response.getWriter();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		if (rootUrl == null || keyword == null){
			jsonResult.put("error", true);
			jsonResult.put("message", "root url or keyword null");
			writer.write(jsonResult.toJSONString());
			return;
		}
		
		Searcher searcher = new Searcher();
		try {
			List <RelevantPage> pages = searcher.search(Indexer.getDefaultIndexDirectoryPath(rootUrl), Indexer.FIELD_BODY, keyword);
			jsonResult.put("result", pages);
			response.getWriter().write(jsonResult.toJSONString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			jsonResult.put("error", true);
			jsonResult.put("message", "root url wrong");
			writer.write(jsonResult.toJSONString());
			
		}
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
	}
}
