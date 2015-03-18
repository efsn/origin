package com.esen.jdbc.dialect.impl.oracle;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
------------------------------------------
 2004.12.10  oracle8i的lobSample中仍有bug，写入后无法清空原有数据；所以在第一次更新时需要先写入一个空的lob
 2004.11.27  在websphere上基于oracle9i填报数据时，大字段出错：
 java.lang.ClassCastException: com.ibm.ws.rsadapter.jdbc.WSJdbcConnection
 解决方案：
 1. 所有需要写大字段的位置调整，如果是oracle，分两次写入；如果一条记录有多个大字段，只需再提交一次；
   a. 上报数据；
   b. 写日志；如果信息不长就不用memo字段；成批提交时，将memo不空的单独逐条提交，其他的成批提交；
   c. 恢复备份的数据；yk处理
 2004.08.13      Oracle 仅支持单引号括起的串
 2004.07.08      ORACLE不支持bool型字段
 FROM DOCUMENT :
 2003.12.20 : oracle 中创建trigger的语句中用\r\n分隔，不接受；全部用空格分隔后，ok!；so,be careful of \r\n
 2003.12.19 : oracle 的命名规则（from 9i release doc)：
 Names must be from 1 to 30 bytes long
 Names of databases are limited to 8 bytes
 Names cannot contain quotation marks.
 Names are not case sensitive.
 A name must begin with an alphabetic character from your database character set
 unless surrounded by double quotation marks.
 Names can contain only alphanumeric characters from your database character set
 and the underscore (_), dollar sign ($), and pound sign (#).
 Oracle strongly discourages you from using $ and #.
 Names of database links can also contain periods (.) and "at" signs (@).
 If your database character set contains multibyte characters, Oracle recommends
 that each name for a user or a role contain at least one single-byte character.
 A name cannot be an Oracle reserved word.
 Depending on the Oracle product you plan to use to access a database object,
 names might be further restricted by other product-specific reserved words.
 Do not use the word DUAL as a name for an object or part. DUAL is the name of a dummy table.
 The Oracle SQL language contains other words that have special meanings.
 These words include datatypes, function names, and keywords (the uppercase
 words in SQL statements, such as DIMENSION, SEGMENT, ALLOCATE, DISABLE, and so
 forth). These words are not reserved. However, Oracle uses them internally.
 Therefore, if you use these words as names for objects and object parts,
 your SQL statements may be more difficult to read and may lead to unpredictable
 results.
 In particular, do not use words beginning with "SYS_" as schema object names,
 and do not use the names of SQL built-in functions for the names of schema
 objects or user-defined functions.
 Within a namespace, no two objects can have the same name.
 The following schema objects share one namespace:
 Tables
 Views
 Sequences
 Private synonyms
 Stand-alone procedures
 Stand-alone stored functions
 Packages
 Materialized views
 User-defined types
 Each of the following schema objects has its own namespace:
 Indexes
 Constraints
 Clusters
 Database triggers
 Private database links
 Dimensions
 Because tables and views are in the same namespace, a table and a view in the
 same schema cannot have the same name. However, tables and indexes are in
 different namespaces. Therefore, a table and an index in the same schema can
 have the same name.
 Each schema in the database has its own namespaces for the objects it contains.
 This means, for example, that two tables in different schemas are in different
 namespaces and can have the same name.
 Each of the following nonschema objects also has its own namespace:
 User roles
 Public synonyms
 Public database links
 Tablespaces
 Rollback segments
 Profiles
 Parameter files (PFILEs) and server parameter files (SPFILEs)
 Because the objects in these namespaces are not contained in schemas, these
 namespaces span the entire database.
 Columns in the same table or view cannot have the same name. However,
 columns in different tables or views can have the same name.
------------------------------------------------------
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.jdbc.pool.PooledSQLException;
import com.esen.jdbc.pool.impl.oracle.OraclePooledPreparedStatement;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;
public class OracleDef
    extends DbDef {
  /**
   * 定义数值最大精度；
   */
  public static final int MaxPrecision = 38;
  
  public OracleDef(Dialect dl) {
    super(dl);
  }
  
  /**
   * 20091102
   * 提供修改字段时，调整字段长度；
   * Oracle 主键和唯一、一般索引，列组合长度都是6389
   */
  protected int adjustFieldLengthByIndex(TableMetaData tbmd, String colname, int len, int maxKeysLen,
      TableColumnMetaData col) {
    TableIndexMetaData[] indx = tbmd.getIndexes();
    if(indx!=null&&indx.length>0){
      int newlen = len;
      for(int i=0;i<indx.length;i++){
        TableIndexMetaData indxi = indx[i];
        if(/*indxi.isUnique()&&*/isIndexColumn(indxi,colname)){
          String[] indxcols = indxi.getColumns();
          int keylen = 0;
          for(int j=0;j<indxcols.length;j++){
            TableColumnMetaData colj = getColumn(tbmd,indxcols[j]);
            keylen += colj.getLen();
          }
          int maxlen = maxKeysLen - (keylen-col.getLen());
          if(newlen>maxlen){
            newlen =  maxlen;
          }
        }
      }
      return newlen;
    }
    return len;
  }
  
  /**
   * 20090902
   * Oracle单个字段，字符类型，最大长度是4000
   * 索引长度也有限制：
   * ORA-01450: 超出最大的关键字长度 (6398)
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
      adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},dbinf.getMaxVarcharFieldLength());
    }
    checkKeysAndIndexesMaxLength(tmdh,dbinf.getMaxKeyOfFieldsLength());
  }
  
  protected String formatUpcaseFieldName(String fdname) {
    return fdname.toUpperCase();
  }

  public String funcLeftSql(String fldname, int cnt) {
    return " SUBSTR(" + fldname + ",1," + cnt + ") ";
  }
  public void modifyColumnForDesc(Connection conn, String tablename,String fieldname,String desc) throws SQLException{
    String sql = getDescToFieldSql(tablename,fieldname,desc);
    Statement stat = conn.createStatement();
    try{
      stat.executeUpdate(sql);
    }finally{
      if(stat!=null)
        stat.close();
    }
  }
  protected void addDescToField(Connection conn,String tbname) throws SQLException {
    Statement stat = conn.createStatement();
    try {
      for (int i = 0; i < tbMetaHelper.getColumnCount(); i++) {
        TableColumnMetaData fi =  tbMetaHelper.getColumn(i);
        if(fi.getDesc()!=null&&fi.getDesc().length()>0){
          String csql = getDescToFieldSql(tbname,fi);
          stat.executeUpdate(csql);
        }
      }
    }
    finally {
      stat.close();
    }
  }
  private String getDescToFieldSql(String tbname,TableColumnMetaData fi) {
    return getDescToFieldSql(tbname,fi.getName(),fi.getDesc());
  }
  private String getDescToFieldSql(String tbname,String fieldname,String desc) {
    //COMMENT ON COLUMN table_name.field_name IS 'desc'
    //Oracle也不要双引号，否则找不到表
    StringBuffer sql = new StringBuffer(64);
    sql.append("COMMENT ON COLUMN ");
    sql.append(tbname);
    sql.append(".");
    sql.append(getColumnName(fieldname));
    sql.append(" IS '");
    if(desc!=null)
      sql.append(desc);
    sql.append('\'');
    return sql.toString();
  }
  /**
   * 定义Memo字段
   * 由于long类型一张表只能定义一个long字段，改用varchar2(4000);
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " VARCHAR2(4000) " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
  }
  /**
   * 获得创建临时表sql
   * 在Oracle8i或以上版本中，可以创建以下两种临时表：
1。会话特有的临时表
    CREATE GLOBAL TEMPORARY <TABLE_NAME> (<column specification>)
    ON COMMIT PRESERVE ROWS；
  
2。事务特有的临时表
    CREATE GLOBAL TEMPORARY <TABLE_NAME> (<column specification>)
    ON COMMIT DELETE ROWS；
   CREATE GLOBAL TEMPORARY TABLE MyTempTable
所建的临时表虽然是存在的，但是你试一下insert 一条记录然后用别的连接登上去select，记录是空的，明白了吧，我把下面两句话再贴一下：
--ON COMMIT DELETE ROWS 说明临时表是事务指定，每次提交后ORACLE将截断表（删除全部行）
--ON COMMIT PRESERVE ROWS 说明临时表是会话指定，当中断会话时ORACLE将截断表。
冲突的问题更本不用考虑.
  
临时表只是保存当前会话(session)用到的数据，数据只在事务或会话期间存在。
  
通过CREATE GLOBAL TEMPORARY TABLE命令创建一个临时表，对于事务类型的临时表，
数据只是在事务期间存在，对于会话类型的临时表，数据在会话期间存在。
  
会话的数据对于当前会话私有。每个会话只能看到并修改自己的数据。DML锁不会加到
临时表的数据上。下面的语句控制行的存在性。
  
● ON COMMIT DELETE ROWS 表名行只是在事务期间可见
● ON COMMIT PRESERVE ROWS 表名行在整个会话期间可见
  
可以对临时表创建索引，视图，出发器，可以用export和import工具导入导出表的
定义，但是不能导出数据。表的定义对所有的会话可见。
   * @param tablename String
   * @param fldDdl String
   * @return String
   */
  public String getTempTableCreateDdl(String tablename, String fldDdl) {
    return "CREATE GLOBAL TEMPORARY TABLE " + tablename + "(" + fldDdl +
        ") ON COMMIT DELETE ROWS ";
  }
  protected String getCreateTableByQueryStr(String tablename,boolean istemp) {
    if(istemp)
      return "CREATE GLOBAL TEMPORARY TABLE "+ tablename+" ON COMMIT DELETE ROWS ";
    return "CREATE TABLE "+ tablename;
  }
  //oracle D,T,P是一样的
  protected boolean equalsFieldType(char srct,char dest){
    if(srct==dest) return true;
    boolean f1 = srct=='D'||srct=='T'||srct=='P';
    boolean f2 = dest=='D'||dest=='T'||dest=='P';
    if(f1&&f2) return true;
    return false;
  }

  protected boolean objectExists(DatabaseMetaData _dmd, String username, String objectname, String[] types) throws SQLException {
    return super.objectExists(_dmd, username.toUpperCase(), objectname.toUpperCase(), types);
  }
  
  public void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException{
    StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" MODIFY ");
    ddl.append(getFieldDefine(coltype,fieldname,len,scale));
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(ddl.toString());
    }
    finally {
      stmt.close();
    }
  }
  
  private String getFieldDefine(char coltype, String fieldname, int len, int scale) throws SQLException {
    switch (coltype) {
      case DbDefiner.FIELD_TYPE_STR:
        return fieldname + " VARCHAR2("+len+")";
      case DbDefiner.FIELD_TYPE_INT:{
        return formatModifyNumberDefiner(fieldname, len, 0);
      }
      case DbDefiner.FIELD_TYPE_FLOAT:{
        return formatModifyNumberDefiner(fieldname, len, scale);
      }
      case DbDefiner.FIELD_TYPE_DATE:
        return fieldname + " DATE ";
      case DbDefiner.FIELD_TYPE_TIME:
        return fieldname + " TIMESTAMP ";
      case DbDefiner.FIELD_TYPE_TIMESTAMP:
        return fieldname + " TIMESTAMP ";
      case DbDefiner.FIELD_TYPE_LOGIC:
        return fieldname + " VARCHAR2(1)";
      case DbDefiner.FIELD_TYPE_memo:
      case DbDefiner.FIELD_TYPE_CLOB:
        return fieldname + " CLOB DEFAULT EMPTY_CLOB()";
      case DbDefiner.FIELD_TYPE_BINARY:
        return fieldname + " BLOB DEFAULT EMPTY_BLOB()";
      default:
        throw new SQLException(
            "database not support to define this type of field,type:" +coltype);
    }
  }
  
  private String formatModifyNumberDefiner(String fieldname, int len, int scale) {
    int prcs[] = formatNumberPrecision(len,scale,MaxPrecision);
    if(prcs[0]==0){
      return fieldname + " NUMBER";
    }
    return fieldname + " NUMBER("+prcs[0]+","+prcs[1]+")";
  }
  
  /**
   * Oracle9 支持修改列名rename操作
   * @param conn Connection
   * @param tablename String
   * @param new_col String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param defaultvalue String
   * @param unique boolean
   * @param col String
   * @throws Exception
   */
  public void modifyColumn(Connection conn,String tablename, String col, String new_col,char coltype,
                           int len, int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
    if(col==null||col.length()==0){
//      throw new SQLException("修改列名不能为空！");
    	throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
    }
    String col2 = getColumnName(dl, col);
    String new_col2 = getColumnName(dl, new_col);
    List ddls = new ArrayList();
    
    StringBuffer renddl = null;
    if(coltype==FIELD_TYPE_FLOAT){
      if(len>38) len=38;
      if(dec<0) dec = 4;
    }
    if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
      //修改字段名；
      renddl = new StringBuffer(32).append("ALTER TABLE ");
      renddl.append(tablename);
      renddl.append(" RENAME COLUMN ");
      renddl.append(col2).append(" TO ").append(new_col2);
      ddls.add(renddl.toString());
      col2 = new_col2;
    }
    Dialect dl = SqlFunc.createDialect(conn);
    TableColumnMetaData tcol = dl.getTableColumnMetaData(tablename, col);
    //修改属性：类型，长度
    /**
     * 20090210 如果类型和长度相同，则不需要修改这两项；
     *          做这个判断是因为如果是bolb字段，修改时报错；
     * 20090716 去掉长度的判断，是因为可能需要修改数值的精度，比如(10,2)改为(10,4)
     * 20090721 对于字符类型和数值类型，可以修改字段长度和精度；
     *          20090716直接去掉长度的判断，导致不能修改原类型的长度；
     */
    if (isNotEqual(SqlFunc.getSubsectionType(tcol.getType()) ,coltype,tcol.getLen(),tcol.getScale(),len,dec )) {
      /**
       * 20091102
       * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
       */
      TableMetaData tbmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
      len = this.adjustFieldLengthForModify(tbmd, col2, len);
      StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
      ddl.append(tablename);
      ddl.append(" MODIFY ");
      ddl.append(getFldDdl(new TableColumnMetaDataHelper(col2, coltype, len, dec, true, false, null,null), true));
      ddls.add(ddl.toString());
    }
    
    //判断是否需要修改unique属性
    if(tcol.isUnique()!=unique){
      if(unique){
        //增加unique属性
        StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
        sql.append(tablename).append(" ADD UNIQUE(").append(col2).append(")");
        ddls.add(sql.toString());
      }else{
        //删除unique属性
        /**
         * 20090220
         * unique有可能是主键，这里主键不能被删除；
         */
        TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
        String[] keys = tmd.getPrimaryKey();
        if(keys!=null&&keys.length==1&&keys[0].equalsIgnoreCase(col2)){
          //判断是主键，不进行修改；
        }else{
          StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
          sql.append(tablename).append(" DROP UNIQUE(").append(col2).append(")");
          ddls.add(sql.toString());
        }
      }
    }
    /**
     * 判断是否需要修改default值
     * Oracle中，''和null 是一致的，所以在都非空且值不一致，或者一个空且一个非空的情况下才需要修改默认值；
     * 这里非空是指既不是''也不是null；
     */
    boolean isNullDefaltValue = StrFunc.isNull(defaultvalue);
    boolean isNullSrcDefaultValue = StrFunc.isNull(tcol.getDefaultValue());
    if((!isNullDefaltValue&&!isNullSrcDefaultValue
        &&!StrFunc.compareStr(defaultvalue, tcol.getDefaultValue()))
        ||(isNullDefaltValue&&!isNullSrcDefaultValue)
        ||(!isNullDefaltValue&&isNullSrcDefaultValue)){
      if(defaultvalue==null){
        //删除原有default值
        StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
        sql.append(tablename).append(" MODIFY ").append(col2).append(" DEFAULT NULL");
        ddls.add(sql.toString());
      }else {
        if(defaultvalue.length()==0)
          defaultvalue = "''";
        //修改defualt值
        StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
        sql.append(tablename).append(" MODIFY ").append(col2).append(" DEFAULT ").append(defaultvalue);
        ddls.add(sql.toString());
      }
    }
    //判断是否需要修改是否允许空值
    if(tcol.isNullable()!=nullable){
      if(nullable){
        //设置允许为空
        StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
        sql.append(tablename).append(" MODIFY ").append(col2).append(" NULL");
        ddls.add(sql.toString());
      }else{
        //设置不允许空值
        StringBuffer sql =  new StringBuffer(32).append("ALTER TABLE ");
        sql.append(tablename).append(" MODIFY ").append(col2).append(" NOT NULL");
        ddls.add(sql.toString());
      }
    }
    Statement stmt = conn.createStatement();
    try {
      for(int i=0;i<ddls.size();i++){
        stmt.execute((String)ddls.get(i));
      }
    }
    finally {
      stmt.close();
    }
  }

  /**
   * 如果原类型srcType和目标类型coltype不一致，返回true；
   * 1)由于Oracle没有Time类型，定义的'T'类型使用timestamp('P')类型代替；
   *   所以如果数据库表的类型srcType='P' ，目标类型coltype='T' 其实是一致的；
   * 2)由于Oracle没有int类型，所以定义的'I'类型，实现是number(30,0),读取表结构该字段类型是'N';
   *   所以原数据库类型srcType='N'和目标类型coltype='I'是一致的；
   * 3)由于Orcale的date可以保存时间，所以从数据库读取出来date类型是'P' ,参见OracleTableMetaDataImpl.setType方法225行；
   *   因此Oracle数据库'D'和'P'是一致的；
   * @param srcType
   * @param coltype
   * @return
   */
  private boolean isNotEqual(char srcType, char coltype,int len,int scale,int len2,int scale2) {
    if(srcType=='P' && coltype=='T') {
      return false;
    }
    
    /**
     * BI-4596 20110426
     * Oracle字段没有整形字段，当目标字段是整形，且长度和原字段相同，小数位数都是0,则认为字段类型是相同的，不需要修改；
     */
    if (((srcType=='N' && coltype=='I') || (srcType=='I' && coltype=='N')) 
    		&& len == len2 
    		&& scale == scale2
    		&& scale2 == 0) {
      return false;
    }
    
    if (srcType == 'P' && coltype == 'D'
      || srcType == 'D' && coltype == 'P'){
      return false;
    }
    
    //逻辑型
    if (srcType == DbDefiner.FIELD_TYPE_STR 
    		&& len == 1
    		&& coltype == DbDefiner.FIELD_TYPE_LOGIC) {
    	return false;
    }
 
    if (coltype == 'C' && len != len2){
    	return true;
    }
    
    if (coltype == 'N' && (len != len2 || scale != scale2)){
    	return true;
    }
    
    return srcType != coltype;
  }

  /**
   * To rename a table, view, sequence, or private synonym for a table, view, or sequence.
   * Oracle automatically transfers integrity constraints, indexes, and grants on
   * the old object to the new object.
   * Oracle invalidates all objects that depend on the renamed object, such as views,
   * synonyms, and stored procedures and functions that refer to a renamed table.
   * @param conn Connection
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  protected void renameObject(Connection conn, String oldname,
                              String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
    	/**
    	 * Oracle更改表名不支持带schema前缀；
    	 * 否则会出现：ORA-01765: 不允许指定表的所有者名 异常；
    	 */
    	String[] oldtbs = getTableNameForDefaultSchema(oldname,dbinf);
    	String[] newtbs = getTableNameForDefaultSchema(newname,dbinf);
    	if(!StrFunc.isNull(oldtbs[0])
    			&&!oldtbs[0].equalsIgnoreCase(dbinf.getDefaultSchema())){
//    		throw new SQLException("Oracle更改表名不支持更改别的用户的表；");
    		throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.oracle.oracledef.unsupportou", "Oracle更改表名不支持更改别的用户的表；"));
    	}
      ddl.executeUpdate("RENAME " + oldtbs[1] + " TO " + newtbs[1]);
    }
    finally {
      ddl.close();
    }
  }
  public void dropTempTable(Connection conn, String catalog, String table) throws
  SQLException {
    dropTable(conn,catalog,table);
  }
  /**
   * 更改index名
   * 语法：alter index index_name rename to new_name
   * @param conn Connection
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  protected void renameIndex(Connection conn, String oldname,
                             String newname) throws Exception {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("ALTER INDEX " + oldname + " RENAME TO " + newname);
    }
    finally {
      ddl.close();
    }
  }
  /**
   * 更改表名
   * @param conn Connection
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  public void renameTable(Connection conn, String oldname, String newname) throws
  SQLException {
    renameObject(conn,oldname,newname);
  }
  /**
   * 定义Blob字段
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    //BLOB DEFAULT EMPTY_BLOB()
    return getColumnName(fi.getName()) + " BLOB DEFAULT EMPTY_BLOB() " + getTailDdl(null, fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
  }

  /**
   * 判断索引名是否存在
   * select index_name from user_indexes where index_name=''
   * 或者 select uo.object_name from user_objects uo where uo.object_type = 'INDEX' and uo.object_name='name';
   * 这里name要大写；同“TRIGGER”
   * @param conn Connection
   * @param indexname String
   * @throws Exception
   * @return boolean
   */
  public boolean indexExists(Connection conn, String tbname, String indexname) throws SQLException {
    String dbuser = conn.getMetaData().getUserName();
    String sql = "select index_name from user_indexes " + "where index_name='" + indexname.toUpperCase()
        + "' and table_owner='" + dbuser.toUpperCase() + "'";
    //System.out.println(sql);
    Statement stmt = conn.createStatement();
    try {
      ResultSet rs = stmt.executeQuery(sql);
      try {
        return ((rs.next() ? true : false));
      }
      finally {
        /**
         * 20090922
         * 原来的程序没有关闭ResultSet；
         * 可能造成一直持有一个游标；
         */
        if (rs != null)
          rs.close();
      }
    }
    finally {
      if (stmt != null)
        stmt.close();
    }

  }
  /**
   * 定义自增长字段
   * Orcale通过Sequence实现
   * @param thisField String
   * @param step int
   * @return String
   */
  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
	  if (len <= 0) {
		  len = LEN_INT4;
	  } else if (len > MaxPrecision) {
		  len = MaxPrecision;
	  }
   return thisField + " NUMBER(" + len + ") NOT NULL ";
  }
  /**
   * 定义Clob字段
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    //CLOB DEFAULT EMPTY_CLOB()
    return getColumnName(fi.getName()) + " CLOB DEFAULT EMPTY_CLOB() " + getTailDdl(null, fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_CLOB, isUpdate);
  }

  private void execDdl(Connection conn, String ddl) throws SQLException {
    Statement stmt = conn.createStatement();
    try {
      stmt.executeUpdate(ddl);
    }
    finally {
      stmt.close();
    }
  }
  /**
   * 删除Sequence
   * @param conn Connection
   * @param table String
   * @throws Exception
   */
  protected void dropSequence(Connection conn, String seqname) throws Exception {
    if (sequenceExists(conn, seqname)) {
      Statement stat = conn.createStatement();
      try{
        stat.executeUpdate("drop sequence " + seqname);
      }finally{
        stat.close();
      }
    }
  }
  /**
   * Oralce用Sequence,Trigger实现自增长字段
   * 所有表共用一个Sequence
   * @param conn Connection
   * @param tablename String
   * @param fldname String
   * @throws Exception
   */
  protected void additionOfAutoInc(Connection conn, String tablename,
                                   String fldname) throws SQLException {
    //create a sequence
    String ddl = null;
    String seqname = null;
    try {
      //所有表共用一个sequence
      seqname = "SEQ_Esensoft_IREPORT5";
      if (!sequenceExists(conn, seqname)) {
        ddl = "create sequence " + seqname + " \r\n" +
            "start with 1 " + "\r\n" +
            "increment by 1 " + "\r\n" +
            "nomaxvalue";
        execDdl(conn, ddl);
      }
    }
    catch (SQLException ex) {
//      throw new PooledSQLException("创建sequence:" + seqname + "\r\n\r\n" +
//                                      ddl + "\r\n\r\n出现异常:", ex);
    	Object[] param=new Object[]{seqname,ddl};
    	 throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.oracle.oracledef.sequenceerr", "创建sequence:{0}\r\n\r\n{1}\r\n\r\n出现异常:", param), ex);
    }
    //create a trigger
    String trigname = null;
    ddl = null;
    try {
      trigname = getTriggerName(conn, "T" + tablename);
      ddl = " create or replace trigger " + trigname + " " +
          "before insert on " + tablename +
          " for each row begin select " + seqname +
          ".nextval into :new." + fldname +
          " from dual;end;"; /*com.esen.db.TaskDb.FIELD_INDEX*/

      execDdl(conn, ddl);
    }
    catch (SQLException ex) {
//      throw new PooledSQLException("创建trigger:" + trigname + "\r\n\r\n" +
//                                      ddl + "\r\n\r\n出现异常:" ,ex);
    	Object[] param=new Object[]{trigname,ddl};
    	 throw new PooledSQLException(I18N.getString("com.esen.jdbc.dialect.impl.oracle.oracledef.triggererr", "创建trigger:{0}\r\n\r\n{1}\r\n\r\n出现异常:\"", param) ,ex);

    }
  }

  /**
   * 同触发器
   * @param conn
   * @param nm
   * @return
   * @throws SQLException
   */
  private boolean sequenceExists(Connection conn, String nm) throws SQLException {
    String sql = "select sequence_name from user_sequences " +
        "where sequence_name='" + nm.toUpperCase() + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
      return ( (rs.next() ? true : false));
    }
    finally {
      rs.close();
      stmt.close();
    }
  }

/*  private String getSequenceName(Connection conn, String nm) throws Exception {
    if (nm == null) {
      return null;
    }
    String basenm = nm;
    int i = 1;
    while (sequenceExists(conn, nm)) {
      nm = basenm + i;
      i++;
    }
    return nm;
  }*/

  /**
   * 20110527
   * Oracle检查触发器名字是否存在，没有考虑大小写，导致创建重复的触发器出现异常；
   */
  private boolean triggerExists(Connection conn, String nm) throws SQLException {
    String sql = "select trigger_name from user_triggers " +
        "where trigger_name='" + nm.toUpperCase() + "'";
    Statement stmt = conn.createStatement();
    ResultSet rs = null;
    try {
      rs = stmt.executeQuery(sql);
      return ( (rs.next() ? true : false));
    }
    finally {
      if(rs!=null)
        rs.close();
      if(stmt!=null)
        stmt.close();
    }
  }

  private String getTriggerName(Connection conn, String nm) throws SQLException {
    if (nm == null) {
      return null;
    }
    nm = formatName(nm);
    /**
     * 20120305
     * Oracle下建立索引名加前缀'T', 可能导致名称超过最大允许长度, 截掉最后一个字符
     * 这种改法仍有可能出错, 不过发生的几率相当较小, 先这么处理
     */
    int max_length = dbinf.getMaxTableNameLength();
    if( nm.length() > max_length) {
      nm = nm.substring(0, max_length - 1);
    }
    int i = 1;
    while (triggerExists(conn, nm)) {
      nm = nm + i;
      nm = formatName(nm);
      i++;
    }
    return nm;
  }

  protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    /**
     * 20090216
     * Oracle数值精度是38，这里做限制；
     */
    int len = fi.getLen();
    if(len<=0||len>38)
      len = 38;
    return getColumnName(fi.getName()) + " NUMBER("+len+") " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_INT, isUpdate);
  }
  
  /**
   * 用于存放生成数值型字段定义的容器，由于可能一张表有很多数值字段，所以这里使用一个StringBuffer对象；
   */
  private StringBuffer numddl;
  /**
   * 有些数据库表的数值字段，创建表时没有给精度，以前的程序读取该表结构，认为小数位数是0，当作整数处理，
   * 因此copy数据，在目的数据库建表，把该字段当作整数，写入数据时，小数位数都丢失了；
   * 现在支持建表时给数值字段的整数位数和小数位数都给0，表示不知道数值的精度，
   * 对Oracle可以直接用NUMBER不给精度，对于其他数据库都有不同的处理方法；
   * Oracle数值精度是 38 
   * 对于Oracle可以不指定数值精度，也就是NUMBER不带参数
   */
  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	if(numddl==null){
      numddl = new StringBuffer(64);//初始长度由16改为64，如果表的数值型字段很多，还是有影响的；
	}else{
		numddl.setLength(0);
	}
    numddl.append(getColumnName(fi.getName()));
    numddl.append(" NUMBER");
    //如果长度为0，则不指定长度；
    if(fi.getLen()>0){
      /**
       * 20090721 
       * 变态的定义：number(126,38) ,可能来自mysql的备份时，读取的表结构；
       * 第一个参数是总长度，包括小数位数，第二个参数是小数位数；
       * 解决：
       * 如果总长度小于小数位数，则总长度=总长度+小数位数；
       * 如果总长度大于38，取38；
       * 如果小数位数大于等于38，取8；小于0则取0；
       */
      int[] prcs = formatNumberPrecision(fi.getLen(),fi.getScale(),MaxPrecision);
      numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();
  }
  /**
   * Oracle9,10没有time数据类型，它用timestamp表示；
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIMESTAMP " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIME, isUpdate);
  }
  /**
   * Oracle9,10增加了timestamp类型；
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TIMESTAMP " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
  }
  /**
   * 字符串类型，Oracle推荐使用varchar2
   * @param fldname String
   * @param len int
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " VARCHAR2(" + (fi.getLen() > 0 ? fi.getLen() : 1) + ") " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_STR, isUpdate);
  }

  /**
   * 经测试，Oracle对于超过1000的varchar2类型，在写入[1001,2000]长度的字符串时，可能出现：
   * ORA-01461：仅能绑定要插入 LONG 列的 LONG 值；在{@link OraclePooledPreparedStatement#setString(int, String)}
   * 中我们对字符串添加了空格，在这里需要增大字段的长度。
   * @param str
   * @return
   */
	public void defineField(String thisField, char FieldType, int len, int dec,
			String defaultvalue, boolean nullable, boolean unique) {
		if (FieldType == 'C' && len > 1000 && len < 2001) {
			len = 2001;
		}
		super.defineField(thisField, FieldType, len, dec, defaultvalue,
				nullable, unique, null);
	}

	protected String getFieldCommentSql(String viewname, String colname, String comment) {
		if (StrFunc.isNull(viewname) || StrFunc.isNull(colname) || StrFunc.isNull(comment))
			return "";
		
		return getDescToFieldSql(viewname, colname, comment);
	}
}
