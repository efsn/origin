package com.esen.jdbc.dbdefiner;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTests {

  public static Test suite() {
    TestSuite suite = new TestSuite("Test for com.esen.jdbc.dbdefiner");
    //$JUnit-BEGIN$
    suite.addTestSuite(DbDefinerMssql2005Test.class);
    suite.addTestSuite(DbDefinerMysqlTest.class);
    suite.addTestSuite(DbDefinerMssqlTest.class);
    suite.addTestSuite(DbDefinerSybaseTest.class);
    suite.addTestSuite(DbDefinerOracleTest.class);
    suite.addTestSuite(DbDefinerDB2Test.class);
    //suite.addTestSuite(DbDefinerSybaseIQTest.class);
    //$JUnit-END$
    return suite;
  }

}
