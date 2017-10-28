package tool;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import com.ldyy.data.Json;
import com.ldyy.data.Tree;
import com.ldyy.data.Xml;

public class Medicine {
	public static void main(String[] args) {
		File file = new File("E://medicine");
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			int i = 0;
//			while (reader.readLine() != null) {
				String str = reader.readLine();
				Tree<String> t = Json.getTree(str);
				System.out.println(Xml.toString(t));
				i++;
//			}
			System.out.println(i);
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
	}
}
