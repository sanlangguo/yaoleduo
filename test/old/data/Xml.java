package old.data;

import java.io.ByteArrayInputStream;
import java.util.Map.Entry;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Xml {
	public static String toString(Tree<String> tree) {
		StringBuilder sb = new StringBuilder();
		sb.append("<").append(tree.name);
		for (Entry<String, String> tmp : tree.getParas().entrySet()) {
			sb.append(" ").append(tmp.getKey()).append("=\"").append(tmp.getValue()).append("\"");
		}
		sb.append(">");
		if (tree.value != null) {
			sb.append(tree.value);
		} else {
			for (Tree<String> tmp : tree.branchs) {
				sb.append(toString(tmp));
			}
		}
		sb.append("</").append(tree.name).append(">");
		return sb.toString();
	}
	
	public static Tree<String> getTree(String XMLstr) {
		return recurseElm(parseXML(XMLstr).getDocumentElement());
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
	
	private static Tree<String> recurseElm(Node node) {
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
				String name = branch.getName();
				if (branch.paraSize() == 0 && branch.branchSize() == 0) {
					if (tree.containsKey(name)) {
						tree.removeLeafOnly(name);
						tree.addBranchLast(branch);
					} else
						tree.put(name, branch.getValue());
				} else
					tree.addBranchLast(branch);
			} else if (tmp.getNodeType() == Node.TEXT_NODE) {
				if (tmp.getNodeValue().trim().length() == 0)
					continue;
				tree.setValue(tmp.getNodeValue().trim());
			}
		}
		return tree;
	}
}
