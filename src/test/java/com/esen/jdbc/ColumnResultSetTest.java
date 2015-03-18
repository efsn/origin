package com.esen.jdbc;

import java.sql.*;

import junit.framework.TestCase;

public class ColumnResultSetTest extends TestCase {
  public void  testColumnResultSet() throws Exception{
    Connection conn = null;
    try{
      conn = getConnection();
      ResultSet[] rss = new ResultSet[2];
      Statement stat = conn.createStatement();
      Statement stat2 = conn.createStatement();
      rss[0] = stat.executeQuery("select id,name from ta order by id");
      while(rss[0].next()){
         System.out.println(rss[0].getString(1)+" "+rss[0].getString(2));
      }
      rss[0].beforeFirst();
      System.out.println("-----------------------");
      rss[1] = stat2.executeQuery("select id,email from tb order by id");
      while(rss[1].next()){
        System.out.println(rss[1].getString(1)+" "+rss[1].getString(2));
     }
     rss[1].beforeFirst();
     System.out.println("-----------------------");
      int[] relation = {1};
      ResultSet rs3 = ColumnResultSet.getInstance(rss,relation,false);
      while(rs3.next()){
        System.out.println(rs3.getString(1)+" "+rs3.getString(2)+" "+rs3.getString(3));
      }
    }catch(Exception ex){
      ex.printStackTrace();
    }
    finally{
      if(conn!=null)
        conn.close();
    }
  }
  private Connection getConnection() throws ClassNotFoundException, SQLException{
    Class.forName("com.mysql.jdbc.Driver");
    return DriverManager.getConnection(
            "jdbc:mysql://127.0.0.1/dw2?useUnicode=true&characterEncoding=GB2312",
            "root", "dw");
  }
}
