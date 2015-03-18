package com.esen.jdbc.etl.data;

import java.io.Serializable;

public class IrptTableData extends IrptDataSet implements Serializable {
  private String _name = null;
  private String shtag = null;
  private String option = null;
  private String ishj = null;
  public IrptTableData(String name,int size){
    super(size);
    _name = name;
  }
  public String getName(){
    return _name;
  }
  
  public String getShtag(){
    return shtag;
  }
  public String getOption(){
    return option;
  }
  
  public String getIshj(){
    return ishj;
  }
  
  public void setShtag(String value){
    shtag= value;
  }
  
  public void setOption(String value){
    option = value;
  }
  public void setIshj(String value){
    ishj = value;
  }
}
