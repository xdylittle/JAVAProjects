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

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import isub.ISub;

public class ValuePair {
	ArrayList<String> synp1;
	ArrayList<String> synp2;
	
	public ValuePair(){
		synp1 = new ArrayList<String>();
		synp2 = new ArrayList<String>();
	}
	public double Similarity(String property1, String property2){
		//ֱ�Ӽ����������Ե����ƶ�
		double similarity = ISub.getSimilarity(property1, property2);
		
		//����wordnet�任֮������������Ե����ƶ�
		String[] p1 = testStandard(hump(property1)).split(" ");
		String[] p2 = testStandard(hump(property2)).split(" ");
		getCombination(1,"",p1,1);//���㾭��wordnet�任���ÿ��property�ĸ��ֲ�ͬ���
		getCombination(2,"",p2,2);
		for(int i = 0; i< synp1.size(); i++){
			for(int j = 0; j< synp2.size(); j++){
				double sim = ISub.getSimilarity(synp1.get(i), synp2.get(j));
				if(sim > similarity)
					similarity = sim;
			}
		}
		
		//����stem�任֮������������Ե����ƶ�
		
		return similarity;
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
					synp1.add(str+" "+synptemp.get(i));
				else
					synp2.add(str+" "+synptemp.get(i));
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
				getCombination(length+1,str+" "+synptemp.get(i),p1,index);
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
	    dict.open();//�򿪴ʵ�
	    IIndexWord idxWord = null;
	    int size = 0;
	    idxWord =dict.getIndexWord(synword, POS.ADJECTIVE);
	    if(idxWord != null){
	    	size = idxWord.getWordIDs().size();
	    	for(int i = 0; i< size; i++){
	    		IWordID wordID = idxWord.getWordIDs().get(i) ; // 1st meaning
	    		IWord word = dict.getWord(wordID);
	    		ISynset synset = word.getSynset (); //ISynset��һ���ʵ�ͬ��ʼ��Ľ�
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
	    		ISynset synset = word.getSynset (); //ISynset��һ���ʵ�ͬ��ʼ��Ľӿ�
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
	    		ISynset synset = word.getSynset (); //ISynset��һ���ʵ�ͬ��ʼ��Ľӿ�
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
	    		ISynset synset = word.getSynset (); //ISynset��һ���ʵ�ͬ��ʼ��Ľӿ�
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
		String[] p1 = propertypair.testStandard(propertypair.hump("AppleOfPear")).split(" ");
		propertypair.getCombination(1, "", p1,1);
		for(int i = 0; i< propertypair.synp1.size(); i++){
			System.out.println(propertypair.synp1.get(i));
		}
	}
}