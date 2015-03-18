package com.esen.jdbc.pool.impl.sybase;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.DefaultResultSetMetaData;

public class SybaseIQResultSetMetaData extends DefaultResultSetMetaData {

  public SybaseIQResultSetMetaData(ResultSetMetaData rsmd, DataBaseInfo dbinfo) {
    super(rsmd, dbinfo);
  }
  
  public int getColumnType(int column) throws SQLException {
    int t = super.getColumnType(column);
    /**
     * SybaseIQ中，对date,time,datetime,timestamp类型返回值与其他主流数据库不一致；
     * date=9;time=10;datetime=timestamp=11
     * 这里转换成标准的数据类型；
     * 方便对日期数据类型的处理；
     */
    switch (t) {
      case 9:
        return Types.DATE;
      case 10:
        return Types.TIME;
      case 11:
        return Types.TIMESTAMP;
    }
    return t;
  }
}
