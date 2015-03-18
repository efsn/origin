package com.esen.jdbc.etl.impl;

import java.io.File;
import java.sql.Connection;
import java.sql.Statement;

import com.esen.dbf.DBFReader;
import com.esen.dbf.DBFRecordSet;
import com.esen.dbf.field.DBFField;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.etl.EasyEtl;
import com.esen.jdbc.etl.EtlDataDest_DefaultImpl;
import com.esen.util.ExceptionHandler;
import com.esen.util.FileFunc;
import com.esen.util.IProgress;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class EasyEtlImpl extends EasyEtl {
  private final String RESERVED_WORDS = " ACCESS DECIMAL INITIAL ON START ADD NOT INSERT ONLINE SUCCESSFUL ALL DEFAULT INTEGER OPTION SYNONYM ALTER DELETE INTERSECT OR SYSDATE AND DESC INTO ORDER TABLE ANY DISTINCT IS PCTFREE THEN AS DROP LEVEL PRIOR TO ASC ELSE LIKE PRIVILEGES TRIGGER AUDIT EXCLUSIVE LOCK PUBLIC UID BETWEEN EXISTS LONG RAW UNION BY FILE MAXEXTENTS RENAME UNIQUE FROM FLOAT MINUS RESOURCE UPDATE CHAR FOR MLSLABEL REVOKE USER CHECK SHARE MODE ROW VALIDATE CLUSTER GRANT MODIFY ROWID VALUES COLUMN GROUP NOAUDIT ROWNUM VARCHAR COMMENT HAVING NOCOMPRESS ROWS VARCHAR2 COMPRESS IDENTIFIED NOWAIT SELECT VIEW CONNECT IMMEDIATE NULL SESSION WHENEVER CREATE IN NUMBER SET WHERE CURRENT INCREMENT OF SIZE WITH DATE INDEX OFFLINE SMALLINT ";

  private boolean isCreateTable = false;
  
  private boolean isClearTable = false;
  
  private IProgress ipro;
  
  //导入单个dbf文件
  public void importDbf(String dbFilefPath, Connection conn, String tableName) {
//    addLog("开始导入dbf文件：" + FileFunc.extractFileName(dbFilefPath));
	  addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.startimp", "开始导入dbf文件：{0}", new Object[]{FileFunc.extractFileName(dbFilefPath)}));
    try {
      DBFReader reader = new DBFReader(new File(dbFilefPath));
      try {
        Dialect dialect = SqlFunc.createDialect(conn);
        DbDefiner definer = dialect.createDbDefiner();
        try {
          int[] columnInfor = getInfor(conn, definer, reader, tableName);
          if (columnInfor == null)
            return;
          int columnCount = columnInfor[0];
          int bigFieldCount = columnInfor[1];
          EtlDataDest_DefaultImpl defImpl = new EtlDataDest_DefaultImpl(conn,
              tableName);
          //每step条记录，执行一次批插入,count为计数器
          //如果包含大字段，每1条记录就commit 1次，否则每1000条记录commit 1次
          try {
            int step = bigFieldCount > 0 ? 1 : 1000;
            int count = 0;
            int nowCount = 0;
            DBFRecordSet rs = reader.getDbfRecordSet();
            while (rs.hasNext()) {
              if (count <= step) {
                for (int i = 0; i < columnCount; i++) {
                  defImpl.setValue(i, rs.getObject(i));
                }
                defImpl.postARow();
                count++;
                nowCount++;
              }
              else {
                defImpl.postAll();
                count = 0;
              }
//              setLastLog("成功导入第"+nowCount+"行");
              setLastLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.impnl", "成功导入第{0}行", new Object[]{String.valueOf(nowCount)}));
            }
            if (count != 0)
              defImpl.postAll();
//            addLog("成功导入dbf文件：" + FileFunc.extractFileName(dbFilefPath)+" 共"+nowCount+"行");
            Object[] param=new Object[]{FileFunc.extractFileName(dbFilefPath),String.valueOf(nowCount)};
            addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.impdbfsuc", "成功导入dbf文件：{0}共{1}行", param));
          }
          finally {
            defImpl.closePreparedStatement();
          }
        }
        finally {
          definer.clearDefineInfo();
        }
      }
      finally {
        reader.close();
      }
    }
    catch (Exception ex) {
      ipro.addLog(StrFunc.exception2str(ex));
      ExceptionHandler.rethrowRuntimeException(ex);
    }
  }

  public void importText(String fn, Connection conn, String table, char colsep) {
    // TODO 自动生成方法存根

  }

  public void setIprogress(IProgress ipro) {
    this.ipro = ipro;
  }
  
  public void setAutoCreateTable(boolean v) {
    this.isCreateTable = v;    
  }

  public void setClearTableFirst(boolean v) {
    this.isClearTable = v;
  }

  //(创建或找到目的数据库表)返回一个包含两个元素的数组,第一个元素为该dbf的字段数,第二个元素为memo型字段（大字段）的个数
  private int[] getInfor(Connection conn, DbDefiner definer, DBFReader reader, String table) throws Exception {
    boolean isTableExist = definer.tableExists(conn, null, table);
    if (!isTableExist && isCreateTable) {
      return createTable(conn, definer, reader, table);
    }
    else if (isTableExist && isClearTable) {
      return deleteRecords(conn, definer, reader, table);
    }
    else if (isTableExist && !isClearTable) {
      return getDBFInfor(reader);
    }
    return null;
  }

  //根据dbf文件中的信息,创建表结构
  private int[] createTable(Connection conn, DbDefiner definer, DBFReader reader, String table) throws Exception {
//    addLog("开始创建表："+table);
	  addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.startcreateta", "开始创建表：{0}", new Object[]{table}));
    int[] columnInfor = { -1, 0 };
    definer.clearDefineInfo();
    int fieldCount = reader.getDbfFieldCount();
    columnInfor[0] = fieldCount;
    for (int i = 0; i < fieldCount; i++) {
      DBFField field = reader.getDbfField(i);
      String fieldName = field.getName();
      if (RESERVED_WORDS.indexOf(" " + fieldName + " ") != -1)
        fieldName += "_";
      char fieldType = (char) field.getType();
      switch (fieldType) {
        case 'C'://character
          definer.defineStringField(fieldName, field.getLength(), null, true, false);
          break;
        case 'N'://numeric
          definer.defineFloatField(fieldName, field.getLength(), field.getDecimal(), null, true, false);
          break;
        case 'F'://float
          definer.defineFloatField(fieldName, field.getLength(), field.getDecimal(), null, true, false);
          break;
        case 'D'://date
          definer.defineDateField(fieldName, null, true, false);
          break;
        case 'B'://double
          definer.defineIntField(fieldName, field.getLength(), null, true, false);
          break;
        case 'I'://integer
          definer.defineIntField(fieldName, field.getLength(), null, true, false);
          break;
        case 'L'://logical
          definer.defineLogicField(fieldName, null, true, false);
          break;
        case 'M'://memo
          definer.defineMemoField(fieldName, null, true, false);
          columnInfor[1]++;
          break;
        default:
          definer.defineStringField(fieldName, field.getLength(), null, true, false);
      }
    }
    definer.createTable(conn, null, table);
//    addLog("成功创建表"+table);
    addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.createtasuc", "成功创建表{0}", new Object[]{table}));
    return columnInfor;
  }

  //清空表
  private int[] deleteRecords(Connection conn, DbDefiner definer, DBFReader reader, String table) throws Exception {
//    addLog("开始清空表："+table+" 的数据");
	  addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.cleartastart", "开始清空表：{0} 的数据", new Object[]{table}));
    Statement stmt = conn.createStatement();
    try {
      String deleteSQL = "delete from " + table;
      stmt.executeUpdate(deleteSQL);
//      addLog("成功清空表："+table+" 的数据");
      addLog(I18N.getString("com.esen.jdbc.etl.impl.easyetlimpl.cleartasuc", "成功清空表：{0} 的数据\"", new Object[]{table}));
    }
    finally {
      stmt.close();
    }
    return getDBFInfor(reader);
  }

  //获取表中字段总数及'M'字段(大字段)的个数
  private int[] getDBFInfor(DBFReader reader) throws Exception {
    int[] columnInfor = { -1, 0 };
    int fieldCount = reader.getDbfFieldCount();
    int bigFieldCount = 0;
    for (int i = 0; i < fieldCount; i++) {
      DBFField field = reader.getDbfField(i);
      char fieldType = (char) field.getType();
      if (fieldType == 'M')
        bigFieldCount++;
    }
    columnInfor[0] = fieldCount;
    columnInfor[1] = bigFieldCount;
    return columnInfor;
  }
  
  private void addLog(String log) {
    if (this.ipro != null) {
      this.ipro.checkCancel();
      this.ipro.addLog(log);
    }
  }
  
  private void setLastLog(String log) {
    if (this.ipro != null) {
      this.ipro.checkCancel();
      this.ipro.setLastLog(log);
    }
  }
}
