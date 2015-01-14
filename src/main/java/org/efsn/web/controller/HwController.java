package org.efsn.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;
import org.springframework.web.servlet.mvc.LastModified;

/**
 * @author Chan
 * @version 1.0
 * Created on 2014/11/10
 */
public class HwController extends AbstractController implements LastModified{
    
    private long lastModified;
    
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception{
        // get/valid param
        // bind param to command obj
        // next page
        ModelAndView mav = new ModelAndView();
        // add model data, any pojo obj
        mav.addObject("msg", "Fuck Hellow World!");
        // set logic view name
        mav.setViewName("hello");
        return mav;
    }

    @Override
    protected ModelAndView handleRequestInternal(HttpServletRequest request, HttpServletResponse response) throws Exception{
        /*
         * get/valid parameter
         * bind args to command obj
         * next page
         */
        
        
        ModelAndView mv = new ModelAndView();
        mv.addObject("msg", "Fuck hello World!");
        mv.setViewName("hello");
        return mv;
        
//        response.getWriter().write("<a href='http://www.baidu.com'>this</a>");
//        return null;
        
    }

    @Override
    public long getLastModified(HttpServletRequest request){
        if(lastModified == 0){
            lastModified = System.currentTimeMillis();
        }
        return lastModified;
    }

}
