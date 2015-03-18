package com.esen.upgrade;

import java.sql.Connection;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.upgrade.impl2_2m2.VfsUpgrade22m2;

public class VfsUpgradeMgr {
  public void upgrade(Connection con, DbDefiner dd, String oldTable, String newTable) throws Exception {
    new VfsUpgrade22m2().upgrade(con, dd, oldTable, newTable);
  }
}
