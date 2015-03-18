package com.esen.jdbc.data;

public class DbPool {
  private String name;
  private String driverclass;
  private String url;
  private String user;
  private String password;
  public DbPool(String name,String driverclass,String url,
      String user,String password){
    this.name = name;
    this.driverclass = driverclass;
    this.url = url;
    this.user = user;
    this.password = password;
  }
  public String getName(){
    return name;
  }
  public String getDriverclass() {
    return driverclass;
  }
  public String getUrl() {
    return url;
  }
  public String getUser() {
    return user;
  }
  public String getPassword() {
    return password;
  }
  
}
