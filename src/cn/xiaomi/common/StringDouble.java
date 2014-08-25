package cn.xiaomi.common;

public class StringDouble implements Comparable<StringDouble> {

	private double score;
	private String name;
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public StringDouble(double score, String name) {
		this.score = score;
		this.name = name;
	}
	public StringDouble(String target) {
		int index=target.indexOf(":");
		if(index<=0){
			this.score = 0;
			this.name = "";
		}else{
			this.name = target.substring(0,index);
			this.score =Double.parseDouble(target.substring(index+1));	
		}
	}
	@Override
	public String toString() {
		return name + ":" +String.format("%.5f", score);
	}
	@Override
	public int compareTo(StringDouble o) {
		if(this.score>o.score){
			return 1;
		}else if(this.score<o.score){
			return -1;
		}else{
			return 0;
		}
	}
	
}
