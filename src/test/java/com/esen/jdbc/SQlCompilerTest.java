package com.esen.jdbc;


import com.esen.jdbc.sql.parser.*;

import junit.framework.TestCase;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SQlCompilerTest extends TestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SQlCompilerTest.class);
  }

  private static final String sqls[] = {
      "SELECT * FROM TableName",
      "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE WHERE SALARY >= 50000",
      "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE WHERE POSITION = \'Manager\'",
      "SELECT SUM(SALARY), AVG(SALARY) FROM EMPLOYEESTATISTICSTABLE",
      "SELECT TODAY() FROM XXX",
      "select * from fact_nsrxx t\r\n"
          + "where NOT (SWHZDJRQ > =\'09-10月-1999\')",
      "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE\r\n"
          + "WHERE POSITION = \'Manager\' AND (SALARY > 50000 OR BENEFIT > 10000)",
      "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE\r\n"
          + "WHERE SALARY BETWEEN 30000 AND 50000",
      "SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME\r\n"
          + "FROM ANTIQUES, ANTIQUEOWNERS\r\n" + "WHERE SELLERID = OWNERID\r\n"
          + "ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID",
      "SELECT xxb.B10 as xb10,\r\n"
          + "COUNT(b0.C3) as bcntc3 ,sum(b0.C3) as bc3,sum(b0.C4) as bc4,sum(b0.C5) as bc5,sum(b0.C6) as bc6,sum(b0.C7) as bc7,sum(b0.C8) as bc8 FROM irpt5test_B0 b0, irpt5test_XXB xxb WHERE (xxb.bbq_=b0.bbq_) and \r\n"
          + "(xxb.userid_=b0.userid_) and (xxb.B10 is not null) group by \r\n"
          + "xxb.B10,bc3\r\n" + " ",
      "select bbhid from (select bbhid, sum(b1->a1),sum(b1->a2) from b1 where bbq_ like \'2003%\' and sum(b1->a1)>0 and sum(b1->a2)>0)\r\n"
          + "\r\n"
          + "INTERSECT\r\n"
          + "\r\n"
          + "select bbhid from b1 where bbq_ = \'200401--\' and b1->a1>1000 and b1->a2>1000\r\n"
          + " ",
      "select * from `ireport50`.`ssxb_b0`\r\n"
          + "where userid_ in (select userid_ from `ireport50`.`irpt_log` )",
      "select bbq_,(c2+100) as aaa, (select count(*) from `ireport50`.`irpt_log`) as ddd from `ireport50`.`ssxb_b0`\r\n"
          + "where userid_ in (select userid_ from `ireport50`.`irpt_log` )\r\n"
          + "order by aaa,ddd\r\n" + "",
      "SELECT E_Name FROM Employees_Norway\r\n" + "UNION\r\n"
          + "SELECT E_Name FROM Employees_USA "+"UNION\r\n"
          + "SELECT C_Name FROM Employees_CHINA",
      "SELECT field1, field2, field3\r\n" + "FROM first_table\r\n"
          + "INNER JOIN second_table\r\n"
          + "ON first_table.keyfield = second_table.foreign_keyfield",
          "SELECT column_name FROM table_name\r\n" + 
          "WHERE column_name IN (value1,value2)"

  };

  public void testSQlCompilerString() {
    for (int i = 0; i < sqls.length; i++) {
      String sql = sqls[i];
      System.out.println(sql);
      System.out.println();
      try {
        SQlCompiler compiler = new SQlCompiler(sql);
        //System.out.println(compiler.compile().toString());
        SelectStateMent select=compiler.compile();
        SelectStateMent qs[]=select.getQuerys();
        if (qs.length>1) {
          System.out.println(select.toString());
        }
        System.out.println();

      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
  

}
