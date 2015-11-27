package hu.elte.ik.robotika.futar.vertx.backend.util.data;

public class int4 {
	
	private int a,b,c,d;
	
	int4(int a, int b, int c, int d) {
		this.a = a;
		this.b = b;
		this.c = c;
		this.d = d;
	}
	
	public int get(int n) {
		int result = -1;
		switch(n) {
			case 1:
				result = a;
				break;
			case 2:
				result = b;
				break;
			case 3:
				result = c;
				break;
			case 4:
				result = d;
				break;
		}
		return result;
	}
	
	public int4 getAll() {
		return (new int4(a,b,c,d));
	}
	
	public void printAll() {
		System.out.print("(" + a + ", " + b + ", " + c + ", " + d + ")");
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(getClass() != obj.getClass()) return false;
		
		int4 other = (int4) obj;
		if(this.a == other.a && this.b == other.b && this.c == other.c && this.d == other.d) return true;
		return false;
	}
}















