package com.esen.jdbc.sql;

import com.esen.db.sql.Field;
import com.esen.db.sql.SelectTable;
import com.esen.db.sql.rank.AccField;
import com.esen.db.sql.rank.DefaultRatioField;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.dialect.Dialect;

public class TestSelectTableMssql2005 extends TestSelectTable {
  public ConnectionFactory createConnectionFactory(){
    return new SimpleConnectionFactory(
        "net.sourceforge.jtds.jdbc.Driver",
        "jdbc:jtds:sqlserver://192.168.1.123:1433/irpt",
        "sa", "admin",true);
  }
  /**
   * mssql2005 不支持累计求和
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
    /*//strbbq_分组，按str_降序对zb的连续累计求和；
    AccField af = new AccField(dl,tag+".num_",new String[]{tag+".strbbq_"},tag+".str_",true,false,false,"acc1");
    st.addField(af);*/
    //strbbq_分组，求zb总和；
    AccField af2 = new AccField(dl,tag+".num_",new String[]{tag+".strbbq_"},null,true,false,false,"acc2");
    st.addField(af2);
    //按strbbq_,str_升序对zb连续累计求和；
    /*AccField af3 = new AccField(dl,tag+".num_",null,tag+".strbbq_,"+tag+".str_",false,false,false,"acc3");
    st.addField(af3);
    //求总和，相当于sum(zb)
    AccField af4 = new AccField(dl,tag+".num_",null,null,false,false,false,"acc4");
    st.addField(af4);*/
    //按strbbq_分组，求zb比重
    DefaultRatioField rf = new DefaultRatioField(tag+".num_",af2,"biz");
    st.addField(rf);
    
    String sql2 = st.getSql(getConnectionFactory().getDialect());
    assertEquals(true, excuteQuery(sql2));
  }
  /**
   * mssql2005 因为不支持累计求和，所以top百分比也不支持；
   */
  public void testTopPct() throws Exception{
    
  }
}
