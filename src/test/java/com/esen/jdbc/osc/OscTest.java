package com.esen.jdbc.osc;

import java.sql.*;

public class OscTest {
  public Connection getConn() throws SQLException, ClassNotFoundException {
    Class.forName("com.oscar.Driver");
    String url = "jdbc:oscar://localhost/OSRDB";
    String user = "IREPORT";
    String passwd = "IREPORT";
    return DriverManager.getConnection(url, user, passwd);
  }
  /**
   * @param args
   * @throws SQLException 
   */
  public static void main(String[] args) throws Exception {
    OscTest ot = new OscTest();
    Connection conn = null;
    try{
      conn = ot.getConn();
      DatabaseMetaData meta = conn.getMetaData();
      System.out.println(meta.getDatabaseProductName());
      System.out.println(meta.getDatabaseProductVersion());
      System.out.println(meta.getDatabaseMajorVersion());
      System.out.println(meta.getDatabaseMinorVersion());
    }finally{
      if(conn!=null){
        conn.close();
      }
    }
  }

}
