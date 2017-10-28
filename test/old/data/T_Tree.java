package old.data;


public class T_Tree {
	public static void main(String[] args) {
		Tree<String> t = new Tree<String>("a");
		t.put("b", "18");
		t.put("c", "28");
		System.out.println(t);
		
		t.remove("c");
		System.out.println(t);
		
		Tree<String> t1 = new Tree<String>("b");
		t1.put("t", "111");
		t.addBranchLast(t1);
		System.out.println(t);
	}
}
