package weight;

import  java.lang.reflect.Method;
import  java.util.ArrayList;
import  java.util.Iterator;

import  org.tartarus.snowball.SnowballProgram;

public   class  Tokenizer  {
     public   static  String language  =   "english" ;

     private   static  SnowballProgram stemmer  =   null ;

     private   static  Method stemMethod  =   null ;

     public   static  String tokenize(String source)  {
         if  (Tokenizer.stemmer  ==   null )  {
             try   {
                Class stemClass  =  Class.forName( "org.tartarus.snowball.ext." 
                         + Tokenizer.language +   "Stemmer" );
                Tokenizer.stemmer  =  (SnowballProgram) stemClass.newInstance();
                Tokenizer.stemMethod  =  stemClass
                        .getMethod( "stem" ,  new  Class[ 0 ]);
            }   catch  (Exception e)  {
            	e.printStackTrace();
                System.out.println( " Error when initializing Stemmer! " );
                System.exit( 1 );
            } 
        } 

        ArrayList tokens  =   new  ArrayList();
        StringBuffer buffer  =   new  StringBuffer();
         for  ( int  i  =   0 ; i  <  source.length(); i ++ )  {
             char  character  =  source.charAt(i);
             if  (Character.isLetter(character))  {
                buffer.append(character);
            }   else   {
                 if  (buffer.length()  >   0 )  {
                    tokens.add(buffer.toString());
                    buffer  =   new  StringBuffer();
                } 
            } 
        } 
         if  (buffer.length()  >   0 )  {
            tokens.add(buffer.toString());
        } 

        ArrayList words  =   new  ArrayList();

        ArrayList allTheCapitalWords  =   new  ArrayList();

        nextToken:  for  (Iterator allTokens  =  tokens.iterator(); allTokens
                .hasNext();)  {
            String token  =  (String) allTokens.next();

             boolean  allUpperCase  =   true ;
             for  ( int  i  =   0 ; i  <  token.length(); i ++ )  {
                 if  ( ! Character.isUpperCase(token.charAt(i)))  {
                    allUpperCase  =   false ;
                } 
            } 
             if  (allUpperCase)  {
                allTheCapitalWords.add(token);
                 continue  nextToken;
            } 

             int  index  =   0 ;
            nextWord:  while  (index  <  token.length())  {
                nextCharacter:  while  ( true )  {
                    index ++ ;
                     if  ((index  ==  token.length())
                             ||   ! Character.isLowerCase(token.charAt(index)))  {
                         break  nextCharacter;
                    } 
                } 
                words.add(token.substring( 0 , index).toLowerCase());
                token  =  token.substring(index);
                index  =   0 ;
                 continue  nextWord;
            } 
        } 

         try   {
             for  ( int  i  =   0 ; i  <  words.size(); i ++ )  {
                Tokenizer.stemmer.setCurrent((String) words.get(i));
                Tokenizer.stemMethod.invoke(Tokenizer.stemmer,  new  Object[ 0 ]);
                words.set(i, Tokenizer.stemmer.getCurrent());
            } 
        }   catch  (Exception e)  {
            e.printStackTrace();
        } 

        words.addAll(allTheCapitalWords);
        String temp = "";
        Iterator it = words.iterator();
        while(it.hasNext()){
        	temp = temp+it.next().toString()+" ";
        }
       
         return  temp;
    } 
}

