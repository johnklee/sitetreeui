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
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.apache.lucene.util.Version;
import org.jsoup.Jsoup;

//import android.text.Html;


import edu.uci.ics.crawler4j.crawler.Page;
import edu.uci.ics.crawler4j.url.WebURL;
import flib.util.Tuple;

public class Indexer implements Observer{

	public static final String FIELD_BODY = "body";
	public static final String FIELD_URL = "url";
	public static final String FIELD_ID = "id";
	private String indexDirectoryPath;	
	private IndexWriter indexWriter;
	private static String indexStorageRootPath;
	private static final String DEFAULT_USER = "user";
	
	// secure mark
    public static enum State {ACTIVE, DEAD};
    private State state = State.ACTIVE;
    private String source;
    //

	static {
		File tmpDir = new File(System.getProperty("java.io.tmpdir"));
		File tmpLuceneDir = new File(tmpDir, "lucene");
		indexStorageRootPath = tmpLuceneDir.getAbsolutePath()+"/";
	}
	
	
	public Indexer(String rootUrl) throws IOException {
		this(DEFAULT_USER, rootUrl);
	}
	
	public static String getDefaultIndexDirectoryPath(String rootUrl) {
		return getIndexDirectoryPath(DEFAULT_USER, rootUrl);
	}
	
	private static String getIndexDirectoryPath(String user, String rootUrl){
		return indexStorageRootPath + user + "_" + rootUrl.replaceAll("\\:|\\.|\\?|\\/|\\&|\\~", "");
	}
	
	
	public Indexer(String user, String rootUrl) throws IOException {
		indexDirectoryPath = getIndexDirectoryPath(user, rootUrl) ;
		Directory indexDirectory = new SimpleFSDirectory(new File(indexDirectoryPath), new SimpleFSLockFactory());
		
		Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_47);
		IndexWriterConfig config = new IndexWriterConfig(Version.LUCENE_47, analyzer);
		indexWriter = new IndexWriter(indexDirectory, config);
		
		// secure mark
		source = rootUrl;
		//
	}
	
	
	public void close() throws IOException{
		if (indexWriter != null) indexWriter.close();
	}
	
	public void processPage (Page page) {
		WebURL url = page.getWebURL();
		System.out.printf("\t[Test] Content Type=%s...\n", page.getContentType());
		if (page.getContentType()!=null &&
			page.getContentType().equals("text/html")) {
			String html = Jsoup.parse(page.getParseData().toString()).text();
			System.out.println("\t[Test] html content= " + html);
			Document document = new Document();
			document.add(new StringField(FIELD_URL, url.getURL(), Field.Store.YES));
			document.add(new TextField(FIELD_BODY, html, Field.Store.YES));
			document.add(new StringField(FIELD_ID, String.valueOf(url.getDocid()), Field.Store.YES));
			try {
                //indexWriter.addDocument(document);
                indexWriter.updateDocument(new Term(FIELD_URL, url.getURL()), document);
                //logger.info(String.format("Page indexed %s", page.getWebURL().getURL()));
	        } catch (IOException e) {
	                e.printStackTrace();
	        }

			
		}
		
	}

	@Override
	public void update(Observable o, Object obj) {
		// needs to know whether it is the last page of the user-requested website
		Tuple rt = (Tuple)obj;	
		if(rt.getBoolean(0))
		{
			/*Page Done*/	
			Page page = (Page)rt.get(1);
			processPage(page);
			
			
			
			
		}
		else
		{
			/*Page Fail*/
			
			
		}
		
		
		
	}
	
	// secure mark
	public Indexer.State getState() {
        return state;
	}
	
	public String getRootUrl() {
		return source;
	}
	
	public static String getIndexStorageRootPath() {
		return indexStorageRootPath;
	}
	//
	

}
