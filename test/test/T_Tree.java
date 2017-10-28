package test;

import com.ldyy.data.Json;
import com.ldyy.data.Tree;

public class T_Tree {
	public static void main(String[] args) {
		Tree<String> result = Json
				.getTree("{ID:00000000000009_01_01,status:1,ret:0,userType:21,Sys_dataMethod0:update,Sys_dataBase0:indent,Sys_dataKey0:ID,Sys_dataCol0:status}");
		System.out.println(result);
		for (Tree<String> tmp : result.listBranchs()) {
			System.out.println(result.removeBranch(tmp));
			System.out.println(result + " ~ " + tmp.getName());
		}
	}
}
