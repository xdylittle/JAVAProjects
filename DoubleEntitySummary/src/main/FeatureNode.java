package main;

public class FeatureNode {
	private String property;
	private String value;
	int e_num;//��ʾ��ʵ��1����ʵ��2��feature
	int index;
	boolean display;//������ժҪ���Ƿ�Ҫ����
	
	public FeatureNode(String property, String value, int e_num, boolean display){
		this.property = property;
		this.value = value;
		this.e_num = e_num;
		this.display = display;
	}
	
	public String getPro(){
		return this.property;
	}
	public String getVal(){
		return this.value;
	}
	public int getENum(){
		return this.e_num;
	}
	public int getIndex(){
		return this.index;
	}
	public boolean getDis(){
		return display;
	}
	
	public void setPro(String property){
		this.property = property;
	}
	public void setVal(String value){
		this.value = value;
	}
	public void setENum(int e_num){
		this.e_num = e_num;
	}
	public void setIndex(int index){
		this.index = index;
	}
	public void setDis(boolean display){
		this.display = display;
	}
}
