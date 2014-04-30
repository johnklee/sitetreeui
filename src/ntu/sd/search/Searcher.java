package ntu.sd.search;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class Searcher {
	private static final int NUMBER_OF_RETRIEVED_DOCUMENT = 5;
	public static List <SearchResult> search( String indexDirectoryPath, String field, String keyword) throws IOException, ParseException{
		IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexDirectoryPath)));
		IndexSearcher searcher = new IndexSearcher(reader);
		TopScoreDocCollector collector = TopScoreDocCollector.create(NUMBER_OF_RETRIEVED_DOCUMENT, true);
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		Query query = new QueryParser(Version.LUCENE_47, field, analyzer).parse(keyword);
        searcher.search(query, collector);
        ScoreDoc[] hits = collector.topDocs().scoreDocs;
        
        List <SearchResult> searchResultList = new ArrayList <SearchResult>();
        // organize SearchResult
        for(int i=0;i<hits.length;++i) {
          int docId = hits[i].doc;
          
          Document document = searcher.doc(docId);
          
          
          List <IndexableField> fieldList = document.getFields();
          searchResultList.add(new SearchResult(hits[i].score, fieldList));
          
        }
        reader.close();
		return searchResultList;
		
	}

}
