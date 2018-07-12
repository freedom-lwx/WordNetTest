package org.nuaa.freedom.wn2nj;


import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import edu.mit.jwi.item.ISynset;

import org.neo4j.graphdb.Label;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

/*
 * 
 * 直接使用Neo4J的包装好的API进行操作 ，初步设想遇到了两个问题
 * 1 如何打开已经存在的数据库？
 * 2 创建结点时，节点的属性和创建节点这个步骤是分开的
 * 
 * */
public class Neo4J_Dealer {
	
	private GraphDatabaseFactory dbFactory ;
	private GraphDatabaseService db ;
	public Map<Integer,Node> dbNode; //内存中存储所有的syn节点，以便创建关系
	
	public Neo4J_Dealer() {
		// TODO Auto-generated constructor stub
		dbFactory = new GraphDatabaseFactory();
		db = dbFactory.newEmbeddedDatabase(new File("D:/WD2NJ"));
		dbNode=new HashMap<>();
	}
	
	
	public enum Tutorials implements Label {
		NOUN,VERB;
	}
	public enum Noun_labels implements Label {
		noun_Tops,noun_act,noun_animal,noun_artifact,noun_attribute,noun_body,noun_cognition,noun_communication,noun_event,noun_feeling,noun_food,noun_group,noun_location,noun_motive,noun_object,noun_person,noun_phenomenon,noun_plant,noun_possession,noun_process,noun_quantity,noun_relation,noun_shape,noun_state,noun_substance,noun_time,error;
	}
	
	public Label getLabel(int i ) {
		if(i>26||i<0)
			return Noun_labels.error;
		return Noun_labels.values()[i];
	}

	
	public void createSynsets(int label,int SID,String GLOSS) {
		try (Transaction tx = db.beginTx()) {
			
			Node synNode = db.createNode( getLabel(label ));
			synNode.setProperty("SID", SID);
			synNode.setProperty("GLOSS",GLOSS);
			tx.success();
			dbNode.put(SID, synNode);
//			Node scalaNode = db.createNode(Tutorials.NOUN);
//			scalaNode.setProperty("TutorialID", "SCALA001");
//			scalaNode.setProperty("Title", "Learn Scala");
//			scalaNode.setProperty("NoOfChapters", "20");
//			scalaNode.setProperty("Status", "Completed");
//			
//			Relationship relationship = javaNode.createRelationshipTo
//			(scalaNode,RelationshipType.withName("TEST"));
//			relationship.setProperty("Id","1234");
//			relationship.setProperty("OOPS","YES");
//			relationship.setProperty("FP","YES");
		}
		
	}
	
	public void createSynsets(Iterator<ISynset> it) {
		try (Transaction tx = db.beginTx()) {
			int i=0;
			while(it.hasNext()){
				ISynset s=it.next();
				Node synNode = db.createNode( getLabel(s.getLexicalFile().getNumber() ));
				synNode.setProperty("SID",  s.getOffset());
				synNode.setProperty("GLOSS", s.getGloss());
		
				dbNode.put(s.getOffset(), synNode);
				System.out.println(i/82192.0);
				i++;
				
				
			}
			System.out.println(dbNode.size());
			
			
		
			tx.success();
			System.out.println("ok");
//			Node scalaNode = db.createNode(Tutorials.NOUN);
//			scalaNode.setProperty("TutorialID", "SCALA001");
//			scalaNode.setProperty("Title", "Learn Scala");
//			scalaNode.setProperty("NoOfChapters", "20");
//			scalaNode.setProperty("Status", "Completed");
//			
//			Relationship relationship = javaNode.createRelationshipTo
//			(scalaNode,RelationshipType.withName("TEST"));
//			relationship.setProperty("Id","1234");
//			relationship.setProperty("OOPS","YES");
//			relationship.setProperty("FP","YES");
		}
		
	}
	
	
  public static void main(String[] args) {
		

 }
}