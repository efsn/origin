package com.esen.jdbc.dialect.impl.mssql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;


/**
 * 20090929
 * SqlServer2000,2005都改用通过查系统表获取表结构，数据库列表；
 * 原因是由于权限的原因，登录名和数据库表所有者可能不一致，原来的程序通过登录名查询所属数据表，可能无法查到数据；
 * 
 * @author dw
 *
 */
public class MssqlTableMetaData extends TableMetaDataImpl {

  public MssqlTableMetaData(DbMetaDataImpl owner, String tablename) {
    super(owner, tablename);
  }

  protected void initColumns() throws Exception {
    //初始化columns变量，并设置字段名和字段类型、字段是否自增长属性；
    initColumnsTypeAndAutoInc();
    //获取字段其他字段属性；
    queryFieldProps();
  }
  
  
  /**
   * sqlserver 默认值读出来，数值都用(...)括起来，这里将外层的括号去掉；
   * 字符型，有'', 这里也将''去掉；
   */
  protected String getDefaultValue(String def){
    if(def!=null){
      if(def.startsWith("(")&&def.endsWith(")")){
        def = def.substring(1,def.length()-1);
        if(def.startsWith("(")&&def.endsWith(")")){
          def = def.substring(1,def.length()-1);
        }
      }
      
    }
    return super.getDefaultValue(def);
  }
  /**
   * 20090929
   * 获取查询表结构的sql，2000和2005有细微的差别；
   * sql结果集字段说明，按顺序：
   * 表明，字段名，是否自增长，类型名称，长度，数值精度，小数位数，是否为空，默认值，字段说明；
   * 其中是否自增长，是否为空，其值=1表示真；
   * 长度是占用字节数；
   * 
   * 类型名称是形如：int,varchar,...等字符串，无法转换成java.sql.Types中的类型数值；
   * 
   * @return
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
      +"        left join sysproperties g on a.id=g.id and a.colid=g.smallid \n"
      +" where a.id=object_id('"+getTableName()+"')\n"
      +" order by a.id,a.colorder";
    return sql;
  }
  
  private void queryFieldProps() throws Exception {
    Connection con = owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
        String sql = getFieldPropsSql();
        ResultSet rs = sm.executeQuery(sql);
        try{
          getFieldProps(rs);
        }finally{
          if(rs!=null)
            rs.close();
        }
      }
      finally {
        if(sm!=null)
          sm.close();
      }
    }
    finally {
      owner.closeConnection(con);
    }
  }

  /**
   * 结果集字段说明，按顺序：
   * 1表名，2字段名，3是否自增长，4类型名称，5长度，6数值精度，7小数位数，8是否为空，9默认值，10字段说明；
   * @param rs
   * @throws SQLException 
   */
  private void getFieldProps(ResultSet rs) throws SQLException {
    while (rs.next()) {
      String fieldname = rs.getString(2);
      MssqlTableColumnMetaData col = (MssqlTableColumnMetaData) getColumn(fieldname);
      col.setAutoInc(isTrue(rs.getString(3)));
      col.setLength(rs.getInt(5));

      char tc = SqlFunc.getType(col.getType());
      if (tc == DbDefiner.FIELD_TYPE_FLOAT || tc == DbDefiner.FIELD_TYPE_INT) {
        col.setLength(rs.getInt(6));
      }
      
			/* 
			 * ISSUE: BI-8143: added by liujin 2013.05.02
			 * 设置相应的标度值。
			 * 标度为空时，值为 0.
			 */
			col.setScale(rs.getInt(7));

      col.setNullable(isTrue(rs.getString(8)));
      col.setDefaultValue(getDefaultValue(rs.getString(9)));
      col.setDesc(rs.getString(10));
    }
  }


  private boolean isTrue(String v) {
    if(v!=null&&v.equals("1"))
      return true;
    return false;
  }


  private void initColumnsTypeAndAutoInc() throws Exception{
    Connection con = owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
        ResultSet rs = sm.executeQuery(SQL_COLUMN + getTableName()
            + " where 1>2");
        try {
          ResultSetMetaData rmeta = rs.getMetaData();
          int count = rmeta.getColumnCount();
          for (int i = 0; i < count; i++) {
            MssqlTableColumnMetaData col = new MssqlTableColumnMetaData(this,rmeta.getColumnName(i+1));
            col.setAutoInc(rmeta.isAutoIncrement(i+1));
            col.setType(rmeta.getColumnType(i+1));
            addColumn(col);
          }
        }
        finally {
          if(rs!=null)
            rs.close();
        }
      }
      finally {
        if(sm!=null)
          sm.close();
      }
    }
    finally {
      owner.closeConnection(con);
    }
  }
  
  
  /**
   * 查询主键字段
select c.name as fieldname, i.name as indexname,k.keyno as keyno,o.xtype as xtype
from sysindexkeys k
left join sysindexes i on k.id=i.id and k.indid=i.indid
left join syscolumns c on c.id=k.id and c.colid=k.colid
left join sysobjects o on i.id=o.parent_obj and i.name=o.name
where  c.id=object_id('test_1')  and o.xtype='PK'

keyno 是主键字段的顺序；
   * @throws Exception 
   */
  protected void initPrimaryKey()  {
    try{
    Connection con = owner.getConnection();
    try {
      Statement sm = con.createStatement();
      try {
        String sql = "select c.name as fieldname, i.name as indexname,k.keyno as keyno,o.xtype as xtype\n"
             +"from sysindexkeys k\n"
             +"left join sysindexes i on k.id=i.id and k.indid=i.indid\n"
             +"left join syscolumns c on c.id=k.id and c.colid=k.colid\n"
             +"left join sysobjects o on i.id=o.parent_obj and i.name=o.name\n"
             +"where  c.id=object_id('"+getTableName()+"')  and o.xtype='PK'";
        ResultSet rs = sm.executeQuery(sql);
        try {
          List l = new ArrayList(5);
          while(rs.next()){
            String[] key = new String[3];
            key[0] = rs.getString(2);//主键名
            key[1] = rs.getString(1);//主键字段
            key[2] = rs.getString(3);//主键序号
            l.add(key);
          }
          if(l.size()>0){
            Collections.sort(l,new Comparatored());
            String[] primarykey = new String[l.size()];
            for(int i=0;i<primarykey.length;i++){
              primarykey[i] = ((String[])l.get(i))[1];
            }
            this.setPrimaryKey(primarykey);
          }
        }
        finally {
          if(rs!=null)
            rs.close();
        }
      }
      finally {
        if(sm!=null)
          sm.close();
      }
    }
    finally {
      owner.closeConnection(con);
    }
    }catch(Exception ex){
      throw new RuntimeException(ex);
    }
  }
  
  /**
   * 查询索引；
select c.name as fieldname, i.name as indexname,k.keyno as keyno,o.xtype as xtype
from sysindexkeys k
left join sysindexes i on k.id=i.id and k.indid=i.indid
left join syscolumns c on c.id=k.id and c.colid=k.colid
left join sysobjects o on i.id=o.parent_obj and i.name=o.name
where  c.id=object_id('test_1')

xtype='UQ' 表示唯一索引；
xtype='PK' 表示主键索引；
xtype is null 为空表示一般索引；

查询索引，包含主键索引；
   */
  protected void initIndexes() throws Exception {
    List list = new ArrayList(10);
    try{
      Connection con = owner.getConnection();
      try {
        Statement sm = con.createStatement();
        try {
          String sql = "select c.name as fieldname, i.name as indexname,k.keyno as keyno,o.xtype as xtype\n"
               +"from sysindexkeys k\n"
               +"left join sysindexes i on k.id=i.id and k.indid=i.indid\n"
               +"left join syscolumns c on c.id=k.id and c.colid=k.colid\n"
               +"left join sysobjects o on i.id=o.parent_obj and i.name=o.name\n"
               +"where  c.id=object_id('"+getTableName()+"') ";
          ResultSet rs = sm.executeQuery(sql);
          try {
            while(rs.next()){
              String[] indexfield = new String[4];
              indexfield[0] = rs.getString(2);//索引名
              indexfield[1] = rs.getString(1);//索引字段
              indexfield[2] = rs.getString(3);//索引序号
              indexfield[3] = rs.getString(4);//索引类型，='UQ'表示唯一索引；
              list.add(indexfield);
            }
          }
          finally {
            if(rs!=null)
              rs.close();
          }
        }
        finally {
          if(sm!=null)
            sm.close();
        }
      }
      finally {
        owner.closeConnection(con);
      }
      }catch(Exception ex){
        throw new RuntimeException(ex);
      }
      initIndexes(list);
  }

  
  /**
   * list中是数组元素集合；
   * 每个元素结构：[索引名，索引字段名，索引序号，索引类型]
   * @param list
   * @throws SQLException
   */
  private void initIndexes(List list) throws SQLException {
    if(list.size()==0) return;
    Map indMap = new HashMap();
    for(int i=0;i<list.size();i++){
      String[] indfield = (String[])list.get(i);
      Object o = indMap.get(indfield[0]);
      if(o==null){
        List inds = new ArrayList(3);
        inds.add(indfield);
        indMap.put(indfield[0], inds);
      }else{
        List inds = (List)o;
        inds.add(indfield);
      }
    }
    Set indNames = indMap.keySet();
    Iterator it = indNames.iterator();
    int k=0;
    while(it.hasNext()){
      String indexname = (String)it.next();
      List fields = (List)indMap.get(indexname);
      Collections.sort(fields,new Comparatored());
      
      String[] cols = new String[fields.size()];
      boolean isUnique = false;
      for(int i=0;i<fields.size();i++){
        String[] fd = (String[])fields.get(i);
        cols[i] = fd[1];
        if(i==0){
          isUnique = "UQ".equals(fd[3])||"PK".equals(fd[3]);//主键索引也是唯一的；
        }
      }
      TableIndexMetaDataImpl imd = new TableIndexMetaDataImpl(indexname,cols,isUnique);
      this.addIndexMeta(imd);
    }
  }
}
