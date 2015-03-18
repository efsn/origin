package com.esen.jdbc.dialect.impl.oracle;

import com.esen.jdbc.dialect.DbDefiner;

public class Oracle817Dialect extends OracleDialect {

  public Oracle817Dialect(Object f) {
    super(f);
  }
  public DbDefiner createDbDefiner() {
    return new Oracle817Def(this);
  }
}
