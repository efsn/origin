package com.esen.jdbc.exptosql;

import com.esen.util.exp.ExpVarImpl;

public class TestVarCell extends ExpVarImpl {
  private int sqltype;
  public TestVarCell(String name, char tp,int sqltype) {
    super(name, tp);
    this.sqltype = sqltype;
  }
  public int getSqlType(){
    return sqltype;
  }
}
