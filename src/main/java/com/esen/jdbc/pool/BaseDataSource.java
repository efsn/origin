package com.esen.jdbc.pool;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

import com.esen.jdbc.JdbcConnectionDebug;

/**
 * 此类的存在是为了兼容旧程序；
 * @author dw
 */
public class BaseDataSource extends DataSourceImpl {
  
	/**
	 * 数据库连接池调试记录活动着的连接
	 */
	private JdbcConnectionDebug debug = new JdbcConnectionDebug();
	
  public BaseDataSource(Properties props) {
    super();
    setProperties(props);
  }

  public BaseDataSource() {
    super();
  }
  
  public void close() throws SQLException{
    release();
    debug.clear();
  }
  
  /**
   * 重写父类的方法,  如果日志级次小于等于LOG_LEVER_WARN, 在活动连接调试中移除这个链接
   */
  protected void closeConnection(PooledConnection conn) throws SQLException {
  	if(isDebugStackLog()) { 
  		debug.remove(conn);
  	}
    super.closeConnection(conn);
  }

  /**
   * 重写父类的方法,  如果日志级次小于等于LOG_LEVER_WARN, 在活动连接调试中记录这个链接
   */
  public Connection getConnection() throws SQLException {
    Connection conn = super.getConnection();
    if(isDebugStackLog()) { 
    	debug.put(conn);
    }
    return conn;
  }
  
  public JdbcConnectionDebug getJdbcConnectionDebug() {
  	if(isDebugStackLog()) { 
  		return debug;
  	}
  	return null;
  }

}
