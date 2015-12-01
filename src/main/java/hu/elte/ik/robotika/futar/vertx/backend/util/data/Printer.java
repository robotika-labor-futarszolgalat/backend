package hu.elte.ik.robotika.futar.vertx.backend.util.data;

import java.util.ArrayList;

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
}
