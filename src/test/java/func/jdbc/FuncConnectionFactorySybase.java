package func.jdbc;

import com.esen.jdbc.ConnectionFactory;

class FuncConnectionFactorySybase extends FuncConnectionFactory {

  public FuncConnectionFactorySybase() {

  }

  public String getUrl(String ip, String sid, String port) {
    return "jdbc:sybase:Tds:" + getDefaultIp(ip) + ":" + getDefaultPort(port, "5000") + "/" + sid + "?charset=cp936";
  }

  public String getName() {
    return DB_SYBASE;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[5];
  }

  public String getDriver() {
    return DRIVER_SYBASE;
  }
}
