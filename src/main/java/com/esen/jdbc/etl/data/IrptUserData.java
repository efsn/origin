package com.esen.jdbc.etl.data;

import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class IrptUserData implements Serializable {
  private IrptUserDataMeta _userDataMeta = null;
  private String _taskid ,_userid,_username,_btype,_bbq,_upid;
  private ArrayList _tables = null;
  private ArrayList _tablesnames = null;
  
  private void initTables(){
    if (_userDataMeta==null) return;
    _tables  = new ArrayList(_userDataMeta.getDataMetaCount());
    _tablesnames =  new ArrayList(_userDataMeta.getDataMetaCount());
    
  }
  public IrptUserData(IrptUserDataMeta dataMeta){
    _userDataMeta = dataMeta;
    initTables();
  }
  public void  setUserDataMeta(IrptUserDataMeta dataMeta){
    _userDataMeta = dataMeta;
    initTables();
  }
  
  public IrptUserDataMeta getUserDataMeta(){
    return _userDataMeta;
  }
  
  public String getTaskId(){
    return _taskid;
  }
  
  public void setTaskId(String value){
    _taskid = value;
  }
  
  public String getUserId(){
    return _userid;
  }
  public void setUserId(String value ){
    _userid = value;
  }
  /**
   * 获得当前户名字 
   */
  public String getUserName(){
    return _username;
  }
  public void setUserName(String value){
    _username = value;
  }
  /**
   * 获得报表户类型
   * @return
   */
  public String getBtype(){
    return _btype;
  }
  public void setBtype(String value){
    _btype= value;
  }
  /**
   * 获得报表期
   * @return
   */
  public String getBbq(){
    return _bbq;
  }
  public void setBbq(String value){
    _bbq = value;
  }
  /**
   * 获得上级代码
   * @return
   */
  public String getUpid(){
    return _upid;
  }
  
  public void setUpid(String value){
    _upid = value;
  }
  
  public String[] getTableNames(){
    String [] t = new String[_tablesnames.size()];
    _tablesnames.toArray(t);
    return t;
  }
  
  public IrptTableData getTable(int index){
    return (IrptTableData)_tables.get(index);
  }
  
  public IrptTableData2 getTable2(int index){
    return (IrptTableData2)_tables.get(index);
  }
  public IrptTableData getTable(String name){
    int index = _tablesnames.indexOf(name); 
    return (IrptTableData)_tables.get(index);
  }
  
  public IrptTableData2 getTable2(String name){
    int index = _tablesnames.indexOf(name);
    if (index ==-1) 
        return null;
    return (IrptTableData2)_tables.get(index);
  }  
  public int getTableCount(){
    return _tables.size();
  }
  
  public void addTableData(IrptTableData data){
    _tablesnames.add(data.getName());
    _tables.add(data);
  }
  public void addTableData2(IrptTableData2 data2){
    _tablesnames.add(data2.getName());
    _tables.add(data2);
  }  
  public void firstAllData(){
    for(int i =0 ;i <getTableCount();++i){
      IrptTableData2 data2 = getTable2(i);
      if (data2== null) continue;
      data2.reset();
      data2.next();
    }
    
  }
}
