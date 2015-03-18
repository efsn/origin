package com.esen.exp;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;

public class FormatExpToSqlUtilForOracleTest extends FormatExpToSqlUtilTest {

  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "oracle.jdbc.driver.OracleDriver",
        "jdbc:oracle:thin:@localhost:1521:orcl",
        "test", "test","debug");
  }

  public void testToSql() {
    FormatExpToSqlUtil sqlutil = new FormatExpToSqlUtil(getDialect());
    sqlutil.setRemoveQuotesForConst(true);
    assertEquals(" SUBSTR(XXB.FIELD,1,2)", sqlutil.toSqlExp("left(xxb.field,2)"));
    assertEquals("XXB.FIELD like '12%'", sqlutil.toSqlExp("left(xxb.field,2)='12'"));
    assertEquals("FUN2(select field from tbname )", sqlutil.toSqlExp("fun2(\"select field from tbname \")"));
    assertEquals("FUN2(select field from tbname where field like 'aa%')", sqlutil.toSqlExp("fun2(\"select field from tbname where field like 'aa%'\")"));
    
    assertEquals(" CONCAT(#newParentDir#,#newFileName#)", sqlutil.toSqlExp("strcat('#newParentDir#','#newFileName#')"));
    assertEquals(" LENGTH(#parentDir#)", sqlutil.toSqlExp("len('#parentDir#')"));
    assertEquals(" LENGTH(PARENTDIR)- LENGTH(#parentDir#)", sqlutil.toSqlExp("len(PARENTDIR) - len('#parentDir#')"));
    assertEquals("SUBSTR(PARENTDIR_,LENGTH(PARENTDIR)+1,LENGTH(#parentDir#))", sqlutil.toSqlExp("mid('PARENTDIR_',length('PARENTDIR'),length('#parentDir#'))"));
    
 
  }

}
