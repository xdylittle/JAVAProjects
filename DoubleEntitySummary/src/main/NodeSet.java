package main;

import java.util.HashSet;

public class NodeSet {
	HashSet<Integer> nodelist;
	double total_profit;
	double total_length;
	
	public NodeSet(){
		nodelist = new HashSet<Integer>();
		total_profit = 0;
		total_length = 0;
	}
	
	public HashSet<Integer> getNode(){
		return this.nodelist;
	}
	public double getProfit(){
		return this.total_profit;
	}
	public double getLength(){
		return this.total_length;
	}
	
	public void addNode(int node){
		nodelist.add(node);
	}
	public void addProfit(double profit){
		this.total_profit += profit;
	}
	public void addLength(double length){
		this.total_length += length;
	}
}
