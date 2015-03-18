package com.esen.jdbc.dialect.impl;

import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.ArrayFunc;
import com.esen.util.i18n.I18N;

/**
 * 索引接口的实现类；
 * @author dw
 *
 */
public class TableIndexMetaDataImpl implements TableIndexMetaData {
  private String name;//索引名称

  private boolean isunique;

  private String[] columns;//索引列

  /**
   * 构造一个索引，包含索引名，索引字段，索引是否唯一
   * @param name
   * @param columns
   * @param unique
   */
  public TableIndexMetaDataImpl(String name,String columns[],boolean unique) {
    this.name = name;
    this.columns = columns;
    this.isunique = unique;
  }
  
  /**
   * 构造一个索引
   * @param name
   * @param cols
   *        索引字段是','隔开的字符串："field1,field2" 
   *        现在兼容头尾有括号的模式："(field1,field2)" 
   * @param unique
   */
  public TableIndexMetaDataImpl(String name,String cols,boolean unique) {
    if(cols==null||cols.length()==0){
//    	 throw new RuntimeException("没有定义索引字段；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.tableindexmetadataimpl.noindexdef", "没有定义索引字段；"));
    }
    cols = cols.trim();
    if(cols.charAt(0)=='('&&cols.charAt(cols.length()-1)==')'){
      cols = cols.substring(1,cols.length()-1);
    }
    this.name = name;
    this.columns = ArrayFunc.excludeNullStrs(cols.split(","));
    this.isunique = unique;
  }

  public String getName() {
    return this.name;
  }

  public boolean isUnique() {
    return this.isunique;
  }

  public String[] getColumns() {
    return this.columns;
  }

}
