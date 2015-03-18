package func.jdbc;

import com.esen.jdbc.ConnectionFactory;
import com.esen.util.StrFunc;

class FuncConnectionFactoryDb2 extends FuncConnectionFactory {

  public FuncConnectionFactoryDb2() {
  }

  public String getUrl(String ip, String sid, String port) {
    return "jdbc:db2://" + getDefaultIp(ip) + ":" + getDefaultPort(port, "50000") + "/" + sid;
  }

  public String getName() {
    return DB_DB2;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[4];
  }

  public String getDriver() {
    return DRIVER_DB2;
  }
}
