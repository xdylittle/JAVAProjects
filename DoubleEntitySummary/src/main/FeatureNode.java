package main;

public class FeatureNode {
	private String property;
	private String value;
	int e_num;
	int index;
	
	public FeatureNode(String property, String value, int e_num){
		this.property = property;
		this.value = value;
		this.e_num = e_num;
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
}
