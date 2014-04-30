package ntu.sd.search;

import java.util.List;

import ntu.sd.index.Indexer;

import org.apache.lucene.index.IndexableField;

public class SearchResult {
	private String url;
	private String id;
	private String score;
	public SearchResult(float score,List <IndexableField> documentFieldList){
		this.score = String.valueOf(score);
		for (IndexableField field : documentFieldList){
			if (field.name().equals(Indexer.FIELD_URL)){
				url = field.stringValue();
			} else if (field.name().equals(Indexer.FIELD_ID)) {
				id = field.stringValue();
			} 
		} 
		
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

}
