package hu.elte.ik.robotika.futar.vertx.backend.util;

import hu.elte.ik.robotika.futar.vertx.backend.util.data.IntN;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class DataReader {
	private int numberOfNodes;//number of nodes
	private ArrayList<IntN> bt = new ArrayList<IntN>();//numberOfNodes quartets
	private ArrayList<IntN> realCoords = new ArrayList<IntN>();//numberOfNodes tuples
	
	private int numberOfEdges;//number of edges
	private ArrayList<IntN> edges = new ArrayList<IntN>();//numberOfEdges triplets
	
	public void readNodes(String file) throws FileNotFoundException
	{
		Scanner scanner = new Scanner(new File(file));
		numberOfNodes = scanner.nextInt();

		for (int j = 0; j < numberOfNodes; j++) {
			IntN btData = new IntN(new int[4]);
			IntN rcData = new IntN(new int[2]);
			
			for (int i = 0; i < 4; ++i)
			{
				btData.set(i, scanner.nextInt());
			}
			for (int i = 0; i < 2; ++i) {
				rcData.set(i, scanner.nextInt());
			}
			
			bt.add(btData);
			realCoords.add(rcData);
		}
		scanner.close();
		
//		printer.printIntNArray(bt);
//		printer.printIntNArray(realCoords);
	}
	
	public void readEdges(String file) throws FileNotFoundException {
		//read edges
		Scanner scanner = new Scanner(new File(file));
		numberOfEdges = scanner.nextInt();
		
		for (int j = 0; j < numberOfEdges; j++) {
			IntN eData = new IntN(new int[3]);
			
			for (int i = 0; i < 3; ++i)
			{
				eData.set(i, scanner.nextInt());
			}
			edges.add(eData);
		}
		scanner.close();
		
//		printer.printIntNArray(edges);
	}

	public int getNumberOfNodes() {
		return numberOfNodes;
	}

	public ArrayList<IntN> getBt() {
		return bt;
	}

	public ArrayList<IntN> getRealCoords() {
		return realCoords;
	}

	public int getNumberOfEdges() {
		return numberOfEdges;
	}

	public ArrayList<IntN> getEdges() {
		return edges;
	}

}
