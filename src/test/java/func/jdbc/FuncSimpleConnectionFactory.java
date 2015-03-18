package func.jdbc;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.pool.BaseDataSource;

import func.FuncSys;

public class FuncSimpleConnectionFactory extends SimpleConnectionFactory {
  private String name;

  public FuncSimpleConnectionFactory(String name, String driverClassName, String url, String userName, String password) {
    super(driverClassName, url, userName, password);
    this.name = name;
  }

  public FuncSimpleConnectionFactory(String name, String driverClassName, String url, String userName, String password,
      String logLever) {
    super(driverClassName, url, userName, password, logLever);
    this.name = name;
  }

  /**
   * @deprecated
   * @param driverClassName
   * @param url
   * @param userName
   * @param password
   * @param debug
   */
  public FuncSimpleConnectionFactory(String name, String driverClassName, String url, String userName, String password,
      boolean debug) {
    super(driverClassName, url, userName, password, debug);
    this.name = name;
  }

  /**
   * @deprecated
   * @param datasource
   * @param debug
   */
  public FuncSimpleConnectionFactory(String name, String datasource, boolean debug) {
    super(datasource, debug);
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  /**
   * 确保可以连接到数据库
   * 如果已经关闭的连接池,可以调用此方法重新连接
   */
  public void ensureConnect() {
    Integer stateObj = (Integer) FuncSys.getDeepDeclaredFieldValue(this.getDataSource(), "state");
    Integer STATE_STARTED = (Integer) FuncSys.getDeepDeclaredFieldValue(this.getDataSource(), "STATE_STARTED");
    if (stateObj.equals(STATE_STARTED)) {
      return;
    }
    BaseDataSource ds = this.getDataSource();
    Properties props = new Properties();
    props.setProperty("url", ds.getUrl());
    props.setProperty("driverClassName", ds.getDriverClassName());
    props.setProperty("username", ds.getUsername());
    props.setProperty("password", ds.getPassword());
    props.setProperty("logLevel", ds.getLogLeverStr());
    BaseDataSource dataSource = new BaseDataSource();
    dataSource.setProperties(props);
    this.setDataSource(dataSource);
  }

  public Connection getConnection() throws SQLException {
    ensureConnect();
    return super.getConnection();
  }
}
