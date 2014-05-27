package modules.server.utils.beans;

import java.util.ArrayList;
import java.util.List;

public class RstJSon {
	List<URLNode> nodes = new ArrayList<URLNode>();
	List<Link> links = new ArrayList<Link>();
	
	public List<URLNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<URLNode> nodes) {
		this.nodes = nodes;
	}
	public List<Link> getLinks() {
		return links;
	}
	public void setLinks(List<Link> links) {
		this.links = links;
	}		
}
