package org.nuaa.freedom.wn2nj;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import edu.mit.jwi.item.Pointer;

public class Reader {
	private String wnhome ="D:\\WordNet\\3.1";
	private String path = wnhome + File.separator+ "dict";
	private URL url;       //创建一个URL对象，指向WordNet的ditc目录
	IDictionary dict;
	
	public static void main(String[] args) throws IOException {
		Reader test=new Reader();
		//test.getWords(POS.NOUN);
		//test.initSynset(POS.NOUN);
		//test.readSynset(POS.NOUN);
		//test.readWords(POS.NOUN);
	//	test.initSynsetWithDealer(POS.NOUN);
		//test.initSynsetWithExa(POS.NOUN);
		//test.readSynsetWithExa(POS.NOUN);
		test.readWordsWithExa(POS.NOUN);
		return;
	}
	
	
	public Reader() throws IOException {
		url=new URL("file", null, path);
		dict=new Dictionary(url);
		dict.open();
	}
	
	public Iterator<IIndexWord> getWords(POS pos) {

		//Iterator<IIndexWord> kk=dict.getIndexWordIterator(pos);
		return dict.getIndexWordIterator(pos);
	}
	
	public void initSynset(POS pos) {
		
		Iterator<ISynset> kk=dict.getSynsetIterator(pos);
		//System.out.println(kkk.getGloss()+" "+kkk.getOffset()+" "+kkk.getID().getOffset()+" "+kkk.getLexicalFile().getName()+" "+kkk.getWords());
		Neo4J_Exa test = new Neo4J_Exa("bolt://localhost:7687", "lwx", "1234");
		int i=0;
		
		while(kk.hasNext()){
			ISynset s=kk.next();
			test.addSynset(s.getOffset(), s.getLexicalFile().getName().replace(".","_"), s.getGloss());
			System.out.println(i/82192.0);
			i++;
		}
	    test.close();
	}
	
	
	public void readSynset(POS pos) {
		Iterator<ISynset> kk=dict.getSynsetIterator(pos);
		//System.out.println(kkk.getGloss()+" "+kkk.getOffset()+" "+kkk.getID().getOffset()+" "+kkk.getLexicalFile().getName()+" "+kkk.getWords());
		Neo4J_Exa test = new Neo4J_Exa("bolt://localhost:7687", "lwx", "1234");
		int i=0;
		
		while(kk.hasNext()){
			i++;
			ISynset s=kk.next();
			System.out.println("   "+s.getLexicalFile());
			Map<IPointer, List<ISynsetID>>m=s.getRelatedMap();
		for (IPointer pointer : m.keySet()) {
			//System.out.println("relationship : " + pointer.getName()+" symbol : "+pointer.getSymbol());
			for (ISynsetID sid : m.get(pointer)) {
				//System.out.println("		related synsets: "+dict.getSynset(sid).getGloss());
				//test.addRelationShip(s.getOffset(),s.getLexicalFile().toString().replace(".", "_") ,sid.getOffset(),dict.getSynset(sid).getLexicalFile().toString().replace(".", "_"), pointer.getName());
				System.out.println(s.getLexicalFile().toString().replace(".", "_")+"~~~"+dict.getSynset(sid).getLexicalFile().toString().replace(".", "_"));
				
			}
//			
		}
			System.out.println(i/82192.0);
		}
	    test.close();
		//MATCH (s1:Synset),(s2:Synset) WHERE (s1.SID=7846) AND (s2.SID=4475)  CREATE (s1)-[:Synset]->(s2)
	}
	
	public void readWords(POS pos) {
		Iterator<IIndexWord> kk=dict.getIndexWordIterator(pos);
		//System.out.println(kkk.getGloss()+" "+kkk.getOffset()+" "+kkk.getID().getOffset()+" "+kkk.getLexicalFile().getName()+" "+kkk.getWords());
		Neo4J_Exa test = new Neo4J_Exa("bolt://localhost:7687", "lwx", "1234");
		int i=0;
		
		while(kk.hasNext()){
			
			IIndexWord s=kk.next();
			String wordLemma=s.getLemma();
			//System.out.println(s.getID()+ " " );
			List<IWordID> idList=s.getWordIDs();
			for (IWordID IwordID : idList) {
				i++;
				//test.addWord(wordLemma,IwordID.getSynsetID().getOffset());
				//System.out.println(wordLemma+" : "+IwordID.getSynsetID().getOffset() );
			}
		//	System.out.println(i/6969782.0);
		}
	    test.close();
	    System.out.println(i/146512.0);
		//MATCH (s1:Synset),(s2:Synset) WHERE (s1.SID=7846) AND (s2.SID=4475)  CREATE (s1)-[:Synset]->(s2)
	}
	public void readWordsWithExa(POS pos) {
		Iterator<IIndexWord> kk=dict.getIndexWordIterator(pos);
		Neo4J_Exa.open();
		Neo4J_Exa.addWordWithWN(kk, dict);
		Neo4J_Exa.close();
		
	}
	public void initSynsetWithExa(POS pos) {
		
		Iterator<ISynset> kk=dict.getSynsetIterator(pos);
		Neo4J_Exa.open();
		Neo4J_Exa.addSynsetWithWN(kk);
		Neo4J_Exa.close();
		
	}
	
	public void readSynsetWithExa(POS pos) {
		Iterator<ISynset> kk=dict.getSynsetIterator(pos);
		Neo4J_Exa.open();
		Neo4J_Exa.addRelationShipWithWN(kk, dict);
		Neo4J_Exa.close();
	}
	
	public void initSynsetWithDealer(POS pos) {
		
		Iterator<ISynset> kk=dict.getSynsetIterator(pos);
		Neo4J_Dealer dealer=new Neo4J_Dealer();
		dealer.createSynsets(kk);
	}
	
	
	

}
