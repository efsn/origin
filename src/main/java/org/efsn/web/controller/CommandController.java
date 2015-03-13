package org.efsn.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import template.bean.User;

@Controller
public class CommandController{
    
    @RequestMapping("/command.do")
    public ModelAndView handle(@ModelAttribute("user") User user)throws Exception{
        return new ModelAndView("register").addObject(user);
    }
    
    @ModelAttribute("user")
    public User getUser(){
        return new User();
    }
    
    

}
