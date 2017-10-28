package com.ldyy.tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public abstract class FileUtil {
	private static Logger log = Logger.getLogger(FileUtil.class);
	
	public static List<String> fileSelect(String filePath, String expandName) {
		File file = new File(filePath);
		List<String> fileList = new ArrayList<String>();
		File[] subFile = file.listFiles();
		for (File f : subFile) {
			if (!f.isDirectory()) {
				String fileName = f.getName();
				if (fileName.endsWith(expandName)) {
					fileList.add(f.getAbsolutePath());
				}
			}
		}
		return fileList;
	}

	public static List<String> readFileByLines(String fileName) {
		List<String> list = new ArrayList<String>();
		File file = new File(fileName);
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				if (tempString.length() > 0) {
					list.add(tempString);
				}
			}
			reader.close();
		} catch (IOException e) {
			log.error("", e);
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return list;
	}
	
	public static String readFileAll(String fileName, String charset) {
		String msg = null;
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(fileName);
			int len = fis.available();
			byte[] b = new byte[len];
			fis.read(b);
			msg = new String(b, charset);
		} catch (Exception e) {
			log.error("", e);
		} finally {
			try {
				fis.close();
			} catch (Exception e) {
			}
		}
		return msg;
	}
	
	public static byte[] readFileBytes(String fileName) {
		FileInputStream fis = null;
		byte[] b = null;
		try {
			fis = new FileInputStream(fileName);
			int len = fis.available();
			b = new byte[len];
			fis.read(b);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fis != null) {
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
		return b;
	}
	
	public static boolean writeFileAll(String fileName, String context) {
		return writeFileAll(fileName, context, "UTF-8");
	}
	
	public static boolean writeFileAll(String fileName, String context, String charSet) {
		try {
			byte[] b = context.getBytes(charSet);
			return writeFileAll(fileName, b, 0, b.length);
		} catch (UnsupportedEncodingException e) {
			log.error("", e);
		}
		return false;
	}
	
	public static boolean writeFileAll(String fileName, byte[] b, int off, int len) {
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(fileName);
			fos.write(b, off, len);
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			log.error("", e);
		}
		return false;
	}
}
