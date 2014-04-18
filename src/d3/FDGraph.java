package d3;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;


/**
 * Servlet implementation class FDGraph
 */
@WebServlet("/FDGraph")
public class FDGraph extends HttpServlet {
	private static final long serialVersionUID = 1L;
	public static class FDGraphData{
		List<Node> nodes = new ArrayList<Node>();
		List<Link> links = new ArrayList<Link>();
		
		public FDGraphData(){}
		public List<Node> getNodes() {
			return nodes;
		}
		public void setNodes(List<Node> nodes) {
			this.nodes = nodes;
		}
		public List<Link> getLinks() {
			return links;
		}
		public void setLinks(List<Link> links) {
			this.links = links;
		}				
	}
	public static class Node{
		String name;
		int group;
		public Node(String n, int g){this.name =n; this.group = g;}
		public Node(){}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public int getGroup() {
			return group;
		}
		public void setGroup(int group) {
			this.group = group;
		}		
	}
	
	public static class Link{
		int source;
		int target;
		int value;
		
		public Link(int s, int t, int v){source=s; target=t; value=v;}
		public Link(){}
		
		public int getSource() {
			return source;
		}
		public void setSource(int source) {
			this.source = source;
		}
		public int getTarget() {
			return target;
		}
		public void setTarget(int target) {
			this.target = target;
		}
		public int getValue() {
			return value;
		}
		public void setValue(int value) {
			this.value = value;
		}		
	}

    /**
     * Default constructor. 
     */
    public FDGraph() {
    	
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
		try
		{		
			InputStream in = this.getClass().getResourceAsStream("miserables.json");
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String line;
			StringBuffer jsonStrBuf = new StringBuffer();
			while((line=br.readLine())!=null)
			{
				jsonStrBuf.append(line);
			}
			br.close();
			System.out.printf("%s\n", jsonStrBuf.toString());
			response.getWriter().write(new String(jsonStrBuf.toString().getBytes("UTF8"),"BIG5"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
			FDGraphData datas = new FDGraphData();
	    	datas.getNodes().add(new Node("n1",0));
	    	datas.getNodes().add(new Node("n2",2));
	    	datas.getNodes().add(new Node("n3",0));
	    	datas.getLinks().add(new Link(0,1,1));
	    	datas.getLinks().add(new Link(1,2,2));
	    	JSONObject jsonObject = JSONObject.fromObject(datas);
			
	    	System.out.printf("%s\n", jsonObject);
			response.getWriter().write(new String(jsonObject.toString().getBytes("UTF8"),"BIG5"));
		}				    	
	}

}
