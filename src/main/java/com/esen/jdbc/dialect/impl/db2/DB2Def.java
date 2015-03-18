package com.esen.jdbc.dialect.impl.db2;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
 * 创建DB2数据库时，
 * 字符集用 UTF-8 也可以支持中文，但是某些中文字符支持不是很好；
 * 例如：插入“各个”字符串，DB2认为 sql 没有结束；
 * 用 GBK 不会出现中文字符问题，但是不能用 Type4 驱动，只能用 Type2 驱动；
 * 用 Type2 驱动需要安装DB2客户端，并做相应配置；
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public class DB2Def
    extends DbDef {
  
  public static final int MaxPrecision = 31;
  
  public DB2Def(Dialect dl) {
    super(dl);
  }
  /**
   * DB2不支持对数值型字段设置数字串值
   * @return boolean
   */
  public boolean canSetNumAsStr() {
    return false;
  }
  /**
   * 20091102
   * 提供修改字段时，调整字段长度；
   * DB2 主键和唯一、一般索引，列组合长度都是1000
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
   * DB2定义字段，索引，主键最大长度是1000
   */
  protected void checkKeysAndIndexesMaxLength(TableMetaDataHelper tmdh) {
    int maxKeyLength = dbinf.getMaxKeyOfFieldsLength();
    //检查字段
    for(int i=0;i<tmdh.getColumnCount();i++){
      TableColumnMetaDataHelper fieldi = (TableColumnMetaDataHelper)tmdh.getColumn(i);
       /**
       * DB2 varchar类型最大长度是32672
       */
      adjustFieldLength(new TableColumnMetaDataHelper[]{fieldi},dbinf.getMaxVarcharFieldLength());
      /**
       * 20091022
       * 唯一属性的字段，就是唯一索引，列组合最大值是1000
       */
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
         * db2的主键字段不允许为空，否则报异常：
         * DB2 SQL Error: SQLCODE=-542, SQLSTATE=42831, SQLERRMC=STR_, DRIVER=3.50.152
         * -542   42831   可以为空的列不允许作为主健的一部分包含在内
         */
        
        if(keyfield.isNullable()){
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
   * 创建Catalog
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  public void createCatalog(Connection conn, String catalog) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      ddl.executeUpdate("create db " + catalog +
                        " using codeset GBK territory CN");
    }
    finally {
      ddl.close();
    }
  }

  protected String formatUpcaseFieldName(String fdname) {
    return fdname.toUpperCase();
  }

  protected String getMemoFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    return getColumnName(fi.getName()) + " LONG VARCHAR " +
	    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_memo, isUpdate);
	  }

  /**
   * DB2 更改表名，如果表有相关视图、触发器、约束条件，则会无法删除；
   * 必须先删掉相关对象，在更名；
   * 
   * 这里对相关视图做了处理，先删除，更名，然后重新创建；
   */
  public void renameTable(Connection conn, String oldname, String newname) throws
      SQLException {
    if(this.viewExists(conn, oldname)){
      /**
       * 如果是视图，则调用更改视图名方法；
       */
      renameView(conn,oldname,newname);
      return;
    }
    Statement ddl = conn.createStatement();
    try {
      //读取相关视图
      String[] vns = getViewFromTable(ddl,oldname);
      List ll = new ArrayList();
      if(vns!=null){
        for(int i=0;i<vns.length;i++){
          //读取相关视图的创建语句，并保存到中间变量；
          String vtext = getViewText(ddl,vns[i]);
          ll.add(vtext);
          dropView(conn, vns[i]);
        }
      }
      //更名
      /**
  	 * Db2更改表名不支持带schema前缀；
  	 */
  	String[] oldtbs = getTableNameForDefaultSchema(oldname,dbinf);
  	String[] newtbs = getTableNameForDefaultSchema(newname,dbinf);
  	if(!StrFunc.isNull(oldtbs[0])
  			&&!oldtbs[0].equalsIgnoreCase(dbinf.getDefaultSchema())){
  		//throw new SQLException("DB2更改表名不支持更改别的用户的表；");
  		throw new SQLException(I18N.getString("com.esen.jdbc.dialect.impl.db2.db2def.chtname", "DB2更改表名不支持更改别的用户的表；"));
  	}
      ddl.executeUpdate("rename table " + oldtbs[1] + " to " + newtbs[1]);
      //重建视图
      for(int i=0;i<ll.size();i++){
        String vtexti = (String)ll.get(i);
        //将视图创建语句中的涉及的原表名，改为新表名；
        String newvi = Pattern.compile("\\s"+oldtbs[1]+"\\s?",Pattern.CASE_INSENSITIVE).matcher(vtexti).replaceAll(" "+newtbs[1]+" ");
        ddl.execute(newvi);
      }
      
    }
    finally {
      ddl.close();
    }
  }
  
  /**
   * DB2不支持直接更改视图名；
   * 采用删除，重建的方式；
   */
  private void renameView(Connection conn, String oldname, String newname) throws SQLException {
    Statement ddl = conn.createStatement();
    try {
      String vtext = getViewText(ddl,oldname);
      dropView(conn, oldname);
      //更名重建；
      String newvi = Pattern.compile("\\s"+oldname+"\\s?",Pattern.CASE_INSENSITIVE).matcher(vtext).replaceAll(" "+newname+" ");
      ddl.execute(newvi);
    }
    finally {
      ddl.close();
    }
  }
  /**
   * 找出表名相关的视图
   * @param stat
   * @param tbname
   * @return
   * @throws SQLException
   */
  protected String[] getViewFromTable(Statement stat,String tbname) throws SQLException{
	  String[] tbs = getTableNameForDefaultSchema(tbname,dbinf);
    String sql = "select viewname from SYSCAT.VIEWDEP where viewschema='"+tbs[0]
    +"' and bname='"+tbs[1].toUpperCase()+"'";
    ResultSet rs = stat.executeQuery(sql);
    List ll = new ArrayList();
    while(rs.next()){
      ll.add(rs.getString(1));
    }
    if(ll.size()>0){
      String[] tbs2 = new String[ll.size()];
      ll.toArray(tbs2);
      return tbs2;
    }
    return null;
  }
  
  /**
   * 返回指定视图名的创建视图语句；
   * @param stat
   * @param vname
   * @return
   * @throws SQLException
   */
  protected String getViewText(Statement stat,String vname) throws SQLException{
	  String[] tbs = getTableNameForDefaultSchema(vname,dbinf);
    String sql = "select text from SYSCAT.VIEWS where viewschema='"+tbs[0]
    +"' and viewname='"+tbs[1].toUpperCase()+"'";
    ResultSet rs = stat.executeQuery(sql);
    while(rs.next()){
      return rs.getString(1);
    }
    return null;
  }
  
  public boolean tableOrViewExists(Connection conn,String tvname)throws SQLException{
	  String[] tbs = getTableNameForDefaultSchema(tvname,dbinf);
	    String sql = "select tabname from SYSCAT.TABLES where TABSCHEMA='"+tbs[0]+"' " +
        "and tabname='"+tbs[1].toUpperCase()+"' ";
    return excuteSql(conn,sql);
  }
  
  /**
   * 20091020 
   * 原来的实现方法，不能把DB2中的物化视图查出来；
   * 由于创建物化视图使用create table 语法，删除物化视图使用 drop table 语法；
   * 这里判断物化视图是否存在，使用判断表是否存在的方法； 
   * 
   * select tabname from SYSCAT.TABLES where TABSCHEMA='DB2ADMIN' and tabname='MQT_EMP' and (type='T' or type='S')
   * 20110714 判断表是否存在，是在默认的schema 下，这里使用TABSCHEMA条件进行判断，更加准确；
   */
  public boolean tableExists(Connection conn, String catalog, String tablename) throws SQLException {
	  String[] tbs = getTableNameForDefaultSchema(tablename,dbinf);
	  String sql = "select tabname from SYSCAT.TABLES where TABSCHEMA='"+tbs[0]+"' " +
    		"and tabname='"+tbs[1].toUpperCase()+"' and (type='T' or type='S')";
    return excuteSql(conn,sql);
  }
  
  public boolean viewExists(Connection conn,String viewname) throws SQLException{
	  String[] tbs = getTableNameForDefaultSchema(viewname,dbinf);
    String sql = "select tabname from SYSCAT.TABLES where TABSCHEMA='"+tbs[0]+"' " +
        "and tabname='"+tbs[1].toUpperCase()+"' and type='V'";
    return excuteSql(conn,sql);
  }
  
  protected String getBlobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
  		/*IRPT-7783 解决DB2数据库上附件大小超过创建BLOB字段时指定的最大长度限制时，报错：DB2 SQL Error: SQLCODE=-302, SQLSTATE=22001, SQLERRMC=null, DRIVER=3.52.95的问题
  		 * 这个地方暂时改为50M， 以后需要传一个参数控制这个值 by RX @ 2012-04-23
  		 */
	    return getColumnName(fi.getName()) + " BLOB(50M) " +
	    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_BINARY, isUpdate);
	  }
  public boolean indexExists(Connection conn,String tablename, String indexname) throws SQLException {
	  String[] tbs = getTableNameForDefaultSchema(tablename,dbinf);
	  String sql = "select INDNAME from SYSCAT.INDEXES " +
        "where INDNAME='" + indexname.toUpperCase() + "' and TABNAME='"+tbs[1].toUpperCase()+"' and TABSCHEMA='"+tbs[0]+"'";
        //+" and tbname='"+tablename.toUpperCase()+"'";
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
    return thisField + " INTEGER  NOT NULL  GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1, NO CACHE)";
  }

  protected String getClobFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    return getColumnName(fi.getName()) + " CLOB(1M) " +
	    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_CLOB, isUpdate);
	  }
  //db2 T,P是一样的
  protected boolean equalsFieldType(char srct,char dest){
    return srct==dest||(srct=='T'&&dest=='P')||(srct=='P'&&dest=='T');
  }
  
  /**
   * 快速轻便的修改字段长度的方法；
   * DB2 只支持字符类型的长度改大；
   * 其他不支持；
   */
  public void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException{
    if(coltype==FIELD_TYPE_STR){
      modifyStrCol(conn,tablename,fieldname,len,null,false);
    }
  }
  
  

  /**
   * 更改表结构
   * 不直接支持表字段的更名，类型的转换，非字符类型长度的变化等；
   * 这里提供一个修改的方法：
   * 根据要修改的字段建新表，copy原表数据到新表，删除原表，更名新表为原表名；
   * 注意：在进行写入表数据过程中，不能调用此方法修改字段信息；
   *      比如DataCopy的写入功能；需要跟据值长度，来修改表的字段长度；
   * 
   * @param conn Connection
   * @param tablename String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param col String
   * @throws Exception
   */
  public void modifyColumn(Connection conn,String tablename, String col, String new_col,char coltype,
                           int len, int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException {
    /**
     * 这里不用系统变量dl，是因为此方法给了conn, 通过系统变量dl获取表结构时，会造成嵌套获取连接；
     */
    Dialect dl = SqlFunc.createDialect(conn);
    /**
     * 字段可能不是只有长度发生变化，DB2字段修改通过建新表实现；
     * 下面注释的代码是多余的；
     */
    /*TableColumnMetaData cmd = dl.getTableColumnMetaData(tablename, col);
    char srcColType = getFieldType(cmd.getType());
    if((new_col==null||new_col.length()==0||col.equalsIgnoreCase(new_col))
        &&srcColType==coltype
        &&srcColType==FIELD_TYPE_STR){
      if(cmd.getLen()<len){
        //字符串类型长度可以直接改大
        modifyStrCol(conn,tablename,col,len,defaultvalue,unique);
      }
      //不能改小；
      return;
    }*/
    //先根据新类型创建表，在copy数据；
    //但是数据目前只支持如下数据的转换，其他转换不copy数据；
    //int -> number,str
    //number -> int,str
    //str -> int,num 如果不是数字值，报异常
    //date -> str,timestamp
    //timestamp -> date
    TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
    String newtbname = createTable(conn,dl,tmd,col,new_col,coltype,len,dec,defaultvalue,unique,nullable,false);
    String copysql = getCopyeSql(conn,dl,tmd,newtbname,col,new_col,coltype,false);
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(copysql);
    }
    finally {
      stmt.close();
    }
    dropTable(conn, null, tablename);
    renameTable(conn, newtbname, tablename);
  }
  
  /**
   * 20090825
   * DB2为修改字段和删除字段后，创建的新表 copy数据；
   * @param conn
   * @param dl
   * @param tmd
   * @param newtbname
   * @param col
   * @param new_col
   * @param coltype
   * @param isDel
   *        是否删除字段col的标识；
   *        =true , 则 new_col,coltype参数无效；
   * @return
   */
  private String getCopyeSql(Connection conn, Dialect dl, TableMetaData tmd,String newtbname,
      String col, String new_col, char coltype,boolean isDel) {
    StringBuffer sql = new StringBuffer(256);
    sql.append("insert into ").append(newtbname).append(" (");
    TableColumnMetaData[] cols = tmd.getColumns();
    for(int i=0;i<cols.length;i++){
      TableColumnMetaData colmd = cols[i];
      if(colmd.isAutoInc())
        continue;
      String colname = colmd.getName();
      if(colname.equalsIgnoreCase(col)){
        if(!isDel){
          sql.append(getColumnName(new_col==null||new_col.length()==0?colname:new_col)).append(',');
        }
      }else{
        sql.append(getColumnName(colname)).append(',');
      }
    }
    sql.deleteCharAt(sql.length()-1);
    sql.append(")");
    sql.append(" select ");
    for(int i=0;i<cols.length;i++){
      TableColumnMetaData colmd = cols[i];
      if(colmd.isAutoInc())
        continue;
      String colname = colmd.getName();
      if(colname.equalsIgnoreCase(col)){
        if(!isDel){
          sql.append(dl.funcToSqlVar(getColumnName(colname), colmd.getType(), getSqlType(coltype), null)).append(',');
        }
      }else{
        sql.append(getColumnName(colname)).append(',');
      }
    }
    sql.deleteCharAt(sql.length()-1);
    sql.append(" from ").append(tmd.getTableName());
    return sql.toString();
  }
  
  /**
   * 20090825
   * DB2为修改字段和删除字段创建新的表；
   * @param conn
   * @param dl
   * @param tmd
   * @param col
   * @param new_col
   * @param coltype
   * @param len
   * @param dec
   * @param defaultvalue
   * @param unique
   * @param nullable
   * @param isDel
   *        是否删除字段col的标识；
   *        =true , 则 new_col,coltype,len,dec,defualtvalue,unique,nullable参数无效；
   * @return
   * @throws Exception
   */
  private String createTable(Connection conn, Dialect dl, TableMetaData tmd, String col, String new_col,
      char coltype, int len, int dec, String defaultvalue, boolean unique, boolean nullable,boolean isDel) throws SQLException {
    DbDefiner dbf = dl.createDbDefiner();
    dbf.clearDefineInfo();
    String desttable = dbf.getCreateTableName(conn, tmd.getTableName(), null);
    String incName = null;//自动增长字段名；
    String[] keys = tmd.getPrimaryKey();
    for(int i=0;i<tmd.getColumnCount();i++){
      TableColumnMetaData colmd = tmd.getColumn(i);
      String colname = colmd.getName();
      boolean iskey = isKey(colname,keys);
      if(colname.equalsIgnoreCase(col)){
        //如果是主键，db2不能维空；
        if(!isDel){
          //定义修改字段；
        	//如果是主键，则不能定义为unique，否则建表出错；
          dbf.defineField(new_col==null||new_col.length()==0?colname:new_col, coltype, len, dec, getDefineDefaultValue(defaultvalue,coltype), iskey?false:nullable, iskey?false:unique);
        }
      }else{
        if(colmd.isAutoInc()){
          incName = colname;
          dbf.defineAutoIncField(colname, 1);
        }else{
          char tp = getFieldType(colmd.getType());
          int l = colmd.getLen();
          int cl = colmd.getScale();
          //不用修改的字段，使用原来的属性
          dbf.defineField(colname, tp, l
              , cl, getDefineDefaultValue(colmd.getDefaultValue(),tp), colmd.isNullable(), iskey?false:colmd.isUnique(),colmd.getDesc());
        }
      }
    }
    //创建主键
    if(keys!=null){
      StringBuffer ks = new StringBuffer(10*keys.length);
      for(int i=0;i<keys.length;i++){
        /**
         * 20090807
         * 主键中的字段可能改名，创建新表是用新的字段建主键；
         * 否则建表失败，主键字段不是表的字段；
         */
        String keyfield = keys[i];
        if(keyfield.equalsIgnoreCase(col)){
          if(isDel) continue;//删除的字段是主键之一；
          if(new_col!=null&&new_col.length()>0){
            keyfield = new_col;
          }
        }
        ks.append(keyfield);
        ks.append(',');
      }
      if(ks.length()>0){
        ks.deleteCharAt(ks.length()-1);
        dbf.definePrimaryKey(ks.toString());
      }
    }
    //创建原表的索引
    TableIndexMetaData[] indexes = tmd.getIndexes();
    if(indexes!=null){
    for(int i=0;i<indexes.length;i++){
    	TableIndexMetaData imd = indexes[i];
    	String fds[] = imd.getColumns();
    	if(fds.length==1 && incName!=null && fds[0].equalsIgnoreCase(incName))
    	  continue;//过滤自动增长字段的索引；
    	if(fds.length==1&&fds[0].equalsIgnoreCase(col)){
    	  //要修改的字段是unique，现在需要将其修改为!unique,这里的唯一索引就不要再建了；
    	  if(imd.isUnique()&&!unique)
    	    continue;
    	}
    	StringBuffer fields = new StringBuffer(fds.length*20);
    	for(int j=0;j<fds.length;j++){
    		if(fields.length()>0) fields.append(",");
    		if(fds[j].equalsIgnoreCase(col)){
    		  if(isDel){
    		    continue; //需要删除的字段，就不用在建索引了；
    		  }
    			fields.append(new_col==null||new_col.length()==0?col:new_col);
    		}else fields.append(fds[j]);
    	}
    	if(fields.length()>0){//加了删除标识，对删除字段的单索引，就不用建了；
    	  fields.insert(0, '(');
    	  fields.append(")");
    	  dbf.defineIndex(imd.getName(), fields.toString(), imd.isUnique());
    	}
    }
    }
    /**
     * 20091102
     * 修改字段自动调整字段长度，依据DB2某些限制，比如主键列组合最大长度是1000
     */
    dbf.createTable(conn, desttable,false,true);
    return desttable;
  }
  /**
   * 建表时，给的默认值需要用'引起来；
   * @param defaultValue
   * @param coltype 
   * @return
   */
  private String getDefineDefaultValue(String defaultValue, char coltype) {
    if(defaultValue==null) return null;
    if(coltype!=DbDefiner.FIELD_TYPE_STR) return defaultValue;
    if(defaultValue.length()==0) return "''";
    if(defaultValue.length()>=2&&defaultValue.startsWith("'")&&defaultValue.endsWith("'"))
      return defaultValue;
    return "'"+defaultValue+"'";
  }
  private boolean isKey(String col, String[] keys) {
    if(keys==null) return false;
    for(int i=0;i<keys.length;i++){
      if(col.equalsIgnoreCase(keys[i]))
        return true;
    }
    return false;
  }
  private void modifyStrCol(Connection conn, String tablename, String col,
      int len, String defaultvalue, boolean unique) throws SQLException {
    StringBuffer ddl = new StringBuffer(64).append("ALTER TABLE ");
    ddl.append(tablename);
    ddl.append(" ALTER COLUMN ");
    /**
     * 20091117
     * 可能是关键字，会加引号，只在修改字段时使用，根据字段名查该字段属性，需要使用原字段名；
     */
    String col2 = getColumnName(dl, col);
    /**
     * 20091102
     * 修改字段长度时，自动根据数据库限制，比如主键组合长度等限制，调整字段长度；
     */
    Dialect dl = SqlFunc.createDialect(conn);
    TableMetaData tbmd = dl.createDbMetaData().getTableMetaData(tablename);
    TableColumnMetaData colmeta = null;
    for(int i=0;i<tbmd.getColumnCount();i++){
      if(tbmd.getColumnName(i).equalsIgnoreCase(col)){
        colmeta = tbmd.getColumns()[i];
      }
    }
    int newlen = adjustFieldLengthForModify(tbmd,col,len);
    /**
     * 20091112 
     * DB2 9.5 及以下版本，不支持字段长度改小；
     * 20091117
     * 不是和调整过的长度比较，而是和原字段长度比较；
     */
    if(newlen<=colmeta.getLen()){
      return;
    }
    ddl.append(getFldDdl(new TableColumnMetaDataHelper(col2 + " SET DATA TYPE ", FIELD_TYPE_STR, newlen,
        0, true, unique,defaultvalue,null),true));
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
  /**
   * DB2 8不能直接删除字段
   * 20090825
   * 现在使用下面方法实现：
   * 创建新表，copy数据，删除原表，更名新表名为原表名；
   */
  public void dropColumn(Connection conn, String tablename, String col)
    throws SQLException {
    TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
    String newtbname = createTable(conn,dl,tmd,col,null,'C',-1,-1,null,false,true,true);
    String copysql = getCopyeSql(conn,dl,tmd,newtbname,col,null,'C',true);
    Statement stmt = conn.createStatement();
    try {
      stmt.execute(copysql);
    }
    finally {
      stmt.close();
    }
    dropTable(conn, null, tablename);
    renameTable(conn, newtbname, tablename);
    
  }
  protected String getIntFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    return getColumnName(fi.getName()) + " INTEGER " +
	    getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_INT, isUpdate);
	  }
  /**
   * DB2数值最大长度是31，包含小数位数；
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
	      //无法确定精度，使用double类型；
	      numddl.append(" DOUBLE");
	    }
	    numddl.append(' ');
	    numddl.append(getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_FLOAT, isUpdate));
	    return numddl.toString();

	  }

	  protected String getTimeFldDdl(TableColumnMetaData fi, boolean isUpdate) {
	    //DB2将time类型定义成timestamp， 在setTime时报类型不一致异常；
	    return getColumnName(fi.getName()) + " TIME " +
	        getTailDdl(fi.getDefaultValue(), fi.isNullable(), fi.isUnique(),fi.getDesc(),DbDefiner.FIELD_TYPE_TIMESTAMP, isUpdate);
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
	  /**
	   * 
	ALTER TABLE test_1 ADD field1 VARCHAR(20)  UNIQUE  NOT NULL 

	SQL0193N  在 ALTER TABLE 语句中，列 "FIELD3                             
	                                   " 已被指定为 NOT NULL，而 DEFAULT 
	子句却未指定或被指定为 DEFAULT NULL。

	解释: 

	当对已存在的表添加新列时，必须为所有现有的行给此新列赋值。缺省情况
	下，指定空值。但是，因为该列定义为NOT
	NULL，所以必须定义除空之外的缺省值。


	ALTER TABLE db2admin.test_1 ADD field33 VARCHAR(20)  UNIQUE   DEFAULT ''
	ALTER TABLE db2admin.test_1 ADD field33 VARCHAR(20)  UNIQUE 

	SQL0542N  "FIELD33                                                
	               " 不能是一列主键或唯一键，因为它可包含空值。

	解释: 

	PRIMARY KEY 子句或 UNIQUE 子句中标识的列 "<名称>"
	被定义为允许空值。 

	联合系统用户：某些 数据源 不向 "<名称>"
	提供适当的值。在这些情况下，适当的字段包含一些值（如
	"unknown"），指示实际值未知。 


	ALTER TABLE db2admin.test_1 ADD field5 VARCHAR(20)  not null;

	SQL0193N  在 ALTER TABLE 语句中，列 "FIELD5                             
	                                   " 已被指定为 NOT NULL，而 DEFAULT 
	子句却未指定或被指定为 DEFAULT NULL。

	解释: 

	当对已存在的表添加新列时，必须为所有现有的行给此新列赋值。缺省情况
	下，指定空值。但是，因为该列定义为NOT
	NULL，所以必须定义除空之外的缺省值。 

	用户响应: 

	除去对列的 NOT NULL 限制或为列提供除空之外的缺省值。



	ALTER TABLE test_1 ADD field1 VARCHAR(20)  UNIQUE  NOT NULL DEFAULT ''
	执行成功

	ALTER TABLE db2admin.test_1 ADD field4 VARCHAR(20)  
	执行成功

	ALTER TABLE db2admin.test_1 ADD field5 VARCHAR(20)  default ''
	成功

	ALTER TABLE db2admin.test_1 ADD field6 VARCHAR(20)  not null default ''
	成功

  结论：增加字段时 unique 必须 not null default ''
                not null 必须  default ''
	   */
	  protected String getTailDdl(String defaultvalue, boolean nullable, boolean unique, String desc, char t,
      boolean isUpdate) {
    if (isUpdate) {
      //增加字段
      StringBuffer str = new StringBuffer(32);
      if (unique) {
        str.append(" UNIQUE NOT NULL ");
        String def = getDefualtSql(defaultvalue, t);
        if (def != null)
          str.append(def);
        return str.toString();
      }else
      if (!nullable) {
        str.append(" NOT NULL ");
        String def = getDefualtSql(defaultvalue, t);
        if (def != null)
          str.append(def);
        return str.toString();
      }else
      if (defaultvalue != null && defaultvalue.length() > 0) {
        str.append(" DEFAULT ").append(defaultvalue);
      }

      return str.toString();
    }
    else {
      //建表
      StringBuffer str = new StringBuffer(16);
      if (defaultvalue != null && defaultvalue.length()>0) {
        str.append(" DEFAULT ").append(defaultvalue);
      }
      if(unique) {
        /**
         * 建表时，设置unique，必须为not null;
         * 否则报：42831 主键或唯一键列不允许空值，异常；
         */
        str.append(" UNIQUE NOT NULL");
      }else{
        if(!nullable) str.append(" NOT NULL ");
      }
      return str.toString();
    }
  }
	  
    protected void addDescToField(Connection conn,String tbname) throws SQLException {
	    Statement stat = conn.createStatement();
	    try {
	      for (int i = 0; i < this.tbMetaHelper.getColumnCount(); i++) {
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
	  }private String getDescToFieldSql(String tbname,TableColumnMetaData fi) {
	    return getDescToFieldSql(tbname,fi.getName(),fi.getDesc());
	  }
	  private String getDescToFieldSql(String tbname,String fieldname,String desc) {
	    StringBuffer sql = new StringBuffer(50);
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
		
	  protected String getFieldCommentSql(String viewname, String colname, String comment) {
		  if (StrFunc.isNull(viewname) || StrFunc.isNull(colname) || StrFunc.isNull(comment))
			  return "";
	  			
		  return getDescToFieldSql(viewname, colname, comment);
	  }
}
