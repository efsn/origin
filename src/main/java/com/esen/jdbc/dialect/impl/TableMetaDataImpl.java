package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;

/**
 * 从数据库获取表结构的基类；
 * 各个数据库的实现都继承该类，尽量复用代码；
 * @author dw
 *
 */
public class TableMetaDataImpl extends TableMetaDataHelper {

  protected DbMetaDataImpl owner;

  private boolean initprimarykey;

  protected boolean isinitindex;//记录是否初始化indexes
  
  /**
   * 判断参数tablename是不是数据库表名；
   * 如果是视图，isTable=false;
   * 如果isTable==false， 则不需要获取索引，主键等信息；
   */
  protected boolean isTable;

  protected static final String SQL_COLUMN = "SELECT * FROM ";

  public TableMetaDataImpl(DbMetaDataImpl owner, String tablename) {
    super(tablename);
    this.owner = owner;
    checkTableName();
  }
  
  /**
   * 获取所属数据库结构对象；
   * @return
   */
  protected DbMetaDataImpl getOwner(){
	  return owner;
  }

  /**
   * 判断表名是否是数据库表、视图，还是其他类型；
   */
  private void checkTableName() {
    try {
      Connection conn = owner.getConnection();
      try{
        Dialect dl = SqlFunc.createDialect(conn);
        DbDefiner def = dl.createDbDefiner();
        /*
      	 * imp by RX 2014.09.10
      	 * 为分析组件添加此处理
      	 * 分析组件处理的是可能较为复杂的SQL，作为主题表分析时，可能要求在使用时动态分析表结构
      	 * 加个try/catch，避免传入表名为复杂SQL时报异常，导致无法分析表结构信息
      	 * to do:目前暂只对Oracle支持较好，其他数据库类型待专项测试
      	 */
        try {
        	isTable = def.tableExists(conn, null, tablename);
        }
        catch(Exception e) {
        	isTable = false;
        }
      }finally{
        owner.closeConnection(conn);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    
  }


  /**
   * 返回字段
   * @return
   * @throws Exception 
   */
  public TableColumnMetaData[] getColumns() {
    initCols();
    return super.getColumns();
  }

  protected void initCols() {
    if (columnList == null||columnList.size()==0) {
      try {
        this.initColumns();
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
  
  public TableColumnMetaData getColumn(int i) {
    initCols();
    return  super.getColumn(i);
  }
  
  public TableColumnMetaData getColumn(String colname){
	  initCols();
	  return  super.getColumn(colname);
  }

  public int getColumnCount() {
    initCols();
    return super.getColumnCount();
  }

/*  public int getColumnIndex(String colname) {
    for (int i = 0; i < getColumnCount(); i++) {
      if (getColumnName(i).equalsIgnoreCase(colname)) {
        return i;
      }
    }
    return -1;
  }*/

  protected void setColumnDesc(int i, String coldes) {
    ((TableColumnMetaDataHelper)getColumn(i)).setDesc(coldes);
  }

  public synchronized String[] getPrimaryKey() {
    if(!isTable){//如果是视图，不需要获取主键；
      return null;
    }
    if(!initprimarykey){
      initPrimaryKey();
      initprimarykey = true;
    }
    return super.getPrimaryKey();
  }
  /**
   * 返回数据库用户名；
   * 主要用于DatabaseMetaData.getColumn(...)等系统函数做schema参数；
   * 有些数据库需要大些；
   * @param dbUserName
   * @return
   */
  protected String getSchemaName(){
    return owner.getSchemaName();
  }
  /**
   * 通过调用jdbc的 getPrimaryKeys 方法获取主键；
   */
  protected void initPrimaryKey() {
    List pklist = new ArrayList();
    String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
    try {
      Connection con = this.owner.getConnection();
      try {
        DatabaseMetaData md = con.getMetaData();
        ResultSet rs = md.getPrimaryKeys(null, tbs[0], tbs[1]);
        try{
        while(rs.next()){
          String tbname = rs.getString("TABLE_NAME");
          if(tbname.equals(tbs[1])){
            String[] pk = new String[3];
            pk[0]= rs.getString("PK_NAME");//可能为空
            pk[1]= rs.getString("COLUMN_NAME");
            pk[2]= rs.getString("KEY_SEQ");
            pklist.add(pk);
          }
        }
        }finally{
          if(rs!=null)
            rs.close();
        }
      }finally {
        this.owner.closeConnection(con);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    if(pklist.size()>0){
      Collections.sort(pklist,new Comparatored());
      String[] primarykey = new String[pklist.size()];
      for(int i=0;i<primarykey.length;i++){
        primarykey[i] = ((String[])pklist.get(i))[1];
      }
      this.setPrimaryKey(primarykey);
      /**
       * 主键只能有一组，这里返回的是主键的字段数组；
       * 原来的代码是错误的，主键不可能有多组；
       */
    }
  }

  public class Comparatored implements Comparator{

    public int compare(Object o1, Object o2) {
      String[] pk1 = (String[])o1;
      String[] pk2 = (String[])o2;
      int sort1 = Integer.parseInt(pk1[2]);
      int sort2 = Integer.parseInt(pk2[2]);
      
      return sort1-sort2;
    }
    
  }
  protected void initColumns() throws Exception {
    Connection con = this.owner.getConnection();
    try {
      DatabaseMetaData md = con.getMetaData();
      String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
      ResultSet rs = md.getColumns(null, tbs[0], tbs[1], null);
      try {
        while (rs.next()) {
          String tbname = rs.getString("TABLE_NAME");
          /**
           * 20090218
           * 可能会有重复的表，比如t_hy将会把tahy表（如果存在）的字段读进来；
           */
          if(!tbname.equalsIgnoreCase(tbs[1])){
            continue;
          }
          String colname = rs.getString("COLUMN_NAME");
          int tp = rs.getInt("DATA_TYPE");
          int len = rs.getInt("COLUMN_SIZE");
          int dec = rs.getInt("DECIMAL_DIGITS");
          String isnullable = rs.getString("IS_NULLABLE");
          boolean nullable = isnullable == null || !isnullable.trim().equals("NO");
          String defvalue = rs.getString("COLUMN_DEF");
          String desc = rs.getString("REMARKS");
          TableColumnMetaDataProvider column = new TableColumnMetaDataProvider(this,colname);
          column.setLable(colname);
          column.setType(tp);
          column.setLength(len);
          column.setScale(dec);
          column.setNullable(nullable);
          
          column.setDefaultValue(getDefaultValue(defvalue));
          column.setDesc(desc);
          addColumn(column);
        }
      }
      finally {
        rs.close();
      }
    }
    finally {
      this.owner.closeConnection(con);
    }

  }
  
  /**
   * 从数据库里读取的默认值，如果是常量会可能用''括起来，根据jdbc驱动不同，实现的不同：
   * 一般通过DatabaseMetaData.getColumns()获得的默认值是不带''的；
   * 而通过系统表查出来的默认值一般都会带''；
   * 这里通过一个统一的方法处理。
   * 20120223 dw
   * 20122023 dw
   * @param defvalue
   * @return
   */
	protected String getDefaultValue(String defvalue) {
		//读出NULL表示没有默认值；
		if("null".equalsIgnoreCase(defvalue)){
			return null;
		}
		
		/*
		 * TODO
		 * modify by liujin 2014.06.27
		 * 默认值如果去掉单引号，就无法区分是 常量、函数、表达式了，所以不去掉
		 * 目前不确定哪种情况下，获取到字符类型默认值不带单引号，先不处理
		 * 后期开发过程中如果遇到问题，再继续修改
		 */	
//		if (defvalue != null) {
//			/**
//			 * 这里读出的默认值，如果是字符串类型默认值，默认值都用''扩起来，读取时将''去掉；
//			 */
//			defvalue = defvalue.trim();
//			//如果是空串，会出异常；
//			if (defvalue.length() > 0 && defvalue.charAt(0) == '\'' && defvalue.endsWith("'"))
//				defvalue = defvalue.substring(1, defvalue.length() - 1);
//		}
		return defvalue;
	}

  public TableIndexMetaData[] getIndexes() {
    if(!isTable){//如果是视图，不需要获取索引；
      return null;
    }
    if (!isinitindex) {
      try {
        this.initIndexes();
        this.isinitindex = true;
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return super.getIndexes();
  }
  
  protected static final String INDEX_NAME = "INDEX_NAME";
  protected static final String INDEX_NON_UNIQUE="NON_UNIQUE";
  protected static final String INDEX_COLUMN_NAME="COLUMN_NAME";

  protected void initIndexes() throws Exception {
    /**
     * 20090803
     * 初始化索引前，需要初始化字段列表；
     * 原因是初始化索引需要读取字段列表，这两个初始化都需要获取数据库连接；
     * 如果在初始化索引里面，再初始化字段列表，就会有嵌套连接；
     */
    initCols();
    Connection con = owner.getConnection();
    try {
    	/*
    	 * ESENFACE-1029: modify by liujin 2014.12.29
    	 * Syabase 中，dbmd.getIndexInfo 在 connection 为非自动提交时，会报错
    	 * 错误信息为：在tempdb'数据库的一个多语句事务内''CREATE TABLE'命令不被允许
    	 * 将 connection 修改为自动提交。
    	 */
    	Dialect dl = SqlFunc.createDialect(con);
    	boolean isAutoCommit = con.getAutoCommit();
    	if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
    		con.commit();
    		con.setAutoCommit(true);
    	}
    	try {
	      DatabaseMetaData dbmd = con.getMetaData();
	      String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(),owner.getDataBaseInfo());
	      ResultSet rs = dbmd.getIndexInfo(null, tbs[0], tbs[1], false, false);
	      HashMap pvs = new HashMap();
	      //int count = rs.getMetaData().getColumnCount();
	      try{
	      while(rs.next()){
	        String[] pv = new String[3];
	        pv[0] = rs.getString(INDEX_NAME);
	        if(pv[0]==null) continue;
	        pv[1] = rs.getString(INDEX_NON_UNIQUE);
	        pv[2] = rs.getString(INDEX_COLUMN_NAME);
	        //检查索引字段是不是表字段；
	        if(!checkIndexField(pv[2])) continue;
	        Object o = pvs.get(pv[0]);
	        if(o==null){
	          List l = new ArrayList();
	          l.add(pv);
	          pvs.put(pv[0], l);
	        }else{
	          List l = (List)o;
	          l.add(pv);
	        }
	        /*for(int i=0;i<count;i++){
	          System.out.print(rs.getString(i+1)+"\t");
	        }
	        System.out.println();*/
	      }
	      }finally{
	        if(rs!=null)
	          rs.close();
	      }
	      Set keys = pvs.keySet();
	      Iterator it = keys.iterator();
	      while(it.hasNext()){
	        String indexname = (String)it.next();
	        List l = (List)pvs.get(indexname);
	        String cols[] = new String[l.size()];
	        boolean unique = false;
	        for(int i=0;i<l.size();i++){
	          String[] pv = (String[])l.get(i);
	          if(i==0){
	            if(pv[1].equals("0")){
	              //sqlserver2000 这里返回0和1，  0表示唯一；
	              unique = true;
	            }else if (pv[1].equals("1")){
	              unique = false;
	            }else{
	              unique = !Boolean.valueOf(pv[1]).booleanValue();
	            }
	          }
	          cols[i] = pv[2];
	        }
	        TableIndexMetaDataImpl imd = new TableIndexMetaDataImpl(indexname,cols,unique);
	        this.addIndexMeta(imd);
	      }
    	} finally {
        	if (!dl.supportCreateTableInTransaction() && !isAutoCommit) {
        		con.setAutoCommit(false);
        	}
    	}
    }finally{
      owner.closeConnection(con);
    }
  }
  
  /**
   * 20090801
   * 检查索引字段是不是表字段；
   * 做这个检查是因为有时候索引字段并不是表字段，比如Oracle 的物化视图；
   * 如果索引字段不是表字段，该索引将被忽略；
   * @param indexfield
   * @return
   */
  protected boolean checkIndexField(String indexfield) {
    for(int i=0;i<this.getColumnCount();i++){
      String field = this.getColumnName(i);
      if(field.equalsIgnoreCase(indexfield)){
        return true;
      }
    }
    return false;
  }

	public Object[] getFieldSample(String field, int howToSample) throws SQLException {
		String sql = getSampleSql(field,howToSample);
		
		List l = new ArrayList(10);
		Connection conn = owner.getConnection();
		try {
			Dialect dl = SqlFunc.createDialect(conn);
			String sqlstr = sql;
			if (howToSample == FIELD_SAMPLE_SELECTRANDOM) {
				sqlstr = dl.getLimitString(sqlstr, 0, 10);
			}
			Statement stat = conn.createStatement();
			ResultSet rs = stat.executeQuery(sqlstr);
			int n = 0;
			while (rs.next()) {
				if (n >= 100000) {
					/**
					 * 考虑内存空间的影响，如果记录条数超过10w条，则只缓存10w条记录；
					 */
					break;
				}
				l.add(rs.getObject(1));
				n++;
			}
			rs.close();
			stat.close();
		}
		finally {
			owner.closeConnection(conn);
		}
		if (l.size() > 0) {
			Object[] objs = new Object[l.size()];
			l.toArray(objs);
			return objs;
		}
		else {
			/**
			 * 如果查不到值返回空数组，而不是null；
			 * 目的是为了缓存实现类方便处理是否已经缓存；
			 */
			return new Object[0];
		}
	}

	protected String getSampleSql(String field, int howToSample) {
		StringBuffer sql = new StringBuffer(64);
		switch(howToSample){
			case FIELD_SAMPLE_SELECTRANDOM:{
				sql.append("select ");
				sql.append(field).append(" from ").append(tablename);
				sql.append(" where ").append(field).append(" is not null");
				break;
			}
			case FIELD_SAMPLE_DISTINCTALL:{
				sql.append("select ");
				sql.append("distinct ");
				sql.append(field).append(" from ").append(tablename);
				sql.append(" where ").append(field).append(" is not null");
				break;
			}
			case FIELD_SAMPLE_MAX:{
				sql.append("select max(");
				sql.append(field).append(") from ").append(tablename);
				break;
			}
			
		}

		return sql.toString();
	}
}

