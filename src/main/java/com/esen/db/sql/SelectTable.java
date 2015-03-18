package com.esen.db.sql;

import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.exp.Expression;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.util.ExpSuperCompilerHelper;
import com.esen.util.i18n.I18N;

/**
 * 描述一个sql的类
 * 可以嵌套，允许有内连接（多表联合查询）；
 * @author user
 *
 */
public class SelectTable extends BaseTable {
  
  private String tag; //用于嵌套子查询别名

  private List flist; //字段集合

  private List tlist; //表的集合，可以是嵌套

  private List clist; //条件集合
  private List hlist; //结果集过滤条件，有group by 用having

  private List glist; //分组集合

  private List olist; //排序集合

  private boolean needDistinct = false;

  public static final char CONDITION_AND_SYMBOL = '&';

  public static final char CONDITION_OR_SYMBOL = '|';
  
  private SelectTable upst = null;
  
  /**
   * 20100327
   * 引用一个计数器，用于记录每次addTable(...)时赋予每个Table成员的别名；
   * 目的是为了不重名；
   */
  private int tableCounter;

  public SelectTable() {
    super();
    flist = new ArrayList();
    tlist = new ArrayList();
    clist = new ArrayList();
    glist = new ArrayList();
    olist = new ArrayList();
    hlist = new ArrayList();
  }

  public SelectTable(String tag) {
    this();
    setAlias(tag);
  }
  public void clearAll(){
    flist.clear();
    tlist.clear();
    clist.clear();
    glist.clear();
    olist.clear();
    hlist.clear();
    tag = null;
  }
  public SelectTable getUpSelectTable(){
    return upst;
  }
  /**
   * 设置其父sql
   * @param upst
   */
  public void setUpSelectTable(SelectTable upst){
    this.upst = upst;
  }
  public boolean needDistinct() {
    return needDistinct;
  }

  public void setDistinct(boolean f) {
    this.needDistinct = f;
  }

  /**
   * 作为子查询时，设置别名
   * @param tag
   */
  public void setAlias(String tag) {
    this.tag = tag;
  }

  public String getAlias() {
    return tag;
  }
  /**
   * @deprecated
   * @param tag
   */
  public void setTag(String tag) {
    this.tag = tag;
  }
  /**
   * @deprecated
   * @return
   */
  public String getTag() {
    return tag;
  }
  /**
   * 获取分页sql
   * @param conf
   * @param start
   * @param limit
   * @return
   */
  public String getPageSql(Dialect dl, int start, int limit){
    if(start<0||limit<=0) return getSql(dl);
    return dl.getLimitString(getSql(dl), start, limit);
  }
  
  public String getSql(Dialect dl) {
	  return getSql(dl,0);
  }

  /**
   * 根据不同的数据库，生成不同的Sql
   * 增加参数oracleJoinType,原因如果分析sql不能使用(+)连接语法，则整个sql都不能使用，
   * 加这个参数用于递归时不用重新分析是否能使用(+)连接语法
   * 
   * @param conf 为空，则生成sql92标准sql
   * @param oracleJoinType 此参数用于分析Oracle的sql结构能否使用(+)连接语法；
   * =1 表示可以使用(+)连接语法；
   * =0 表示还没有进行分析；
   * =-1表示不能使用(+)连接语法；
   * @return
   */
  public String getSql(Dialect dl,int oracleJoinType) {
	  if(oracleJoinType==0){
		  oracleJoinType = oracleFitLeftJoin(dl);
	  }
    int len = (flist.size()+tlist.size()+clist.size())*10;
    StringBuffer sql = new StringBuffer(len);
    sql.append("select ");
    if (needDistinct())
      sql.append("distinct ");
    for (int i = 0; i < getFieldCount(); i++) {
      if (i > 0)
        sql.append(",");
      Field f = getField(i);
      if(f.getType()==Field.FTYPE_SELECTFIELD){
        SelectField sf = (SelectField)f;
        sql.append(sf.toString(dl));
      }else
        sql.append(getField(i).toString());
    }
    sql.append("\r\n");
    sql.append("from ");
    for (int i = 0; i < getTableCount(); i++) {
      BaseTable bt = getTable(i);
      generatorTable(sql, i, bt,dl,oracleJoinType);
    }
    optiCondition();
    if (getConditionCount() > 0) {
      sql.append("\r\n");
      sql.append("where ");
    }
    int k = 0;
    if(oracleJoinType>0){
    	k=procOracleJoin(dl,sql);
    }
    addCondition(sql, k);
    addGroupby(sql);
    addHavingCondition(sql);
    addOrderby(sql);
    return sql.toString();
  }
  
  /**
   * Oracle数据库，且sql中的关联条件符合使用Oracle自身关联表达式(+)语法，则返回1；
   * 否则返回-1
   * @param conf
   * @return
   */
  private int oracleFitLeftJoin(Dialect dl) {
	if(dl!=null){
		DataBaseInfo dbinf = dl.getDataBaseInfo();
		if(dbinf.isOracle()||dbinf.isTimesTen()){
			if(SelectTableUtil.isFitLeftJoinGrammerForOracle(this)){
				return 1;
			}
		}
	}
	return -1;
}

private void optiCondition() {
    for(int i=getConditionCount()-1;i>0;i--){
      String cond = getConditionStr(i);
      if(canRemove(cond))
        removeCondition(i);
    }
  }
/**
 * 将类似 ... like '%' 
 *       ... like '%%'
 *       ... like '?'
 * 都过滤掉；
 * @param cond
 * @return
 */
  private boolean canRemove(String cond) {
    String cc[] = cond.split(" [lL][iI][kK][eE] ");
    if(cc!=null&&cc.length>1){
      String v = cc[1].trim();
      if(v.matches("'[%?]+'"))
        return true;
    }
    return false;
  }

  private void addHavingCondition(StringBuffer sql) {
    if( hlist.size()>0)
      sql.append("\r\nhaving ");
    int k = 0;
    for (int i = 0; i < this.getHavingConditionCount(); i++) {
      ConditionInfo c = getHavingCondition(i);
      String cond = c.getCondtion().trim();
      //处理与条件，把与条件放在一起
      if (c.getSymbol() == CONDITION_AND_SYMBOL) {
        if (k > 0)
          sql.append(" and ");
        sql.append("(");
        sql.append(cond);
        sql.append(")");
        k++;
      }
    }
    for (int i = 0; i < this.getHavingConditionCount(); i++) {
      ConditionInfo c = getHavingCondition(i);
      String cond = c.getCondtion().trim();
      //处理或条件，把或条件放在一起
      if (c.getSymbol() == CONDITION_OR_SYMBOL) {
        if (k > 0)
          sql.append(" or ");
        sql.append("(");
        sql.append(cond);
        sql.append(")");
        k++;
      }
    }
    
  }

  /**
   * 处理Oracle817的连接方式；
   * 现在Oracle 都改为特有的连接语法；
   * 返回有多少个连接条件；
   * @param conf 
   * @param sql
   * @return
   */
  private int procOracleJoin(Dialect dl, StringBuffer sql) {
    int k=0;
    //oracle8 , TimesTen 一样的语法
    if (dl!=null&&(dl.getDataBaseInfo().isOracle()||dl.getDataBaseInfo().isTimesTen())) {
      StringBuffer joincond = new StringBuffer(128);
      //将Oracle817表间连接条件加入where语句；
      BaseTable bt0 = getTable(0);
      for (int i = 1; i < getTableCount(); i++) {
        BaseTable bt = getTable(i);
        boolean fl = bt.getJoinOnCondition() != null
            && bt.getJoinOnCondition().length() > 0;
        if (fl) {
          if (k > 0)
            joincond.append(" and ");
          joincond.append("(");
          joincond.append(getJoinCondForOracle(bt0,bt));
          joincond.append(")");
          k++;
        }
        String joinWhere = bt.getJoinWhereCondition();
        if(joinWhere!=null&&joinWhere.length()>0){
          if (k > 0)
            joincond.append(" and ");
          joincond.append(joinWhere);
        }
      }
      if(joincond.length()>0&&getConditionCount()==0){
        sql.append("\r\n");
        sql.append("where ");
      }
      if(joincond.length()>0){
        sql.append(joincond.toString());
      }
    }
    return k;
  }

  /**
   * 处理Oracle的join条件表达式；
   * @param bt0
   * @param bt
   * @return
   */
  private Object getJoinCondForOracle(BaseTable bt0, BaseTable bt) {
	String joinstr = bt.getJoinOnCondition();
	if(bt.getJoinType()==BaseTable.LEFT_JOIN){
		return SelectTableUtil.getLeftJoinConditionForOracle(joinstr, bt.getAlias());
	}else if(bt.getJoinType()==BaseTable.RIGHT_JOIN){
		return SelectTableUtil.getLeftJoinConditionForOracle(joinstr, bt0.getAlias());
	}
	return joinstr;
}

private void addOrderby(StringBuffer sql) {
    if (getOrderByFieldCount() > 0) {
      sql.append("\r\n");
      sql.append("order by ");
    }
    for (int i = 0; i < getOrderByFieldCount(); i++) {
      if (i > 0)
        sql.append(",");
      sql.append(getOrderByField(i).toString());
    }
  }

  private void addGroupby(StringBuffer sql) {
    if (getGroupByCount() > 0) {
      sql.append("\r\n");
      sql.append("group by ");
    }
    for (int i = 0; i < getGroupByCount(); i++) {
      if (i > 0)
        sql.append(",");
      sql.append(getGroupBy(i));
    }
  }

  private void addCondition(StringBuffer sql, int k) {
    for (int i = 0; i < this.getConditionCount(); i++) {
      ConditionInfo c = getCondition(i);
      String cond = c.getCondtion().trim();
      //处理与条件，把与条件放在一起
      if (c.getSymbol() == CONDITION_AND_SYMBOL) {
        if (k > 0)
          sql.append(" and ");
        sql.append("(");
        sql.append(cond);
        sql.append(")");
        k++;
      }
    }
    for (int i = 0; i < this.getConditionCount(); i++) {
      ConditionInfo c = getCondition(i);
      String cond = c.getCondtion().trim();
      //处理或条件，把或条件放在一起
      if (c.getSymbol() == CONDITION_OR_SYMBOL) {
        if (k > 0)
          sql.append(" or ");
        sql.append("(");
        sql.append(cond);
        sql.append(")");
        k++;
      }
    }
  }
  

  private void generatorTable(StringBuffer sql, int i, BaseTable bt, Dialect dl, int oracleJoinType) {
    if (oracleJoinType>0) {
    	/**
    	 * 现在Oracle 都改为特有的join语法;
    	 */
      generatorOracleTable(sql, i, bt,dl,oracleJoinType);
    }
    else {
      generatorOtherDbTable(sql, i, bt,dl);
    }
  }

  private void generatorOtherDbTable(StringBuffer sql, int i, BaseTable bt,Dialect dl ) {
    if (i == 0) {
      if (bt instanceof SelectTable) {
        SelectTable st = (SelectTable) bt;
        sql.append("(\r\n");
        sql.append(st.getSql(dl,-1));
        sql.append(")");
      }
      else if (bt instanceof RealTable) {
        RealTable rt = (RealTable) bt;
        sql.append(rt.getTable());
      }
      sql.append(" ").append(bt.getAlias());
    }
    if (i > 0) {
      sql.append("\r\n");
      boolean fl = bt.getJoinOnCondition() != null
          && bt.getJoinOnCondition().length() > 0;
      if (fl) {
        if (bt.getJoinType() == LEFT_JOIN) {
          sql.append("left join ");
        }
        else if (bt.getJoinType() == INNER_JOIN) {
          sql.append("inner join ");
        }
        else if (bt.getJoinType() == RIGHT_JOIN) {
          sql.append("right join ");
        }
        else if (bt.getJoinType() == FULL_JOIN) {
          sql.append("full join ");
        }else{
          sql.append("inner join ");
        }
      }
      else {
        /**
         * 20100225
         * 当两表或者两sql进行join如果没有join条件，则使用cross join连接进行笛卡尔乘积；
         * cross join语法是sql92标准，Oracle,db2,mysql,Sybase,Sql Server 都支持；
         * 原来只对Oracle使用这个语法，其他数据库使用','连接，这个只在mysql中可以使用，其他数据库比如DB2会出错：
         * DB2 SQL Error: SQLCODE=-338, SQLSTATE=42972, SQLERRMC=null, DRIVER=3.52.95 
         * 
         * 20100709 BI-3820
         * SybaseAse12.5(15.0)都不支持cross join
         * 
         * BI-6269
         * DB2 8 也不支持cross join
         * 为了兼容所有数据库，这里去掉cross join 改用直接用逗号连接。
         * 20120222 dw
         */
         /*if(dl.getDataBaseInfo().isSybase()){*/
           sql.append(",");
         /*}else{
           sql.append("cross join ");
         }*/
      }
      if (bt instanceof SelectTable) {
        SelectTable st = (SelectTable) bt;
        sql.append("(\r\n");
        sql.append(st.getSql(dl,-1));
        sql.append(")");
      }
      else if (bt instanceof RealTable) {
        RealTable rt = (RealTable) bt;
        sql.append(rt.getTable());
      }
      sql.append(" ").append(bt.getAlias());
      if (fl) {
        sql.append("\r\n");
        sql.append("on ").append(bt.getJoinOnCondition());
        String joinWhere = bt.getJoinWhereCondition();
        if(joinWhere!=null&&joinWhere.length()>0){
          sql.append(" and ").append(joinWhere);
        }
      }
    }
  }

  private void generatorOracleTable(StringBuffer sql, int i, BaseTable bt,
		  Dialect dl, int oracleJoinType) {
    if (i > 0) {
      sql.append("\r\n");
      sql.append(",");
    }
    if (bt instanceof SelectTable) {
      SelectTable st = (SelectTable) bt;
      sql.append("(\r\n");
      sql.append(st.getSql(dl,oracleJoinType));
      sql.append(")");
    }
    else if (bt instanceof RealTable) {
      RealTable rt = (RealTable) bt;
      sql.append(rt.getTable());
    }
    sql.append(" ").append(bt.getAlias());
  }

  public void addField(Field f) {
    if(f!=null)
      flist.add(f);
  }
  public void addField(int pos,Field f) {
    if(f!=null)
      flist.add(pos,f);
  }
  public Field removeField(int i){
    return (Field)flist.remove(i);
  }
  public int getFieldCount() {
    return flist.size();
  }

  public Field getField(int i) {
    return (Field) flist.get(i);
  }
  
  /**
   * 查询字段内容的字段， 只按照字段的field属性查找
   * @param field  要比较的field属性
   * @return 字段
   */
  public Field getField(String field) {
  	int size = flist.size();
  	for(int i=0; i< size; i++) {
  		if(((Field) flist.get(i)).getField().equals(field)) {
  			return (Field) flist.get(i);
  		}
  	}
  	return null;
  }

  public void setField(int i, Field f) {
    flist.set(i, f);
  }
  public RealTable getRealTable(String tbname) {
    return getRealTable(tbname,null,null);
  }
  public RealTable getAliesRealTable(String tbname, String tag) {
    for (int i = 0; i < getTableCount(); i++) {
      BaseTable t = getTable(i);
      if (t instanceof RealTable) {
        RealTable rt = (RealTable) t;
        if (rt.getTable().equalsIgnoreCase(tbname)&&rt.getAlias().equals(tag)) {
          return rt;
        }
      }
    }
    return null;
  }
  /**
   * 通过别名找数据库事实表；
   * 只在本层找；
   * @param tag
   * @return
   */
  public RealTable getAliesRealTable( String tag) {
    for (int i = 0; i < getTableCount(); i++) {
      BaseTable t = getTable(i);
      if (t instanceof RealTable) {
        RealTable rt = (RealTable) t;
        if (rt.getAlias().equals(tag)) {
          return rt;
        }
      }
    }
    return null;
  }
  /**
   * 返回指定表名及其关联条件一致的关联表对象；
   * @param tbname
   * @param rfield   tbname的关联字段；
   * @param rfield2  对应关联表的关联字段；
   * @return
   */
  public RealTable getRealTable(String tbname,String[] rfield,String[] rfield2) {
    for (int i = 0; i < getTableCount(); i++) {
      BaseTable t = getTable(i);
      if (t instanceof RealTable) {
        RealTable rt = (RealTable)t;
        if (rt.getTable().equalsIgnoreCase(tbname)) {
          if(rfield==null||rfield2==null)
            return rt;
          if(rfield.length!=rfield2.length)
//            throw new RuntimeException("错误的关联字段参数");
        	  throw new RuntimeException(I18N.getString("com.esen.db.sql.selecttable.wrongjoinargs", "错误的关联字段参数"));
          String oncond = rt.getJoinOnCondition();
          if(equalsOnCondition(oncond,rt.getAlias(),rfield,rfield2))
            return rt;
        }
      }
    }
    return null;
  }
  
  private boolean equalsOnCondition(String oncond, String tag2, String[] rfield, String[] rfield2) {
    Expression onexp = new Expression(oncond);
    onexp.compile(new ExpSuperCompilerHelper());
    ExpressionNode nd = onexp.getRootNode();
    return parserOnCondNode(nd,tag2,rfield,rfield2);
  }

  private boolean parserOnCondNode(ExpressionNode nd, String tag2, String[] rfield, String[] rfield2) {
    if(nd.isOperator()&&nd.getOp().getIndex()==ExpFuncOp.OPINDEX_EQUAL){
      ExpressionNode n1 = nd.getNode(0);
      ExpressionNode n2 = nd.getNode(1);
      if(n1.isData()&&n2.isData()){
        ExpVar vc1 = n1.getVar();
        ExpVar vc2 = n2.getVar();
        String v1 = vc1.getName();
        String v2 = vc2.getName();
        int p1 = v1.indexOf('.');
        int p2 = v2.indexOf('.');
        if(v1.substring(0,p1).equalsIgnoreCase(tag2)&&!v2.substring(0,p2).equalsIgnoreCase(tag2)){
          if(!parserField(v1.substring(p1+1),v2.substring(p2+1),rfield,rfield2))
            return false;
        }else if(!v1.substring(0,p1).equalsIgnoreCase(tag2)&&v2.substring(0,p2).equalsIgnoreCase(tag2)){
          if(!parserField(v2.substring(p2+1),v1.substring(p1+1),rfield,rfield2))
            return false;
        }else return false;
      }
    }
    for(int i=0;i<nd.getNodeCount();i++){
      if(!parserOnCondNode(nd.getNode(i),tag2,rfield,rfield2))
        return false;
    }
    return true;
  }

  private boolean parserField(String rfd, String rfd2, String[] rfield, String[] rfield2) {
    for(int i=0;i<rfield.length;i++){
      if(rfd.equalsIgnoreCase(rfield[i]) && rfd2.equalsIgnoreCase(rfield2[i]))
        return true;
    }
    return false;
  }

  public void addTable(String tbname) {
    if (tbname == null || tbname.length() == 0) {
//      throw new RuntimeException("表名为空；");
    	 throw new RuntimeException(I18N.getString("com.esen.db.sql.selecttable.nulltablename", "表名为空；"));
    }
    RealTable rt = getRealTable(tbname);
    if (rt == null) {
      rt = new RealTable(tbname, getAutoTag());
      addTable(rt);
    }
  }

  /**
   * 当调用addTable()时，为每个成员获取一个别名；
   * 20100327
   * 引用一个计数器，目的是为了不重名；
   * 原来的代码使用getTableCount()做基数，当调用remove删除一个成员，再调用addTable时，别名就和上次增加的别名重复；
   * @return
   */
  public String getAutoTag() {
    String g = getAutoTag(tableCounter++);//String.valueOf((char) ('a' + getTableCount()));
    if (tag != null && tag.length() > 0) {
      g = tag + g;
    }
    /**
     * 有的数据库sql中不支持$,比如as400
     */
    return g.length()>1?g+"_":g;
  }

  private String getAutoTag(int n) {
    int k = n/26;
    int m = n%26;
    if(k>0){
      return getAutoTag(k-1)+String.valueOf((char) ('a' + m));
    }else{
      return String.valueOf((char) ('a' + m));
    }
  }
  
  public void addTable(BaseTable t) {
    if(t==null) return;
    if (t instanceof SelectTable) {
      SelectTable st = (SelectTable) t;
      /**
       * 20100325 BI-3276 出现别名重复的问题；
       * 原因：SelectTable嵌套构造sql时，如果一个SelectTable实例被多次join到新的SelectTable实例，就会出现别名重复问题；
       * 这是代码上的bug，一个st1实例join到另一个st2的实例，会给这个st1实例设置别名，每次都必须重新设置，因为st2中有自己的别名体系；
       * 原来的代码如果st1有别名，则使用自己的别名，加入到st2后，可能里面的SelectTable实例别名重复，执行时，在Oracle出现：
       * ORA-00918: 未明确定义列 异常；
       * 
       */
      //if (st.getAlias() == null) {
        st.setAlias(getAutoTag());
      //}
     
      st.setUpSelectTable(this);
    }
    tlist.add(t);
  }

  public void addTable(int i, RealTable rt) {
    if(rt==null) return;
    tlist.add(i, rt);
  }

  public int getTableCount() {
    return tlist.size();
  }

  public BaseTable getTable(int i) {
    return (BaseTable) tlist.get(i);
  }
  public BaseTable removeTable(int i){
    return (BaseTable) tlist.remove(i);
  }
  /**
   * 添加与条件
   * @param cf
   */
  public void addCondition(String cf) {
    if(!isTrueCondition(cf))
      clist.add(new ConditionInfo(cf, CONDITION_AND_SYMBOL));
  }

  private boolean isTrueCondition(String cf) {
    try{
    Expression onexp = new Expression(cf);
    onexp.compile(new ExpSuperCompilerHelper());
    if(onexp.isConstExp()&&onexp.evaluateBoolean(null))
      return true;
    }catch(Exception ex){
      //有些sql表达式遍野不通过，比如 xxx in ('10','20') ,这里不管
    }
    return false;
  }

  public void addCondition(int pos, String cf) {
    if(cf!=null&&cf.length()>0)
      clist.add(pos, new ConditionInfo(cf, CONDITION_AND_SYMBOL));
  }

  /**
   * 添加或条件
   * @param cf
   */
  public void addOrCondition(String cf) {
    if(cf!=null&&cf.length()>0)
      clist.add(new ConditionInfo(cf, CONDITION_OR_SYMBOL));
  }
  /**
   * 添加结果集过滤 与条件
   * @param cf
   */
  public void addHavingCondition(String cf){
    if(cf!=null&&cf.length()>0)
      hlist.add(new ConditionInfo(cf, CONDITION_AND_SYMBOL));
  }
  public void addHavingOrCondition(String cf){
    if(cf!=null&&cf.length()>0)
      hlist.add(new ConditionInfo(cf, CONDITION_OR_SYMBOL));
  }
  
  public int getConditionCount() {
    return clist.size();
  }
  public ConditionInfo getCondition(int i){
    return (ConditionInfo) clist.get(i);
  }
  public String getConditionStr(int i) {
    return getCondition(i).getCondtion();
  }
  public ConditionInfo removeCondition(int i){
    return (ConditionInfo)clist.remove(i);
  }
  
  public int getHavingConditionCount(){
    return hlist.size();
  }
  public ConditionInfo getHavingCondition(int i){
    return (ConditionInfo) hlist.get(i);
  }
  public String getHavingConditionStr(int i){
    return getHavingCondition(i).getCondtion();
  }

  public void addGroupBy(String grb) {
    if(grb!=null&&grb.length()>0)
      glist.add(grb);
  }
  public void addGroupBy(int pos,String grb) {
    if(grb!=null&&grb.length()>0)
      glist.add(pos,grb);
  }

	public void setGroupBy(int pos, String grb) {
		if (grb != null && grb.length() > 0)
			glist.set(pos, grb);
	}
  public String removeGroupBy(int i){
    if(glist.size()>i)
      return (String)glist.remove(i);
    return null;
  }
  public int getGroupByCount() {
    return glist.size();
  }

  public String getGroupBy(int i) {
    return (String) glist.get(i);
  }

  /**
   * 排序字段集合
   * @param i
   * @return
   */
  public OrderByInfo getOrderByField(int i) {
    return (OrderByInfo) olist.get(i);
  }
  public OrderByInfo removeOrderByField(int i){
    return (OrderByInfo) olist.remove(i);
  }
  /**
   * 排序字段个数
   * @return
   */
  public int getOrderByFieldCount() {
    return olist.size();
  }
  public void clearOrderByFields(){
    olist.clear();
  }
  /**
   *  true降序
   * @param f
   * @param sort
   */
  public void addOrderByField(Dialect dialect,String f, boolean sort,boolean desc_nullsfirst,boolean asc_nullslast) {
    if(f!=null&&f.length()>0)
      olist.add(new OrderByInfo(dialect,f, sort,desc_nullsfirst,asc_nullslast));
  }
  /**
   * sql92 标准sql
   */
  public String toString(){
    return getSql(null);
  }

  public Object clone() {
    SelectTable st = (SelectTable)super.clone();
    st.tag = tag;
    st.needDistinct = needDistinct;
    st.flist = new ArrayList(flist);
    st.tlist = new ArrayList(tlist.size());
    for(int i=0;i<tlist.size();i++){
      BaseTable bt = (BaseTable)tlist.get(i);
      st.tlist.add(bt.clone());
    }
    st.clist = new ArrayList(clist);

    st.glist = new ArrayList(glist);

    st.olist = new ArrayList(olist);

    st.hlist = new ArrayList(hlist);

    return st;
  }

}

class ConditionInfo {
  private String cond;

  private char symbol;

  public ConditionInfo(String cond, char symbol) {
    this.cond = cond;
    this.symbol = symbol;
  }

  public String getCondtion() {
    return cond;
  }

  public char getSymbol() {
    return symbol;
  }
}
