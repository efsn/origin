package org.efsn.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.LastModified;
import org.springframework.web.servlet.support.WebContentGenerator;

/**
 * @author Codeyn
 * @version 1.0
 * Created on 2014/11/10
 */

@Controller
@RequestMapping("/hello.do")
public class HwController extends WebContentGenerator implements LastModified, Response{
    
    private long lastModified;

    @Override
    public ModelAndView res(HttpServletRequest request, HttpServletResponse response) throws Exception{
        response.getWriter().write("<a href='http://www.baidu.com'>this</a>");
        return null;
    }

    @Override
    public long getLastModified(HttpServletRequest request){
        if(lastModified == 0){
            lastModified = System.currentTimeMillis();
        }
        return lastModified;
    }

}
