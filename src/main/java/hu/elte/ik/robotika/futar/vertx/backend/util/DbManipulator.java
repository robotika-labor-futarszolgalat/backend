package hu.elte.ik.robotika.futar.vertx.backend.util;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Map;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Result;
import org.neo4j.graphdb.Transaction;

public class DbManipulator {
	
	private GraphDatabaseService graphDb;
	private DataReader reader;
	
	public DbManipulator(GraphDatabaseService graphDb) {
		this.graphDb = graphDb;
		reader = new DataReader();
	}
	
	public void readNodes(String file) {
		try {
			reader.readNodes(file);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	public void readEdges(String file) {
		try {
			reader.readEdges(file);
		} catch(FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	private enum RelTypes implements RelationshipType {
		KNOWS
	}
	
	//TODO
	//throws exception
	//valtozora h kell szurni?
	public void findPerson(String realName) {
		try (Transaction tx = graphDb.beginTx();	
			 Result result = graphDb.execute("MATCH (n) "
			 + "WHERE n.type = 'user' AND n.name = {realName} RETURN n")) {
			 
			System.out.println(result.resultAsString());
			
			tx.success();
		}	
	}
	
	//TODO
	public ArrayList<IntN> findShortestPath(IntN start, IntN end) {
		ArrayList<IntN> result = new ArrayList<IntN>();
		
		try (Transaction tx = graphDb.beginTx()) {
			Result s = graphDb.execute( "MATCH (n) "
			+ "WHERE n.type = 'node' AND n.posX = 0 AND n.posY = 0 RETURN n" ) ;
			
			Result e = graphDb.execute( "MATCH (n) "
			+ "WHERE n.type = 'node' AND n.posX = 50 AND n.posY = 50 RETURN n" ) ;
			
			Map<String, Object> rowS = s.next();
		    Node startNode = (Node) rowS.values().toArray()[0];
		       
		    Map<String, Object> rowE = e.next();
		    Node endNode = (Node) rowE.values().toArray()[0];
		
			PathFinder<WeightedPath> finder = GraphAlgoFactory.dijkstra(PathExpanders.forTypeAndDirection( RelTypes.KNOWS,  Direction.BOTH), "cost");
			
			WeightedPath path = finder.findSinglePath(endNode, startNode);
			System.out.println(path);
			
			String[] splitted = path.toString().split(">");
			
			for(int i=splitted.length-1; i>=0; --i) {	
				result.add(findNodeById((int) splitted[i].charAt(1)));
			}
					System.out.println(result);
			
			
			tx.success();
		}
		
		return result;
	}
	
	//TODO
	public IntN findNodeById(int id) {
		Node node;
		
		try (Transaction tx = graphDb.beginTx()) {
			Result result = graphDb.execute( "MATCH (n) "
			+ "WHERE n.type = 'node' AND n.id = id RETURN n" ) ;
			
			Map<String, Object> row = result.next();
		    node = (Node) row.values().toArray()[0];
		    			
			tx.success();
		} 
		
		return new IntN((int) node.getProperty("posX"), (int) node.getProperty("posY"));
	}
	
	public void insertNode(IntN coords, IntN bt) {
		try (Transaction tx = graphDb.beginTx()) {
			
			Node node = graphDb.createNode();
			node.setProperty("type", "node");
			node.setProperty("posX",coords.get(0));
			node.setProperty("posY",coords.get(1));
			node.setProperty("first",bt.get(0));
			node.setProperty("second",bt.get(1));
			node.setProperty("third",bt.get(2));
			node.setProperty("fourth",bt.get(3));
			
			tx.success();
		}
	}
	
	public void insertMapNodes() {
		try (Transaction tx = graphDb.beginTx()) {
			
			for(int i=0; i<reader.getNumberOfNodes(); ++i) {
				Node node = graphDb.createNode();
				node.setProperty("type", "node");
				node.setProperty("posX",reader.getRealCoords().get(i).get(0));
				node.setProperty("posY",reader.getRealCoords().get(i).get(1));
				node.setProperty("first",reader.getBt().get(i).get(0));
				node.setProperty("second",reader.getBt().get(i).get(1));
				node.setProperty("third",reader.getBt().get(i).get(2));
				node.setProperty("fourth",reader.getBt().get(i).get(3));
			}
			
			tx.success();
		}
	}
	
	public void insertMapEdges() {		
		try (Transaction tx = graphDb.beginTx();	
			 Result result = graphDb.execute("MATCH (n {type: 'node'}) RETURN n")) {
			
			ArrayList<Node> nodes = new ArrayList<Node>();
			while(result.hasNext()) {
		         Map<String, Object> row = result.next();
		         nodes.add((Node) row.values().toArray()[0]);
		    }
			
			for(int i=0; i<reader.getNumberOfEdges(); ++i) {
				Relationship rel = nodes.get(reader.getEdges().get(i).get(0)).createRelationshipTo(nodes.get(reader.getEdges().get(i).get(1)), RelTypes.KNOWS);
				rel.setProperty("cost", reader.getEdges().get(i).get(2));
				Relationship rel2 = nodes.get(reader.getEdges().get(i).get(1)).createRelationshipTo(nodes.get(reader.getEdges().get(i).get(0)), RelTypes.KNOWS);
				rel2.setProperty("cost", reader.getEdges().get(i).get(2));
			}
			
			tx.success();
		}
	}
	
	public void insertEdge(IntN edge) {
		//TODO
	}
	
	public void insertTestUsers() {
		try (Transaction tx = graphDb.beginTx()) {
		
			for(int i=0; i<3; ++i) {
				Node testUser = graphDb.createNode();
				testUser.setProperty("type", "user");
				testUser.setProperty("name", "test_user_" + i);
				testUser.setProperty("username", "test_user_" + i);
				testUser.setProperty("pwd", "test_user_" + i);
				testUser.setProperty("room", i);
			}
			
			tx.success();
		}
	}
	
	public void insertUser(String name, String username, String pwd, int room) {
		try (Transaction tx = graphDb.beginTx()) {
		
			Node user = graphDb.createNode();
			user.setProperty("type", "user");
			user.setProperty("name", name);
			user.setProperty("username", username);
			user.setProperty("pwd", pwd);
			user.setProperty("room", room);
		
			tx.success();
		}
	}
	
	public void deleteEdge(int posX, int posY) {
		//TODO
	}
	
	public void deleteNode(int posX, int posY) {
		//TODO
	}
	
	public void deleteuser(String username) {
		//TODO
	}
	
	public void deleteAllEdge() {
		try (Transaction tx = graphDb.beginTx();	
			 Result result = graphDb.execute("START r=relationship(*) DELETE r")) {
			 
			tx.success();
		}
	}
	
	public void deleteAllNode() {
		try (Transaction tx = graphDb.beginTx();	
			 Result result = graphDb.execute("MATCH (n {type: 'node'}) DELETE n")) {
			
			tx.success();
		}
	}
	
	public void deleteAlluser() {
		try (Transaction tx = graphDb.beginTx();	
			 Result result = graphDb.execute("MATCH (n {type: 'user'}) DELETE n")) {
			
			tx.success();
		}
	}
	
}
