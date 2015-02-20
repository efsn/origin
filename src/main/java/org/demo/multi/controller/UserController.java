package org.demo.multi.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.util.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.multiaction.MultiActionController;

import template.bean.User;

public class UserController extends MultiActionController{
    
    private UserService userService;
    
    private String createView;
    private String updateView;
    private String deleteView;
    private String listView;
    private String redirectToListView;
    
    
    public ModelAndView create(HttpServletRequest request, HttpServletResponse response, User user){
        if("GET".equals(request.getMethod())){
            //jump to create view
            ModelAndView mv = new ModelAndView(this.getCreateView());
            mv.addObject(this.getCommandName(user), user);
            
            BindException errors = new BindException(user, getCommandName(user));
            if(!StringUtils.hasLength(user.getUsername())){
                errors.rejectValue("username", "username.not.empty", "Please enter your usename");
            }
            if(errors.hasErrors()){
                mv.addAllObjects(errors.getModel());
            }
            
            return mv;
        }
        userService.create(user);
        return new ModelAndView(this.getRedirectToListView());
    }
    
    public ModelAndView update(HttpServletRequest request, HttpServletResponse response, User user){
        if("GET".equals(request.getMethod())){
            //jump to update view
            ModelAndView mv = new ModelAndView(this.getUpdateView());
            mv.addObject(this.getCommandName(user), user);
            return mv;
        }
        userService.update(user);
        return new ModelAndView(this.getRedirectToListView());
    }
    
    public ModelAndView list(HttpServletRequest request, HttpServletResponse response, User user){
        ModelAndView mv = new ModelAndView(getListView());
        mv.addObject("map", userService.getMap());
        return mv;
    }

    
    protected String getCommandName(Object obj){
        return "command";
    }
    
    public UserService getUserService(){
        return userService;
    }

    public void setUserService(UserService userService){
        this.userService = userService;
    }

    public String getCreateView(){
        return createView;
    }

    public void setCreateView(String createView){
        this.createView = createView;
    }

    public String getUpdateView(){
        return updateView;
    }

    public void setUpdateView(String updateView){
        this.updateView = updateView;
    }

    public String getDeleteView(){
        return deleteView;
    }

    public void setDeleteView(String deleteView){
        this.deleteView = deleteView;
    }

    public String getListView(){
        return listView;
    }

    public void setListView(String listView){
        this.listView = listView;
    }

    public String getRedirectToListView(){
        return redirectToListView;
    }

    public void setRedirectToListView(String redirectToListView){
        this.redirectToListView = redirectToListView;
    }
    
}
