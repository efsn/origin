package com.esen.jdbc.etl.data;

import java.io.Serializable;
import java.util.ArrayList;

import com.esen.util.StrFunc;
import com.esen.util.StringMap;

/**
 * ireport 任务的meta 信息 
 * @author jzp
 *
 */
public class IrptUserDataMeta implements Serializable {
  private ArrayList _list = null;
  private ArrayList _namelist = null;
  public IrptUserDataMeta(){
    _list = new ArrayList();
    _namelist = new ArrayList();
  }
  /**
   * 获得有多少个meta
   * @return
   */
  public int getDataMetaCount(){
    return _list.size();
  }
  
  public IrptDataMeta getIrptDataMeta(int i){
    return (IrptDataMeta)_list.get(i);
  }
  public IrptDataMeta getIrptDataMeta(String name){
    int index = _namelist.indexOf(name);
    return (IrptDataMeta)_list.get(index);
  }
  public void  addIrptDataMeta(IrptDataMeta dataMeta){
    _namelist.add(dataMeta.getName());
    _list.add(dataMeta);
  }
  /**
   * 从字符串载入此信息
   * @param str
   */
  public void loadFromString(String str){
    StringMap map = new StringMap(";",":");
    map.loadFromString(str);
    loadFromStringMap(map);
  }
  public void loadFromStringMap(StringMap map){
    String [] tablenames = map.getKeys();
    for(int i=0 ;i <tablenames.length;++i){
      String fields = map.getValue(tablenames[i]);
      if (StrFunc.isNull(fields)) continue;
      IrptDataMeta datameta = new IrptDataMeta(tablenames[i]);
      this.addIrptDataMeta(datameta);
      String [] flds = fields.split(",");
      for(int j =0 ; j < flds.length;++j){
        IrptFieldInfo fldinfo = new IrptFieldInfo();
        if (flds[j].equalsIgnoreCase("BBQ"))
          fldinfo.FIELD_NAME = "BBQ_";
        else if (flds[j].equalsIgnoreCase("USERID"))
          fldinfo.FIELD_NAME = "USERID_";
        else if (flds[j].equalsIgnoreCase("USERNAME"))
          fldinfo.FIELD_NAME = "USERNAME_";
        else if (flds[j].equalsIgnoreCase("BTYPE"))
          fldinfo.FIELD_NAME = "BTYPE_";
        else if (flds[j].equalsIgnoreCase("UPID"))
          fldinfo.FIELD_NAME = "UPID_";
        else if (flds[j].equalsIgnoreCase("OPTION"))
          fldinfo.FIELD_NAME = "OPTION_";
        else if (flds[j].equalsIgnoreCase("SHTAG"))
          fldinfo.FIELD_NAME = "SHTAG_";
        else if (flds[j].equalsIgnoreCase("ISHJ"))
          fldinfo.FIELD_NAME = "ISHJ_";
        else
          fldinfo.FIELD_NAME = flds[j];
        datameta.addFieldInfo(fldinfo);
      }
    }
  }
  
   
}
