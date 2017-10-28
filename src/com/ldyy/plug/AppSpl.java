package com.ldyy.plug;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ldyy.data.Json;
import com.ldyy.data.Tree;
import com.ldyy.module.MsgFlow;
import com.ldyy.tool.Config;
import com.ldyy.tool.DBCon;
import com.ldyy.tool.DBCtrl;
import com.ldyy.tool.EndStream;
import com.ldyy.tool.SMS;

public class AppSpl {
	private Logger log = Logger.getLogger(this.getClass());

	public String getNo_(Tree<String> result, String arg) {
		// !可使用新的数据库操作接口
		Connection conn = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			String sql = "select serialNo from `serialno` where ID='" + arg
					+ "' for update";
			rs = stmt.executeQuery(sql);

			rs.next();
			String no = rs.getString("serialNo");
			sql = "update `serialno` set serialNo=serialNo+1 where ID='" + arg
					+ "'";

			if (stmt.executeUpdate(sql) == 0) {
				// 应该是不会发生的错误
				result.put("ret", "-4");
				result.put("rtmsg", "数据库错误");
				result.put("Sys_closeFlag", "");
				throw new RuntimeException("更新流水号错误");
			}
			conn.commit();
			return no;
		} catch (Throwable t) {
			log.error("获取流水号时发生异常", t);
			try {
				conn.rollback();
			} catch (Throwable e1) {
				log.error("发生异常后，回滚失败。", e1);
			}
			result.put("ret", "-4");
			result.put("rtmsg", "数据库错误");
			result.put("Sys_closeFlag", "");
			return null;
		} finally {
			DBCtrl.close(rs, stmt);
			DBCon.releaseCon(conn);
		}
	}

	public String concat_(Tree<String> result, String arg) {
		String[] a1 = arg.split("=");
		String[] a2 = a1[1].split("[+]");
		StringBuilder sb = new StringBuilder();
		for (String tmp : a2) {
			if (tmp.startsWith("~"))
				sb.append(result.get(tmp.substring(1)));
			else
				sb.append(tmp);
		}
		result.put(a1[0], sb.toString());
		return null;
	}

	public void check(Tree<String> result, String str, String args) {
		String[] tmp = args.split("/");
		if (!result.get(tmp[0]).equals(result.get(tmp[1]))) {
			result.put("ret", tmp[2]);
			result.put("rtmsg", tmp[3]);
			result.put("Sys_closeFlag", "");
		}
	}

	public void authCode(Tree<String> result, String str) {
		// 限制时间，避免被刷
		int a = (int) (Math.random() * 1000000);
		String ac = add0(a, 6);
		int ans = 0;
		try {
			ans = SMS.sendSMS(result.get("cellphone"),"您本次的手机验证码为：" + ac + "【药乐多】","");
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (ans > 0)
			result.put("authCode", ac);
		else {
			result.put("ret", -ans + "");
			result.put("rtmsg", "短信验证码发送失败，请检查手机号稍后重试！");
		}
	}

	public void checkUser(Tree<String> result, String str) {
		result.put(result.get("type"), result.get("value"));
		boolean b = DBCtrl.select("Customer", result,
				new String[] { result.get("type") }, new String[] { "ID" });
		if (b) {
			result.put("ret", "1");
			result.put("rtmsg", "用户已存在");
			result.put("Sys_closeFlag", "");
		}
	}

	public void login(Tree<String> result, String str) {
		result.put(result.get("type"), result.get("value"));
		boolean c = DBCtrl.select("Customer", result,
				new String[] { result.get("type"), "password" }, new String[] {
						"type", "level", "ID", "cellphone", "linkman",
						"remark", "areaCode" });
		String level = result.get("level");

		if (!c) {
			result.put("ret", "1");
			result.put("rtmsg", "用户名或密码错误");
			result.put("Sys_closeFlag", "");
		} else if (level.equals("0")) {
			result.put("ret", "2");
			result.put("rtmsg", "未提交审核资料");
		} else if (level.equals("-1")) {
			result.put("ret", "3");
			result.put("rtmsg", "尚未审核");
			result.put("Sys_closeFlag", "");
		} else if (level.equals("-2")) {
			result.put("ret", "4");
			result.put("rtmsg", "资质审核未通过");
		}
	}

	public void drugList(Tree<String> result, String str) {
		String[] tmp = result.get("search").toUpperCase().split(" {1,}");
		int pageSize = Integer.parseInt(result.get("pageSize"));
		int pageNum = Integer.parseInt(result.get("pageNum"));
		StringBuilder where = new StringBuilder();
		String retail = result.get("retail");
		if (retail != null && retail.trim().length() != 0) {
			where.append(" search like '%rt=").append(retail).append("%' and");
		}
		for (String tp : tmp) {
			where.append(" search like '%").append(tp).append("%' and");
		}
		where.append(" status=1");
		if (pageNum == 1) {
			DBCtrl.selectOne("drugSearch", result,
					new String[] { "count(ID)" }, where.toString());
			result.put("size", result.remove("count(ID)"));
		}
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		String time = date.format(now);
		result.put("time", time);

		StringBuilder in = new StringBuilder();
		in.append("select t.id from (select ID from `drugSearch` where")
				.append(where.toString()).append(" limit ")
				.append(pageSize * (pageNum - 1)).append(",").append(pageSize)
				.append(") as t");
		List<Tree<String>> drugL = drugL(result, in.toString(), "drugList");
		for (Tree<String> t : drugL)
			t.put("imgUrl", t.get("ID") + ".png");
		addList(drugL, result, "drugList");
	}

	public void drugInfo(Tree<String> result, String str) {
		String tmp = result.get("ID");
		StringBuilder in = new StringBuilder();
		in.append(tmp);
		SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd");
		Date now = new Date();
		String time = date.format(now);
		result.put("time", time);
		List<Tree<String>> drugL = drugL(result, in.toString(), "drugInfo");
		result.addBranch(drugL.get(0));
	}

	private static List<Tree<String>> drugL(Tree<String> result, String in,
			String name) {
		Map<String, String> cfg = Config.getM("drugL");
		String str = cfg.keySet().toString().trim();
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(str.substring(1, str.length() - 1))
				.append(" from `drug` where ID in (").append(in).append(")");
		List<Tree<String>> drugL = DBCtrl.getSelectList(sb.toString(), name);
		return drugL;
	}

	private static void addList(List<Tree<String>> l, Tree<String> father,
			String name) {
		if (l.size() == 0) {
			Tree<String> t = new Tree<String>(name);
			t.haveSameName = 1;
			father.addBranch(t);
		} else if (l.size() == 1) {
			Tree<String> t = l.get(0);
			t.haveSameName = 1;
			father.addBranch(t);
		} else {
			father.addBranchs(l);
		}
	}

	private static String add0(int str, int n) {
		return add0(str + "", n);
	}

	private static String add0(String str, int n) {
		String len = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
		return len.substring(128 - n + str.length()) + str;
	}

	private static SimpleDateFormat sdf = new SimpleDateFormat(
			"yyyy-MM-dd HH:mm:ss");
	private static DecimalFormat df = new DecimalFormat(".00");

	public void checkTrol(Tree<String> result, String str) {
		// for (Tree<String> t : result.getBranchs("drugs")) {
		//
		// }
		result.put("trolley", result.getBranch("trol").toString());
	}

	public void getTrol(Tree<String> result, String str) {
		Map<String, String> data = DBCtrl
				.getSelect("select trolley from customer where ID="
						+ result.get("ID"));
		String tmp = data.get("trolley");
		// if (tmp == null || tmp.length() == 0) {
		// result.addBranch(new Tree<String>("trol"));
		// return;
		// }
		Tree<String> trolley = Json.getTree(tmp);
		if (trolley == null || trolley.branchSize() == 0) {
			result.addBranch(new Tree<String>("trol"));
			result.addBranch(new Tree<String>("trolInfo"));
			return;
		}
		trolley.reName("trol");
		result.addBranch(trolley);
		List<String> l = new ArrayList<String>();
		for (Tree<String> t : trolley)
			l.add(t.getName());
		String in = l.toString().trim();
		in = in.substring(1, in.length() - 1);
		List<Tree<String>> drugL = drugL(result, in, "trolInfo");
		Tree<String> trolInfo = new Tree<String>("trolInfo");
		for (Tree<String> t : drugL) {
			t.reName(t.get("ID"));
			trolInfo.addBranch(t);
		}
		result.addBranch(trolInfo);
	}

	public void indent(Tree<String> result, String str) {
		Tree<String> drugs = result.getBranch("drugs");
		List<String> l = new ArrayList<String>();
		for (Tree<String> t : drugs)
			l.add(t.getName());
		if (l.size() == 0) {
			result.put("ret", "1");
			result.put("rtmsg", "订单内容不能为空");
			result.put("Sys_closeFlag", "");
		}
		Tree<String> indent = new Tree<String>("indent");
		result.addBranch(indent);
		String indentID = null;
		Tree<String> notEnough = null;
		String in = l.toString().trim();
		in = in.substring(1, in.length() - 1);
		StringBuilder sb = new StringBuilder();
		sb.append(
				"select ID,name,venderd,standard,expiry,approval,price,number,provider,retail,pack,package from `drug` where ID in (")
				.append(in).append(") for update");

		Connection conn = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sb.toString());
			ResultSetMetaData rsmd = rs.getMetaData();

			List<String> IDs = new ArrayList<String>();
			for (; rs.next();) {
				Tree<String> data = new Tree<String>("drugs");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String value = rs.getString(i);
					if (value == null)
						continue;
					data.put(rsmd.getColumnName(i), value.trim());
				}
				int num = Integer.parseInt(drugs.get(data.get("ID")));

				int number = Integer.parseInt(data.get("number"));
				if (number >= num) {
					if (indentID == null) {
						indentID = getNo_(result, "Indent");
						System.out.println(result);
						DBCtrl.select("area", result, new String[] { "areaCode" },
								new String[] { "ID1" });
					}
					Tree<String> provider = indent.getBranch(data
							.get("provider"));
					if (provider == null) {
						provider = new Tree<String>(data.get("provider"));
						indent.addBranch(provider);
						provider.addBranch(new Tree<String>("tmp"));
						provider.put("money", "0.00");
					}
					data.put("number", num + "");
					provider.addBranch(data);
					float money = Float.parseFloat(provider.get("money"))
							+ Float.parseFloat(data.get("price")) * num;
					provider.put("money", df.format(money));
					Tree<String> drug = provider.getBranch("tmp");
					Tree<String> tmp = new Tree<String>("drugs");
					String ID = data.get("ID");
					tmp.put("ID", ID);
					tmp.put("number", data.get("number"));
					tmp.put("price", data.get("price"));
					drug.addBranch(tmp);
					if (data.haveSameName == 0)
						data.haveSameName = 1;
					number -= num;
					String status = "1";
					String retail = data.get("retail");
					if (retail.equals("0")
							&& number < Integer.parseInt(data.get("package")))
						status = "0";
					else if (retail.equals("1")
							&& number < Integer.parseInt(data.get("pack")))
						status = "0";
					if (status.equals("0")) {
						stmt.addBatch("update `drugsearch` set status=0 where ID="
								+ ID);
						IDs.add(ID);
					}
					StringBuilder sql = new StringBuilder();
					sql.append("update `drug` set number=").append(number)
							.append(",status=").append(status)
							.append(" where ID=").append(ID);
					stmt.addBatch(sql.toString());
				} else {
					if (notEnough == null)
						notEnough = new Tree<String>("notEnough");
					notEnough.addBranch(data);
					if (data.haveSameName == 0)
						data.haveSameName = 1;
				}
			}
			if (IDs.size() > 0) {
				in = IDs.toString().trim();
				in = in.substring(1, in.length() - 1);
				stmt.addBatch("update `drugsearch` set status=0 where ID in ("
						+ in + ")");
			}

			String num = add0(indent.branchSize(), 2);
			String date = sdf.format(new Date());
			int i = 0;
			for (Tree<String> t : indent.listBranchs()) {
				i++;
				StringBuilder id = new StringBuilder();
				id.append(add0(indentID, 14)).append("_").append(num)
						.append("_").append(add0(i, 2));
				t.put("ID", id.toString());
				t.put("provider", t.getName());
				t.put("customer", result.get("ID"));
				t.put("saler", result.get("ID1"));
				t.put("status", "0");
				t.put("datetime", date);
				t.put("info", t.getBranch("tmp").toString());
				t.reName(id.toString());
				String sql = DBCtrl.insert("indent", t);
				t.removeBranch("tmp");
				t.removeBranch("info");
				stmt.addBatch(sql);
			}
			if (indentID != null) {
				result.add("indentID", indentID);
				result.add("indentNum", indent.branchSize() + "");
			}
			if (notEnough != null)
				indent.addBranch(notEnough);
			stmt.executeBatch();
			conn.commit();
		} catch (Throwable t) {
			log.error("生成订单时发生异常", t);
			try {
				conn.rollback();
			} catch (Throwable e) {
				log.error("发生异常后，回滚失败。", e);
			}
			result.put("ret", "-4");
			result.put("rtmsg", "数据库错误");
			result.put("Sys_closeFlag", "");
		} finally {
			DBCtrl.close(rs, stmt);
			DBCon.releaseCon(conn);
		}
	}

	public void indentList(Tree<String> result, String str) {
		int pageSize = Integer.parseInt(result.get("pageSize"));
		int pageNum = Integer.parseInt(result.get("pageNum"));
		StringBuilder where = new StringBuilder();
		int userID = Integer.parseInt(result.get("userID"));
		if (userID > 9999999 && userID < 90000000)
			result.put("customer", userID + "");
		else if (userID > 89999999)
			result.put("provider", userID + "");
		else if (userID > 8999999)
			result.put("saler", userID + "");

		String ID = result.get("ID");
		if (ID != null && ID.trim().length() > 0) {
			where.append("ID like '%").append(ID).append("%' and ");
		} else {
			String status = result.get("status");
			if (status != null && status.trim().length() > 0)
				where.append("status=").append(status).append(" and ");
			String dateF = result.get("dateF");
			String dateT = result.get("dateT");
			if (dateF != null && dateF.trim().length() > 0)
				where.append("datetime between '").append(dateF)
						.append(" 00:00:00' and '").append(dateT)
						.append(" 23:59:59' and ");
		}

		String[] users = { "customer", "provider", "saler" };
		for (String user : users) {
			String value = result.get(user);
			if (value != null && value.trim().length() > 0)
				where.append(user).append("=").append(value).append(" and ");
		}

		if (where.length() != 0)
			where.delete(where.length() - 4, where.length());
		else
			where.append("1=1 ");
		if (pageNum == 1) {
			DBCtrl.selectOne("indent", result, new String[] { "count(ID)" },
					where.toString());
			result.put("size", result.remove("count(ID)"));
		}
		where.append("order by ID desc limit ").append(pageSize * (pageNum - 1)).append(",")
				.append(pageSize);

		Map<String, String> cfg = Config.getM("indentL");
		str = cfg.keySet().toString().trim();
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(str.substring(1, str.length() - 1))
				.append(" from `indent` where ").append(where.toString());
		System.out.println(sb.toString());
		List<Tree<String>> indentL = DBCtrl.getSelectList(sb.toString(),
				"indentList");
		addList(indentL, result, "indentList");
		if (indentL.size() == 0)
			return;
		List<String> l = new ArrayList<String>();
		List<String> lc = new ArrayList<String>();
		for (Tree<String> t : indentL) {
			lc.add(t.get("customer") + "0");
			Tree<String> tmp = Json.getTree(t.remove("info"));
			boolean flag = tmp.branchSize() == 1;
			for (Tree<String> drug : tmp.listBranchs()) {
				l.add(drug.get("ID"));
				t.addBranch(drug);
				if (flag)
					drug.haveSameName = 1;
			}
		}
		String in = lc.toString().trim();
		in = in.substring(1, in.length() - 1);
		sb = new StringBuilder();
		sb.append(
				"select AID,areaCode,address,linkman,cellphone from `address` where AID in (")
				.append(in).append(")");
		System.out.println(sb);
		List<Tree<String>> address = DBCtrl.getSelectList(sb.toString(),
				"address");
		Tree<String> addres = new Tree<String>("address");
		for (Tree<String> ads : address) {
			ads.reName(ads.get("AID"));
			addres.addBranch(ads);
		}
		in = l.toString().trim();
		in = in.substring(1, in.length() - 1);
		sb = new StringBuilder();
		sb.append(
				"select ID,name,venderd,standard,expiry,approval from `drug` where ID in (")
				.append(in).append(")");
		List<Tree<String>> drugInfo = DBCtrl.getSelectList(sb.toString(),
				"drug");
		Tree<String> drugs = new Tree<String>("drugs");
		for (Tree<String> drug : drugInfo) {
			drug.reName(drug.get("ID"));
			drug.remove("ID");
			drugs.addBranch(drug);
		}
		for (Tree<String> t : indentL) {
			t.addBranchs(addres.getBranch(t.get("customer") + "0")
					.listBranchs());
			for (Tree<String> drug : t.getBranchs("drugs")) {
				drug.addBranchs(drugs.getBranch(drug.get("ID")).listBranchs());
			}
		}
	}

	public void submit(Tree<String> result, String str) {
		String indentID = result.get("indentID");
		if (str.equals("0")) {
			int n = Integer.parseInt(result.get("indentNum"));
			String num = add0(n, 2);
			n++;
			List<String> IDs = new ArrayList<String>();
			for (int i = 1; i < n; i++) {
				StringBuilder id = new StringBuilder();
				id.append(add0(indentID, 14)).append("_").append(num)
						.append("_").append(add0(i, 2));
				IDs.add(id.toString());
			}
			if (!cancelIndent_(IDs.toArray(new String[] {}))) {
				result.put("ret", "1");
				result.put("rtmsg", "取消订单失败");
				result.put("Sys_closeFlag", "");
			}
		} else {
			String sql = "update `indent` set status=1 where ID like '" + add0(indentID, 14) + "%'";
			DBCtrl.submit(sql);
		}
	}

	private boolean cancelIndent_(String... IDs) {
		StringBuilder inDent = new StringBuilder();
		for (String tmp : IDs) {
			inDent.append("'").append(tmp).append("',");
		}
		inDent.deleteCharAt(inDent.length() - 1);
		StringBuilder sb = new StringBuilder();
		sb.append("select info from `indent` where ID in (").append(inDent)
				.append(") and status<3 for update");
		System.out.println(sb);
		Connection conn = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			conn.setAutoCommit(false);
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sb.toString());
			ResultSetMetaData rsmd = rs.getMetaData();

			List<String> drugs = new ArrayList<String>();
			Tree<Integer> drugsTY = new Tree<Integer>("nums");
			int num = 0;
			for (; rs.next();) {
				Tree<String> indent = new Tree<String>("indent");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String value = rs.getString(i);
					if (value == null)
						continue;
					indent.put(rsmd.getColumnName(i), value.trim());
				}
				Tree<String> tmp = Json.getTree(indent.get("info"));
				for (Tree<String> drug : tmp.listBranchs()) {
					drugs.add(drug.get("ID"));
					drugsTY.add(drug.get("ID"),
							Integer.parseInt(drug.get("number")));
				}
				num++;
			}
			if (num == 0)
				return false;
			rs.close();

			String in = drugs.toString().trim();
			in = in.substring(1, in.length() - 1);
			sb = new StringBuilder();
			sb.append("select ID,number,status from `drug` where ID in (")
					.append(in).append(") for update");
			System.out.println(sb);
			rs = stmt.executeQuery(sb.toString());
			rsmd = rs.getMetaData();
			Tree<Integer> drugsT = new Tree<Integer>("nums");
			drugs = new ArrayList<String>();
			for (; rs.next();) {
				Tree<String> drug = new Tree<String>("drug");
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String value = rs.getString(i);
					if (value == null)
						continue;
					drug.put(rsmd.getColumnName(i), value.trim());
				}
				drugsT.add(drug.get("ID"), Integer.parseInt(drug.get("number")));
				if (drug.get("status").equals("0"))
					drugs.add(drug.get("ID"));
			}
			if (drugs.size() > 0) {
				in = drugs.toString().trim();
				in = in.substring(1, in.length() - 1);
				sb = new StringBuilder();
				sb.append("update `drugsearch` set status=1 where ID in (")
						.append(in).append(")");
				System.out.println(sb);
				stmt.addBatch(sb.toString());
			}

			for (Tree<Integer> tmp : drugsTY.listBranchs()) {
				drugsT.put(tmp.getName(),
						drugsT.get(tmp.getName()) + tmp.getValue());
			}
			for (Tree<Integer> tmp : drugsT.listBranchs()) {
				sb = new StringBuilder();
				sb.append("update `drug` set status=1,number=")
						.append(tmp.getValue()).append(" where ID=")
						.append(tmp.getName());
				System.out.println(sb);
				stmt.addBatch(sb.toString());
			}

			sb = new StringBuilder();
			sb.append("update `indent` set status=6 where ID in (")
					.append(inDent).append(") and status<3");
			System.out.println(sb);
			stmt.addBatch(sb.toString());

			stmt.executeBatch();
			conn.commit();
			return true;
		} catch (Throwable t) {
			log.error("取消订单时发生异常", t);
			try {
				conn.rollback();
			} catch (Throwable e) {
				log.error("发生异常后，回滚失败。", e);
			}
			return false;
		} finally {
			DBCtrl.close(rs, stmt);
			DBCon.releaseCon(conn);
		}
	}

	private static String imgPath = Config.get("imgPath");

	public void upload(Tree<String> result, String str) {
		String ID = result.get("ID");
		Long id = Thread.currentThread().getId();
		InputStream is = MsgFlow.iss.remove(id);
		EndStream es = new EndStream(is);
		String end = "\r\n";
		String flag = end + es.readE(end);
		System.out.println(flag);
		String tmp;
		String name = null;
//		String type = null;
		while (true) {
			tmp = es.readE(end);
			System.out.println("1" + tmp);
			String[] tps = tmp.split(";");
			for (String tp : tps) {
				if (tp.trim().startsWith("name")) {
					name = tp.substring(tp.indexOf("\"") + 1,
							tp.lastIndexOf("\""));
				}
//				else if (tp.trim().startsWith("filename")) {
//					type = tp.substring(tp.lastIndexOf("."),
//							tp.lastIndexOf("\""));
//				}
			}
			while (true) {
				tmp = es.readE(end);
				System.out.println("2" + tmp);
				if (tmp.length() == 0)
					break;
			}
			byte[] b = es.readEnd(flag);
			try {
				StringBuilder sb = new StringBuilder();
				sb.append(imgPath).append(ID).append("_").append(name)
						.append(".").append("jpg");
				OutputStream os = new FileOutputStream(sb.toString());
//				b = Img.pressImg(b);
				os.write(b);
				os.flush();
				os.close();
			} catch (Exception e) {
				result.put("ret", "1");
				result.put("rtmsg", "上传文件失败");
				result.put("Sys_closeFlag", "");
				log.error("保存文件出错", e);
			}
			tmp = es.readE(end);
			System.out.println("3" + tmp);
			if (tmp.equals("--")) {
				break;
			}
		}
	}

	SimpleDateFormat sdf0 = new SimpleDateFormat("yyyy-MM-dd");

	public String getDate_(Tree<String> result) {
		Date now = new Date();
		String date = sdf0.format(now);
		return date;
	}

	public void salesLogin(Tree<String> result, String str) {
		result.put(result.get("cellphone"), result.get("password"));
		boolean c = DBCtrl.select("inside", result, new String[] { "cellphone",
				"password" }, new String[] { "ID", "name", "type" });
		if (!c) {
			result.put("ret", "1");
			result.put("rtmsg", "手机号或密码错误");
			result.put("Sys_closeFlag", "");
		}
	}

	public void checkPhone(Tree<String> result, String str) {
		boolean b = DBCtrl.select("inside", result,
				new String[] { "cellphone" }, new String[] { "id" });
		if (!b) {
			result.put("ret", "1");
			result.put("rtmsg", "手机号输入有误");
			result.put("Sys_closeFlag", "");
		}
	}

	public void salesList(Tree<String> result, String str) {
		int pageSize = Integer.parseInt(result.get("pageSize"));
		int pageNum = Integer.parseInt(result.get("pageNum"));
		StringBuilder where = new StringBuilder();
		int userID = Integer.parseInt(result.get("userID"));
		if (userID > 8999999 && userID < 10000000)
			result.put("saler", userID + "");

		String cellphone = result.get("cellphone");
		String name = result.get("name");
		if (cellphone != null && cellphone.trim().length() > 0) {
			where.append("cellphone like '%").append(cellphone)
					.append("%' and ");
		} else if (name != null && name.trim().length() > 0) {
			where.append("name like '%").append(name).append("%' and ");
		} else {
			String dateF = result.get("dateF");
			String dateT = result.get("dateT");
			if (dateF != null && dateF.trim().length() > 0)
				where.append("date between '").append(dateF)
						.append(" 00:00:00' and '").append(dateT)
						.append(" 23:59:59' and ");

			String areaCode = result.get("areaCode");
			if (areaCode == null)
				areaCode = "";
			else if (areaCode.endsWith("000000"))
				areaCode = "";
			else if (areaCode.endsWith("0000"))
				areaCode = areaCode.substring(0, 2);
			else if (areaCode.endsWith("00"))
				areaCode = areaCode.substring(0, 4);

			StringBuilder in = new StringBuilder();
			in.append(
					"select t.ID1 from (select ID1 from `area` where areaCode like '")
					.append(areaCode).append("%') as t");

			if (areaCode.length() != 0)
				where.append("ID in (").append(in.toString()).append(") and ");
		}

		String value = result.get("saler");
		if (value != null && value.trim().length() > 0)
			where.append("grade=").append(value).append(" or ID=")
					.append(value).append(" and ");
		where.append("type=20");
		if (pageNum == 1) {
			DBCtrl.selectOne("inside", result, new String[] { "count(ID)" },
					where.toString());
			result.put("size", result.remove("count(ID)"));
		}
		where.append(" limit ").append(pageSize * (pageNum - 1)).append(",")
				.append(pageSize);

		Map<String, String> cfg = Config.getM("salesL");
		str = cfg.keySet().toString().trim();
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(str.substring(1, str.length() - 1))
				.append(" from `inside` where ").append(where.toString());
		System.out.println(sb.toString());
		List<Tree<String>> salesL = DBCtrl.getSelectList(sb.toString(),
				"salesList");
		if (salesL.size() == 0) {
			addList(salesL, result, "salesList");
			return;
		}
		List<String> IDs = new ArrayList<String>();
		for (Tree<String> tmp : salesL) {
			String n = tmp.get("grade");
			if (n != null)
				IDs.add(n);
		}

		if (IDs.size() > 0) {
			String in = IDs.toString().trim();
			in = in.substring(1, in.length() - 1);
			sb = new StringBuilder();
			sb.append("select ID,name from `inside` where ID in (").append(in)
					.append(")");
			System.out.println(sb);
			List<Tree<String>> gradeL = DBCtrl.getSelectList(sb.toString(),
					"gL");
			Tree<String> t = new Tree<String>("root");
			for (Tree<String> tmp : gradeL) {
				t.add(tmp.get("ID"), tmp.get("name"));
			}
			System.out.println(t);
			for (Tree<String> tmp : salesL) {
				String n = tmp.get("grade");
				if (n != null)
					tmp.put("grade", t.get(n));
			}
		}
		addList(salesL, result, "customerList");
	}

	public void customerList(Tree<String> result, String str) {
		int pageSize = Integer.parseInt(result.get("pageSize"));
		int pageNum = Integer.parseInt(result.get("pageNum"));
		StringBuilder where = new StringBuilder();
		int userID = Integer.parseInt(result.get("userID"));
		if (userID > 8999999 && userID < 10000000)
			result.put("saler", userID + "");

		String cellphone = result.get("cellphone");
		String name = result.get("name");
		if (cellphone != null && cellphone.trim().length() > 0) {
			where.append("cellphone like '%").append(cellphone)
					.append("%' and ");
		} else if (name != null && name.trim().length() > 0) {
			where.append("name like '%").append(name).append("%' and ");
		} else {
			String dateF = result.get("dateF");
			String dateT = result.get("dateT");
			if (dateF != null && dateF.trim().length() > 0)
				where.append("date between '").append(dateF)
						.append(" 00:00:00' and '").append(dateT)
						.append(" 23:59:59' and ");

			String type = result.get("type");
			if (type != null && type.trim().length() > 0)
				where.append("type=").append(type).append(" and ");
			String level = result.get("level");
			if (level != null && level.trim().length() > 0)
				where.append("level=").append(level).append(" and ");

			String areaCode = result.get("areaCode");
			if (areaCode == null)
				areaCode = "";
			else if (areaCode.endsWith("000000"))
				areaCode = "";
			else if (areaCode.endsWith("0000"))
				areaCode = areaCode.substring(0, 2);
			else if (areaCode.endsWith("00"))
				areaCode = areaCode.substring(0, 4);

			if (areaCode.length() != 0)
				where.append("areaCode like '").append(areaCode)
						.append("%' and ");
		}

		String value = result.get("saler");
		List<String> IDs = new ArrayList<String>();
		if (value != null && value.trim().length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append(
					"select areaCode from `area` where ID1 in (select ID from `inside` where grade=")
					.append(value).append(" or ID=").append(value).append(")");
			where.append("areaCode in (").append(sb.toString())
					.append(") and ");
		}
		if (where.length() != 0)
			where.delete(where.length() - 4, where.length());
		else
			where.append("1=1 ");
		if (pageNum == 1) {
			DBCtrl.selectOne("customer", result, new String[] { "count(ID)" },
					where.toString());
			result.put("size", result.remove("count(ID)"));
		}
		where.append("order by ID desc limit ").append(pageSize * (pageNum - 1)).append(",")
				.append(pageSize);

		Map<String, String> cfg = Config.getM("customerL");
		str = cfg.keySet().toString().trim();
		StringBuilder sb = new StringBuilder();
		sb.append("select ").append(str.substring(1, str.length() - 1))
				.append(" from `customer` where ").append(where.toString());
		System.out.println(sb.toString());
		List<Tree<String>> customerL = DBCtrl.getSelectList(sb.toString(),
				"customerList");
		if (customerL.size() == 0) {
			addList(customerL, result, "customerList");
			return;
		}
		IDs = new ArrayList<String>();
		for (Tree<String> tmp : customerL)
			IDs.add(tmp.get("ID") + "0");

		String in = IDs.toString().trim();
		in = in.substring(1, in.length() - 1);
		str = cfg.keySet().toString().trim();
		sb = new StringBuilder();
		sb.append("select AID,address from `address` where AID in (")
				.append(in).append(")");
		System.out.println(sb);
		List<Tree<String>> addressL = DBCtrl.getSelectList(sb.toString(), "aL");
		Tree<String> t = new Tree<String>("root");
		for (Tree<String> tmp : addressL) {
			tmp.reName(tmp.get("AID"));
			t.addBranch(tmp);
		}
		System.out.println(t);
		for (Tree<String> tmp : customerL)
			tmp.addBranchs(t.getBranch(tmp.get("ID") + "0"));
		addList(customerL, result, "customerList");
	}

	public void areaList(Tree<String> result, String str) {
		int pageSize = Integer.parseInt(result.get("pageSize"));
		int pageNum = Integer.parseInt(result.get("pageNum"));
		StringBuilder where = new StringBuilder();

		String cellphone = result.get("cellphone");
		String name = result.get("name");
		if (cellphone != null && cellphone.trim().length() > 0) {
			where.append("cellphone like '%").append(cellphone)
					.append("%' and ");
		} else if (name != null && name.trim().length() > 0) {
			where.append("name like '%").append(name).append("%' and ");
		}

		Map<String, String> cfg = Config.getM("salesL");
		str = cfg.keySet().toString().trim();
		str = str.substring(1, str.length() - 1);
		List<Tree<String>> salesL = null;
		if (where.length() > 0) {
			StringBuilder sb = new StringBuilder();
			sb.append("select ").append(str).append(" from `inside` where ")
					.append(where.toString()).append("type=20");
			System.out.println(sb.toString());
			salesL = DBCtrl.getSelectList(sb.toString(), "salesL");
			if (salesL.size() == 0) {
				addList(salesL, result, "areaList");
				return;
			}
			where = new StringBuilder();
			where.append("ID1 in (");
			for (Tree<String> tmp : salesL)
				where.append(tmp.get("ID")).append(",");
			where.deleteCharAt(where.length() - 1);
			where.append(")");
		} else {
			String areaCode = result.get("areaCode");
			if (areaCode == null)
				areaCode = "";
			else if (areaCode.endsWith("000000"))
				areaCode = "";
			else if (areaCode.endsWith("0000"))
				areaCode = areaCode.substring(0, 2);
			else if (areaCode.endsWith("00"))
				areaCode = areaCode.substring(0, 4);

			where.append("areaCode like '").append(areaCode).append("%'");
		}

		if (pageNum == 1) {
			DBCtrl.selectOne("area", result,
					new String[] { "count(areaCode)" }, where.toString());
			result.put("size", result.remove("count(areaCode)"));
		}
		where.append(" limit ").append(pageSize * (pageNum - 1)).append(",")
				.append(pageSize);

		StringBuilder sb = new StringBuilder();
		sb.append("select areaCode,ID1,remark from `area` where ").append(
				where.toString());
		System.out.println(sb.toString());
		List<Tree<String>> areaL = DBCtrl.getSelectList(sb.toString(),
				"areaList");
		if (areaL.size() == 0) {
			addList(areaL, result, "areaList");
			return;
		}
		List<String> IDs = new ArrayList<String>();
		if (salesL == null) {
			for (Tree<String> tmp : areaL) {
				String ID = tmp.get("ID1");
				if (!ID.equals("0"))
					IDs.add(ID);
			}
			if (IDs.size() > 0) {
				String in = IDs.toString().trim();
				in = in.substring(1, in.length() - 1);
				sb = new StringBuilder();
				sb.append("select ").append(str)
						.append(" from `inside` where ID in (").append(in)
						.append(")");
				System.out.println(sb);
				salesL = DBCtrl.getSelectList(sb.toString(), "salesL");
			}
		}
		IDs = new ArrayList<String>();
		Tree<String> tt = new Tree<String>("root");
		if (salesL != null) {
			for (Tree<String> tmp : salesL) {
				tmp.reName(tmp.get("ID"));
				tt.addBranch(tmp);
				String n = tmp.get("grade");
				if (n != null)
					IDs.add(n);
			}
			if (IDs.size() > 0) {
				String in = IDs.toString().trim();
				in = in.substring(1, in.length() - 1);
				sb = new StringBuilder();
				sb.append("select ID,name from `inside` where ID in (")
						.append(in).append(")");
				System.out.println(sb);
				List<Tree<String>> gradeL = DBCtrl.getSelectList(sb.toString(),
						"gL");
				Tree<String> t = new Tree<String>("root");
				for (Tree<String> tmp : gradeL) {
					t.add(tmp.get("ID"), tmp.get("name"));
				}
				System.out.println(t);
				for (Tree<String> tmp : salesL) {
					String n = tmp.get("grade");
					if (n != null)
						tmp.put("grade", t.get(n));
				}
			}
		}
		for (Tree<String> tmp : areaL) {
			String ID = tmp.get("ID1");
			if (ID.equals("0"))
				continue;
			tmp.addBranchs(tt.getBranch(ID).listBranchs());
		}
		addList(areaL, result, "areaList");
	}

	public void changeIndent(Tree<String> result, String str) {
		String status = result.get("status");
		String userType = result.get("userType");
		if (userType.equals("20")) {

		}
		if (status.equals("6")) {
			if (!cancelIndent_(result.get("ID"))) {
				result.put("ret", "1");
				result.put("rtmsg", "取消订单失败");
				result.put("Sys_closeFlag", "");
			}
		} else {
			String sql = DBCtrl.update("indent", result, new String[] { "ID" },
					new String[] { "status", "delivery", "deliveryID" });
			DBCtrl.submit(sql);
		}
	}
}
