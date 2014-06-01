package ntu.sd.performance.strategy.yslow;

import java.util.ArrayList;

import ntu.sd.performance.strategy.AnalysisStrategy;
import ntu.sd.performance.util.Result;
import ntu.sd.performance.util.RuleResult;

public class FakeYslowStrategy implements AnalysisStrategy{

	@Override
	public Result analyze(String URL) {
		Result rst = new Result();
		rst.setNumOfRule(1);
		RuleResult frr = new RuleResult();
		frr.setDescription("ForTesting");
		frr.setRuleName("FakeRule");
		frr.setScore(0);
		ArrayList<RuleResult> list = new ArrayList<RuleResult>();
		list.add(frr);
		rst.setRuleResult(list);
		return rst;
	}
}
