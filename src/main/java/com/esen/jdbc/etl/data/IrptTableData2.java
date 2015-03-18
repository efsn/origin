package com.esen.jdbc.etl.data;

import java.io.Serializable;
import java.util.ArrayList;

public class IrptTableData2 implements Serializable  {
  
  
  private ArrayList values = null;
  private int cursor = -1;
  private String _name = null;
  public IrptTableData2(String nm){
    _name = nm;
    values = new ArrayList(10);
  }
  
  public String getName(){
    return _name;
  }
  public void reset(){
    cursor = -1;
  }
  
  public boolean isEmpty(){
    return values.size()==0;
  }
  /**
   * 判断是否有下条数据
   * @return
   */
  public boolean next(){
    ++cursor;
    return cursor<values.size();
  }
  /**
   * 获得边长表当前的行数据
   * @return
   */
  public IrptTableData getData(){
    return (IrptTableData)values.get(cursor);
  }
  
  public void addIrptTableData(IrptTableData tableData){
    values.add(tableData);
  }
  
  public Object getFieldValue(int index){
    IrptTableData data = getData();
    if (data == null) return null;
    return data.getFieldValue(index);
  }
}
