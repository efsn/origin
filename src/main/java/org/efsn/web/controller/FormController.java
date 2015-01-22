package org.efsn.web.controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.servlet.mvc.SimpleFormController;

import template.bean.User;

public class FormController extends SimpleFormController{
    @Override
    protected void doSubmitAction(Object command) throws Exception{
        User user = (User)command;
        System.out.println(user.getUsername() + ":" + user.getPassword());
    }
    
    @Override
    protected Object formBackingObject(HttpServletRequest request)
            throws Exception{
        User user = new User();
        user.setUsername("Please enter your username!");
        return user;
    }
    
    @Override
    protected Map referenceData(HttpServletRequest request) throws Exception{
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("address", Arrays.asList("New York, Hong Kong"));
        return map;
    }
    
}
