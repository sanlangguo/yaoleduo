package com.ldyy.tool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class Jxzh {
	private Map<String, Map<String, String>> root = new HashMap<String, Map<String, String>>();

	public Jxzh(String content) {
		recurseElm(content);
	}

	public String get(String key) {
		int num = key.lastIndexOf(".");
		if (num > -1)
			return root.get("&." + key.substring(0, num)).get(key.substring(num + 1));
		return root.get("&").get(key);
	}

	// public double getN(String key) {
	// int num = key.lastIndexOf(".");
	// if (num > -1)
	// return Double.parseDouble(root.get("&." + key.substring(0,
	// num)).get(key.substring(num + 1)));
	// return Double.parseDouble(root.get("&").get(key));
	// }

	public Map<String, String> getM(int key) {
		return getM("" + key);
	}

	public Map<String, String> getM(String key) {
		if (key.length() == 0)
			return root.get("&");
		return root.get("&." + key);
	}

	private void recurseElm(String str) {
		if (str == null)
			return;
		Map<String, String> map = null;
		int index0 = 0;
		int index2 = 0;
		boolean context = false;
		int arr = 0;
		boolean array = false;
		Deque<String> father = new ArrayDeque<String>();
		String present = "&";
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '"' && str.charAt(i - 1) != '\\') {
				context = !context;
			}
			if (context)
				continue;
			if (c == '[') {
				array = true;
				arr++;
			}
			if (c == ']') {
				arr--;
				if (arr == 0)
					array = false;
			}
			if (array)
				continue;

			if (c == '{') {
				String name = getLine(father) + present;
				if (map != null)
					map.put("#Map-" + present, name);
				map = new HashMap<String, String>();
				root.put(name, map);
				father.addLast(present);
				index0 = i + 1;
			} else if (c == ':') {
				String tag = str.substring(index0, i).trim();
				if (tag.startsWith("\"")) {
					tag = tag.substring(1, tag.length() - 1);
				}
				index2 = i + 1;
				present = tag;
			} else if (c == ',') {
				index0 = i + 1;
				String value = str.substring(index2, i);
				if (!value.contains("}")) {
					if (value.startsWith("\""))
						value = value.substring(1, value.length() - 1);
					map.put(present.trim(), value.trim().replace("\\", ""));
				}
			} else if (c == '}') {
				String value = str.substring(index2, i);
				if (!value.contains("}")) {
					if (value.startsWith("\""))
						value = value.substring(1, value.length() - 1);
					map.put(present.trim(), value.trim().replace("\\", ""));
				}
				father.removeLast();
				String name = getLine(father);
				if (name.length() > 0)
					map = root.get(name.substring(0, name.length() - 1));
			}
		}
	}

	private String getLine(Deque<String> father) {
		StringBuilder sb = new StringBuilder();
		for (String tmp : father) {
			sb.append(tmp).append(".");
		}
		return sb.toString();
	}

	public Jxzh() {
		root.put("&", new HashMap<String, String>());
	}

	public void put(String key, int value) {
		put(key, "" + value);
	}

	public void put(String key, String value) {
		// TODO 应适用于增加分支
		if (key.indexOf(".") > -1) {
			int n = key.lastIndexOf(".");
			Map<String, String> d = root.get("&." + key.substring(0, n));
			if (d == null) {
				put(key.substring(0, n), new HashMap<String, String>());
				d = root.get("&." + key.substring(0, n));
			}
			d.put(key.substring(n + 1), value);
		} else
			root.get("&").put(key, value);
	}

	public void put(String key, Map<String, String> map) {
		if (key.length() == 0)
			root.put("&", map);
		key = "&." + key;
		root.put(key, map);
		int n1 = key.length();
		int n2 = key.lastIndexOf(".", n1);
		while (n2 > -1) {
			map = root.get(key.substring(0, n2));
			if (map == null) {
				map = new HashMap<String, String>();
				root.put(key.substring(0, n2), map);
				map.put("#Map-" + key.substring(n2 + 1, n1), key.substring(0, n1));
			} else {
				map.put("#Map-" + key.substring(n2 + 1, n1), key.substring(0, n1));
				break;
			}
			n1 = n2;
			n2 = key.lastIndexOf(".", n1 - 1);
		}
	}

	public void putI(String key, Map<String, Integer> map) {
		Map<String, String> d = new HashMap<String, String>();
		for (Entry<String, Integer> tmp : map.entrySet())
			d.put(tmp.getKey(), tmp.getValue().toString());
		put(key, d);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		Map<String, String> map = root.get("&");
		if (map == null)
			return null;
		getM(sb, map);
		return sb.toString();
	}

	private void getM(StringBuilder sb, Map<String, String> map) {
		sb.append("{");
		boolean flag = false;
		for (Entry<String, String> tmp : map.entrySet()) {
			String key = tmp.getKey();
			if (key.startsWith("#Map-")) {
				sb.append("\"").append(key.substring(5)).append("\":");
				getM(sb, root.get(tmp.getValue()));
			} else {
				if (key.equals("#flag"))
					continue;
				if (tmp.getValue() != null) {
					if (tmp.getValue().startsWith("[") && tmp.getValue().endsWith("]"))
						sb.append("\"").append(key).append("\":").append(tmp.getValue());
					else
						sb.append("\"").append(key).append("\":\"").append(tmp.getValue()).append("\"");
				}
			}
			sb.append(",");
			flag = true;
		}
		if (flag)
			sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
	}

	public static void main(String[] args) {
		Jxzh j = new Jxzh();
		Map<String, String> map = j.getM("");
		System.out.println(j);
		map.put("a", "");
		System.out.println(j);
		
	}
}
