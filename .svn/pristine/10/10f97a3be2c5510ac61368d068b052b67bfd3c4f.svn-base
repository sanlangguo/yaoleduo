package com.ldyy.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

public abstract class Config {
	private static Map<String, String> cfg = new HashMap<String, String>();
	private static Map<String, Map<String, String>> msg = new HashMap<String, Map<String, String>>();

//	static {
//		init();
//	}

	public static void init() {
		String path = Thread.currentThread().getContextClassLoader().getResource("").toString();
		System.out.println(path);
		path = path.substring(6, path.indexOf("WEB-INF"));
		PropertyConfigurator.configure(path + "cfg/log/log4j.properties");
		init(path + "cfg/parameter/parameter.properties");
		getCfg(path + "cfg/msg");
		cfg.put("imgPath", path + "/App/img/");
	}

	public static void init(String path) {
		getParameter(path);
	}

	public static String get(String key) {
		return cfg.get(key);
	}

	public static Integer geti(String key) {
		return Integer.parseInt(cfg.get(key));
	}
	
	public static Map<String, String> getM(String key) {
		return msg.get(key);
	}

	private static void getParameter(String path) {
		Properties prop = new Properties();
		InputStream inStream = null;

		try {
			inStream = new FileInputStream(path);
			prop.load(inStream);

			for (Entry<Object, Object> entry : prop.entrySet()) {
				cfg.put((String) entry.getKey(), (String) entry.getValue());
			}
		} catch (IOException e) {
			throw new RuntimeException("配置读取失败");
		} finally {
			prop.clear();
			try {
				inStream.close();
			} catch (IOException e) {
				e.printStackTrace();
				if (inStream != null)
					inStream = null;
			}
		}
	}
	
	private static void getCfg(String path) {
		System.out.println(path);
		List<String> fileNames = FileUtil.fileSelect(path, "");
		for (String fileName : fileNames) {
			//System.out.println(fileName);
			List<String> lines = FileUtil.readFileByLines(fileName);
			if (lines.size() == 0)
				continue;
			lines.remove(0);
			Map<String, String> cfg = new HashMap<String, String>();
			for (String line : lines) {
				String[] strs = line.split("\t{1,}");
				cfg.put(strs[0], strs[1]);
			}
			msg.put(fileName.substring(fileName.lastIndexOf(File.separator) + 1), cfg);
			if (!fileName.endsWith("_")) {
				System.out.print(fileName.substring(fileName.lastIndexOf(File.separator) + 1));
				System.out.print(",");
			}
		}
		
		Map<String, String> answer = msg.get("answer_");
		for (Entry<String, Map<String, String>> tmp : msg.entrySet()) {
			if (tmp.getKey().equals("answer_"))
				continue;
			if (tmp.getKey().endsWith("_"))
				tmp.getValue().putAll(answer);
		}
	}
}
