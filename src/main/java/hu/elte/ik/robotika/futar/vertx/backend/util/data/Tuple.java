package hu.elte.ik.robotika.futar.vertx.backend.util.data;

public class Tuple {
	private int first, second;
	
	public Tuple(int first, int second) {
		this.first = first;
		this.second = second;
	}

	public void setAll(int first, int second) {
		this.first = first;
		this.second = second;
	}
	
	public int getFirst() {
		return first;
	}

	public int getSecond() {
		return second;
	}	
}
