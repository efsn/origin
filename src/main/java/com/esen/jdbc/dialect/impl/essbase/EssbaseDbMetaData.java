package com.esen.jdbc.dialect.impl.essbase;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

public class EssbaseDbMetaData extends DbMetaDataImpl {

	public EssbaseDbMetaData(Connection con) {
		super(con);
	}

	public EssbaseDbMetaData(ConnectionFactory conf) {
		super(conf);
	}

	protected TableMetaData createTableMetaData(String tablename) {
		return new EssbaseTableMetaData(this, tablename);
	}

	protected ArrayList selectAllTableNames() {
		return getAllObjectName(new String[] { "CUBE" });
	}

	protected ArrayList selectAllViewNames() {
		/**
		 * BI-5546
		 * 外面很多地方调用都没有判断是否为空；
		 * 这里直接返回空list；
		 */
		return new ArrayList();
	}

	/**
	 * Essbase服务器如果要获取连接，总是获取一个新的；
	 * 原因是：olap2j驱动连接essbase，其是通过连接essbase provider service服务来实现的；
	 * 如果缓存连接，则有时会出现异常：
	 * 无法激活多维数据集"zdsy2/new2) x_"，因为它们在多维数据集"zdsy2/new2"上已经处于活动状态。请先清除活动的多维数据集，然后重试。 
	 */
	public Connection getConnection() throws SQLException {
		if (con != null)
			return con;
		return connectionFactory.getNewConnection();
	}

	protected ArrayList getAllObjectName(String[] objs){
	    try {
	      Connection conn = getConnection();
	      try {
	        DatabaseMetaData _dmd = conn.getMetaData();
	        ResultSet _rs = _dmd.getTables(null, null, null, objs);
	        ArrayList l = new ArrayList(32);
	        try {
	          while (_rs.next()) {
	        	String schema = _rs.getString(2);
	            String tbName = _rs.getString(3);
	            l.add(schema+"."+tbName);
	          }
	        }
	        finally {
	          if (_rs != null)
	            _rs.close();
	        }
	        return l;
	      }
	      finally {
	        closeConnection(conn);
	      }
	    }
	    catch (Exception e) {
	      throw new RuntimeException(e);
	    }
	  }
}
