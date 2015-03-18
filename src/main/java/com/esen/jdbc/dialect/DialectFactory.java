package com.esen.jdbc.dialect;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.db2.DB29Dialect;
import com.esen.jdbc.dialect.impl.db2.DB2AS400Dialect;
import com.esen.jdbc.dialect.impl.db2.DB2Dialect;
import com.esen.jdbc.dialect.impl.dm.DM7Dialect;
import com.esen.jdbc.dialect.impl.dm.DMDialect;
import com.esen.jdbc.dialect.impl.essbase.EssbaseDialect;
import com.esen.jdbc.dialect.impl.gbase.GBase8tDialect;
import com.esen.jdbc.dialect.impl.gbase.GBaseDialect;
import com.esen.jdbc.dialect.impl.greenplum.GreenplumDialect;
import com.esen.jdbc.dialect.impl.kingbasees.KingBaseESDialect;
import com.esen.jdbc.dialect.impl.mssql.Mssql2005Dialect;
import com.esen.jdbc.dialect.impl.mssql.MssqlDialect;
import com.esen.jdbc.dialect.impl.mysql.MysqlDialect;
import com.esen.jdbc.dialect.impl.netezza.NetezzaDialect;
import com.esen.jdbc.dialect.impl.oracle.Oracle817Dialect;
import com.esen.jdbc.dialect.impl.oracle.OracleDialect;
import com.esen.jdbc.dialect.impl.oscar.OscarDialect;
import com.esen.jdbc.dialect.impl.sql92.SQL92Dialect;
import com.esen.jdbc.dialect.impl.sybase.SybaseDialect;
import com.esen.jdbc.dialect.impl.sybase.SybaseIQDialect;
import com.esen.jdbc.dialect.impl.teradata.TeradataDialect;
import com.esen.jdbc.dialect.impl.timesten.TimesTenDialect;
import com.esen.jdbc.dialect.impl.vertica.VerticaDialect;

/**
 * 数据定义创建工厂接口
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */
public class DialectFactory {
  private Connection _conn;
  /**
   * @deprecated
   * @param con
   */
  private DialectFactory(Connection con){
    _conn = con;
  }
  /**
   * 兼容1.5版本的方法；
   * 主要用于ireport产品的使用；
   * @deprecated
   * @param con
   * @return
   */
  public static DialectFactory getInstance(Connection con){
    return new DialectFactory(con);
  }
  /**
   * @deprecated
   * @return
   */
  public Dialect createDialect() {
    DataBaseInfo db = DataBaseInfo.createInstance(_conn);
    return createDialect(_conn, db);
  }
  public static final Dialect createDialect(Connection con){
    DataBaseInfo db = DataBaseInfo.createInstance(con);
    return createDialect(con,db);
  }
  public static final Dialect createDialect(ConnectionFactory conf){
    return createDialect(conf,conf.getDbType());
  }
  
  private static Dialect createDialect(Object con_or_conf, DataBaseInfo db) {
    int dbtype = db.getDbtype();
    switch(dbtype){
      case SqlConst.DB_TYPE_ORACLE:
        if(db.isOracle8i())
          return  new Oracle817Dialect(con_or_conf);
        return new OracleDialect(con_or_conf);
      case SqlConst.DB_TYPE_DB2:
    	  if(db.isDB2ForAS400()){
    		  return new DB2AS400Dialect(con_or_conf);
    	  }
        if(db.getDatabaseMajorVersion()>=9)
          return new DB29Dialect(con_or_conf);
        return new DB2Dialect(con_or_conf);
      case SqlConst.DB_TYPE_MSSQL:
        if(db.isMssql2005())
          return new Mssql2005Dialect(con_or_conf);
        return new MssqlDialect(con_or_conf);
      case SqlConst.DB_TYPE_MYSQL:
        return new MysqlDialect(con_or_conf);
        
		case SqlConst.DB_TYPE_SYBASE:
			return new SybaseDialect(con_or_conf);

		case SqlConst.DB_TYPE_SYBASE_IQ:
			return new SybaseIQDialect(con_or_conf);

      case SqlConst.DB_TYPE_OSCAR:
        return new OscarDialect(con_or_conf);
      case SqlConst.DB_TYPE_TIMESTEN:
        return new TimesTenDialect(con_or_conf);
      case SqlConst.DB_TYPE_KINGBASE_ES:
        return new KingBaseESDialect(con_or_conf); 
      case SqlConst.DB_TYPE_DM:
      	if (db.isDM7()) {
      		return new DM7Dialect(con_or_conf);
      	}
    	return new DMDialect(con_or_conf);
      	
      case SqlConst.DB_TYPE_ESSBASE:
    	return new EssbaseDialect(con_or_conf);
      case SqlConst.DB_TYPE_GBASE:
        return new GBaseDialect(con_or_conf);
      case SqlConst.DB_TYPE_GBASE_8T:
          return new GBase8tDialect(con_or_conf);
      case SqlConst.DB_TYPE_NETEZZA:
    	  return new NetezzaDialect(con_or_conf);
      case SqlConst.DB_TYPE_GREENPLUM:
    	  return new GreenplumDialect(con_or_conf);
      case SqlConst.DB_TYPE_TERADATA:
    	  return new TeradataDialect(con_or_conf);
      case SqlConst.DB_TYPE_VERTICA:
    	  return new VerticaDialect(con_or_conf);
      case SqlConst.DB_TYPE_OTHER:
        default:
        return new SQL92Dialect(con_or_conf);
    }
  }

}
