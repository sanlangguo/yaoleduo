package old.data;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

public class Json {
	public static String toString(Tree<String> tree) {
		StringBuilder sb = new StringBuilder();
		changeMap(tree, sb);
		return sb.toString();
	}
	
	private static void changeMap(Tree<String> tree, StringBuilder sb) {
		sb.append("{");
		for (Tree<String> tmp : tree.listBranchs()) {
			sb.append("\"").append(tmp.getName()).append("\":");
			String value = tmp.getValue();
			if (value == null) {
				//!需要判断是否是List
				changeMap(tmp, sb);
				sb.append(",");
			} else {
				sb.append("\"").append(value).append("\",");
			}
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
	}
	
	private static void changeList(List<Tree<String>> ltree, StringBuilder sb) {
		
	}
	
	public static Tree<String> getTree(String str) {
		if (str == null)
			return null; 
		int index0 = 0;
		int index2 = 0;
		boolean context = false;
		boolean escape = false;
		Deque<String> path = new ArrayDeque<String>();
		String present = "root";
		Tree<String> tree = null;

		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"' && !escape)
				context = !context;
			if (c == '\\' && !escape)
				escape = true;
			else
				escape = false;
			if (context)
				continue;

			if (c == '{') {
				if (path.size() != 0 && path.getLast().startsWith("["))
					present = path.getLast().substring(1);
				path.add("{" + present);
				Tree<String> father = tree;
				tree = new Tree<String>(present);
				if (father != null) {
					father.addBranchLast(tree);
				}
				index0 = i + 1;
			} else if (c == ':') {
				String tag = str.substring(index0, i).trim();
				if (tag.startsWith("\"")) {
					tag = tag.substring(1, tag.length() - 1);
				}
				index2 = i + 1;
				present = tag.trim();
			} else if (c == ',') {
				index0 = i + 1;
				String value = str.substring(index2, i).trim();
				if (!value.endsWith("}") && !value.endsWith("]")) {
					if (value.startsWith("\""))
						value = value.substring(1, value.length() - 1).trim();
					if (path.getLast().startsWith("{")) {
						tree.put(present, value);
					} else if (path.getLast().startsWith("[")) {
						present = path.getLast().substring(1);
						Tree<String> tmp = new Tree<String>(present, value);
						tree.addBranchLast(tmp);
					}
				}
				if (path.getLast().startsWith("["))
					index2 = i + 1;
			} else if (c == '}') {
				String value = str.substring(index2, i).trim();
				if (!value.endsWith("}") && !value.endsWith("]")) {
					if (value.startsWith("\""))
						value = value.substring(1, value.length() - 1).trim();
					tree.put(present, value);
				}
				if (tree.getParent() == null)
					return tree;
				tree = tree.getParent();
				path.removeLast();
			} else if (c == '[') {
				if (path.size() != 0 && path.getLast().startsWith("["))
					present = path.getLast().substring(1);
				path.add("[" + present);
				index2 = i + 1;
			} else if (c == ']') {
				String value = str.substring(index2, i).trim();
				if (!value.endsWith("}") && !value.endsWith("]")) {
					if (value.startsWith("\""))
						value = value.substring(1, value.length() - 1).trim();
					present = path.getLast().substring(1);
					Tree<String> tmp = new Tree<String>(present, value);
					tree.addBranchLast(tmp);
				}
				path.removeLast();
			}
		}
		return null;
	}
}
