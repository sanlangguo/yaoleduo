package test;

import java.util.ArrayList;
import java.util.List;

import com.ldyy.tool.FileUtil;

public class T_3l {
	public static void main(String[] args) {
		String path = System.getProperty("user.dir") + "/example/3l";
		List<String> l = FileUtil.readFileByLines(path);
		List<String[]> sheng = new ArrayList<String[]>();
		List<String[]> shi = new ArrayList<String[]>();
		List<String[]> qu = new ArrayList<String[]>();
		for (String tmp : l) {
			String[] tmps = tmp.split(",");
			String id = tmps[1].trim();
			id = id.substring(1, 7);
			String name = tmps[2].trim();
			name = name.substring(1, name.length() - 1);
			if (id.endsWith("0000")) {
				sheng.add(new String[] { id, name });
			} else if (id.endsWith("00")) {
				shi.add(new String[] { id, name });
			} else {
				qu.add(new String[] { id, name });
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		int i1 = 0, i2 = 0;
		for (String[] tmp : sheng) {
			String id1 = tmp[0].substring(0, 2);
			sb.append("\"").append(id1)
					.append("\":{\"name\":\"").append(tmp[1]).append("\",");
			System.out.println(tmp[0]);
			for (;;i1++) {
				if (i1 == shi.size())
					break;
				tmp = shi.get(i1);
				System.out.println("\t" + tmp[0]);
				if (!tmp[0].startsWith(id1))
					break;
				sb.append("\"").append(tmp[0].substring(2, 4))
						.append("\":{\"name\":\"").append(tmp[1]).append("\",");
				String id2 = tmp[0].substring(0, 4);
				for (;;i2++) {
					if (i2 == qu.size())
						break;
					tmp = qu.get(i2);
					System.out.println("\t\t" + tmp[0]);
					if (!tmp[0].startsWith(id2))
						break;
					sb.append("\"").append(tmp[0].substring(4, 6))
							.append("\":\"").append(tmp[1]).append("\",");
				}
				sb.deleteCharAt(sb.length() - 1);
				sb.append("},");
			}
			sb.deleteCharAt(sb.length() - 1);
			sb.append("},");
		}
		sb.deleteCharAt(sb.length() - 1);
		sb.append("}");
		System.out.println(sb.toString());
	}
}
