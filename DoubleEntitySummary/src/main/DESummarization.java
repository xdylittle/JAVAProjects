package main;

import grasp.GRASP;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;

import virtuoso.VirtuosoSql;
import weight.*;

public class DESummarization {
	
	public void CompareSummary(String entity1, String graphiri1, String entity2, String graphiri2){
		//得到实体对应的所有feature
		ArrayList<FeatureNode> feature1 = VirtuosoSql.findFeature(entity1,graphiri1,1);
		ArrayList<FeatureNode> feature2 = VirtuosoSql.findFeature(entity2,graphiri2,2);
		double[][] profit = new double[feature1.size()+feature2.size()][feature1.size()+feature2.size()];
		int[] weight = new int[feature1.size()+feature2.size()];
		
		//初始化profit和weight
		for(int i = 0; i< feature1.size(); i++){
			for(int j = 0; j< feature2.size(); j++){
				ProperyPair propair = new ProperyPair();
				double ppairc = propair.ComparableDegree(feature1.get(i).getPro(), feature2.get(j).getPro() ,0);
				double ppaird = propair.DistinctionDegree(feature1.get(i).getPro(),graphiri1, feature2.get(j).getPro(), graphiri2);
				
				ValuePair valpair = new ValuePair();
				double vpair = valpair.Similarity(feature1.get(i).getVal(), feature2.get(j).getVal(),0);
				
				double effectiveness = ppairc*ppaird*vpair;
				
				if(Double.isNaN(effectiveness) == true){
					effectiveness = 0;
				}
				if(i == 0 && j == 8){
					System.out.println(feature2.get(j).getVal()+" "+feature1.get(i).getVal()+" "+vpair);
				}
				profit[i][j+feature1.size()] = effectiveness;
				profit[j+feature1.size()][i] = effectiveness;
			}
		}
		for(int i = 0; i < feature1.size(); i++){
			for(int j = 0; j< feature1.size(); j++){
				if(i == j){
					FeatureSelf featureself = new FeatureSelf();
					profit[i][j] = featureself.SelfInformation(feature1.get(i).getPro(), feature1.get(i).getVal());
				}
				else{
					profit[i][j] = 0;
				}
			}
		}
		for(int i = 0; i < feature2.size(); i++){
			for(int j = 0; j< feature2.size(); j++){
				if(i == j){
					FeatureSelf featureself = new FeatureSelf();
					profit[i+feature1.size()][j+feature1.size()] = featureself.SelfInformation(feature2.get(i).getPro(), feature2.get(i).getVal());
				}
				else{
					profit[i+feature1.size()][j+feature1.size()] = 0;
				}
			}
		}
		for(int i = 0; i< profit.length; i++){
			for(int j = 0; j< profit[0].length; j++){
				System.out.print(profit[i][j]+" ");
			}
			System.out.println();
		}
		for(int i = 0; i < weight.length; i++){
			weight[i] = 1;
		}
		
		//根据GRASP选择使profit最大的满足限制的feature
		int constration = 6;
		GRASP grasps = new GRASP(weight.length, constration, profit, weight);
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
		
		for(int i = 0; i< Index.size(); i++){
			System.out.println(Index.get(i));
		}
		
		//设定阀值，生成新的二位profit数组，为下面的怎么呈现摘要做准备
		double threshold = 0;
		ArrayList<ArrayList<Double>> display_profit = new ArrayList<ArrayList<Double>>();
		ArrayList<FeatureNode> feature1_display = new ArrayList<FeatureNode>();
		ArrayList<FeatureNode> feature2_display = new ArrayList<FeatureNode>();
		int k = 0;
		for(int i = 0; i < feature1.size(); i++){
			if(feature1.get(i).getDis() == true){
				FeatureNode node1 = new FeatureNode(feature1.get(i).getPro(), feature1.get(i).getVal(), 1, false, k);
				k++;
				feature1_display.add(node1);
				ArrayList<Double> row = new ArrayList<Double>();
				int p = 0;
				for(int j = 0; j < feature2.size(); j++){
					if(feature2.get(j).getDis() == true){
						if(profit[feature1.get(i).getIndex()][feature2.get(j).getIndex()+feature1.size()] > threshold){
							row.add(profit[feature1.get(i).getIndex()][feature2.get(j).getIndex()+feature1.size()]);
						}
						else
							row.add(0.0);
						if(k == 1){
							FeatureNode node2 = new FeatureNode(feature2.get(j).getPro(), feature2.get(j).getVal(), 2, false, p);
							p++;
							feature2_display.add(node2);
						}
					}
				}
				display_profit.add(row);
			}
		}
		for(int i = 0; i< feature1_display.size(); i++){
			System.out.println(feature1_display.get(i).getPro()+" ");
		}
		System.out.println("*************************************");
		for(int i = 0; i< feature2_display.size(); i++){
			System.out.println(feature2_display.get(i).getPro()+" ");
		}
		System.out.println("*************************************");
		for(int i = 0; i< display_profit.size(); i++){
			ArrayList<Double>temp = display_profit.get(i);
			for(int j = 0; j< temp.size(); j++){
				System.out.print(temp.get(j)+" ");
			}
			System.out.println();
		}
		
		//呈现摘要
		ArrayList<NodeSet> setlist = new ArrayList<NodeSet>();
		for(int i = 0; i< display_profit.size(); i++){
			ArrayList<Double> temp = display_profit.get(i);
			NodeSet nodeset = new NodeSet();
			for(int j = 0; j< temp.size(); j++){
				if(temp.get(j)> 0){
					int number = findNode(setlist,2,j);
					if(number == -1){
						nodeset.addNode(20+j);
						feature2_display.get(j).setDis(true);
						nodeset.addNode(10+i);
						feature1_display.get(i).setDis(true);
						nodeset.addProfit(temp.get(j));
						nodeset.addLength(1);
					}
					else{
						setlist.get(number).addNode(20+j);
						feature2_display.get(j).setDis(true);
						setlist.get(number).addNode(10+i);
						feature1_display.get(i).setDis(true);
						setlist.get(number).addProfit(temp.get(j));
						setlist.get(number).addLength(1);
					}
				}
			}
			if(nodeset.getNode().size() > 0){
				setlist.add(nodeset);
			}
		}
		sort(setlist);
		for(int i = 0; i < setlist.size(); i++){
			NodeSet nodeset = setlist.get(i);
			HashSet<Integer> node = nodeset.getNode();
			Iterator<Integer> it = node.iterator();
			while(it.hasNext()){
				int temp = it.next();
				if(temp/10 == 1){
					String pro = feature1_display.get(temp%10).getPro();
					String val = feature1_display.get(temp%10).getVal();
					System.out.println(feature1_display.get(temp%10).getENum()+" "+pro+"    "+val);
				}
				else{
					String pro = feature2_display.get(temp%10).getPro();
					String val = feature2_display.get(temp%10).getVal();
					System.out.println(feature2_display.get(temp%10).getENum()+" "+pro+"    "+val);
				}
			}
			
		}
		System.out.println("-------------------------------------------------------------------");
		for(int i = 0; i< feature1_display.size(); i++){
			if(feature1_display.get(i).getDis() == false){
				String pro = feature1_display.get(i).getPro();
				String val = feature1_display.get(i).getVal();
				System.out.println(feature1_display.get(i).getENum()+" "+pro+" "+val);
			}
		}
		for(int i = 0; i < feature2_display.size(); i++){
			if(feature2_display.get(i).getDis() == false){
				String pro = feature2_display.get(i).getPro();
				String val = feature2_display.get(i).getVal();
				System.out.println(feature2_display.get(i).getENum()+" "+pro+" "+val);
			}
		}
		
	}
	
	public void sort(ArrayList<NodeSet> setlist){
		for(int i=0;i<setlist.size()-1;i++){
			for(int j=i+1;j<setlist.size();j++){
				double profiti = setlist.get(i).getProfit()/setlist.get(i).getLength();
				double profitj = setlist.get(j).getProfit()/setlist.get(j).getLength();
				if (profiti > profitj){
					NodeSet temp=setlist.get(i);
					setlist.set(i, setlist.get(j));
					setlist.set(j, temp);
			 	}
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
		DESummarization test = new DESummarization();
		test.CompareSummary("http://data.linkedmdb.org/resource/performance/71441", "http://linkedmdb.org", "http://data.linkedmdb.org/resource/performance/71442","http://linkedmdb.org");
	}
}
