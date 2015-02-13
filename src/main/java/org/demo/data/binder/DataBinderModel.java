package org.demo.data.binder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.demo.data.PhoneNumber;
import org.demo.data.SchoolInfo;
import org.demo.data.UserState;

public class DataBinderModel{
    private String username;
    private boolean bool;
    private SchoolInfo sInfo;
    private List list;
    private Map map;
    private PhoneNumber phoneNumber;
    private Date date;
    private UserState state;
    
    
    public String getUsername(){
        return username;
    }
    public void setUsername(String username){
        this.username = username;
    }
    public boolean isBool(){
        return bool;
    }
    public void setBool(boolean bool){
        this.bool = bool;
    }
    public SchoolInfo getsInfo(){
        return sInfo;
    }
    public void setsInfo(SchoolInfo sInfo){
        this.sInfo = sInfo;
    }
    public List getList(){
        return list;
    }
    public void setList(List list){
        this.list = list;
    }
    public Map getMap(){
        return map;
    }
    public void setMap(Map map){
        this.map = map;
    }
    public PhoneNumber getPhoneNumber(){
        return phoneNumber;
    }
    public void setPhoneNumber(PhoneNumber phoneNumber){
        this.phoneNumber = phoneNumber;
    }
    public Date getDate(){
        return date;
    }
    public void setDate(Date date){
        this.date = date;
    }
    public UserState getState(){
        return state;
    }
    public void setState(UserState state){
        this.state = state;
    }
    
}
