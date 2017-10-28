package test;

import java.util.Map;

import com.ldyy.tool.Config;
import com.ldyy.tool.DBCon;
import com.ldyy.tool.DBCtrl;

public class T_mysql {
	public static void main(String[] args) {
		Config.init();
		DBCon.init();
		String sql = "update `customer` set level='-3' where ID=10000027";
		DBCtrl.submit(sql);
		for (int i = 0; i < 5; i++) {
			Map<String, String> map = DBCtrl.getSelect("select level from `Customer` where cellphone='15538127777'");
			System.out.println(map.get("level"));
		}
	}
}
