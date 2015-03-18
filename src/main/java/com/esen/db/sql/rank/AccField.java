package com.esen.db.sql.rank;

import com.esen.db.sql.Field;
import com.esen.db.sql.OrderByInfo;
import com.esen.jdbc.dialect.Dialect;
/**
SQL> select deptno,ename,sal,
　　　2　sum(sal) over (partition by deptno order by ename) 部门连续求和,--各部门的薪水"连续"求和
　　　3　sum(sal) over (partition by deptno) 部门总和,　-- 部门统计的总和，同一部门总和不变
　　　4　100*round(sal/sum(sal) over (partition by deptno),4) "部门份额(%)",
　　　5　sum(sal) over (order by deptno,ename) 连续求和, --所有部门的薪水"连续"求和
　　　6　sum(sal) over () 总和,　-- 此处sum(sal) over () 等同于sum(sal)，所有员工的薪水总和
　　　7　100*round(sal/sum(sal) over (),4) "总份额(%)"
　　　8　from emp

 desc_nullsfirst=true; //降序时NULL排序到最前
     asc_nullslast=true;   //升序时NULL排序到最后
 * @author dw
 */
public class AccField extends Field {
  private StringBuffer field;
  private String tag;
  public AccField(Dialect dialect,String zb,String[] partitions,String tag){
    this(dialect,zb,partitions,null,false,false,false,tag);
  }
  public AccField(Dialect dialect,String zb,String[] partitions,String orderby,boolean desc,boolean desc_nullsfirst,boolean asc_nullslast,String tag){
    super();
    
    field = new StringBuffer(256);
    field.append("sum(").append(zb).append(")");
    field.append(" over (");
    if(partitions!=null&&partitions.length>0){
      field.append("partition by ");
      for(int i=0;i<partitions.length;i++){
        if(i>0) field.append(",");
        field.append(partitions[i]);
      }
    }
    if(orderby!=null&&orderby.length()>0){
      boolean f = OrderByInfo.isProcNullSort(dialect);
      field.append(" order by ").append(orderby);
      if (desc){
        field.append(" desc ");
        if(f&&!desc_nullsfirst)
          field.append(" nulls last");
      }else{
        if(f&&!asc_nullslast){
          field.append(" nulls first");
        }
      }
    }
    field.append(")");
    this.tag = tag;
  }
  public String getField(){
    return field.toString();
  }
  public String getAlias(){
    return tag;
  }
  public int getType(){
    return FTYPE_ZB;
  }
  public String toString(){
    if(tag==null||tag.length()==0)
      return field.toString();
    return field.toString() + " as " + tag;
  }
}
