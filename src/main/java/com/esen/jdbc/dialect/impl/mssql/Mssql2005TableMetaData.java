package com.esen.jdbc.dialect.impl.mssql;

import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * 20090929
 * SqlServer2000也同样的实现，改到父类；
 * @author Administrator
 *
 */
public class Mssql2005TableMetaData extends MssqlTableMetaData {
  
  public Mssql2005TableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }
  
  /**
   * sqlserver2005 获取表结构sql；
   * 
   */
  protected String getFieldPropsSql(){
    String sql = "SELECT \n"
      +"        d.name as tbname,\n"
      +"        a.name as fieldname,\n"
      +"        COLUMNPROPERTY( a.id,a.name,'IsIdentity') as isIdentity,\n"
      +"        b.name as typename,\n"
      +"        a.length as length,\n"
      +"        COLUMNPROPERTY(a.id,a.name,'PRECISION') as prec,\n"
      +"        COLUMNPROPERTY(a.id,a.name,'Scale') as scale,\n"
      +"        a.isnullable as isnullable,\n"
      +"        e.text as defaultvalue,\n"
      +"        cast(g.[value] as varchar) as description\n"
      +" FROM syscolumns a\n"
      +"        left join systypes b on a.xtype=b.xusertype\n"
      +"        inner join sysobjects d on a.id=d.id  and (d.xtype='U' or d.xtype='V')\n"
      +"        left join syscomments e on a.cdefault=e.id\n"
      +"        left join sys.extended_properties g on a.id=g.major_id and a.colid=g.minor_id \n"
      +" where a.id=object_id('"+getTableName()+"')\n"
      +" order by a.id,a.colorder";
    return sql;
  }
}
