package com.esen.jdbc.pool.impl.kingbasees;

import java.sql.ResultSet;
import java.sql.SQLException;

import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledResultSet;

/**
 * 封装结果集接口，绕开getObject()的bug；
 * @author dw
 *
 */
public class KingBaseESPooledResultSet extends PooledResultSet {

  public KingBaseESPooledResultSet(ResultSet rs, PooledConnection pconn) throws SQLException {
    super(rs, pconn);
  }
  
  /**
   * 20100423
   * 人大金仓数据库更新了jdbc包，可以支持从getObject(index)读取数值类型值了；
   * 因此注释掉下面的代码；
   */
  
  /*public Object getObject(int columnIndex) throws SQLException {
    switch(getSQLType(columnIndex)){
      case java.sql.Types.CLOB :
      case java.sql.Types.LONGVARCHAR: 
        return getString(columnIndex);
      case Types.FLOAT:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.REAL:
      case Types.DECIMAL: {
        *//**
         * 20100414
         * KingBaseES数据库在结果集中通过rs.getObject()读取值时，可能出现的:
         * [KingbaseES JDBC Driver]非法BigDecimal值
         * at com.kingbase.jdbc2.AbstractJdbc2ResultSet.toBigDecimal(Unknown Source)
         * at com.kingbase.jdbc2.AbstractJdbc2ResultSet.getBigDecimal(Unknown Source)
         * at com.kingbase.jdbc3.AbstractJdbc3ResultSet.getObject(Unknown Source)
         * at com.esen.jdbc.pool.impl.PooledResultSet.getObject(PooledResultSet.java:250)
         * 
         * 暂时使用getDouble()获取；
         *//*
        return new Double(getDouble(columnIndex));
      }
      default: {
        return getObjectOther(columnIndex);
      }
    }
  }*/
}
