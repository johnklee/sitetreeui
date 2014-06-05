package ntu.sd.search;

import java.util.List;

import ntu.sd.index.Indexer;

import org.apache.lucene.index.IndexableField;

public class RelevantPageFactory {
	public static RelevantPage create(float score, List <IndexableField> documentFieldList) {
		RelevantPage page = new RelevantPage();
		page.setScore(String.valueOf(score));
		for (IndexableField field : documentFieldList){
			if (field.name().equals(Indexer.FIELD_URL)){
				page.setUrl(field.stringValue());
			} else if (field.name().equals(Indexer.FIELD_ID)) {
				page.setId(field.stringValue());
			} 
		}
		return page;
		
	}

}
