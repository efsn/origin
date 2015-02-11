package org.demo.data.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

public class ErrorController extends AbstractCommandController{
    
    private String errorView;
    
    @Override
    protected ModelAndView handle(HttpServletRequest request,
                                  HttpServletResponse response, 
                                  Object command, 
                                  BindException errors)throws Exception{
        errors.reject("username.not.empty");
        errors.reject("username.not.empty1", "Username not empty1");
        errors.reject("username.length.error", new Object[]{6, 10}, "Username length should six to ten");
        return new ModelAndView(getErrorView(), errors.getModel());
    }

    public String getErrorView(){
        return errorView;
    }

    public void setErrorView(String errorView){
        this.errorView = errorView;
    }
    
}
