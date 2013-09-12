package com.grasp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

public class GRASP {
	/**ȫ�ֱ���*/
	ArrayList<Integer> Q_Sum;//ǰk��ѡ�������ܺ�
	ArrayList<Integer> Set;
	ArrayList<Integer> SetPrecise;
	ArrayList<Integer> r;
	double[][] profit;
	int[] weight;
	int scale;
	int constraint;
	Random rand;
	double BestLBPrecise;
	
	//���캯��
	public GRASP(int scale, int constraint, double[][] profit, int[] weight){
		this.scale = scale;
		this.constraint = constraint;
		rand = new Random();
		Q_Sum = new ArrayList<Integer>();
		this.profit = new double[scale][scale];
		for(int i = 0; i < scale; i++){
			Q_Sum.add(0);
			for(int j = 0; j< scale; j++){
				this.profit[i][j] = profit[i][j];
			}
		}
		SetPrecise = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		for(int i = 0; i< scale; i++){
			SetPrecise.add(i);
			r.add(0);
		}
		this.weight = new int[scale];
		BestLBPrecise = 0;
		System.arraycopy(weight, 0, this.weight, 0, scale);
	}
	
	//����R(S)�е�ÿ�������greedy function��ֵ
	public double f_greedy(ArrayList<Integer> Set, int index){
		double numerator = 0, denominator = 0;
		for(int i = 0; i< Set.size(); i++){
			numerator = numerator + profit[Set.get(i)][index];
			denominator = denominator + weight[Set.get(i)];
			for(int j = 0; j < Set.size(); j++){
				if(Set.get(i)>= Set.get(j))
					numerator = numerator + profit[Set.get(i)][Set.get(j)];
			}
		}
		numerator = numerator + profit[index][index];
		denominator = denominator + weight[index];
		return numerator/denominator;
	}
	
	//ѡ��鿴�ĳ���
	public int min(int MinLen, int MaxLen, int left){
		int temp = rand.nextInt(MaxLen-MinLen+1) + MinLen;
		if(temp <= left)
			return temp;
		else
			return left;
	}
	
	public int CaculateQ_kl(TreeMap<Double,Integer> RCL, int k, ArrayList<Integer>Qk){
		Iterator<Double> iterator = RCL.keySet().iterator();
		int sum = 0;
		while(iterator.hasNext()){
			double key = iterator.next();
			sum = sum + (k -Qk.get(RCL.get(key)));
		}
		return sum;
	}
	
	//����S����R(S)
	public ArrayList<Integer> CaculateRS(ArrayList<Integer> Set){
		ArrayList<Integer> R_S = new ArrayList<Integer>();
		int current_weight = 0;
		for(int i = 0; i < Set.size(); i++){
			//System.out.println(Set.get(i));
			current_weight = current_weight + weight[Set.get(i)];
		}
		for(int i = 0; i < scale; i++){
			if(!Set.contains(i)){
				if(weight[i] + current_weight <= constraint ){
					R_S.add(i);
				}
			}
		}
		return R_S;
	}
	
	//����׶κ���
	public void Construction(int MinLen, int MaxLen, ArrayList<Integer> IndexSet, int k){
		int l = 1;
		Set = new ArrayList<Integer>(IndexSet);//�Ѿ�ѡ�������±�
		//ͨ��Set����û�б�ѡ�뵫����������������±�ļ���
		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(IndexSet));
		while(R_S.size() > 0){
			TreeMap<Double,Integer> RCL = new TreeMap<Double,Integer>();
			TreeMap<Double,Double> q_kl = new TreeMap<Double,Double>();
			for(int i = 0; i< R_S.size(); i++){
				RCL.put(f_greedy(Set,R_S.get(i)), R_S.get(i));
			}
			int len = min(MinLen, MaxLen,R_S.size());
			int RCL_len = RCL.size();
			for(int i = 0; i< RCL_len-len; i++){
				RCL.remove(RCL.firstKey());
			}
			int Q_kl = CaculateQ_kl(RCL,k,Q_Sum);
			Iterator<Double> iterator = RCL.keySet().iterator();
			while(iterator.hasNext()){
				double key = iterator.next();
				double value = (k-Q_Sum.get(RCL.get(key)))/(double)Q_kl;
				q_kl.put(key, value);
			}
			int m = rand.nextInt(Q_kl) + 1;
			double temp_sum = 0,compare = (double)m/Q_kl;
			iterator = RCL.keySet().iterator();
			double key = 0;
			while(iterator.hasNext() && temp_sum < compare){
				key = iterator.next();
				temp_sum = temp_sum + q_kl.get(key);
			}
			Set.add(RCL.get(key)); //����S
			//����R(S)
			R_S.clear();
			R_S = new ArrayList<Integer>(CaculateRS(Set));
			l = l + 1;
		}
	}
	
	
	public void LocalSearch(){
		//for(int i = 0; i < Set.size(); i++)
		//	System.out.print(Set.get(i)+" ");
		//System.out.println();
		boolean Terminate = false;
		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(Set));
		while(!Terminate){
			double Addition = 0;
			double HighestAddition = 0;
			int AddIndex = 0;
			int ReplaceIndexS = 0;
			int ReplaceIndexRS = 0;
			//�ж����һ��Ԫ��
			for(int i = 0; i < R_S.size(); i++){
				for(int j = 0; j < Set.size(); j++){
					Addition = Addition + profit[Set.get(j)][R_S.get(i)];
				}
				Addition = Addition + profit[R_S.get(i)][R_S.get(i)];
				if(Addition > HighestAddition){
					HighestAddition = Addition;
					AddIndex = R_S.get(i);
				}
				else{
					Addition = 0;
				}
			}
			//�ж��滻һ��Ԫ��
			for(int i = 0; i < Set.size(); i++){
				int index = Set.get(i);
				ArrayList<Integer> SetTest = new ArrayList<Integer>(Set);
				SetTest.remove(i);
				ArrayList<Integer> SetTemp = new ArrayList<Integer>(SetTest);
				ArrayList<Integer> RSTemp = new ArrayList<Integer>(CaculateRS(SetTemp));
				Addition = 0 - profit[index][index];
				for(int k = 0; k < SetTemp.size(); k++){
					Addition = Addition - profit[index][SetTemp.get(k)];
				}
				double Additiontemp = Addition;
				for(int k = 0; k < RSTemp.size(); k++){
					Addition = Additiontemp;
					for(int j = 0; j < SetTemp.size(); j++){
						Addition = Addition + profit[SetTemp.get(j)][RSTemp.get(k)];
					}
					Addition = Addition + profit[RSTemp.get(k)][RSTemp.get(k)];
					if(Addition - HighestAddition > 0.00001){ 
						HighestAddition = Addition;
						ReplaceIndexS = index;
						ReplaceIndexRS = RSTemp.get(k);
					}
				}
				System.out.println(HighestAddition);
			}
			if(HighestAddition > 0.00001){
				if(ReplaceIndexS == 0 && ReplaceIndexRS == 0){
					Set.add(AddIndex);
				}
				else{
					Set.remove((Integer)ReplaceIndexS);
					Set.add(ReplaceIndexRS);
				}
			}
			else{
				Terminate = true;
			}
		}
	}
	
	public double CaculateLB(){
		double lb = 0;
		for(int i = 0; i< Set.size(); i++){
			for(int j = 0; j < Set.size(); j++){
				if(Set.get(i) >= Set.get(j)){
					lb += profit[Set.get(i)][Set.get(j)];
				}
			}
		}
		//System.out.println(lb);
		return lb;
	}
	
	public void UpdateQSum(){
		for(int i = 0; i< Set.size(); i++){
			int current = Q_Sum.get(Set.get(i));
			Q_Sum.set(Set.get(i), current+1);
		}
	}
	
	
	public double ProcessGRASP(int gamma,int beta,int lambda,int sigma){
		int MinLen = gamma, MaxLen = gamma+beta, count = 0, m = 0;
		double BestLB = 0, LB = 0;
		ArrayList<Integer> TempSet = new ArrayList<Integer>();
		ArrayList<Integer> TempQ_Sum = new ArrayList<Integer>();
		while(count != lambda){
			int k = 0;
			for(k = m*sigma+1; k <= (m+1)*sigma; k++){
				if(k == m*sigma+1){
					TempQ_Sum.clear();
					for(int i = 0; i < Q_Sum.size(); i++)
						TempQ_Sum.add(Q_Sum.get(i));
				}
				Construction(MinLen, MaxLen, TempSet, k);
				LocalSearch();
				BestLB = CaculateLB();
				UpdateQSum();
			}
			if(BestLB > LB){
				LB = BestLB;
				count = 0;
			}
			else{
				count = count + 1;
				MinLen = MaxLen + 1;
				MaxLen = MaxLen + beta;
			}
			TempSet.clear();
			for(int j = 0; j < scale; j++){
				if(TempQ_Sum.get(j) == m*sigma && m > 0){
					TempSet.add(j);
				}
			}
			m = m + 1;
		}
		/*System.out.println("---------------------------------------------------------------");
		for(int i = 0; i < Set.size(); i++)
			System.out.print(Set.get(i)+" ");
		System.out.println();
		System.out.println(BestLB);*/
		return BestLB;
	}
	
	public void combination(int n, int m, int k, int index){ 
		if(index == m){ //�����Ͻ�� 
			/*for(int i = 0; i < m; ++i) 
				System.out.print(r.get(i)); 
			System.out.println();*/
			double total = 0;
			for(int i = 0; i < m; ++i){
				for (int j = 0; j < m; ++j){
					if(r.get(i) >= r.get(j)){
						total += profit[r.get(i)][r.get(j)];
					}
				}
			}
			if (total > BestLBPrecise)
				BestLBPrecise = total;
			return; 
		} 
		for(int i = k; i < n; ++i){ 
			r.set(index, SetPrecise.get(i)); 
			combination(n, m, i + 1, index + 1);//ע�������������i + 1 
		} 
	} 
	
	
	//������
	public static void main(String []args){
		int scale = 100;
		int constrations = 0;
		int[] weight = new int[scale];
		double[][] profit = new double[scale][scale];
		File file = new File("C:\\Users\\xdy\\Desktop\\instance\\jeu_100_25_1.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // һ�ζ���һ�У�ֱ������nullΪ�ļ�����
            while ((tempString = reader.readLine()) != null) {
                // ��ʾ�к�
            	//System.out.println(line);
                if(line == 3 ){
                	tempString = tempString.replaceAll("   ", "	");
                	tempString = tempString.replaceAll("  ", "	");
                	tempString = tempString.replaceAll(" ", "	");
                	String[] str = tempString.split("	");
                	for(int i = 0; i < str.length; i++){
                		profit[i][i] = Double.parseDouble(str[i]);
                	}
                }
                else if(line >=4 && line <= 100){
                	tempString = tempString.replaceAll("   ", "	");
                	tempString = tempString.replaceAll("  ", "	");
                	tempString = tempString.replaceAll(" ", "	");
                	String[] str = tempString.split("	");
                	for(int i = 0; i < str.length; i++){
                		profit[line-4][i+line-3] = Double.parseDouble(str[i]);
                	}
                }
                else if (line == 105){
                	constrations = Integer.parseInt(tempString);
                }
                else if(line == 106){
                	tempString = tempString.replaceAll("   ", "	");
                	tempString = tempString.replaceAll("  ", "	");
                	tempString = tempString.replaceAll(" ", "	");
                	String[] str = tempString.split("	");
                	for(int i = 0; i < str.length; i++){
                		weight[i] = Integer.parseInt(str[i]);
                	}
                }
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        for (int i = 0; i < scale; i++){
        	for (int j = 0; j < scale; j++){
        		if(j <= i)
        			profit[i][j] = profit[j][i];
        	}
        }
		for(int i = 0 ; i < scale; i++){
			for(int j = 0; j < scale; j++){
				if(j >= i){
					if(profit[i][j] != profit[j][i]){
						System.out.println("dsfdf");
					}
				}
			}
		}
		GRASP test = new GRASP(scale, constrations, profit, weight);
		//test.combination(scale, constrations, 0, 0);
		//System.out.println(test.BestLBPrecise);
		long startTime=System.currentTimeMillis();   //��ȡ��ʼʱ��  
		double BestLB = test.ProcessGRASP(1, 3, 1, 5);
		long endTime=System.currentTimeMillis(); //��ȡ����ʱ��  
		System.out.println("��������ʱ�䣺 "+(endTime-startTime)+"ms"); 
		System.out.println(BestLB);
		//System.out.println((test.BestLBPrecise-BestLB)/test.BestLBPrecise);
	}
}
