/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;


/**
 *
 * @author daq
 */
public class AdaBoost extends Classifier {

	ArrayList<HashMap<Double,Double>> score;
	ArrayList<HashMap<Double,Integer>> labelmap;
	ArrayList<ArrayList<Property>> all_pro;
	ArrayList<Double> total;
	ArrayList<Double> weights;
	int T;
	
    public AdaBoost() {
    	
    }

    @Override
    public void train(boolean[] isCategory, double[][] features, double[] labels) {
    	Random random = new Random();
    	handleMissData(features,labels);
    	T = 10;
    	double[] D = new double[features.length];
    	for(int i = 0; i< D.length; i++)
    		D[i] = 1/(double)features.length;
    	labelmap = new ArrayList<HashMap<Double,Integer>>(T);
    	all_pro = new ArrayList<ArrayList<Property>>(T);
    	score = new ArrayList<HashMap<Double,Double>>(T);
    	total = new ArrayList<Double>(T);
    	weights = new ArrayList<Double>(T);
    	for(int number = 0; number < T; number++){
    		labelmap.add(new HashMap<Double,Integer>());
    		all_pro.add(new ArrayList<Property>());
    		score.add(new HashMap<Double,Double>());
    		
    		ArrayList<ArrayList<Double>> featurestemp = new ArrayList<ArrayList<Double>>();
    		ArrayList<Double> labelstemp = new ArrayList<Double>();
    		while(featurestemp.size() == 0){
    			featurestemp.clear();
    			labelstemp.clear();
    			for(int i = 0; i< features.length; i++){
    				ArrayList<Double> featuretemp = new ArrayList<Double>();
    				if(random.nextDouble() < D[i]){
    					for(int j = 0; j< features[0].length; j++){
    						featuretemp.add(features[i][j]);
    					}
    					featurestemp.add(featuretemp);
    					labelstemp.add(labels[i]);
    				}
    			}
    		}
    		
    		double[][] featuresx = new double[featurestemp.size()][features[0].length];
    		double[] labelsx = new double[featurestemp.size()];
    		for(int i = 0; i< featurestemp.size(); i++){
    			for(int j = 0; j < featurestemp.get(0).size(); j++)
    				featuresx[i][j] = featurestemp.get(i).get(j);
    			labelsx[i] = labelstemp.get(i);
    		}
    		
    		trainAda(isCategory,featuresx,labelsx,number);
    		
    		double[] labels_predict = new double[features.length];
    		double error = 0;
    		for(int i = 0; i< features.length; i++){
    			double label = predictTest(features[i],number);
    			labels_predict[i] = label;
    			if(label - labels[i] != 0)
    				error += 1.0;
    		}
    		if(error == 0){
    			T = number-1;
    			break;
    		}
    		error = error/features.length;
    		double alpha = 0.5*Math.log((1-error)/error);
    		double fenmu = 0.0;
    		for(int i = 0; i< D.length; i++){
    			if(labels_predict[i] - labels[i] > 0.0001){
    				D[i] = D[i]*Math.pow(Math.E, alpha);
    			}
    			else{
    				D[i] = D[i]*Math.pow(Math.E, -alpha);
    			}
    			if(fenmu < D[i])
    				fenmu = D[i];
    		}
    		for(int i = 0; i< D.length; i++){
    			D[i] = D[i]/fenmu;
    		}
    		
        	weights.add(alpha);
    	}
    }
    
    public void trainAda(boolean[] isCategory, double[][] features, double[] labels, int number) {
    	//确定每个类的个数
    	for(int i = 0; i< labels.length; i++){
    		if(labelmap.get(number).containsKey(labels[i])){
    			int count = labelmap.get(number).get(labels[i]);
    			labelmap.get(number).put(labels[i], count+1);
    		}
    		else{
    			labelmap.get(number).put(labels[i], 1);
    			score.get(number).put(labels[i], 0.0);
    		}
    	}
    	total.add((double)features.length);
    	for(int i = 0; i < features[0].length; i++){
    		ArrayList<Double> featuretemp = new ArrayList<Double>();
    		for(int j = 0; j < features.length; j++){
    			featuretemp.add(features[j][i]);
    		}
    		Property property = new Property(i,isCategory[i]);
    		if(isCategory[i] == true)
    			property.setInfo_pro(featuretemp, labels);
    		else{
    			property.setAS(featuretemp, labels);
    		}
    		all_pro.get(number).add(property);
    	}
    	
    }

    @Override
    public double predict(double[] features) {
    	ArrayList<Double> labels = new ArrayList<Double>();
    	HashMap<Double,Double> labels_weight = new HashMap<Double,Double>();
    	for(int number = 0; number < T; number ++){
    		labels.add(predictTest(features,number));
    	}
    	for(int i = 0; i< labels.size(); i++){
    		if(labels_weight.containsKey(labels.get(i))){
    			double temp = labels_weight.get(labels.get(i));
    			temp = temp + weights.get(i);
    			labels_weight.put(labels.get(i), temp);
    		}
    		else{
    			labels_weight.put(labels.get(i), weights.get(i));
    		}
    	}
        return max(labels_weight);
    }
    
    public double predictTest(double[] features,int number){
    	Set<Double> key = score.get(number).keySet();
    	Iterator<Double> it = key.iterator();
    	while(it.hasNext()){
    		double label = it.next();
    		double scoree = (double)labelmap.get(number).get(label)/total.get(number);
    		for(int i = 0; i< features.length; i++){
    			double p_p = 0;
    			int index = i;
    			double feature = features[i];
    			Property p = findP(index,number);
    			if(p.getIsNum() == true){
    				HashMap<Double,HashMap<Double,Integer>> info_pro = p.getInfoPro();
    				if(info_pro.containsKey(feature)){
    					if(info_pro.get(feature).containsKey(label)){
    						p_p = (double)info_pro.get(feature).get(label)/(double)labelmap.get(number).get(label);
    					}
    					else{
    						p_p = 1/(double)(labelmap.get(number).get(label)+info_pro.keySet().size()+1);
    					}
    				}
    				else{
    					//System.out.println(info_pro.get(feature).get(label));
    					p_p = 1/(double)(labelmap.get(number).get(label)+info_pro.keySet().size()+1);
    				}
    			}
    			else{
    				double avg = p.getAvg().get(label);
    				double std = p.getStd().get(label);
    				double a = Math.PI;
    				double b = 0-(feature-avg)*(feature-avg)/2/(std*std);
    				p_p = Math.pow(a, b);
    				p_p = p_p/(Math.sqrt(2*Math.PI)*std);
    				if(std == 0)
    					p_p = 1;
    			}
    			scoree = scoree*p_p;
    		}
    		score.get(number).put(label, scoree);
    	}
    	return max(score.get(number));
    }
    
    public static void main(String[] args){
    	DataSet ds = new DataSet("./data/segment.data");
    	//NaiveBayes nb = new NaiveBayes();
    	Evaluation eva = new Evaluation(ds, "AdaBoost");
        eva.crossValidation();
        System.out.println("mean and standard deviation of accuracy:" + eva.getAccMean() + "," + eva.getAccStd());
        //System.out.println("mean and standard deviation of RMSE:" + eva.getRmseMean() + "," + eva.getRmseStd());
    	/*nb.train(ds.getIsCategory(), ds.getFeatures(), ds.getLabels());
    	for(int i = 0; i< 1; i++){
    		double label = nb.predict(ds.getFeatures()[i]);
    		//System.out.println(label);
    	}*/
    }
    
    public double max(HashMap<Double,Double> scorex){
    	double max = 0;
    	double c = 0;
    	Iterator<Double> it = scorex.keySet().iterator();
    	while(it.hasNext()){
    		double key = it.next();
    		double current = scorex.get(key);
    		if(current >= max){
    			max = current;
    			c = key;
    		}
    		//System.out.println(key+" "+current);
    	}
    	//System.out.println(c);
    	return c;
    }
    public Property findP(int index, int number){
    	for(int i= 0; i< all_pro.get(number).size(); i++){
    		if(all_pro.get(number).get(i).getIndex() - index == 0)
    			return all_pro.get(number).get(i);
    	}
    	return null;
    }
    
    public void handleMissData(double[][] features, double[] labels){
       	if(features == null){
       		return;
       	}
       	List<ArrayList> missIndex = new ArrayList<ArrayList>();
       	for(int i=0; i<features[0].length; i++){
       		missIndex.add(new ArrayList());
       	}
       	double[] sumToAvg = new double[features[0].length];
       	for(int i=0; i<features.length; i++){
       		for(int j=0; j<features[0].length; j++){
       			if(features[i][j] >=0 || features[i][j]<=0)
       				sumToAvg[j] += features[i][j];
       			else{
       				missIndex.get(j).add(i);
       			}
       		}
       	}
       	for(int i=0; i<features[0].length; i++){
       		if(missIndex.get(i).size() > 0){
       			sumToAvg[i] = Math.round((sumToAvg[i]/(features.length-missIndex.get(i).size())));
   	    		for(int j=0; j<missIndex.get(i).size(); j++){
   	    			features[(int)missIndex.get(i).get(j)][i] = sumToAvg[i];
   	    		}
       		}
       	}
    }
}

class Property{
	boolean isNum;
	int index;
	boolean useful;
	HashMap<Double,HashMap<Double,Integer>> info_pro;
	HashMap<Double,Double> Avg;
	HashMap<Double,Double> Std;
	
	public Property(int index, boolean isNum){
		this.isNum = isNum;
		this.index = index;
	}
	
	public boolean getUse(){
		return this.useful;
	}
	public int getIndex(){
		return this.index;
	}
	
	public boolean getIsNum(){
		return this.isNum;
	}
	
	public HashMap<Double,HashMap<Double,Integer>> getInfoPro(){
		return this.info_pro;
	}
	
	public HashMap<Double,Double> getAvg(){
		return this.Avg;
	}
	
	public HashMap<Double,Double> getStd(){
		return this.Std;
	}
	public void setInfo_pro(ArrayList<Double> feature, double[] labels){
		info_pro = new HashMap<Double,HashMap<Double,Integer>>();
		HashSet<Double> featuretemp = new HashSet<Double>();
		for(int i = 0; i< feature.size(); i++){
			featuretemp.add(feature.get(i));
		}
		Iterator<Double> it = featuretemp.iterator();
		while(it.hasNext()){
			info_pro.put(it.next(), new HashMap<Double,Integer>());
		}
		for(int i = 0; i< feature.size(); i++){
			HashMap<Double, Integer> temp = info_pro.get(feature.get(i));
			if(temp.containsKey(labels[i])){
				int count = temp.get(labels[i]);
				temp.put(labels[i], count+1);
				info_pro.put(feature.get(i), temp);
			}
			else{
				//System.out.println(labels[i]);
				temp.put(labels[i], 1);
				info_pro.put(feature.get(i), temp);
			}
		}
		Set<Double> key_feature = info_pro.keySet();
		it = key_feature.iterator();
		/*while(it.hasNext()){
			double key1 = it.next();
			HashMap temp = info_pro.get(key1);
			Set<Double> key_label = temp.keySet();
			Iterator<Double> it1 = key_label.iterator();
			while(it1.hasNext()){
				double key2 = it1.next();
				System.out.println(key1+" "+key2+" "+temp.get(key2));
			}
		}
		System.out.println();*/
	}
	
	public void setAS(ArrayList<Double> feature, double[] labels){
		Avg = new HashMap<Double,Double>();
		Std = new HashMap<Double,Double>();
		HashSet<Double> labeltemp = new HashSet<Double>();
		for(int i = 0; i< labels.length; i++){
			labeltemp.add(labels[i]);
		}
		Iterator<Double> it = labeltemp.iterator();
		while(it.hasNext()){
			ArrayList<Double> ftemp = new ArrayList<Double>();
			double label = it.next();
			double sum = 0;
			double avg = 0;
			for(int i = 0; i< labels.length; i++){
				if(labels[i] == label){
					sum = sum + feature.get(i);
					ftemp.add(feature.get(i));
				}
			}
			avg = sum/ftemp.size();
			Avg.put(label, avg);
			sum = 0;
			for(int i = 0; i < ftemp.size(); i++){
				sum = sum + (avg-ftemp.get(i))*(avg-ftemp.get(i));
			}
			//System.out.println(avg+" "+sum);
			Std.put(label, Math.sqrt(sum/ftemp.size()));
		}
	}
}
