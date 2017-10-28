package old.data;

import com.ldyy.tool.FileUtil;

public class T_Xml {
	public static void main(String[] args) {
		String xml = FileUtil.readFileAll(System.getProperty("user.dir") + "/example/Tree.xml", "UTF-8");
		Tree<String> t = Xml.getTree(xml);
		System.out.println(t);
	}
}
