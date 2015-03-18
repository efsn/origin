package com.esen.jdbc.pool.impl.oracle;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.DefaultResultSetMetaData;

public class OracleResultSetMetaData extends DefaultResultSetMetaData {

  public OracleResultSetMetaData(ResultSetMetaData rsmd, DataBaseInfo dbinfo) {
    super(rsmd, dbinfo);
  }
  
  public int getColumnType(int column) throws SQLException {
    int t = super.getColumnType(column);
    /**
     * Oracle数据库对与nvarchar2类型的字段，返回1111值；
     * 这里转换成标准的数据类型；
     * 方便对字符数据类型的处理；
     * 同样的改动还有OracleTableColumnMetaDataImpl类；
     * 
     * 但是这样的处理并不好，在DataCopy数据时，会将源表的nvarchar2类型字段，copy成目的表的varchar2类型；
     * 当然数据上没有任何差别，但不是真正的copy；TODO
     */
    switch (t) {
      case 1111:
        return Types.VARCHAR;
    }
    return t;
  }
}
