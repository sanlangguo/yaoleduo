package old.data;

import com.ldyy.tool.FileUtil;

public class T_Json {
	public static void main(String[] args) throws Throwable {
		String json = FileUtil.readFileAll(System.getProperty("user.dir") + "/example/Tree.json", "UTF-8");
		Tree<String> tree = Json.getTree(json);
		System.out.println(tree);
	}
}
