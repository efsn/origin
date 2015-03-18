package com.esen.jdbc.etl.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * irptuserdata的个list
 * @author jzp
 *
 */
public class IrptUserDataList implements Serializable {
  private ArrayList list = null;
  private String _taskid = null;
  private HashMap keys = null;
  IrptUserDataMeta _userDataMeta = null;
  
  public IrptUserDataList(IrptUserDataMeta meta){
    _userDataMeta = meta;
    list = new ArrayList(100);
    keys = new HashMap(3000);
  }
  
  public int getCount(){
    return list.size();
  }
  
  public void addData(IrptUserData data){
    list.add(data);
    keys.put(formatKey(data.getBbq(),data.getUserId(),data.getBtype()), new Integer(list.size()-1));
  }
  
  public IrptUserData getData(int index){
    return (IrptUserData)list.get(index);
  }
  public void  setUserDataMeta(IrptUserDataMeta dataMeta){
    _userDataMeta = dataMeta;
  }
  
  public IrptUserDataMeta getUserDataMeta(){
    return _userDataMeta;
  }
  
  public void firstAllUserData(){
    for(int i=0 ;i< this.getCount() ;++i){
      this.getData(i).firstAllData();
    }
  }
  
  public String getTaskId(){
    return _taskid;
  }
  
  public void setTaskId(String value){
    _taskid = value;
  }
  
  public IrptUserData getNewData(){
    IrptUserData data = new IrptUserData(_userDataMeta);
    data.setTaskId(_taskid);
    addData(data);
    return data;
  }
  

  private String formatKey(String bbq,String bbh,String btype){
    StringBuffer buf = new StringBuffer(50);
    buf.append(bbq).append(";").append(bbh).append(";").append(btype);
    return buf.toString();
  }

  
  //根据关键字查找userdata
  public IrptUserData findUserData(String key){
    Integer index = (Integer)keys.get(key);
    if (index!=null)
      return getData(index.intValue());
    else
      return null;
  }
  public IrptUserData findUserData(String bbq,String bbh,String btype){
    return findUserData(formatKey(bbq,bbh,btype));
  }
  /**
   * 清空所有的数据
   *
   */
  public void clear(){
    list.clear();
    keys.clear();
    list.trimToSize();
  }
}
