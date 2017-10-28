package test;

import java.io.ByteArrayInputStream;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.ldyy.data.Tree;
import com.ldyy.tool.FileUtil;

public class T_w3cDom {
	private static String path = System.getProperty("user.dir") + "/example/";
	
	public static void main(String[] args) throws Throwable {
		String msg = FileUtil.readFileAll(path + "Action", "UTF-8");
		Document dom = parseXML(msg);
		Thread.sleep(10 * 1000);
		Node node = dom.getDocumentElement();
		recurseElm(node);
	}
	
	private static Document parseXML(String XMLstr) {
		try {
			ByteArrayInputStream bais = new ByteArrayInputStream(
					XMLstr.getBytes("UTF-8"));
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document dom = db.parse(bais);
			return dom;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	static AtomicInteger ai = new AtomicInteger();
	
	private static Tree<String> recurseElm(Node node) {
		if (node.getNodeName().equals("item")) {
			System.out.println(ai.incrementAndGet());
		}
		Tree<String> tree = new Tree<String>(node.getNodeName());
		NamedNodeMap nnm = node.getAttributes();
		int len = nnm.getLength();
		for (int i = 0; i < len; i++) {
			Node tmp = nnm.item(i);
			tree.putPara(tmp.getNodeName(), tmp.getNodeValue());
		}
		NodeList nl = node.getChildNodes();
		len = nl.getLength();
		for (int i = 0; i < len; i++) {
			Node tmp = nl.item(i);
			if (tmp.getNodeType() == Node.ELEMENT_NODE) {
				Tree<String> branch = recurseElm(tmp);
				tree.addBranch(branch);
			} else if (tmp.getNodeType() == Node.TEXT_NODE) {
				if (tmp.getNodeValue().trim().length() == 0)
					continue;
				tree.setValue(tmp.getNodeValue().trim());
			}
		}
		return tree;
	}
}
