package main;

import grasp.GRASP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import virtuoso.VirtuosoSql;
import weight.*;

public class DESummarization {
	
	public void CompareSummary(String entity1, String entity2){
		//得到实体对应的所有feature
		ArrayList<FeatureNode> feature1 = VirtuosoSql.findFeature(entity1);
		ArrayList<FeatureNode> feature2 = VirtuosoSql.findFeature(entity2);
		double[][] profit = new double[feature1.size()+feature2.size()][feature1.size()+feature2.size()];
		int[] weight = new int[feature1.size()+feature2.size()];
		
		//初始化profit和weight
		for(int i = 0; i< feature1.size(); i++){
			for(int j = 0; j< feature2.size(); j++){
				ProperyPair propair = new ProperyPair();
				double ppairc = propair.ComparableDegree(feature1.get(i).getPro(), feature2.get(j).getPro() ,0);
				double ppaird = propair.DistinctionDegree(feature1.get(i).getPro(), feature2.get(j).getPro());
				
				ValuePair valpair = new ValuePair();
				double vpair = valpair.Similarity(feature1.get(i).getVal(), feature2.get(i).getVal());
				
				double effectiveness = ppairc*ppaird*vpair;
				profit[i][j+feature1.size()] = effectiveness;
			}
		}
		for(int i = 0; i < weight.length; i++){
			weight[i] = 1;
		}
		
		//根据GRASP选择使profit最大的满足限制的feature
		GRASP grasps = new GRASP(weight.length, 6, profit, weight);
		ArrayList<Integer> Index = grasps.ProcessGRASP(1, 3, 1, 1);
		for(int i = 0; i < Index.size(); i++){
			if(Index.get(i)-feature1.size() < 0){
				int index = Index.get(i);
				feature1.get(index).setDis(true);
			}
			else{
				int index = Index.get(i)-feature1.size();
				feature2.get(index).setDis(true);
			}
		}
		
		//设定阀值，生成新的二位profit数组，为下面的怎么呈现摘要做准备
		double threshold = 0;
		ArrayList<ArrayList<Double>> display_profit = new ArrayList<ArrayList<Double>>();
		ArrayList<FeatureNode> feature1_display = new ArrayList<FeatureNode>();
		ArrayList<FeatureNode> feature2_display = new ArrayList<FeatureNode>();
		int k = 0;
		for(int i = 0; i < feature1.size(); i++){
			if(feature1.get(i).getDis() == true){
				FeatureNode node1 = new FeatureNode(feature1.get(i).getPro(), feature1.get(i).getVal(), 1, true, k);
				k++;
				feature1_display.add(node1);
				ArrayList<Double> row = new ArrayList<Double>();
				int p = 0;
				for(int j = 0; j < feature2.size(); j++){
					if(feature2.get(j).getDis() == true){
						if(profit[feature1.get(i).getIndex()][feature2.get(j).getIndex()] > threshold){
							row.add(profit[feature1.get(i).getIndex()][feature2.get(j).getIndex()]);
						}
						else
							row.add(0.0);
						if(i == 0){
							FeatureNode node2 = new FeatureNode(feature2.get(j).getPro(), feature2.get(j).getVal(), 2, true, p);
							p++;
							feature2_display.add(node2);
						}
					}
				}
				display_profit.add(row);
			}
		}
		
		//呈现摘要
		ArrayList<NodeSet> setlist = new ArrayList<NodeSet>();
		for(int i = 0; i< display_profit.size(); i++){
			ArrayList<Double> temp = display_profit.get(i);
			NodeSet nodeset = new NodeSet();
			for(int j = 0; j< temp.size(); j++){
				if(temp.get(j)> 0){
					int number = findNode(setlist,i,j);
					if(number == -1){
						nodeset.addNode(20+j);
						nodeset.addNode(10+i);
						nodeset.addProfit(temp.get(j));
						nodeset.addLength(1);
					}
					else{
						setlist.get(number).addNode(20+j);
						setlist.get(number).addNode(10+i);
						setlist.get(number).addProfit(temp.get(j));
						setlist.get(number).addLength(1);
					}
				}
			}
			if(nodeset.getNode().size() > 0){
				setlist.add(nodeset);
			}
		}
		
		
	}
	
	public int findNode(ArrayList<NodeSet> setlist,int e_num, int index){
		int number = -1;
		if(setlist.size() == 0)
			return -1;
		for(int i = 0; i < setlist.size(); i++){
			HashSet<Integer> nodeset = setlist.get(i).getNode();
			Iterator<Integer> it = nodeset.iterator();
			while(it.hasNext()){
				int node = it.next();
				if(node/10 == e_num && node%10 == index){
					number = i;
					break;
				}
			}
		}
		return number;
	}
	public static void main(String[] args){
		
	}
}
