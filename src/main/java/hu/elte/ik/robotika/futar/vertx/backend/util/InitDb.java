package hu.elte.ik.robotika.futar.vertx.backend.util;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

public class InitDb {
	
	private static GraphDatabaseService graphDb;
	private static DbManipulator db;

	public static void main(String[] args) {
			
		try {
		
			createDb();
			db = new DbManipulator(graphDb);
			db.readNodes("hu/elte/ik/robotika/futar/vertx/backend/database/map/nodes.txt");
			db.readEdges("hu/elte/ik/robotika/futar/vertx/backend/database/map/edges.txt");
			
			db.insertMapNodes();
			db.insertMapEdges();
			db.insertTestUsers();
					
//			printer.printDb(graphDb);
//			printer.printAllUsers(graphDb);
//			printer.printAllNodes(graphDb);
//			printer.printAllEdges(graphDb);
			
//			db.insertUser("Peter Huszti", "peterhuszti", "futar", 2);
//			db.insertNode(new IntN(-1,-1), new IntN(-1,-1,-1,-1));
			
//			printer.printDb(graphDb);

//			db.deleteNode(-1, -1);
//			db.deleteuser("peterhuszti");
			
//			printer.printDb(graphDb);
			
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
//			db.deleteAllEdge();
//			db.deleteAllNode();
//			db.deleteAlluser();
			shutDown();
		}
		
	}
	
	public static void createDb(){
//		System.out.println();
//	    System.out.println( "Starting database ..." );
		
	    graphDb = new GraphDatabaseFactory().newEmbeddedDatabase( "hu/elte/ik/robotika/futar/vertx/backend/database/" );
	}
	
	public static void shutDown() {
//        System.out.println();
//        System.out.println( "Shutting down database ..." );

		graphDb.shutdown();
    }
	
}
