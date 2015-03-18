package com.esen.upgrade;

import java.sql.Connection;

import com.esen.jdbc.dialect.DbDefiner;

/**
 * 对vfs升级
 * @author zhuchx
 */
public interface VfsUpgrade {
  public void upgrade(Connection con, DbDefiner dd, String oldTable, String newTable) throws Exception;
}
