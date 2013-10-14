package isub;
import weight.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Test{
	public static void main(String []args){
		double x = ISub.getSimilarity("apple", "appletree");
		System.out.println(x);
		
	}
	/*int start = 0;
	int end = 0;
	public Test(int start, int end){
		this.start = start;
		this.end = end;
	}
	public void choose_pair(){
		ArrayList<String> subject = new ArrayList<String>();
		Connection conn = DBConnPool.getSummarization();
		Statement st = null;
		ResultSet rs = null;
		try {
			st = (Statement)conn.createStatement();
			rs = st.executeQuery("SELECT s FROM `entity_summarization_2012nov`.`abstract_long` WHERE id >"+ start +" AND id<" + end);
			while(rs.next()){
				subject.add(rs.getString(1));
			}
			rs.close();
			st.close();
			conn.close();
		}catch(SQLException e){
			System.out.println("查询失败");
			e.printStackTrace();
		}
		for(int i = 0; i< subject.size(); i++){
			for(int j = i+1; j< subject.size(); j++){
				double x = ISub.getSimilarity(subject.get(i).substring(3).toLowerCase(),subject.get(j).substring(3).toLowerCase());
				if(x > 0.5 && !subject.get(i).contains("_") && !subject.get(j).contains("_")){
					System.out.println(subject.get(i)+","+subject.get(j));
				}
			}
		}
	}
	public static void main(String []args){
		DBParam.init();
		int count = 3550567;
		int quotient = count / 20;
		BlockingQueue<Runnable> queue = new LinkedBlockingQueue<Runnable>();  
		ThreadPoolExecutor exec = new ThreadPoolExecutor(4, 5, 7, TimeUnit.DAYS, queue);
		for (int i = 0; i<= quotient; i++) {
			Test a;
			if(i == quotient)
				a = new Test(i*20, count+1);
			else 
				a = new Test(i*20, (i+1)*20+1);
			exec.execute(a);
		}
		exec.shutdown();
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		choose_pair();
	}*/
}
