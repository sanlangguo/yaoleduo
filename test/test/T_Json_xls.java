package test;

import java.util.List;

import com.ldyy.tool.FileUtil;

public class T_Json_xls {
	public static void main(String[] args) {
		String path = System.getProperty("user.dir") + "/example/xls";
		List<String> l = FileUtil.readFileByLines(path);
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean f1 = true;// 是否第一行，代表第一个省无需结尾符号
		boolean f2 = false;// 是否换省后的第一个市，代表第一个市无需结尾符号
		boolean f3 = false;// 是否换市后的第一个区，代表第一个区无需结尾符号
		for (String tmp : l) {
			System.out.println(tmp);
			if (tmp.contains("老号段"))
				continue;
			String[] tmps = tmp.split("\t");
			if (tmps[0].endsWith("0000")) {
				if (f1)
					f1 = false;
				else
					sb.append("}},");
				f2 = true;
				sb.append("\"").append(tmps[0].substring(0, 2))
						.append("\":{\"name\":\"").append(tmps[1])
						.append("\",");
			} else if (tmps[0].endsWith("00")) {
				if (f2)
					f2 = false;
				else
					sb.append("},");
				f3 = true;
				if (!tmps[2].contains(tmps[1]))
					if (tmps[2].contains(tmps[1].substring(0, tmps[1].length() - 1)))
						tmps[1] = tmps[1].substring(0, tmps[1].length() - 1);
					else
						break;
				sb.append("\"").append(tmps[0].substring(2, 4))
						.append("\":{\"name\":\"")
						.append(tmps[2].substring(tmps[1].length()))
						.append("\",");
			} else {
				if (f3)
					f3 = false;
				else
					sb.append(",");
				if (!tmps[3].contains(tmps[2]))
					if (tmps[3].contains(tmps[2].substring(0, tmps[2].length() - 1)))
						tmps[2] = tmps[2].substring(0, tmps[2].length() - 1);
					else
						break;
				sb.append("\"").append(tmps[0].substring(4, 6))
						.append("\":\"")
						.append(tmps[3].substring(tmps[2].length()))
						.append("\"");
			}
		}
		sb.append("}}}");
		System.out.println(sb.toString());
	}
}
