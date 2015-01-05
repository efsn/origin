package com.blue.sys.servlet.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class LoginFilter implements Filter {
    public void init(FilterConfig filterConfig) {
        System.out.println(filterConfig.getInitParameter("loger"));
    }

    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();
        if(null != session.getAttribute("user")){
            chain.doFilter(request, response);
        }
        else{
            HttpServletResponse res = (HttpServletResponse) response;
            res.sendRedirect("/sys/index.jsp");
        }
    }

    public void destroy() {
        System.out.println("Release AuthorLoginFilter");
    }

}
