package com.esen.db.sql;

import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.Expression;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.util.ExpSuperCompilerHelper;
import com.esen.util.i18n.I18N;

public class OracleFitLeftJoinGrammer {
	public OracleFitLeftJoinGrammer(){
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
	public boolean isFitLeftJoinGrammer(SelectTable st) {
		/**
		 * 20110329
		 * 对于复合Olap生成的sql对象递归处理，算法改进；
		 */
		if(st instanceof SelectUnionTable){
			SelectUnionTable stu = (SelectUnionTable)st;
			SelectTable[] stus = stu.getSelectTables();
			for(int k=0;k<stus.length;k++){
				if(!isFitLeftJoinGrammer((SelectTable)stus[k])){
					return false;
				}
			}
		}
		int joinType = -1;
		for(int i=0;i<st.getTableCount();i++){
			BaseTable sti = st.getTable(i);
			if(sti instanceof SelectTable){
				if(!isFitLeftJoinGrammer((SelectTable)sti)){
					return false;
				}
			}
			if(i==0) {
				continue;
			}
			/**
			 * BI-4795 20110504
			 * 如果sql中对第一个表的关联方式，有左连接，也有右连接，比如：
			 * ...from a left join b on a.id=b.id right join c on a.id=c.id ...
			 * 这样的连接也不能使用(+)的连接语法，否则会出现
			 * ORA-01417: 表可以外部连接到至多一个其它的表 的 异常
			 * 
			 * BI-4881 20110524
			 * 这里判断一个sql是否同时有左连接和右连接，内链接不参与；
			 * 即：如果同时只有内链接和左连接，或者同时只有内链接和右连接，是可以使用(+)连接表达式的；
			 */
			if (sti.getJoinType() != 0) {
				if (joinType < 0) {
					joinType = sti.getJoinType();
				}
				else {
					if (joinType != sti.getJoinType()) {
						return false;
					}
				}
			}
			if(!isFitLeftJoinGrammer(sti)){
				return false;
			}
		}
		
		return true;
	}
	
	private boolean isFitLeftJoinGrammer( BaseTable sti) {
		String joinstr = sti.getJoinOnCondition();
		if(joinstr!=null&&joinstr.length()>0){
			Expression exp = new Expression(joinstr);
			/**
			 * add by jzp 2013-03.04  ISSUE:BI-8172 
			 * 农发行中表关联关系中使用了 这样 "ssss"^"ffss" 连接符号^会翻译成双竖线 ||，无法通过编译
			 * 所以不能采用默认的表达式编译器
			 */
			ExpSuperCompilerHelper expSupHelper = new ExpSuperCompilerHelper();
			try{
				exp.compile(expSupHelper,expSupHelper);
			}catch(Exception e){
				throw new RuntimeException(I18N.getString("com.esen.db.sql.OracleFitLeftJoinGrammer.java.exptioncompile",
						"表达式无法通过编译:") + joinstr);
				//throw new RuntimeException("表达式无法通过编译:"+joinstr);
			}
			/**
			 * BI-4640 20110411
			 * 判断一个连接表达式中，是否有三个以上（包含三个）的表的字段引用；
			 * 如果是，则返回false，防止发生ORA-01417: 表可以外部连接到至多一个其它的表 异常，
			 * 说明不能使用Oracle的(+)外连接方法来解决ORA-01445: 无法从不带保留关键字的表的联接视图中选择 ROWID 或采样 异常问题；
			 * 
			 * 这个帖子上的问题，之所以会出现，是因为原来的代码总是和sql中的第一个表做比较，
			 * 来判断连接表达式中是否包含三个及其以上的表字段；
			 * 这是不全面的，比如这里的d表本来要和a表关联，但是a表上没有关联字段，需要a表先和b表关联，然后d再和b关联，
			 * 这样d表的关联表达式是d和b的字段组合，原来的代码却总是和a做比较，认为超过了三个以上的表字段引用；
			 * 
			 * 现在改为直接判断关联表达式，看里面是否包含三个及其以上的表字段引用；
			 */
			List aliasList = new ArrayList();
			fitLeftJoinGrammer(aliasList,exp.getRootNode());
			if(aliasList.size()>2){
				return false;
			}
		}
		return true;
	}

	private void fitLeftJoinGrammer(List list ,ExpressionNode nd) {
		if(nd.isData()){
			ExpVar var = nd.getVar();
			String field = var.getName();
			int p = field.indexOf(".");
			if(p>0){
				String alias = field.substring(0,p);
				if(!list.contains(alias))
					list.add(alias);
			}
		}
		for(int i=0;i<nd.getNodeCount();i++){
			fitLeftJoinGrammer(list,nd.getNode(i));
		}
	}

	
}
