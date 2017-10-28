package test;

import java.util.ArrayList;
import java.util.List;

public class T_List {
	public static void main(String[] args) {
		List<String> a = new ArrayList<String>();
		a.add("a");
		a.add("b");
		a.add("c");
		for (String tmp : a) {
			
			if (tmp.equals("a")) {
				a.remove(tmp);
//				break;
			}
			System.out.println(tmp);
		}
	}
}
