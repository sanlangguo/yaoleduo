package com.ldyy.module;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import com.ldyy.data.Tree;

public abstract class MsgChange {
	// ！用$前缀存储方法，无需substring(1)
	private static Map<String, Map<String, Method>> allMethod = new ConcurrentHashMap<String, Map<String, Method>>();
	private static Map<String, Object> allObject = new ConcurrentHashMap<String, Object>();

	private static Logger log = Logger.getLogger(MsgChange.class);

	public static synchronized void init(String path) {
		if (allObject.containsKey(path)) {
			return;
		}
		try {
			Object obj = Class.forName(path).newInstance();

			Method[] ms = obj.getClass().getDeclaredMethods();
			Map<String, Method> map = new HashMap<String, Method>();
			for (Method m : ms) {
				map.put(m.getName(), m);
			}
			allMethod.put(path, map);
			allObject.put(path, obj);
		} catch (Exception e) {
			log.error("找不到配置的特殊业务处理函数类", e);
		}
	}

	// 解包
	public static void getResultMapSelf(Tree<String> resultMap,
			Map<String, String> config, Map<String, String> session) {
		String rstr = config.get("Sys_Object");
		Object sm = allObject.get(rstr);
		if (sm == null) {
			init(rstr);
			sm = allObject.get(rstr);
		}
		Map<String, Method> methods = allMethod.get(rstr);
		for (int i = 0;; i++) {
			if (config.containsKey("Sys_B_method" + i)) {
				rstr = config.get("Sys_B_method" + i);
				if (rstr.startsWith("$")) {
					rstr = rstr.substring(1);
					String[] tmp = rstr.split(":", 2);
					try {
						if (methods.containsKey(tmp[0])) {
							if (tmp.length > 1) {
								if (tmp[1].startsWith("~")) {
									methods.get(tmp[0]).invoke(sm, resultMap,
											null,
											resultMap.get(tmp[1].substring(1)));
								} else {
									methods.get(tmp[0]).invoke(sm, resultMap,
											null, tmp[1]);
								}
							} else {
								methods.get(tmp[0]).invoke(sm, resultMap, null);
							}
						}
						if (resultMap.get("Sys_closeFlag") != null) {
							resultMap.remove("Sys_closeFlag");
							return;
						}
					} catch (Exception e) {
						log.error(tmp[0] + "系统业务配置方法有误", e);
						resultMap.put("ret", "-3");// ?
						resultMap.put("rtmsg", "系统业务配置方法有误");
						return;
					}
				} else if (rstr.startsWith("#")) {
					// 存入session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					session.put(strs[1], resultMap.get(strs[0]));
				} else if (rstr.startsWith("&")) {
					// 读取session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.get(strs[0]));
				} else if (rstr.startsWith("*")) {
					// 取出session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.remove(strs[0]));
				}
			} else {
				break;
			}
		}

		for (String str : config.keySet()) {
			if (str.startsWith("Sys_"))
				continue;
			rstr = config.get(str);
			if (rstr.startsWith("$")) {
				// 调用方法
				if (resultMap.get(str) != null) {
					rstr = rstr.substring(1);
					String[] tmp = rstr.split(":", 2);
					try {
						if (methods.containsKey(tmp[0])) {
							if (tmp.length > 1) {
								if (tmp[1].startsWith("~")) {
									methods.get(tmp[0]).invoke(sm, resultMap,
											resultMap.get(str),
											resultMap.get(tmp[1].substring(1)));
								} else {
									methods.get(tmp[0]).invoke(sm, resultMap,
											resultMap.get(str), tmp[1]);
								}
							} else {
								methods.get(tmp[0]).invoke(sm, resultMap,
										resultMap.get(str));
							}
						}
					} catch (Exception e) {
						log.error(tmp[0] + "系统业务配置方法有误", e);
						resultMap.put("ret", "-3");// ?
						resultMap.put("rtmsg", "系统业务配置方法有误");
						return;
					}
				}
			} else if (rstr.startsWith("~")) {
				// 向总线复制
				rstr = rstr.substring(1);
				resultMap.put(rstr, resultMap.get(str));
			} else if (rstr.startsWith("!")) {
				// do nothing
			} else if (rstr.startsWith("@")) {
				// 如果不存在,向总线置默认值
				rstr = rstr.substring(1);
				if (resultMap.get(str) == null) {
					resultMap.put(str, rstr);
				}
			} else {
				// 如果不存在,向总线置默认值
				if (resultMap.get(str) == null) {
					resultMap.put(str, rstr);
				}
			}
		}
		
		for (int i = 0;; i++) {
			if (config.containsKey("Sys_A_method" + i)) {
				rstr = config.get("Sys_A_method" + i);
				if (rstr.startsWith("$")) {
					rstr = rstr.substring(1);
					String[] tmp = rstr.split(":", 2);
					try {
						if (methods.containsKey(tmp[0])) {
							if (tmp.length > 1) {
								if (tmp[1].startsWith("~")) {
									methods.get(tmp[0]).invoke(sm, resultMap,
											null,
											resultMap.get(tmp[1].substring(1)));
								} else {
									methods.get(tmp[0]).invoke(sm, resultMap,
											null, tmp[1]);
								}
							} else {
								methods.get(tmp[0]).invoke(sm, resultMap, null);
							}
						}
						if (resultMap.get("Sys_closeFlag") != null) {
							resultMap.remove("Sys_closeFlag");
							return;
						}
					} catch (Exception e) {
						log.error(tmp[0] + "系统业务配置方法有误", e);
						resultMap.put("ret", "-3");// ?
						resultMap.put("rtmsg", "系统业务配置方法有误");
						return;
					}
				} else if (rstr.startsWith("#")) {
					// 存入session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					session.put(strs[1], resultMap.get(strs[0]));
				} else if (rstr.startsWith("&")) {
					// 读取session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.get(strs[0]));
				} else if (rstr.startsWith("*")) {
					// 取出session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.remove(strs[0]));
				}
			} else {
				break;
			}
		}
	}

	// 组包
	public static void getResultMapOther(Tree<String> resultMap,
			Map<String, String> config, Map<String, String> session) {
		String rstr = config.get("Sys_Object");
		Object sm = allObject.get(rstr);
		if (sm == null) {
			init(rstr);
			sm = allObject.get(rstr);
		}
		Map<String, Method> methods = allMethod.get(rstr);
		for (int i = 0;; i++) {
			if (config.containsKey("Sys_B_method" + i)) {
				rstr = config.get("Sys_B_method" + i);
				if (rstr.startsWith("$")) {
					rstr = rstr.substring(1);
					String[] tmp = rstr.split(":", 2);
					tmp[0] += "_";
					try {
						if (methods.containsKey(tmp[0])) {
							if (tmp.length > 1) {
								if (tmp[1].startsWith("~")) {
									methods.get(tmp[0]).invoke(sm, resultMap,
											resultMap.get(tmp[1].substring(1)));
								} else {
									methods.get(tmp[0]).invoke(sm, resultMap,
											tmp[1]);
								}
							} else {
								methods.get(tmp[0]).invoke(sm, resultMap);
							}
						}
						if (resultMap.get("Sys_closeFlag") != null) {
							resultMap.remove("Sys_closeFlag");
							return;
						}
					} catch (Exception e) {
						log.error(tmp[0] + "系统业务配置方法有误", e);
						resultMap.put("ret", "-3");// ?
						resultMap.put("rtmsg", "系统业务配置方法有误");
						return;
					}
				} else if (rstr.startsWith("#")) {
					// 存入session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					session.put(strs[1], resultMap.get(strs[0]));
				} else if (rstr.startsWith("&")) {
					// 读取session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.get(strs[0]));
				} else if (rstr.startsWith("*")) {
					// 取出session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.remove(strs[0]));
				}
			} else {
				break;
			}
		}

		for (String str : config.keySet()) {
			if (str.startsWith("Sys_"))
				continue;
			rstr = config.get(str);
			if (rstr.startsWith("$")) {
				rstr = rstr.substring(1);
				String[] tmp = rstr.split(":", 2);
				tmp[0] += "_";
				try {
					if (methods.containsKey(tmp[0])) {
						if (tmp.length > 1) {
							if (tmp[1].startsWith("~")) {
								resultMap.put(
										str,
										(String) methods.get(tmp[0]).invoke(
												sm,
												resultMap,
												resultMap.get(tmp[1]
														.substring(1))));
							} else {
								resultMap.put(str, (String) methods.get(tmp[0])
										.invoke(sm, resultMap, tmp[1]));
							}
						} else {
							resultMap.put(str, (String) methods.get(tmp[0])
									.invoke(sm, resultMap));
						}
					}
				} catch (Exception e) {
					log.error(tmp[0] + "系统业务配置方法有误", e);
					resultMap.put("ret", "-3");// ?
					resultMap.put("rtmsg", "系统业务配置方法有误");
					return;
				}
			} else if (rstr.startsWith("~")) {
				// 从总线中获得
				rstr = rstr.substring(1);
				resultMap.put(str, resultMap.get(rstr));
			} else if (rstr.startsWith("!")) {
				// change do nothing
			} else if (rstr.startsWith("@")) {
				// 强制使用固定值
				rstr = rstr.substring(1);
				resultMap.put(str, rstr);
			} else {
				// 如果总线不存在,置默认值
				if (resultMap.get(str) == null) {
					resultMap.put(str, rstr);
				}
			}
		}

		for (int i = 0;; i++) {
			if (config.containsKey("Sys_A_method" + i)) {
				rstr = config.get("Sys_A_method" + i);
				if (rstr.startsWith("$")) {
					rstr = rstr.substring(1);
					String[] tmp = rstr.split(":", 2);
					tmp[0] += "_";
					try {
						if (methods.containsKey(tmp[0])) {
							if (tmp.length > 1) {
								if (tmp[1].startsWith("~")) {
									methods.get(tmp[0]).invoke(sm, resultMap,
											resultMap.get(tmp[1].substring(1)));
								} else {
									methods.get(tmp[0]).invoke(sm, resultMap,
											tmp[1]);
								}
							} else {
								methods.get(tmp[0]).invoke(sm, resultMap);
							}
						}
						if (resultMap.get("Sys_closeFlag") != null) {
							resultMap.remove("Sys_closeFlag");
							return;
						}
					} catch (Exception e) {
						log.error(tmp[0] + "系统业务配置方法有误", e);
						resultMap.put("ret", "-3");// ?
						resultMap.put("rtmsg", "系统业务配置方法有误");
						return;
					}
				} else if (rstr.startsWith("#")) {
					// 存入session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					session.put(strs[1], resultMap.get(strs[0]));
				} else if (rstr.startsWith("&")) {
					// 读取session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.get(strs[0]));
				} else if (rstr.startsWith("*")) {
					// 取出session
					rstr = rstr.substring(1);
					String[] strs = rstr.split(":");
					resultMap.put(strs[1], session.remove(strs[0]));
				}
			} else {
				break;
			}
		}
	}
}
