package com.blue.sys.servlet.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class EncodingFilter implements Filter {
    public void init(FilterConfig filterConfig) {
        System.out.println(filterConfig.getInitParameter("charset"));
    }

    public void doFilter(ServletRequest request,
            ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        String charset = request.getContentType();
        if(!"utf-8".equals(charset)){
            try{
                request.setCharacterEncoding("utf-8");
                chain.doFilter(request, response);
            } catch(UnsupportedEncodingException e){
                e.printStackTrace();
            }
        }
        else{
            chain.doFilter(request, response);
        }
    }

    public void destroy() {
        System.out.println("Release EncodingFilter");
    }
}
