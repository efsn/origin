package com.esen.jdbc.dialect.impl.mysql;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

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
 * mysql jdbc 3.1.x版本驱动写Clob有问题; 3.0.x支持好些
 * 2003.12.29     在mysql-connector-java-3.0.9-stable-bin.jar中，必须使用
 *                setBinaryStream才能写入大字段,使用setBlob存在
 *                indexOutOfBounds必然错误！
 *                在更高的版本中，mysql的setBlob内部都是通过setBinaryStream实现的,
 *                但是setBinaryStream受服务器环境变量的影响，最大不能超过
 *                max_allowed_packet；

 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public class MysqlDef extends DbDef {
  public static final int MaxPrecision = 65;
  public MysqlDef(Dialect dl) {
    super(dl);
  }
  private boolean haveIncfField;
  
  /**
   * 20110620
   * Mysql字符类型的值，默认不区分大小写，而Oracle,Db2表内数据是区分大小写的；
   * 为了和Oracle数据库统一，这里将mysql字符类型的值都创建成区分大小写的；
   */
  protected String getStrFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    return getColumnName(fi.getName()) + " VARCHAR(" + (fi.getLen() > 0 ? fi.getLen() : 1) + ") BINARY " +
	        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_STR, isUpdate);
	  }
  
  /**
   * 20090902
   * MySQL的每个单表中所创建的索引长度是有限制的;
   * 单个字段如果定义了唯一属性，长度也有这个限制；
   * MySQL的varchar主键只支持不超过768个字节 或者 768/2=384个双字节 或者 768/3=256个三字节的字段；
   * 而GBK是双字节的，UTF-8是三字节的；
   * 例：
   * create table test(test varchar(256) primary key)charset=UTF8;
　   * -- 错误 
   * -- ERROR 1071 (42000): Specified key was too long; max key length is 767 bytes
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    /**
     * Mysql中字段定义长度是字符长度，MaxKeyLength是字节长度；
     * 字符集的不同，索引最大字符长度也不同；
     */
    int maxKeyLen = dbinf.getMaxKeyOfFieldsLength();
    
    //检查主键和索引的定义
    checkKeysAndIndexesMaxLength(tmdh,maxKeyLen);
  }
  
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh, int maxKeyLength) {
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
      for(int i=0;i<_pkeys.length;i++){
        keys[i] = (TableColumnMetaDataHelper)tmdh.getColumn(_pkeys[i]);
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
        /**
         * Mysql只有唯一索引和主键有列组合长度限制；
         * 20100128
         * 实际测试一般索引也有限制；
         */
        //if(indx.isUnique()){
          adjustFieldLength(keys,maxKeyLength);
        //}
      }
    }
  }
	/*
	 * IRPT-8814: MySQL 主键长度限制
	 * 最大为 3072 字节时，创建表时需要定义  row_format= compressed
	 */
	public String getTableCreateDdl(String tablename, String fldDdl) {
		if(((MysqlDataBaseInfo)dbinf).useCompressedTable()) {
			return "CREATE TABLE " + tablename + "(" + fldDdl + ") row_format = compressed";
		}
		return super.getTableCreateDdl(tablename, fldDdl);
	}

  /**
   * mysql表名通配符精确匹配
   * @param str
   * @return
   */
  private String getTransferSql(String str) {
    str = str.replaceAll("\\\\", "\\\\\\\\\\\\\\\\");
    str = str.replaceAll("%", "\\\\\\\\%");
    str = str.replaceAll("_", "\\\\\\\\_");
    str = str.replace('*', '%');
    str = str.replace('?', '_');
    return str;
  }

  public void modifyColumnForDesc(Connection conn, String tablename,String fieldname,String desc) throws SQLException{
    Dialect ddl = SqlFunc.createDialect(conn);//不使用全局变量dl，是为了避免嵌套链接
    TableColumnMetaData[] cols = ddl.createDbMetaData().getTableMetaData(tablename).getColumns();
    TableColumnMetaData col = getColumnMetaData(cols,fieldname);
    if(col==null){
//      throw new SQLException("表"+tablename+"没有字段："+fieldname);
    	Object[] param=new Object[]{tablename,fieldname};
    	  throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.mysql.mysqldef.nosuchfield", "表{0}没有字段：{1}", param));
    }
    if(!StrFunc.compareStr(desc,  col.getDesc())){
      TableColumnMetaDataHelper fi = new TableColumnMetaDataHelper(fieldname,SqlFunc.getType(col.getType()),col.getLen(),col.getScale(),true,false,null,desc);
      String sql = "alter table "+tablename+" modify ";
      if (col.isAutoInc()) {
    	  sql += getIdFieldDdl(col.getName(), 1, desc);
      } else {
    	  sql += getFldDdl(fi,true);
      }
      Statement stat = conn.createStatement();
      try{
        stat.executeUpdate(sql);
      }finally{
        if(stat!=null)
          stat.close();
      }
    }
  }
  private TableColumnMetaData getColumnMetaData(TableColumnMetaData[] cols,
      String fieldname) {
    for(int i=0;i<cols.length;i++){
      TableColumnMetaData coli = cols[i];
      if(fieldname.equalsIgnoreCase(coli.getName())){
        return coli;
      }
    }
    return null;
  }
  /**
   * 由于临时表用databaseMetaData的getTables方法访问不到，无从判断是否已经存在
   * 所以用Mysql专有的if not exists避免再次创建抛出异常；
   * @param tablename
   * @param fldDdl
   * @return
   */
  public String getTempTableCreateDdl(String tablename,String fldDdl){
    return "CREATE TEMPORARY TABLE IF NOT EXISTS " +tablename + "("+fldDdl+ ") ";
  }
  protected String getTailDdl(String defaultvalue, boolean nullable,
      boolean unique, String desc,char t, boolean isUpdate) {
    StringBuffer str = new StringBuffer(32);
    if (defaultvalue != null && defaultvalue.length()>0) {
      str.append(" DEFAULT ").append(defaultvalue);
    }
    if(unique) str.append(" UNIQUE ");
    if(!nullable) str.append(" NOT NULL ");
    else str.append(" NULL ");
    if(desc!=null&&desc.length()>0){
      //mysql 给字段加描述信息
      str.append(" COMMENT ").append('\'').append(desc).append('\'');
    }
    return str.toString();
  }
  /**
   * TEXT 64k
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate){
    return getColumnName(fi.getName()) + " TEXT " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
  }
  /**
   * MEDIUMTEXT 16M
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected  String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate){
    return getColumnName(fi.getName()) + " MEDIUMTEXT " + getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_CLOB, isUpdate);
  }
  /**
   * LONGBLOB 2G; MEDIUMBLOB 16M;
   * @param fldname String
   * @param defaultvalue String
   * @param nullable boolean
   * @param unique boolean
   * @return String
   */
  protected  String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate){
    return getColumnName(fi.getName()) + " LONGBLOB " +getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
  }
  public void clearDefineInfo() {
    haveIncfField = false;
    super.clearDefineInfo();
  }
  
  /**
   * 20090714
   * 如果有自动增长字段，此方法必须先于定义主键的方法definePrimaryKey调用；
   */
  public void defineAutoIncField(String thisField, int gap) {
    super.defineAutoIncField(thisField, gap);
    /**
     * 20090714
     * Mysql中如果定义了自动增长字段，则该字段必须是主键，或者是主键的一部分；
     * 否则会无法创建表；
     */
    definePrimaryKey(thisField);
    this.haveIncfField = true;
  }
  /**
   * 设置自动增长字段
   * mysql自动增长字段必须是主键；
   * 如果设置了自动增长字段，则自动设置成主键，后面调用definePrimaryKey无效；
   * @param thisField String
   * @param step int
   * @return String
   */
  protected  String getIdFieldDdl(String thisField, int step, String desc){
	  return getIdFieldDdl(thisField, 10, step, desc);

  }
  
  /**
   * 设置自动增长字段
   * mysql自动增长字段必须是主键；
   * 如果设置了自动增长字段，则自动设置成主键，后面调用definePrimaryKey无效；
   * @param thisField String
   * @param step int
   */
  protected  String getIdFieldDdl(String thisField, int step){
	  return getIdFieldDdl(thisField, step, null);
  }
  
  /**
   * 指定长度时，根据长度确定自增长字段的数据类型
   */
  protected String getIdFieldDdl(String thisField, int len, int step, String desc) {
	  StringBuffer idddl = new StringBuffer();
	    idddl.append(thisField);
	    String type = getIntType(len);
	    
	    if (StrFunc.isNull(type)) {
	    	if(len >= 19) {
	    		type = "BIGINT";
	    	} else {
	    		type = "INTEGER";
	    	}
	    }
	    idddl.append(' ').append(type);
	    idddl.append(" AUTO_INCREMENT ");
	    if (!StrFunc.isNull(desc)) {
	    	idddl.append(" COMMENT ").append('\'').append(desc).append('\'');
	    }
	    return idddl.toString();
  }
  
  public void definePrimaryKey(String fieldNames) {
    if(this.haveIncfField) {
      /**
       * 20090714 BI-840
       * 建表时如果有自动增长字段，却设置了其他字段是主键；
       * 由于Mysql中自动增长字段必须是主键，所以将这个其他字段设置成唯一索引；
       */
      defineIndex("ind_auto_"+fieldNames, "("+fieldNames+")", true);
      return;
    }
    super.definePrimaryKey(fieldNames);
  }
  /**
   * mysql5 数值精度最大值： 65
   */
  protected String getNumericFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    StringBuffer numddl = new StringBuffer();
    numddl.append(getColumnName(fi.getName()));
    //如果长度为0，则不指定长度；
    if(fi.getLen()>0){
      numddl.append(" DECIMAL");
      int[] prcs = formatNumberPrecision(fi.getLen(),fi.getScale(),MaxPrecision);
      numddl.append('(').append(prcs[0]).append(',').append(prcs[1]).append(')');
    }else{
      //无法指导精度的使用double类型；
      numddl.append(" DOUBLE");
    }
    numddl.append(' ');
    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
    return numddl.toString();

  }
  /**
   * mysql timestamp, The range is '1970-01-01 00:00:00' to partway through the year 2037.范围太窄
   * 改用 datetime类型，A date and time combination. The supported range is '1000-01-01 00:00:00' to '9999-12-31 23:59:59'
   */
  protected String getTimeStampFldDdl(TableColumnMetaData fi, boolean isUpdate) {
    return getColumnName(fi.getName()) + " DATETIME " +
      getTimeStampTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc());
  }
  private String getTimeStampTailDdl(String defaultvalue, boolean nullable,
      boolean unique,String desc) {
    StringBuffer str = new StringBuffer(32);
    if (defaultvalue != null&&defaultvalue.length()>0) {
      str.append(" DEFAULT ").append(defaultvalue);
    }
    if(nullable){
      str.append(" NULL ");
    }else{
      //timestamp不为空，则default值给系统时间，要不然mysql5自动加上系统时间而且每次update都自动更新；
      if(defaultvalue==null){
        str.append(" DEFAULT 0");
      }
      str.append(" NOT NULL ");
    }
    if(unique)
      str.append(" UNIQUE ");
    if(desc!=null&&desc.length()>0){
      //mysql 给字段加描述信息
      str.append(" COMMENT ").append('\'').append(desc).append('\'');
    }
    return str.toString();
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
        return fieldname + " VARCHAR("+len+") BINARY ";
      case DbDefiner.FIELD_TYPE_INT:
        return fieldname + " INT";
      case DbDefiner.FIELD_TYPE_FLOAT:
        int[] prcs = formatNumberPrecision(len,scale,MaxPrecision);
        if(prcs[0]==0){
          return fieldname + " DOUBLE";
        }
        return fieldname + " DECIMAL("+prcs[0]+","+prcs[1]+")";
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
        return fieldname + " MEDIUMTEXT";
      case DbDefiner.FIELD_TYPE_BINARY:
        return fieldname + " LONGBLOB";
      default:
        throw new SQLException(
            "database not support to define this type of field,type:" +coltype);
    }
  }
  
  protected int[] formatNumberPrecision(int len,int scale,int maxLen){
    if(len<=0){
      len = 0;
      scale = 0;
    }
    if(len<scale){
      len = len+scale;
    }
    if(len>maxLen){
      len = maxLen;
    }
    //Mysql的小数位数最大值是30，这里如果超过了取12位，为了统一；
    if(scale>=30){
      scale = 12;
    }
    if(scale<0){
      scale = 0;
    }
    return new int[]{len,scale};
  }
  
  
  public void modifyColumn(Connection conn, String tablename, String col,
      String new_col, char coltype, int len, int dec, String defaultvalue,
      boolean unique, boolean nullable) throws SQLException {
    if (col == null || col.length() == 0) {
//      throw new SQLException("修改列名不能为空！");
    	throw new SQLException(I18N.getString("JDBC.COMMON.COLUMNNAMECANNOTBEEMPTY", "修改列名不能为空！"));
    }
    String col2 = getColumnName(dl, col);
    String new_col2 = getColumnName(dl, new_col);
    List ddls = new ArrayList();
    StringBuffer ddl = new StringBuffer("ALTER TABLE ");
    ddl.append(tablename);
    
    /*
     * 判断是否需要更改unique属性；
     * 由于Mysql是通过index实现unique的，因此将唯一改为非唯一，需要删除对应索引；
     */
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tbmd = dl.createDbMetaData().getTableMetaData(tablename);
    TableColumnMetaData tcol = tbmd.getColumn(col);
    if (new_col != null && new_col.length() > 0 && !col.equals(new_col)) {
      ddl.append(" CHANGE ");
      ddl.append(col2).append(" ");
      ddl.append(getFldDdl(new TableColumnMetaDataHelper(new_col2, coltype, len, dec
          , nullable, unique,null,null),true));
      col2 = new_col2;
    }
    else {
      /**
       * 20091102
       * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
       */
      len = adjustFieldLengthForModify(tbmd,col2,len);
      ddl.append(" MODIFY ");
      ddl.append(getFldDdl(new TableColumnMetaDataHelper(col2, coltype, len, dec,
          nullable, unique, null, null),true));
    }
    ddls.add(ddl.toString());
    
    /**
     * BI-6039 不需要执行的字段修改语句。
     * 读默认值判断是否需要修改，在执行相应的sql语句。
     */
		if (!StrFunc.compareStr(defaultvalue, tcol.getDefaultValue())) {
			if (defaultvalue == null || defaultvalue.length() == 0) {
				//删除原有default值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2).append(" DROP DEFAULT");
				ddls.add(sql.toString());
			}
			else {
				//修改defualt值
				StringBuffer sql = new StringBuffer(32).append("ALTER TABLE ");
				sql.append(tablename).append(" ALTER ").append(col2).append(" SET DEFAULT ").append(defaultvalue);
				ddls.add(sql.toString());
			}
		}

    if(tcol.isUnique()!=unique){
      if(!unique){
        //删除索引
        String[] indexname = getUniqueIndexName(tbmd,col,true);
        if (indexname != null) {
          for (int i = 0; i < indexname.length; i++) {
            String sql = "drop index " + indexname[i] + " on " + tablename;
            ddls.add(sql);
          }
        }
      }
    }
    Statement stmt = conn.createStatement();
    try {
      for(int i=0;i<ddls.size();i++)
        stmt.execute((String)ddls.get(i));
    }
    finally {
      stmt.close();
    }
  }


  /**
   * 更改表名
   * @param conn Connection
   * @param catalog String
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  public void renameTable(Connection conn, String oldname,
                          String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("RENAME TABLE " + oldname+" TO "+ newname);
    }
    finally {
      ddl.close();
    }
  }
  /**
   * 获得索引名
   * Mysql 允许不同表有相同的索引名
   * @param conn Connection
   * @param nm String
   * @throws Exception
   * @return String
   */
  protected String getIndexName(Connection conn,String tbname, String nm) throws Exception {
    return nm;
  }
  /**
   * 判断索引名是否存在
   * @param conn Connection
   * @param nm String
   * @throws Exception
   * @return boolean
   */
  public boolean indexExists(Connection conn,String tbname, String nm) throws SQLException {
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tbname);
    if(tmd==null) return false;//表名不存在
    TableIndexMetaData[] inds = tmd.getIndexes();
    if(inds!=null){
    for(int i=0;i<inds.length;i++){
    	if(inds[i].getName().equalsIgnoreCase(nm))
    		return true;
    }
    }
    return false;
  }
  public boolean viewExists(Connection conn,String viewname) throws SQLException{
    Dialect dl = SqlFunc.createDialect(conn);
    int mv = dl.getDataBaseInfo().getDatabaseMajorVersion();
    if(mv<5)
//      throw new SQLException("Mysql"+mv+"不支持视图；");
    	throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.mysql.mysqldef.unsupportview", "Mysql{0}不支持视图；", new Object[]{String.valueOf(mv)}));
    /**
     * 20100302
     * Mysql在linux环境默认是表名大小写敏感的，这里判断视图是否存在，不转成小写再判断，否则不能做出正确的判断；
     */
    return super.viewExists(conn,viewname.trim());
  }
  
	/**
	 * {@inheritDoc}
	 */
	protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
		if (!isLenValid(fi.getLen())) {
			return getNumericFldDdl(fi, isUpdate);
		}
		
		String type = getIntType(fi.getLen());
		return getColumnName(fi.getName())
				+ " " + type + " "
				+ getTailDdl(fi.getDefaultValue(), fi.isNullable(),
						fi.isUnique(), fi.getDesc(), DbDefiner.FIELD_TYPE_INT,
						isUpdate);
	}
	
	private boolean isLenValid(int len) {
		if (len <= 0 || len >= LEN_INT8) {
			return false;
		}		
		return true;
	}
	
	/*
	 * ESENBI-3032: modify by liujin 2014.12.15
	 * 完善 MySQL 中整型的处理方式，根据定义的长度明确整型的具体类型，不能直接都用 INT
	 */
	/**
	 * 根据整型的长度确定具体的整型
	 * 对于定义为整型的字段，需要根据具体长度确定数据类型，不能都当做 INT 处理。
	 * 
	 * @param len
	 * @return 返回具体的数据类型，不能定义为整型时，返回值为 null
	 */
	private String getIntType(int len) {
		if (!isLenValid(len)) {
			return null;
		}
		
		if (len <= LEN_INT1) {
			return "TINYINT";
		}
		if (len <= LEN_INT2) {
			return "SMALLINT";
		}
		if (len <= LEN_INT3) {
			return "MEDIUMINT";
		}
		if (len <= LEN_INT4) {
			return "INTEGER";
		}
		if (len <= LEN_INT8) {
			return "BIGINT";
		}
		return null;
	}
}
