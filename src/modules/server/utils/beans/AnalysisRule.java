package modules.server.utils.beans;

public class AnalysisRule {
	String rule;
	String description;
	int score;
	
	public AnalysisRule(String r, String des, int score)
	{
		this.rule = r;
		this.description = des;
		this.score = score;
	}
	
	public String getRule() {
		return rule;
	}
	public void setRule(String rule) {
		this.rule = rule;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public int getScore() {
		return score;
	}
	public void setScore(int score) {
		this.score = score;
	}		
}
