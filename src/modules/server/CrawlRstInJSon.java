package modules.server;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.zip.GZIPInputStream;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import modules.server.utils.CrawlerMediator;
import modules.server.utils.beans.AnalysisRule;
import modules.server.utils.beans.Link;
import modules.server.utils.beans.RstJSon;
import modules.server.utils.beans.URLNode;
import net.sf.json.JSONObject;
import ntu.sd.performance.util.Result;
import ntu.sd.performance.util.RuleResult;
import ntu.sd.utils.SiTree;
import ntu.sd.utils.SiTree.Node;

import org.apache.commons.lang.ArrayUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 * Servlet implementation class CrawlRst
 */
@WebServlet("/CrawlJSon")
public class CrawlRstInJSon extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public CrawlRstInJSon() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		//Retrieve crawling result in JSON
		response.setContentType("application/x-json");
		response.setCharacterEncoding("UTF-8");
		Writer out = response.getWriter();
		HttpSession session = request.getSession();
		
		Map<String,String> cacheMap = (Map<String,String>)session.getAttribute("cache");
		CrawlerMediator cm = (CrawlerMediator)session.getAttribute("cm");
		String cacheJson=null;
		if(cacheMap==null)
		{
			cacheMap = new HashMap<String,String>();
			session.setAttribute("cache", cacheMap);
		}
		else if((cacheJson=cacheMap.get(cm.url))!=null)
		{
			out.write(cacheJson);
			out.flush();
			return;
		}
		
		
		RstJSon rj = new RstJSon();		
		if(cm!=null && cm.siTree!=null)
		{
			int id=0;	
			SiTree siTree = cm.siTree;
			if(siTree==null)
			{
				System.out.printf("\t[Test] SiTree is null!\n");
			}
			Map<String,Integer> idMap = new HashMap<String,Integer>();
			for(Node n:siTree)
			{
				System.out.printf("\t[Test] %s...(%d)\n", n.url.getURL(), n.url.getDocid());
				if(n.isValid) idMap.put(n.url.getURL(), id++);
				else idMap.put(n.pageFetchResult.getOriginalURL(), id++);
			}
			Queue<Node> pqueue = new LinkedList<Node>();
			Queue<Node> nqueue = new LinkedList<Node>();
			if(siTree.root!=null) nqueue.add(siTree.root);
			int level=0;
			while(!nqueue.isEmpty())
			{
				pqueue.addAll(nqueue);
				nqueue.clear();
				while(!pqueue.isEmpty())
				{
					Node node = pqueue.poll();
					URLNode urln = new URLNode();
					urln.setLvl(level);
					if(node.isValid) {
						urln.setUrl(node.url.getURL());
						urln.setContentType(node.page.getContentType());
						//idMap.put(node.url.getURL(), id);
					}
					else {
						urln.setUrl(node.pageFetchResult.getOriginalURL());	
						//idMap.put(node.pageFetchResult.getOriginalURL(), id);
					}					
					
					urln.setId(idMap.get(urln.getUrl()));
					
					if(node.isValid)
						urln.setContentType(node.page.getContentType());
					else
						urln.setContentType("NA");
					

					Result aszRst = cm.aRstMap.get(idMap.get(urln.getUrl()));
					if(aszRst!=null)
					{
						
						for(RuleResult rr:aszRst.getRuleResult())
						{
							urln.getAnalysis().add(new AnalysisRule(rr.getRuleName(), rr.getDescription(), rr.getScore()));
						}
					}
					if(node.isValid)
					{
						String pageCnt=null;
						String cntType = node.page.getContentType();
						System.out.printf("\t[Test] ContentType=%s:'%s'...\n", node.page.getContentType(), node.page.getWebURL().getURL());
						if(cntType!=null && cntType.contains("text/html"))						
						{							
							pageCnt = new String(node.page.getContentData(), "UTF8");
							Document doc = Jsoup.parse(pageCnt, "http://localhost/FF/");
							Elements elms = doc.getElementsByTag("title");
							if(elms.size()>0) urln.setTitle(elms.get(0).text());
							else urln.setTitle("");
						}
						else {
							urln.setTitle("");
							pageCnt="";
						}
						
						/*if(node.page.getContentEncoding()!=null) {
							if(node.page.getContentEncoding().equalsIgnoreCase("gzip"))
							{
								try
								{
									System.out.printf("\t[Test] %s content (%d)...\n", node.page.getContentEncoding(), node.page.getContentData().length);
									int MAX_CONTENT_LENT = 1024*1024*20;
									int PAGE_BODY_BUFFER = 1024*10;
									InputStream instream = new ByteArrayInputStream(node.page.getContentData()); 
									instream = new GZIPInputStream(instream);
									int rs=-1, ts=0;
									byte bytes[] = new byte[PAGE_BODY_BUFFER];
									List<Byte> byteList = new ArrayList<Byte>();
									while((rs=instream.read(bytes))>0)
									{
										for(int i=0;i<rs;i++) byteList.add(bytes[i]);
										ts+=rs;
										if(ts>MAX_CONTENT_LENT) {					
										    instream.close();
											throw new IOException(String.format("Page Size=%d exceed Maximum Acceptable Size=%d!", ts, MAX_CONTENT_LENT));
										}
										//System.out.printf("\t[Test] Read %d...\n", rs);
									}		    
								    instream.close();		    
									pageCnt =  new String(ArrayUtils.toPrimitive(byteList.toArray(new Byte[byteList.size()])), "UTF8");
								}
								catch(Exception e){
									e.printStackTrace();
									pageCnt="";
								}
							}
							else pageCnt = new String(node.page.getContentData(), node.page.getContentEncoding());
						}
						else {
							pageCnt = new String(node.page.getContentData(), "UTF8");
						}*/
						
					} // if(node.isValid)
					else urln.setTitle("");
					rj.getNodes().add(urln);
					for(Node c:node.childs.values())
					{
						int cid = -1;
						if(c.isValid) cid = idMap.get(c.url.getURL());
						else cid = idMap.get(c.pageFetchResult.getOriginalURL());
						if(cid>0) rj.getLinks().add(new Link(urln.getId(), cid));
						else System.err.printf("\t[Error] Illegal URL!\n");
					}
					nqueue.addAll(node.childs.values());					
				}
				level++;
			}
		}
		JSONObject jsonObj = JSONObject.fromObject(rj);
		System.out.printf("\t[Test] RespJSON:\n%s\n", jsonObj);
		cacheMap.put(cm.url, jsonObj.toString());
		out.write(jsonObj.toString());
		out.flush();
		cm.siTree.close();
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
