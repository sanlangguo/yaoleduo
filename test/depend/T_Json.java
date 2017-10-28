package depend;

import com.ldyy.data.Json;
import com.ldyy.data.Tree;


public class T_Json {
	public static void main(String[] args) throws Throwable {
		String json = "{16:tt,82:tt,88:tt,95:tt,96:tt,205:tt,255:tt,258:tt,262:tt,723:tt,1178:tt,13675:tt,19122:tt,19124:tt,19127:tt,21465:tt,36655:tt,96667:tt,98908:tt,98911:tt}";
		Tree<String> tree = Json.getTree(json);
		System.out.println(tree);
		System.out.println(tree.branchSize());
	}
}
