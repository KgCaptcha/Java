package com.kyger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class demo extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    public demo() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
    	// 编码
    	request.setCharacterEncoding("utf-8");
    	response.setCharacterEncoding("utf-8");;
        response.setContentType("text/html; charset=utf-8");
        
        // 后台处理
        if (request.getMethod().equals("POST")){
	        String html, appId, appSecret, Token, cty;
	
            cty = request.getParameter("cty");
                        
            // 设置 AppId 及 AppSecret，在应用管理中获取
            if (cty == "1"){
                appId = "rA9qRcl6";
                appSecret = "6h75TuboCunnHNQhI5zzxZOZav0Wzf9e";
            } else if (cty == "2") {
                appId = "nC1sCjwg";
                appSecret = "Vq2okDtS8XqtRgCH2sR9SLq0A5eS30Cq";
            } else {
                appId = "4gXIWZzz";
                appSecret = "VqFz4RCxtzYu9IzvhvtiEQDdPrmkA7If";
            }
            
            // 填写你的 AppId 和 AppSecret，在应用管理中获取
	        KgCaptchaSDK KgRequest = new KgCaptchaSDK(appId, appSecret);
			
	
	        // 前端验证成功后颁发的 token，有效期为两分钟
			KgRequest.token = request.getParameter("kgCaptchaToken");
			// System.out.print(KgRequest.token);
	
	        // 填写应用服务域名，在应用管理中获取
			KgRequest.appCdn = "https://cdn9.kgcaptcha.com";
	
	        // 请求超时时间，秒
			KgRequest.connectTimeout = 5;
			
	        // 用户登录或尝试帐号，当安全策略中的防控等级为3时必须填写，一般情况下可以忽略
	        // 可以填写用户输入的登录帐号（如：request.getParameter("username"），可拦截同一帐号多次尝试等行为
			KgRequest.userId = "kgCaptchaDemo";
			
			// request 对象，当安全策略中的防控等级为3时必须填写，一般情况下可以忽略
			KgRequest.request = request;
			// java 环境中无法提供 request 对象，请分别定义：clientIp|clientBrowser|domain 参数，即：
			// KgRequest.clientIp = "127.0.0.1";  // 填写客户端IP
			// KgRequest.clientBrowser = "";  // 客户端浏览器信息
			// KgRequest.domain = "http://localhost";  // 你的授权域名或服务IP		
			
	        // 发送验证请求
			Map<String, String> requestResult = KgRequest.sendRequest();
	        if("0".toString().equals(requestResult.get("code"))) {
	            // 验签成功逻辑处理 ***
	
	            // 这里做验证通过后的数据处理
	            // 如登录/注册场景，这里通常查询数据库、校验密码、进行登录或注册等动作处理
	            // 如短信场景，这里可以开始向用户发送短信等动作处理
	            // ...
	
	            html = "<script>alert('验证通过');history.back();</script>";
	        } else {
	            // 验签失败逻辑处理
	        	html = "<script>alert(\"" + requestResult.get("msg") + " - " + requestResult.get("code") + "\");history.back();</script>";
	        }		
			
			response.getWriter().append(html);
        } else {
        	response.sendRedirect("index.html");
        }
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

}
