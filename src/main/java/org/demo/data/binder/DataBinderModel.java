package org.demo.data.binder;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.demo.data.PhoneNumber;
import org.demo.data.SchoolInfo;
import org.demo.data.UserState;
import org.demo.data.formatter.PhoneNumberA;
import org.springframework.format.annotation.DateTimeFormat;

public class DataBinderModel{
    
    @NotNull(message="{username.not.empty}")
    @Size(min=5, max=20, message="{username.length}")
    @Pattern(regexp="^[a-zA-Z_]\\w{4,19}$", message="{username.patterns}")
    private String username;
    
    private String password;
    private boolean bool;
    private SchoolInfo sInfo;
    private List list;
    private Map map;
    
    @PhoneNumberA
    private PhoneNumber phoneNumber;
    
    @DateTimeFormat(pattern="yyyy-MM-ddHH:mm:ss")
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
    public String getPassword(){
        return password;
    }
    public void setPassword(String password){
        this.password = password;
    }
    
}
