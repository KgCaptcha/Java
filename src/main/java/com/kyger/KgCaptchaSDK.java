/*
 * JSP/Java v11
 * KgCaptcha v1.0.0
 * http://www.KgCaptcha.com
 *
 * Copyright © 2022 Kyger. All Rights Reserved.
 * http://www.kyger.com.cn
 *
 * Copyright © 2022 by KGCMS.
 * http://www.kgcms.com
 *
 * Date: Thu May 20 15:28:23 2022
*/
package com.kyger;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class KgCaptchaSDK {
    public String appCdn = "https://cdn9.kgcaptcha.com"; // 风险防控服务URL
    public String appId; // 公钥
    public String appSecret; // 秘钥

    public int connectTimeout = 5;  // 连接超时断开请求，秒

    public String clientIp = "";  //  客户端IP，安全等级为1和2时必须设置
    public String clientBrowser = "";  // 客户端浏览器信息，安全等级为1和2时必须设置
    public String userId;  // 用户手机号/ID/登录用户名，安全等级为2时必须设置


    public String domain;  // 授权域名，当前应用域名
    public String token;  // 前端验证成功后颁发的 token

    private int time;  // 当时时间戳
    private Map<String, String> data;  // 请求数据包
    private String dataURL = "";  // 提交的数据包
	
    // 获取客户端信息需要 request 对象：客户IP/浏览器信息/来路
    // 如果没有定义 request 对象，请手动定义 clientIp|clientBrowser|domain
    public HttpServletRequest request = null;  
    
	// 构造函数
	public KgCaptchaSDK(String appId, String appSecret) {
        this.appId = appId;
        this.appSecret = appSecret;
        this.time = (int)(System.currentTimeMillis() / 1000);
	}
	
	
	public Map<String, String> sendRequest() {
        String PostUrl = this.signUrl();
        String PostData = this.dataURL;
        
		OutputStreamWriter out = null ;
        BufferedReader in = null;  
        String result = ""; 
        try {  
            URL realUrl = new URL(PostUrl);  
            URLConnection conn = realUrl.openConnection();  
            conn.setRequestProperty("accept", "*/*");  
            conn.setRequestProperty("connection", "Keep-Alive");  
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("ContentType", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Referer", this.domain);
            conn.setDoOutput(true);  
            conn.setDoInput(true); // POST
            conn.setReadTimeout(this.connectTimeout * 1000);
            conn.setConnectTimeout(this.connectTimeout * 1000);
            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");  
            out.write(PostData);
            out.flush();  
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(),"UTF-8"));  
            String line;  
            while ((line = in.readLine()) != null) {  
                result += line;  
            }  
        } catch (Exception e) {
        	result = "{ \"code\": 20000, \"msg\": \"" + String.valueOf(e) + "\"}";
        } finally { 
            try {  
                if (out != null) {  
                    out.close();  
                }  
                if (in != null) {  
                    in.close();  
                }  
            } catch (IOException ex) {  
            	result = "{ \"code\": 20000, \"msg\": \"" + String.valueOf(ex) + "\"}";
            }  
        }
             
        Map<String, String> dict = new HashMap<String, String>();
        dict.put("code", substr(result, "code\":", ",", 0).trim());
        dict.put("msg", substr(result, "msg\": \"", "\"", 2).trim());
        return dict; 
    }
	
	public static String substr(String str, String start, String end, Integer flag) {		
		String r;
		switch(flag){
			case 0, 1:
				r = str.substring(str.indexOf(start), str.indexOf(end));
				break;
			case 2:
				r = str.substring(str.indexOf(start), str.lastIndexOf(end));
				break;
			case 3:
				r = str.substring(0, str.indexOf(start));
				break;
			case 4:
				r = str.substring(str.indexOf(start));
				break;
			default:
				r = "ParameterError";
				break;
		}
		
		if(Arrays.asList(new Integer[]{0, 2, 4}).contains(flag)) {  
			r = r.substring(start.length()); 
		}
		if(flag == 1) {
			r += end;
		}	
		return r;
	}
	
	
	public static String md5(String str) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(str.getBytes());
			return new BigInteger(1, md.digest()).toString(16);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
		
    // 生成签名 URL
    public String signUrl() {
    	String data = "";
        this.data = this.putData();       
        for(Map.Entry<String, String> e: this.data.entrySet()) {
        	data += e.getKey() +  e.getValue();
        	this.dataURL += "&" + e.getKey() + "=" +  e.getValue();
        }
        // System.out.println(data);
        // System.out.println(md5(this.appId + data + this.appSecret));
        return this.appCdn + "/requestBack?appid=" + this.appId + "&sign=" + md5(this.appId + data + this.appSecret);
    }
    
	// 数据包
	public Map<String, String> putData() {
        // 如果没有定义 KgCaptchaSDK.request，请手动定义：clientIp|clientBrowser|domain
        if(this.request != null) {
        	this.clientIp = this.getIP();
        	this.clientBrowser = this.request.getHeader("User-Agent");
        	this.domain = this.request.getScheme() + "://" + this.request.getServerName();
        }			
			
		Map<String, String> r = new LinkedHashMap<String, String>(){
			private static final long serialVersionUID = 1L;
		};
		r.put("ip", this.clientIp);
		r.put("browser", this.clientBrowser);
		r.put("time", String.valueOf(this.time));
		r.put("uid", this.userId);
		r.put("timeout", String.valueOf(this.connectTimeout));
		r.put("token", this.token);
		return r;
	}
	
	// 获取客户端IP
	public String getIP() {
	    String ip = this.request.getHeader("x-forwarded-for");
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	        ip = this.request.getHeader("Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	        ip = this.request.getHeader("WL-Proxy-Client-IP");
	    }
	    if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
	        ip = this.request.getRemoteAddr();
	    }
	    if (ip.contains(",")) {
	        return ip.split(",")[0];
	    } else {
	        return ip.equals("0:0:0:0:0:0:0:1") ? "127.0.0.1" : ip;
	    }
	}
}
