package ntu.sd.index;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

//import android.text.Html;

import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.parser.HtmlParseData;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class Indexer implements Observer{

	public static final String FIELD_BODY = "body";
	public static final String FIELD_URL = "url";
	public static final String FIELD_ID = "id";
	private String indexDirectoryPath;	
	private IndexWriter indexWriter;
	
	public Indexer(String rootPath,String uid, String rootUrl) throws IOException {
		indexDirectoryPath = rootPath + uid + "_" + rootUrl.replaceAll("\\:|\\.|\\?|\\/|\\&", "");
		Directory indexDirectory = new SimpleFSDirectory(new File(indexDirectoryPath), new SimpleFSLockFactory());
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		indexWriter = new IndexWriter(indexDirectory, config);
		
	}
	
	public String getIndexDirectoryPath(){
		return indexDirectoryPath;
	}
	
	public void close() throws IOException{
		if (indexWriter != null) indexWriter.close();
	}

	@Override
	public void update(Observable o, Object obj) {
		// needs to know whether it is the last page of the user-requested website
		Tuple rt = (Tuple)obj;	
		if(rt.getBoolean(0))
		{
			/*Page Done*/	
			Page page = (Page)rt.get(1);
			WebURL url = page.getWebURL();

			
			if (page.getContentType().equals("text/html")) {
				String html = Jsoup.parse(page.getParseData().toString()).text();
				Document document = new Document();
				document.add(new StringField(FIELD_URL, url.getURL(), Field.Store.YES));
				document.add(new TextField(FIELD_BODY, html, Field.Store.YES));
				document.add(new StringField(FIELD_ID, String.valueOf(url.getDocid()), Field.Store.YES));
				try {
					indexWriter.addDocument(document);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
			}
			
			
			
		}
		else
		{
			/*Page Fail*/
			
			
		}
		
		
		
	}
	
	

}
