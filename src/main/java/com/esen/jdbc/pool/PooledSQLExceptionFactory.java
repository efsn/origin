package com.esen.jdbc.pool;

import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.pool.impl.db2.DB2PooledSQLException;
import com.esen.jdbc.pool.impl.dm.DMPooledSQLException;
import com.esen.jdbc.pool.impl.gbase.GBase8tPooledSQLException;
import com.esen.jdbc.pool.impl.gbase.GBasePooledSQLException;
import com.esen.jdbc.pool.impl.greenplum.GreenplumPooledSQLException;
import com.esen.jdbc.pool.impl.mssql.MssqlPooledSQLException;
import com.esen.jdbc.pool.impl.mysql.MysqlPooledSQLException;
import com.esen.jdbc.pool.impl.netezza.NetezzaPooledSQLException;
import com.esen.jdbc.pool.impl.oracle.OraclePooledSQLException;
import com.esen.jdbc.pool.impl.sybase.SybaseIQPooledSQLException;
import com.esen.jdbc.pool.impl.sybase.SybasePooledSQLException;
import com.esen.jdbc.pool.impl.teradata.TeradataPooledSQLException;
import com.esen.jdbc.pool.impl.vertica.VerticaPooledSQLException;

/**
 * 创建PooledSQLException类的工厂；
 * 对各个数据库实现统一的异常code
 *
 * @author dw
 */
public class PooledSQLExceptionFactory {
	public static PooledSQLException getInstance(DataBaseInfo db,SQLException ex){
		int dbtype = db.getDbtype();
		switch(dbtype){
			case SqlConst.DB_TYPE_ORACLE:{
				return new OraclePooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_MYSQL:{
				return new MysqlPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_DB2:{
				return new DB2PooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_MSSQL:{
				return new MssqlPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_SYBASE:{
				return new SybasePooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_SYBASE_IQ:{
				return new SybaseIQPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_GBASE:{
				return new GBasePooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_GBASE_8T:{
				return new GBase8tPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_DM:{
				return new DMPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_NETEZZA:{
				return new NetezzaPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_GREENPLUM:{
				return new GreenplumPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_TERADATA:{
				return new TeradataPooledSQLException(ex);
			}
			case SqlConst.DB_TYPE_VERTICA:{
				return new VerticaPooledSQLException(ex);
			}
			default:{
				return new PooledSQLException(ex);
			}
		}
	}
}
