package func.jdbc;

import java.sql.*;
import java.util.ArrayList;

import com.esen.jdbc.ConnectionFactory;
import com.esen.util.*;

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
 * @author work
 */
public class FuncSqlExecute {
  private ConnectionFactory fct;

  private Connection con;

  private PreparedStatement ps;

  private Statement sm;

  private ResultSet rs;

  private Exception exception;

  public static FuncSqlExecute getInstance(ConnectionFactory fct) {
    return new FuncSqlExecute(fct);
  }

  public FuncSqlExecute(ConnectionFactory fct) {
    this.fct = fct;
  }

  public void setConnectionFactory(ConnectionFactory fct) {
    close();
    this.fct = fct;
  }

  public ConnectionFactory getConnectionFactory() {
    return this.fct;
  }

  private Connection getConnection() throws Exception {
    return this.getConnectionFactory().getConnection();
  }

  /**
   * 执行指定的sql,是否存在结果
   * @param sql
   * @return
   */
  public boolean hasResult(String sql) {
    try {
      return executeQuery(sql).next();
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    finally {
      this.close();
    }
    return false;
  }

  public boolean isTableExist(String tablename) {
    try {
      if (con == null)
        con = this.getConnection();
      return this.getConnectionFactory().getDbDefiner().tableExists(con, null, tablename);
    }
    catch (Exception e) {
      try {
        this.close();
      }
      catch (Exception ex) {
        ex.printStackTrace();
      }
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return false;
  }

  public void removeAllData(String tablename) {
    try {
      String sql = "delete from " + tablename;
      this.executeUpdate(sql);
    }
    finally {
      this.close();
    }
  }

  public void dropTable(String tablename) {
    try {
      String sql = "drop table " + tablename;
      this.executeUpdate(sql);
    }
    finally {
      this.close();
    }
  }

  /**
   * 执行sql,并返回执行结果
   * @param sql
   * @return
   */
  public ResultSet executeQuery(String sql) {
    this.closeStatement();
    this.closeResultSet();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      sm = con.createStatement();
      rs = sm.executeQuery(sql);
      return rs;
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return null;
  }

  /**
   * 执行sql,并返回执行结果
   * @param sql
   * @param obj
   * @return
   */
  public ResultSet executeQuery(String sql, Object[] obj) {
    this.closePreparesStatement();
    this.closeResultSet();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      ps = con.prepareStatement(sql);
      int len = obj == null ? 0 : obj.length;
      for (int i = 0; i < len; i++) {
        ps.setObject(i + 1, obj[i]);
      }
      rs = ps.executeQuery();
      return rs;
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return null;
  }

  /**
   * 执行sql
   * @param sql
   */
  public void executeUpdate(String sql) {
    this.closeStatement();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      try {
        con.setAutoCommit(false);
        sm = con.createStatement();
        sm.executeUpdate(sql);
        con.commit();
      }
      catch (Exception e) {
        con.rollback();
        throw e;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
  }

  /**
   * 执行sql
   * @param sql
   * @param obj
   */
  public void executeUpdate(String sql, Object[] obj) {
    this.closePreparesStatement();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      try {
        con.setAutoCommit(false);
        ps = con.prepareStatement(sql);
        int len = obj == null ? 0 : obj.length;
        for (int i = 0; i < len; i++) {
          ps.setObject(i + 1, obj[i]);
        }
        ps.executeUpdate();
        con.commit();
      }
      catch (Exception e) {
        con.rollback();
        throw e;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
  }

  /**
   * 返回PreparedStatement
   * @param sql
   * @return
   */
  public PreparedStatement getPreparedStatement(String sql) {
    this.closePreparesStatement();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      ps = con.prepareStatement(sql);
      return ps;
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return null;
  }

  /**
   * 执行sql,并返回影响的行数
   * @return
   */
  public int executeUpdatePreparedStatement() {
    if (ps == null)
      throw new RuntimeException("PreparedStatement 没有创建");
    try {
      con.setAutoCommit(false);
      try {
        int count = ps.executeUpdate();
        con.commit();
        return count;
      }
      catch (Exception e) {
        con.rollback();
        throw e;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return 0;
  }

  /**
   * 执行批处理，在外部加入批处理的sql
   */
  public void executeBatchPreparedStatement() {
    if (ps == null)
      throw new RuntimeException("PreparedStatement 没有创建");
    try {
      con.setAutoCommit(false);
      try {
        ps.executeBatch();
        con.commit();
      }
      catch (Exception e) {
        con.rollback();
        throw e;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
  }

  /**
   * 返回Statement
   * @return
   */
  public Statement getStatement() {
    this.closeStatement();
    this.rethrowException();
    try {
      if (con == null)
        con = this.getConnection();
      sm = con.createStatement();
      return sm;
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
    return null;
  }

  /**
   * 执行批处理
   * @param sqls
   */
  public void executeUpdateBatch(String[] sqls) {
    try {
      sqls = ArrayFunc.excludeNullStrs(sqls);
      if (sqls == null || sqls.length == 0)
        return;
      Statement sm = this.getStatement();
      con.setAutoCommit(false);
      try {
        for (int i = 0; i < sqls.length; i++) {
          sm.addBatch(sqls[i]);
        }
        sm.executeBatch();
        con.commit();
      }
      catch (Exception e) {
        con.rollback();
        throw e;
      }
    }
    catch (Exception e) {
      ExceptionHandler.rethrowRuntimeException(e);
    }
  }

  /**
   * 是否有异常
   * @return
   */
  public boolean hasException() {
    return getException() != null;
  }

  /**
   * 返回异常
   * @return
   */
  public Exception getException() {
    return exception;
  }

  /**
   * 关闭连接
   */
  public void close() {
    close(true);
  }

  /**
   * 关闭连接，如果closeConnection为false,则不关闭Connection
   * @param closeConnection
   */
  public void close(boolean closeConnection) {
    this.closeResultSet();
    this.closePreparesStatement();
    this.closeStatement();
    if (closeConnection) {
      this.closeConnection();
    }
    rethrowException();
  }

  /**
   * 关闭Connection
   */
  private void closeConnection() {
    if (con == null)
      return;
    try {
      con.close();
      con = null;
    }
    catch (Exception e) {
      addException(e, true);
    }
  }

  /**
   * 关闭PreparedStatement
   */
  private void closePreparesStatement() {
    if (ps == null)
      return;
    try {
      ps.close();
      ps = null;
    }
    catch (Exception e) {
      addException(e, true);
    }
  }

  /**
   * 关闭Statement
   */
  private void closeStatement() {
    if (sm == null)
      return;
    try {
      sm.close();
      sm = null;
    }
    catch (Exception e) {
      addException(e, true);
    }
  }

  /**
   * 关闭ResultSet
   */
  private void closeResultSet() {
    if (rs == null)
      return;
    try {
      rs.close();
      rs = null;
    }
    catch (Exception e) {
      addException(e, true);
    }
  }

  private void addException(Exception e, boolean print) {
    if (print)
      e.printStackTrace();
    addException(e);
  }

  private void addException(Exception e) {
    if (exception != null)
      return;
    exception = e;
  }

  private void rethrowException() {
    if (this.hasException())
      ExceptionHandler.rethrowRuntimeException(this.getException());
  }
}
