package auxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;  
import java.util.Random;
import java.util.concurrent.ExecutionException;  
import java.math.BigDecimal;  

import static java.lang.Math.*;  

/**
 *
 * @author daq
 */
public class RandomForest extends Classifier {

    boolean isClassification;
    ArrayList<TreeNode> rootlist;

    public RandomForest() {
    	isClassification = true;
    	rootlist = new ArrayList<TreeNode>();
    }

	/*
	* isCategory[k] indicates whether the kth attribut is discrete or continuous, the last attribute is the label
	* features[i] is the feature vector of the ith sample
	* labels[i] is the label of he ith sample
	*/
	@Override
    public void train(boolean[] isCategory, double[][] features, double[] labels) {
		handleMissData(features,labels);
		Random random = new Random();
        for(int number = 0; number < 10; number++){
        	ArrayList<ArrayList<Double>> featurestemp = new ArrayList<ArrayList<Double>>();
    		ArrayList<Double> labelstemp = new ArrayList<Double>();
        	ArrayList<Integer> CandAttr = new ArrayList<Integer>();
        	for(int i = 0; i< features[0].length; i++)
        		CandAttr.add(i);
        	for(int i = 0; i< features.length; i++){
        		int index = Math.abs(random.nextInt())%features.length;
        		//int index = i;
        		ArrayList<Double> featuretemp = new ArrayList<Double>();
        		for(int j = 0; j< features[0].length; j++){
        			featuretemp.add(features[index][j]);
        		}
        		featurestemp.add(featuretemp);
        		labelstemp.add(labels[index]);
        	}
        	rootlist.add(buildTree(featurestemp,CandAttr,labelstemp,0,isCategory,1));
        }
    }

	/*
	* features is the feature vector of the test sample
	* you need to return the label of test sample
	*/
    @Override
    public double predict(double[] features) {
    	ArrayList<Double> labels = new ArrayList<Double>();
    	for(int number = 0; number < rootlist.size(); number++){
            TreeNode node = rootlist.get(number);
            while(node.child.size() > 0){
            	if(node.getType() == 1){
            		double value = node.child.get(0).getValue();
            		if(features[(int)node.getName()] <= value){
            			node = node.child.get(0);
            		}
            		else
            			node = node.child.get(1);
            	}
            	else{
            		int size = node.child.size();
            		int i = 0;
            		int index = 0;
            		double best = 0;
            		double current = 0;
            		for(i = 0; i < size; i++){
            			current = Math.abs(node.child.get(i).getValue()-features[(int)node.getName()]);
            			if(current <= best){
            				index = i;
            				best = current;
            			}
            		}
            		node = node.child.get(index);
            	}
            }
            labels.add(node.getName()); 
    	}
    	return mode(labels);
    }
    
    public TreeNode buildTree(ArrayList<ArrayList<Double>> D, ArrayList<Integer> attribute_list, ArrayList<Double> labels, double attr_value, boolean[] isCategory, int depth){
    	TreeNode node = new TreeNode();
    	/*for(int i = 0; i < labels.length; i++){
    		System.out.print(labels[i]);
    	}
    	System.out.println();*/
    	node.setValue(attr_value);
    	node.setDepth(depth);
    	if(Issameclass(labels)){
    		node.setName(labels.get(0));
    		return node;
    	}
    	if(attribute_list.size() == 0 || depth == 15){
    		node.setName(mode(labels));
    		return node;
    	}
    	Gain gain = new Gain(D,labels,attribute_list,isCategory);
    	int splitting_attribute = gain.bestAttr();
    	if(splitting_attribute == -1){
    		node.setName(mode(labels));
    		return node;
    	}
    	/*for(int i = 0; i< attribute_list.size(); i++){
    		System.out.print(attribute_list.get(i)+" ");
    	}
    	System.out.print(D.length+" "+labels.length+" ");
    	System.out.println(splitting_attribute);*/
    	node.setName(splitting_attribute);
    	ArrayList<Integer> attributej = new ArrayList<Integer>();
    	for(int i = 0; i< attribute_list.size(); i++){
    		if(attribute_list.get(i) != splitting_attribute)
    			attributej.add(attribute_list.get(i));
    	}
    	if(isCategory[splitting_attribute] == true){
    		node.setType(0);
    		HashSet<Double> values = gain.getValues(splitting_attribute);
    		Iterator<Double> it = values.iterator();
    		while(it.hasNext()){
    			double value = it.next();
    			ArrayList<ArrayList<Double>> Dj = new ArrayList<ArrayList<Double>>();
    			ArrayList<Double> labelsj = new ArrayList<Double>();
    			for(int i = 0; i < D.size(); i++){
    				ArrayList<Double> Djtemp = new ArrayList<Double>();
    				if(D.get(i).get(splitting_attribute) == value){
    					for(int j = 0; j < D.get(0).size(); j++){
    						Djtemp.add(D.get(i).get(j));
    					}
    					labelsj.add(labels.get(i));
    					Dj.add(Djtemp);
    				}
    			}
    			node.child.add(buildTree(Dj,attributej,labelsj,value,isCategory,depth+1));
    		}
    	}
    	else{
    		node.setType(1);
    		double bestValue = gain.getSplittingValue();
    		ArrayList<ArrayList<Double>> Dj1 = new ArrayList<ArrayList<Double>>();
    		ArrayList<Double> labelsj1 = new ArrayList<Double>();
			ArrayList<ArrayList<Double>> Dj2 = new ArrayList<ArrayList<Double>>();
			ArrayList<Double> labelsj2 = new ArrayList<Double>();
			for(int i = 0; i < D.size(); i++){
				ArrayList<Double> Dj1temp = new ArrayList<Double>();
				ArrayList<Double> Dj2temp = new ArrayList<Double>();
				if(D.get(i).get(splitting_attribute) <= bestValue){
					for(int j = 0; j < D.get(0).size(); j++){
						Dj1temp.add(D.get(i).get(j));
					}
					labelsj1.add(labels.get(i));
					Dj1.add(Dj1temp);
				}
				else{
					for(int j = 0; j < D.get(0).size(); j++){
						Dj2temp.add(D.get(i).get(j));
					}
					labelsj2.add(labels.get(i));
					Dj2.add(Dj2temp);
				}
			}
			node.child.add(buildTree(Dj1,attributej,labelsj1,bestValue,isCategory,depth+1));
			node.child.add(buildTree(Dj2,attributej,labelsj2,bestValue,isCategory,depth+1));
    	}
    	return node;
    }
    public double avg(double[] labels){
    	double sum = 0;
    	for(int i = 0; i< labels.length; i++){
    		sum += labels[i];
    	}
    	return sum/labels.length;
    }
    public boolean Issameclass(ArrayList<Double> labels){
    	if(labels.size() == 1){
    		return true;
    	}
    	for(int i = 1; i< labels.size(); i++){
    		if(labels.get(i) != labels.get(0))
    			return false;
    	}
    	return true;
    }
    public double mode(ArrayList<Double> labels){
    	double most = labels.get(0);
    	sort(labels);
        int count = 1;
        int longest = 0;
        for (int i = 0; i < labels.size() - 1; i++) {
            if (labels.get(i) - labels.get(i+1) == 0) {
                count++;
            } else {
                count = 1;// 如果不等于，就换到了下一个数，那么计算下一个数的次数时，count的值应该重新符值为一
                continue;
            }
            if (count > longest) {
                most = labels.get(i);
                longest = count;
            }
        }
        return most;
    }
    public void sort(ArrayList<Double> labels){
    	for(int i=0;i<labels.size()-1;i++){
    		for(int j=i+1;j<labels.size();j++){
    			if (labels.get(i) > labels.get(j)){
    				double temp=labels.get(i);
    				labels.set(i, labels.get(j));
    				labels.set(j, temp);
    			}
    		}
    	}
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
    }
    /*public void handleMissData(double[][] features, double[] labels){
    	System.out.println(Double.isNaN(Double.parseDouble("NaN")));
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
    }*/
    public void handleMissData(double[][] features, double[] labels){
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
    public static void main(String[] args){
    	DataSet ds = new DataSet("./data/breast-cancer.data");
    	RandomForest dt = new RandomForest();
    	Evaluation eva = new Evaluation(ds, "RandomForest");
        eva.crossValidation();
        System.out.println("mean and standard deviation of accuracy:" + eva.getAccMean() + "," + eva.getAccStd());
        //System.out.println("mean and standard deviation of RMSE:" + eva.getRmseMean() + "," + eva.getRmseStd());
    	//dt.train(ds.getIsCategory(), ds.getFeatures(), ds.getLabels());
    	//for(int i = 0; i< ds.getFeatures().length; i++){
    		//double label = dt.predict(ds.getFeatures()[i]);
    		//System.out.println(label);
    	//}
    }
    
}

class TreeNode{
	private double name; //节点名（分裂属性的名称）   
    private double value;
    ArrayList<TreeNode> child; //子结点集合  
    private int nodetype;
    private int depth;
    public TreeNode() {  
        this.name = 0;  
        //this.rule = new ArrayList<String>();  
        this.child = new ArrayList<TreeNode>(); 
        this.nodetype = 0;//表示离散
    }  
    public ArrayList<TreeNode> getChild() {  
        return child;  
    }  
    public void setChild(ArrayList<TreeNode> child) {  
        this.child = child;  
    }  
    public double getValue(){
    	return value;
    }
    public void setValue(double value){
    	this.value = value;
    }
    public double getName() {  
        return name;  
    }  
    public void setName(double name) {  
        this.name = name;  
    }
    public int getType(){
    	return this.nodetype;
    }
    public void setType(int type){
    	this.nodetype = type;
    }
    public int getDepth(){
    	return this.depth;
    }
    public void setDepth(int depth){
    	this.depth = depth;
    }
}

class Gain{
	ArrayList<ArrayList<Double>> D;
	ArrayList<Double> labels;
	ArrayList<Integer> attr_list;
	boolean[] isCategory;
	double splitting_value;
	public Gain(ArrayList<ArrayList<Double>> D, ArrayList<Double> labels, ArrayList<Integer> attr_list, boolean[] isCategory){
		this.D = D;
		this.labels = labels;
		this.attr_list = attr_list;
		this.isCategory = isCategory;
	}
	public int bestAttr(){
		int index = 0;
		double best = 0.0;
		double best_value = 0.0;
		double current = 0.0;
		for(int i = 0; i < attr_list.size(); i++){
			current = gainInfo(D, labels, attr_list.get(i), isCategory);
			if(current >= best){
				index = attr_list.get(i);
				best = current;
				best_value = this.splitting_value;
			}
		}
		/*System.out.println(index+" "+best_value+" "+this.D.length);
		for(int i = 0; i < this.D.length; i++){
			for(int j = 0; j < attr_list.size(); j++){
				System.out.print(this.D[i][attr_list.get(j)]+" ");
			}
			System.out.print(labels[i]);
			System.out.println();
		}
		for(int i = 0; i< attr_list.size(); i++)
			System.out.print(attr_list.get(i)+" ");
		System.out.println();*/
		this.splitting_value = best_value;
		if(best == 0.0)
			return -1;
		return index;
	}
	
	public double gainInfo(ArrayList<ArrayList<Double>> D, ArrayList<Double> labels, int attr, boolean[] isCategory){
		double gain = 0;
		if(isCategory[attr] == true){
			int i = 0;
			double info = 0;
			double infoa = 0;
			HashSet<Double> hash_labels = new HashSet<Double>();
			HashMap<Double,Integer> column = new HashMap<Double,Integer>();
			HashSet<Double> hash_D = new HashSet<Double>();
			HashMap<Double,Integer> row = new HashMap<Double,Integer>();
			for(i = 0; i< labels.size(); i++)
				hash_labels.add(labels.get(i));
			Iterator<Double> it = hash_labels.iterator();
			i = 0;
			while(it.hasNext()){
				column.put(it.next(), i);
				i ++;
			}
			for(i = 0; i< D.size(); i++){
				hash_D.add(D.get(i).get(attr));
			}
			it = hash_D.iterator();
			i = 0;
			while(it.hasNext()){
				row.put(it.next(), i);
				i ++;
			}
			double[][] pre_info = new double[hash_D.size()+1][hash_labels.size()+1];
			for(i = 0; i< pre_info.length; i++){
				for(int j = 0; j< pre_info[0].length; j++){
					pre_info[i][j] = 0;
				}
			}
			for(i = 0; i < labels.size(); i++){
				int row_index = row.get(D.get(i).get(attr));
				int col_index = column.get(labels.get(i));
				pre_info[row_index][col_index]++;
				pre_info[row_index][hash_labels.size()]++;
				pre_info[hash_D.size()][col_index]++;
			}
			for(i = 0; i< pre_info.length-1; i++){
				pre_info[hash_D.size()][hash_labels.size()] += pre_info[i][hash_labels.size()];
			}
			double sum = pre_info[hash_D.size()][hash_labels.size()];
			for(i = 0; i< pre_info[0].length-1; i++){
				double p = pre_info[hash_D.size()][i]/sum;
				if(p > 0)
					info -= p*((Math.log(p))/(Math.log((double)2)));
			}
			for(i = 0; i< pre_info.length-1; i++){
				double infod = 0;
				for(int j = 0; j< pre_info[0].length-1; j++){
					double p = pre_info[i][j]/pre_info[i][pre_info[0].length-1];
					if(p > 0)
						infod -= p*((Math.log(p))/(Math.log((double)2)));
				}
				infoa += infod*pre_info[i][pre_info[0].length-1]/sum;
			}
			gain = info - infoa;
		}
		else{
			double best_value = 0;
			double best_gain = 0;
			double temp_gain = 0;
			HashSet<Double> featuretemp = new HashSet<Double>();
			for(int i = 0; i< D.size(); i++)
				featuretemp.add(D.get(i).get(attr));
			Object[] feature = featuretemp.toArray();
			Arrays.sort(feature);
			/*for(int i = 0; i< feature.length; i++){
				System.out.print(feature[i]+" ");
			}
			System.out.println();*/
			for(int i = 0; i < feature.length-1; i++){
				double info = 0;
				double infoa = 0;
				double value = ((double)feature[i]+(double)feature[i+1])/2;
				HashSet<Double> hash_labels = new HashSet<Double>();
				HashMap<Double,Integer> column = new HashMap<Double,Integer>();
				int j = 0;
				for(j = 0; j< labels.size(); j++)
					hash_labels.add(labels.get(j));
				Iterator<Double> it = hash_labels.iterator();
				j = 0;
				while(it.hasNext()){
					column.put(it.next(), j);
					j ++;
				}
				double[][] pre_info = new double[3][hash_labels.size()+1];
				for(j = 0; j< pre_info.length; j++){
					for(int k = 0; k< pre_info[0].length; k++){
						pre_info[j][k] = 0;
					}
				}
				for(j = 0; j < labels.size(); j++){
					int row_index = 0;
					if(D.get(j).get(attr) <= value){
						row_index = 0;
					}
					else
						row_index = 1;
					int col_index = column.get(labels.get(j));
					pre_info[row_index][col_index]++;
					pre_info[row_index][hash_labels.size()]++;
					pre_info[2][col_index]++;
				}
				for(j = 0; j< pre_info.length-1; j++){
					pre_info[2][hash_labels.size()] += pre_info[j][hash_labels.size()];
				}
				/*for(j = 0; j < pre_info.length; j++){
					for(int k = 0; k < pre_info[0].length; k++)
						System.out.print(pre_info[j][k]);
					System.out.println();
				}*/
				double sum = pre_info[2][hash_labels.size()];
				for(j = 0; j< pre_info[0].length-1; j++){
					double p = pre_info[2][j]/sum;
					if(p > 0)
						info -= p*((Math.log(p))/(Math.log((double)2)));
				}
				for(j = 0; j< pre_info.length-1; j++){
					double infod = 0;
					for(int k = 0; k< pre_info[0].length-1; k++){
						double p = pre_info[j][k]/pre_info[j][pre_info[0].length-1];
						if(p > 0)
							infod -= p*((Math.log(p))/(Math.log((double)2)));
					}
					infoa += infod*pre_info[j][pre_info[0].length-1]/sum;
				}
				temp_gain = info - infoa;
				if(temp_gain > best_gain){
					best_gain = temp_gain;
					best_value = value;
				}
			}
			gain = best_gain;
			//System.out.println(gain);
			splitting_value = best_value;
		}
		/*for(i = 0; i< D.length; i++){
			for(int j = 0; j < D[0].length; j++){
				System.out.print(D[i][j]+" ");
			}
			System.out.println();
		}
		for(i = 0; i < labels.length; i++)
			System.out.print(labels[i]+" ");
		System.out.println();
		System.out.println(attr);
		for(i = 0; i< pre_info.length; i++){
			for(int j = 0; j < pre_info[0].length; j++)
				System.out.print(pre_info[i][j]+" ");
			System.out.println();
		}*/
		return gain;
	}
	
	public HashSet<Double> getValues(int attr){
		HashSet<Double> hash_D = new HashSet<Double>();
		for(int i = 0; i < D.size(); i++){
			hash_D.add(D.get(i).get(attr));
		}
		return hash_D;
	}
	public double getSplittingValue(){
		return this.splitting_value;
	}
}