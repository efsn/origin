package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Types;

import com.esen.jdbc.dialect.impl.TableColumnMetaDataProvider;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * SybaseIQ的表字段属性实现类；
 * 主要是SybaseIQ获取的日期类型返回值和一般数据库的标准不一样；
 * @author dw
 *
 */
public class SybaseIQTableColumnMetaData extends TableColumnMetaDataProvider {

  public SybaseIQTableColumnMetaData(TableMetaDataImpl meta, String name) {
    super(meta, name);
  }
  public int getType(){
    /**
     * 20090901 BI-2444
     * SybaseIQ中，对date,time,datetime,timestamp类型返回值与其他主流数据库不一致；
     * date=9;time=10;datetime=timestamp=11
     * 这里转换成标准的数据类型；
     * 方便对日期数据类型的处理；
     * 同样的改动还有SybaseIQResultSetMetaData类；
     */
    switch (sqltype) {
      case 9:
        return Types.DATE;
      case 10:
        return Types.TIME;
      case 11:
        return Types.TIMESTAMP;
    }
    return sqltype;
  }
}
