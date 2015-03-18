package func.jdbc;

import com.esen.jdbc.ConnectionFactory;

class FuncConnectionFactoryMssql2005 extends FuncConnectionFactoryMssql {
  public FuncConnectionFactoryMssql2005() {

  }

  public String getName() {
    return DB_MSSQL2005;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[3];
  }
}
