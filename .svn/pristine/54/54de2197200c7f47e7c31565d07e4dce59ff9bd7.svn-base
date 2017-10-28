package com.ldyy.tool;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ldyy.data.Tree;

public abstract class DBCtrl {
	private static Logger log = Logger.getLogger(DBCtrl.class);
	// ！多数据库支持，跨数据库事务
	// ！以后不使用all这类非特色字段，以免冲突
	/** key: table name, value: [key:colName, value: column type] */
	public static Map<String, Map<String, Integer>> tableConfig = new ConcurrentHashMap<String, Map<String, Integer>>();

	/**
	 * 多条语句的执行
	 * 
	 * @param sqlMap
	 * @return
	 */
	public static boolean submit(String... sql) {
		boolean b = false;
		Connection con = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con.setAutoCommit(false);
			stmt = con.createStatement();
			for (int i = 0; i < sql.length; i++) {
				stmt.addBatch(sql[i]);
			}
			stmt.executeBatch();

			con.commit();
			b = true;
		} catch (Throwable e) {
			log.error("执行sql时发生异常", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("发生异常后，回滚失败。", e1);
			}
		}
		close(rs, stmt);
		DBCon.releaseCon(con);
		return b;
	}

	public static Map<String, String> getSelect(String sql) {
		Connection conn = DBCon.getConnection();
		Map<String, String> ans = new HashMap<String, String>();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();

			for (; rs.next();) {
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String value = rs.getString(i);
					if (value == null)
						continue;
					ans.put(rsmd.getColumnName(i), value.trim());
				}
			}
		} catch (Throwable t) {
			log.error("执行sql[" + sql + "]时发生异常", t);
			return null;
		} finally {
			close(rs, stmt);
			DBCon.releaseCon(conn);
		}
		return ans;
	}

	/**
	 * 
	 * @param sqlMap
	 *            含有sql的map
	 * @param result
	 *            查询结果
	 * @return 执行是否
	 */
	public static boolean select(String tableName, Tree<String> sqlMap,
			String[] keyWord, String[] getCol) {
		boolean b = false;
		Connection con = DBCon.getConnection();
		if (!tableConfig.containsKey(tableName)) {
			addTableInfo(tableName, con);
		}
		ResultSet rs = null;
		Statement stmt = null;
		try {
			Map<String, Integer> colInfoMap = tableConfig.get(tableName);

			StringBuilder col = new StringBuilder();
			if (getCol.length == 1 && getCol[0].equals("all")) {
				col.append("*");
			} else {
				for (String key : getCol) {
					col.append(key);
					col.append(",");
				}
				col.deleteCharAt(col.length() - 1);
			}
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			sql.append(col);
			sql.append(" from `");
			sql.append(tableName);
			sql.append("` where ");
			sql.append(getWhereString(sqlMap, keyWord, colInfoMap));
			log.debug(sql.toString());

			stmt = con.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if (!rs.next()) {
				b = false;
			} else {
				for (String key : getCol) {
					if (key.equals("all")) {
						for (String temp0 : colInfoMap.keySet()) {
							if (rs.getString(temp0) != null)
								sqlMap.put(temp0, rs.getString(temp0));
						}
						break;
					}
					if (rs.getString(key) != null)
						sqlMap.put(key, rs.getString(key));
				}
				b = true;
			}
		} catch (Throwable e) {
			log.error("", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("回滚失败", e1);
			}
		}
		close(rs, stmt);
		DBCon.releaseCon(con);
		return b;
	}

	public static String selectSql(String tableName,
			Tree<String> sqlMap, String[] keyWord, String[] getCol) {
		Connection con = DBCon.getConnection();
		if (!tableConfig.containsKey(tableName)) {
			addTableInfo(tableName, con);
		}
		StringBuilder sql = new StringBuilder();
		Map<String, Integer> colInfoMap = tableConfig.get(tableName);

		StringBuilder col = new StringBuilder();
		if (getCol.length == 1 && getCol[0].equals("all")) {
			col.append("*");
		} else {
			for (String key : getCol) {
				col.append(key);
				col.append(",");
			}
			col.deleteCharAt(col.length() - 1);
		}
		sql.append("select ");
		sql.append(col);
		sql.append(" from `");
		sql.append(tableName);
		sql.append("` where ");
		sql.append(getWhereString(sqlMap, keyWord, colInfoMap));
		log.debug(sql.toString());
		DBCon.releaseCon(con);
		return sql.toString();
	}

	/**
	 * 拼接sql
	 * 
	 * @param sqlMap
	 * @param keyWord
	 * @param setCol
	 * @return
	 */
	public static String update(String tableName, Tree<String> sqlMap,
			String[] keyWord, String[] setCol) {
		if (!tableConfig.containsKey(tableName)) {
			Connection con = DBCon.getConnection();
			addTableInfo(tableName, con);
			DBCon.releaseCon(con);
		}
		Map<String, Integer> colInfoMap = tableConfig.get(tableName);

		StringBuilder set = new StringBuilder();
		if (setCol.length == 1 && setCol[0].equals("all")) {
			for (String key : colInfoMap.keySet()) {
				String tmp = sqlMap.get(key);
				if (tmp != null && tmp.length() > 0) {
					set.append(key).append("=");
					if (canAddQuote(colInfoMap.get(key))) {
						set.append("'").append(tmp).append("',");
					} else {
						set.append(tmp).append(",");
					}
				}
			}
		} else {
			for (String key : setCol) {
				if (sqlMap.get(key) != null) {
					set.append(key).append("=");
					if (canAddQuote(colInfoMap.get(key))) {
						set.append("'").append(sqlMap.get(key)).append("',");
					} else {
						set.append(sqlMap.get(key)).append(",");
					}
				}
			}
		}
		set.deleteCharAt(set.length() - 1);

		StringBuilder sql = new StringBuilder();
		sql.append("update `").append(tableName);
		sql.append("` set ").append(set).append(" where ");
		sql.append(getWhereString(sqlMap, keyWord, colInfoMap));
		log.debug(sql.toString());
		set = null;
		return sql.toString();
	}

	/**
	 * 根据sqlMap中的key/value拼接update的sql
	 * 
	 * @param sqlMap
	 * @return
	 */
	public static String insert(String tableName, Tree<String> sqlMap) {
		if (!tableConfig.containsKey(tableName)) {
			Connection con = DBCon.getConnection();
			addTableInfo(tableName, con);
			DBCon.releaseCon(con);
		}
		StringBuilder colName = new StringBuilder();
		StringBuilder value = new StringBuilder();
		colName.append("(");
		value.append("(");
		Map<String, Integer> colInfoMap = tableConfig.get(tableName);
		for (String cn : colInfoMap.keySet()) {
			if (sqlMap.get(cn) != null) {
				String temp1 = sqlMap.get(cn);
				if (temp1 != null && temp1.length() > 0) {
					colName.append(cn);
					colName.append(",");
					if (canAddQuote(colInfoMap.get(cn))) {
						value.append("'");
						value.append(temp1);
						value.append("',");
					} else {
						value.append(temp1);
						value.append(",");
					}
				}
			}
		}
		colName.deleteCharAt(colName.length() - 1);
		colName.append(")");
		value.deleteCharAt(value.length() - 1);
		value.append(")");

		StringBuilder sql = new StringBuilder();
		sql.append("insert into `").append(tableName).append("` ");
		sql.append(colName).append(" values ").append(value);
		log.debug(sql.toString());
		return sql.toString();
	}

	public static boolean getInsert(String tableName, Tree<String> sqlMap) {
		boolean b = false;
		String sql = insert(tableName, sqlMap);
		Connection con = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			con.setAutoCommit(false);
			stmt = con.createStatement();
			stmt.execute(sql);
			rs = stmt.executeQuery("SELECT LAST_INSERT_ID()");
			if (rs.next()) {
				sqlMap.put("id", rs.getString("LAST_INSERT_ID()"));
			}
			con.commit();
			b = true;
		} catch (Throwable e) {
			log.error("执行sql时发生异常", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("发生异常后，回滚失败。", e1);
			}
		}
		close(rs, stmt);
		DBCon.releaseCon(con);
		return b;
	}

	public static String getWhereString(Tree<String> cmap,
			String[] keyWords, Map<String, Integer> colInfoMap) {
		StringBuilder where = new StringBuilder();
		for (String key : keyWords) {
			if (cmap.get(key) != null) {
				where.append(key).append("=");
				if (canAddQuote(colInfoMap.get(key))) {
					where.append("'").append(cmap.get(key)).append("'");
				} else {
					where.append(cmap.get(key));
				}
				where.append(" and ");
			}
		}
		where.delete(where.length() - 5, where.length() - 1);

		return where.toString();
	}

	/**
	 * 把表的列名信息保存到dbconfig中
	 * 
	 * @param Sys_database
	 *            表名
	 * @param con
	 *            数据库连接
	 * @return 是否成功添加到 dbconfig中
	 */
	// ！失败的处理，健壮性策略
	public static boolean addTableInfo(String tableName, Connection con) {
		boolean b = false;
		Statement stmt = null;
		ResultSet rs = null;
		try {
			StringBuilder sql = new StringBuilder();
			sql.append("select * from `");
			sql.append(tableName);
			sql.append("` where 1=2");
			stmt = con.createStatement();
			rs = stmt.executeQuery(sql.toString());

			ResultSetMetaData rsmd = rs.getMetaData();
			int colcount = rsmd.getColumnCount();
			Map<String, Integer> t = new HashMap<String, Integer>();
			for (int i = 1; i <= colcount; i++) {
				t.put(rsmd.getColumnName(i), rsmd.getColumnType(i));
			}
			tableConfig.put(tableName, t);
			b = true;
		} catch (Throwable e) {
			log.error("", e);
		}
		close(rs, stmt);
		return b;
	}

	public static void close(ResultSet rs, Statement stmt) {
		try {
			if (rs != null)
				rs.close();
		} catch (Exception e) {
			log.error("", e);
		}
		try {
			if (stmt != null)
				stmt.close();
		} catch (Exception e) {
			log.error("", e);
		}
	}

	/**
	 * 数值类型、布尔型不加引号；
	 * 
	 * @param t
	 *            列类型
	 * @return false if t is number or boolean. otherwise return true.
	 */
	public static boolean canAddQuote(int t) {
		return !(t == Types.INTEGER || t == Types.SMALLINT || t == Types.BIT || t == Types.BIGINT);
	}

	// ---------- 以下为新增方法
	// ------------------------------------------------------------------------------------------

	/**
	 * @param tableName
	 *            表名
	 * @param sqlMap
	 *            参数值
	 * @param getCol
	 *            查询结果
	 * @param whereStr
	 *            where条件的SQL
	 * @return 查询一条数据
	 */
	public static boolean selectOne(String tableName,
			Tree<String> sqlMap, String[] getCol, String whereStr) {
		boolean b = false;
		Connection con = DBCon.getConnection();
		if (!tableConfig.containsKey(tableName)) {
			addTableInfo(tableName, con);
		}
		ResultSet rs = null;
		Statement stmt = null;
		try {
			Map<String, Integer> colInfoMap = tableConfig.get(tableName);

			StringBuilder col = new StringBuilder();
			if (getCol.length == 1 && getCol[0].equals("all")) {
				col.append("*");
			} else {
				for (String key : getCol) {
					col.append(key);
					col.append(",");
				}
				col.deleteCharAt(col.length() - 1);
			}
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			sql.append(col);
			sql.append(" from `");
			sql.append(tableName);
			sql.append("` where ");
			sql.append(whereStr);
			log.debug(sql.toString());

			stmt = con.createStatement();
			rs = stmt.executeQuery(sql.toString());
			if (!rs.next()) {
				b = false;
			} else {
				for (String key : getCol) {
					if (key.equals("all")) {
						for (String temp0 : colInfoMap.keySet()) {
							if (rs.getString(temp0) != null)
								sqlMap.put(temp0, rs.getString(temp0));
						}
						break;
					}
					if (rs.getString(key) != null)
						sqlMap.put(key, rs.getString(key));
				}
				b = true;
			}
		} catch (Throwable e) {
			log.error("", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("回滚失败", e1);
			}
		}
		close(rs, stmt);
		DBCon.releaseCon(con);

		return b;
	}

	/**
	 * @param tableName
	 *            表名
	 * @param getCol
	 *            查询结果
	 * @param whereStr
	 *            where条件的SQL
	 * @return 查询数据集合
	 */
	public static List<Map<String, String>> selectList(String tableName,
			String[] getCol, String whereStr) {
		List<Map<String, String>> dataList = null;

		Connection con = DBCon.getConnection();
		if (!tableConfig.containsKey(tableName)) {
			addTableInfo(tableName, con);
		}
		ResultSet rs = null;
		Statement stmt = null;
		try {
			Map<String, Integer> colInfoMap = tableConfig.get(tableName);

			StringBuilder col = new StringBuilder();
			if (getCol.length == 1 && getCol[0].equals("all")) {
				col.append("*");
			} else {
				for (String key : getCol) {
					col.append(key);
					col.append(",");
				}
				col.deleteCharAt(col.length() - 1);
			}
			StringBuilder sql = new StringBuilder();
			sql.append("select ");
			sql.append(col);
			sql.append(" from `");
			sql.append(tableName);
			sql.append("` where ");
			sql.append(whereStr);
			log.debug(sql.toString());

			stmt = con.createStatement();
			rs = stmt.executeQuery(sql.toString());

			dataList = new ArrayList<Map<String, String>>();
			while (rs.next()) {
				Map<String, String> dataMap = new HashMap<String, String>();
				if (getCol.length == 1 && getCol[0].equals("all")) {
					for (String temp0 : colInfoMap.keySet()) {
						if (rs.getString(temp0) != null) {
							dataMap.put(temp0, rs.getString(temp0));
						}
					}
				} else {
					for (String key : getCol) {
						if (rs.getString(key) != null) {
							dataMap.put(key, rs.getString(key));
						}
					}
				}
				dataList.add(dataMap);
			}
		} catch (Throwable e) {
			log.error("", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("回滚失败", e1);
			}
			return null;
		}
		close(rs, stmt);
		DBCon.releaseCon(con);

		return dataList;
	}

	/**
	 * @param tableName
	 *            表名
	 * @param sqlMap
	 *            请求参数
	 * @param setCol
	 *            更新字段
	 * @param whereStr
	 *            更新条件
	 * @return 是否更新成功
	 */
	public static boolean update(String tableName, Map<String, String> sqlMap,
			String[] setCol, String whereStr) {
		boolean b = false;
		Connection con = DBCon.getConnection();
		if (!tableConfig.containsKey(tableName)) {
			addTableInfo(tableName, con);
		}

		Statement stmt = null;
		try {
			Map<String, String> cmap = sqlMap;
			Map<String, Integer> colInfoMap = tableConfig.get(tableName);

			StringBuilder set = new StringBuilder();
			if (setCol.length == 1 && setCol[0].equals("all")) {
				for (String key2 : cmap.keySet()) {
					if (colInfoMap.containsKey(key2)) {
						String temp = cmap.get(key2);
						if (temp != null && temp.length() > 0) {
							set.append(key2).append("=");
							if (canAddQuote(colInfoMap.get(key2))) {
								set.append("'").append(temp).append("',");
							} else {
								set.append(temp).append(",");
							}
						}
					}
				}
			} else {
				for (String key : setCol) {
					if (cmap.containsKey(key)) {
						set.append(key).append("=");
						if (canAddQuote(colInfoMap.get(key))) {
							set.append("'").append(cmap.get(key)).append("',");
						} else {
							set.append(cmap.get(key)).append(",");
						}
					}
				}
			}
			set.deleteCharAt(set.length() - 1);

			StringBuilder sql = new StringBuilder();
			sql.append("update `").append(tableName);
			sql.append("` set ").append(set).append(" where ");
			sql.append(whereStr);
			log.debug(sql.toString());
			set = null;

			stmt = con.createStatement();
			if (stmt.executeUpdate(sql.toString()) > 0) {
				b = true;
			}
		} catch (Throwable e) {
			log.error("", e);
			try {
				con.rollback();
			} catch (Throwable e1) {
				log.error("回滚失败", e1);
			}
		}
		close(null, stmt);
		DBCon.releaseCon(con);

		return b;
	}

	/**
	 * @param sql
	 *            查询脚本
	 * @return 列表查询
	 */
	public static List<Tree<String>> getSelectList(String sql, String treeName) {
		List<Tree<String>> dataList = new ArrayList<Tree<String>>();

		Connection conn = DBCon.getConnection();
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();

			for (; rs.next();) {
				Tree<String> dataMap = new Tree<String>(treeName);
				for (int i = 1; i <= rsmd.getColumnCount(); i++) {
					String value = rs.getString(i);
					if (value == null)
						continue;
					dataMap.put(rsmd.getColumnName(i), value.trim());
				}
				dataList.add(dataMap);
			}
		} catch (Throwable t) {
			log.error("执行sql[" + sql + "]时发生异常", t);
			return null;
		} finally {
			close(rs, stmt);
			DBCon.releaseCon(conn);
		}
		return dataList;
	}

}