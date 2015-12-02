package hu.elte.ik.robotika.futar.vertx.backend.util.data;

import java.util.ArrayList;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

	public class Printer {
	
		public static void printGrid(IntN[][] grid) {
			int gridSizeVer = grid.length;
			int gridSizeHor = grid[0].length;
			
			for(int i=0; i<gridSizeVer; ++i) {
				for(int j=0; j<gridSizeHor; ++j) {
					grid[i][j].printAll();
					System.out.print(" ");
				}
				System.out.println("");
			}
			System.out.println("");
		}
		
		public static void printIntNArray(ArrayList<IntN> list) {		
			for(int i=0; i<list.size(); ++i) {
				list.get(i).printAll();
			}
			System.out.println("");
		}
		
		public static void printDb(GraphDatabaseService graphDb) {
			System.out.println("Printing all database ...");
			try (Transaction tx = graphDb.beginTx();				
				 Result result = graphDb.execute("MATCH (n) RETURN n")) {
				
				System.out.println(result.resultAsString()); 

				tx.success();
			}	
		}
		public static void printAllUser(GraphDatabaseService graphDb) {
			System.out.println("Printing all users ...");
			try (Transaction tx = graphDb.beginTx();				
				 Result result = graphDb.execute("MATCH (n {type: 'user'}) RETURN n")) {
				
				System.out.println(result.resultAsString()); 

				tx.success();
			}	
		}
		public static void printAllNode(GraphDatabaseService graphDb) {
			System.out.println("Printing all nodes ...");
			try (Transaction tx = graphDb.beginTx();				
				 Result result = graphDb.execute("MATCH (n {type: 'node'}) RETURN n")) {
				
				System.out.println(result.resultAsString()); 

				tx.success();
			}	
		}
		
		public static void printAllEdge(GraphDatabaseService graphDb) {
			System.out.println("Printing all edges ...");
			try (Transaction tx = graphDb.beginTx();				
				 Result result = graphDb.execute("START n=node(*) MATCH (n)-[r]->(m) RETURN n,r,m;")) {
				
				System.out.println(result.resultAsString()); 

				tx.success();
			}	
		}
}