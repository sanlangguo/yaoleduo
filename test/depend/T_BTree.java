package depend;

import com.ldyy.data.BTree;

public class T_BTree {
	public static void main(String[] args) {
		BTree<String> bt = new BTree<String>();
		System.out.println(bt.findNode("a"));
	}
}
