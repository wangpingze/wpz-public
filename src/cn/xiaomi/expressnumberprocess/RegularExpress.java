package cn.xiaomi.expressnumberprocess;

import java.util.regex.Pattern;

public class RegularExpress implements Comparable<RegularExpress> {

	private int fixNum;//规则中固定位数长度
	private int length;//规则匹配的快递长度
	private double rate;//固定位数长度所占的比例
	private Pattern regex;//规则
	public RegularExpress(int fixNum, int length,String regex) {
		super();
		this.fixNum = fixNum;
		this.length = length;
		this.rate=(double)fixNum/length;
		this.regex=Pattern.compile(regex);
	}
	public int getFixNum() {
		return fixNum;
	}
	public void setFixNum(int fixNum) {
		this.fixNum = fixNum;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
	public Pattern getRegex() {
		return regex;
	}
	public void setRegex(Pattern regex) {
		this.regex = regex;
	}
	@Override
	public String toString() {
		return  fixNum + "\t" + length
				+ "\t" + regex.pattern();
	}
	@Override
	public int compareTo(RegularExpress o) {
		if(this.rate>o.rate){
			return 1;
		}else if(this.rate<o.rate){
			return -1;
		}else{
			return 0;
		}
	}
	
	
}
