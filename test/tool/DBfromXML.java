package tool;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ldyy.data.Tree;
import com.ldyy.data.Xml;
import com.ldyy.tool.DBCtrl;
import com.ldyy.tool.FileUtil;

public class DBfromXML {
	private static String path = System.getProperty("user.dir") + "/example/";
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		String msg = FileUtil.readFileAll(path + "Action", "UTF-8");
		Tree<String> t = Xml.getTree(msg);
		System.out.println(t.branchSize());
//		Document dom = getDocument(msg);
//		Element root = dom.getRootElement();
//		List<Element> le = root.elements("dataitems");
//		Element datas = le.get(0);
//		List<Element> lds = datas.elements();
//		for (Element data : lds) {
//			List<Element> ld = data.elements();
//			Map<String, String> tmp = new HashMap<String, String>();
//			for (Element tpe : ld) {
//				String value = tpe.getTextTrim();
////				if (tpe.getName().equals("YXQ") && value.length() < 10)
////					value = value + "-01";
////				if (tpe.getName().equals("SCRQ") && value.length() < 10)
////					value = value + "-01";
//				tmp.put(tpe.getName(), value);
//			}
//			String sql = DBCtrl.insert("Medicine", tmp);
//			DBCtrl.submit(sql);
//		}
	}
}
