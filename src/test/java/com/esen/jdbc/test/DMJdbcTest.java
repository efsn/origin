package com.esen.jdbc.test;

import java.sql.*;
public class DMJdbcTest {
  //定义DM JDBC驱动串
  String jdbcString = "dm.jdbc.driver.DmDriver";
  //定义DM URL串
  String urlString = "jdbc:dm://192.168.1.224:12345/testdb";
  //定义连接用户名
  String userName = "test";
  //定义连接用户口令
  String userPwd = "111111";
  //定义连接对象
  Connection conn = null;
  //加载JDBC驱动程序
  public void LoadJdbcDriver() throws SQLException
  {
    try
    {
      System.out.println("Load JDBC Driver");
      //加载JDBC
      Class.forName(jdbcString);
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  //连接DM数据库
  public void DmConnect() throws SQLException
  {
    try
    {
      System.out.println("Connect to DM Server");
      //连接
      conn = DriverManager.getConnection(urlString, userName, userPwd);
    }
    catch(SQLException ex)
    {
      ex.printStackTrace();
    }
  }
  
  //关闭连接
  public void CloseConnect() throws SQLException
  {
    try
    {
      System.out.println("Close Connect");
      //连接
      conn.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  //插入数据 
  public void InsertData() throws SQLException
  {
    try
    {
      String sqlC = "create table dmjdbc_demo_01(c1 int, c2 int);";
      String sqlI = "insert into dmjdbc_demo_01 values(1, 1);";
      String sqlD = "drop table dmjdbc_demo_01;";

      //创建语句对象
      Statement stmt = conn.createStatement();
      
      //执行语句
      stmt.executeUpdate(sqlC);
      stmt.executeUpdate(sqlI);
      stmt.executeUpdate(sqlD);
      
      //关闭语句
      stmt.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  //查询数据
  public void SelectData() throws SQLException
  {
    try
    {
      //查询数据的SQL语句
      String sqlStr = "SELECT * FROM SYSDBA.SYSTABLES";
      //创建语句对象
      Statement stmt = conn.createStatement();
      //执行语句
      ResultSet rs = stmt.executeQuery(sqlStr);
      //显示结果集
      DisPlayResultSet(rs);
      //关闭结果集
      rs.close();
      //关闭语句
      stmt.close();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }

  //显示数据
  public void DisPlayResultSet(ResultSet rs) throws SQLException
  {
    try
    {
      //取结果集元数据
      ResultSetMetaData rsmd = rs.getMetaData();
      //取得结果集所包含的列数
      int numCols = rsmd.getColumnCount();
      //显示列标头
      for(int i=1; i<=numCols; i++)
      {
        if(i==numCols-1)
        {
          System.out.print(rsmd.getColumnLabel(i) + "\t\t\t");
        }
        else
        {
          System.out.print(rsmd.getColumnLabel(i) + "\t\t");
        } 
      }
      System.out.println("");
      //显示结果集中所有数据
      while(rs.next())
      {
        for(int i=1; i<=numCols; i++)
        {
          System.out.print(rs.getString(i) + "\t\t");
        }
        System.out.println("");
      }
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }
  
  //入口函数
  public static void main(String args[])
  {
    try
    {
      //新建对象
      DMJdbcTest demo = new DMJdbcTest();
      //加载驱动
      demo.LoadJdbcDriver();
      //连接数据库
      demo.DmConnect();
      demo.InsertData();
      //查询数据
      demo.SelectData();
      //关闭连接
      demo.CloseConnect();
    }
    catch(Exception ex)
    {
      ex.printStackTrace();
    }
  }
}

