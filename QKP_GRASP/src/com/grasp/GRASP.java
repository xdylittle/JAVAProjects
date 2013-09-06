package com.grasp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

public class GRASP {
	/**全局变量*/
	ArrayList<Integer> Q_Sum;//前k次选择结果的总和
	ArrayList<Integer> Set;
	int[][] profit;
	int[] weight;
	int scale;
	int constraint;
	Random rand;
	
	//构造函数
	public GRASP(int scale, int constraint, int[][] profit, int[] weight){
		this.scale = scale;
		this.constraint = constraint;
		rand = new Random();
		Q_Sum = new ArrayList<Integer>();
		this.profit = new int[scale][scale];
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
		for(int i = 0; i < Set.size(); i++)
			System.out.print(Set.get(i)+" ");
		System.out.println();
		boolean Terminate = false;
		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(Set));
		while(!Terminate){
			int Addition = 0;
			int HighestAddition = 0;
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
				//System.out.println(Set.size());
				int index = Set.get(i);
				ArrayList<Integer> SetTest = new ArrayList<Integer>(Set);
				SetTest.remove(i);
				ArrayList<Integer> SetTemp = new ArrayList<Integer>(SetTest);
				ArrayList<Integer> RSTemp = new ArrayList<Integer>(CaculateRS(SetTemp));
				Addition = 0 - profit[index][index];
				for(int k = 0; k < SetTemp.size(); k++){
					Addition = Addition - profit[index][SetTemp.get(k)];
				}
				for(int k = 0; k < RSTemp.size(); k++){
					int Additiontemp = Addition;
					for(int j = 0; j < SetTemp.size(); j++){
						Addition = Addition + profit[SetTemp.get(j)][RSTemp.get(k)];
					}
					Addition = Addition + profit[RSTemp.get(k)][RSTemp.get(k)];
					if(Addition > HighestAddition){
						HighestAddition = Addition;
						ReplaceIndexS = index;
						ReplaceIndexRS = RSTemp.get(k);
					}
					else{
						Addition = Additiontemp;
					}
				}
			}
			if(HighestAddition > 0){
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
	
	public int CaculateLB(){
		int lb = 0;
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
	
	
	public int GRASP(int gamma, int beta, int lambda, int sigma){
		int MinLen = gamma, MaxLen = gamma+beta, BestLB = 0, LB = 0, count = 0, m = 0;
		ArrayList<Integer> TempSet = new ArrayList<Integer>();
		while(count != lambda){
			int k = 0;
			for(k = m*sigma+1; k <= (m+1)*sigma; k++){
				Construction(MinLen, MaxLen, TempSet, k);
				/*for(int i = 0; i < Set.size(); i++)
					System.out.print(Set.get(i)+" ");
				System.out.println();*/
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
				if(Q_Sum.get(j) == (m+1)*sigma){
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
		int[] weight = {1,1,2};
		int[][] profit = {{1,6,4},{6,0,2},{4,2,12}};
		GRASP test = new GRASP(3, 2, profit, weight);
		/*ArrayList<Integer>set = new ArrayList<Integer>();
		set.add(0);
		set.add(1);
		test.LocalSearch(set);*/
		int BestLB = test.GRASP(1, 1, 5, 1);
		System.out.println(BestLB);
	}
}
