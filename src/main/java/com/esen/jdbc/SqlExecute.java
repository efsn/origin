package com.esen.jdbc;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;

/**
 * 执行sql,此对象只能在同步线程中持有,此对象是非线程同步的
 * 用法：首先创建此对象，然后执行sql,最后必须关闭连接，如果要执行多个sql,则必须先关闭一次再调用
 *      如果执行连续的sql，则执行一条sql时，上面一条sql的执行结果将被关闭
 * 例：
 *  SqlExecute exe = null;
 *  try{
 *      ResultSet rs = exe.executeQuery("select * from tab");
 *      rs = exe.executeUpdate("update tab set id_='123'");
 *  }finally{
 *      exe.close();
 *  }
 *  
 *  2014.8.19  去掉变量conn,ps,rs,避免程序混乱
 *  by huling    
 * @author work
 */
public class SqlExecute {

	private static final Logger log = LoggerFactory.getLogger(SqlExecute.class);

	private ConnectionFactory fct;

	public static SqlExecute getInstance(ConnectionFactory fct) {
		return new SqlExecute(fct);
	}

	public SqlExecute(ConnectionFactory fct) {
		this.fct = fct;
	}

	public void setConnectionFactory(ConnectionFactory fct) {
		this.fct = fct;
	}

	public ConnectionFactory getConnectionFactory() {
		return this.fct;
	}

	public Connection getConnection() throws SQLException {
		return this.getConnectionFactory().getConnection();
	}


	public boolean isTableExist(String tablename) throws SQLException {
		return isTableExist(tablename, false);
	}

	public boolean isTableOrViewExist(String tablename) throws SQLException {
		return isTableExist(tablename, true);
	}

	private boolean isTableExist(String tablename, boolean containView) throws SQLException {
		if (StrFunc.isNull(tablename)) {
			return false;
		}
		Connection conn = this.getConnection();
		try {
			if (containView) {
				return this.getConnectionFactory().getDbDefiner().tableOrViewExists(conn, tablename);
			}
			else {
				return this.getConnectionFactory().getDbDefiner().tableExists(conn, null, tablename);
			}
		}finally{
			conn.close();
		}
	}

	public void removeAllData(String tablename) throws SQLException {
		this.executeUpdate("delete from " + tablename);
	}

	public void dropTable(String tablename) throws SQLException {
		this.executeUpdate("drop table " + tablename);
	}

	/**
	 * 执行sql
	 * @param sql
	 * @throws SQLException 
	 */
	public int executeUpdate(String sql) throws SQLException {
		int r = 0;
		Connection con = this.getConnection();
		try {
			con.setAutoCommit(false);
			Statement sm = con.createStatement();
			try {
				r = sm.executeUpdate(sql);
			} finally {
				sm.close();
			}
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			throw e;
		}finally{
			con.close();
		}
		return r;
	}

	/**
	 * 执行sql
	 * @param sql
	 * @param obj
	 * @throws SQLException 
	 */
	public int executeUpdate(String sql, Object[] obj) throws SQLException {
		int r = 0;
		Connection con = this.getConnection();
		try {
			con.setAutoCommit(false);
			PreparedStatement ps = con.prepareStatement(sql);
			try{
				int len = obj == null ? 0 : obj.length;
				for (int i = 0; i < len; i++) {
					ps.setObject(i + 1, obj[i]);
				}
				r = ps.executeUpdate();
			}finally{
				ps.close();
			}
			con.commit();
		} catch (SQLException e) {
			con.rollback();
			throw e;
		} finally {
			con.close();
		}
		return r;
	}

	/**
	 * 执行批处理
	 * @param sqls
	 * @throws SQLException 
	 */
	public int[] executeUpdateBatch(String[] sqls) throws SQLException {
		int[] rs = null;
		Connection con = this.getConnection();
		try {
			sqls = ArrayFunc.excludeNullStrs(sqls);
			if (sqls == null || sqls.length == 0)
				return rs;
			Statement sm = con.createStatement();
			try{
				con.setAutoCommit(false);
				try {
					for (int i = 0; i < sqls.length; i++) {
						sm.addBatch(sqls[i]);
					}
					rs = sm.executeBatch();
					con.commit();
				}
				catch (SQLException e) {
					con.rollback();
					throw e;
				}
			}finally{
				sm.close();
			}
		}finally{
			con.close();
		}
		return rs;
	}
}
