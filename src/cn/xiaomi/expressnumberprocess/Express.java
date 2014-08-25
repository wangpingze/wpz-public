package cn.xiaomi.expressnumberprocess;

public class Express implements Comparable<Express> {

	private String name;//快递公司名
	private String number;//快递单号
	public Express(String name, String number) {
		super();
		this.name = name;
		this.number = number;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getNumber() {
		return number;
	}
	public void setNumber(String number) {
		this.number = number;
	}
	@Override
	public String toString() {
		return name + "\t" + number;
	}
	@Override
	public int compareTo(Express o) {
		return this.name.compareTo(o.name);
	}
	
}
