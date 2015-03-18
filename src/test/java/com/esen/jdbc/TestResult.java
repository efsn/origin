package com.esen.jdbc;

import java.math.BigDecimal;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import junit.framework.TestCase;

public class TestResult extends TestCase {
  
  public void testDirectConnectionResultSet() throws Exception {
    Class.forName("oracle.jdbc.driver.OracleDriver");
    Connection conn = DriverManager.getConnection(
        "jdbc:oracle:thin:@192.168.1.200:1521:orcdb", "tax", "tax");
    String sql = "Select to_char(t.nsrdzdah),t.nsrdzdah,t.yxq_q From fact_nsrxx t Where t.yxq_q=to_date('2005-12-13','yyyy-mm-dd') And t.nsrdzdah>=2.10005000004723E17";
    try{
      long t = System.currentTimeMillis();
    PreparedStatement pstat = conn.prepareStatement(sql);
    ResultSet rs = pstat.executeQuery();
    loadData(rs);
    pstat.close();
    System.out.println("直接连接数据库查询"+(System.currentTimeMillis()-t)+"毫秒；");
    }finally{
      conn.close();
    }
  }
  private void loadData(ResultSet rs)throws Exception {
    int num = 0;
    while(rs.next()){
      double d = rs.getDouble(2);
      String dv = rs.getString(2);
      BigDecimal bd = rs.getBigDecimal(2);
      bd.toString();
      System.out.println(rs.getString(1)+","+dv+","+d+",\""+NumberFormat.getInstance().format(d)+"\","+bd.toString());
      num++;
    }
    System.out.println("count: "+num);
  }
/*  public void testPoolConnectionResultSet() throws Exception {
    SimpleConnectionFactory dbf = new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@10.54.17.193:1521:ireport", "test", "test");
    try {
      Connection conn = dbf.getConnection();
      try {
        String sql = "select * from fact_nsrxx t";
        long t = System.currentTimeMillis();
        PreparedStatement pstat = conn.prepareStatement(sql);
        ResultSet rs = pstat.executeQuery();
        List list = new ArrayList();
        loadData(list,rs);
        pstat.close();
        System.out.println("连接池连接数据库查询"+(System.currentTimeMillis()-t)+"毫秒；");
      }
      finally {
        conn.close();
      }
    }
    finally {
      dbf.close();
    }
  }*/
}
