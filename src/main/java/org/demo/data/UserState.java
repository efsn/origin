package org.demo.data;

public class UserState{
    
    private String state;

    public String getState(){
        return state;
    }

    public void setState(String state){
        this.state = state;
    }
    
    @Override
    public String toString(){
        return state;
    }
    
}
