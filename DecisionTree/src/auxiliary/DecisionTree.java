package auxiliary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.HashMap;  
import java.util.concurrent.ExecutionException;  
import java.math.BigDecimal;  

import static java.lang.Math.*;  

/**
 *
 * @author daq
 */
public class DecisionTree extends Classifier {

    boolean isClassification;
    TreeNode root;

    public DecisionTree() {
    	isClassification = true;
    }

	/*
	* isCategory[k] indicates whether the kth attribut is discrete or continuous, the last attribute is the label
	* features[i] is the feature vector of the ith sample
	* labels[i] is the label of he ith sample
	*/
	@Override
    public void train(boolean[] isCategory, double[][] features, double[] labels) {
		handleMissData(features);
        isClassification = isCategory[isCategory.length - 1];
        if (isClassification) { // classification
        	ArrayList<Integer> CandAttr = new ArrayList<Integer>();
        	for(int i = 0; i< features[0].length; i++)
        		CandAttr.add(i);
        	root = buildTree(features,CandAttr,labels,0,isCategory);
        } else { // regression
        	ArrayList<Integer> CandAttr = new ArrayList<Integer>();
        	for(int i = 0; i< features[0].length; i++)
        		CandAttr.add(i);
        	root = buildTreeR(features,CandAttr,labels,0,isCategory);
        }
    }

	/*
	* features is the feature vector of the test sample
	* you need to return the label of test sample
	*/
    @Override
    public double predict(double[] features) {
        if (isClassification) { // classification
            TreeNode node = root;
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
            return node.getName();
        } else { // regression
        	TreeNode node = root;
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
            return node.getName();
        }
    }
    
    public TreeNode buildTree(double[][] D, ArrayList<Integer> attribute_list, double[] labels, double attr_value, boolean[] isCategory){
    	TreeNode node = new TreeNode();
    	/*for(int i = 0; i < labels.length; i++){
    		System.out.print(labels[i]);
    	}
    	System.out.println();*/
    	node.setValue(attr_value);
    	if(Issameclass(labels)){
    		node.setName(labels[0]);
    		return node;
    	}
    	if(attribute_list.size() == 0){
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
    			int count = 0;
    			double value = it.next();
    			for(int i = 0; i < D.length; i++){
    				if(D[i][splitting_attribute] == value)
    					count++;
    			}
    			double[][] Dj = new double[count][D[0].length];
    			double[] labelsj = new double[count];
    			int index = 0;
    			for(int i = 0; i < D.length; i++){
    				if(D[i][splitting_attribute] == value){
    					for(int j = 0; j < D[0].length; j++){
    						Dj[index][j] = D[i][j];
    					}
    					labelsj[index] = labels[i];
    					index++;
    				}
    			}
    			node.child.add(buildTree(Dj,attributej,labelsj,value,isCategory));
    		}
    	}
    	else{
    		node.setType(1);
    		double bestValue = gain.getSplittingValue();
    		int count = 0;
    		for(int i = 0; i < D.length; i++){
    			if(D[i][splitting_attribute]<=bestValue)
    				count++;
    		}
    		double[][] Dj1 = new double[count][D[0].length];
			double[] labelsj1 = new double[count];
			double[][] Dj2 = new double[D.length-count][D[0].length];
			double[] labelsj2 = new double[D.length-count];
			int index1 = 0;
			int index2 = 0;
			for(int i = 0; i < D.length; i++){
				if(D[i][splitting_attribute] <= bestValue){
					for(int j = 0; j < D[0].length; j++){
						Dj1[index1][j] = D[i][j];
					}
					labelsj1[index1] = labels[i];
					index1++;
				}
				else{
					for(int j = 0; j < D[0].length; j++){
						Dj2[index2][j] = D[i][j];
					}
					labelsj2[index2] = labels[i];
					index2++;
				}
			}
			node.child.add(buildTree(Dj1,attributej,labelsj1,bestValue,isCategory));
			node.child.add(buildTree(Dj2,attributej,labelsj2,bestValue,isCategory));
    	}
    	return node;
    }
    public TreeNode buildTreeR(double[][] D, ArrayList<Integer> attribute_list, double[] labels, double attr_value, boolean[] isCategory){
    	TreeNode node = new TreeNode();
    	/*for(int i = 0; i < labels.length; i++){
    		System.out.print(labels[i]);
    	}
    	System.out.println();*/
    	node.setValue(attr_value);
    	if(Issameclass(labels)){
    		node.setName(labels[0]);
    		return node;
    	}
    	if(attribute_list.size() == 0){
    		node.setName(avg(labels));
    		return node;
    	}
    	Gain gain = new Gain(D,labels,attribute_list,isCategory);
    	int splitting_attribute = gain.bestAttrR();
    	if(splitting_attribute == -1){
    		node.setName(avg(labels));
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
    			int count = 0;
    			double value = it.next();
    			for(int i = 0; i < D.length; i++){
    				if(D[i][splitting_attribute] == value)
    					count++;
    			}
    			double[][] Dj = new double[count][D[0].length];
    			double[] labelsj = new double[count];
    			int index = 0;
    			for(int i = 0; i < D.length; i++){
    				if(D[i][splitting_attribute] == value){
    					for(int j = 0; j < D[0].length; j++){
    						Dj[index][j] = D[i][j];
    					}
    					labelsj[index] = labels[i];
    					index++;
    				}
    			}
    			node.child.add(buildTreeR(Dj,attributej,labelsj,value,isCategory));
    		}
    	}
    	else{
    		node.setType(1);
    		double bestValue = gain.getSplittingValue();
    		int count = 0;
    		for(int i = 0; i < D.length; i++){
    			if(D[i][splitting_attribute]<=bestValue)
    				count++;
    		}
    		double[][] Dj1 = new double[count][D[0].length];
			double[] labelsj1 = new double[count];
			double[][] Dj2 = new double[D.length-count][D[0].length];
			double[] labelsj2 = new double[D.length-count];
			int index1 = 0;
			int index2 = 0;
			for(int i = 0; i < D.length; i++){
				if(D[i][splitting_attribute] <= bestValue){
					for(int j = 0; j < D[0].length; j++){
						Dj1[index1][j] = D[i][j];
					}
					labelsj1[index1] = labels[i];
					index1++;
				}
				else{
					for(int j = 0; j < D[0].length; j++){
						Dj2[index2][j] = D[i][j];
					}
					labelsj2[index2] = labels[i];
					index2++;
				}
			}
			node.child.add(buildTreeR(Dj1,attributej,labelsj1,bestValue,isCategory));
			node.child.add(buildTreeR(Dj2,attributej,labelsj2,bestValue,isCategory));
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
    public boolean Issameclass(double[] labels){
    	if(labels.length == 1){
    		return true;
    	}
    	for(int i = 1; i< labels.length; i++){
    		if(labels[i] != labels[0])
    			return false;
    	}
    	return true;
    }
    public double mode(double[] labels){
    	Arrays.sort(labels);
        int count = 1;
        int longest = 0;
        double most = 0;
        for (int i = 0; i < labels.length - 1; i++) {
            if (labels[i] == labels[i + 1]) {
                count++;
            } else {
                count = 1;// 如果不等于，就换到了下一个数，那么计算下一个数的次数时，count的值应该重新符值为一
                continue;
            }
            if (count > longest) {
                most = labels[i];
                longest = count;
            }
        }
        return most;
    }
    public void handleMissData(double[][] features){
       	if(features == null){
       		return;
       	}
       	
       	/*calculate each feature's average value and assign it to the miss one*/
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
    public static void main(String[] args){
    	DataSet ds = new DataSet("./data/housing.data");
    	//DecisionTree dt = new DecisionTree();
    	Evaluation eva = new Evaluation(ds, "DecisionTree");
        eva.crossValidation();
        //System.out.println("mean and standard deviation of accuracy:" + eva.getAccMean() + "," + eva.getAccStd());
        System.out.println("mean and standard deviation of RMSE:" + eva.getRmseMean() + "," + eva.getRmseStd());
    	/*dt.train(ds.getIsCategory(), ds.getFeatures(), ds.getLabels());
    	for(int i = 0; i< ds.getFeatures().length; i++){
    		double label = dt.predict(ds.getFeatures()[i]);
    		System.out.println(label);
    	}*/
    }
    
}

class TreeNode{
	private double name; //节点名（分裂属性的名称）   
    private double value;
    ArrayList<TreeNode> child; //子结点集合  
    private int nodetype;
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
}

class Gain{
	double[][] D;
	double[] labels;
	ArrayList<Integer> attr_list;
	boolean[] isCategory;
	double splitting_value;
	public Gain(double[][] D, double[] labels, ArrayList<Integer> attr_list, boolean[] isCategory){
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
	public int bestAttrR(){
		int index = 0;
		double best = 0;
		double best_value = 0.0;
		double current = 0.0;
		for(int i = 0; i < attr_list.size(); i++){
			current = gainInfoR(D, labels, attr_list.get(i), isCategory);
			if(i == 0){
				index = attr_list.get(0);
				best = current;
				best_value = this.splitting_value;
			}
			else{
				if(current < best){
					index = attr_list.get(i);
					best = current;
					best_value = this.splitting_value;
				}
			}
		}
		boolean re = true;
		for(int i = 1; i < this.D.length; i++){
			if(this.D[i][index] != this.D[0][index])
				re = false;
		}
		this.splitting_value = best_value;
		//if(best == 0)
		//	return -1;
		if(re == true)
			return -1;
		return index;
	}
	public double gainInfo(double[][] D, double[] labels, int attr, boolean[] isCategory){
		double gain = 0;
		if(isCategory[attr] == true){
			int i = 0;
			double info = 0;
			double infoa = 0;
			HashSet<Double> hash_labels = new HashSet<Double>();
			HashMap<Double,Integer> column = new HashMap<Double,Integer>();
			HashSet<Double> hash_D = new HashSet<Double>();
			HashMap<Double,Integer> row = new HashMap<Double,Integer>();
			for(i = 0; i< labels.length; i++)
				hash_labels.add(labels[i]);
			Iterator<Double> it = hash_labels.iterator();
			i = 0;
			while(it.hasNext()){
				column.put(it.next(), i);
				i ++;
			}
			for(i = 0; i< D.length; i++){
				hash_D.add(D[i][attr]);
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
			for(i = 0; i < labels.length; i++){
				int row_index = row.get(D[i][attr]);
				int col_index = column.get(labels[i]);
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
			for(int i = 0; i< D.length; i++)
				featuretemp.add(D[i][attr]);
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
				for(j = 0; j< labels.length; j++)
					hash_labels.add(labels[j]);
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
				for(j = 0; j < labels.length; j++){
					int row_index = 0;
					if(D[j][attr] <= value){
						row_index = 0;
					}
					else
						row_index = 1;
					int col_index = column.get(labels[j]);
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
	public double gainInfoR(double[][] D, double[] labels, int attr, boolean[] isCategory){
		double gain = 0;
		if(isCategory[attr] == true){
			int i = 0;
			HashSet<Double> hash_feature = new HashSet<Double>();
			for(i = 0; i< D.length; i++){
				hash_feature.add(D[i][attr]);
			}
			Iterator<Double> it = hash_feature.iterator();
			while(it.hasNext()){
				double current_gain = 0;
				ArrayList<Double> label = new ArrayList<Double>();
				double sum = 0;
				double average = 0;
				double feature = it.next();
				for(i = 0; i < labels.length ;i++){
					if(feature == D[i][attr]){
						label.add(labels[i]);
						sum += labels[i];
					}
				} 
				average = sum/label.size();
				for(i = 0; i< label.size(); i++){
					current_gain = current_gain + (label.get(i)-average)*(label.get(i)-average);
				}
				gain = gain + current_gain/label.size();
			}
			//System.out.println(gain);
		}
		else{
			/*for(int i = 0; i< D.length; i++){
				for(int j = 0; j < D[0].length; j++){
					System.out.print(D[i][j]+" ");
				}
				System.out.println();
			}
			for(int i = 0; i < labels.length; i++)
				System.out.print(labels[i]+" ");
			System.out.println();
			System.out.println(attr);*/
			double best_value = 0;
			double best_gain = 0;
			double temp_gain = 0;
			HashSet<Double> featuretemp = new HashSet<Double>();
			for(int i = 0; i< D.length; i++)
				featuretemp.add(D[i][attr]);
			Object[] feature = featuretemp.toArray();
			Arrays.sort(feature);
			if(feature.length == 1){
				double sum1 = 0;
				double average1 = 0;
				for(int i = 0; i< labels.length; i++){
					sum1 = sum1 + labels[i];
				}
				average1 = sum1/labels.length;
				for(int i = 0; i< labels.length; i++){
					temp_gain = temp_gain+(labels[i]-average1)*(labels[i]-average1);
				}
				temp_gain = temp_gain/labels.length;
				gain = temp_gain;
				//System.out.println(gain);
				splitting_value = (double)feature[0];
			}
			else{
				for(int i = 0; i < feature.length-1; i++){
					double current_gain1 = 0;
					double current_gain2 = 0;
					double value = ((double)feature[i]+(double)feature[i+1])/2;
					ArrayList<Double> label1 = new ArrayList<Double>();
					ArrayList<Double> label2 = new ArrayList<Double>();
					double sum1 = 0;
					double average1 = 0;
					double sum2 = 0;
					double average2 = 0;
					for(int j = 0; j < D.length ;j++){
						if(D[j][attr] <= value){
							sum1 += labels[j];
							label1.add(labels[j]);
						}
						else{
							sum2 += labels[j];
							label2.add(labels[j]);
						}
					} 
					average1 = sum1/label1.size();
					average2 = sum2/label2.size();
					for(int j = 0; j< label1.size(); j++){
						current_gain1 = current_gain1 + (label1.get(j)-average1)*(label1.get(j)-average1);
					}
					for(int j = 0; j< label2.size(); j++){
						current_gain2 = current_gain2 + (label2.get(j)-average2)*(label2.get(j)-average2);
					}
					temp_gain = current_gain1/label1.size()+current_gain2/label1.size();
					if(i == 0){
						best_gain = temp_gain;
						best_value = value;
					}
					else{
						if(temp_gain < best_gain){
							best_gain = temp_gain;
							best_value = value;
						}
					}
				}
				gain = best_gain;
				//System.out.println(gain);
				splitting_value = best_value;
			}
		}
		return gain;
	}
	public HashSet<Double> getValues(int attr){
		HashSet<Double> hash_D = new HashSet<Double>();
		for(int i = 0; i < D.length; i++){
			hash_D.add(D[i][attr]);
		}
		return hash_D;
	}
	public double getSplittingValue(){
		return this.splitting_value;
	}
}