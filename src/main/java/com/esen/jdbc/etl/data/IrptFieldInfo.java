package com.esen.jdbc.etl.data;

import java.io.Serializable;

/**
 * 字段信息
 * @author jzp
 *
 */
public class IrptFieldInfo implements Serializable {
  public String FIELD_NAME = null;
  public char FIELD_TYPE = 'C';
  public String FIELD_CAPTION = null;
  public IrptFieldInfo(){
    
  }
  public IrptFieldInfo(String fldname,char fldtype){
    FIELD_NAME = fldname ;
    FIELD_TYPE = fldtype;
  }
}
