package com.esen.jdbc.etl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.util.ExceptionHandler;

public class EtlDataDest_DefaultImpl implements EtlDataDest {

  /**
   * 构造一个数据写入对象，con是一个数据库链接，此链接由传入者自己关闭，此对象不负责关闭它
   * tableorsql是一个表名或者是sql，如果是sql那么应该是一个insert into的sql
   * @param con
   * @param tableorsql
   */
  private PreparedStatement pstmt;
  
  public EtlDataDest_DefaultImpl(Connection con , String tableorsql) throws Exception{
    String insertSql = SqlFunc.isInsertInto(tableorsql) ? tableorsql
        : getInsertSql(con, tableorsql);
    pstmt = con.prepareStatement(insertSql);
  }
  
  /**
   * 一行的数据都setvalue完后调用此函数
   * @throws SQLException 
   */
  public void postARow() throws SQLException{
    pstmt.addBatch();
  }
  
  /**
   * 设置第fieldIndex个字段的值,fieldIndex从0开始
   * @throws SQLException 
   * @throws  
   */
  public void setValue(int fieldIndex, Object value){
    try{
      pstmt.setObject(fieldIndex+1, value);
    }
    catch(Exception ex){
      ExceptionHandler.rethrowRuntimeException(ex);
    }
  }
  
  /**
   * 所有行都写入后调用此函数
   * @throws SQLException 
   */
  public void postAll() throws SQLException{
    pstmt.executeBatch();
  }
  
  /**
   * 数据全部写入完成后，关闭connection之前，调用此函数，关闭PreparedStatement，
   * @throws SQLException 
   */
  public void closePreparedStatement() throws SQLException{
    pstmt.close();
  }
  
  /**
   * 以更新计数的形式检索当前结果；如果结果为 ResultSet 对象或没有更多结果，则返回 -1。每个结果只应调用一次此方法
   * @return
   * @throws SQLException 
   */
  public int getUpdateCount() throws SQLException{
    return pstmt.getUpdateCount();
  }

  private String getInsertSql(Connection con,String tableName) throws Exception{
    StringBuffer insertSql = new StringBuffer("insert into ");
    insertSql.append(tableName);
    insertSql.append(" values (");
    Statement stmt = con.createStatement();
    try{
      ResultSet rs = stmt.executeQuery("select * from "+tableName+" where 0>1");
      try{
        ResultSetMetaData rsmt = rs.getMetaData();
        int columnCount = rsmt.getColumnCount();
        for(int i=1;i<columnCount;i++){
          insertSql.append("?,");
        }
        insertSql.append("?)");
      }
      finally{
        rs.close();
      }
    }
    finally{
      stmt.close();
    }
    return insertSql.toString();
  }
}
