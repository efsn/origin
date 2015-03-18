package com.esen.db.sql;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.ExceptionHandler;

/**
 * 将复杂的sql拆成多个临时表，分步计算；
 * 为了分析sql方便，用S表示一个完整的select语句，用T表示一个数据库表；
    S(TST) 第一个S表示一个sql由一两个数据库表和一个子sql组成，TST是并列关系关联；
    第二个S表示这个子sql是全部由数据库表组成的，没有子sql；
    
    例：
    S(S(SS)S)
    S(S(SSS)S)
    S(SS(TTSS))

    红色部分本身就是子sql，其自身结构还嵌套子sql，这样的节点可以使用临时表替换；

    如果多层嵌套： S(S(S(STT)T)ST)
     蓝色部分用一个临时表替换；
     红色（包含蓝色）再用一个临时表，这个临时表将引用蓝色部分的临时表；
 * @author dw
 *
 */
public class SelectTableStep {
  private SelectTable newst;
  private ConnectionFactory conf;
  private Connection conn;
  private Statement stat;
  private List temptblist;//临时表表名集合
  private List tlist; //创建临时表的sql集合
  private List oracleInsertSqlList;//用于存储oracle的插入语句；
  public SelectTableStep(SelectTable st){
    newst = (SelectTable)st.clone();
  }

  /*
   * 分析过程需要保证不能自动提交，否则给创建的临时表数据插入的数据无法查询到
   */
  private void analyse(SelectTable st) {
    for(int i=0;i<st.getTableCount();i++){
      BaseTable bt = st.getTable(i);
      if(bt instanceof SelectTable){
        SelectTable sti = (SelectTable)bt;
        if(checkStep(sti)){
          analyse(sti);
        }
        if(haveSubSelect(sti)){
          analyseStep(st,sti,i);
        }
      }
    }
  }
  /**
   * 将子sql用临时表替换sti
   * Oracle和SybaseIQ用临时表，其他暂时用物理表代替
   * 分析过程Oracle和SybaseIQ需要保证不能自动提交，否则给创建的事务型临时表数据插入的数据无法查询到
   * 其他创建的物理表反而需要
   * @param st
   * @param i
   * @param sti 
   */
  private void analyseStep(SelectTable st, SelectTable sti, int i) {
    SelectTable st_tmp = (SelectTable)sti.clone();
    String temptb =null;
    try {
      DataBaseInfo dbtype = conf.getDbType();
      if(dbtype.isOracle())
        temptb = createOracleTempTable(st_tmp);
      else if(dbtype.isSybaseIQ())
        temptb = createSybaseIQTempTable(st_tmp);
      else temptb = createDefaultempTable(st_tmp);
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    st.removeTable(i);
    RealTable rt = new RealTable(temptb,st_tmp.getAlias());
    rt.addJoinOnCondition(st_tmp.getJoinOnCondition());
    rt.addJoinWhereCondition(st_tmp.getJoinWhereCondition());
    rt.setJoinType(st_tmp.getJoinType());
    st.addTable(i, rt);
  }
  
  private String createDefaultempTable(SelectTable st_tmp) throws Exception {
    Dialect dl = conf.getDialect();
    //temptb = def.getCreateTableName(conn, "temp", String.valueOf(Math.round(Math.random()*100000)));
    String temptb = "temp_"+String.valueOf(Math.round(Math.random()*1000000));
    String querysql = st_tmp.getSql(dl);
    String tempsql = dl.getCreateTableByQureySql(temptb, querysql, false);
    /*
     * ISSUE:ESENBI-1462 chg by RX 2014.09.25
     * 改动ESENBI-1462后带来的改动，
     * 之前在OlapResultMulImpl#queryData分析分步执行SQL前，设置了autocommit=true
     * 但是Oracle和SybaseIQ等创建的事务型临时表一旦提交数据就丢失了，导致查询不到数据，需要设置autocommit=false
     * 其他数据库的则需要创建物理实体表（需要autocommit=true），在查询全部完成后会删除创建的临时表
     */
    boolean autoCommit = this.conn.getAutoCommit();
    try {
    	this.conn.setAutoCommit(true);
    	stat.executeUpdate(tempsql);
      if(temptblist==null)
        temptblist = new ArrayList();
      temptblist.add(temptb);
      if(tlist==null)
        tlist = new ArrayList();
      tlist.add(tempsql);
      return temptb;
    }
    finally {
    	this.conn.setAutoCommit(autoCommit);
    }
  }

  private String createSybaseIQTempTable(SelectTable st_tmp) throws Exception {
    Dialect dl = conf.getDialect();
    //temptb = def.getCreateTableName(conn, "temp", String.valueOf(Math.round(Math.random()*100000)));
    String temptb = "#temp_"+String.valueOf(Math.round(Math.random()*1000000));
    String querysql = st_tmp.getSql(dl);
    String tempsql = dl.getCreateTableByQureySql(temptb, querysql, true);
    /*
     * ISSUE:ESENBI-1462 chg by RX 2014.09.25
     * 解决Oracle和SybaseIQ数据库中报表属性“临时表优化SQL”勾选后计算结果不正确的问题
     */
    boolean autoCommit = this.conn.getAutoCommit();
    try {
    	this.conn.setAutoCommit(false);
    	stat.executeUpdate(tempsql);
    } finally {
    	this.conn.setAutoCommit(autoCommit);
    }
    if(temptblist==null)
      temptblist = new ArrayList();
    temptblist.add(temptb);
    if(tlist==null)
      tlist = new ArrayList();
    tlist.add(tempsql);
    return temptb;
  }

  /**
   * Oracle有两种临时表
   * 1.事务型临时表用CREATE GLOBAL TEMPORARY TABLE <tbname> ON COMMIT DELETE ROWS as (select ...) 语句创建时，自动提交了，后面取数据就为空；
   *   需要改成：先创建表结构，在insert数据，但是表结构需要查一次数据库，这就需要操作两次数据库；效率可能有问题；
   * 2.会话型临时表，涉及到临时表删除问题；
   * 3.直接创建物理表，用完就删除；
   * 现在采用第一种方法
   * @param st_tmp
   * @return
   * @throws Exception
   * @throws SQLException
   */
  private String createOracleTempTable(SelectTable st_tmp) throws Exception {
    Dialect dl = conf.getDialect();
    //temptb = def.getCreateTableName(conn, "temp", String.valueOf(Math.round(Math.random()*100000)));
    String temptb = "temp_"+String.valueOf(Math.round(Math.random()*1000000));
    String querysql = st_tmp.getSql(dl);
    //String tempsql = def.getCreateTableByQureySql(temptb, querysql, false);
    
    //使用事务型临时表：
    //querysql = "select * from ("+querysql+") xx_ where 1=0";//获得结构
    //注释掉上句是因为Oracle Bug，有时可能出现 ORA-3113 "end of file on communication channel" 错误；
    //主要原因是后面的条件(1=0)有问题，做如下改动：
    String metasql = "select * from ("+querysql+") xx_ where rownum<0";
    String tempsql = dl.getCreateTableByQureySql(temptb, metasql, true);
    stat.executeUpdate(tempsql);
    if(temptblist==null)
      temptblist = new ArrayList();
    temptblist.add(temptb);
    String insertsql = "insert into "+temptb+" "+querysql;
    if(oracleInsertSqlList==null){
      oracleInsertSqlList = new ArrayList();
    }
    oracleInsertSqlList.add(insertsql);
    //每次create 都会自动提交，因此将insertsql储存起来，等都创建完毕在执行；
    //stat.executeUpdate(insertsql); 
    if(tlist==null)
      tlist = new ArrayList();
    tlist.add(tempsql);
    tlist.add(insertsql);
    return temptb;
  }

  private boolean haveSubSelect(SelectTable st) {
    for(int i=0;i<st.getTableCount();i++){
      BaseTable bt = st.getTable(i);
      if(bt instanceof SelectTable){
        return true;
      }
    }
    return false;
  }
  /**
   * 是否需要分步，建立临时表；
   * @param newst
   * @return
   */
  private boolean checkStep(SelectTable st) {
    for (int i = 0; i < st.getTableCount(); i++) {
      BaseTable bt = st.getTable(i);
      if (bt instanceof SelectTable) {
        SelectTable sti = (SelectTable) bt;
        if (sti.getTableCount() > 1){
          for (int j = 0; j < sti.getTableCount(); j++) {
            BaseTable btj = sti.getTable(j);
            if (btj instanceof SelectTable)
              return true;
          }
        }
      }
    }
    return false;
  }
  
  private void setAutoCommit(ConnectionFactory conf, Connection conn) {
  	DataBaseInfo dbi = conf.getDbType();
  	try {
  		/*
  		 * ISSUE:ESENBI-1462 chg by RX 2014.09.25
       * 解决Oracle和SybaseIQ数据库中报表属性“临时表优化SQL”勾选后计算结果不正确的问题
       * 为Oracle/SybaseIQ创建的事务型临时表insert数据时不能自动提交，一旦自动提交，事物结束后数据自动删除了，导致查询不到数据
       * 其他数据库类型构造临时表时创建的是物理实体表，会在查询最终完成后删除物理表
  		 */
  		conn.setAutoCommit(!(dbi.isOracle() || dbi.isSybaseIQ()));
  	}
  	catch (Exception ex) {
  		ExceptionHandler.rethrowRuntimeException(ex);
  	}
  }
  
  /**
   * 在同一个数据库会话中分析sql
   * 因为临时表的数据只在会话中存在，断开连接会话就结束；
   * @param conf  用于生成sql
   * @param conn  数据库会话；不能为空
   * @param stat  在同一事务中创建临时表；
   */
  public void analyse(ConnectionFactory conf,Connection conn, Statement stat){
    this.conf = conf;
    this.conn = conn;
    this.stat = stat;
    /*
     * chg by RX 2014.09.26
     * 不管内部怎么用，此函数用到的数据库连接不要影响外面的使用
     * 这里先将autocommit记住，用完后还原
     */
    try {
	    boolean autoCommit = conn.getAutoCommit();
	    try {
		    this.setAutoCommit(conf, conn);
		    analyse(newst);
		    excuteOracleInsert();
	    }
	    finally {
	    	conn.setAutoCommit(autoCommit);
	    }
    }
    catch (Exception ex) {
    	ExceptionHandler.rethrowRuntimeException(ex);
    }
  }

  private void excuteOracleInsert() {
    if (oracleInsertSqlList == null || oracleInsertSqlList.size() == 0)
      return;
    try {
      for (int i = 0; i < oracleInsertSqlList.size(); i++) {
        String insertsql = (String) oracleInsertSqlList.get(i);
        stat.executeUpdate(insertsql);
      }
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 返回经过临时表替换的sql
   * @return
   */
  public SelectTable getNewSelctTable(){
    return newst;
  }
  /**
   * 返回所有临时表；
   * @return
   */
  public String[] getAllTempTable(){
    if(temptblist==null)
      return null;
    String[] temps = new String[temptblist.size()];
    temptblist.toArray(temps);
    return temps;
  }
  /**
   * 返回所有临时表创建的sql
   * @return
   */
  public String[] getAllTempSql(){
    if(tlist==null)
      return null;
    String[] temps = new String[tlist.size()];
    tlist.toArray(temps);
    return temps;
  }
  /**
   * 会话结束后，删除临时表；
   * @throws Exception 
   * @throws Exception 
   */
  public void deleteTempTable() throws Exception {
    String temptbs[] = getAllTempTable();
    if(temptbs==null||temptbs.length==0)
      return;
    DbDefiner def = conf.getDbDefiner();
    Connection conn = conf.getConnection();
    try{
      for(int i=0;i<temptbs.length;i++){
        try{
          DataBaseInfo dbtype = conf.getDbType();
          if(dbtype.isOracle()||dbtype.isSybase())
            def.dropTempTable(conn, null, temptbs[i]);
          else def.dropTable(conn, null, temptbs[i]);
        }catch(Exception ex){
          ex.printStackTrace();
        }
      }
    }finally{
      if(conn!=null)
        conn.close();
    }
  }
}
