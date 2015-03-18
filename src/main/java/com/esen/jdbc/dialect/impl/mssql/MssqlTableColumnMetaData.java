package com.esen.jdbc.dialect.impl.mssql;

import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class MssqlTableColumnMetaData extends TableColumnMetaDataProvider {

  public MssqlTableColumnMetaData(TableMetaDataImpl meta, String name) {
    super(meta, name);
  }
  public boolean isAutoInc() {
    /**
     * SqlServer2005在初始化获取字段信息时，已经通过查系统表获得了是否为自增长属性值；
     * 这里不需要在查数据库从rsmeta.isAutoIncrement(i) 获取了；
     */
    isInitAutlInc = true;
    return super.isAutoInc();
  }
}
