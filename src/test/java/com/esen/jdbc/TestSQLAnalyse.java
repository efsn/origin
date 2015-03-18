package com.esen.jdbc;

import com.esen.db.sql.analyse.SQLAnalyse;

import junit.framework.TestCase;

public class TestSQLAnalyse extends TestCase {
  
  public void test1(){
    String sql = "select * from testdb";
    SQLAnalyse sa = new SQLAnalyse(sql);
    assertEquals(sql, sa.getNoOrderBySQl());
    assertEquals(0,sa.getOrderBys().length);
    
    sql = "select * from testdb order by id_";
    sa = new SQLAnalyse(sql);
    assertEquals("select * from testdb", sa.getNoOrderBySQl());
    assertEquals(1,sa.getOrderBys().length);
    assertEquals("id_",sa.getOrderBys()[0].getFields()[0].getField());
    assertEquals(null,sa.getOrderBys()[0].getFields()[0].getTag());
    assertEquals(false,sa.getOrderBys()[0].getFields()[0].isDesc());
    
    sql = "select * from testdb a order by a.id_";
    sa = new SQLAnalyse(sql);
    assertEquals("select * from testdb a", sa.getNoOrderBySQl());
    assertEquals(1,sa.getOrderBys().length);
    assertEquals("id_",sa.getOrderBys()[0].getFields()[0].getField());
    assertEquals("a",sa.getOrderBys()[0].getFields()[0].getTag());
    assertEquals(false,sa.getOrderBys()[0].getFields()[0].isDesc());
    
    sql = "select * from testdb a,(select id_,name_ from testdb2 order by id_ desc) b order by a.id_";
    sa = new SQLAnalyse(sql);
    assertEquals("select * from testdb a,(select id_,name_ from testdb2  ) b", sa.getNoOrderBySQl());
    assertEquals(2,sa.getOrderBys().length);
    assertEquals("id_",sa.getOrderBys()[0].getFields()[0].getField());
    assertEquals(null,sa.getOrderBys()[0].getFields()[0].getTag());
    assertEquals(true,sa.getOrderBys()[0].getFields()[0].isDesc());
    
    assertEquals("id_",sa.getLastOrderBy().getFields()[0].getField());
    assertEquals("a",sa.getLastOrderBy().getFields()[0].getTag());
    assertEquals(false,sa.getLastOrderBy().getFields()[0].isDesc());
    
    sql = "select empnum, dept, salary,"+
" rank() over (partition by dept order by salary desc nulls last ) as rank,"+
" dense_rank() over (partition by dept order by salary desc nulls last) as denserank,"+
" row_number() over (partition by dept order by salary desc nulls last) as rownumber"+
" from emptab;";
    sa = new SQLAnalyse(sql);
    assertEquals(sql,sa.getNoOrderBySQl());
    
    sql = "select * from ("+
    " select swjg,hy,zb,"+
    " rank() over (partition by hy order by zb desc) sortn"+
    " from ("+
    " select SUBSTR(a.nsr_swjg_dm,0,7) swjg, SUBSTR(a.hy_dm, 0, 1) hy,sum(a.QBXSSR) zb"+
    " from fact_sbxx a"+
    " where (a.nsr_swjg_dm LIKE '137%')"+
    " group by SUBSTR(a.nsr_swjg_dm,0,7),SUBSTR(a.hy_dm, 0, 1)"+
    " ) order by hy"+
    " ) where sortn<6";
    
    sa = new SQLAnalyse(sql);
    String sql2 = "select * from ("+
    " select swjg,hy,zb,"+
    " rank() over (partition by hy order by zb desc) sortn"+
    " from ("+
    " select SUBSTR(a.nsr_swjg_dm,0,7) swjg, SUBSTR(a.hy_dm, 0, 1) hy,sum(a.QBXSSR) zb"+
    " from fact_sbxx a"+
    " where (a.nsr_swjg_dm LIKE '137%')"+
    " group by SUBSTR(a.nsr_swjg_dm,0,7),SUBSTR(a.hy_dm, 0, 1)"+
    " ) "+
    " ) where sortn<6";
    assertEquals(sql2,sa.getNoOrderBySQl());
    assertEquals(1,sa.getOrderBys().length);
    assertEquals("hy",sa.getOrderBys()[0].getFields()[0].getField());
  }
}
