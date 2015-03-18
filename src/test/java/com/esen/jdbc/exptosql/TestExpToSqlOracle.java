package com.esen.jdbc.exptosql;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.exp.Expression;

public class TestExpToSqlOracle extends TestExpToSql {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@192.168.1.100:1521:esenbi",
        "test", "test","debug");
  }
  public void testDateBbqExp() throws Exception {  
    createTable();
    addData(1);
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("DATE_='2007'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20080101','YYYYMMDD'))",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_='2007----'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20080101','YYYYMMDD'))",tosql.toSqlExp(exp));
    
 /*   exp = new Expression("DATE_='20070000'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20080101','YYYYMMDD'))",tosql.toSqlExp(exp));
*/    
    exp = new Expression("DATE_='200701'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20070201','YYYYMMDD'))",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_='2007-01'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20070201','YYYYMMDD'))",tosql.toSqlExp(exp));
   
    exp = new Expression("DATE_='200701--'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20070201','YYYYMMDD'))",tosql.toSqlExp(exp));
    
/*    exp = new Expression("DATE_='20070100'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20070201','YYYYMMDD'))",tosql.toSqlExp(exp));
    */
    exp = new Expression("DATE_='20070109'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070109','YYYYMMDD') and DATE_<to_date('20070110','YYYYMMDD'))",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_='2007-01-09'");
    exp.compile(ch);
    assertEquals("(DATE_>=to_date('20070109','YYYYMMDD') and DATE_<to_date('20070110','YYYYMMDD'))",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_ like '2007%'");
    exp.compile(ch);
    assertEquals("DATE_ between to_date('20070101 00:00:00','YYYYMMDD HH24:MI:SS') and to_date('20071231 23:59:59','YYYYMMDD HH24:MI:SS')",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_ like '200701*'");
    exp.compile(ch);
    assertEquals("DATE_ between to_date('20070101 00:00:00','YYYYMMDD HH24:MI:SS') and to_date('20070131 23:59:59','YYYYMMDD HH24:MI:SS')",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_ like '____03__'");
    exp.compile(ch);
    assertEquals("to_char(DATE_,'yyyymmdd') like '____03__'",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_='2005,2006----'");
    exp.compile(ch);
    assertEquals("((DATE_>=to_date('20050101','YYYYMMDD') and DATE_<to_date('20060101','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20060101','YYYYMMDD') and DATE_<to_date('20070101','YYYYMMDD')))",tosql.toSqlExp(exp));
    
    exp = new Expression("DATE_='2005,2006----,2007,200801,200802--,200803,20080519'");
    exp.compile(ch);
    assertEquals("((DATE_>=to_date('20050101','YYYYMMDD') and DATE_<to_date('20060101','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20060101','YYYYMMDD') and DATE_<to_date('20070101','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20070101','YYYYMMDD') and DATE_<to_date('20080101','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20080101','YYYYMMDD') and DATE_<to_date('20080201','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20080201','YYYYMMDD') and DATE_<to_date('20080301','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20080301','YYYYMMDD') and DATE_<to_date('20080401','YYYYMMDD')) " +
    		"or (DATE_>=to_date('20080519','YYYYMMDD') and DATE_<to_date('20080520','YYYYMMDD')))",tosql.toSqlExp(exp));
    
  }
}
