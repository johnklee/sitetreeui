package ntu.sd.performance.test;

import static org.junit.Assert.*;
import modules.server.utils.CrawlerMediator;
import ntu.sd.performance.util.Result;

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
		for( Result rl:cm.aRstMap.values()){
			for(int i = 0; i < rl.getRuleResult().size();i++)
			rl.getRuleResult().get(i).print();
		}
		//fail("Not yet implemented");
	}

}
