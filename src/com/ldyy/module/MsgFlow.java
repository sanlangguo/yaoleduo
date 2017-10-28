package com.ldyy.module;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.ldyy.data.Json;
import com.ldyy.data.Tree;
import com.ldyy.tool.Config;
import com.ldyy.tool.DBCtrl;

public class MsgFlow {
	private static Logger log = Logger.getLogger(MsgFlow.class);
	private static int uriLen = Config.get("uriHeader").length();
	public static Map<Long, InputStream> iss = new HashMap<Long, InputStream>();
	public static Map<Long, OutputStream> oss = new HashMap<Long, OutputStream>();
	
	private static Map<String, Set<String>> at = new HashMap<String, Set<String>>();
	
	public static void init() {
		List<Tree<String>> atmp = DBCtrl.getSelectList("select type,privilege from permission", "pm");
		for (Tree<String> tmp : atmp) {
			Set<String> ts = new HashSet<String>();
			String[] ss = tmp.get("privilege").split(",");
			for (String tss : ss) {
				ts.add(tss);
			}
			at.put(tmp.get("type"), ts);
		}
		Set<String> s = at.get("0");
		for (Set<String> ts : at.values()) {
			ts.addAll(s);
		}
	}

	// !未来使用Tree配置，无需分级Map配置
	/**
	 * 流程控制
	 * 
	 * @param request
	 * @param response
	 * @return
	 */
	public static boolean control(HttpServletRequest request,
			HttpServletResponse response) {
		String uri = request.getRequestURI();

		// 分辨请求
		String method = uri.substring(uriLen);
		if (!method.startsWith("App/"))
			return false;
		int index = method.indexOf(";");
		if (index > 0)
			method = method.substring(4, index);
		else
			method = method.substring(4);
		// 获取session信息
		@SuppressWarnings("unchecked")
		Map<String, String> session = (Map<String, String>) request
				.getSession().getAttribute("session");
		log.info("Session: " + session + " ~ Method: " + method);
		// 游客
		if (session == null) {
			session = new HashMap<String, String>();
			session.put("userType", "0");
			request.getSession().setAttribute("session", session);
//			try {
//				response.getOutputStream().write(("JSESSIONID=" + request.getSession().getId()).getBytes());
//			} catch (IOException e) {
//				log.error("赋予Session！", e);
//			}
		}
		// 鉴权
		if (method.startsWith("img"))
			method = "img";
		if (!authentication(method, session.get("userType"))) {
			try {
				response.getOutputStream().write(
						"{\"ret\":\"-1\",\"rtmsg\":\"无权限\"}".getBytes("UTF-8"));
			} catch (Exception e) {
				log.error("无权限应答错误！", e);
			}
			return true;
		}
		// Url带参，图片下载情况
		if (method.startsWith("img"))
			return false;

		Tree<String> result = new Tree<String>("root");
		// 取业务配置
		Map<String, String> cfg = Config.getM(method);
		if (cfg == null) {
			try {
				response.getOutputStream()
						.write("{\"ret\":\"-2\",\"rtmsg\":\"无此业务\"}"
								.getBytes("UTF-8"));
			} catch (Exception e) {
				log.error("无此业务应答错误！", e);
			}
			return true;
		}

		Long id = Thread.currentThread().getId();
		try {
			// 特殊方法（非Json，流式数据）
			if (cfg.containsKey("Sys_Stream")) {
				iss.put(id, request.getInputStream());
			} else {
				// 获取Json
				int len = request.getContentLength();
				InputStream is;
				if (len > 0) {
					is = request.getInputStream();
					byte[] b = read(len, is);
					result = Json.getTree(new String(b, "UTF-8"));
				}
			}
			result.put("ret", "0");
			// 解包处理
			MsgChange.getResultMapSelf(result, cfg, session);

			if (!result.get("ret").equals("0")) {
				cfg = Config.getM(method + "_");
				response.getOutputStream().write(getMsg(result, cfg));
				return true;
			}
			List<String> sqls = dataCtrl(result, cfg);
			// 分支处理
			// for (int i = 0;; i++) {
			// cfg = Config.getM(method + i);
			// if (cfg == null)
			// break;
			// }

			cfg = Config.getM(method + "_");
			if (cfg.containsKey("Sys_Stream")) {
				oss.put(id, response.getOutputStream());
			}
			// 组包处理
			MsgChange.getResultMapOther(result, cfg, session);

			if (!result.get("ret").equals("0")) {
				response.getOutputStream().write(getMsg(result, cfg));
				return true;
			}
			sqls.addAll(dataCtrl(result, cfg));

			if (!DBCtrl.submit(sqls.toArray(new String[] {}))) {
				try {
					response.getOutputStream().write(
							"{\"ret\":\"-4\",\"rtmsg\":\"数据库错误\"}"
									.getBytes("UTF-8"));
				} catch (Exception e) {
					log.error("数据库错误！", e);
				}
				return true;
			}

			if (!cfg.containsKey("Sys_Stream"))
				response.getOutputStream().write(getMsg(result, cfg));
		} catch (Exception e) {
			log.error("业务处理出错！", e);
		} finally {
			iss.remove(id);
			oss.remove(id);
		}
		return true;
	}

	/**
	 * 鉴权
	 * 
	 * @param method
	 * @param userType
	 * @return 是否有此操作权限
	 */
	private static boolean authentication(String method, String userType) {
//		return true;
		return at.get(userType).contains(method);
	}

	private static byte[] read(int len, InputStream is) throws IOException {
		byte[] b = new byte[len];
		int num = 0;
		while (num < len) {
			num += is.read(b, num, len - num);
		}
		return b;
	}

	// 暂时仅支持一级节点过滤
	public static byte[] getMsg(Tree<String> result, Map<String, String> cfg)
			throws Exception {
		log.info(result);
		for (Tree<String> tmp : result.listBranchs()) {
			if (!cfg.containsKey(tmp.getName()))
				result.removeBranch(tmp);
		}
		return result.toString().getBytes("UTF-8");
	}

	private static final String[] str = new String[] { "Sys_dataMethod",
			"Sys_dataBase", "Sys_dataKey", "Sys_dataCol" };

	private static List<String> dataCtrl(Tree<String> resultMap,
			Map<String, String> config) {
		List<String> l = new ArrayList<String>();
		for (int i = 0;; i++) {
			String method = config.get(str[0] + i);
			if (method == null)
				break;
			if (method.equals("insert")) {
				l.add(DBCtrl.insert(config.get(str[1] + i), resultMap));
			}
			if (method.equals("update")) {
				l.add(DBCtrl.update(config.get(str[1] + i), resultMap, config
						.get(str[2] + i).split("/"), config.get(str[3] + i)
						.split("/")));
			}
		}
		return l;
	}
}
