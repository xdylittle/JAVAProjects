package weight;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.ArrayList;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopFilter;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import virtuoso.VirtuosoSql;
import weight.Tokenizer;
import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import isub.ISub;

public class ProperyPair {
	ArrayList<String> synp1;
	ArrayList<String> synp2;
	
	public ProperyPair(){
		synp1 = new ArrayList<String>();
		synp2 = new ArrayList<String>();
	}
	
	//默认传入的是localname
	public double ComparableDegree(String property1, String property2,int casenum){
		
		//直接计算两个属性的相似度
		double similarity0 = -1, similarity1 = -1, similarity2 = -1, similarity3 = -1,similarity = -1;
		if(casenum == 0 || casenum == 4){
			similarity0 = ISub.getSimilarity(property1, property2);
			similarity = (similarity0+1)/2;
		}
		//经过wordnet变换之后计算两个属性的相似度
		else if(casenum == 1 || casenum == 4){
			String[] p1 = testStandard(hump(property1)).split(" ");
			String[] p2 = testStandard(hump(property2)).split(" ");
			synp1.clear();
			synp2.clear();
			getCombination(1,"",p1,1);//计算经过wordnet变换后的每个property的各种不同组合
			getCombination(1,"",p2,2);
			for(int i = 0; i< synp1.size(); i++){
				for(int j = 0; j< synp2.size(); j++){
					double sim = ISub.getSimilarity(synp1.get(i), synp2.get(j));
					if(sim > similarity1)
						similarity1 = sim;
				}
			}
			similarity = (similarity1+1)/2;
		}
		//经过stem变换之后计算两个属性的相似度
		else if(casenum == 2 || casenum == 4){
			String[] p1 = testStandard(hump(property1)).split(" ");
			String pp1 = "";
			String[] p2 = testStandard(hump(property2)).split(" ");
			String pp2 = "";
			for(int i = 0; i< p1.length; i++){
				pp1 = pp1 + Tokenizer.tokenize(p1[i]);
			}
			for(int i = 0; i< p2.length; i++){
				pp2 = pp2 + Tokenizer.tokenize(p2[i]);
			}
			similarity2 = ISub.getSimilarity(pp1, pp2);
			similarity = (similarity2+1)/2;
		}
		else if(casenum == 3 || casenum == 4){
			String[] p1 = testStandard(hump(property1)).split(" ");
			String[] p2 = testStandard(hump(property2)).split(" ");
			for(int i = 0; i< p1.length; i++){
				p1[i] = Tokenizer.tokenize(p1[i]);
			}
			for(int i = 0; i< p2.length; i++){
				p2[i] = Tokenizer.tokenize(p2[i]);
			}
			synp1.clear();
			synp2.clear();
			getCombination(1,"",p1,1);//计算经过wordnet变换后的每个property的各种不同组合
			getCombination(1,"",p2,2);
			for(int i = 0; i< synp1.size(); i++){
				for(int j = 0; j< synp2.size(); j++){
					double sim = ISub.getSimilarity(synp1.get(i), synp2.get(j));
					if(sim > similarity3)
						similarity3 = sim;
				}
				//System.out.println(synp1.get(i)+" "+);
			}
			similarity = (similarity3+1)/2;
		}
		else if(casenum == 4){
			similarity = max(similarity0,similarity1,similarity2,similarity3);
			similarity = (similarity+1)/2;
		}
		return similarity;
	}
	
	public double DistinctionDegree(String property1, String graphiri1, String property2, String graphiri2){
		double featurenum1 = VirtuosoSql.findFeatureNumber(property1, graphiri1);
		double featurenum2 = VirtuosoSql.findFeatureNumber(property2, graphiri2);
		double o1 = VirtuosoSql.findoNumber(property1, graphiri1);
		double o2 = VirtuosoSql.findoNumber(property2, graphiri2);
		double aac1 = featurenum1/o1;
		double aac2 = featurenum2/o2;
		double temp = 2*aac1*aac2/(aac1+aac2);
		return temp;
	}
	
	public double max(double x1, double x2, double x3, double x4){
		double p1, p2;
		if(x1 > x2)
			p1 = x1;
		else
			p1 = x2;
		if(x3 > x4)
			p2 = x3;
		else
			p2 = x4;
		if(p1 > p2)
			return p1;
		else
			return p2;
	}
	public void getCombination(int length,String str, String[] p1,int index){
		if(length == p1.length){
			ArrayList<String> synptemp = new ArrayList<String>();
			try {
				synptemp = getSynonyms(p1[length-1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i< synptemp.size(); i++){
				if(index == 1)
					synp1.add(str+synptemp.get(i));
				else
					synp2.add(str+synptemp.get(i));
			}
		}
		else{
			ArrayList<String> synptemp = new ArrayList<String>();
			try {
				synptemp = getSynonyms(p1[length-1]);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(int i = 0; i< synptemp.size(); i++){
				getCombination(length+1,str+synptemp.get(i)+" ",p1,index);
			}
		}
	}
	public ArrayList<String> getSynonyms(String synword) throws IOException{
		ArrayList<String> temp = new ArrayList<String>();
		if (synword.equals("") || synword == null)
			return temp;
		String wnhome = "D:\\Program Files\\WordNet\\2.1";
	    String path = wnhome + File.separator+ "dict";       
	    File wnDir=new File(path);
	    URL url=new URL("file", null, path);
	    IDictionary dict=new Dictionary(url);
	    dict.open();//打开词典
	    IIndexWord idxWord = null;
	    int size = 0;
	    idxWord =dict.getIndexWord(synword, POS.ADJECTIVE);
	    if(idxWord != null){
	    	size = idxWord.getWordIDs().size();
	    	for(int i = 0; i< size; i++){
	    		IWordID wordID = idxWord.getWordIDs().get(i) ; // 1st meaning
	    		IWord word = dict.getWord(wordID);
	    		ISynset synset = word.getSynset (); //ISynset是一个词的同义词集的接
	    		// iterate over words associated with the synset
	    		for(IWord w : synset.getWords())
	    			temp.add(w.getLemma().toLowerCase());
	    			//System.out.println(w.getLemma());
	    	}
	    }
	    idxWord =dict.getIndexWord(synword, POS.ADVERB);
	    if(idxWord != null){
	    	size = idxWord.getWordIDs().size();
	    	for(int i = 0; i< size; i++){
	    		IWordID wordID = idxWord.getWordIDs().get(i) ; // 1st meaning
	    		IWord word = dict.getWord(wordID);
	    		ISynset synset = word.getSynset (); //ISynset是一个词的同义词集的接口
	    		// iterate over words associated with the synset
	    		for(IWord w : synset.getWords())
	    			temp.add(w.getLemma().toLowerCase());
	    			//System.out.println(w.getLemma());
	    	}
	    }
	    idxWord =dict.getIndexWord(synword, POS.NOUN);
	    if(idxWord != null){
	    	size = idxWord.getWordIDs().size();
	    	for(int i = 0; i< size; i++){
	    		IWordID wordID = idxWord.getWordIDs().get(i) ; // 1st meaning
	    		IWord word = dict.getWord(wordID);
	    		ISynset synset = word.getSynset (); //ISynset是一个词的同义词集的接口
	    		 // iterate over words associated with the synset
	    		for(IWord w : synset.getWords())
	    			temp.add(w.getLemma().toLowerCase());
	    			//System.out.println(w.getLemma());
	    	}
	    }
	    idxWord =dict.getIndexWord(synword, POS.VERB);
	    	if(idxWord != null){
	    	size = idxWord.getWordIDs().size();
	    	for(int i = 0; i< size; i++){
	    		IWordID wordID = idxWord.getWordIDs().get(i) ; // 1st meaning
	    		IWord word = dict.getWord(wordID);
	    		ISynset synset = word.getSynset (); //ISynset是一个词的同义词集的接口
	    		// iterate over words associated with the synset
	    		for(IWord w : synset.getWords())
	    			temp.add(w.getLemma().toLowerCase());
	    			//System.out.println(w.getLemma());
	    	}
	    }
	    if(temp.size() == 0)
	    	temp.add(synword);
	    return temp;
	}
	
	public String hump(String source){
		for(int i = 0; i< source.length(); i++){
			if(Character.isDigit(source.charAt(i)))
					return source;
		}
		int begin = 0;
		String temp = "";
		for(int i = 1; i < source.length(); i++){
			if(Character.isUpperCase(source.charAt(i)) == true){
				if(!Character.isUpperCase(source.charAt(i-1))){
					temp = temp + source.substring(begin, i) + " ";
					begin = i;
				}
			}
		}
		temp = temp + source.substring(begin);
		return temp;
	}
	
	public String testStandard(String testString){
		 if(testString.equals("No ") || testString.equals("nO ") || testString.equals("NO ") || testString.equals("no "))
			 return "no";
		 Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_36);      
		 Reader r = new StringReader(testString.replace("_", " ").replace(",", ""));      
		 StopFilter sf = (StopFilter) analyzer.tokenStream("", r);
		 CharTermAttribute cab = sf.addAttribute(CharTermAttribute.class); 
		 String temp = "";
		 try {
			while (sf.incrementToken()) {      
				 temp = temp + cab.toString()+" ";     
			 }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		} 
		 return temp;
	 }
	
	public static void main(String[] args){
		ProperyPair propertypair = new ProperyPair();
		double x = propertypair.ComparableDegree("family fam", "fam", 3);
		/*String[] p1 = propertypair.testStandard(propertypair.hump("AppleOfPear")).split(" ");
		propertypair.getCombination(1, "", p1,1);
		for(int i = 0; i< propertypair.synp1.size(); i++){
			System.out.println(propertypair.synp1.get(i));
		}*/
		System.out.println(x);
	}
}
