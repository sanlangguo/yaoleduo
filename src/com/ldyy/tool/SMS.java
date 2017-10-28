package com.ldyy.tool;

import java.io.BufferedReader;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class SMS {
	public static void main(String[] args) {
		try {
			sendSMS("15538055695,15538127989,15514575752","短信延迟2分钟以上，丢失率很高【药乐多】","");
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/*
	 * 发送方法  
	 */
	public static int sendSMS(String Mobile,String Content,String send_time) throws MalformedURLException, UnsupportedEncodingException {
		URL url = null;
		String CorpID="WAS004228";//账户名
		String Pwd="hualin@";//密码
		String send_content=URLEncoder.encode(Content.replaceAll("<br/>", " "), "GBK");//发送内容
//		url = new URL("https://sdk2.028lk.com/sdk2/BatchSend2.aspx?CorpID="+CorpID+"&Pwd="+Pwd+"&Mobile="+Mobile+"&Content="+send_content+"&Cell=&SendTime="+send_time);
		url = new URL("http://mb345.com:999/ws/BatchSend2.aspx?CorpID="+CorpID+"&Pwd="+Pwd+"&Mobile="+Mobile+"&Content="+send_content+"&Cell=&SendTime="+send_time);
		BufferedReader in = null;
		int inputLine = 0;
		try {
			System.out.println("开始发送短信手机号码为 ："+Mobile);
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			inputLine = new Integer(in.readLine()).intValue();
		} catch (Exception e) {
			System.out.println("网络异常,发送短信失败！");
			inputLine=-2;
		}
		System.out.println("结束发送短信返回值：  "+inputLine);
		return inputLine;
	}
}
