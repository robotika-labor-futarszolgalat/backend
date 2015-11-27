package hu.elte.ik.robotika.futar.vertx.backend.util.data;

public class tuple {
	private int first, second;
	
	tuple(int first, int second) {
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
