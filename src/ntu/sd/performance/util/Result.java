package ntu.sd.performance.util;

import java.util.ArrayList;

public class Result {
	private int id;
	private int numOfRule;
	private ArrayList<RuleResult> ruleResult = new ArrayList<RuleResult>();
	
	public Result (){
		this.setId(-1);
		this.setNumOfRule(-1);
		this.setRuleResult(null);
	}
	public Result(int n_id,int n_num,ArrayList<RuleResult> Rl ){
		this.setId(n_id);
		this.setNumOfRule(n_num);
		this.setRuleResult(Rl);
	}
	public ArrayList<RuleResult> getRuleResult() {
		return ruleResult;
	}
	public void setRuleResult(ArrayList<RuleResult> ruleResult) {
		this.ruleResult = ruleResult;
	}
	public int getNumOfRule() {
		return numOfRule;
	}
	public void setNumOfRule(int numOfRule) {
		this.numOfRule = numOfRule;
	}
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
}
