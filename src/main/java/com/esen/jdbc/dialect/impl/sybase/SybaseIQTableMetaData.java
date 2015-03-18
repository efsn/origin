package com.esen.jdbc.dialect.impl.sybase;

import java.sql.*;

import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * SybaseIQ获取表结构实现类；
 * @author dw
 *
 */
public class SybaseIQTableMetaData extends TableMetaDataImpl {

  public SybaseIQTableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
    getRealTablename();
    
  }
  /**
   * 20090317
   * 获取数据库中真实的表名；
   * select tname from SYS.SYSCOLUMNS 
   * where creator = 'bi21demo' and upper(tname)=upper('t_test')
   * group by creator,tname 
   * 
   * SybaseIQ 在通过meta.getPrimaryKeys() 和meta.getIndexes() 时，需要真实的表名，此表名区分大小写；
   * 在sql中SybaseIQ不区分大小写，比如 select *  from tbname ， 这里tbname 不区分大小写；
   * 
   */
  private void getRealTablename() {
	  String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
    try {
      Connection con = this.owner.getConnection();
      String tsql = "select tname from SYS.SYSCOLUMNS " +
      "where creator = '"+tbs[0]+"' and upper(tname)=upper('"+tbs[1]+"') " +
      "group by creator,tname ";
      try {
        Statement sm = con.createStatement();
        try {
          ResultSet rs = sm.executeQuery(tsql);
          if(rs.next()){
            tablename = rs.getString(1).trim();
          }
        }
        finally {
          sm.close();
        }
      }
      finally {
        this.owner.closeConnection(con);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
  protected void initIndexes() throws Exception {
    super.initIndexes();
    if(indexlist==null||indexlist.size()==0){
      return;
    }
    /**
     * SybaseIQ 创建表时为每个字段创建默认的索引，这里只返回手工额外定义的索引；
     * 默认的索引名都以"ASIQ_IDX_"开头，根据此过滤；
     */
    for(int i=indexlist.size()-1;i>=0;i--){
      TableIndexMetaData idx = (TableIndexMetaData)indexlist.get(i);
      if(idx.getName().startsWith("ASIQ_IDX_")){
        indexlist.remove(i);
      }
    }
  }
  
  /**
   * 20090317
   * 改用从系统表获取字段的属性；
   * SELECT ( SELECT user_name FROM SYS.SYSUSERPERMS
WHERE user_id = SYSTABLE.creator ) user_name,
column_name, table_name,
( SELECT type_id FROM SYS.SYSDOMAIN
WHERE domain_id = SYSCOLUMN.domain_id ),
nulls, width, scale, pkey, column_id,
"default", SYSCOLUMN.remarks,SYSCOLUMN.max_identity
FROM SYS.SYSCOLUMN == SYS.SYSTABLE
where upper(table_name)=upper('T_TEST') and user_name='bi21demo'

   目前没办法获取是否是自动增长字段的属性；
   */
  protected void initColumns() throws Exception {
    Connection con = this.owner.getConnection();
    String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
    String tsql = "SELECT ( SELECT user_name FROM SYS.SYSUSERPERMS " + "WHERE user_id = SYSTABLE.creator ) user_name,"
        + "column_name, table_name," + "( SELECT type_id FROM SYS.SYSDOMAIN "
        + "WHERE domain_id = SYSCOLUMN.domain_id ) type_id," + "nulls, width, scale, pkey, column_id,"
        + "\"default\", SYSCOLUMN.remarks remarks,SYSCOLUMN.max_identity iden " + "FROM SYS.SYSCOLUMN == SYS.SYSTABLE "
        + "where user_name = '" + tbs[0] + "' and upper(table_name)=upper('" + tbs[1] + "') ";
    try {
      Statement sm = con.createStatement();
      try {
        ResultSet rs = sm.executeQuery(tsql);
        while (rs.next()) {
          String colname = rs.getString("column_name").trim();
          /**
           * 20090901 
           * 使用SybaseIQ的实现，处理date,time,timestamp类型返回值；
           */
          SybaseIQTableColumnMetaData column = new SybaseIQTableColumnMetaData(this, colname);
          column.setLable(colname);
          column.setType(rs.getInt("type_id"));
          column.setLength(rs.getInt("width"));
          column.setScale(rs.getInt("scale"));
          String nulls = rs.getString("nulls").trim();
          column.setNullable(nulls.equals("Y"));
          String def = rs.getString("default");
          column.setDefaultValue(getDefaultValue(def));
          String rem = rs.getString("remarks");
          column.setDesc(rem == null ? null : rem.trim());
          String pkey = rs.getString("pkey").trim();
          if (pkey.equals("Y"))
            column.setUnique(true);
          addColumn(column);
        }
      }
      finally {
        sm.close();
      }
    }
    finally {
      this.owner.closeConnection(con);
    }

  }

}
