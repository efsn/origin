package com.esen.jdbc.sql;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Types;

import com.esen.db.sql.BaseTable;
import com.esen.db.sql.Field;
import com.esen.db.sql.RealTable;
import com.esen.db.sql.SelectTable;
import com.esen.db.sql.SelectUnionTable;
import com.esen.db.sql.rank.AccField;
import com.esen.db.sql.rank.DefaultRatioField;
import com.esen.db.sql.rank.SortNField;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.jdbctest.TestJdbcInit;
import com.esen.util.StrFunc;

public class TestSelectTable extends TestJdbcInit {
  
  /**
   * BI-3276 出现别名重复的问题；
   * 原因：SelectTable嵌套构造sql时，如果一个SelectTable实例被多次join到新的SelectTable实例，就会出现别名重复问题；
   * 这是代码上的bug，一个st1实例join到另一个st2的实例，会给这个st1实例设置别名，每次都必须重新设置，因为st2中有自己的别名体系；
   * 原来的代码如果st1有别名，则使用自己的别名，加入到st2后，可能里面的SelectTable实例别名重复，执行时，在Oracle出现：
   * ORA-00918: 未明确定义列 异常；
   * @throws Exception
   */
  public void testSelectTableAlies() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    st.addField(new Field("int_","int_"));
    st.addField(new Field("str_","str_"));
    st.addCondition(tag+".date_<"+dl.funcToDate("20071101"));
    
    SelectTable st2 = new SelectTable();
    st2.addTable(tablename);
    st2.addField(new Field("num_","num_"));
    st2.addField(new Field("str_","str_"));
    
    //st2加入stx，设置了别名；
    SelectTable stx = new SelectTable();
    stx.addTable(st2);
    
    SelectTable st3 = new SelectTable();
    st3.addTable(st);
    //st2加入st3，应该重新设置别名；
    st3.addTable(st2);
    st2.setJoinType(BaseTable.LEFT_JOIN);
    st2.addJoinOnCondition(st.getAlias()+".str_="+st2.getAlias()+".str_");
    
    st3.addField(new Field(st.getAlias()+"."+st.getField(0).getAlias()));
    
    
    boolean f = excuteQuery(st3.getSql(getConnectionFactory().getDialect()));
    assertEquals(f, true);
    
  }

  /**
   * 测试时间类型条件
   * @throws Exception 
   */
  public void testDateCondition() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    st.addField(new Field("int_","int_"));
    st.addCondition(tag+".date_<"+dl.funcToDate("20071101"));
    String datetime = StrFunc.formatDateTime(System.currentTimeMillis());
    st.addCondition(tag+".time_<"+dl.funcToDateTime(datetime));
    boolean f = excuteQuery(st.getSql(getConnectionFactory().getDialect()));
    assertEquals(f, true);
  }
  
  /**
   * 测试left join,right join
   * @throws Exception 
   */
  public void testJoin() throws Exception{
    createTable();
    addData(10);
    SelectTable st = getJoinTable(BaseTable.LEFT_JOIN);
    String sql = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
    
    st = getJoinTable(BaseTable.RIGHT_JOIN);
    sql = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
    
    st = getJoinTable(BaseTable.INNER_JOIN);
    sql = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
  }

  private SelectTable getJoinTable(int jointype) {
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".num_","zb1");
    st.addField(fd1);
    RealTable rt = new RealTable(tablename,st.getAutoTag());
    st.addTable(rt);
    rt.addJoinOnCondition(tag+".int_="+rt.getAlias()+".int_");
    rt.setJoinType(jointype);
    Field fd2 = new Field(rt.getAlias()+".num_","zb2");
    st.addField(fd2);
    return st;
  }
  /**
   * 测试full join ,Oralce 用union
   * @throws Exception 
   */
  public void testFulljoin() throws Exception{
    createTable();
    addData(10);
    SelectTable st = getJoinTable(BaseTable.FULL_JOIN);
    String sql = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
    
    
  }
  
  /**
   * 测试Union语法，各种数据类型都要用到，特别是日期类型对空值的处理；
   * @throws Exception 
   */
  public void testUnion() throws Exception{
    createTable();
    addData(10);
    
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".str_","zb1");
    st.addField(fd1);
    Field fd2 = new Field(tag+".num_","zb2");
    st.addField(fd2);
    Field fd3 = new Field(tag+".date_","zb3");
    st.addField(fd3);
    Field fd4 = new Field(tag+".timestamp_","zb4");
    st.addField(fd4);
    Field fd5 = new Field(tag+".strbbq_","zb5");
    st.addField(fd5);
    
    Dialect dl = getConnectionFactory().getDialect();
    SelectTable st2 = new SelectTable();
    st2.addTable(tablename);
    Field f1 = new Field(null,"zb1");
    st2.addField(f1);
    Field f2 = new Field("0","zb2");
    st2.addField(f2);
    Field f3 = new Field(dl.funcToSqlConst(null, Types.DATE),"zb3");
    st2.addField(f3);
    Field f4 = new Field(dl.funcToSqlConst(null, Types.TIMESTAMP),"zb4");
    st2.addField(f4);
    Field f5 = new Field(dl.funcToSqlConst(null, Types.VARCHAR),"zb5");
    st2.addField(f5);
    
    SelectTable[] sts = new SelectTable[]{st,st2};
    SelectUnionTable ut = new SelectUnionTable(sts);
    String sql = ut.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
  }
  
  /**
   * 测试排名sql
   排名：
   select empnum, dept, salary,
 rank() over (partition by dept order by salary desc nulls last) as rank,
 dense_rank() over (partition by dept order by salary desc nulls last)as denserank,
 row_number() over (partition by dept order by salary desc nulls last)as rownumber
from emptab;

 EMPNUM  DEPT SALARY  RANK  DENSERANK   ROWNUMBER
 ------  ---- ------  ----  ---------   ---------
 6       1    78000   1     1           1
 2       1    75000   2     2           2
 7       1    75000   2     2           3
 11      1    53000   4     3           4
 5       1    52000   5     4           5
 1       1    50000   6     5           6
--------------------------------------------------
 9       2    51000   1     1           1
 4       2       -    2     2           2 
 
   * 
   * @throws Exception 
   */
  public void testPM() throws Exception{
    createTable();
    addData(10);
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".strbbq_","strbbq");
    st.addField(fd1);
    Field fd2 = new Field(tag+".str_","str");
    st.addField(fd2);
    Field zb =  new Field("sum("+tag+".num_)","zb");
    st.addField(zb);
    Dialect dl = getConnectionFactory().getDialect();
    Field sortzb = new SortNField(dl,SortNField.SORT_RANK,
        new String[]{tag+".strbbq_"},"sum("+tag+".num_)",0,"sortn");
    st.addField(sortzb);
    Field sortzb2 = new SortNField(dl,SortNField.SORT_ROW_NUMBER,
        new String[]{tag+".strbbq_"},"sum("+tag+".num_)",0,"sortn2");
    st.addField(sortzb2);
    Field sortzb3 = new SortNField(dl,SortNField.SORT_DENSE_RANK,
        new String[]{tag+".strbbq_"},"sum("+tag+".num_)",0,"sortn3");
    st.addField(sortzb3);
    st.addGroupBy(tag+".strbbq_");
    st.addGroupBy(tag+".str_");
    st.addCondition(tag+".strbbq_ is not null");
    st.addOrderByField(dl, tag+".strbbq_,sortn", false, true, true);
    
    String sql = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
    
    SelectTable st2 = new SelectTable();
    st2.addTable(tablename);
    tag = st2.getRealTable(tablename).getAlias();
    Field f1 = new Field(tag+".strbbq_","strbbq");
    st2.addField(f1);
    Field f2 = new Field(tag+".str_","str");
    st2.addField(f2);
    Field zb2 =  new Field("sum("+tag+".num_)","zb");
    st2.addField(zb2);
    Field sortzb4 = new SortNField(dl,SortNField.SORT_RANK,
        null,"sum("+tag+".num_)",0,"sortn");
    st2.addField(sortzb4);
    Field sortzb5 = new SortNField(dl,SortNField.SORT_ROW_NUMBER,
        null,"sum("+tag+".num_)",0,"sortn2");
    st2.addField(sortzb5);
    Field sortzb6 = new SortNField(dl,SortNField.SORT_DENSE_RANK,
        null,"sum("+tag+".num_)",0,"sortn3");
    st2.addField(sortzb6);
    st2.addGroupBy(tag+".strbbq_");
    st2.addGroupBy(tag+".str_");
    st2.addCondition(tag+".strbbq_ is not null");
    st2.addOrderByField(dl, tag+".strbbq_,sortn", false, true, true);
    
    String sql2 = st2.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql2));
  }
  
  /**
   * 测试比重sql
   * 比重：
   * select empnum, dept, salary, 
         sum(salary) over (partition by dept) as deptsum,
        salary/sum(salary) over(partition by dept)as percentage
  from emptab; 
   * @throws Exception 
   */
  public void testBiz() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".strbbq_","strbbq");
    st.addField(fd1);
    Field fd2 = new Field(tag+".str_","str");
    st.addField(fd2);
    Field zb =  new Field(tag+".num_","zb");
    st.addField(zb);
    //strbbq_分组，按str_降序对zb的连续累计求和；
    AccField af = new AccField(dl,tag+".num_",new String[]{tag+".strbbq_"},tag+".str_",true,false,false,"acc1");
    st.addField(af);
    //strbbq_分组，求zb总和；
    AccField af2 = new AccField(dl,tag+".num_",new String[]{tag+".strbbq_"},null,true,false,false,"acc2");
    st.addField(af2);
    //按strbbq_,str_升序对zb连续累计求和；
    AccField af3 = new AccField(dl,tag+".num_",null,tag+".strbbq_,"+tag+".str_",false,false,false,"acc3");
    st.addField(af3);
    //求总和，相当于sum(zb)
    AccField af4 = new AccField(dl,tag+".num_",null,null,false,false,false,"acc4");
    st.addField(af4);
    //按strbbq_分组，求zb比重
    DefaultRatioField rf = new DefaultRatioField(tag+".num_",af2,"biz");
    st.addField(rf);
    
    String sql2 = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql2));
  }
  /**
   * 测试top n
   * db2,oracle,sybaseiq 支持sql99标准
   * 其他用sql92标准
   * @throws Exception 
   */
  public void testTopN() throws Exception{
    createTable();
    addData(10);
    sql99topn();
  }
  /**
   * sql99标准 top n

   select strbbq,str,zb
   from (
   select a.strbbq_ as strbbq , a.str_ as str,sum(a.num_) as zb,
        row_number() over (partition by a.strbbq_ order by sum(a.num_) desc) as sortn
   from fact_sbxx a
   where a.strbbq_ is not null
   group by a.strbbq_,a.str_
   order by a.strbbq_,sortn
   ) where sortn<6
   
   select QBXSSR as QBXSSR,HY_DM as HY_DM,ID as ID 
from ( 
select sum(a.QBXSSR) as QBXSSR, SUBSTR(a.HY_DM,1,1) as HY_DM,a.NSRDZDAH as ID,
row_number() over (partition by SUBSTR(a.HY_DM,1,1) order by sum(a.QBXSSR) desc nulls last) as ID_sortn_sortn 
from HZ_M_SBXX a 
group by SUBSTR(a.HY_DM,1,1),a.NSRDZDAH 
order by SUBSTR(a.HY_DM,1,1) nulls first,ID_sortn_sortn nulls first) a 
where (ID_sortn_sortn<=5)) 
   * @throws Exception 
   */
  protected void sql99topn() throws Exception{
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".strbbq_","strbbq");
    st.addField(fd1);
    Field fd2 = new Field(tag+".str_","str");
    st.addField(fd2);
    Field zb =  new Field("sum("+tag+".num_)","zb");
    st.addField(zb);
    Dialect dl = getConnectionFactory().getDialect();
    Field sortzb = new SortNField(dl,SortNField.SORT_RANK,
        new String[]{tag+".strbbq_"},"sum("+tag+".num_)",0,"sortn");
    st.addField(sortzb);
    st.addGroupBy(tag+".strbbq_");
    st.addGroupBy(tag+".str_");
    st.addCondition(tag+".strbbq_ is not null");
    //这里不需要排序，rank已经排过了，而且mssql2005上再排序会出sql错误；
    //st.addOrderByField(dl, tag+".strbbq_,sortn", false, false, false);
    
    SelectTable topn = new SelectTable();
    topn.addTable(st);
    for(int i=0;i<st.getFieldCount();i++){
      Field f = st.getField(i);
      Field fi = new Field(f.getAlias(),f.getAlias());
      topn.addField(fi);
    }
    topn.addCondition("sortn<6");
    topn.addOrderByField(dl, st.getAlias()+".strbbq,sortn", false, false, false);
    String sql = topn.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
  }
  /**
   * sql92标准 top n
   */
  protected void sql92topn(){
    
  }
  /**
   * 测试top 百分比
   * 有些数据库(sybase,mysql)不支持；
   * 
select QBXSSR as QBXSSR,HY_DM as HY_DM,NSRDZDAH as NSRDZDAH
from (
select QBXSSR as QBXSSR,HY_DM as HY_DM,NSRDZDAH as NSRDZDAH,
case when sum(QBXSSR) over (partition by HY_DM)=0 then null else sum(QBXSSR) over (partition by HY_DM order by QBXSSR desc  nulls last)/sum(QBXSSR) over (partition by HY_DM) end as totalpct3,
case when sum(QBXSSR) over (partition by HY_DM)=0 then null else (sum(QBXSSR) over (partition by HY_DM order by QBXSSR desc  nulls last)-QBXSSR)/sum(QBXSSR) over (partition by HY_DM) end as totalpct4
from (
select sum(a.QBXSSR) as QBXSSR, SUBSTR(a.HY_DM,1,1) as HY_DM,a.NSRDZDAH as NSRDZDAH
from FACT_SBXX a
group by  SUBSTR(a.HY_DM,1,1),a.NSRDZDAH) abab) ab
where (totalpct3<=0.2 or (totalpct3>0.2 and totalpct4=0))
   * @throws Exception 
   
   */
  public void testTopPct() throws Exception{
    createTable();
    addData(10);
    SelectTable st = new SelectTable();
    st.addTable(tablename);
    String tag = st.getRealTable(tablename).getAlias();
    Field fd1 = new Field(tag+".strbbq_","strbbq");
    st.addField(fd1);
    Field fd2 = new Field(tag+".str_","str");
    st.addField(fd2);
    Field zb =  new Field("sum("+tag+".num_)","zb");
    st.addField(zb);
    st.addGroupBy(tag+".strbbq_");
    st.addGroupBy(tag+".str_");
    st.addCondition(tag+".strbbq_ is not null");
    
    SelectTable sortst = new SelectTable();
    sortst.addTable(st);
    for(int i=0;i<st.getFieldCount();i++){
      Field f = st.getField(i);
      Field fi = new Field(f.getAlias(),f.getAlias());
      sortst.addField(fi);
    }
    String fstr = "case when sum(zb) over (partition by strbbq)=0 then null else sum(zb) over (partition by strbbq order by zb desc )/sum(zb) over (partition by strbbq) end";
    Field fp = new Field(fstr,"totalpct3");
    sortst.addField(fp);
    String fstr2 = "case when sum(zb) over (partition by strbbq)=0 then null else (sum(zb) over (partition by strbbq order by zb desc )-zb)/sum(zb) over (partition by strbbq) end";  
    Field fp2 = new Field(fstr2,"totalpct4");
    sortst.addField(fp2);
    
    SelectTable topst = new SelectTable();
    topst.addTable(sortst);
    for(int i=0;i<3;i++){
      Field f = sortst.getField(i);
      Field fi = new Field(f.getAlias(),f.getAlias());
      topst.addField(fi);
    }
    topst.addCondition("totalpct3<=0.2 or (totalpct3>0.2 and totalpct4=0)");
    
    String sql = topst.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql));
  }
  
  /**
   * 测试分页
   * @throws Exception 
   */
  public void testLimitPage() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    String sql = "select int_ from "+tablename;
    String sql1 = dl.getLimitString(sql, 0, 2);
    String sql2 = dl.getLimitString(sql, 2, 2);
    Connection conn =  getConnectionFactory().getConnection();
    try{
      Statement stat1 = conn.createStatement();
      ResultSet rs1 = stat1.executeQuery(sql1);
      int[] v1 = SqlFunc.readIntArray(rs1, 1);
      assertEquals(2000,v1[0]);
      assertEquals(2001,v1[1]);
      stat1.close();
      
      Statement stat2 = conn.createStatement();
      ResultSet rs2 = stat2.executeQuery(sql2);
      int[] v2 = SqlFunc.readIntArray(rs2, 1);
      assertEquals(2002,v2[0]);
      assertEquals(2003,v2[1]);
      stat2.close();
      
      PreparedStatement pstat1 = conn.prepareStatement(sql1);
      rs1 = pstat1.executeQuery();
      v1 = SqlFunc.readIntArray(rs1, 1);
      assertEquals(2000,v1[0]);
      assertEquals(2001,v1[1]);
      pstat1.close();
      
      PreparedStatement pstat2 = conn.prepareStatement(sql2);
      rs2 = pstat2.executeQuery();
      v2 = SqlFunc.readIntArray(rs2, 1);
      assertEquals(2002,v2[0]);
      assertEquals(2003,v2[1]);
      pstat2.close();
      
    }finally{
      conn.close();
    }
    
  }
  private int getCount(String sql) throws Exception{
    Connection conn = getConnectionFactory().getConnection();
    try{
      Statement stat = conn.createStatement();
      try{
        ResultSet rs = stat.executeQuery("select count(*) from ("+sql+")");
        rs.next();
        return rs.getInt(1);
      }finally{
        stat.close();
      }
    }finally{
      conn.close();
    }
  }
  /**
   * 测试计数sql
   * @throws Exception 
   */
  public void testCountSql() throws Exception{
    createTable();
    addData(10);
    Dialect dl = getConnectionFactory().getDialect();
    String sql = "select * from "+tablename;
    sql = dl.getCountString(sql);
    assertEquals(true, excuteQuery(sql));
  }
}
