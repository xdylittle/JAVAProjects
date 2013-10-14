package main;

import grasp.GRASP;

import java.util.ArrayList;

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
		//呈现摘要
		
		
	}
	public static void main(String[] args){
		
	}
}
