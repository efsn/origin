package org.demo.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

public class RegisterInterceptor extends HandlerInterceptorAdapter{
    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response, Object handler) throws Exception{
        if(request.getContextPath().startsWith("creat.do")){
            return true;
        }
        //TODO better is cookie
        if(request.getSession().getAttribute("username") != null){
            return true;
        }
        return false;
    }
}
