package modules.server;

import java.io.IOException;
import java.io.Writer;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import modules.server.utils.CrawlerMediator;

/**
 * Servlet implementation class CrawlStatus
 */
@WebServlet("/CrawlStatus")
public class CrawlStatus extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrawlStatus() {
        super();        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		String tid = request.getParameter("id");
		HttpSession session = request.getSession();
		response.setContentType("application/x-json");  
		Writer out = response.getWriter();
		Map<String,CrawlerMediator> tkMap = (Map<String,CrawlerMediator>)session.getAttribute("TM");
		if(tkMap!=null && tkMap.containsKey(tid))
		{
			CrawlerMediator cm = tkMap.get(tid);	
			//System.out.printf("\t[Test] TID=%s with status=%d...\n", tid, cm.stat);
			if(cm.stat==0 || cm.stat==-1)
			{
				tkMap.remove(tid);
				out.write(String.format("{stat:'%d', url:'%s', desc:'%s'}", cm.stat, cm.url, cm.errMsg));
				if(cm.stat==0) session.setAttribute("cm", cm);
			}			
			else if(cm.stat==1)
			{
				out.write(String.format("{stat:'%d', url:'%s', desc:'Crawling page'}", cm.stat, cm.url));
			}
			else if(cm.stat==2)
			{
				out.write(String.format("{stat:'%d', url:'%s', desc:'Indexing page'}", cm.stat, cm.url));				
			}
			else if(cm.stat==3)
			{
				out.write(String.format("{stat:'%d', url:'%s', desc:'Evaluation page'}", cm.stat, cm.url));
			}
			else
			{
				out.write(String.format("{stat:'%d', url:'%s', desc:'%s'}", cm.stat, cm.url, cm.errMsg));
			}
		}
		else
		{
			out.write(String.format("{stat:'-1', url:'', desc:'Unknown'}"));	
		}
		out.flush();
	}
}
