package org.demo.multi.controller;

import java.util.HashMap;
import java.util.Map;

import template.bean.User;

public class UserService{
    private Map<String, User> map = new HashMap<String, User>();
    
    public void create(User user){
        map.put(user.getUsername(), user);
    }
    
    public void update(User user){
        map.put(user.getUsername(), user);
    }

    public Map<String, User> getMap(){
        return map;
    }

    public void setMap(Map<String, User> map){
        this.map = map;
    }
    

}
