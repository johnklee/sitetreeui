package ntu.sd.performance.util;

public class RuleResult {

	private String ruleName;
	private int score;
	private String description;
	
	public RuleResult() {
		this.description = "";
		this.score = -1;
		this.ruleName = "";
	}
	
	public RuleResult(String ruleName,int score,String description) {
		this.description = description;
		this.score = score;
		this.ruleName = ruleName;
	}
	public String getRuleName() {
		return ruleName;
	}
	
	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void print() {
		System.out.println("RuleName: " + ruleName);
		System.out.println("score: " + score);
		System.out.println("Description: " + description);
	}
}
