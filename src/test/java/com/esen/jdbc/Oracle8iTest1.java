package com.esen.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.PoolConnectionFactory;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.BaseDataSource;
import com.esen.jdbc.pool.PoolPropName;
import com.esen.util.MiniProperties;

public class Oracle8iTest1 {
  ConnectionFactory fct;

  public ConnectionFactory getConnectionFactory() throws Exception {
    if (fct == null) {
      String username = "dw";
      String password = "dw";
      String driverclassname = "oracle.jdbc.driver.OracleDriver";
      String url = "jdbc:oracle:thin:@192.168.1.121:1521:tdevdb";

      MiniProperties pro = new MiniProperties();
      pro.setString(PoolPropName.PROP_USERNAME, username);
      pro.setString(PoolPropName.PROP_PASSWORD, password);
      pro.setString(PoolPropName.PROP_DRIVERCLASSNAME, driverclassname);
      pro.setString(PoolPropName.PROP_URL, url);
      pro.setBoolean(PoolPropName.PROP_ISDEBUG, true);
      //pro.setString(PoolPropName.PROP_CHARACTERENCODING, "iso8859_1");
      BaseDataSource dataSource = new BaseDataSource();
      TPoolConnectionFactory dbfct = new TPoolConnectionFactory(dataSource);
      dbfct.getDataSource().setProperties(pro);
      Connection con = dbfct.getConnection();
      if (con == null) {
        throw new RuntimeException("连接创建失败");
      }
      con.close();
      this.fct = dbfct;
    }
    return fct;
  }

  public Connection getConnection() throws Exception {
    return this.getConnectionFactory().getConnection();
  }

  public void test8iinsertstrfxb() throws Exception {
    String str1 = "4Z0PCQKZSL6UUTTZUKTWNSeCUKZBRXK1";
    String str = "按全球市场、全球区域，国家等级，国家分级统计有效CL额度，买家个数";
    String sql = "insert into ECICZT_FXB2 (FXBID,FXBCAPTION) values(?,?)";
    DataBaseInfo db = getConnectionFactory().getDbType();
    Connection con = getConnectionFactory().getConnection();
    try {
      /*PreparedStatement ps = con.prepareStatement(sql);
      try {
        ps.setString(1, str1);
        ps.setString(2, str);
        ps.executeUpdate();
      }
      finally {
        ps.close();
      }*/
    }
    finally {
      con.close();
    }
  }

  public static void main(String[] args) throws Exception {
    Oracle8iTest1 test = new Oracle8iTest1();
    test.test8iinsertstrfxb();
  }
}

class TPoolConnectionFactory extends PoolConnectionFactory {

  public TPoolConnectionFactory(BaseDataSource datasource) {
    super(datasource);
  }

  public void close() {
    try {
      this.getDataSource().close();
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
  }
}
