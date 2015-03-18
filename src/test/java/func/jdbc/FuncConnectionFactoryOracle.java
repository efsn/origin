package func.jdbc;

import com.esen.jdbc.ConnectionFactory;

class FuncConnectionFactoryOracle extends FuncConnectionFactory {

  public FuncConnectionFactoryOracle() {

  }

  public String getUrl(String ip, String sid, String port) {
    return "jdbc:oracle:thin:@" + getDefaultIp(ip) + ":" + getDefaultPort(port, "1521") + ":" + sid;
  }

  public String getName() {
    return DB_ORACLE;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[0];
  }

  public String getDriver() {
    return DRIVER_ORALCE;
  }
}
