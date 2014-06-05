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
import org.json.simple.JSONArray;
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


@WebServlet("/searchDummy")
public class KeywordSearchServletDummy extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public KeywordSearchServletDummy() {
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
		
		JSONObject first = new JSONObject();
		first.put("id", 4);
		first.put("url", "http://blah.net");
		first.put("score", 100);
		JSONObject second = new JSONObject();
		second.put("id", 3);
		second.put("url", "http://kaboom.org");
		second.put("score", 40);
		JSONObject third = new JSONObject();
		third.put("id", 0);
		third.put("url", "http://home.com");
		third.put("score", 10);
		
		JSONArray ar = new JSONArray();
		ar.add(first);
		ar.add(second);
		ar.add(third);
		
		//jsonResult.put("total", 3);
		jsonResult.put("results", ar);
		writer.write(jsonResult.toJSONString());
		return;
		
		
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request,response);
	}
}
