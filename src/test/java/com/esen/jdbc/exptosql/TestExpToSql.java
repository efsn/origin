package com.esen.jdbc.exptosql;

import com.esen.jdbc.FormatExpToSqlExp;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.jdbctest.TestJdbcInit;
import com.esen.util.exp.Expression;
import com.esen.util.exp.util.ExpSuperCompilerHelper;


public class TestExpToSql extends TestJdbcInit {
	
	public void testContactStrOp() throws Exception{
	    Dialect dl = getConnectionFactory().getDialect();
	    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
	    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
	    
	    Expression exp = new Expression("field^'A'");
	    exp.compile(ch);
	    assertEquals("FIELD||'A'",tosql.toSqlExp(exp));
	   
	    exp = new Expression("field^null");
	    exp.compile(ch);
	    assertEquals("FIELD||NULL",tosql.toSqlExp(exp));
	  }
  
	public void testFuncOpIN() throws Exception{
		this.createTable();
	    this.addData(10);
	    Dialect dl = getConnectionFactory().getDialect();
	    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
	    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
	    
	    Expression exp = new Expression("left(str_,1) in ['a','b']");
	    exp.compile(ch);
	    String sqlstr = tosql.toSqlExp(exp);
	    assertEquals(dl.funcLeft("STR_", "1")+" IN ('a','b')",sqlstr);
	    assertEquals(true,this.excuteQuery(this.getSql("str_")+" where "+sqlstr));
	}
	
  public void testIfNull() throws Exception{
    this.createTable();
    this.addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("ifnull(num_,0)");
    exp.compile(ch);
    String sqlstr = tosql.toSqlExp(exp);
    assertEquals(true,this.excuteQuery(this.getSql(sqlstr)));
    
    exp = new Expression("ifnull(str_,'')");
    exp.compile(ch);
    sqlstr = tosql.toSqlExp(exp);
    assertEquals(true,this.excuteQuery(this.getSql(sqlstr)));
    
    exp = new Expression("ifnull(date_,today())");
    exp.compile(ch);
    sqlstr = tosql.toSqlExp(exp);
    assertEquals(true,this.excuteQuery(this.getSql(sqlstr)));
  }
  
  public void testSeconds() throws Exception{
    this.createTable();
    this.addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("seconds(date_,timestamp_)");
    exp.compile(ch);
    String sqlstr = tosql.toSqlExp(exp);
    assertEquals(true,this.excuteQuery(this.getSql(sqlstr)));
    
    exp = new Expression("seconds(date_,null)");
    exp.compile(ch);
    sqlstr = tosql.toSqlExp(exp);
    assertEquals(true,this.excuteQuery(this.getSql(sqlstr)));
  }
  public void testLeftExp() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("left(field,1)='A'");
    exp.compile(ch);
    assertEquals("FIELD like 'A%'",tosql.toSqlExp(exp));
    
    exp = new Expression("'A'=left(field,1)");
    exp.compile(ch);
    assertEquals("FIELD like 'A%'",tosql.toSqlExp(exp));
  }
  
  public void testBbqMultConditions() throws Exception{

    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(null);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    //yyyymmdd
    Expression exp = new Expression("STRBBQ_='2007'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_ like '2007%'");
    exp.compile(ch);
    assertEquals("STRBBQ_  LIKE  '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='2007----'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='20070000'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='200701'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='2007-01'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='200701--'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='20070100'");
    exp.compile(ch);
    assertEquals("STRBBQ_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='20070101'");
    exp.compile(ch);
    assertEquals("STRBBQ_='20070101'",tosql.toSqlExp(exp));
    
    exp = new Expression("'20070101'>=STRBBQ_");
    exp.compile(ch);
    assertEquals("'20070101' >= STRBBQ_",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='2007-01-01'");
    exp.compile(ch);
    assertEquals("STRBBQ_='20070101'",tosql.toSqlExp(exp));
    
    exp = new Expression("STRBBQ_='2006,2007----,20080000,200801,200802--,20080300,20080501'");
    exp.compile(ch);
    assertEquals("(STRBBQ_ like '2006%' or STRBBQ_ like '2007%' " +
    		"or STRBBQ_ like '2008%' or STRBBQ_ like '200801%' " +
    		"or STRBBQ_ like '200802%' or STRBBQ_ like '200803%' " +
    		"or STRBBQ_='20080501')",tosql.toSqlExp(exp));
    
    //yyyymm
    exp = new Expression("CHARBBQYM='2007'");
    exp.compile(ch);
    assertEquals("CHARBBQYM like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM like '2007%'");
    exp.compile(ch);
    assertEquals("CHARBBQYM  LIKE  '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='2007----'");
    exp.compile(ch);
    assertEquals("CHARBBQYM like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='20070000'");
    exp.compile(ch);
    assertEquals("CHARBBQYM like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='200701'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("'200701'<CHARBBQYM");
    exp.compile(ch);
    assertEquals("'200701' < CHARBBQYM",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='2007-01'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='200701--'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='20070100'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='20070101'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='2007-01-01'");
    exp.compile(ch);
    assertEquals("CHARBBQYM='200701'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM='2006,2007----,20080000,200801,200802--,20080300,20080501'");
    exp.compile(ch);
    assertEquals("(CHARBBQYM like '2006%' or CHARBBQYM like '2007%' " +
    		"or CHARBBQYM like '2008%' or CHARBBQYM='200801' " +
    		"or CHARBBQYM='200802' or CHARBBQYM='200803' " +
    		"or CHARBBQYM='200805')",tosql.toSqlExp(exp));
    
    //yyyymm--
    exp = new Expression("CHARBBQYM_='2007'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_ like '2007%'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_  LIKE  '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='2007----'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='20070000'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='200701'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("'200701--'<=CHARBBQYM_");
    exp.compile(ch);
    assertEquals("'200701--' <= CHARBBQYM_",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='200701--'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='20070100'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='20070101'");
    exp.compile(ch);
    assertEquals("CHARBBQYM_ like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM_='2006,2007----,20080000,200801,200802--,20080300,20080501'");
    exp.compile(ch);
    assertEquals("(CHARBBQYM_ like '2006%' or CHARBBQYM_ like '2007%' " +
    		"or CHARBBQYM_ like '2008%' or CHARBBQYM_ like '200801%' " +
    		"or CHARBBQYM_ like '200802%' or CHARBBQYM_ like '200803%' " +
    		"or CHARBBQYM_ like '200805%')",tosql.toSqlExp(exp));
    
    //yyyymm00
    exp = new Expression("CHARBBQYM00='2007'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00 like '2007%'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00  LIKE  '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00='2007----'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00='20070000'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00='200701'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00='200701--'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00='20070100'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 like '200701%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQYM00>='20070100'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 >= '20070100'",tosql.toSqlExp(exp));
    exp = new Expression("CHARBBQYM00>='200701--'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 >= '20070100'",tosql.toSqlExp(exp));
    exp = new Expression("CHARBBQYM00>='200701'");
    exp.compile(ch);
    assertEquals("CHARBBQYM00 >= '20070100'",tosql.toSqlExp(exp));
    //yyyy
    exp = new Expression("CHARBBQY='2007'");
    exp.compile(ch);
    assertEquals("CHARBBQY='2007'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY='2007----'");
    exp.compile(ch);
    assertEquals("CHARBBQY='2007'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY='20070000'");
    exp.compile(ch);
    assertEquals("CHARBBQY='2007'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY='2006,2007----,20080000'");
    exp.compile(ch);
    assertEquals("(CHARBBQY='2006' or CHARBBQY='2007' or CHARBBQY='2008')",tosql.toSqlExp(exp));
    
    //yyyy----
    exp = new Expression("CHARBBQY_='2007'");
    exp.compile(ch);
    assertEquals("CHARBBQY_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY_='2007----'");
    exp.compile(ch);
    assertEquals("CHARBBQY_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("'2007----'>=CHARBBQY_");
    exp.compile(ch);
    assertEquals("'2007----' >= CHARBBQY_",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY_='20070000'");
    exp.compile(ch);
    assertEquals("CHARBBQY_ like '2007%'",tosql.toSqlExp(exp));
    
    exp = new Expression("CHARBBQY_='2006,2007----,20080000'");
    exp.compile(ch);
    assertEquals("(CHARBBQY_ like '2006%' or CHARBBQY_ like '2007%' " +
    		"or CHARBBQY_ like '2008%')",tosql.toSqlExp(exp));
    
    //数值型数据
    //yyyymmdd
    exp = new Expression("INTBBQYMD='2007'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070000 and INTBBQYMD<=20079999)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='2007----'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070000 and INTBBQYMD<=20079999)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='20070000'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070000 and INTBBQYMD<=20079999)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD like '2007%'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070000 and INTBBQYMD<20079999)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='200701'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070100 and INTBBQYMD<=20070199)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='2007-01'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070100 and INTBBQYMD<=20070199)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='200701--'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070100 and INTBBQYMD<=20070199)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='20070100'");
    exp.compile(ch);
    assertEquals("(INTBBQYMD>=20070100 and INTBBQYMD<=20070199)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='20070109'");
    exp.compile(ch);
    assertEquals("INTBBQYMD=20070109",tosql.toSqlExp(exp));
    
    exp = new Expression("'20070109'<=INTBBQYMD");
    exp.compile(ch);
    assertEquals("20070109 <= INTBBQYMD",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='2007-01-09'");
    exp.compile(ch);
    assertEquals("INTBBQYMD=20070109",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYMD='2006,2007----,20080000,200801,200802--,20080300,20080501'");
    exp.compile(ch);
    assertEquals("((INTBBQYMD>=20060000 and INTBBQYMD<=20069999) or" +
    		" (INTBBQYMD>=20070000 and INTBBQYMD<=20079999) or" +
    		" (INTBBQYMD>=20080000 and INTBBQYMD<=20089999) or " +
    		"(INTBBQYMD>=20080100 and INTBBQYMD<=20080199) or " +
    		"(INTBBQYMD>=20080200 and INTBBQYMD<=20080299) or " +
    		"(INTBBQYMD>=20080300 and INTBBQYMD<=20080399) or " +
    		"INTBBQYMD=20080501)",tosql.toSqlExp(exp));
    
    //yyyymm
    exp = new Expression("INTBBQYM='2007'");
    exp.compile(ch);
    assertEquals("(INTBBQYM>=200700 and INTBBQYM<=200799)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='2007----'");
    exp.compile(ch);
    assertEquals("(INTBBQYM>=200700 and INTBBQYM<=200799)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='20070000'");
    exp.compile(ch);
    assertEquals("(INTBBQYM>=200700 and INTBBQYM<=200799)",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='200701'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("'200701'<INTBBQYM");
    exp.compile(ch);
    assertEquals("200701 < INTBBQYM",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='2007-01'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='200701--'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='20070100'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='20070109'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='2007-01-09'");
    exp.compile(ch);
    assertEquals("INTBBQYM=200701",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQYM='2006,2007----,20080000,200801,200802--,20080300,20080501'");
    exp.compile(ch);
    assertEquals("((INTBBQYM>=200600 and INTBBQYM<=200699) " +
    		"or (INTBBQYM>=200700 and INTBBQYM<=200799) " +
    		"or (INTBBQYM>=200800 and INTBBQYM<=200899) " +
    		"or INTBBQYM=200801 or INTBBQYM=200802 " +
    		"or INTBBQYM=200803 or INTBBQYM=200805)",tosql.toSqlExp(exp));
    
    //yyyy
    exp = new Expression("INTBBQY='2007'");
    exp.compile(ch);
    assertEquals("INTBBQY=2007",tosql.toSqlExp(exp));
    
    exp = new Expression("2007<=INTBBQY");
    exp.compile(ch);
    assertEquals("2007 <= INTBBQY",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQY='2007----'");
    exp.compile(ch);
    assertEquals("INTBBQY=2007",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQY='20070000'");
    exp.compile(ch);
    assertEquals("INTBBQY=2007",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQY='200701'");
    exp.compile(ch);
    assertEquals("INTBBQY=2007",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQY='20070109'");
    exp.compile(ch);
    assertEquals("INTBBQY=2007",tosql.toSqlExp(exp));
    
    exp = new Expression("INTBBQY='2006,2007----,20080000'");
    exp.compile(ch);
    assertEquals("(INTBBQY=2006 or INTBBQY=2007 or INTBBQY=2008)",tosql.toSqlExp(exp));
    
  }
  //日期类型数据期
  public void testDateBbqExp() throws Exception {
    
  }
  /**
   * 测试多条件组合，包含xx like '%' 时括号丢失
   * 对XXB.HY LIKE '%'的优化处理不在这里测试；
   */
  public void testFormatExp(){
    FormatExpToSqlExp ft = new FormatExpToSqlExp(null);
    ExpSuperCompilerHelper h = new ExpSuperCompilerHelper();
    
    Expression exp = new Expression("xxb.hy like '%' and (aa>0|bb<0) and (cc>0|dd>0) ");
    exp.compile(h);
    String sqlstr = ft.toSqlExp(exp);
    assertEquals(sqlstr,"(XXB.HY LIKE '%') AND ((AA>0) OR (BB<0)) AND ((CC>0) OR (DD>0))");
    
    exp = new Expression("xxb.hy like '%' and (aa>0) and (cc>0|dd>0) ");
    exp.compile(h);
    sqlstr = ft.toSqlExp(exp);
    assertEquals(sqlstr,"(XXB.HY LIKE '%') AND (AA>0) AND ((CC>0) OR (DD>0))");
    
    exp = new Expression("(aa>0|bb<0) and xxb.hy like '%'  and (cc>0|dd>0) ");
    exp.compile(h);
    sqlstr = ft.toSqlExp(exp);
    assertEquals(sqlstr,"((AA>0) OR (BB<0)) AND (XXB.HY LIKE '%') AND ((CC>0) OR (DD>0))");
    
    exp = new Expression("(aa=0) and xxb.hy like '%'  and (cc>0|dd>0)");
    exp.compile(h);
    sqlstr = ft.toSqlExp(exp);
    assertEquals(sqlstr,"(AA=0) AND (XXB.HY LIKE '%') AND ((CC>0) OR (DD>0))");
    

  }
  public void test_AboutNullExp() throws Exception{
    Dialect dl = getConnectionFactory()==null?null:getConnectionFactory().getDialect();
    FormatExpToSqlExp tosql = new FormatExpToSqlExp(dl);
    ExpSuperCompilerHelper ch = new ExpSuperCompilerHelper();
    Expression exp = new Expression("null != a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("a != null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("null <> a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("a <> null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("null != a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("a != null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("null is not a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("a is not null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("null is a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NULL");
    
    exp = new Expression("a is null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NULL");
    
    exp = new Expression("null = a");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NULL");
    
    exp = new Expression("a = null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NULL");
    
    exp = new Expression("a = ''");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A=''");
    
    exp = new Expression("not a is null");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A IS NOT NULL");
    
    exp = new Expression("a = b");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A=B");
 
    exp = new Expression("a is b");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A=B");
    
    exp = new Expression("a is not b");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A<>B");
    
    exp = new Expression("a != b");
    exp.compile(ch);
    assertEquals(tosql.toSqlExp(exp),"A<>B");
    
  }
  /**
   * 测试int类型数据期
   * @throws Exception 
   */
  public void testIntDateExp() throws Exception{
    tryDateExp("INTBBQYMD");
    tryDateExp("INTBBQYM");
    tryDateExp("INTBBQY");
  }
  /**
   * 测试char类型数据期
   * @throws Exception 
   */
  public void testCharDateExp() throws Exception{
    tryDateExp("STRBBQ_");
    tryDateExp("CHARBBQYM");
    tryDateExp("CHARBBQYM_");
    tryDateExp("CHARBBQY");
    tryDateExp("CHARBBQY_");
  }
  /**
   * 测试date类型数据期
   * @throws Exception 
   */
  public void testDateExp() throws Exception{
    tryDateExp("DATE_");
  }
  /**
   * 测试timestamp类型数据期
   * @throws Exception 
   */
  public void testTimeStampDateExp() throws Exception{
    tryDateExp("TIMESTAMP_");
  }
  /**
   * 将比较符两边的类型转为一致（这里不包括日期类型），以数据库字段的类型为基准；
   * 比如： xxb.a2<>0 xxb.a2是字符 转换为：xxb.a2<>'0'
   *       xxb.a3>'3'  xxb.a3是数字 转换为： xxb.a3<>3
   * @throws Exception 
   */
  public void testConvertType() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("STR_<>0");
    exp.compile(ch);
    String sql = tosql.toSqlExp(exp);
    assertEquals("(STR_<>'0' or STR_ is null)",sql);
    
    exp = new Expression("STR_=0");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("STR_ = '0'",sql);
    
    exp = new Expression("STR_<0");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("STR_ < '0'",sql);
    
    exp = new Expression("'A'>=STR_");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("'A' >= STR_",sql);
    
    exp = new Expression("INT_>'2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("INT_ > 2",sql);
    
    exp = new Expression("INT_<='2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("INT_ <= 2",sql);
    
    exp = new Expression("'2'<=INT_");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("2 <= INT_",sql);
    
    exp = new Expression("INT_<>'2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("(INT_<>2 or INT_ is null)",sql);
    
    exp = new Expression("NUM_>'2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("NUM_ > 2",sql);
    
    exp = new Expression("NUM_<='2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("NUM_ <= 2",sql);
    
    exp = new Expression("NUM_!='2'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals("(NUM_<>2 or NUM_ is null)",sql);
  }
  
  /**
   * 除数为0，使用case when转换
   * @throws Exception 
   */
  public void testDiv() throws Exception{
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression("NUM_/INT_");
    exp.compile(ch);
    String sql = tosql.toSqlExp(exp);
    assertEquals("case when INT_=0 then null else NUM_/INT_ end ",sql);
  }
  
  private void tryDateExp(String field) throws Exception{
    createTable();
    addData(1);
    Dialect dl = getConnectionFactory().getDialect();
    TestFormatExpToSqlExp tosql = new TestFormatExpToSqlExp(dl);
    TestExpSuperCompilerHelper ch = new TestExpSuperCompilerHelper();
    
    Expression exp = new Expression(field+">#20070101#");
    exp.compile(ch);
    String sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+">'20070101'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+">20070101");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"=20070101");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"='20070101'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"=#20070101#");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<=20070101");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<='20070101'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<=#20070101#");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+">today()");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression("days("+field+",'20070201')>0");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression("days("+field+",#20070201#)>0");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    /*
     * (日期型,整型)无法与函数DAYS需要的参数列表相匹配！
函数DAYS需要的参数列表如下：
  日期型,日期型
  字符型,日期型
  日期型,字符型
  字符型,字符型
    exp = new Expression("days("+field+",20070201)>0"); 
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));*/
    
    exp = new Expression(field+" like '200701*'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+" like '2007*'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<>20070101");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<>'20070101'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+"<>#20070101#");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression(field+" like '????02??'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression("year("+field+")='2010'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression("month("+field+")='09'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
    exp = new Expression("day("+field+")='01'");
    exp.compile(ch);
    sql = tosql.toSqlExp(exp);
    assertEquals(true, excuteQuery(getSql(field,sql)));
    
  }
  private String getSql(String field, String sql) {
    
    return "select "+field+" from "+tablename+" where "+sql;
  }
}
