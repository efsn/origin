package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import com.esen.jdbc.dialect.impl.OtherDataBaseInfo;
import com.esen.jdbc.dialect.impl.db2.DB2DataBaseInfo;
import com.esen.jdbc.dialect.impl.dm.DMDataBaseInfo;
import com.esen.jdbc.dialect.impl.essbase.EssbaseDataBaseInfo;
import com.esen.jdbc.dialect.impl.gbase.GBase8tDataBaseInfo;
import com.esen.jdbc.dialect.impl.gbase.GBaseDataBaseInfo;
import com.esen.jdbc.dialect.impl.greenplum.GreenplumDataBaseInfo;
import com.esen.jdbc.dialect.impl.kingbasees.KingBaseESDataBaseInfo;
import com.esen.jdbc.dialect.impl.mssql.MssqlDataBaseInfo;
import com.esen.jdbc.dialect.impl.mysql.MysqlDataBaseInfo;
import com.esen.jdbc.dialect.impl.netezza.NetezzaDataBaseInfo;
import com.esen.jdbc.dialect.impl.oracle.OracleDataBaseInfo;
import com.esen.jdbc.dialect.impl.oscar.OscarDataBaseInfo;
import com.esen.jdbc.dialect.impl.sybase.SybaseDataBaseInfo;
import com.esen.jdbc.dialect.impl.sybase.SybaseIQDataBaseInfo;
import com.esen.jdbc.dialect.impl.teradata.TeradataDataBaseInfo;
import com.esen.jdbc.dialect.impl.timesten.TimeStenDataBaseInfo;
import com.esen.jdbc.dialect.impl.vertica.VerticaDataBaseInfo;
import com.esen.jdbc.orm.impl.ORMConnection;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.util.ExceptionHandler;

/**
 * 获取数据库属性的抽象类；
 * @author dw
 */
public abstract class DataBaseInfo {
  
  /**
   * 此方法兼容原有方法，调用此方法时，conn是已经经过包装的PooledConnection，所以可以不用defaultSchema参数；
   * @param conn
   * @return
   */
  public static final DataBaseInfo createInstance(Connection conn){
    return createInstance(conn,null);
  }
  
  /**
   * 20110517 加defaultSchema参数，用于当用户指定了defaultSchema，使其起作用；
   * 此方法只在初始化连接池时调用，因为初始化使conn是原始连接；
   * @param conn
   * @param defaultSchema
   * @return
   */
  public static final DataBaseInfo createInstance(Connection conn,String defaultSchema){
    if(conn==null) return null;
    if(conn instanceof PooledConnection){
      PooledConnection pconn = (PooledConnection)conn;
      return pconn.get_ds().getDbType();
    }
    
    //特殊处理 ORMConnection
    if (conn instanceof ORMConnection) {
    	Connection pconn = ((ORMConnection)conn).getConnection();
    	if (pconn instanceof PooledConnection) {
    		return ((PooledConnection)pconn).get_ds().getDbType();
    	}
    }

    try {
      DatabaseMetaData dbmd = conn.getMetaData();
      
      String databaseProductVersion = dbmd.getDatabaseProductVersion();
      String dbname = dbmd.getDatabaseProductName().toUpperCase();
      if (dbname.indexOf("ORACLE") >= 0) {
         return new OracleDataBaseInfo(conn,defaultSchema);
      }
      else if (dbname.indexOf("MYSQL") >= 0) {
        return new MysqlDataBaseInfo(conn,defaultSchema);
      }
      else if (dbname.indexOf("MICROSOFT") >= 0) {
        return new MssqlDataBaseInfo(conn,defaultSchema);
      }
      else if (dbname.indexOf("AS")==0||dbname.indexOf("DB2") >= 0) {
          /**
           * 支持AS400中的DB2数据库；
           */
          return new DB2DataBaseInfo(conn,defaultSchema);
        }
      else if (dbname.indexOf("SYBASE") >= 0 || dbname.indexOf("ADAPTIVE") >= 0 || dbname.equals("SQL SERVER")) {
        String version = databaseProductVersion.toUpperCase();
        if (version.indexOf("IQ") >= 0||dbname.indexOf("IQ")>=0) {
          return new SybaseIQDataBaseInfo(conn,defaultSchema);
        }
        else
          //用weblogic自带的sybase jdbc驱动时，dbmd.getDatabaseProductName()得到的值是"SQL Server";
          //sybase 12.5 dbmd.getDatabaseProductName(): "Adaptive Server Enterprise";
          return new SybaseDataBaseInfo(conn,defaultSchema);
      }
      else if (dbname.indexOf("OSCAR") >= 0) {
        return new OscarDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("TIMESTEN")>=0){
        return new TimeStenDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("KINGBASEES")>=0){
        return new KingBaseESDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("DM DBMS")>=0){
    	return new DMDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("ESSBASE")>=0){
    	return new EssbaseDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("GBASE")>=0){
        return new GBaseDataBaseInfo(conn,defaultSchema);
	  } else if (dbname.indexOf("8T DATABASE SERVER") >= 0) {
		  return new GBase8tDataBaseInfo(conn, defaultSchema);
      }else if(dbname.indexOf("NETEZZA")>=0){
    	  return new NetezzaDataBaseInfo(conn,defaultSchema);
      }else if(dbname.indexOf("POSTGRESQL")>=0){
    	  //Greenplum是POSTGRESQL的在开发版本，与POSTGRESQL兼容，jdbc驱动通用
    	  return new GreenplumDataBaseInfo(conn,defaultSchema);
      } else if (dbname.indexOf("TERADATA") >= 0) {
      	  return new TeradataDataBaseInfo(conn, defaultSchema);
      } else if (dbname.indexOf("VERTICA") >= 0) {
      	  return new VerticaDataBaseInfo(conn, defaultSchema);
	  }
      
      return new OtherDataBaseInfo(conn,defaultSchema);
    }
    catch (SQLException e) {
      ExceptionHandler.rethrowRuntimeException(e);
      return null;
    }
  }
  /**
   * 返回登录用户默认的schema名；
   * @return
   */
  public abstract String getDefaultSchema();
  /**
   * 返回数据库连接的url字符串；
   * @return
   */
  public abstract String getJdbcurl();
  
  /**
   * 返回数据库连接的用户名；
   * @return
   */
  public abstract String getUserName();
  
  /**
   * 返回数据库的ip地址或机器名，如果无法获取数据库的地址或机器名，那么返回值可能是null
   * 返回值不会是127.0.0.1或localhost，本地地址会转换为外部地址
   */
  public abstract String getDatabaseAddress();
  
  /**
   * 返回数据库的类型；
   * @return
   */
  public abstract int getDbtype();
  
  /**
   * 返回数据库的名称；
   * @return
   */
  public abstract String getDbName();
  
  public abstract boolean isOracle8i();
  
  public abstract boolean isOracle() ;

  public abstract boolean isDb2();
  public abstract boolean isDb29() ;
  /**
   * 判断数据库是不是DB2 For AS400版本；
   * @return
   */
  public abstract boolean isDB2ForAS400();
  public abstract boolean isMysql();

  public abstract boolean isMssql() ;
  public abstract boolean isMssql2005();
  public abstract boolean isSybase();

  public abstract boolean isSybaseIQ();
  public abstract boolean isOscar();
  public abstract boolean isTimesTen();
  public abstract boolean isKingBaseES();
  public abstract boolean isDM_DBMS();
  public abstract boolean isDM7();
  public abstract boolean isEssbase();
  public abstract boolean isGBase();
  public abstract boolean isGBase8T();
  public abstract boolean isNetezza();
  public abstract boolean isGreenplum();
  public abstract boolean isTeradata();
  public abstract boolean isVertica();

  public abstract boolean isPetaBase();
  
  /**
   * 表名，字段名是否区分大小写；
   * sybase ase 默认区分大小写；
   * sqlserver 默认不区分大小写，但是数据库中的值也不区分大小写；
   * 如果更改排序规则使数据库中的值区分大小写，则也会影响对象名（表名、字段名等）也区分大小写；
   * sybase和sqlserver原理一样，所以这里将sybase和sqlserver都认为是区分大小写的来处理；
   * @return
   */
  public abstract boolean isFieldCaseSensitive();
  
  /**
   * 主版本号；
   * @return
   */
  public abstract int getDatabaseMajorVersion();
  /**
   * 副版本号；
   * @return
   */
  public abstract int getDatabaseMinorVersion() ;

  /**
   * 数据库完整的版本号；
   * @return
   */
  public abstract String getDatabaseProductVersion() ;

  /**
   * 完整的数据库名；
   * @return
   */
  public abstract String getDatabaseProductName() ;
  
  /**
   * 获取jdbc驱动的版本号；
   * @return
   */
  public abstract String getDriverVersion();
  
  /**
   * 获取jdbc驱动的主版本号；
   * @return
   */
  public abstract int getDriverMajorVersion();
  
  /**
   * 数据库表支持的最大字段数；
   * @return
   */
  public abstract int getMaxColumnsInTable();
  
  /**
   * 如果是oracle的话，此函数返回oracle对于的编码， 如：
   * US7ASCII  ZHS16GBK……
   * 其它数据库返回null
   */
  public abstract String getCharacterEncoding();
  

  /**
   * 获得数据库支持的字段名最大长度；
   * @return
   */
  public abstract int getMaxColumnNameLength();
  /**
   * 表名最大长度；
   * @return
   */
  public abstract int getMaxTableNameLength() ;
  /**
   * 索引名最大长度
   * @return
   */
  public abstract int getMaxIndexNameLength();
  
  /**
   * 一个中文字，在数据库的字符类型定义varchar(n)中的长度；
   * Mysql中varchar长度是按字节的，即：20长度，可以存20个汉字；
   * 其他数据库都是按字节存储的，这和字符集有关，一般gbk占2个长度，utf8占3个长度；
   * @return
   */
  public abstract int getNCharLength();
  
  /**
   * 20090902
   * 返回一个中文字符在数据库中的byte长度；
   * 通常gbk字符集占2个长度，utf8字符集占3个长度；
   * @return
   */
  public abstract int getNCharByteLength();

  /**
   * 获取一个用于测试数据库的sql；
   * @return
   */
  public abstract String getCheckSQL() ;
  
  /**
   * 获取数据库主键,唯一索引列组合最大长度；
   * 返回与字段定义长度单位一致的长度；
   * 之所以这样定义，是因为除了Mysql 其他数据库字符类型定义的长度都是字节长度，而mysql是字符长度；
   * @return
   *    可能返回-1, 表示没有限制，或者未知的；
   *    对于sqlserver就没有限制，返回-1
   */
  public abstract int getMaxKeyOfFieldsLength();
  
  /**
   * 返回一般索引列组合最大长度；
   * 返回-1表示无限制；
   * @return
   */
  public abstract int getMaxIndexOfFieldsLength();
  
  /**
   * 返回varchar类型字段 最大长度；
   * -1表示没有限制；
   * @return
   */
  public abstract int getMaxVarcharFieldLength();
  
  /**
   * 检查数据库jdbc属性，是否符合连接池的应用；
   * 比如：jdbc的版本
   * 返回警告提示信息；
   * @return
   */
  public abstract String check();
  
  
  /**
   * BI-6597 增加判断数据库对空字符串的处理的方法, 是否同NULL值等价
   * 默认兼容以前的处理, 返回为true
   * @return
   */
  public abstract boolean isEmptyStringEqualsNull();
  
  /**
   * 一个事务中批量提交的数据行数的限制
   * 默认返回 -1，表示无限制
   * @return int 事务中的最大数据行数
   */
  public abstract int getMaxRowsInTrans();
}
