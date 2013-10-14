package grasp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;
import java.util.TreeMap;

public class GRASP {
	/**全局变量*/
	ArrayList<Integer> Q_Sum;//前k次选择结果的总和
	ArrayList<Integer> Set;
	//ArrayList<Integer> SetPrecise;
	ArrayList<Integer> r;
	double[][] profit;
	int[] weight;
	int scale;
	int constraint;
	Random rand;
	double BestLBPrecise;
	
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
		//SetPrecise = new ArrayList<Integer>();
		r = new ArrayList<Integer>();
		/*for(int i = 0; i< scale; i++){
			SetPrecise.add(i);
			r.add(0);
		}*/
		this.weight = new int[scale];
		BestLBPrecise = 0;
		System.arraycopy(weight, 0, this.weight, 0, scale);
	}
	
	//对在R(S)中的每个项计算greedy function的值
	public double f_greedy(ArrayList<Integer> Set, int index, int currentweight, double objS){
		double numerator = objS, denominator = currentweight;
		for(int i = 0; i< Set.size(); i++){
			numerator = numerator + profit[Set.get(i)][index];
			//for(int j = 0; j < Set.size(); j++){
			//	if(Set.get(i)>= Set.get(j))
			//		numerator = numerator + profit[Set.get(i)][Set.get(j)];
			//}
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
	/*public ArrayList<Integer> CaculateRS( ArrayList<Integer> RS, int currentweight){
		ArrayList<Integer> R_S = new ArrayList<Integer>();
		for(int i = 0; i < RS.size(); i++){
			if(weight[RS.get(i)] + currentweight <= constraint ){
					R_S.add(RS.get(i));
			}
		}
		return R_S;
	}*/
	public ArrayList<Integer> CaculateRS(ArrayList<Integer> Set){
		ArrayList<Integer> R_S = new ArrayList<Integer>();
		int current_weight = 0;
		for(int i = 0; i < Set.size(); i++){
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
		int currentweight = 0;
		double objS = 0.0;
		Set = new ArrayList<Integer>(IndexSet);//已经选择的项的下标
		//通过Set计算没有被选入但是满足条件的项的下标的集合
		ArrayList<Integer> R_S = new ArrayList<Integer>(CaculateRS(IndexSet));
		while(R_S.size() > 0){
			TreeMap<Double,Integer> RCL = new TreeMap<Double,Integer>();
			TreeMap<Double,Double> q_kl = new TreeMap<Double,Double>();
			for(int i = 0; i< R_S.size(); i++){
				RCL.put(f_greedy(Set,R_S.get(i),currentweight,objS), R_S.get(i));
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
			currentweight = currentweight + weight[RCL.get(key)];
			for(int i = 0; i < Set.size(); i++){
				objS = objS + profit[Set.get(i)][RCL.get(key)];
			}
			objS = objS + profit[RCL.get(key)][RCL.get(key)];
			//更新R(S)
			R_S = CaculateRS(Set);
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
	
	
	public ArrayList<Integer> ProcessGRASP(int gamma,int beta,int lambda,int sigma){
		ArrayList<Integer> Index = new ArrayList<Integer>();
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
				//更新使profit最大的集合的下标
				Index.clear();
				for(int x = 0; x < Set.size(); x++){
					Index.add(Set.get(x));
				}
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
		return Index;
	}
}
