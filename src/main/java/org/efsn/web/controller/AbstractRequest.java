package org.efsn.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractRequest{

    public abstract ModelAndView res(HttpServletRequest request, HttpServletResponse response) throws Exception;

    @RequestMapping()
    public ModelAndView handle(HttpServletRequest request, HttpServletResponse response) throws Exception{
        return res(request, response);
    }
    
}
