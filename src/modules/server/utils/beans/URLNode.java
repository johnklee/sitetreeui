package modules.server.utils.beans;

import java.util.ArrayList;
import java.util.List;

public class URLNode {
	int id;
	String title;
	int lvl;
	String url;
	List<AnalysisRule> analysis = new ArrayList<AnalysisRule>();
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public int getLvl() {
		return lvl;
	}
	public void setLvl(int lvl) {
		this.lvl = lvl;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public List<AnalysisRule> getAnalysis() {
		return analysis;
	}
	public void setAnalysis(List<AnalysisRule> analysis) {
		this.analysis = analysis;
	}	
}
