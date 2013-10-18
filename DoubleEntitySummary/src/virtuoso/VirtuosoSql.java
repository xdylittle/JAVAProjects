package virtuoso;

import java.util.ArrayList;
import java.util.HashSet;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;

import virtuoso.jena.driver.VirtGraph;
import virtuoso.jena.driver.VirtuosoQueryExecution;
import virtuoso.jena.driver.VirtuosoQueryExecutionFactory;
import main.FeatureNode;

public class VirtuosoSql {
	/*public static ArrayList<FeatureNode> findFeature(String entity,int e_Num){
		ArrayList<FeatureNode> temp = new ArrayList<FeatureNode>();
		File file = new File("D:\\projects\\double entity summarization\\testdata.txt");
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // һ�ζ���һ�У�ֱ������nullΪ�ļ�����
            while ((tempString = reader.readLine()) != null) {
                // ��ʾ�к�
                //System.out.println("line " + line + ": " + tempString);
            	String[] stringlist = tempString.split("	");
            	if(entity.equals(stringlist[0])){
            		FeatureNode node = new FeatureNode(stringlist[1], stringlist[2], e_Num, false, temp.size());
            		temp.add(node);
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
		return temp;
	}*/
	public static ArrayList<FeatureNode> findFeature(String entity,String graphiri, int e_Num, VirtGraph vgraph){
		ArrayList<FeatureNode> temp = new ArrayList<FeatureNode>();
		Query query = null;
		VirtuosoQueryExecution vqe = null;
		String q = "SELECT ?p ?o FROM <"+ graphiri +"> WHERE {<"+entity+"> ?p ?o.}";
		try{
			long startTime=System.currentTimeMillis();
			query = QueryFactory.create(q);
			vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
			ResultSet rs = vqe.execSelect();
			QuerySolution qsol = null;
			while(rs.hasNext()){
				qsol = rs.nextSolution();
				String property = qsol.get("p").toString();
				String value = qsol.get("o").toString();
				FeatureNode node = new FeatureNode(property,value,e_Num,false,temp.size());
				temp.add(node);
			}
			long endTime=System.currentTimeMillis();
			System.out.println(endTime-startTime);
		}finally{
			if(vqe != null)
				vqe.close();
		}
		return temp;
	}
	
	public static double findFeatureNumber(String property,String graphiri,VirtGraph vgraph){
		double temp = 0.0;
		Query query = null;
		VirtuosoQueryExecution vqe = null;
		String q = "SELECT ?o FROM <"+ graphiri +"> WHERE {?s <"+ property +"> ?o}";
		try{
			query = QueryFactory.create(q);
			vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
			ResultSet rs = vqe.execSelect();
			QuerySolution qsol = null;
			while(rs.hasNext()){
				rs.nextSolution();
				temp ++;
			}
		}finally{
			if(vqe != null)
				vqe.close();
		}
		return temp;
	}
	
	public static double findoNumber(String property,String graphiri,VirtGraph vgraph){
		long startTime=System.currentTimeMillis();
		HashSet<String> o = new HashSet<String>();
		Query query = null;
		VirtuosoQueryExecution vqe = null;
		String q = "SELECT ?o FROM <"+ graphiri +"> WHERE {?s <"+ property +"> ?o}";
		try{
			query = QueryFactory.create(q);
			vqe = VirtuosoQueryExecutionFactory.create(query, vgraph);
			ResultSet rs = vqe.execSelect();
			QuerySolution qsol = null;
			while(rs.hasNext()){
				qsol = rs.nextSolution();
				o.add(qsol.get("o").toString());
			}
		}finally{
			if(vqe != null)
				vqe.close();
		}
		long endTime=System.currentTimeMillis();
		System.out.println(endTime-startTime);
		return o.size();
	}
}
