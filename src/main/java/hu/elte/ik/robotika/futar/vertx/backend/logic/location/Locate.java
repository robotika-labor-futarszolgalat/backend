package hu.elte.ik.robotika.futar.vertx.backend.logic.location;

import hu.elte.ik.robotika.futar.vertx.backend.util.data.Int4;
import hu.elte.ik.robotika.futar.vertx.backend.util.data.Tuple;
import hu.elte.ik.robotika.futar.vertx.backend.util.data.Printer;

import java.util.Random;
import java.util.ArrayList;

public class Locate {
	
	private static int min, max; //measurable min and max dB
	private static int gridSizeVer, gridSizeHor; // vertical and horizontal size of the map
	private static int error; //error in measuring dB, maybe depends on the distance, possibly will be a function
	private static Int4[][] grid; //map
	private static Int4 robot; //recieved coordinates from the robot
	
	public static void init() {		
		min = 0;
		max = 100;
		gridSizeVer = 5;
		gridSizeHor = 10;
		error = 10;
		
		initGrid();
	}
	
	public static void initGrid() {//not perfect, but ok for testing
		grid = new Int4[gridSizeVer][gridSizeHor];
		double diag = pithag(gridSizeVer,gridSizeHor);
		double rate = ((max-min) / (diag)) + 1;
		
		for(int i=0; i<gridSizeVer; ++i) {
			for(int j=0; j<gridSizeHor; ++j) {
				grid[i][j] = new Int4((int)(rate*pithag(gridSizeVer-i-1,gridSizeHor-j-1)) + 1, (int)(rate*pithag(gridSizeVer-i-1,j)) + 1, (int)(rate*pithag(i,(gridSizeHor-j)-1)) + 1, (int)(rate*(pithag(i,j))) + 1);
			}
		}
	}
	
	public static double pithag(int i, int j) {
		return Math.sqrt(i*i + j*j);
	}
	
	public static Int4 getRobotCoords() {//generates a random input
			return new Int4(getRandomInt(),getRandomInt(),getRandomInt(),getRandomInt());
		}	
		
	public static int getRandomInt() {
		Random rand = new Random();
		
		return (rand.nextInt((max - min) + 1) + min);
	}
	
	public static ArrayList<Int4> search(int n, ArrayList<Int4> alreadyFound) {
		ArrayList<Int4> result = new ArrayList<Int4>();
		
		if(alreadyFound == null) {
			for(int i=0; i<gridSizeVer; ++i) {
				for(int j=0; j<gridSizeHor; ++j) {
					if(Math.abs(grid[i][j].get(n) - robot.get(n)) < error) {
						result.add(grid[i][j]);
					}
						
				}
			}	
		}
		else {
			for(int i=0; i<alreadyFound.size(); ++i) {
				if(Math.abs(alreadyFound.get(i).get(n) - robot.get(n)) < error) {
					result.add(alreadyFound.get(i));
				}
			}
		}
		return result;
	}
	
	public static Tuple findRealCoords(Int4 coords) {
		for(int i=0; i<gridSizeVer; ++i) {
			for(int j=0; j<gridSizeHor; ++j) {
				if(coords == grid[i][j]) return new Tuple(i,j);
			}
		}	
		return new Tuple(-1,-1);
	}
	
	public static Int4 closestNode(ArrayList<Int4> possiblePlaces) {
		int minValue = distance(robot, possiblePlaces.get(0));
		int minPlace = 0;
		
		for(int i=1; i<possiblePlaces.size(); ++i) {
			int temp = distance(robot, possiblePlaces.get(i)); 
			if(temp < minValue) {
				minValue = temp;
				minPlace = i;
			}
		}
		return possiblePlaces.get(minPlace);
	}
	
	public static int distance(Int4 first, Int4 second) {
		int result = 0;
		for(int i=1; i<5; ++i) {
			result += Math.abs(first.get(i) - second.get(i));
		}
		return result;
	}
	
	public static void main(String[] args) {
		
		init();
		Printer.printGrid(grid);
		//robot = getRobotCoords();	random values are not valid most of the times
		//robot = new Int4(30,65,40,63); 	//	2 possibility, must decide which is closer
		robot = new Int4(39,65,37,74);	//	only one possibility
		
		System.out.print("The coords of the robot: ");
		robot.printAll();		
		System.out.println("");
		
		ArrayList<Int4> possiblePlaces = null;
		System.out.println("");
		for(int i=1; i<5; ++i) {
			possiblePlaces = search(i,possiblePlaces);
			System.out.println(i + ". search resulst: ");
			Printer.printInt4Array(possiblePlaces);
		}
		System.out.print("The robot is possibly at: ");
		if(possiblePlaces.size() == 1) {
			Printer.printTuple(findRealCoords(possiblePlaces.get(0)));
		}
		else if(possiblePlaces.size() > 1){
			Printer.printTuple(findRealCoords(closestNode(possiblePlaces)));
		}
		else {
			System.out.println("????");
		}
		
	}

}
