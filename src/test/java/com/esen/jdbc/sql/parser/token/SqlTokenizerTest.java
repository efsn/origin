package com.esen.jdbc.sql.parser.token;

import com.esen.jdbc.sql.parser.token.SqlTokenItem;
import com.esen.jdbc.sql.parser.token.SqlTokenizer;
import com.esen.jdbc.sql.parser.token.SqlTokenizerImpl;

import junit.framework.TestCase;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * @author daqun
 * @version 5.0
 */

public class SqlTokenizerTest extends TestCase {

  public static void main(String[] args) {
    junit.textui.TestRunner.run(SqlTokenizerTest.class);
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
          + "WHERE POSITION = \'Manager\' AND (SALARY > 50000 OR BENEFIT > 10000);",
      "SELECT EMPLOYEEIDNO FROM EMPLOYEESTATISTICSTABLE\r\n"
          + "WHERE SALARY BETWEEN 30000 AND 50000",
      "SELECT DISTINCT SELLERID, OWNERLASTNAME, OWNERFIRSTNAME\r\n"
          + "FROM ANTIQUES, ANTIQUEOWNERS\r\n" + "WHERE SELLERID = OWNERID\r\n"
          + "ORDER BY OWNERLASTNAME, OWNERFIRSTNAME, OWNERID",
      "SELECT xxb.B10 as xb10,\r\n"
          + "COUNT(b0.C3) as bcntc3 ,sum(b0.C3) as bc3,sum(b0.C4) as bc4,sum(b0.C5) as bc5,sum(b0.C6) as bc6,sum(b0.C7) as bc7,sum(b0.C8) as bc8 FROM irpt5test_B0 b0, irpt5test_XXB xxb WHERE (xxb.bbq_=b0.bbq_) and \r\n"
          + "(xxb.userid_=b0.userid_) and (xxb.B10 is not null) group by \r\n"
          + "xxb.B10\r\n" + " ",
      "SELECT OWN.OWNERLASTNAME Last Name, ORD.ITEMDESIRED Item Ordered\r\n"
          + "FROM ORDERS ORD, ANTIQUEOWNERS OWN\r\n"
          + "WHERE ORD.OWNERID = OWN.OWNERID\r\n"
          + "AND ORD.ITEMDESIRED IN (SELECT ITEM FROM ANTIQUES);",
      "INSERT INTO ANTIQUES VALUES (21, 01, \'Ottoman\', 200.00);",
      "select userid_ from (select * from b1 where b1.c4>1900) data",
      "SELECT OWNERID, \'is in both Orders & Antiques\' FROM ORDERS, ANTIQUES\r\n"
          + "WHERE OWNERID = BUYERID\r\n" + "UNION\r\n"
          + "SELECT BUYERID, \'is in Antiques only\' FROM ANTIQUES\r\n"
          + "WHERE BUYERID NOT IN (SELECT OWNERID FROM ORDERS);",
      "SELECT t1.id, name1 FROM t1 left join t2 on t1.id = t2.id where ISNULL(t2.id)"

  };

  public void testSqlStateMent() {
    for (int i = 0; i < sqls.length; i++) {
      String sql = sqls[i];
      System.out.println(sql);
      System.out.println();
      try {
        SqlTokenizer tokenizer = new SqlTokenizerImpl(sql);
        while (tokenizer.hasMoreTokens()) {
          SqlTokenItem item = tokenizer.nextToken();
          String prefix = null;
          if (item.isConst())
            prefix = "常量";
          else if (item.isVar())
            prefix = "变量";
          else if (item.isOp())
            prefix = "操作符";
          else if (item.isKey())
            prefix = "关键字";
          System.out.println(prefix + "  " + item.getItem());
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

}
