package func.jdbc;

import com.esen.jdbc.ConnectionFactory;

class FuncConnectionFactorySybaseiq extends FuncConnectionFactorySybase {
  public FuncConnectionFactorySybaseiq() {
  }

  public String getName() {
    return DB_SYBASEIQ;
  }

  protected String[] getCustom() {
    return CUSTOMJDBC[6];
  }
}
