package func.jdbc.test;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import oracle.jdbc.driver.OracleTypes;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.SimpleConnectionFactory;

import func.jdbc.FuncConnectionFactory;
import func.jdbc.FuncJdbc;
import func.jdbc.FuncSimpleConnectionFactory;
import func.jdbc.FuncSqlExecute;

public class TestCase4ConnectionFactory {
  public static void main(String[] args) throws Exception {
    TestCase4ConnectionFactory t = new TestCase4ConnectionFactory();
    //    t.createTable();
    t.testProcedure3();
  }

  private String getTableName() {
    return "test_connectionfactory";
  }

  private ConnectionFactory getConnectionFactory() {
    return FuncConnectionFactory.getOracleCustomConnectionFactory();
  }

  private String createInsertSql(String[] fields) {
    int len = fields.length;
    StringBuffer buf = new StringBuffer(len * 10);
    buf.append("insert into ").append(getTableName()).append("(");
    for (int i = 0; i < len; i++) {
      if (i != 0)
        buf.append(",");
      buf.append(fields[i]);
    }
    buf.append(") values(");
    for (int i = 0; i < len; i++) {
      if (i != 0)
        buf.append(",");
      buf.append("?");
    }
    buf.append(")");

    return buf.toString();
  }

  public void createTable() throws Exception {
    FuncJdbc.createOrRepairTable(this, getConnectionFactory(), getTableName(), "testcase-table-connectionfactory.xml");
  }

  private boolean canConnection(ConnectionFactory fct) throws Exception {
    if (fct == null)
      return false;
    try {
      Connection con = fct.getConnection();
      if (con != null)
        con.close();
      return true;
    }
    catch (Exception e) {
    }
    return false;
  }

  private void printCanConnection(ConnectionFactory fct) throws Exception {
    boolean b = canConnection(fct);
    String name = null;
    if (fct != null) {
      try {
        name = fct.getDbType().getDbName();
      }
      catch (Exception e) {
      }
    }
    if (b) {
      System.out.println(name + ":connect");
    }
    else {
      System.out.println(name + ":can not connect");
    }
  }

  /**
   * =================== 开始测试 ===================
   */

  public void testConnection() throws Exception {
    ConnectionFactory fct = getConnectionFactory();
    printCanConnection(fct);
  }

  public void testConnections() throws Exception {
    ConnectionFactory[] fcts = FuncConnectionFactory.getCustomConnectionFactoryArray();
    int len = fcts == null ? 0 : fcts.length;
    for (int i = 0; i < len; i++) {
      printCanConnection(fcts[i]);
    }
  }

  public void testInsert() throws Exception {
    String[] fields = new String[] { "ID_", "USER_" };
    String sql = createInsertSql(fields);
    FuncSqlExecute se = FuncSqlExecute.getInstance(getConnectionFactory());
    try {
      PreparedStatement ps = se.getPreparedStatement(sql);
      ps.setString(1, "1");
      //      ps.setString(2, null);
      ps.setNull(2, Types.NULL);
      ps.execute();
    }
    finally {
      se.close();
    }
  }

  public void testSelectInfo() {
    ConnectionFactory fct = getConnectionFactory();
    DataCopy dc = DataCopy.createInstance();
    dc.selectInto("", null, null, "abc");
    dc.selectInto("", "aaa", null, "abc");
  }

  public void testInsert2() throws Exception {
    String sql = "insert into  TEST_RPT(PARENTDIR_,FILENAME_,CREATETIME_)  (  (  (select  " + "'/c/cc/cc/',"
        + "'UJMYIIY3DUMTICDJUNIWULY4MLJTUVT4',ISFILE_," + "? from TEST_RPT where FILENAME_="
        + "'B50552' and  PARENTDIR_=" + "'/b/')  )  )  ";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    FuncSqlExecute se = FuncSqlExecute.getInstance(FuncConnectionFactory.getMysqlCustomConnectionFactory());
    try {
      PreparedStatement ps = se.getPreparedStatement(sql);
      int col = 1;
      //      ps.setString(col++, "/c/cc/cc/");
      //      ps.setString(col++, "UJMYIIY3DUMTICDJUNIWULY4MLJTUVT4");
      ps.setTimestamp(col++, time);
      //      ps.setTimestamp(col++, time);
      //      ps.setString(col++, "admin");
      //      ps.setString(col++, "admin");
      //      ps.setString(col++, "KFUQP3IM5MADI25F0BEKLIU2D6FMVVWL_20100106-021604");
      //      ps.setString(col++, "B50552");
      //      ps.setString(col++, "/b/");
      ps.execute();
    }
    finally {
      se.close();
    }
  }

  public void testInsert5() throws Exception {
    String sql = "insert into  TEST_RPT(PARENTDIR_,FILENAME_,CREATETIME_)  ((select '/c/','cc',? from TEST_RPT where FILENAME_='B50552' and  PARENTDIR_='/b/'))";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    FuncSqlExecute se = FuncSqlExecute.getInstance(FuncConnectionFactory.getMysqlCustomConnectionFactory());
    try {
      PreparedStatement ps = se.getPreparedStatement(sql);
      int col = 1;
      //      ps.setString(col++, "/c/cc/cc/");
      //      ps.setString(col++, "UJMYIIY3DUMTICDJUNIWULY4MLJTUVT4");
      ps.setTimestamp(col++, time);
      //      ps.setTimestamp(col++, time);
      //      ps.setString(col++, "admin");
      //      ps.setString(col++, "admin");
      //      ps.setString(col++, "KFUQP3IM5MADI25F0BEKLIU2D6FMVVWL_20100106-021604");
      //      ps.setString(col++, "B50552");
      //      ps.setString(col++, "/b/");
      ps.execute();
    }
    finally {
      se.close();
    }
  }

  public void testInsert3() throws Exception {
    String sql = "insert into  TEST_RPT_BRANCH(FILENAME_,CREATETIME_,BRANCHID_)  (select  ?,"
        + "?,'abc'  from TEST_RPT where FILENAME_=" + "?" + ")";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    FuncSqlExecute se = FuncSqlExecute.getInstance(FuncConnectionFactory.getMysqlCustomConnectionFactory());
    try {
      PreparedStatement ps = se.getPreparedStatement(sql);
      int col = 1;
      ps.setString(col++, "O3INT895WK474AW3KL1ZVZFKZT33RBYA");
      ps.setTimestamp(col++, time);
      ps.setString(col++, "O3INT895WK474AW3KL1ZVZFKZT33RBYA");
      ps.execute();
    }
    finally {
      se.close();
    }
  }

  public void testInsert4() throws Exception {
    String sql = "insert into  TEST_RPT_BRANCH(FILENAME_,CREATETIME_,BRANCHID_)  (select  ?,"
        + "?,'abc'  from TEST_RPT where FILENAME_=" + "?)  ";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    Connection con = FuncConnectionFactory.getOriCustomConnectionFactory("mysql");
    try {
      PreparedStatement ps = con.prepareStatement(sql);
      try {
        int col = 1;
        ps.setString(col++, "B82834");
        ps.setTimestamp(col++, time);
        ps.setString(col++, "B82834");
        ps.execute();
      }
      finally {
        ps.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testUpdate() throws Exception {
    String sql = "update TEST_VFS set PARENTDIR_= CONCAT(value( CONCAT(value(?,''),value(?,'')),''),value( SUBSTR(PARENTDIR_ , ( LENGTH(?)+ LENGTH(?))+ 1,( LENGTH(PARENTDIR_)- LENGTH(?))- LENGTH(?)),'')) where ( PARENTDIR_ like '/uu/src中文/%' )";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    SimpleConnectionFactory fct = FuncConnectionFactory.getDb2CustomConnectionFactory();
    Connection con = fct.getConnection();
    try {
      PreparedStatement ps = con.prepareStatement(sql);
      try {
        int col = 1;
        ps.setString(col++, "/tt/dest中/");
        ps.setString(col++, "src中文");
        ps.setString(col++, "/uu/");
        ps.setString(col++, "src中文");
        ps.setString(col++, "/uu/");
        ps.setString(col++, "src中文");
        ps.execute();
      }
      finally {
        ps.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testUpdate2() throws Exception {
    String sql = "select CONCAT( value(CONCAT(value('/tt/dest中/', ''), value('src中文', '')), ''), value( SUBSTR(PARENTDIR_, (LENGTH('/uu/') + LENGTH('src中文')) + 1, (LENGTH(PARENTDIR_) - LENGTH('/uu/')) - LENGTH('src中文')), '') ) from TEST_VFS where (PARENTDIR_ like '/uu/src中文/%')";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    SimpleConnectionFactory fct = FuncConnectionFactory.getDb2CustomConnectionFactory();
    Connection con = fct.getConnection();
    try {
      PreparedStatement ps = con.prepareStatement(sql);
      try {
        int col = 1;
        //        ps.setString(col++, "/tt/dest中/");
        //        ps.setString(col++, "src中文");
        //        ps.setString(col++, "/uu/");
        //        ps.setString(col++, "src中文");
        //        ps.setString(col++, "/uu/");
        //        ps.setString(col++, "src中文");
        ps.execute();
      }
      finally {
        ps.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testUpdate3() throws Exception {
    //        String sql = "select cast(? as timestamp) from TEST_VFS";

    SimpleConnectionFactory fct = FuncConnectionFactory.getDb2CustomConnectionFactory();
    String field = fct.getDialect().funcToSqlVar("?", Types.TIMESTAMP, Types.TIMESTAMP, null);
    String sql = "select " + field + " from TEST_VFS";
    sql = "select varchar(?) from TEST_VFS";
    sql = "select length(cast(? as varchar(1024))) from test_vfs";
    Timestamp time = new Timestamp(System.currentTimeMillis());
    Connection con = fct.getConnection();
    try {
      PreparedStatement ps = con.prepareStatement(sql);
      try {
        int col = 1;
        //        ps.setString(col++, "/tt/dest中/");
        //        ps.setString(col++, "src中文");
        //        ps.setString(col++, "/uu/");
        //        ps.setString(col++, "src中文");
        //        ps.setString(col++, "/uu/");
        ps.setString(col++, "src中文");
        //        ps.setTimestamp(col++, time);
        ResultSet rs = ps.executeQuery();
        try {
          while (rs.next()) {
            System.out.println(rs.getString(1));
          }
        }
        finally {
          rs.close();
        }
      }
      finally {
        ps.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testProcedure1() throws Exception {
    String sql = "{call output_date}";
    FuncConnectionFactory fcf = FuncConnectionFactory.getOracleInstance();
    String driver = fcf.getDriver();
    String url = fcf.getUrl(null, "test", null);
    String user = "test";
    String pw = "test";
    boolean debug = true;
    boolean asDefault = false;
    FuncSimpleConnectionFactory fct = FuncConnectionFactory.getConnectionFactory(driver, url, user, pw, debug,
        asDefault);
    Connection con = fct.getConnection();
    try {
      CallableStatement cs = con.prepareCall(sql);
      try {
        cs.execute();
      }
      finally {
        cs.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testProcedure2() throws Exception {
    String sql = "{call get_username('862',?)}";
    FuncConnectionFactory fcf = FuncConnectionFactory.getOracleInstance();
    String driver = fcf.getDriver();
    String url = fcf.getUrl(null, "test", null);
    String user = "test";
    String pw = "test";
    boolean debug = true;
    boolean asDefault = false;
    FuncSimpleConnectionFactory fct = FuncConnectionFactory.getConnectionFactory(driver, url, user, pw, debug,
        asDefault);
    Connection con = fct.getConnection();
    try {
      CallableStatement cs = con.prepareCall(sql);
      try {
        cs.registerOutParameter(1, Types.VARCHAR);
        cs.execute();
        String s = cs.getString(1);
        System.out.println(s);
      }
      finally {
        cs.close();
      }
    }
    finally {
      con.close();
    }
  }

  public void testProcedure3() throws Exception {
    String sql = "{call testc(?)}";
    FuncConnectionFactory fcf = FuncConnectionFactory.getOracleInstance();
    String driver = fcf.getDriver();
    String url = fcf.getUrl(null, "test", null);
    String user = "test";
    String pw = "test";
    boolean debug = true;
    boolean asDefault = false;
    FuncSimpleConnectionFactory fct = FuncConnectionFactory.getConnectionFactory(driver, url, user, pw, debug,
        asDefault);
    Connection con = fct.getConnection();
    try {
      CallableStatement cs = con.prepareCall(sql);
      try {
        cs.registerOutParameter(1, OracleTypes.CURSOR);
        cs.execute();
        ResultSet rs = (ResultSet) cs.getObject(1);
        try {
          while (rs.next()) {
            String s = rs.getString(1);
            System.out.println(s);
          }
        }
        finally {
          rs.close();
        }
      }
      finally {
        cs.close();
      }
    }
    finally {
      con.close();
    }
  }
}
