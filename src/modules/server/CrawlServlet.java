package modules.server;

import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import modules.server.utils.CrawlerMediator;

/**
 * Servlet implementation class CrawlServlet
 */
@WebServlet("/Crawl")
public class CrawlServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrawlServlet() {
        super();        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String url = (String)request.getParameter("urls");
		HttpSession session = request.getSession();
		response.setContentType("application/x-json");  
		Writer out = response.getWriter();
		if(url.startsWith("http"))
		{
			System.out.printf("\t[Test] Crawling: %s\n", url);			
			/*Task Process*/
			CrawlerMediator cm = new CrawlerMediator(url);
			new Thread(cm).start();
			Map<String,CrawlerMediator> tkMap = (Map<String,CrawlerMediator>)session.getAttribute("TM");
			if(tkMap==null)
			{
				tkMap = new HashMap<String,CrawlerMediator>();
				session.setAttribute("TM", tkMap);
			}
			int tid=0;	
			while(tkMap.containsKey(String.valueOf(tid)))
			{
				tid=(tid+1)%100;
			}
			tkMap.put(String.valueOf(tid), cm);
			out.write(String.format("{id:'%s', url:'%s', desc:'test'}", tid, url));			
		}
		else
		{
			System.out.printf("\t[Test] Illegal URL: %s\n", url);
			out.write(String.format("{id:'', url:'%s', desc:'Illegal URL'}", url));
		}
		out.flush();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		System.out.printf("\t[Test] Posting...\n");
		doGet(request, response);
	}

}
