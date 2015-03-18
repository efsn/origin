package com.esen.db.sql;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.exp.Expression;
import com.esen.util.exp.util.ExpSuperCompilerHelper;
import com.esen.util.i18n.I18N;


public class SelectTableUtil {
	
	/**
	 * 将Oracle的左连接语法改为特有语法；
	 * 原因是为了解决ORA-01445: 无法从不带保留关键字的表的联接视图中选择 ROWID 或采样 异常；
	 * 当关联的表的总字段数超过1024,就会出现这个异常；
	 * 
	 * 使用Oracle的特殊语法就不会出现，比如：
	 * ... from tb1 left join tb2 on tb1.id=tb2.id ...
	 * 改为：
	 * ...from tb1,tb2 where tb1.id=tb2.id(+) ...
	 * 
	 * 复杂的例子：
	 * from DYB0000107main_B19 a 
	 * left join DYB0000107main_B19 b 
	 * on (a.USERID_=b.USERID_ and a.BBQ_=to_char(add_months(to_date(b.BBQ_,'yyyymm--'),12),'yyyymm--') and a.BTYPE_=b.BTYPE_) 
	 * 改为：
	 * from DYB0000107main_B19 a 
	 * ,DYB0000107main_B19 b 
	 * where (a.USERID_=b.USERID_(+) and a.BBQ_=to_char(add_months(to_date(b.BBQ_(+),'yyyymm--'),12),'yyyymm--') and a.BTYPE_=b.BTYPE_(+)) 
	 * 
	 * 使用方法:
	 * getLeftJoinConditionForOracle("a.id=b.id","b")
	 * 返回: "a.id=b.id(+)"
	 * 
	 * @param joinon
	 *         join 条件表达式；
	 * @param tableAlias
	 *         左连接从表的别名；
	 * @return
	 */
	public static String getLeftJoinConditionForOracle(String joinon,String tableAlias){
		Expression exp = new Expression(joinon);
		exp.compile(new ExpSuperCompilerHelper());
		return exp.formatZz(new IFormatZzForOracleLeftJoinExp(tableAlias));
	}
	
	/**
	 * 判断表间的关联关系表达式，如果有一个关联表达式涉及3个及其以上的表的，则返回false
	 * 这个方法只有Oracle的数据库才会调用；
	 * 返回true，则使用Oracle的自身(+)语法连接；
	 * 返回false，则使用(inner/left) join语法连接；
	 * 
	 * 原因：
	 * a和b的关联条件(A.USERID_=B.USERID_(+)) 
	 * AND (C$.BBQPRE3_=B.BBQ_(+)) 
	 * AND (A.BTYPE_=B.BTYPE_(+))
	 * 涉及到另外一张表C$ , 执行这个sql出现异常：
	 * ORA-01417: 表可以外部连接到至多一个其它的表 
	 * 
	 * 这个异常是说一个表不能同时外连接到多个表；
	 * @return
	 */
	public static boolean isFitLeftJoinGrammerForOracle(SelectTable st){
		OracleFitLeftJoinGrammer olj = new OracleFitLeftJoinGrammer();
		return olj.isFitLeftJoinGrammer(st);
	}
  
  /**
   * 将子sql中的order by语句提到最外层；
   * 主要用于Sybase，其不支持子sql中包含order by 语句；
   * @param st
   * @return
   */
  public static SelectTable formatOrderBy(SelectTable st){
    List tmp = new ArrayList();//记录排序位置
    //分析那些排序字段对应的外层sql的字段；
    formatOrderBy(st,tmp);
    
    /**
     * 20090727
     * 处理排序字段的顺序，里面嵌套sql的排序指标顺序和提到外层的sql要一致；
     * 原来的顺序是倒的；
     */
    for(int i=tmp.size()-1;i>0;i--){
      OrderByInfo oi = (OrderByInfo)tmp.get(i--);
      String ti = (String)tmp.get(i);
      int pos = Integer.parseInt(ti);
      Field fi = st.getField(pos);
      st.addOrderByField(oi.getDialect(),fi.getField(),oi.isSortDesc(),oi.isDesc_nullsfirst(),oi.isAsc_nullslast());
    }
    
    return st;
  }
  
  private static void formatOrderBy(SelectTable st, List tmp) {
    analyseOrderByPos(st,tmp);
    for(int i=0;i<st.getTableCount();i++){
      BaseTable ti = st.getTable(i);
      if(ti instanceof SelectTable){
        SelectTable sti = (SelectTable)ti;
        formatOrderBy(sti,tmp);
      }
    }
    
  }

  private static void analyseOrderByPos(SelectTable st, List tmp) {
    SelectTable upst = st.getUpSelectTable();
    if(upst!=null){
      for(int i=st.getOrderByFieldCount()-1;i>=0;i--){
        OrderByInfo oi = st.removeOrderByField(i);
        String orderstr = oi.getOderbySrt();
        findOrderByPos(st,orderstr,oi,tmp);
        
      }
    }
  }

  private static void findOrderByPos(SelectTable st, String orderstr, OrderByInfo oi, List tmp) {
    boolean havep = orderstr.indexOf(".") > 0;
    int pos = -1;
    for (int i = 0; i < st.getFieldCount(); i++) {
      Field fi = st.getField(i);
      if (havep) {
        if (fi.getField().equals(orderstr)) {
          pos = i;
          break;
        }
      }
      else {
        if (fi.getAlias() != null && fi.getAlias().equals(orderstr)) {
          pos = i;
          break;
        }
      }
    }
    if (pos >= 0) {
      Field f = st.getField(pos);
      SelectTable upst = st.getUpSelectTable();
      if (upst != null) {
        findOrderByPos(upst, f.getAlias() == null ? f.getField() : f.getAlias(),oi, tmp);
      }
      else {
        tmp.add(String.valueOf(pos));
        tmp.add(oi);
      }
    }
  }

  /**
   * 获得指定 指标 字段的sql
   * @param st
   * @param fds 指标字段别名的集合
   * @return
   */
  public static SelectTable formatSelect(SelectTable st,String[] fds){
    int check = 0;
    //去掉多余的指标字段；
    for(int i=st.getFieldCount()-1;i>=0;i--){
      Field f = st.getField(i);
      if(f.getType()==2) continue;//维字段，不能去掉；
      String fname = f.getAlias()!=null?f.getAlias():f.getField();
      if(!contain(fds,fname)){
        removeField(st,i);
        check++;
      }
    }
    if(check==0)
      throw new RuntimeException(I18N.getString("com.esen.db.sql.selecttableutil.1", "找不到指定的指标：{0}", new Object[]{fds.toString()}));
    //优化sql，把没有指标引用的嵌套sql去掉；
    optimization(st);

    //指定顺序，并去掉最外层的维字段；
    orderFields(st,fds);
    return st;
  }

  private static void orderFields(SelectTable st, String[] fds) {
    for(int i=0;i<fds.length;i++){
      Field fi = null;
      for(int j=0;j<st.getFieldCount();j++){
        Field fj = st.getField(j);
        if(fj.getType()==2) continue;
        String fname = fj.getAlias()!=null?fj.getAlias():fj.getField();
        if(fds[i].equalsIgnoreCase(fname)){
          fi = st.removeField(j);
          break;
        }
      }
      st.addField(i,fi);
    }
    //去掉外层维字段；
    for(int i=st.getFieldCount()-1;i>=0;i--){
      Field f = st.getField(i);
      if(f.getType()==2) {
        st.removeField(i);
      }
    }
  }
  
  private static void optimization(SelectTable st) {
    if(st instanceof SelectUnionTable){
      SelectUnionTable sut = (SelectUnionTable)st;
      SelectTable sts[] = sut.getSelectTables();
      for(int k=sts.length-1;k>=0;k--){
        if(nullUnionSelect(sts[k])){
          sut.removeSelectTable(k);
          continue;
        }
        optimization(sts[k]);
      }
      
    }
    for(int i=st.getTableCount()-1;i>=0;i--){
      BaseTable bt = st.getTable(i);
      int n = getZbFieldCount(bt);
      if(n==0&&!haveUse(st,bt.getAlias())){
        st.removeTable(i);
      }
      if(bt instanceof SelectTable){
        SelectTable sti = (SelectTable)bt;
        optimization(sti);
      }
      
    }
    
  }
  private static boolean nullUnionSelect(SelectTable st) {
    for(int i=0;i<st.getFieldCount();i++){
      Field f = st.getField(i);
      if(f.getType()==2) continue;
      if(!f.getField().equals("0")&&!f.getField().equals("null"))
        return false;
    }
    return true;
  }
  private static int getZbFieldCount(BaseTable bt) {
    int n = 0;
    if(bt instanceof SelectTable){
      SelectTable sti = (SelectTable)bt;
      for(int i=0;i<sti.getFieldCount();i++){
        Field f = sti.getField(i);
        if(f.getType()==2) continue;
        n++;
      }
    }
    return n;
  }
  private static boolean haveUse(SelectTable st, String tag) {
    String match = ".*[^A-Za-z0-9_\\$]"+tag+"\\.[A-Za-z0-9_\\$].*";
    for(int i=0;i<st.getFieldCount();i++){
      Field f = st.getField(i);
      if(f.getType()==2) continue;
      if(f.getField().matches(match))
        return true;
    }
    for(int i=0;i<st.getGroupByCount();i++){
      String gstr = st.getGroupBy(i);
      if(gstr.matches(match))
        return true;
    }
    for(int i=0;i<st.getConditionCount();i++){
      String cstr = st.getConditionStr(i);
      if(cstr.matches(match))
        return true;
    }
    return false;
  }
  private static void removeField(SelectTable st, int i) {
    if(st instanceof SelectUnionTable){
      SelectUnionTable sut = (SelectUnionTable)st;
      SelectTable sts[] = sut.getSelectTables();
      for(int k=0;k<sts.length;k++){
        removeField(sts[k],i);
      }
    }
    for(int k=0;k<st.getTableCount();k++){
      BaseTable bt = st.getTable(k);
      if(bt instanceof SelectTable){
        SelectTable stk = (SelectTable)bt;
        for(int j=stk.getFieldCount()-1;j>=0;j--){
          Field fj = stk.getField(j);
          if(onlyUse(st,i,fj,stk.getAlias())){
            removeField(stk,j);
          }
        }
      }
    }
    st.removeField(i);
  }
  /**
   * 判断fj是否只有st的第i个字段引用过；
   * @param st
   * @param i
   * @param fj st的下层sql里的字段
   * @param tagj 
   * @return
   */
  private static boolean onlyUse(SelectTable st, int i, Field fj, String tagj) {
    if(fj.getAlias()!=null){
      String fdj = tagj+"."+fj.getAlias();
      if(onlyUse(st,i,fdj))
        return true;
      if(onlyUse(st,i,fj.getAlias()))
        return true;
    }
    if(onlyUse(st,i,tagj+"."+fj.getField()))
      return true;
    if(onlyUse(st,i,fj.getField()))
      return true;
    return false;
  }
  private static boolean onlyUse(SelectTable st, int i, String fdj) {
    Field f = st.getField(i);
    boolean haveuse = haveUse(f,fdj);
    if(haveuse){
      for(int k=0;k<st.getFieldCount();k++){
        if(k==i) continue;
        if(haveUse(st.getField(k),fdj)){
          return false;
        }
      }
      return true;
    }
    return false;
  }
  private static boolean haveUse(Field f,String fdj){
    String field = f.getField();
    return field!=null&&field.matches(".*[^A-Za-z0-9_\\$]"+fdj+"[^A-Za-z0-9_\\$].*");
  }
  private static boolean contain(String[] fds,String field){
    for(int i=0;i<fds.length;i++){
      if(fds[i]!=null&&fds[i].equalsIgnoreCase(field))
        return true;
    }
    return false;
  }

	/**
	 * 优化计数sql, 去掉left join 当有结果集过滤时，left join不能去掉；
	 * 
	 * @param cst
	 * @return
	 */
	public static SelectTable optimizationCountSql(SelectTable cst,
			Dialect dialect) {
		/*
		 * 拼写的Sql含有Full Join时（如Teradata数据库全连接），不能过度优化（即不能删掉过多的字段） add by RX
		 * 2013.07.24
		 */
		boolean needFullJoinInSql = !(cst instanceof SelectUnionTable)
				&& (null != dialect) && dialect.getDataBaseInfo().isTeradata()
				&& (cst.getTableCount() > 1)
				&& (BaseTable.FULL_JOIN == cst.getTable(1).getJoinType());
		/**
		 * 去掉于非主源表的字段 处理字段的代码移到前面，原因是后面移除leftjoin的表的时候，需要根据字段判断，
		 * 不能把表移除了，却把引用了该表的字段留下；
		 * 
		 * ISSUE:BI-8616 CHG by RX 2013.07.25 非full join情形下才通过删减字段优化
		 */
		if (!needFullJoinInSql) {
			if (!(cst instanceof SelectUnionTable)) {
				String ttag = cst.getTable(0).getAlias();
				if (cst.getTable(0) instanceof SelectTable) {
					for (int i = cst.getFieldCount() - 1; i >= 0; i--) {
						Field f = cst.getField(i);
						String fstr = f.getField();
						if (fstr != null) {
							int p = fstr.indexOf(".");
							if (p > 0 && !ttag.equals(fstr.substring(0, p))) {
								cst.removeField(i);
							}
						}
					}
				}
			}
			// 保留一个字段
			for (int i = cst.getFieldCount() - 1; i > 0; i--) {
				cst.removeField(i);
			}
			
	    /*
	     * ISSUE:BI-9881 sql优化规则权限,导致有分页的情况下由于优化了sql导致不分页.
	     * 现改为子句中都至少含有一个group by 才进行原来的优化流程.  edit by wandj 2014.1.8 
	     */
	    boolean hasgroup =false;
	    if(cst.getGroupByCount()>0){
	    	hasgroup =true;
	    }else{
	    	for(int i=0;i<cst.getTableCount();i++){
		    	BaseTable bt = cst.getTable(i);
		    	if(bt instanceof SelectTable){
		    		SelectTable st = (SelectTable)bt;
		    		if (st.getGroupByCount() > 0) {
							hasgroup = true;
							break;
						}
		    	}
		    }
	    }
	    if (cst.getConditionCount() == 0 && cst.getHavingConditionCount() == 0 && hasgroup) {
				for (int i = cst.getTableCount() - 1; i > 0; i--) {
					BaseTable bt = cst.getTable(i);
					if (bt.getJoinType() == BaseTable.LEFT_JOIN) {
						boolean fg = true;// 判断左连接的表,有没有被分组引用,如果引用了则不能删除此表;

						for (int j = 0; j < cst.getGroupByCount(); j++) {
							String gstr = cst.getGroupBy(j);
							/**
							 * 20100121 这里取group by 字符串中字段的别名引用,看是不是left
							 * join的表的字段;
							 * 有些不是简单的xxx.fieldname的形式，比如：SUBSTR(Ab$_.
							 * SWJG_$DM,1,5) 这里Ab$_可能是left join表的引用，需要把它取出来；
							 * 测试用例： 开发测试专用/开发测试/报表模板/分页/B71987 分页异常
							 */
							if (haveCiteTable(bt.getAlias(), gstr)) {
								fg = false;
								break;
							}
						}
						/**
						 * 20100201 BI-2980 如果字段也引用了bt表，则不能删除表bt 否则出现如下sql错误：
						 * select count(*) as cn_ from ( select max(b.HY_DM) as
						 * B3 from CFCS_XXB a left join DIM_T_LX c on
						 * (a.LX_DM=c.LX_DM) group by SUBSTR(c.LX_DM,1,2)) a
						 * 
						 * ORA-00904: "B"."HY_DM": 标识符无效 测试用例：
						 * 开发测试专用/开发测试/报表模板/分页/B61670 BI-2980 分页sql错误
						 */
						Field fd0 = cst.getField(0);
						if (haveCiteTable(bt.getAlias(), fd0.getField())) {
							fg = false;
						}
						if (fg)
							cst.removeTable(i);
					}
				}
			}
		}

		// 清楚排序字段
		cst.clearOrderByFields();
		// 生成计数sql
		SelectTable st = new SelectTable();
		st.addTable(cst);
		Field countf = new Field("count(*)", "cn_", Field.FTYPE_OTHER);
		st.addField(countf);
		return st;
	}

  /**
   * 判断field参数是否引用了tableAilase表别名；
   * 比如：substr(a.hy_dm,2)  应用了表别名a
   * @param tableAilase
   * @param field
   * @return
   */
  private static boolean haveCiteTable(String tableAilase,String field) {
    Pattern pt = Pattern.compile("(.*[^\\w\\$]{1})?([\\w\\S]+)\\.([\\w\\$]+).*",Pattern.CASE_INSENSITIVE);
    Matcher m = pt.matcher(field);
    if(m.matches())
      field = m.group(2);
    return tableAilase.equals(field);
  }
//  //国际化测试
//  public static void main(String[] args){
//	  System.out.println(I18N.getString("com.esen.db.sql.selecttableutil.nosuchitem", "", new Object[]{"test"}));
//  }
}
