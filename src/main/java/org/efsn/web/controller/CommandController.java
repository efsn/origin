package org.efsn.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import template.bean.User;

public class CommandController extends AbstractCommandController{
    
    public CommandController(){
        this.setCommandClass(User.class);
    }

    @Override
    protected ModelAndView handle(HttpServletRequest req,
                                  HttpServletResponse resp,
                                  Object obj,
                                  BindException e)throws Exception{
        User user = (User)obj;
        ModelAndView mv = new ModelAndView();
        mv.setViewName("command");
        mv.addObject(user);
        mv.addObject(user);
        return mv;
    }
    

}
