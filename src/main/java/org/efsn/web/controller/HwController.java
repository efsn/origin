package org.efsn.web.controller;

import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.Controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Chan
 * @version 1.0
 * Created on 2014/11/10
 */
public class HwController implements Controller{
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

}
