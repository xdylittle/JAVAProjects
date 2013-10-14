package main;

import java.util.ArrayList;

import virtuoso.VirtuosoSql;
import weight.*;

public class DESummarization {
	
	public void CompareSummary(String entity1, String entity2){
		//�õ�ʵ���Ӧ������feature
		ArrayList<FeatureNode> feature1 = VirtuosoSql.findFeature(entity1);
		ArrayList<FeatureNode> feature2 = VirtuosoSql.findFeature(entity2);
		double[][] profit = new double[feature1.size()+feature2.size()][feature1.size()+feature2.size()];
		double[] weight = new double[feature1.size()+feature2.size()];
		
		//��ʼ��profit��weight
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
		
		//����GRASPѡ��ʹprofit�����������Ƶ�feature
		
		
		//����ժҪ
		
	}
	public static void main(String[] args){
		
	}
}
