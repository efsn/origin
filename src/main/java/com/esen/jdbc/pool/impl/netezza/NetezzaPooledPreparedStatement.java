package com.esen.jdbc.pool.impl.netezza;

import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.pool.PooledConnection;
import com.esen.jdbc.pool.PooledPreparedStatement;

public class NetezzaPooledPreparedStatement extends PooledPreparedStatement {

	public NetezzaPooledPreparedStatement(PooledConnection conn,PreparedStatement pstat, String sql) {
		super(conn, pstat, sql);
//		conn.get_ds().setCharacterEncoding("utf8");
//		conn.get_ds().setDestCharSetEncoding("utf8");
	}

	public NetezzaPooledPreparedStatement(PooledConnection conn, Statement stat) {
		super(conn, stat);
//		conn.get_ds().setCharacterEncoding("utf8");
//		conn.get_ds().setDestCharSetEncoding("utf8");
	}

	public void setObject(int parameterIndex, Object x) throws SQLException {
		ParameterMetaData pmd = getParameterMetaData();
		if (pmd == null) {
			setObjectDefault(parameterIndex, x);
			return;
		}
		int t = pmd.getParameterType(parameterIndex);
		if (x == null) {
			setNull(parameterIndex, t);
			return;
		}
		if(t==-9){
//			System.out.println(x);
			setString(parameterIndex,x.toString());
			
		}else if(t==Types.TIME){
			/**
			 * BI-6454 恢复主题集备份包报错：ERROR: Bad time external representation '1970-01-01 08:45:02.0'
			 * Netezza数据库不允许将Timestamp类型的数据写入Time类型的列中，此处需要特殊处理下
			 * modify by baochl
			 * 2012.4.19
			 */
			this.setTime(parameterIndex, SqlFunc.toSqlTime(x));
		}else{
		
			super.setObject(parameterIndex, x);
		}
		
		
	}
	

}
