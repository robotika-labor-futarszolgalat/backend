package hu.elte.ik.robotika.futar.vertx.backend.util.data;

import java.util.ArrayList;

	public class printer {
	
		public static void printGrid(int4[][] grid) {
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
		
		public static void printInt4Array(ArrayList<int4> list) {		
			for(int i=0; i<list.size(); ++i) {
				list.get(i).printAll();
				System.out.println("");
			}
			System.out.println("");
		}
		
		public static void printTuple(tuple t) {
			System.out.println("(" + t.getFirst() + "," + t.getSecond() + ")");
		}
}
