package com.esen.jdbc.dialect.impl.mssql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;
/**
 *
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public class MssqlDef extends DbDef {

  public static final int MaxPrecision = 38;
  
  /**
   * 字段的排序规则定义
   */
  private String collateddl;
  
  public MssqlDef(Dialect dl) {
    super(dl);
    
    /*
     * ISSUE:BI-6480:added by liujin 2013.04.09
     * 字符类型的列的排序规则，区分大小写
     */
    MssqlDataBaseInfo dbinfo = (MssqlDataBaseInfo) dl.getDataBaseInfo();
    this.collateddl = getCollateDdl(dbinfo.getCollation());
  }

  /**
   * 20091022
   * SqlServer varchar类型最大长度是 8000
   * 索引、主键 列组合长度没有限制；
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    //检查字段
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
      /**
       * Sqlserver varchar类型最大长度是8000
       */
      adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},dbinf.getMaxVarcharFieldLength());
    }
  }
  
  public boolean tableOrViewExists(Connection conn,String tvname)throws SQLException{
    String sql = "select name from sysobjects where (xtype='U' or xtype='V') and id=object_id('"+tvname+"')";
    return excuteSql(conn,sql);
  }
  
  /**
   * 20090915
   * SqlServer2005查询表是否存在，改为通过查询系统表实现；
   * 原来的代码通过dbmeta.getTable(...)实现，需要指定schema，一般的数据库都是用户名，
   * sqlserver2005登录用户和schema没有直接的关系，一个登录用户可以有多个schema，比如如果设置了db_owner角色，
   * 它的默认schema就变成dbo, jdbc程序没发知道登录用户的默认schema，如果还是用用户名做schema，则查不到用户表；
   * 所有用到jdbc系统方法：getTable,getColumn,getPrimaryKeys,getIndexes方法的地方都需要改动；
   * 如：获取表结构，列出所有用户表，视图名等；
   * select * from sysobjects where xtype='U' and name='t_test'
   * 
   * 20090929
   * select name from sysobjects where xtype='U' and uid=user_id() and name='test_1'
   * select name from sysobjects where xtype='U' and id=object_id('test_1')
   * 1) 这里表名 test_1 不区分大小写，
   * 2) 这两个sql是等价的，表test_1可能存在与不同的"所有者"，
   * 第二个sql没有要 uid=user_id() 条件，是因为object_id('')函数的参数可以指定表名的所有者：object_id('bi2.test_1'),
   * 不指定所有者，则将在默认所有者中查找；
   * 3) 条件uid=user_id()  表示查的是登录用户的默认所有者；
   * 一般情况登录用户和其默认所有者是一致的，如果给了服务器角色system administrators 权限，其对应的数据表所有者变成dbo；
   * 
   * 原来的代码，是在所属数据库实例的所有"所有者"中找指定的表名，可能会有重复的表，造成结果不准确；
   * 这里的实现是在默认所有者中找指定的表名；
   * 
   */
  public boolean tableExists(Connection conn, String catalog, String tablename) throws SQLException {
    String sql = "select name from sysobjects where xtype='U' and id=object_id('"+tablename+"')";
    return excuteSql(conn,sql);
  }
  
  public boolean viewExists(Connection conn,String viewname) throws SQLException{
    String sql = "select name from sysobjects where xtype='V' and id=object_id('"+viewname+"')";
    return excuteSql(conn,sql);
  }
  

  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TEXT " + getCollateDdl() + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
  }
  public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
    	//BI-6685,临时修改，正式修改TODO
			/*
			 * BUG:BI-8144: added by liujin 2013.04.03
			 * 修改该存储过程的名字，使用小写形式。
			 * 避免数据库排序规则为区分大小写时找不到该存储过程
			 */
      ddl.execute("sp_rename " + getRealTablename(oldname)+","+ getRealTablename(newname));
    }
    finally {
      ddl.close();
    }
  }
  
  /*
   * SP_RENAME AAA,dbo.alter_2935
   * 执行这样的重命名时，会报错，提示"."附近语法错误，去掉schema前缀后正常
   * 先临时修改为截取schema
   * 
   * 正式修改TODO
   */
  private String getRealTablename(String tablename){
  	if(StrFunc.isNull(tablename) || tablename.indexOf(".")<0) return tablename;
  	return tablename.substring(tablename.indexOf(".")+1,tablename.length());
  }
  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " IMAGE " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
  }


  public boolean indexExists(Connection conn,String tbname, String indexname) throws SQLException {
    String sql = "select name from sysindexes " +
        "where name='" + indexname + "'";
    //System.out.println(sql);
    Statement stmt = conn.createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
      return ( (rs.next() ? true : false));
    }
    finally {
      rs = null;
      stmt.close();
    }
  }

  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
    return (thisField + " INT IDENTITY(1, " + step + ") ");
  }
  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TEXT " + getCollateDdl() + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_CLOB, isUpdate);
  }
  //mssql 数据库定义时间类型关键字：DATATIME,不支持TIME
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATETIME " +
        getTailDdl(fi.getDefaultValue(), fi.isNullable(),fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }
  /**
   * 返回指定表中的字段的索引名，主键名，unique约束名，default约束名；
   * 查询指定表的主键，unique字段，索引，对应的字段名和对应的主键，unique约束，索引名：
   * xtype= 'PK' 表示主键，'UQ' 表示unique约束名 ，'D' 表示default约束名， null表示索引名；

select c.name as fieldname, i.name as indexname,o.xtype as xtype
from sysindexkeys k
left join sysindexes i on k.id=i.id and k.indid=i.indid
left join syscolumns c on c.id=k.id and c.colid=k.colid
left join sysobjects o on i.id=o.parent_obj and i.name=o.name
where  k.id=object_id('test_1')
union
select b.name,c.name, 'D' as xtype
from sysconstraints a
inner join syscolumns b on a.id=b.id and a.colid=b.colid
inner join sysobjects c on a.constid=c.id
where a.id=object_id('test_1')

   * 返回一个数组的集合，每个数组有3个元素：{字段名，对象名，类型}
   * 类型对应xtype值；
   * @param conn
   * @param tablename
   * @param col
   * @return
   * @throws SQLException 
   */
  private List getIndexName(Connection conn, String tablename, String col) throws SQLException{
    StringBuffer sql = new StringBuffer(128);
    sql.append("select c.name as fieldname, i.name as indexname,o.xtype as xtype\n");
    sql.append("from sysindexkeys k\n");
    sql.append("left join sysindexes i on k.id=i.id and k.indid=i.indid\n");
    sql.append("left join syscolumns c on c.id=k.id and c.colid=k.colid\n");
    sql.append("left join sysobjects o on i.id=o.parent_obj and i.name=o.name\n");
    sql.append("where k.id=object_id('").append(tablename).append("') and c.name='").append(col).append("'\n");
    sql.append("union\n");
    sql.append("select b.name,c.name, 'D' as xtype\n");
    sql.append("from sysconstraints a\n");
    sql.append("inner join syscolumns b on a.id=b.id and a.colid=b.colid\n");
    sql.append("inner join sysobjects c on a.constid=c.id\n");
    sql.append("where a.id=object_id('").append(tablename).append("') and b.name='").append(col).append("'\n");
    Statement stmt = conn.createStatement();
    List l = new ArrayList();
    try {
        ResultSet rs = stmt.executeQuery(sql.toString());
        while(rs.next()){
          String[] v = new String[]{rs.getString(1),rs.getString(2),rs.getString(3)};
          l.add(v);
        }
    }
    finally {
      stmt.close();
    }
    if(l.size()>0)
      return l;
    return null;
  }
  /**
   * 删除字段；
   * sqlserver 中如果字段有索引,unique约束，default值等约束，将删除不成功；
   * 需要先将这些约束删除，在删字段；
   */
  public void dropColumn(Connection conn, String tablename, String col) throws SQLException {
    List ddls = new ArrayList();
    List indx = getIndexName(conn,tablename,col);
    if(indx!=null){
      for(int i=0;i<indx.size();i++){
        String[] ind = (String[])indx.get(i);
        String xtype = ind[2];
        if(xtype!=null&&(xtype.equals("PK")||xtype.equals("UQ")||xtype.equals("D"))){
          //删除主键，uniquey约束，default约束
          StringBuffer addl = new StringBuffer(32).append("ALTER TABLE ");
          addl.append(tablename).append(" drop constraint ").append(ind[1]);
          ddls.add(addl.toString());
        }else{
          //删除索引
          StringBuffer addl = new StringBuffer(32).append("DROP INDEX ");
          addl.append(tablename).append(".").append(ind[1]);
          ddls.add(addl.toString());
        }
      }
    }
    //删除约束；
    if(ddls.size()>0){
      String[] ddlsql = new String[ddls.size()];
      ddls.toArray(ddlsql);
      executeSql(conn,ddlsql);
    }
    //删除字段；
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" DROP COLUMN ").append(col);
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }
  
  public void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException{
    StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" alter column ");
    ddl.append(getFieldDefine(coltype,fieldname,len,scale));
    /**
     * 20091022
     * 设置conn.setAutoCommit(false)，执行修改字段属性的sql后，如果不提交事务，
     * 则其他线程获取连接后，执行sql发生堵塞，直到事务提交；
     * 测试用例：testTranslationForAlter()
     */
    boolean f = conn.getAutoCommit();
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(ddl.toString());
      if(!f){//如果在一个事务中，执行修改字段，则修改后，立即提交事务；
        conn.commit();
      }
    }
    finally {
      stmt.close();
    }
  }
  
  private String getFieldDefine(char coltype, String fieldname, int len, int scale) throws SQLException {
    switch (coltype) {
      case DbDefiner.FIELD_TYPE_STR:
        return fieldname + " VARCHAR("+len+")";
      case DbDefiner.FIELD_TYPE_INT:
        return fieldname + " INT";
      case DbDefiner.FIELD_TYPE_FLOAT:{
        int[] prcs = formatNumberPrecision(len,scale,MaxPrecision);
        if(prcs[0]==0){
          return fieldname + " FLOAT";
        }
        return fieldname + " NUMERIC("+prcs[0]+","+prcs[1]+")";
      }
      case DbDefiner.FIELD_TYPE_DATE:
        return fieldname + " DATE ";
      case DbDefiner.FIELD_TYPE_TIME:
        return fieldname + " DATETIME ";
      case DbDefiner.FIELD_TYPE_TIMESTAMP:
        return fieldname + " DATETIME ";
      case DbDefiner.FIELD_TYPE_LOGIC:
        return fieldname + " VARCHAR(1)";
      case DbDefiner.FIELD_TYPE_memo:
        return fieldname + " TEXT";
      case DbDefiner.FIELD_TYPE_CLOB:
        return fieldname + " TEXT";
      case DbDefiner.FIELD_TYPE_BINARY:
        return fieldname + " IMAGE";
      default:
        throw new SQLException(
            "database not support to define this type of field,type:" +coltype);
    }
  }
  
  /**
   * 修改表结构
   * 读取源表字段的原有属性，进行比较再做修改；
   * 比较特殊的地方：
   * 1. 增加unique属性：
   *    alter table test_2 add  UNIQUE(field1)
   *    但是如果field1本来就有unique属性，会报错；
   *    这里需要判断field是否需要增加unique属性；
   * 2. 去除unique属性：
   *    查找unique约束名：
   *    查询字段对应的索引名：可以查所有索引名，
   *    包括：主键，字段的unique约束名，一般索引名；但是无法区分他们；
   *    select c.name as fieldname, i.name as indexname
   *    from sysindexkeys k,sysindexes i,syscolumns c
   *    where k.id=i.id and k.indid=i.indid
   *    and  c.id=k.id and c.colid=k.colid
   *    and k.id=object_id('test_2') 
   *    扩展上面的，带上索引类型， xtype: 'UQ' 表示唯一索引 ；'PK' 表示主键
   *    这个sql只能查主键和unique约束名；不能查一般索引名；
   *    select c.name as fieldname, i.name as indexname,o.xtype as xtype
   *    from sysindexkeys k,sysindexes i,syscolumns c, sysobjects o
   *    where k.id=i.id and k.indid=i.indid
   *    and  c.id=k.id and c.colid=k.colid
   *    and  i.id=o.parent_obj and i.name=o.name and o.xtype='UQ'
   *    and k.id=object_id('表名') and c.name='字段名'
   *    删除唯一约束：
   *    Alter table 表名 drop constraint 约束名=(indexname)
   *    
   * 3. 修改default值：
   *    ALTER TABLE 表名 add DEFAULT ('修改后的默认值') for 字段名 WITH VALUES
   *    注：如果该字段以前已经有默认值了，在修改之前需要先将约束删除，否则会报错
   *    删约束的ＳＱＬ：
   *    Alter table 表名 drop constraint 约束名
   *    关于约束名，需要先查一下,
   *    查约束名的ＳＱＬ：
   *    select c.name from sysconstraints a
   *    inner join syscolumns b on a.id=b.id and a.colid=b.colid
   *    inner join sysobjects c on a.constid=c.id
   *    where a.id=object_id('表名')
   *    and b.name='字段名'
   * @param conn Connection
   * @param tablename String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param unique boolean
   * @param col String
   * @throws Exception
   */
  public void modifyColumn(Connection conn, String tablename, String col,String new_col,
                           char coltype, int len, int dec,String defaultvalue,boolean unique, boolean nullable) throws SQLException {
    if (col == null || col.length() == 0) {
//      throw new SQLException("修改列名不能为空！");
    	throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
    }
    Dialect dl = SqlFunc.createDialect(conn);
    /**
     * 优化代码，避免读取两次数据库获取表结构；
     */
    TableMetaDataHelper tbmd = (TableMetaDataHelper)dl.createDbMetaData(conn).getTableMetaData(tablename);
    tbmd.getColumnCount();//初始化表字段；
    TableColumnMetaData tcol = tbmd.getColumn( col);
    if(tcol==null)
//      throw new SQLException("数据库表"+tablename+"没有字段："+col);
    	throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.mssql.mssqldef.nosuchfield", "表{0}没有字段：{1}",new Object[]{tablename,col}));
    List ddl = new ArrayList();
    String col2 = getColumnName(dl, col);
    //sql server 字段名不区分大小写
    if(new_col!=null&&new_col.length()>0&&!col.equalsIgnoreCase(new_col)){
      //需要修改字段名
			/*
			 * BUG:BI-8144: added by liujin 2013.04.03
			 * 修改该存储过程的名字，使用小写形式。
			 * 避免数据库排序规则为区分大小写时找不到该存储过程
			 */
      StringBuffer renddl = new StringBuffer(32).append("sp_rename ");
      renddl.append("'").append(tablename).append(".").append(col2).append("','").append(new_col).append("','COLUMN'");
      ddl.add(renddl.toString());
      col2 = getColumnName(dl, new_col);
    }
    boolean deled = false;
    
    /**
     * 20091102
     * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
     */
    
    len = this.adjustFieldLengthForModify(tbmd, col2, len);
    //修改其他属性，类型，长度，是否为空
    //当字段有unique约束，将为空转为不空，会报有对象依赖该列，不能修改的提示；反过来将不空转为空可以；
    if (isNotEqual(SqlFunc.getSubsectionType(tcol.getType()), coltype) 
        || (coltype=='C' && len!=tcol.getLen())//只有字符类型和数值类型才比较长度；
        || (coltype=='N' && (len!=tcol.getLen()||dec!=tcol.getScale()))
        || tcol.isNullable() != nullable) {
      if(tcol.isUnique()&&!nullable){
        //原字段unique，且不修改为非unique,空转不空，删除unique
        deleteUniqueSql(conn, tablename, col2, ddl);
        deled = true;
      }
      StringBuffer altstr = new StringBuffer(64).append("alter table ");
      altstr.append(tablename);
      altstr.append(" alter column ");
      altstr.append(getFldDdl(new TableColumnMetaDataHelper(col2, coltype, len, dec, nullable, false,null,null), false));
      ddl.add(altstr.toString());
    }
    if(deled&&unique){
      //还原被删除的unique
      addUniqueSql(tablename, ddl, col2);
    }
    if(tcol.isUnique()!=unique){
      //需要修改unique属性；
      if(unique){
        //增加unique属性
        addUniqueSql(tablename, ddl, col2);
      }else if(!deled){
        //删除unique属性
        deleteUniqueSql(conn, tablename, col2, ddl);
      }
    }
    if(!StrFunc.compareStr(defaultvalue, tcol.getDefaultValue())){
      if(defaultvalue==null){
        //删除原有default值
        String deleteDefaultValueSql = deleteDefaultValueSql(conn, tablename, col);
        if(deleteDefaultValueSql!=null)
          ddl.add(deleteDefaultValueSql);
      }else if(defaultvalue.length()>0){
        if(tcol.getDefaultValue()==null){
          //增加default值；ALTER TABLE 表名 add DEFAULT ('修改后的默认值') for 字段名 WITH VALUES
          ddl.add(addDefaultValueSql(tablename, defaultvalue, col2));
        }else{
          //修改default值；
          String deleteDefaultValueSql = deleteDefaultValueSql(conn, tablename, col);
          if(deleteDefaultValueSql!=null)
            ddl.add(deleteDefaultValueSql);
          ddl.add(addDefaultValueSql(tablename, defaultvalue, col2));
        }
      }
    }
    
    if(ddl.size()>0){
      String[] ddlsql = new String[ddl.size()];
      ddl.toArray(ddlsql);
      executeSql(conn,ddlsql);
    }
  }
  
  /**
   * 如果原类型srcType和目标类型coltype不一致，返回true；
   * 1)由于SqlServer没有Time类型，定义的'T'类型使用timestamp('P')类型代替；
   *   所以如果数据库表的类型srcType='P' ，目标类型coltype='T' 其实是一致的；
   * @param srcType
   * @param coltype
   * @return
   */
  private boolean isNotEqual(char srcType, char coltype) {
    if(srcType=='P'&&coltype=='T')
      return false;
    return srcType!=coltype;
  }
  
  private void addUniqueSql(String tablename, List ddl, String col2) {
    StringBuffer addl = new StringBuffer(32).append("ALTER TABLE ");
    addl.append(tablename).append(" ADD UNIQUE(");
    addl.append(col2).append(")");
    ddl.add(addl.toString());
  }
  private void deleteUniqueSql(Connection conn, String tablename, String col, List ddl) throws SQLException {
    String[] ucns = getConstraintName(conn,tablename,col,"UQ");
    if(ucns!=null){
      for(int i=0;i<ucns.length;i++){
        StringBuffer addl = new StringBuffer(32).append("ALTER TABLE ");
        addl.append(tablename).append(" drop constraint ").append(ucns[i]);
        ddl.add(addl.toString());
      }
    }
  }
  /**
   * 获得指定字段的索引约束名；
   * 可能有多个；
   *    select i.name 
   *    from sysindexkeys k,sysindexes i,syscolumns c, sysobjects o
   *    where k.id=i.id and k.indid=i.indid
   *    and c.id=k.id and c.colid=k.colid
   *    and i.id=o.parent_obj and i.name=o.name and o.xtype='UQ'
   *    and k.id=object_id('表名') and c.name='字段名'
   * @param conn
   * @param tablename
   * @param col
   * @param xtype
   *          ='UQ' 表示 唯一索引；'PK' 表示主键;  为空表示所有类型索引；
   * @return
   * @throws SQLException 
   */
  private String[] getConstraintName(Connection conn, String tablename, String col,String xtype) throws SQLException {
    StringBuffer sql = new StringBuffer(64);
    sql.append("select i.name\n");
    sql.append("from sysindexkeys k,sysindexes i,syscolumns c\n");
    if(xtype!=null){
      sql.append(", sysobjects o\n");
    }
    sql.append("where k.id=i.id and k.indid=i.indid\n");
    sql.append("and c.id=k.id and c.colid=k.colid\n");
    if(xtype!=null){
      sql.append("and i.id=o.parent_obj and i.name=o.name\n");
      sql.append("and o.xtype='").append(xtype).append("'\n");
    }
    sql.append("and k.id=object_id('").append(tablename).append("') and c.name='").append(col).append("'");
    Statement stmt = conn.createStatement();
    List l = new ArrayList();
    try {
        ResultSet rs = stmt.executeQuery(sql.toString());
        while(rs.next()){
          l.add(rs.getString(1));
        }
    }
    finally {
      stmt.close();
    }
    if(l.size()>0){
      String[] ucn = new String[l.size()];
      l.toArray(ucn);
      return ucn;
    }
    return null;
  }
  private String addDefaultValueSql(String tablename, String defaultvalue, String col2) {
    StringBuffer addl = new StringBuffer(32).append("ALTER TABLE ");
    addl.append(tablename).append(" add DEFAULT ").append(defaultvalue).append(" for ");
    addl.append(col2).append(" WITH VALUES");
    return addl.toString();
  }
  private String deleteDefaultValueSql(Connection conn, String tablename, String col) throws SQLException {
    String defname = getDefautConstraintName(conn,tablename,col);
    if(defname!=null){
      //Alter table 表名 drop constraint 约束名
      StringBuffer addl = new StringBuffer(32).append("ALTER TABLE ");
      addl.append(tablename).append(" drop constraint ").append(defname);
      return addl.toString();
    }
    return null;
  }
  /**
   * 查约束名的ＳＱＬ：
   *    select c.name from sysconstraints a
   *    inner join syscolumns b on a.id=b.id and  a.colid=b.colid
   *    inner join sysobjects c on a.constid=c.id
   *    where a.id=object_id('表名')
   *    and b.name='字段名'
   * @param conn
   * @param tablename
   * @param col
   * @return
   * @throws SQLException 
   */
  private String getDefautConstraintName(Connection conn, String tablename, String col) throws SQLException {
    StringBuffer sql = new StringBuffer(64);
    sql.append("select c.name from sysconstraints a\n");
    sql.append("inner join syscolumns b on a.id=b.id and a.colid=b.colid\n");
    sql.append("inner join sysobjects c on a.constid=c.id\n");
    sql.append("where a.id=object_id('").append(tablename).append("')\n");
    sql.append("and b.name='").append(col).append("'");
    Statement stmt = conn.createStatement();
    try {
        ResultSet rs = stmt.executeQuery(sql.toString());
        if(rs.next()){
          return rs.getString(1);
        }
    }
    finally {
      stmt.close();
    }
    return null;
  }
  private void executeSql(Connection conn,String[] sqls) throws SQLException{
    Statement stmt = conn.createStatement();
    try {
      for(int i=0;i<sqls.length;i++)
        stmt.execute(sqls[i]);
    }
    finally {
      stmt.close();
    }
  }
  protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " INT " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_INT, isUpdate);
  }
  /**
   * ALTER TABLE 只允许添加可包含空值或指定了 DEFAULT 定义的列。
   * 因为列 'field4' 不能包含空值且未指定 DEFAULT 定义，所以无法将该列添加到表 'test_1' 中。
   * SQL如下：
   * ALTER TABLE test_1 ADD field4 DATETIME  UNIQUE  NOT NULL
   * 
   * 主要用于增加字段；
   */
  protected String getTailDdl(String defaultvalue, boolean nullable,
      boolean unique,String desc,char t, boolean isUpdate) {
    if(isUpdate){
      StringBuffer str = new StringBuffer(16); 
      if(unique) str.append(" UNIQUE ");
      if(!nullable) {
        //非空，必须有defualt值
        str.append(" NOT NULL ");
        if (defaultvalue != null && defaultvalue.length()>0) {
          str.append(" DEFAULT ").append(defaultvalue);
        }else{
          str.append(getDefualtSql(defaultvalue, t));
        }
      }else{
        //可空，根据参数定义default值；
        if (defaultvalue != null && defaultvalue.length()>0) {
          str.append(" DEFAULT ").append(defaultvalue);
        }
      }
      return str.toString();
    }else{
      return super.getTailDdl(defaultvalue, nullable, unique, desc, t, isUpdate);
    }
  }
  /**
   * mssql 数值最大精度：38
   */
  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    StringBuffer numddl = new StringBuffer();
    numddl.append(getColumnName(fi.getName()));
    //如果长度为0，则不指定长度；
    if(fi.getLen()>0){
      numddl.append(" NUMERIC");
      int[] prcs = formatNumberPrecision(fi.getLen(),fi.getScale(),MaxPrecision);
      numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
    }else {
      //无法确定精度，可以使用double类型：
      numddl.append(" FLOAT");
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();
  }
  protected String getDateFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATETIME " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_DATE, isUpdate);
  }
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATETIME " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
  }

	/**
	 * {@inheritDoc}
	 */
	protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		return getColumnName(fi.getName())
				+ " VARCHAR(" 
				+ (fi.getLen() > 0 ? fi.getLen() : 1)
				+ ") "
				+ getCollateDdl()
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_STR,
						isUpdate);
	}

	/*
	 * ISSUE:BI-6480:added by liujin 2013.04.08
	 * MSSQL 在默认情况下，列的排序规则与数据库一致。
	 * 当数据库不区分大小写时，列也不区分大小写。
	 * 修改为在创建表时，在列上添加区分大小写的排序规则，避免从其他数据库导入数据时主键冲突。
	 */
	/**
	 * 获取字段排序规则定义
	 * @return 排序规则定义
	 */
	private String getCollateDdl(String collation) {
		if (StrFunc.isNull(collation))
			return "";

		int i = collation.indexOf("_CI"); // CI 表示不区分大小写
		int j = collation.indexOf("_CS"); // CS 表示区分大小写

		/*
		 * 数据库的排序不区分大小写，列定义中需添加排序规则定义
		 * 将排序规则名称中的 CI 改成  CS
		 */
		if (i > 0 && j < 0) {
			/*
			 * ESENBI-2488: modify by liujin 2014.11.19
			 * 恢复为使用默认的排序规则，避免因大小写带来的诸多问题
			 * 
			 * TODO
			 * 会存在类似 oracle 的数据迁移过来出错的问题
			 */
			//return " COLLATE " + collation.substring(0, i) + "_CS" + collation.substring(i + 3);
		}

		//数据库的排序规则区分大小写，列定义中不需添加额外定义
		return "";
	}

	/**
	 * 获取字段排序规则定义
	 * @return 排序规则定义
	 */
	public String getCollateDdl() {
		return this.collateddl;
	}

}
