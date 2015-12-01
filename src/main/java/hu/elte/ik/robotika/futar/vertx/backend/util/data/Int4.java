package hu.elte.ik.robotika.futar.vertx.backend.util.data;

public class IntN {
		
	protected int data[];
	
	public IntN(int... data) {
		this.data = new int[data.length];
		
		int i = 0;
		for(int d: data) {
			this.data[i] = d;
			i++;
		}
	}
	
	public IntN getAll() {
		return this;
	}

	public void printAll() {
		System.out.print("(" + data[0]);
		for(int i=1; i<data.length; ++i) {
			System.out.print(", " + data[i]);	
		}
		System.out.print(")\n");
	}

	public void set(int i, int n) {
		data[i] = n;
	}
	
	public void setAll(int[] data) {
		this.data = data;
	}
	
	public int get(int i) {
		return data[i];
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		
		IntN other = (IntN) obj;
		if(this.data == other.data) return true;
		return false;
	}
}