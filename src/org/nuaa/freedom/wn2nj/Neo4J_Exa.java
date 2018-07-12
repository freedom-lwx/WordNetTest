package org.nuaa.freedom.wn2nj;

import org.neo4j.driver.internal.shaded.io.netty.buffer.DuplicatedByteBuf;
import org.neo4j.driver.v1.*;
import org.neo4j.driver.v1.types.Node;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IPointer;
import edu.mit.jwi.item.ISynset;
import edu.mit.jwi.item.ISynsetID;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;

import static org.neo4j.driver.v1.Values.parameters;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Neo4J_Exa {
	// Driver objects are thread-safe and are typically made available
	// application-wide.
	static private Driver driver;

	private GraphDatabaseService db;

	static public void open() {

		driver = GraphDatabase.driver("bolt://localhost:7687", AuthTokens.basic("lwx", "1234"));
	}

	public Neo4J_Exa(String uri, String user, String password) {

		// driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
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

	static public void addSynsetWithWN(Iterator<ISynset> IT) {
		// Sessions are lightweight and disposable connection wrappers.

		try (Session session = driver.session()) {
			try (Transaction tx = session.beginTransaction()) {

				int i = 0;
				while (IT.hasNext()) {
					ISynset s = IT.next();
					String str = "CREATE (a:Synset:" + s.getLexicalFile().getName().replace(".", "_")
							+ " {SID: {S} , Gloss:{G}})";
					tx.run(str, parameters("S", s.getOffset(), "G", s.getGloss()));
					System.out.println(i / 82192.0);
					i++;
				}
				tx.success(); // Mark this write as successful.
			}
		}
	}

	static public void addRelationShipWithWN(Iterator<ISynset> IT, IDictionary dict) {
		int i = 0;
		try (Session session = driver.session()) {
			while (IT.hasNext()) {
				i++;
				ISynset s = IT.next();
				StringBuffer match = new StringBuffer();
				StringBuffer where = new StringBuffer();
				StringBuffer create = new StringBuffer();
				String createHead="CREATE (s1)";
				
				match.append("MATCH (s1:"+s.getLexicalFile().toString().replace(".", "_")+")");
				where.append(" WHERE (s1.SID="+s.getOffset()+")");
				
				Map<IPointer, List<ISynsetID>> m = s.getRelatedMap();
				for (IPointer pointer : m.keySet()) {
					for (ISynsetID sid : m.get(pointer)) {
						// String str="MATCH (s1:"+s.getLexicalFile().toString().replace(".",
						// "_")+"),(s2:"+dict.getSynset(sid).getLexicalFile().toString().replace(".",
						// "_")+") WHERE (s1.SID={S1}) AND (s2.SID={S2}) CREATE
						// (s1)-[a:"+pointer.getName().replace("-", "").replace(" ", "")+"]->(s2)";
						ISynset synset=dict.getSynset(sid);
						match.append(",(s"+synset.getOffset()+":"+synset.getLexicalFile().toString().replace(".", "_")+")");
						where.append(" AND (s"+synset.getOffset()+".SID="+synset.getOffset()+") ");
						create.append("CREATE (s1)-[:"+pointer.getName().replace("-", "").replace(" ", "")+"]->(s"+synset.getOffset()+") ");
//						String str = "MATCH (s1:" + s.getLexicalFile().toString().replace(".", "_") + "),(s2:"
//								+ dict.getSynset(sid).getLexicalFile().toString().replace(".", "_")
//								+ ") WHERE (s1.SID={S1}) AND (s2.SID={S2})  CREATE (s1)-[a:"
//								+ pointer.getName().replace("-", "").replace(" ", "") + "]->(s2)";
//
//						//System.out.println(str);
//						StatementResult run = session.run(str, parameters("S1", s.getOffset(), "S2", sid.getOffset()));
					}
				}
				
				String strplus=match.toString()+where.toString()+create.toString();
				session.run(strplus);
				System.out.println(strplus);
				System.out.println(i / 82192.0);
			}

		}
	}

	static public void addWordWithWN(Iterator<IIndexWord> IT,IDictionary dict) {
		// Sessions are lightweight and disposable connection wrappers.
		try (Session session = driver.session()) {
			// Wrapping Cypher in an explicit transaction provides atomicity
			// and makes handling errors much easier.
			int i=0;
			while(IT.hasNext()){
				IIndexWord s=IT.next();
				boolean flag=true;
				StringBuffer match = new StringBuffer();
				StringBuffer where = new StringBuffer();
				StringBuffer create = new StringBuffer();
				match.append("MATCH ");
				where.append(" WHERE ");
				create.append(" CREATE(word:Word{lamma:\""+s.getLemma()+"\"}) ");
				
				String wordLemma=s.getLemma();
				List<IWordID> idList=s.getWordIDs();
				for (IWordID ID : idList) {
					ISynset synset=dict.getSynset(ID.getSynsetID());
					if(flag) {					
						match.append("(s"+synset.getOffset()+":Synset:"+synset.getLexicalFile().toString().replace(".", "_")+")");
						where.append("(s"+synset.getOffset()+".SID="+synset.getOffset()+")");		
						flag=false;
					}
					else {
						match.append(",(s"+synset.getOffset()+":Synset:"+synset.getLexicalFile().toString().replace(".", "_")+")");
						where.append(" AND (s"+synset.getOffset()+".SID="+synset.getOffset()+")");						
					}
					create.append("CREATE (word)-[:word2synset]->(s"+synset.getOffset()+") ");	
					//session.run(str, parameters("x", word));					
					i++;
					//test.addWord(wordLemma,IwordID.getSynsetID().getOffset());
					//System.out.println(wordLemma+" : "+IwordID.getSynsetID().getOffset() );
				}
				String str = match.toString()+where.toString()+create.toString();
				//System.out.println(str);
				session.run(str);	
			//	System.out.println(i/6969782.0);
				 System.out.println(i/146512.0);
			}
		   
			

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

	private void printWord(String initial) {
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

	static public void close() {
		// Closing a driver immediately shuts down all open connections.
		driver.close();
	}

	public static void main(String... args) throws IOException {

	}
}