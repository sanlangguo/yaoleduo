package test;

import java.util.HashSet;

public class T_Super<T> extends HashSet<T> {
	private static final long serialVersionUID = 1L;
	
	public boolean add(T a) {
		System.out.println(a);
		// return this.add(a);
		return super.add(a);
	}
	
	public static void main(String[] args) {
		T_Super<String> t = new T_Super<String>();
		t.add("test");
		System.out.println(t.size());
		
		new C().t();
	}
	
	public static class A {
		public void t() {
			throw new RuntimeException();
		}
	}
	
	public static class B extends A{
		public void t() {
			super.t();
		}
	}
	
	public static class C extends B{
		public void t() {
			super.t();
		}
	}
}
