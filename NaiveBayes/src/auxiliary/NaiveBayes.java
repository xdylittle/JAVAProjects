/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package auxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 *
 * @author daq
 */
public class NaiveBayes extends Classifier {
	HashMap<Double,Double> score;
	HashMap<Double,Integer> labelmap;
	ArrayList<Property> all_pro;
	double total;

    public NaiveBayes() {
    	labelmap = new HashMap<Double,Integer>();
    	all_pro = new ArrayList<Property>();
    	score = new HashMap<Double,Double>();
    }

    @Override
    public void train(boolean[] isCategory, double[][] features, double[] labels) {
    	//确定每个类的个数
    	handleMissData(features,labels);
    	for(int i = 0; i< labels.length; i++){
    		if(labelmap.containsKey(labels[i])){
    			int count = labelmap.get(labels[i]);
    			labelmap.put(labels[i], count+1);
    		}
    		else{
    			labelmap.put(labels[i], 1);
    			score.put(labels[i], 0.0);
    		}
    	}
    	total = features.length;
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
    		all_pro.add(property);
    	}
    	
    }

    @Override
    public double predict(double[] features) {
    	Set<Double> key = score.keySet();
    	Iterator<Double> it = key.iterator();
    	while(it.hasNext()){
    		double label = it.next();
    		double scoree = (double)labelmap.get(label)/total;
    		for(int i = 0; i< features.length; i++){
    			double p_p = 0;
    			int index = i;
    			double feature = features[i];
    			Property p = findP(index);
    			if(p.getIsNum() == true){
    				HashMap<Double,HashMap<Double,Integer>> info_pro = p.getInfoPro();
    				if(info_pro.containsKey(feature)){
    					if(info_pro.get(feature).containsKey(label)){
    						p_p = (double)info_pro.get(feature).get(label)/(double)labelmap.get(label);
    					}
    					else{
    						p_p = 1/(double)(labelmap.get(label)+info_pro.keySet().size()+1);
    					}
    				}
    				else{
    					//System.out.println(info_pro.get(feature).get(label));
    					p_p = 1/(double)(labelmap.get(label)+info_pro.keySet().size()+1);
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
    		score.put(label, scoree);
    	}
    	return max(score);
    }
    
    public double max(HashMap<Double,Double> score){
    	double max = 0;
    	double c = 0;
    	Iterator<Double> it = score.keySet().iterator();
    	while(it.hasNext()){
    		double key = it.next();
    		double current = score.get(key);
    		if(current >= max){
    			max = current;
    			c = key;
    		}
    		//System.out.println(key+" "+current);
    	}
    	//System.out.println(c);
    	return c;
    }
    public Property findP(int index){
    	for(int i= 0; i< all_pro.size(); i++){
    		if(all_pro.get(i).getIndex() == index)
    			return all_pro.get(i);
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
    
    /*public void handleMissData(double[][] features, double[] labels){
    	if(features == null){
       		return;
       	}
    	ArrayList<Double> de = new ArrayList<Double>();
    	for(int i = 0; i < features.length; i++){
    		for(int j = 0; j < features[0].length; j++){
    			if(Double.isNaN(features[i][j])){
    				de = FindD(features,labels,labels[i],j);
    				features[i][j] = modeD(de);
    				//System.out.println(modeD(de));
    			}
    		}
    	}
    }
    public ArrayList<Double> FindD(double[][] features, double[] labels, double label, int attr){
    	ArrayList<Double> temp = new ArrayList<Double>();
    	for(int i = 0; i < features.length; i++){
    		if(labels[i] == label){
    			if(!Double.isNaN(features[i][attr])){
    				temp.add(features[i][attr]);
    			}
    		}
    	}
    	return temp;
    }
    public double modeD(ArrayList<Double> labels){
    	Object[] temp = labels.toArray();
    	Arrays.sort(temp);
        int count = 1;
        int longest = 0;
        double most = 0;
        for (int i = 0; i < temp.length - 1; i++) {
            if ((double)temp[i] == (double)temp[i + 1]) {
                count++;
            } else {
                count = 1;// 如果不等于，就换到了下一个数，那么计算下一个数的次数时，count的值应该重新符值为一
                continue;
            }
            if (count > longest) {
                most = (double)temp[i];
                longest = count;
            }
        }
        return most;
    }*/
    
    public static void main(String[] args){
    	DataSet ds = new DataSet("D:\\eclipsespace\\NaiveBayes\\data\\breast-cancer.data");
    	//NaiveBayes nb = new NaiveBayes();
    	Evaluation eva = new Evaluation(ds, "NaiveBayes");
        eva.crossValidation();
        System.out.println("mean and standard deviation of accuracy:" + eva.getAccMean() + "," + eva.getAccStd());
        //System.out.println("mean and standard deviation of RMSE:" + eva.getRmseMean() + "," + eva.getRmseStd());
    	/*nb.train(ds.getIsCategory(), ds.getFeatures(), ds.getLabels());
    	for(int i = 0; i< 1; i++){
    		double label = nb.predict(ds.getFeatures()[i]);
    		//System.out.println(label);
    	}*/
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