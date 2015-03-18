package com.esen.jdbc.etl.data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * irptserver 数据的data
 * @author jzp
 *
 */
public class IrptDataSet implements Serializable {
  private ArrayList values =null;
  public IrptDataSet(int size){
    values = new ArrayList(size);
    for(int i =0 ; i<size;++i){
      values.add(null);
    }
  }
  public Object getFieldValue(int index){
    return values.get(index);
  }
  public void setFieldValue(int index ,Object value){
    values.set(index, value);
  }

}
