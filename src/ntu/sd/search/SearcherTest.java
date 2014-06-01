package ntu.sd.search;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import ntu.sd.index.Indexer;

import org.apache.lucene.queryparser.classic.ParseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SearcherTest {
	String rootUrl;
	Searcher searcher;
	@Before
	public void setUp() throws Exception {
		System.out.println("Test Start");
		rootUrl = "http://nkfly.github.io/";
		searcher = new Searcher();
	}

	@After
	public void tearDown() throws Exception {
		System.out.println("Test End");
	
		
	}


	@Test
	public void testFound() {
		
		String keyword = "CUDA function";
		List <RelevantPage> pages = null;
		try {
			pages = searcher.search(Indexer.getDefaultIndexDirectoryPath(rootUrl), Indexer.FIELD_BODY, keyword);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("searcher exception");
		} 
		
		assertNotNull(pages);
		assertSame("there should be 1 return page", 1, pages.size());
		
		
	}
	@Test
	public void testNotFound() {
		
		String keyword = "Incredible";
		List <RelevantPage> pages = null;
		try {
			pages = searcher.search(Indexer.getDefaultIndexDirectoryPath(rootUrl), Indexer.FIELD_BODY, keyword);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			fail("searcher exception");
		} 
		
		assertNotNull(pages);
		assertSame("there should be 0 return page", 0, pages.size());
		
	}

}
