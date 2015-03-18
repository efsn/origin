package com.esen.vfs2;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;

import func.FuncTestCase;
import func.jdbc.FuncConnectionFactory;

public class TestConnectionFactory extends FuncTestCase {

  public static void main(String[] args) throws Exception {
    ConnectionFactory fct = getConnectionFactory();
    Connection con = fct.getConnection();
    if (con != null) {
      System.out.println("connect");
      con.close();
    }
    else {
      System.out.println("can not connect");
    }
  }

  private static ConnectionFactory getConnectionFactory() {
    //    //    String driver = "com.mysql.jdbc.Driver";
    //    //    String url = "jdbc:mysql://localhost:3306/vfs?useUnicode=true&characterEncoding=utf8";
    //    String driver = "oracle.jdbc.driver.OracleDriver";
    //    String url = "jdbc:oracle:thin:@192.168.80.128:1521:test";
    //    String user = "system";
    //    String pw = "test";
    //    SimpleConnectionFactory fct = new SimpleConnectionFactory(driver, url, user, pw, "debug");
    //    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    //    fm.setConnectionFactory("*", fct);
    //    DefaultConnectionFactory.set(fm);
    //    return fct;
    return FuncConnectionFactory.getDb2CustomConnectionFactory();
  }
}
