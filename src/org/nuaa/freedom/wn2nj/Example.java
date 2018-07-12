package org.nuaa.freedom.wn2nj;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;

import org.xml.sax.ext.LexicalHandler;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IExceptionEntry;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.LexFile;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;
import edu.mit.jwi.item.SynsetID;

public class Example {

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
	      testDitctionary();
	}
    public static void testDitctionary() throws IOException{

        // construct the URL to the Wordnet dictionary directory

       // String wnhome = System.getenv("WN_HOME"); //获取环境变量WNHOME
    	String wnhome ="D:\\WordNet\\3.1";
        String path = wnhome + File.separator+ "dict";
        //System.out.println(path);
        URL url=new URL("file", null, path);  //创建一个URL对象，指向WordNet的ditc目录

        // construct the dictionary object and open it

        IDictionary dict=new Dictionary(url);

        dict.open(); //打开词典

       
        Iterator<IIndexWord> it= dict.getIndexWordIterator(POS.NOUN);
        
//        for (;it.hasNext();) {
//        	System.out.println(it.next().getLemma());
//		}

        // look up first sense of the word "dog "

       
        IIndexWord idxWord=dict.getIndexWord("cake", POS.NOUN);//获取一个索引词，（dog,名词）
        
        IWordID wordID=idxWord.getWordIDs().get(0);//获取dog第一个词义ID

        IWord word = dict.getWord(wordID); //获取该词
        
        System.out.println("cake 的 noun有"+idxWord.getWordIDs().size()+" 个！");
        System .out . println ("Id = " + wordID );

        System .out . println (" 词元 = " + word . getLemma ());

        System .out . println (" 注解 = " + word . getSynset (). getGloss ());
       // System.out.println(word.getSenseKey());
        
//        List<ISynsetID> synset=word.getSynset().getRelatedSynsets(Pointer.HYPERNYM);
//        for (ISynsetID iSynsetID : synset) {
//        	System.out.println(dict.getSynset(iSynsetID).getGloss());
//		}
//        
//        Iterator<ISynset> synset2=dict.getSynsetIterator(POS.NOUN);
// 
//        while (synset2.hasNext()) {
//			System.out.println(synset2.next());
//		}
        
        
        
        /*
         * GET all the words related to time
         * 
         * */
        ISynsetID time=new SynsetID(15147173, POS.NOUN);
        for(ISynsetID iID: dict.getSynset(time).getRelatedSynsets(Pointer.HYPONYM)) {
        	ISynset synset3=dict.getSynset(iID);
            for(IWord W: synset3.getWords()) {
            	System.out.println(W.getLemma()+" "+W.getLexicalID()+" ");
            	
            }
        }
        
        /*
         * get all the synsets in the noun.time
         * */
        int i=0;
        Iterator<ISynset> kk =dict.getSynsetIterator(POS.NOUN);
        while (kk.hasNext()) {
        	i++;
        	ISynset kkk=kk.next();
        	if (kkk.getLexicalFile()==LexFile.NOUN_TIME) {
        		for (IWord ddd  : kkk.getWords()) {
					//System.out.print(ddd.getLemma()+" ");
				}
        		//System.out.println(" ");
			}
		}
        System.out.println("we have "+i+" noun synsets");
        
       System.out.println(LexFile.getLexicalFile(26)); 
   
       i=0;
       Iterator<IIndexWord> words=dict.getIndexWordIterator(POS.NOUN);
       while (words.hasNext()) {
    	   words.next();
		//System.out.println(words.next().getLemma());
		i++;
	}
       System.out.println("we have "+i+" noun words");
       
       
       /*
        * Words derived from specified IsysnsetID with more detail
        * */
       
       ISynsetID test1=new SynsetID(406181, POS.NOUN);
       System.out.println(test1);
       for (IWord www : dict.getSynset(test1).getWords()) {
		System.out.println(www.getLemma());
		System.out.println(www.getID());
	}
       
        
        
        
}

}