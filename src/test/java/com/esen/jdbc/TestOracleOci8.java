package com.esen.jdbc;

import java.sql.*;

public class TestOracleOci8 {
  public static void main(String[] args) throws Exception{
    Class.forName("oracle.jdbc.driver.OracleDriver");
    //jdbc:oracle:thin:@192.168.1.200:1521:orcdb
    //jdbc:oracle:oci8:@orcdb_200
    Connection conn = DriverManager.getConnection("jdbc:oracle:oci:@orcdb_200"
        , "tax", "tax");
    try{
      Statement stat = conn.createStatement();
      ResultSet rs = stat.executeQuery("select hy_dm,hy_mc from dim_hy");
      while(rs.next()){
        String mc = rs.getString(2);
        System.out.println(rs.getString(1)+"\t"+mc);
        //mc = new String(mc.getBytes("iso8859_1"),"GBK");
        //System.out.println("\t"+mc);
      }
      stat.close();
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
}
