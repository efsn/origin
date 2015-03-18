package com.esen.jdbc;

import com.esen.jdbc.dbdefiner.DbDefinerDB2Test;
import com.esen.jdbc.dbdefiner.DbDefinerMssql2005Test;
import com.esen.jdbc.dbdefiner.DbDefinerMssqlTest;
import com.esen.jdbc.dbdefiner.DbDefinerMysqlTest;
import com.esen.jdbc.dbdefiner.DbDefinerOracleTest;
import com.esen.jdbc.dbdefiner.DbDefinerSybaseTest;
import com.esen.jdbc.dbdefiner.DbDefinerTeradataTest;
import com.esen.jdbc.dialect.TestDialectDB2For9;
import com.esen.jdbc.dialect.TestDialectMySql;
import com.esen.jdbc.dialect.TestDialectOracle;
import com.esen.jdbc.dialect.TestDialectSqlServer;
import com.esen.jdbc.dialect.TestDialectSqlServer2005;
import com.esen.jdbc.dialect.TestDialectSybase;
import com.esen.jdbc.dialect.TestDialectSybaseIQ;
import com.esen.jdbc.dialect.TestDialectTeradata;

import junit.framework.Test;
import junit.framework.TestSuite;

public class JdbcAllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("TestAll for com.esen.jdbc");
    //$JUnit-BEGIN$
    //测试连接池
    suite.addTestSuite(TestDataSource.class);
    
    //测试DbDefiner
    suite.addTestSuite(DbDefinerMssql2005Test.class);
    suite.addTestSuite(DbDefinerMysqlTest.class);
    suite.addTestSuite(DbDefinerMssqlTest.class);
    suite.addTestSuite(DbDefinerSybaseTest.class);
    suite.addTestSuite(DbDefinerOracleTest.class);
    suite.addTestSuite(DbDefinerDB2Test.class);
    //suite.addTestSuite(DbDefinerSybaseIQTest.class);
    suite.addTestSuite(DbDefinerTeradataTest.class);
    
    //测试Dialect
    suite.addTestSuite(TestDialectDB2For9.class);
    suite.addTestSuite(TestDialectOracle.class);
    suite.addTestSuite(TestDialectMySql.class);
    suite.addTestSuite(TestDialectSqlServer.class);
    suite.addTestSuite(TestDialectSqlServer2005.class);
    suite.addTestSuite(TestDialectSybase.class);
    //suite.addTestSuite(TestDialectSybaseIQ.class);
    suite.addTestSuite(TestDialectTeradata.class);
    
    //$JUnit-END$
    return suite;
  }

}
