package com.esen.jdbc.dialect.impl.sybase;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataHelper;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;
/**
 * sybase的字段名区分大小写
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public class SybaseDef extends DbDef {
  public static final int MaxPrecision = 38;
  public SybaseDef(Dialect dl) {
    super(dl);
  }
  /**
   * Sybase不支持对数值型字段设置数字串值
   * @return boolean
   */
  public boolean canSetNumAsStr() {
    return false;
  }
  
  /**
   * 20091102
   * 提供修改字段时，调整字段长度；
   * Sybase 主键和唯一、一般索引，列组合长度都是2600
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
   * 20090903
   * SybaseAse 索引最大长度是 2600
   * 出现异常:2600 is the maximum allowable size of an index
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    int maxKeyLength = dbinf.getMaxKeyOfFieldsLength();
    //对单个字段，如果定义了唯一属性，是通过索引实现的，也要限制长度；
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
      if(fieldi.isUnique()){
        adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},maxKeyLength);
      }
    }
    //检查主键
    String[] _pkeys = tmdh.getPrimaryKey();
    if(_pkeys!=null&&_pkeys.length>0){
      TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[_pkeys.length];
      for (int i = 0; i < _pkeys.length; i++) {
        keys[i] = (TableColumnMetaDataHelper) tmdh.getColumn(_pkeys[i]);
        TableColumnMetaDataHelper keyfield = keys[i];
        /**
         * 20090903
         * SybaseAse的主键字段不允许为空，否则报异常：
         * Column 'STR_' is part of a primary key constraint and cannot be null.
         */

        if (keyfield.isNullable()) {
          keyfield.setNullable(false);
        }
      }
      adjustFieldLength(keys,maxKeyLength);
    }
    //检查索引；
    TableIndexMetaData[] _index = tmdh.getIndexes();
    if(_index!=null&&_index.length>0){
      for (int i = 0; i < _index.length; i++) {
        TableIndexMetaData indx =  _index[i];
        String[] fields = indx.getColumns();
        TableColumnMetaDataHelper[] keys = new TableColumnMetaDataHelper[fields.length];
        for(int j=0;j<fields.length;j++){
          keys[j] = (TableColumnMetaDataHelper)tmdh.getColumn(fields[j]);
        }
        adjustFieldLength(keys,maxKeyLength);
      }
    }
  }
  /**
   * SYBASE   默认的锁方式为页锁，如相应系统要求为行锁，用sa登录，执行：  
  sp_configure   "lock   scheme",0,datarows  
  则所有数据库中以后新建的表(table)的锁方式为行锁。  
          如果设置一个已经存在的表(tabel)的锁方式为行锁，则执行：  
  alter   table   table_name   lock   datarows  
          注意：行锁可以减少“死锁”，但会降低服务器(sql   server)的性能，只有经常发生“死锁”  
  现象时才有必要把锁方式改为行锁。   
  
  当建立一个新表时，可以不使用这个缺省值，可采用如下的句法格式
  create table <tablename>() lock  datarows
   */
  public String getTableCreateDdl(String tablename, String fldDdl) {
    if(dbinf.getMaxColumnsInTable()>0&&dbinf.getMaxColumnsInTable()<1024){
      //数据库支持的表最大字段不足1024，且创建的表字段超过了数据库支持的最大字段数；
      if(this.tbMetaHelper.getColumnCount()>dbinf.getMaxColumnsInTable())
        return "CREATE TABLE " + tablename + "(" + fldDdl + ") lock  datarows";
    }
    return super.getTableCreateDdl(tablename, fldDdl);
  }
  private int DEVICE_MINSIZE = 80;//暂定80M
  /**
   * 在SYBASE数据库系统中创建数据库时需要指定一足够大的数据库尺寸；
   * 若太小，系统会停止响应
   * 此时，需要DBA转储数据或日志；或者增加额外物理设备
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  public void createCatalog(Connection conn, String catalog)throws SQLException {
    Statement ddl = conn.createStatement();
    try{
      ddl.executeUpdate("use master ");
      ddl.executeUpdate("create database " + catalog + "on default="+ DEVICE_MINSIZE);
    }catch (SQLException e){
      throw new SQLException("can't create database "+catalog+"! Sybase default device capacity is too low! (at least >"+DEVICE_MINSIZE+"M)");
    }finally{
      ddl.close();
    }
  }

  public void dropTempTable(Connection conn, String catalog, String table) throws SQLException {
    dropTable(conn, catalog, table);
  }
  /**
   * 增加一个unique且非空的字段时，
   * ALTER TABLE test_2 ADD field2 varchar(20)  unique not null; //报错
   * 可以先增加一个unique字段，再修改其为非空；
   * ALTER TABLE test_2 ADD field2 varchar(20)  unique;
   * ALTER TABLE test_2 modify field2 not null
   * 
   */
  public void addColumn(Connection conn, String tablename, String newcol, char coltype, int len, int dec,
      String defaultvalue, boolean nullable, boolean unique) throws SQLException {
    newcol = getColumnName(newcol);
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" ADD ");
    ddl.append(getFldDdl(new TableColumnMetaDataHelper(newcol, coltype, len, dec, nullable, unique,defaultvalue,null), true));
    StringBuffer ddl2 = new StringBuffer(32);
    if(unique&&!nullable){
      ddl2.append("ALTER TABLE ").append(tablename).append(" MODIFY ");
      ddl2.append(newcol).append(" NOT NULL");
    }
    Statement stmt = conn.createStatement();
    try {
      stmt.executeUpdate(ddl.toString());
      if(ddl2.length()>0)
        stmt.executeUpdate(ddl2.toString());
    }
    finally {
      stmt.close();
    }
  }
  /**
   * Sybase,SybaseIQ 字段默认是否可以为空，需要 NULL 关键字
   */
  protected String getTailDdl(String defaultvalue, boolean nullable,
      boolean unique,String desc,char t, boolean isUpdate) {
    StringBuffer str = new StringBuffer(32);
    if (defaultvalue != null && defaultvalue.length()>0) {
      str.append(" DEFAULT ").append(defaultvalue);
    }
    if(isUpdate){
      /**
       * 增加字段时：
       * create table test_2 (field1 varchar(20) unique not null)
       * ALTER TABLE test_2 ADD field2 varchar(20)  unique not null; //报错
       * ALTER TABLE test_2 ADD field2 varchar(20)  unique;
       * ALTER TABLE test_2 ADD field3 varchar(20) default 'b' unique ;
       * ALTER TABLE test_2 ADD field4 varchar(20) default 'b' null ;
       * ALTER TABLE test_2 ADD field5 varchar(20) default 'b' not null ;
       * ALTER TABLE test_2 ADD field6 varchar(20)  null ;
       * ALTER TABLE test_2 ADD field7 varchar(20)  not null ; //报错
       * ALTER TABLE test_2 ADD field8 varchar(20) default 'b' unique not null ;//报错
       * ALTER TABLE test_2 ADD field9 varchar(20) default 'b' unique null ;
       * 结论：unique时，不能非空;
       *          可以先增加一个unique字段，再修改其为非空；
       *          ALTER TABLE test_2 modify field2 not null
       *      非unique且非空，必须有default值；
       */
      if (unique) {
        str.append(" UNIQUE ");
      }else{
        if (!nullable) {
          if (defaultvalue == null || defaultvalue.length()==0) {
            str.append(getDefualtSql(defaultvalue, t));
          }
          str.append(" NOT NULL ");
        }else{
          //为空，这里不需要设置；
        	//SYBASE ASE 上 image字段默认是非空的 ，当允许为空时 显性指定 add by jzp 2012-09-18
        	str.append(" NULL ");
        }
      }
    }
    else {
      if (unique) {
        str.append(" UNIQUE ");
      }
      if (!nullable) {
        str.append(" NOT NULL ");
      }
      else
        str.append(" NULL ");
    }
    return str.toString();
  }
  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TEXT " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
  }
  
  public void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException{
    Dialect dl = SqlFunc.createDialect(conn);
    TableColumnMetaData tcol = dl.getTableColumnMetaData(conn, tablename, fieldname);
    /**
     * 20090827 BIDEV-830
     * 由于SybaseAse，如果更改的属性和原属性相同，会出异常；
     * 所以这里先把原属性读取出来，做判断在更改；
     * 判断条件：
     * 1)字段类型不同
     * 2)浮点类型和字符类型，长度和原属性不同；
     * 3)浮点类型，小数位数和原属性不同；
     * 3个条件任意一个为真就做修改；
     * 这里注意：int类型不需要修改长度；
     */
    if (SqlFunc.getType(tcol.getType()) != coltype || ((coltype == 'N' || coltype == 'C') && tcol.getLen() != len)
        || (coltype == 'N' && tcol.getScale() != scale)) {
      StringBuffer ddl = new StringBuffer(32).append("ALTER TABLE ");
      ddl.append(tablename);
      ddl.append(" MODIFY ");
      ddl.append(getFieldDefine(coltype, fieldname, len, scale));

      /**
       * 20091010
       * SybaseAse执行导入数据时，如果设置conn.setAutoCommit(false)，
       * 导入数据过程中，如果有其他程序需要从连接池获取连接，且此时连接池已经没有空闲连接，在创建新的连接时，发生堵塞；
       * 必须等这个导入数据的事务执行完毕才能获取新连接，但是此时如果导入数据的事务，执行了字段扩容的操作，这个操作需要获取连接池的某些信息，
       * 比如getDbType()，此方法是和获取新连接同步的，由于获取新连接发生堵塞，这里会wait，造成死锁；
       * 
       * SybaseAse执行一个事务的同时，不能创建新的连接，原因是：
       * 设置conn.setAutoCommit(false)，执行修改字段属性的sql后，如果不提交事务，则获取新的数据连接时发生堵塞，直到提交事务；
       * 
       * 解决办法：在修改字段属性时，如果conn.getAutoCommit()=false,则执行修改sql后，立即执行conn.commit();
       */
      boolean f = conn.getAutoCommit();
      Statement stmt = conn.createStatement();
      try {
        stmt.execute(ddl.toString());
        if(!f){//如果在一个事务中，执行修改字段，则修改后，立即提交事务，否则Sybase无法获取新连接；
          conn.commit();
        }
      }
      finally {
        stmt.close();
      }
    }
  }
  
  private String getFieldDefine(char coltype, String fieldname, int len, int scale) throws SQLException {
    switch (coltype) {
      case DbDefiner.FIELD_TYPE_STR:
        return fieldname + " VARCHAR("+len+")";
      case DbDefiner.FIELD_TYPE_INT:
        return fieldname + " INT";
      case DbDefiner.FIELD_TYPE_FLOAT:
        int[] prcs = formatNumberPrecision(len,scale,MaxPrecision);
        if(prcs[0]==0){
          return fieldname + " FLOAT";
        }
        return fieldname + " NUMERIC("+prcs[0]+","+prcs[1]+")";
      case DbDefiner.FIELD_TYPE_DATE:
        return fieldname + " DATE ";
      case DbDefiner.FIELD_TYPE_TIME:
        return fieldname + " TIME ";
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
   * 
sp_rename

Changes the name of a user-created object or user-defined datatype in the current database.
Syntax

sp_rename objname, newname [,“index” | “column”]

Parameters

objname
    is the original name of the user-created object (table, view, column, stored procedure, index, trigger, 
    default, rule, check constraint, referential constraint, or user-defined datatype). 
    If the object to be renamed is a column in a table, objname must be in the 
    form “table.column”. If the object is an index, objname must be in the 
    form “table.indexname”.
newname
    is the new name of the object or datatype. The name must conform to the rules 
    for identifiers and must be unique to the current database.
index
    specifies that the object you are renaming is an index, not a column. This argument 
    allows you to rename an index that has the same name as a column, without dropping 
    and re-creating the index.
column
    specifies that the object you are renaming is a column, not an index. 
    This argument is part of the same option as the index argument.
    
    If a column and an index have the same name, use the [,“index” | “column”] argument,
     which specifies whether to rename the index or the column. In the following sample, 
     assume that both an index and a column named idx exist:

sp_rename "t.idx", new_idx, "column"

sp_rename "t.idx", new_idx, "index"
   *
   * 修改默认值：
   * ALTER TABLE test_1 replace zb2 default  23
   */
  public void modifyColumn(Connection conn, String tablename, String col, String new_col, char coltype, int len,
      int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
    if (col == null || col.length() == 0) {
//      throw new SQLException("修改列名不能为空！");
    	throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
    }
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tbmd = dl.createDbMetaData().getTableMetaData(tablename);
    TableColumnMetaData[] clmds = tbmd.getColumns();
    TableColumnMetaData tcol = null;
    for(int i=0;i<clmds.length;i++){
      TableColumnMetaData clmd = clmds[i];
      if(clmd.getName().equals(col)) {
        tcol = clmd;
        break;
      }
    }
    if(tcol==null){
//      throw new SQLException("表"+tablename+"不存在字段："+col);
    	Object[] param=new Object[]{tablename,col};
    	 throw new SQLException(I18N.getString("JDBC.COMMON.NOFIELD", "表{0}不存在字段：{1}", param));
    }
    List ddls = new ArrayList();
    if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
      if (SqlFunc.isKeyWord(new_col))
//        throw new SQLException(new_col + "为关键字，Sybase不支持修改字段为关键字；");
    	  throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.sybase.sybasedef.notkey", "{0}为关键字，Sybase不支持修改字段为关键字；", new Object[]{new_col}));
      StringBuffer renddl = new StringBuffer("sp_rename ");
      //jdbc中要使用单引号
      /**
       * 只能更改默认schema下的表，如果表名带有schema前缀，修改将不成功；
       */
      String[] tbs = getTableNameForDefaultSchema(tablename, dbinf);
      renddl.append("'").append(tbs[1]).append(".").append(col).append("','");
      renddl.append(new_col).append("',").append("'column'");
      ddls.add(renddl.toString());
      col = new_col;//改名
    }
    String col2 = getColumnName(col);
    
    /**
     * 20091102
     * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
     */
    len = this.adjustFieldLengthForModify(tbmd, col2, len);
    //比较类型，长度
    if(SqlFunc.getType(tcol.getType())!=coltype
        || ((coltype == 'N' || coltype == 'I' || coltype == 'C') && tcol.getLen() != len)
        || (coltype == 'N' && tcol.getScale() != dec)
        ){
      StringBuffer ddl = new StringBuffer("ALTER TABLE ");
      ddl.append(tablename);
      ddl.append(" MODIFY ");
      //处理类型，长度
      ddl.append(getFldDdl(new TableColumnMetaDataHelper(col2, coltype, len, dec,  true, false,null,null), true));
			
	//字段的原始属性为非空时，该语句中带有  NULL 会改变原定义，需要去掉
	if (tcol.isNullable() == false) {
		int ind = ddl.toString().indexOf(" NULL");
		if (ind > 0) {
			ddl.delete(ind, ind + " NULL".length());
		}
	}
      ddls.add(ddl.toString());
    }
    //比较是否非空
    if(tcol.isNullable()!=nullable){
      StringBuffer ddl = new StringBuffer("ALTER TABLE ");
      ddl.append(tablename);
      ddl.append(" MODIFY ").append(col2);
      if(nullable){
        ddl.append(" NULL");
      }else{
        ddl.append(" NOT NULL");
      }
      ddls.add(ddl.toString());
    }
    //比较unique属性
    if(tcol.isUnique()!=unique){
      if(unique){
        //增加unique: alter table test_2 add unique(field4)
        String sql = "alter table "+tablename+" add unique("+col2+")";
        ddls.add(sql);
      }else{
        //删除unique: alter table test_2 drop constraint index_name
        String[] indexname = getUniqueIndexName(tbmd,col,true);
        if(indexname!=null){
        for(int i=0;i<indexname.length;i++){
          String sql = "alter table "+tablename+" drop constraint "+indexname[i];
          ddls.add(sql);
        }
        }
      }
    }
    //比较default值
    if (!StrFunc.compareStr(tcol.getDefaultValue(), defaultvalue)) {
      //重设default值：ALTER TABLE test_1 replace zb2 default defaultvalue
      if(StrFunc.isNull(defaultvalue))
        defaultvalue = "NULL";
      String sql = "alter table " + tablename + " replace " + col2 + " default " + defaultvalue;
      ddls.add(sql);
    }
    
    /**
     * Sybase修改字段结构不支持事务；
     * 进行修改前，如果已经进入了一个事务，先调用commit将事务关闭，在设置AutoCommit为true；
     * 这样可以避免在事务内设置AutoCommit参数是报：
     * SET CHAINED command not allowed within multi-statement transaction.
     * 的异常；
     * 这里的改动同 dropColumn方法；
     * 上面的办法在12.5.2 ebf11948 版本没有问题；
     * 但是在12.5.1 ebf11522 版本有问题：
     * 事务开始时 调用 conn.setAutCommit(false);
     * 由于修改字段结构不支持事务，需要将其conn.setAutCommit(true);
     * setAutCommit()的实现掉用getAutoCommit()获取当前状态进行判断设置（不一致就设置）；
     * 这时此版本的Sybase报SET CHAINED command not allowed within multi-statement transaction.异常；
     * 代码如下：
     * Connection con = getConnection();//直接获取连接，不从连接池获取
      try{
        con.setAutoCommit(false);
        Statement stat = con.createStatement();
        try{
          con.commit();
          boolean f = con.getAutoCommit();
          if(!f)
            con.setAutoCommit(true); //上面调用了con.getAutoCommit();这里执行报错：SET CHAINED command not allowed within multi-statement transaction
          stat.executeUpdate("alter table t_test22 modify date_  datetime null");
          con.setAutoCommit(false);
          stat.executeUpdate("insert into t_test22 (id_,name_)values('222','bb')");
          con.commit();
          con.setAutoCommit(true);
        }finally{
          stat.close();
        }
      }finally{
        con.close();
      }
     */
    boolean f = conn.getAutoCommit();
    if (!f) {
      conn.commit();
      conn.setAutoCommit(true);
    }
    Statement stmt = conn.createStatement();
    try {
      for(int i=0;i<ddls.size();i++) {
        stmt.execute((String)ddls.get(i));
      }
      if (!f) {
        //还原事务状态；
        conn.setAutoCommit(f);
      }
	} finally {
      stmt.close();
    }
  }

  	public String repairTable(Connection conn, Document xml)
			throws SQLException {
		boolean isAutoCommit = conn.getAutoCommit();
		
		//sybase 不能在事务中执行修复表定义的语句
		if (!isAutoCommit) {
			conn.setAutoCommit(true);
		}
		
		String ret = super.repairTable(conn, xml);
		
		if (!isAutoCommit) {
			conn.setAutoCommit(false);
		}
		
		return ret;
	}

  public void renameTable(Connection conn, String oldname, String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("sp_rename " + oldname+","+ newname);
    }
    finally {
      ddl.close();
    }
  }
  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    //IMAGE
    return getColumnName(fi.getName()) + " IMAGE " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
  }
  
  public boolean indexExists(Connection conn, String tbname, String indexname) throws SQLException {
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
	  if (len <= 0) {
		  len = LEN_INT4;
	  } else {
		  len = MaxPrecision;
	  }
    return (thisField +" NUMERIC(" + len + ",0) IDENTITY ");
  }

  protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		int len = fi.getLen();
		//len <= 0 是作为 Integer
		if (len >= 10) {
			return getNumericFldDdl(fi, isUpdate);
		}
		
		/*
		 * BUG:ESENFACE-1057: modify by liujin 2014.06.13
		 * 对于定义为整型的字段，需要根据具体长度确定数据类型，不能都当做 INT 处理。
		 */
		String type = "INTEGER";
		if (len > 0 && len < 3) {
			type = "TINYINT";
		} else if (len < 5) {
			type = "SMALLINT";
		} else if (len < 10) {
			type = "INTEGER";
//		} else {
//			type = "BIGINT"; //没有 Bigint 类型
		}

		return getColumnName(fi.getName())
				+ " " + type + " "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_INT,
						isUpdate);
	}
  /**
   * 对于包含小数点的数字，可使用精确数值类型 numeric 和 decimal;
精确数值类型可接受两种可选参数， precision 和 scale;
precision 指定了能够在该列中存储的最大小数位数。它包括小数点
左右两侧的所有位数。可将精度指定为 1 至 38 位范围内的一个值，
或者使用缺省的 18 位精度。
scale 指定了能够存储到小数点右侧的最大位数。标度必须小于或等
于 precision。可将标度指定为 0 至 38 位范围内的一个值，或者使用
缺省的 0 位标度。
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
      //无法确定精度，使用float类型：
      numddl.append(" FLOAT");
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();

  }

  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " TEXT " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_CLOB, isUpdate);
  }
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATETIME " +
    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
  }
  public void dropColumn(Connection conn, String tablename, String col)
      throws SQLException {
    /**
     * 如果字段有唯一约束，删除出异常；
     * 有一般的索引，包括和其他字段的联合索引，都删不掉；
     * 先删除约束和索引，在删字段；
     * 现在问题是，无法知道 是约束，还是索引，删除约束和删除索引的语法不一样，TODO
     */
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tbmd = dl.createDbMetaData().getTableMetaData(tablename);
    String[] indexname = getUniqueIndexName(tbmd,col,true);
    String[] deleteUnique = null;
    if(indexname!=null){
      deleteUnique = new String[indexname.length];
      for(int i=0;i<indexname.length;i++)
        deleteUnique[i] = "alter table "+tablename+" drop constraint "+indexname[i];
        //deleteUnique[i] = "drop index "+tablename+"."+indexname[i];
    }
    StringBuffer ddl = new StringBuffer("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" DROP ").append(col);
    boolean f = conn.getAutoCommit();
    /**
     * Sybase删除字段不支持事务；
     */
    if(!f){
      conn.commit();
      conn.setAutoCommit(!f);
    }
    Statement stmt = conn.createStatement();
    try {
      if(deleteUnique!=null){
        for(int i=0;i<deleteUnique.length;i++)
          stmt.execute(deleteUnique[i]);
      }
      stmt.execute(ddl.toString());
      
    }
    finally {
      stmt.close();
    }
    if(!f){
      conn.setAutoCommit(f);
    }
  }

}
