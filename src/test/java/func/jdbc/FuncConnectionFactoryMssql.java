package func.jdbc;

import com.esen.jdbc.ConnectionFactory;

class FuncConnectionFactoryMssql extends FuncConnectionFactory {

  public FuncConnectionFactoryMssql() {

  }

  public String getUrl(String ip, String sid, String port) {
    return "jdbc:sqlserver://" + getDefaultIp(ip) + ":" + getDefaultPort(port, "1433") + ";databaseName=" + sid + ";";
  }

  public String getName() {
    return DB_MSSQL;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[2];
  }

  public String getDriver() {
    return DRIVER_MSSQL;
  }
}
