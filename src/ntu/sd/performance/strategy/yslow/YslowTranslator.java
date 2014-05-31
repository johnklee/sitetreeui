package ntu.sd.performance.strategy.yslow;

import java.util.ArrayList;

import ntu.sd.performance.util.RuleResult;

public class YslowTranslator {

	
	private int index = 0;
	public String StringParse(String s, int index){
		String temp="" ;
		index++;
		int endIndex = s.indexOf("\"", index);
		temp = s.substring(index, endIndex);
		//System.out.println(temp);
		return temp;
	}
	
	public String MessageParse(String s, int index){
		String temp="" ;
		index++;
		int endIndex = s.indexOf("\",", index);
		temp = s.substring(index, endIndex);
		//System.out.println(temp);
		return temp;
	}
	
	public String ScoreParse(String s, int index){
		String temp="" ;
		index++;
		int endIndex = s.indexOf(",", index);
		temp = s.substring(index, endIndex);
		//System.out.println(temp);
		return temp;
	}
	
	public String CompParse(String s, int index){
		String temp="" ;
		//System.out.println("??\n\n" + s.substring(index,index+5) + "??\n\n" );
		index=index+2;
		int endIndex = s.indexOf("]", index);
		temp = s.substring(index, endIndex);
		//System.out.println(temp);
		return temp;
	}
	
	public RuleResult RuleParse(String s){
		
		RuleResult r = new RuleResult();
		String temp;
		int ti= 0;
		//System.out.println(s.substring(index,index+3));
		
		
		
		if(s.substring(index, index+1).compareTo("\"")==0){
			temp = StringParse(s,index);
			r.setRuleName(temp);
			index= index+temp.length()+2;
			
			index = index+2;
		} // if
		
		if(s.substring(index, index+1).compareTo("\"")==0){
			ti = index;
			temp = StringParse(s,index);
			//System.out.println(temp);
			if (temp.compareTo("score")==0){
				
				index= index+temp.length()+2;
				if(s.substring(index, index+1).compareTo(":")==0){
					temp = ScoreParse(s,index);
					index= index+temp.length()+2;
					r.setScore(Integer.parseInt(temp));
				}// if
			}
			else index = ti;
		
			
		} // if
		
		if(s.substring(index, index+1).compareTo("\"")==0){
			temp = StringParse(s,index);
			index= index+temp.length()+3;
			if(s.substring(index, index+1).compareTo("\"")==0){
				temp = MessageParse(s,index);
				index= index+temp.length()+2;
				
				r.setDescription(temp);
			}// if
		
			index++;
			
		} // if
		
		if(s.substring(index, index+1).compareTo("\"")==0){
			temp = StringParse(s,index);
			index= index+temp.length()+2;
			if(s.substring(index, index+1).compareTo(":")==0){
				temp = CompParse(s,index);
				index= index+temp.length()+2;
				
				r.setDescription(r.getDescription()+"  "+temp);
			}// if
		
			index=index+3;
			
		} // if

		
		return r;
	}
	
	

	public ArrayList<RuleResult> ResParse(String s){
		index = s.indexOf("\"g\"");
		ArrayList<RuleResult> ResList = new ArrayList<RuleResult>();
		RuleResult r = new RuleResult();
		index=index+5;
		
		for(int i = 0 ; i < 23 ;i++) {
			//System.out.println(s.substring(index,index+8));
			r=RuleParse(s);
			ResList.add(r);
		}
	
		//System.out.println(s.substring(index,index+3));
		return ResList;
	}
	
}
