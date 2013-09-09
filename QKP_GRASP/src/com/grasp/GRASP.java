package com.grasp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

public class GRASP {
	/**全局变量*/
	ArrayList<Integer> Q_Sum;//前k次选择结果的总和
	ArrayList<Integer> Set;
	double[][] profit;
	int[] weight;
	int scale;
	int constraint;
	Random rand;
	
	//构造函数
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
		this.weight = new int[scale];
		System.arraycopy(weight, 0, this.weight, 0, scale);
	}
	
	//对在R(S)中的每个项计算greedy function的值
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
	
	//选择查看的长度
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
	
	//根据S计算R(S)
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
	
	//构造阶段函数
	public void Construction(int MinLen, int MaxLen, ArrayList<Integer> IndexSet, int k){
		int l = 1;
		Set = new ArrayList<Integer>(IndexSet);//已经选择的项的下标
		//通过Set计算没有被选入但是满足条件的项的下标的集合
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
			Set.add(RCL.get(key)); //更新S
			//更新R(S)
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
			//判断添加一个元素
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
			//判断替换一个元素
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
		System.out.println("---------------------------------------------------------------");
		for(int i = 0; i < Set.size(); i++)
			System.out.print(Set.get(i)+" ");
		System.out.println();
		System.out.println(BestLB);
		return BestLB;
	}
	//主函数
	public static void main(String []args){
		int scale = 400;
		int[] weight = new int[scale];
		double[][] profit = new double[scale][scale];
		int constrations = 30;
		for(int i = 0; i< scale; i++){
			weight[i] = 1;
		}
		for(int i = 0; i < scale; i++){
			for(int j = 0; j < scale; j++){
				if(j < i){
					profit[i][j] = profit[j][i];
				}
				else{
					profit[i][j] = Math.random();
				}
			}
		}
		/*for(int i = 0; i < scale; i++){
			for(int j = 0; j < scale; j++){
				System.out.print(profit[i][j]+"   ");
			}
			System.out.println();
		}*/
		long startTime=System.currentTimeMillis();   //获取开始时间  
		GRASP test = new GRASP(scale, constrations, profit, weight);
		/*ArrayList<Integer>set = new ArrayList<Integer>();
		set.add(0);
		set.add(1);
		test.LocalSearch(set);*/
		double BestLB = test.ProcessGRASP(1, 3, 3, 1);
		long endTime=System.currentTimeMillis(); //获取结束时间  
		System.out.println("程序运行时间： "+(endTime-startTime)+"ms"); 
		System.out.println(BestLB);
	}
}
