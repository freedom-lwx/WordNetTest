package org.nuaa.freedom.wn2nj;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
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
import static org.nuaa.freedom.wn2nj.Commons.*;

public class Reader {
	public static final String WORDNET_HOME = "D:\\WordNet\\3.1\\dict";


	private URL url;
	private IDictionary dict;
	private Neo4J_Exa neo4jWorker;

	public static void main(String[] args) throws IOException {
		Reader test = new Reader();
//		test.initSynsetWithExa(POS.NOUN);
		test.initWordsWithExa(POS.NOUN);
		
		

	}

	public Reader() throws IOException {
		url = new URL("file", null, WORDNET_HOME);
		dict = new Dictionary(url);
		dict.open();
		neo4jWorker = new Neo4J_Exa("bolt://localhost:7687", "lwx", "1234");
	}

	public Iterator<IIndexWord> getAllWords(POS pos) {
		return dict.getIndexWordIterator(pos);
	}

	public void initSynset2Neo4J(POS pos) {
		Iterator<ISynset> kk = dict.getSynsetIterator(pos);
		int i = 0;
		while (kk.hasNext()) {
			ISynset s = kk.next();
			neo4jWorker.addSynset(s.getOffset(), s.getLexicalFile().getName().replace(".", "_"), s.getGloss());
			System.out.println(i * 1.0 / pos.getNumber());
			i++;
		}

	}

	@Deprecated
	public void readSynset(POS pos) {
		Iterator<ISynset> kk = dict.getSynsetIterator(pos);
		int i = 0;
		while (kk.hasNext()) {
			i++;
			ISynset s = kk.next();
			System.out.println("   " + s.getLexicalFile());
			Map<IPointer, List<ISynsetID>> m = s.getRelatedMap();
			for (IPointer pointer : m.keySet()) {
				for (ISynsetID sid : m.get(pointer)) {
					System.out.println(s.getLexicalFile().toString().replace(".", "_") + "~~~"
							+ dict.getSynset(sid).getLexicalFile().toString().replace(".", "_"));
				}
			}
			System.out.println(i / 82192.0);
		}
	}

	@Deprecated
	public void readWords(POS pos) {
		Iterator<IIndexWord> kk = dict.getIndexWordIterator(pos);
		// System.out.println(kkk.getGloss()+" "+kkk.getOffset()+"
		// "+kkk.getID().getOffset()+" "+kkk.getLexicalFile().getName()+"
		// "+kkk.getWords());
		int i = 0;

		while (kk.hasNext()) {

			IIndexWord s = kk.next();
			String wordLemma = s.getLemma();
			// System.out.println(s.getID()+ " " );
			List<IWordID> idList = s.getWordIDs();
			for (IWordID IwordID : idList) {
				i++;
				// test.addWord(wordLemma,IwordID.getSynsetID().getOffset());
				// System.out.println(wordLemma+" : "+IwordID.getSynsetID().getOffset() );
			}
			// System.out.println(i/6969782.0);
		}
		System.out.println(i / 146512.0);
		// MATCH (s1:Synset),(s2:Synset) WHERE (s1.SID=7846) AND (s2.SID=4475) CREATE
		// (s1)-[:Synset]->(s2)
	}

	public void initWordsWithExa(POS pos) {
		Iterator<IIndexWord> wordIt = dict.getIndexWordIterator(pos);
		//多线程
		neo4jWorker.initWordInNeo4J(wordIt, dict);
	}

	public void initSynsetWithExa(POS pos) {
		Iterator<ISynset> synsetIt = dict.getSynsetIterator(pos);
		//多线程
		neo4jWorker.initSynsetInNeo4J(synsetIt,dict);
		
	}



}
