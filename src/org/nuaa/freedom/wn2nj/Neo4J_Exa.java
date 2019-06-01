package org.nuaa.freedom.wn2nj;

import org.neo4j.driver.internal.shaded.io.netty.buffer.DuplicatedByteBuf;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import static org.neo4j.driver.v1.Values.parameters;
import static org.nuaa.freedom.wn2nj.Commons.*;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Neo4J_Exa {
	// Driver objects are thread-safe and are typically made available
	// application-wide.
	private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() * 2;
	private Driver driver;
	private ExecutorService executorService ;

	public Neo4J_Exa(String uri, String user, String password) {
		driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic(user, password));
		refreshExecutor();
	}

	private void refreshExecutor() {
		// TODO Auto-generated method stub
		if (executorService!=null) {
			executorService.shutdownNow();
		}
		executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
	}

	public void addWord(String word, String LABLE, int synID) {
		// Sessions are lightweight and disposable connection wrappers.
		try (Session session = driver.session()) {
			// Wrapping Cypher in an explicit transaction provides atomicity
			// and makes handling errors much easier.
			try (Transaction tx = session.beginTransaction()) {
				String s = "CREATE (a:Word:" + LABLE + " {lamma: {x}}) ";
				tx.run("CREATE (a:Word:{L} {lamma: {x}}) ", parameters("x", word));
				tx.success(); // Mark this write as successful.
			}
		}
	}

	public void addSynset(int iSynsetID, String lexical, String gloss) {
		// Sessions are lightweight and disposable connection wrappers.
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				String s = "CREATE (a:Synset:" + lexical + " {SID: {S} , Gloss:{G}})";
				tx.run(s, parameters("S", iSynsetID, "G", gloss));
				tx.success(); // Mark this write as successful.
			}

		}
	}

	public void initSynsetInNeo4J(Iterator<ISynset> it, IDictionary dict) {
		// Sessions are lightweight and disposable connection wrappers.
		Set<ISynset> synset = new HashSet<>();
		while (it.hasNext()) {
			synset.add(it.next());
		}

		ISynset[] synsetArray = synset.toArray(new ISynset[0]);
		int split = synsetArray.length / THREAD_POOL_SIZE;
		
		System.out.println("Init synset ...");
		for (int i = synsetArray.length; i >= 0; i -= split) {
			executorService.execute(new AddSynsetNodeTask(synsetArray,(i-split)>0? (i-split):0, i));
		}
		
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.DAYS);
			refreshExecutor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Init synset over.");

		
		System.out.println("Init synset relation ...");
		for (int i = synsetArray.length; i >= 0; i -= split) {
			executorService.execute(new AddSynsetRelationTask(synsetArray, dict, (i-split)>0? (i-split):0, i));
		}
		
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.DAYS);
			refreshExecutor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Init synset relation over.");

		
	}
	
	public void initWordInNeo4J(Iterator<IIndexWord> it, IDictionary dict) {
		Set<IIndexWord> synset = new HashSet<>();
		while (it.hasNext()) {
			synset.add(it.next());
		}
		IIndexWord[] wordArray = synset.toArray(new IIndexWord[0]);
		
		int split = wordArray.length / THREAD_POOL_SIZE;
		System.out.println("Init words ...");
		
		for (int i = wordArray.length; i >= 0; i -= split) {
			executorService.execute(new AddWordNodeTask(wordArray,(i-split)>0? (i-split):0, i));
		}
		
		try {
			executorService.shutdown();
			executorService.awaitTermination(1, TimeUnit.DAYS);
			refreshExecutor();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Init words over.");
		
	}

	class AddSynsetNodeTask implements Runnable {
		ISynset[] synsetArray;
		int start;
		int end;
		public AddSynsetNodeTask(ISynset[] synsetArray, int start, int end) {
			this.synsetArray = synsetArray;
			this.start = start;
			this.end = end;
		}
		@Override
		public void run() {
			// TODO Auto-generated method stub
			try (Session session = driver.session()) {
				for (int k = start; k < end; k++) {
					ISynset s = synsetArray[k];
					String str = "CREATE (a:Synset:" + s.getLexicalFile().getName().replace(".", "_")
							+ " {SID: {S} , Gloss:{G}})";
					session.run(str, parameters("S", s.getOffset(), "G", s.getGloss()));
				}
			}
		}

	}
	
	class AddSynsetRelationTask implements Runnable {
		ISynset[] synsetArray;
		int start;
		int end;
		IDictionary dict;

		public AddSynsetRelationTask(ISynset[] synsetArray,IDictionary dict, int start, int end) {
			this.synsetArray = synsetArray;
			this.start = start;
			this.end = end;
			try {
				this.dict=new Dictionary(new URL("file", null, Reader.WORDNET_HOME));
				this.dict.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void run() {
			try (Session session = driver.session()) {
				for (int k = start; k < end; k++) {
					ISynset s = synsetArray[k];
					StringBuffer match = new StringBuffer();
					StringBuffer where = new StringBuffer();
					StringBuffer create = new StringBuffer();
					String createHead = "CREATE (s1)";

					match.append("MATCH (s1:" + s.getLexicalFile().toString().replace(".", "_") + ")");
					where.append(" WHERE (s1.SID=" + s.getOffset() + ")");

					Map<IPointer, List<ISynsetID>> m = s.getRelatedMap();
					for (IPointer pointer : m.keySet()) {
						for (ISynsetID sid : m.get(pointer)) {
							// String str="MATCH (s1:"+s.getLexicalFile().toString().replace(".",
							// "_")+"),(s2:"+dict.getSynset(sid).getLexicalFile().toString().replace(".",
							// "_")+") WHERE (s1.SID={S1}) AND (s2.SID={S2}) CREATE
							// (s1)-[a:"+pointer.getName().replace("-", "").replace(" ", "")+"]->(s2)";
							ISynset synset = dict.getSynset(sid);
							match.append(",(s" + synset.getOffset() + ":"
									+ synset.getLexicalFile().toString().replace(".", "_") + ")");
							where.append(" AND (s" + synset.getOffset() + ".SID=" + synset.getOffset() + ") ");
							create.append("CREATE (s1)-[:" + pointer.getName().replace("-", "").replace(" ", "")
									+ "]->(s" + synset.getOffset() + ") ");
//							String str = "MATCH (s1:" + s.getLexicalFile().toString().replace(".", "_") + "),(s2:"
//									+ dict.getSynset(sid).getLexicalFile().toString().replace(".", "_")
//									+ ") WHERE (s1.SID={S1}) AND (s2.SID={S2})  CREATE (s1)-[a:"
//									+ pointer.getName().replace("-", "").replace(" ", "") + "]->(s2)";
							//
//							//System.out.println(str);
//							StatementResult run = session.run(str, parameters("S1", s.getOffset(), "S2", sid.getOffset()));
						}
					}

					String strplus = match.toString() + where.toString() + create.toString();
					session.run(strplus);
				}
			}
			
			this.dict.close();
		}

	}

	
	
	private class AddWordNodeTask implements Runnable{
		IIndexWord[] wordArray;
		IDictionary dict; 
		int start; 
		int end;
		public AddWordNodeTask(IIndexWord[] wordArray, int start, int end) {
			this.wordArray=wordArray;
			this.start=start;
			this.end=end;
			try {
				this.dict=new Dictionary(new URL("file", null, Reader.WORDNET_HOME));
				this.dict.open();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		@Override
		public void run() {
			try (Session session = driver.session()) {
				// Wrapping Cypher in an explicit transaction provides atomicity
				// and makes handling errors much easier.
				for (int j = start; j < end; j++) {
					IIndexWord s = wordArray[j];
					boolean flag = true;
					StringBuffer match = new StringBuffer();
					StringBuffer where = new StringBuffer();
					StringBuffer create = new StringBuffer();
					match.append("MATCH ");
					where.append(" WHERE ");
					create.append(" CREATE(word:Word{lamma:\"" + s.getLemma() + "\"}) ");
					String wordLemma = s.getLemma();
					List<IWordID> idList = s.getWordIDs();
					for (IWordID ID : idList) {
						ISynset synset = dict.getSynset(ID.getSynsetID());
						if (flag) {
							match.append("(s" + synset.getOffset() + ":Synset:"
									+ synset.getLexicalFile().toString().replace(".", "_") + ")");
							where.append("(s" + synset.getOffset() + ".SID=" + synset.getOffset() + ")");
							flag = false;
						} else {
							match.append(",(s" + synset.getOffset() + ":Synset:"
									+ synset.getLexicalFile().toString().replace(".", "_") + ")");
							where.append(" AND (s" + synset.getOffset() + ".SID=" + synset.getOffset() + ")");
						}
						create.append("CREATE (word)-[:word2synset]->(s" + synset.getOffset() + ") ");
						// session.run(str, parameters("x", word));

						// test.addWord(wordLemma,IwordID.getSynsetID().getOffset());
						// System.out.println(wordLemma+" : "+IwordID.getSynsetID().getOffset() );
					}
					String str = match.toString() + where.toString() + create.toString();
					session.run(str);
				}
					

			}
			this.dict.close();
		}
		
	}
	

	public void addRelationShip(int set1, String lable1, int set2, String lable2, String Type) {
		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {
				// tx.run("MERGE (a:Synset {SID: {S} , Type:{T} , Gloss:{G}})", parameters("S",
				// iSynsetID,"T",lexical,"G",gloss));
				String s = "MATCH (s1:" + lable1 + "),(s2:" + lable2
						+ ") WHERE (s1.SID={S1}) AND (s2.SID={S2})  CREATE (s1)-[a:"
						+ Type.replace("-", "").replace(" ", "") + "]->(s2)";
				tx.run(s, parameters("S1", set1, "S2", set2));
				tx.success(); // Mark this write as successful.
			}
		}
	}

	public void printWord(String initial) {
		try (Session session = driver.session()) {
			// Auto-commit transactions are a quick and easy way to wrap a read.
			StatementResult result = session.run("MATCH (Word) WHERE a.lamma STARTS WITH {x} RETURN a.word AS word",
					parameters("x", initial));
			// Each Cypher execution returns a stream of records.
			while (result.hasNext()) {
				Record record = result.next();
				// Values can be extracted from a record by index or name.
				System.out.println(record.get("word").asString());
			}
		}
	}

	public List<Long> checkRelation(String word1, String word2, int maxGap) {
		List<Long> list = new LinkedList<>();
		long times;
		try (Session session = driver.session()) {
			long x = 0;
			for (int i = 1; i <= maxGap; i++) {
				times = System.currentTimeMillis();
				String state = String.format("MATCH k=(a {lamma:\"%s\"})-[*%d]-(b{lamma:\"%s\"}) RETURN DISTINCT k",
						word1, i, word2);
				StatementResult result = session.run(state);

				if (result.hasNext()) {
					times = System.currentTimeMillis() - times;
					for (int j = i; j <= maxGap; j++) {
						list.add(x + times);
					}
					break;
				}
				x += System.currentTimeMillis() - times;
				list.add(x);
			}
		}
		return list;
	}

}