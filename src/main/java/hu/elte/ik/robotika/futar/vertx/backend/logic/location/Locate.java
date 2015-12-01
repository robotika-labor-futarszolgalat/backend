package hu.elte.ik.robotika.futar.vertx.backend.logic.location;

import hu.elte.ik.robotika.futar.vertx.backend.util.data.IntN;
import hu.elte.ik.robotika.futar.vertx.backend.util.data.Printer;

import java.util.Random;
import java.util.ArrayList;

public class Locate {
	
	private static int min, max; //measurable min and max dB
	private static int gridSizeVer, gridSizeHor; // vertical and horizontal size of the map
	private static int error; //error in measuring dB, maybe depends on the distance, possibly will be a function
	private static IntN[][] grid; //map
	private static IntN robot; //recieved coordinates from the robot
	
	public static void init() {		
		min = 0;
		max = 100;
		gridSizeVer = 5;
		gridSizeHor = 10;
		error = 10;
		
		initGrid();
	}
	
	public static void initGrid() {//not perfect, but ok for testing
		grid = new IntN[gridSizeVer][gridSizeHor];
		double diag = pithag(gridSizeVer,gridSizeHor);
		double rate = ((max-min) / (diag)) + 1;
		
		for(int i=0; i<gridSizeVer; ++i) {
			for(int j=0; j<gridSizeHor; ++j) {
				grid[i][j] = new IntN((int)(rate*pithag(gridSizeVer-i-1,gridSizeHor-j-1)) + 1, (int)(rate*pithag(gridSizeVer-i-1,j)) + 1, (int)(rate*pithag(i,(gridSizeHor-j)-1)) + 1, (int)(rate*(pithag(i,j))) + 1);
			}
		}
	}
	
	public static double pithag(int i, int j) {
		return Math.sqrt(i*i + j*j);
	}
	
	public static IntN getRobotCoords() {//generates a random input
			return new IntN(getRandomInt(),getRandomInt(),getRandomInt(),getRandomInt());
		}	
		
	public static int getRandomInt() {
		Random rand = new Random();
		
		return (rand.nextInt((max - min) + 1) + min);
	}
	
	public static ArrayList<IntN> search(int n, ArrayList<IntN> alreadyFound) {
		ArrayList<IntN> result = new ArrayList<IntN>();
		
		if(alreadyFound == null) {
			for(int i=0; i<gridSizeVer; ++i) {
				for(int j=0; j<gridSizeHor; ++j) {
					if(Math.abs(grid[i][j].get(n-1) - robot.get(n-1)) < error) {
						result.add(grid[i][j]);
					}					
				}
			}	
		}
		else {
			for(int i=0; i<alreadyFound.size(); ++i) {
				if(Math.abs(alreadyFound.get(i).get(n-1) - robot.get(n-1)) < error) {
					result.add(alreadyFound.get(i));
				}
			}
		}
		return result;
	}
	
	public static IntN findRealCoords(IntN coords) {
		for(int i=0; i<gridSizeVer; ++i) {
			for(int j=0; j<gridSizeHor; ++j) {
				if(coords == grid[i][j]) return new IntN(i,j);					
			}
		}	
		return new IntN(-1,-1);
	}
	
	public static IntN closestNode(ArrayList<IntN> possiblePlaces) {
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
	
	public static int distance(IntN first, IntN second) {
		int result = 0;
		for(int i=1; i<5; ++i) {
			result += Math.abs(first.get(i-1) - second.get(i-1));
		}
		return result;
	}
	
	public static void main(String[] args) {
		
		init();
		printer.printGrid(grid);
		//robot = getRobotCoords();	random values are not valid most of the times
		robot = new IntN(30,65,40,63);	// 2 possibility, must decide which is closer
		//robot = new IntN(39,65,37,74);	//	only one possibility
		
		System.out.print("The coords of the robot: ");
		robot.printAll();		
		System.out.println("");
		
		ArrayList<IntN> possiblePlaces = null;
		System.out.println("");
		for(int i=1; i<5; ++i) {
			possiblePlaces = search(i,possiblePlaces);
			System.out.println(i + ". search resulst: ");
			printer.printIntNArray(possiblePlaces);	
		}
		System.out.print("The robot is possibly at: ");
		if(possiblePlaces.size() == 1) {
			findRealCoords(possiblePlaces.get(0)).printAll();
		}
		else if(possiblePlaces.size() > 1){
			findRealCoords(closestNode(possiblePlaces)).printAll();
		}
		else {
			System.out.println("????");
		}
		
	}
}