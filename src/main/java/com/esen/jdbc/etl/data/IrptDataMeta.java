package com.esen.jdbc.etl.data;

import java.io.Serializable;
import java.util.ArrayList;



/**
 * irptserver   dataset 的信息结构
 * @author jzp
 *
 */
public class IrptDataMeta implements Serializable {
  private String _name = null;
  private ArrayList _list = null;
  private ArrayList _namelist = null;
  private IrptDataMeta(){
    
  }
  /**
   *  
   * @param nm DataMeta名字
   */
  public IrptDataMeta(String nm){
    _name = nm;
    _list = new ArrayList(10);
    _namelist = new ArrayList(10);
  }
  public String getName(){
    return _name;
  }
  /**
   * 获得字段数
   */
  public int getFieldCount(){
    return _list.size();
  }
  /**
   * 获得某个字段的详细信息
   * @return
   */
  public IrptFieldInfo getFieldInfo(int index){
    return (IrptFieldInfo)_list.get(index);
  }
  
  public void addFieldInfo(IrptFieldInfo fldInfo){
    _namelist.add(fldInfo.FIELD_NAME.toUpperCase());
    _list.add(fldInfo);
  }
  /**
   * 获得字段名对应的index
   * @param fldname
   * @return
   */
  public int getFieldIndex(String fldname){
    return _namelist.indexOf(fldname.toUpperCase());
  }
}
