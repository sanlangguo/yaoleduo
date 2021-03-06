package com.ldyy;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.ldyy.module.MsgFlow;
import com.ldyy.tool.Config;
import com.ldyy.tool.DBCon;

public class DataEntry implements Filter {
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		System.out.println(System.getProperty("user.dir"));
		Config.init();
		DBCon.init();
		MsgFlow.init();
	}

	@Override
	public void destroy() {}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain fc) throws IOException, ServletException {
		// --------------------------------------------------------
		// getHeaderNames(打印整个http请求消息)
		HttpServletRequest rqt = (HttpServletRequest) request;
//		HttpServletResponse rsp = (HttpServletResponse) response;
//		rsp.setHeader("Access-Control-Allow-Origin", "*");
//		rsp.setHeader("Access-Control-Allow-Methods",
//				"POST, GET, PUT, OPTIONS, DELETE");
//		rsp.setHeader("Access-Control-Max-Age", "3600");
//		rsp.setHeader("Access-Control-Allow-Headers",
//				"x-requested-with, Content-Type");
//		rsp.setHeader("Access-Control-Allow-Credentials", "true");
		System.out.println(rqt.getMethod() + " " + rqt.getRequestURI());
//		if (rqt.getMethod().equals("OPTIONS")) {
//			fc.doFilter(request, response);
//		}
		// --------------------------------------------------------
		if (!MsgFlow.control((HttpServletRequest) request,
				(HttpServletResponse) response))
			fc.doFilter(request, response);
	}
}
