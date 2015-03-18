package com.esen.jdbc.pool.impl.mssql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.jdbc.pool.PooledConnection;

public class MssqlPooledPreparedStatement extends MssqlPooledStatement {

  public MssqlPooledPreparedStatement(PooledConnection conn,
      PreparedStatement pstat, String sql) {
    super(conn, pstat, sql);
  }
  public ResultSet executeQuery() throws SQLException {
    ResultSet rs = super.executeQuery();
    return getLimitQeury(sql, rs);
  }
  
  /**
   * 20091230
   * <pre>
com.microsoft.sqlserver.jdbc.SQLServerDriver
JDBC Driver 1.2 :  sqljdbc.jar
需要jdk1.4， 支持sqlserver2000,sqlserver2005
驱动版本：1.2.2828.100

_pstat.setNull(pindex,Types.Null) 异常
java.lang.NullPointerException
        at com.microsoft.sqlserver.jdbc.AppDTVImpl$SetValueOp.executeDefault(Unknown Source)
        at com.microsoft.sqlserver.jdbc.DTV.executeOp(Unknown Source)
        at com.microsoft.sqlserver.jdbc.AppDTVImpl.setValue(Unknown Source)
        at com.microsoft.sqlserver.jdbc.DTV.setValue(Unknown Source)
        at com.microsoft.sqlserver.jdbc.Parameter.setValue(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.setObject(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.setNull(Unknown Source)
        at com.esen.jdbc.pool.impl.PooledPreparedStatement.setNull(PooledPreparedStatement.java:83)


_pstat.setObject(parameterIndex, null) 异常
com.microsoft.sqlserver.jdbc.SQLServerException: 操作数类型冲突: nvarchar 与 image 不兼容
        at com.microsoft.sqlserver.jdbc.SQLServerException.makeFromDatabaseError(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.getNextResult(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.doExecutePreparedStatement(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement$PrepStmtExecCmd.doExecute(Unknown Source)
        at com.microsoft.sqlserver.jdbc.TDSCommand.execute(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerConnection.executeCommand(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeCommand(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerStatement.executeStatement(Unknown Source)
        at com.microsoft.sqlserver.jdbc.SQLServerPreparedStatement.executeUpdate(Unknown Source)
        at com.esen.jdbc.pool.impl.PooledPreparedStatement.executeUpdate(PooledPreparedStatement.java:77)
   * </pre>
   * 改为调用父类的setObject(parameterIndex,null);
   * 此方法先获取parameterIndex字段的类型t，再调用_pstat.setNull(parameterIndex,t)
   * 不过获取对应字段的类型需要执行一个亚查询sql ：select field1,feild2... from tablename where 1>2
   */
  public void setNull(int parameterIndex, int sqlType) throws SQLException {
    if(sqlType==Types.NULL){
      setObject(parameterIndex,null);
    }else{
      super.setNull(parameterIndex, sqlType);
    }
  }

}
