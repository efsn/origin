package com.esen.upgrade.impl2_2m2;

import java.sql.Connection;
import java.sql.Statement;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.upgrade.VfsUpgrade;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;
//changlog 国际化2012.9.12 panql
public class VfsUpgrade22m2 implements VfsUpgrade {

  public void upgrade(Connection con, DbDefiner dd, String oldTable, String newTable) throws Exception {
    if (StrFunc.isNull(oldTable) || !dd.tableOrViewExists(con, oldTable)) {
      return;
    }
    if (StrFunc.isNull(newTable) || !dd.tableOrViewExists(con, newTable)) {
      throw new RuntimeException(I18N.getString("com.esen.upgrade.impl2_2m2.vfsupgrade22m2.1", "vfs对应的数据库表不存在,无法升级"));
    }
    String sql1 = "insert into "
        + newTable
        + "(PARENTDIR_,FILENAME_,ISFILE_,CREATETIME_,MODIFYTIME_,OWNER_,MENDER_,CHARSET_,MIMETYPE_,SIZE_,CONTENT_) select PARENTDIR,FILENAME,ISFILE,LASTMODIFY_,LASTMODIFY_,'admin','admin',CHAESET_,MIME_TYPE,SIZE_,CONTENT_ from "
        + oldTable;
    String sql2 = "update " + newTable + " set ISFILE_='1' where ISFILE_='f'";
    String sql3 = "update " + newTable + " set ISFILE_='0' where ISFILE_='d'";
    con.setAutoCommit(false);
    Statement sm = con.createStatement();
    try {
      sm.addBatch(sql1);
      sm.addBatch(sql2);
      sm.addBatch(sql3);
      sm.executeBatch();
      con.commit();
    }
    catch (Exception e) {
      con.rollback();
      throw e;
    }
    finally {
      sm.close();
    }
  }
//  //国际化测试
//  public static void main(String[] args){
//	  System.out.println(I18N.getString("com.esen.upgrade.impl2_2m2.vfsupgrade22m2.notable4vfs", ""));
//  }
}
