package com.esen.jdbc.dialect.impl.oracle;

import java.sql.Types;

import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableTriggerMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;

/**
 * Oracle9i级以上版本的表结构字段实现类；
 * 获取自动增长，需要读取触发器；
 * @author dw
 *
 */
public class OracleTableColumnMetaData extends TableColumnMetaDataProvider {

  private OracleTableMetaData ometa;

  private boolean initDesc;

  private boolean initDefaultValue;
  
  public OracleTableColumnMetaData(OracleTableMetaData meta, String name) {
    super(meta,name);
    this.ometa = (OracleTableMetaData)this.meta;
  }
  public int getType(){
    /**
     * 20090710
     * Oracle数据库对与nvarchar2类型的字段，返回1111值；
     * 这里转换成标准的数据类型；
     * 方便对字符数据类型的处理；
     * 同样的带动还有OracleResultSetMetaData类；
     */
    switch(sqltype){
      case 1111:
        return Types.VARCHAR;
    }
    return this.sqltype;
  }
 public String getDefaultValue(){
    if(!initDefaultValue){
      try {
        ometa.initDefaultValue();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      TableColumnMetaData[] cols = ometa.getColumns();
      for (int i = 0; i < cols.length; i++) {
        OracleTableColumnMetaData col = (OracleTableColumnMetaData)cols[i];
        col.initDefaultValue = true;
      }
      initDefaultValue = true;
    }
    return this.defaultvalue;
    
  }
  public String getDesc(){
    if(!initDesc){
      try {
        ometa.initDesc();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
      TableColumnMetaData[] cols = ometa.getColumns();
      for (int i = 0; i < cols.length; i++) {
        OracleTableColumnMetaData col = (OracleTableColumnMetaData)cols[i];
        col.initDesc = true;
      }
      initDesc = true;
    }
    return super.getDesc();
  }

  public OracleTableMetaData getMeta() {
    return ometa;
  }

  protected void initColumnsAutoInc() throws Exception {
    TableTriggerMetaData[] tgs = getMeta().getTriggers();//这里获得的触发器是控制自动增长字段的
    if (tgs != null && tgs.length != 0) {
      TableTriggerMetaData tg;
      int r;
      for (int i = 0; i < tgs.length; i++) {
        tg = tgs[i];
        String[] cls = tg.getColumn();
        /**
         * 20090916  dw
         * 取得触发器中自动增长的字段,这里cls可能为空,因为可能这个触发器不是用于自动增长的;
         */
        if (cls==null||cls.length != 1)
          continue;
        OracleTableColumnMetaData col = (OracleTableColumnMetaData)meta.getColumn(cls[0]);
        if(col!=null){
          col.setAutoInc(true);
        }
      }
    }
    
  }
}
