package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;

/**
 * 20090803
 * 增加这个类是为了对某些特殊的属性，在访问时才初始化；
 * 比如是否唯一属性，增长字段属性等；
 * 
 * 使用此类的原因是：
 * 从数据库读取表结构时，判断一个字段是否唯一，需要结合单字段的主键，唯一索引；
 * 所有从数据库读取的字段属性类都继承此类；
 * @author dw
 *
 */
public class TableColumnMetaDataProvider extends TableColumnMetaDataImpl {
  
  protected TableMetaDataImpl meta;
  private boolean isInitUnique;
  protected boolean isInitAutlInc;

  public TableColumnMetaDataProvider(TableMetaDataImpl meta,String name){
    super(name);
    this.meta = meta;
  }
  public boolean isUnique() {
    if (!isInitUnique) {
      initColumnsUnique();
      /**
       * 调用一个字段的唯一属性值，会初始化所有字段的该属性；
       * 这里将初始化标识符值为true;
       */
      TableColumnMetaData[] cols = meta.getColumns();
      for (int i = 0; i < cols.length; i++) {
        TableColumnMetaDataProvider col = (TableColumnMetaDataProvider)cols[i];
        col.isInitUnique = true;
      }
    }
    return super.isUnique();
  }
  
  protected void initColumnsUnique() {
    TableIndexMetaData[] idxes = meta.getIndexes();
    if (idxes != null && idxes.length != 0) {
      TableIndexMetaData idx;
      for (int i = 0; i < idxes.length; i++) {
        idx = idxes[i];
        if (idx.isUnique() && idx.getColumns().length == 1) {
          String name = idx.getColumns()[0];
          /**
           * 20090801 BIDEV-755
           * 对于Oracle 的物化视图，查出来的索引字段，可能不是物化视图中的字段；
           * 如果不是字段，则忽略；
           */
          TableColumnMetaDataProvider col = (TableColumnMetaDataProvider)meta.getColumn(name);
          if(col!=null){
            col.setUnique(true);
          }
        }
      }
    }
    /**
     * 20090220
     * 如果是主键，也是唯一
     */
    String[] keys = meta.getPrimaryKey();
    if(keys!=null&&keys.length==1){
      TableColumnMetaDataImpl col = (TableColumnMetaDataImpl)meta.getColumn(keys[0]);
      if(col!=null){
        col.setUnique(true);
      }
    }
    
  }
  public boolean isAutoInc() {
    if (!isInitAutlInc) {
      try{
        initColumnsAutoInc();
      }catch(Exception ex){
        throw new RuntimeException(ex);
      }
      TableColumnMetaData[] cols = meta.getColumns();
      for (int i = 0; i < cols.length; i++) {
        TableColumnMetaDataProvider col = (TableColumnMetaDataProvider)cols[i];
        col.isInitAutlInc = true;
      }
    }
    return super.isAutoInc();
  }
  
  protected void initColumnsAutoInc() throws Exception {
    Connection con = meta.owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
        ResultSet rs = sm.executeQuery(meta.SQL_COLUMN + meta.getTableName()
            + " where 1>2");
        try {
          ResultSetMetaData rmeta = rs.getMetaData();
          int count = rmeta.getColumnCount();
          for (int i = 1; i <= count; i++) {
            if(rmeta.isAutoIncrement(i)){
              TableColumnMetaDataImpl col = (TableColumnMetaDataImpl)meta.getColumn(rmeta.getColumnName(i));
              col.setAutoInc(true);
              break;//不可能有多个自动增长字段；
            }
          }
        }
        finally {
          rs.close();
        }
      }
      finally {
        sm.close();
      }
    }
    finally {
      meta.owner.closeConnection(con);
    }
    
  }
}
