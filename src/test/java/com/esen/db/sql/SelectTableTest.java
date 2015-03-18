package com.esen.db.sql;

import com.esen.db.sql.BaseTable;
import com.esen.db.sql.Field;
import com.esen.db.sql.RealTable;
import com.esen.db.sql.SelectTable;

import junit.framework.TestCase;

public class SelectTableTest extends TestCase {

  public void testGetTable(){
    SelectTable stav = new SelectTable();
    stav.addField(new Field("avg(c.zb1)","avgqb"));
    stav.addField(new Field("SUBSTR(c.HY, 0, 1)","bhy",Field.FTYPE_DIM));
    stav.addTable(new RealTable("test_b0","c"));
    stav.addCondition("c.PID_ LIKE '14201%'");
    stav.addCondition("c.bbq_ = '200502--' or c.bbq_ = '200503--'");
    stav.addGroupBy("SUBSTR(c.HY, 0, 1)");
    stav.addOrderByField(null,"SUBSTR(c.HY, 0, 1)",false,false,true);
    
    SelectTable st2 = new SelectTable();
    st2.addField(new Field("sum(a.zb1)","zb"));
    st2.addField(new Field("sum(abs(a.zb1 - b.avgqb)) / count(*)","zba"));
    st2.addField(new Field("SUBSTR(a.HY, 0, 1)","hy",Field.FTYPE_DIM));
    st2.addTable(new RealTable("test_b0","a"));
    stav.setAlias("b");
    st2.addTable(stav);
    stav.addJoinOnCondition("SUBSTR(a.HY, 0, 1) = b.bhy");
    st2.addCondition("a.PID_ LIKE '14201%'");
    st2.addCondition("a.bbq_ = '200502--' or a.bbq_ = '200503--'");
    st2.addGroupBy("SUBSTR(a.HY, 0, 1)");
    st2.addOrderByField(null,"SUBSTR(a.HY, 0, 1)",false,false,true);
    
    SelectTable st3 = new SelectTable();
    st3.addField(new Field("sum(nvl(d.zb1, 0))","zb"));
    st3.addField(new Field("SUBSTR(d.HY, 0, 1)","hy"));
    st3.addTable(new RealTable("test_b0","d"));
    st3.addCondition("d.PID_ LIKE '14201%'");
    st3.addCondition("d.bbq_ = '200501--' or d.bbq_ = '200502--'");
    st3.addGroupBy("SUBSTR(d.HY, 0, 1)");
    st3.addOrderByField(null,"SUBSTR(d.HY, 0, 1)",false,false,true);
    
    SelectTable st = new SelectTable();
    st.addField(new Field("bb.zb"));
    st.addField(new Field("aa.zb"));
    st.addField(new Field("aa.hy"));
    st2.setAlias("aa");
    st.addTable(st2);
    st3.setAlias("bb");
    st3.setJoinType(BaseTable.LEFT_JOIN);
    st3.addJoinOnCondition("aa.hy = bb.hy");
    st.addTable(st3);
    
    System.out.println(st.getSql(null));
    SelectTable sts = (SelectTable)st.clone();
    System.out.println("-------------------------");
    System.out.println(sts.getSql(null));
  }
}
