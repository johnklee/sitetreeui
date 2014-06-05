package ntu.sd.search;

import java.util.List;

import ntu.sd.index.Indexer;

import org.apache.lucene.index.IndexableField;
import org.json.simple.JSONObject;

public class RelevantPage {
	private String url;
	private String id;
	private String score;
	public RelevantPage(){
		
	}
	
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getScore() {
		return score;
	}
	public void setScore(String score) {
		this.score = score;
	}
	
	@Override
	public String toString() {
		JSONObject jsonResult = new JSONObject();
		jsonResult.put("url", url);
		jsonResult.put("id", id);
		jsonResult.put("score", score);
		return jsonResult.toJSONString();
	}

}
