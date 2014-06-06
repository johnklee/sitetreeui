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
	
	
	public String setDes(String s){
		String temp="" ;
		if(s.compareTo("ynumreq")==0) return "Make fewer HTTP requests";
		else if(s.compareTo("ycdn")==0) return "Use a CDN";
		else if(s.compareTo("yemptysrc")==0) return "Avoid empty src or href";
		else if(s.compareTo("yexpires")==0) return "Add an Expires header";
		else if(s.compareTo("ycompress")==0) return "Compress components";
		else if(s.compareTo("ycsstop")==0) return "Put CSS at top";
		else if(s.compareTo("yjsbottom")==0) return "Put Javascript at the bottom";
		else if(s.compareTo("yexpressions")==0) return "Avoid CSS expression";
		else if(s.compareTo("yexternal")==0) return "Make JS and CSS external";
		else if(s.compareTo("ydns")==0) return "Reduce DNS lookups";
		else if(s.compareTo("yminify")==0) return "Minify JS and CSS";
		else if(s.compareTo("yredirects")==0) return "Avoid redirects";
		else if(s.compareTo("ydupes")==0) return "Remove duplicate JS and CSS";
		else if(s.compareTo("yetags")==0) return "Configure ETags";
		else if(s.compareTo("yxhr")==0) return "Make Ajax cacheable";
		else if(s.compareTo("yxhrmethod")==0) return "Use GET for AJAX requests";
		else if(s.compareTo("ymindom")==0) return "Reduce the Number of DOM elements";
		else if(s.compareTo("yno404")==0) return "No 404s";
		else if(s.compareTo("ymincookie")==0) return "Reduce Cookie Size";
		else if(s.compareTo("ycookiefree")==0) return "Use Cookie-free Domains";
		else if(s.compareTo("ynofilter")==0) return "Avoid filters";
		else if(s.compareTo("yimgnoscale")==0) return "Don't Scale Images in HTML";
		else if(s.compareTo("yfavicon")==0) return "Make favicon Small and Cacheable";
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
			r.setDescription(setDes(temp));
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
				if(temp.length()!=0)
					r.setDescription(r.getDescription()+":"+temp);
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
