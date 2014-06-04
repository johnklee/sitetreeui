package ntu.sd.performance.test;

import static org.junit.Assert.*;
import modules.server.utils.CrawlerMediator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CrawlerMediatorTest {
	CrawlerMediator cm;
	@Before
	public void setUp() throws Exception {
		cm = new CrawlerMediator("http://140.112.31.76/FF/crawlme/");
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testPerf() {
		cm.crawl();
		cm.perf();
		//fail("Not yet implemented");
	}

}
