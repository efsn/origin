package com.esen.jdbc.pool.impl.sybase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.pool.DataSourceImpl;
import com.esen.jdbc.pool.PooledConnection;

/**
   * 在12.5.1 ebf11522 版本有问题：
   * 事务开始时 调用 conn.setAutCommit(false);
   * 由于修改字段结构不支持事务，需要将其conn.setAutCommit(true);
   * setAutCommit()的实现掉用getAutoCommit()获取当前状态进行判断设置（不一致就设置）；
   * 这时conn.setAutCommit(true)此版本的Sybase报SET CHAINED command not allowed within multi-statement transaction.异常；
 * @author dw
 *
 */
public class SybasePooledConnection extends PooledConnection {
  private boolean autoCommit;
  
  public SybasePooledConnection(DataSourceImpl ds, Connection con,long connectionIndex) throws SQLException {
    super(ds, con, connectionIndex);
    autoCommit=con.getAutoCommit();
  }
  public boolean getAutoCommit() throws SQLException {
    checkActive();
    return autoCommit;
  }
  /*
   * 为了解决12.5.1 ebf11522 版本问题；
   * 此类定义一个中间变量记录事务状态，获取事务状态不用调用原连接对象con
   * @see com.esen.jdbc.pool.impl.PooledConnection#setAutoCommit(boolean)
   */
  public void setAutoCommit(boolean autoCommit2) throws SQLException {
    checkActive();
    //避免重复设置相同的值；
    if(autoCommit!=autoCommit2){
      conn.setAutoCommit(autoCommit2);
      autoCommit = autoCommit2;
    }
  }
}
