package modules.server.utils.beans;

public class Link {	
	int source;
	int target;
	
	public Link(int s, int t){source=s; target=t;}

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
}
