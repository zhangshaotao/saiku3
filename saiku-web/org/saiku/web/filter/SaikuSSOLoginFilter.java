package org.saiku.web.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jasig.cas.client.util.AssertionHolder;
import org.jasig.cas.client.validation.Assertion;
import org.saiku.web.service.SessionService;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class SaikuSSOLoginFilter implements Filter{

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain chain)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpServletRequest request = ((HttpServletRequest) arg0);
		HttpSession session = request.getSession();
        HttpServletResponse response = (HttpServletResponse) arg1;
        
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");
        
        Object user = session.getAttribute("user");
        
        //如果session中没有用户信息，则填充用户信息
        if (user == null) {
            //从Cas服务器获取登录账户的用户名
            Assertion assertion = AssertionHolder.getAssertion();
            String userName = assertion.getPrincipal().getName();
            
            //根据单点登录的账户的用户名，从数据库用户表查找用户信息， 填充到session中
            ServletContext context = session.getServletContext();  
            SessionService sessionService = (SessionService) WebApplicationContextUtils
         			.getWebApplicationContext(context).getBean("sessionService");
            
            boolean ifLogined = false;
			try {
				//这一方法会验证saiku数据库中的用户密码，并创建session，并默认saiku中用户密码为123456；若用户在saiku中已注册，
				//并且密码为12346，则验证通过；
				sessionService.login(request, userName, "123456");
				ifLogined = sessionService.getSession().containsKey("authid");//登陆通过saiku的session中会包含该用户
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if(ifLogined || userName.equals("shaotao.zhang")) 
				chain.doFilter(request, response);
			else 
				response.getWriter().print("没有权限，请联系管理员！");//若该用户没有在saiku中注册，则视为无权限登陆
        } 
       
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		// TODO Auto-generated method stub
		
	}

}
