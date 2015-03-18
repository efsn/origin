package com.esen.jdbc;

import java.sql.Types;
import java.util.Calendar;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.StrFunc;
import com.esen.util.exp.ExpConstData;
import com.esen.util.exp.ExpEvaluateHelper;
import com.esen.util.exp.ExpFuncOp;
import com.esen.util.exp.ExpUtil;
import com.esen.util.exp.ExpVar;
import com.esen.util.exp.Expression;
import com.esen.util.exp.ExpressionNode;
import com.esen.util.exp.IFormatZz;
import com.esen.util.exp.util.ExpSuperCompilerHelper;
import com.esen.util.exp.util.ExpVarObject;
import com.esen.util.i18n.I18N;

/**
 * 将用户输入的表达式转换成sql表达
 * @author yukun
 *
 */
public class FormatExpToSqlExp implements IFormatZz {
	public static final String SQL_FALSE = "1>2";

	public static final String SQL_TRUE = "2>1";

	public static final String SPLITOR = "[\\r|\\n|\\||\\,]+";

	private Dialect dialect;

	public FormatExpToSqlExp(Dialect dialect) {
		this.dialect = dialect;
	}

	public Dialect getDialect() {
		return dialect;
	}

	/**
	 * 将输入的表达式expstr，转换成dl数据库语法的 sql表达式；
	 * 输入的字符串将使用默认的编译器ExpSuperCompilerHelper编译；
	 * @param expstr
	 * @return
	 */
	public String toSqlExp(String expstr) {
		Expression exp = new Expression(expstr);
		exp.compile(new ExpSuperCompilerHelper());
		return toSqlExp(exp);
	}

	public String toSqlExp(Expression exp) {
		return exp.formatZz(this, true);
	}

	public String toSqlExp(ExpressionNode expNd) {
		return expNd.formatZz(this, true);
	}

	public String formatZz(ExpVar var) {
		return null;
	}

	public String formatNode(ExpressionNode pnode, ExpressionNode nd) {
		if (nd.isConst()) {
			switch (nd.getConstData().getType()) {
				case ExpUtil.TOSTR:
					return quateSqlConstStr(nd.getConstData());
				case ExpUtil.TODAT:
					return quateSqlConstDate(nd.getConstData().toDate());
				case ExpUtil.TOLGC:
					return nd.getConstData().toBoolean() ? SQL_TRUE : SQL_FALSE;
			}
			return null;
		}

		if (nd.isData()) {
			ExpVar var = nd.getVar();
			if (var.getImplType() == ExpVar.VARTYPE_OBJECT) {
				/**
				 * 20090220
				 * 特殊处理报表参数，现在的报表参数在表达式树结构中是一个对象，在转换为SQL时，可以借助与VarData对象中的方法，是字符串的需要上引号、是日期的需要转换成
				 * 对应数据库的日期常量定义方式......
				 * 测试用例：开发测试--报表模版--报表参数--枚举参数中文问题
				 */
				ExpVarObject vo = (ExpVarObject) var;
				Object o = vo.toStr(getExpEvaluateHelperForCalcConstInSql());
				char tp = ExpConstData.guessVarDataType(pnode, nd, o);
				String vstr = quateSqlConst(nd, tp);
				if (vstr != null)
					return vstr;
			}
		}

		if (nd.isConstExp()) {
			String vstr = formatConstExp(nd);
			if (vstr != null)
				return vstr;
		}
		if (nd.isFunc()) {
			return formatFuncNode(nd);
		}
		else if (nd.isOperator()) {
			String v = formatOpNode(nd);
			if (v != null)
				return v;
			v = formatLeftNode(nd);
			if (v != null)
				return v;
			//将比较符两边的类型转为一致
			v = formatDifferentTypes(nd);
			if (v != null)
				return v;
		}
		return null;
	}

	/**
	 * 处理常量表达式，这里直接计算了返回；
	 * 比如：len('abc') 这里返回 3 
	 * 如果需要原样返回，或者翻译len方法为sql语法，请重载此方法；
	 * @param nd
	 * @return
	 */
	public String formatConstExp(ExpressionNode nd) {
		char returnType = nd.getReturnType();
		return quateSqlConst(nd, returnType);
	}

	private String quateSqlConst(ExpressionNode nd, char returnType) {
		switch (returnType) {
			case ExpUtil.TOSTR:
				return quateSqlConstStr(evalstr(nd));
			case ExpUtil.TODAT:
				return quateSqlConstDate(nd.evaluateDate(getExpEvaluateHelperForCalcConstInSql()));
			case ExpUtil.TOINT:
				return String.valueOf(nd.evaluateInt(getExpEvaluateHelperForCalcConstInSql()));
			case ExpUtil.TOFLT:
				return String.valueOf(nd.evaluateDouble(getExpEvaluateHelperForCalcConstInSql()));
			case ExpUtil.TOLGC:
				return formatNode_constexp_logic(nd);
		}
		/**
		 * 20100526 BI-3726
		 * 如果报表参数的属性值参与指标运算：_s(xxb.tzze)/@p.value 
		 * 解析@p.value这个常量要当做一个整体来运算；
		 * 原来的代码分别运算@p和value是不正确的，sql生成有误；
		 */
		if (nd.isOperator()) {
			int o = nd.getOp().getIndex();
			if (o == ExpFuncOp.OPINDEX_DOT) {
				return evalstr(nd);
			}
		}
		return null;
	}

	/**
	 * 将形如：left(xxx,1)='A' 的条件表达式转换为： xxx like 'A%'
	 * @param nd
	 * @return
	 */
	private String formatLeftNode(ExpressionNode nd) {
		if (nd.isOperator() && nd.getOp().getIndex() == ExpFuncOp.OPINDEX_EQUAL) {
			ExpressionNode n1 = nd.getNode(0);
			ExpressionNode n2 = nd.getNode(1);
			if (n1.isFunc() && n1.getFunc().getIndex() == ExpFuncOp.FUNCINDEX_LEFT && (n2.isConst() || n2.isConstExp())) {
				return formatLeftNode(n1, n2);
			}
			if (n2.isFunc() && n2.getFunc().getIndex() == ExpFuncOp.FUNCINDEX_LEFT && (n1.isConst() || n1.isConstExp())) {
				return formatLeftNode(n2, n1);
			}
		}
		return null;
	}

	private String formatLeftNode(ExpressionNode n1, ExpressionNode n2) {
		ExpressionNode nd = n1.getNode(0);
		String field = nd.formatZz(this, true, n1);
		String v = evalstr(n2);
		return field + " like '" + v + "%'";
	}

	private String formatNode_constexp_logic(ExpressionNode nd) {
		//避免把is not null 转换为 true或false
		if (nd.isOperator() && nd.getOp().getIndex() == ExpFuncOp.OPINDEX_NOT && nd.getNode(0).isConstNull()) {
			return null;
		}
		return evalbool(nd) ? SQL_TRUE : SQL_FALSE;
	}

	private String quateSqlConstDate(Calendar calendar) {
		if (calendar == null) {
			return ExpUtil.VALUE_NULL;
		}
		return this.dialect.funcToDate(StrFunc.date2str(calendar, "yyyymmdd"));
	}

	/**
	 * BI-4888 20110531
	 * 对于空常量，sql中应该正确的处理；
	 * xxx=null -> xxx is null
	 * xxx='null' -> xxx='null' 
	 * @param data
	 * @return
	 */
	private String quateSqlConstStr(ExpConstData data) {
		if (data.isNull()) {
			return ExpUtil.VALUE_NULL;
		}
		return quateSqlConstStr(data.toString());
	}

	private String quateSqlConstStr(String s) {
		if (s == null) {
			return ExpUtil.VALUE_NULL;
		}
		return '\'' + s + '\'';
	}

	private String formatOpNode(ExpressionNode nd) {
		switch (nd.getOp().getIndex()) {
			/**
			 * BI-5944 增加日期+-操作
			 */
			case ExpFuncOp.OPINDEX_PLUS: {
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				if (n1.isReturnDate() && n2.isReturnInt()) {
					return getDialect().formatOffsetDate(n1.formatZz(this, true, nd),
							StrFunc.parseInt(n2.formatZz(this, true, nd), 0), 'd');
				}
				break;
			}
			case ExpFuncOp.OPINDEX_SUB: {
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				if (n1.isReturnDate() && n2.isReturnInt()) {
					return getDialect().formatOffsetDate(n1.formatZz(this, true, nd),
							0 - StrFunc.parseInt(n2.formatZz(this, true, nd), 0), 'd');
				}
				break;
			}
			case ExpFuncOp.OPINDEX_LEFT_SQUAREBRACKET: {
				StringBuffer r = new StringBuffer(nd.getNodeCount() * 10);
				r.append("(");
				for (int i = 0, len = nd.getNodeCount(); i < len; i++) {
					r.append(nd.getNode(i).formatZz(this, true, nd));
					if (i < len - 1) {
						r.append(',');
					}
				}
				r.append(")");
				return r.toString();
			}
			case ExpFuncOp.OPINDEX_NOTEQUAL_C:
			case ExpFuncOp.OPINDEX_NOTEQUAL: {
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				if (n1.isConstNull()) {//null != a  -->>  a is not null 
					return n2.formatZz(this, true, nd) + " IS NOT " + n1.formatZz(this, true, nd);
				}
				else if (n2.isConstNull()) {// a != null  -->>  a is null
					return n1.formatZz(this, true, nd) + " IS NOT " + n2.formatZz(this, true, nd);
				}
				//其他的情况 :  a!=b  -->>  a <> b
			}
			case ExpFuncOp.OPINDEX_IS:
			case ExpFuncOp.OPINDEX_EQUAL: {
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				String ffstr2 = n2.formatZz(this, true, nd);
				String ffstr = n1.formatZz(this, true, nd);
				/**
				 * 对于 a is null 表达式的翻译不应该加上 or a = '' ;
				 * 原因：
				 * 1) null和''是不一样的，不能认为是相同的意义；
				 * 2) 如果a是数值类型，or a='' 在db2这种严格规定数据类型的数据库上是会报错的；
				 */
				if (n1.isConstNull()) {//null is a    null = a 
					if (n2.isOperator() && n2.getFunc().getIndex() == ExpFuncOp.OPINDEX_NOT) { //null is not a --> a is not null
						return formatZzFuncNode(n2, 0) + " IS NOT " + ffstr;
					}
					else {//null is a --> a is null
						return ffstr2 + " IS NULL";
					}
				}
				else if (n2.isConstNull()) {
					if (n1.isOperator() && n1.getFunc().getIndex() == ExpFuncOp.OPINDEX_NOT) {// not a is null
						return formatZzFuncNode(n1, 0) + " IS NOT " + ffstr2;
					}
					else {// a is null --> a is null
						return ffstr + " IS NULL";
					}
				}
				else if (n2.isOperator() && n2.getFunc().getIndex() == ExpFuncOp.OPINDEX_NOT) {// a is not null  a is not b
					ExpressionNode nn1 = n2.getNode(0);
					if (nn1.isConstNull()) {//a is not null
						return ffstr + " IS" + ffstr2;
					}
					else {//a is not b --> a<>b
						return ffstr + "<>" + nn1.formatZz(this, true, n2);
					}
				}/*else{
					 if(n1.isConst()&&ffstr!=null&&ffstr.equals("''")){// '' = a -->  a=''
					   return ffstr2+" IS NULL";
					 }
					 if(n2.isConst()&&ffstr2!=null&&ffstr2.equals("''")){// a='' -->  a=''
					   return ffstr+" IS NULL ";
					 }
					}*/
				//其他情况：a is b； 如果 a,b有日期类型，需要到下面处理
				//return n1.formatZz(this, true)+" = "+n2.formatZz(this, true);
				/**
				 * 这里=和<>的case处理都不加break，原因是：
				 * 处理形如bbq()='2008'的条件要到下面数据期处理函数中处理。
				 * 比如可能是月报，生成的sql 是 a.bbq like '2008%' ；
				 * 否则会生成a.bbq='2008'查不到数据；
				 * 
				 * --20111121 dw
				 */
			}
			case ExpFuncOp.OPINDEX_LIKE:
			case ExpFuncOp.OPINDEX_GREAT:
				;
			case ExpFuncOp.OPINDEX_GREATEQUAL:
				;
			case ExpFuncOp.OPINDEX_LESS:
				;
			case ExpFuncOp.OPINDEX_LESSEQUAL: {
				//和日期型变量,其中一个是日期，另一个不是日期，则试图将不是日期的参数转换成日期类型；
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				if (isDate(n1, n2)) {
					String v = formatDataOpNode_WithDateCompare(nd);
					if (v != null)
						return v;
					v = formatOpNode_WithDateCompare(nd);
					if (v != null)
						return v;
				}

				break;
			}
			case ExpFuncOp.OPINDEX_BETWEEN: {
				ExpressionNode n1 = nd.getNode(0);
				if (getDataType(n1) == ExpUtil.TODAT) {
					return formatOpNode_WithDate_between(nd);
				}
				break;
			}
			case ExpFuncOp.OPINDEX_SQL_OR:
			case ExpFuncOp.OPINDEX_OR: {// true || abc = true
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				boolean r = n1.isConstExp() && (n1.getReturnType() == ExpUtil.TOLGC);
				boolean l = n2.isConstExp() && (n2.getReturnType() == ExpUtil.TOLGC);
				if (r && l && !evalbool(n1) && !evalbool(n2))
					return SQL_FALSE;
				if (r)
					return evalbool(n1) ? SQL_TRUE : n2.formatZz(this, true, nd);
				if (l)
					return evalbool(n2) ? SQL_TRUE : n1.formatZz(this, true, nd);
				String vv = formatCondition(nd);
				if (vv != null)
					return vv;
				break;
			}
			case ExpFuncOp.OPINDEX_SQL_AND:
			case ExpFuncOp.OPINDEX_AND: {
				//去掉以前对常量真条件的处理，现在改为ExpressionOptimize优化处理；
				String vv = formatCondition(nd);
				if (vv != null)
					return vv;
				break;
			}
			case ExpFuncOp.OPINDEX_CASE:
				return formatZz_op_case(nd);
			case ExpFuncOp.OPINDEX_DIV: {
				ExpressionNode n2 = nd.getNode(1);
				if (n2.isConst()) {
					double n = n2.evaluateDouble(getExpEvaluateHelperForCalcConstInSql());
					if (n != 0)
						return null;
				}
				String v = formateDiv(nd);
				if (v != null)
					return v;
				break;
			}
			case ExpFuncOp.OPINDEX_STRCONCAT: {
				/*
				 * BUG:BI-8617: deleted by liujin 2013.07.02
				 * 对 Teradata 数据库使用  dialect 中提供的方法。
				 */	
				DataBaseInfo db = getDialect().getDataBaseInfo();
				if (db.isMysql() || db.isTeradata()) {
					//字符串连接，比如： 'aa'^'bb' 在mysql中必须使用concat函数;
					//如果转换成 'aa'||'bb' ,在Mysql中是或运算；
					return getDialect().funcStrCat(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
				}
				break;
			}
			case ExpFuncOp.OPINDEX_IN:
			case ExpFuncOp.OPINDEX_NOTIN: {
				return getSqlForFuncIN(nd);
			}
		}
		return null;
	}

	private String getSqlForFuncIN(ExpressionNode nd) {
		ExpressionNode nd0 = nd.getNode(0);
		ExpressionNode nd1 = nd.getNode(1);
		if (!nd0.isConstExp() && nd1.isConstExp()) {
			return getSqlForFuncIN(nd, nd0, nd1);
		}
		if (nd0.isConstExp() && !nd1.isConstExp()) {
			return getSqlForFuncIN(nd, nd1, nd0);
		}
		return null;
	}

	private String getSqlForFuncIN(ExpressionNode nd, ExpressionNode nd0, ExpressionNode nd1) {
		String fd = nd0.formatZz(this, true, nd);
		String[] vs = nd1.evaluateStringArray(getExpEvaluateHelperForCalcConstInSql());
		StringBuffer sql = new StringBuffer(32);
		sql.append(fd).append(formatZz_Op(nd)).append("(");
		if (vs != null) {
			int k = vs.length;
			for (int i = 0; i < k; i++) {
				if (i > 0)
					sql.append(',');
				sql.append('\'').append(vs[i]).append('\'');
			}
		}
		sql.append(")");
		return sql.toString();
	}

	private String formateDiv(ExpressionNode nd) {
		if (isDivzeroReturnInfinity()) {
			ExpressionNode n1 = nd.getNode(0);
			ExpressionNode n2 = nd.getNode(1);
			String fmstr = n2.formatZz(this, false, nd);
			StringBuffer sql = new StringBuffer(256);
			sql.append("case when ");
			sql.append(fmstr).append("=0");
			sql.append(" then ");
			/**
			 * 除数为0，返回null，如果设置了“null当0处理”，这里也要转换成0；
			 * 原因是有些列间运算，有除因子，如果返回null，则整个计算都返回null，这是不被允许的。
			 */
			if (isNullToZero()) {
				sql.append("0");
			}
			else {
				sql.append("null");
			}
			sql.append(" else ");
			if (n1.getNodeCount() > 0)
				sql.append("(");
			DataBaseInfo db = dialect.getDataBaseInfo();
			/*
			 * db2的除法问题，参看wiki上帖子
			 * http://192.168.1.147:8080/wiki/display/softwaredev/DB2
			 * 需要将分子用double函数转换下；
			 */
			if (db.isDb2()) {
				sql.append("double(");
			}
			sql.append(n1.formatZz(this, false, nd));
			if (db.isDb2()) {
				sql.append(")");
			}
			if (n1.getNodeCount() > 0)
				sql.append(")");
			sql.append(formatZz_Op(nd));
			if (n2.getNodeCount() > 0)
				sql.append("(");
			sql.append(fmstr);
			if (n2.getNodeCount() > 0)
				sql.append(")");
			sql.append(" end ");
			return sql.toString();
		}
		return null;
	}

	private String formatDataOpNode_WithDateCompare(ExpressionNode nd) {
		ExpressionNode n1 = nd.getNode(0);
		ExpressionNode n2 = nd.getNode(1);
		if (isDateExp(n1) && (n2.isConst() || n2.isConstExp())) {
			String v = formatDateNode(nd, n1, n2);
			if (v != null)
				return v;
		}
		if (isDateExp(n2) && (n1.isConst() || n1.isConstExp())) {
			String v = formatDateNode(nd, n2, n1);
			if (v != null)
				return v;
		}
		return null;
	}

	private String formatDateNode(ExpressionNode nd, ExpressionNode n1, ExpressionNode n2) {
		int sqltype = getDataSqlType(n1);
		char dt = SqlFunc.getType(sqltype);
		switch (dt) {
			/**
			 * BI-6484 timestamp类型字段过滤时生成的sql没有优化
			 * SqlFunc.getType中将timestamp、time、date都返回DbDefiner.FIELD_TYPE_DATE。
			 * 由于修改getType返回相应的数据类型，导致bbq()函数的条件处理有问题
			 * 
			 */
			//			case DbDefiner.FIELD_TYPE_TIMESTAMP:
			//			case DbDefiner.FIELD_TYPE_TIME:
			case DbDefiner.FIELD_TYPE_DATE:
				return formatSingleDateTypeCompare(nd, n1, n2, sqltype);
			case 'C':
				return formatCharDateCompare(nd, n1, n2);
			case 'I':
			case 'N':
				return formatIntDateCompare(nd, n1, n2);
		}
		return null;
	}

	/**
	 * bbq()对应日期类型数据期时，条件的处理：
	 * bbq()='20050101'
	 *    (a.SSSQ_Q>=to_date('20050101','YYYYMMDD') and a.SSSQ_Q<to_date('20050102','YYYYMMDD'))
	 * bbq()>='20050101'
	 *    (a.SSSQ_Q>=to_date('20050101','YYYYMMDD'))
	 * bbq() like '200501*' & bbq() like '200501%' & bbq() like '200501??' & bbq() like '200501__'
	 *    (a.SSSQ_Q between to_date('20050101','YYYYMMDD') and to_date('20050131 23:59:59','YYYYMMDD HH24:MI:SS'))
	 * bbq() like '2005*' & bbq() like '2005%' & bbq() like '2005????' & bbq() like '2005____'
	 *    (a.SSSQ_Q between to_date('20050101','YYYYMMDD') and to_date('20051231 23:59:59','YYYYMMDD HH24:MI:SS'))
	 * bbq() like '????01??' &  bbq() like '____01__'
	 *    (to_char(a.SSSQ_Q,'yyyymmdd') like '____01__')
	 * bbq()!='20050102' & bbq()<>'20050102'
	 *    (a.SSSQ_Q>=to_date('20050103','YYYYMMDD') or a.SSSQ_Q<to_date('20050102','YYYYMMDD'))
	 * 
	 * 20090213  以前没考虑的：
	 * bbq()>'20060102'
	 * 以前翻译的sql：a.bbq_>to_date('20060102','yyyymmdd') , 包含了20060102这天；
	 * 正确的sql：   a.bbq_>=to_date('20060103','yyyymmdd')
	 * bbq()<='20060102'
	 * 以前翻译的sql：a.bbq_<=to_date('20060102','yyyymmdd') , 没有包含了20060102这天；
	 * 正确的sql：   a.bbq_<to_date('20060103','yyyymmdd')
	 * 测试用例：
	 * 开发测试--报表模版--数据期相关--日期类型数据期--日期小于等于条件和大于条件
	 * @param nd
	 * @param n1
	 * @param n2
	 * @param dt 
	 * @return
	 */
	private String formatSingleDateTypeCompare(ExpressionNode nd, ExpressionNode n1, ExpressionNode n2, int sqltype) {
		String field = n1.formatZz(this, true, nd);
		String value = evalstr(n2);

		switch (nd.getOp().getIndex()) {
			case ExpFuncOp.OPINDEX_EQUAL:
				return procDateSql(nd, field, value, sqltype);
			case ExpFuncOp.OPINDEX_NOTEQUAL:
			case ExpFuncOp.OPINDEX_NOTEQUAL_C:
				return procDateSql2(nd, field, value, sqltype);
			case ExpFuncOp.OPINDEX_GREAT: {
				return procDateSqlGreat(nd, sqltype, field, value);
			}
			case ExpFuncOp.OPINDEX_LESSEQUAL: {
				return procDateSqlLessEqual(nd, sqltype, field, value);
			}
			case ExpFuncOp.OPINDEX_GREATEQUAL:
			case ExpFuncOp.OPINDEX_LESS: {
				Calendar cal = StrFunc.parseCalendar(value, null);
				if (cal == null) {
					//无法获取日期对象，不优化
					return getFilterSql(nd, field, value);
				}
				return getFilterSql(nd, field, funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			}
			case ExpFuncOp.OPINDEX_LIKE: {
				if (value.matches("[0-9]{8}")) {
					return procDateSqlOne(nd, field, value, sqltype);
				}
				else if (value.matches("[0-9]{6}(\\*|%)?") || value.matches("[0-9]{6}(\\?|_){2}")) {
					StringBuffer sql = new StringBuffer(128);
					String month = value.substring(0, 6);
					int day = getDay(month);
					sql.append(field);
					sql.append(" between ");
					sql.append(funcToDate(getDialect(), month + "01 00:00:00", sqltype));
					sql.append(" and ");
					sql.append(funcToDate(getDialect(), month + day + " 23:59:59", sqltype));
					return sql.toString();
				}
				else if (value.matches("[0-9]{4}(\\*|%)?") || value.matches("[0-9]{4}(\\?|_){4}")) {
					StringBuffer sql = new StringBuffer(128);
					String year = value.substring(0, 4);
					sql.append(field);
					sql.append(" between ");
					sql.append(funcToDate(getDialect(), year + "0101 00:00:00", sqltype));
					sql.append(" and ");
					sql.append(funcToDate(getDialect(), year + "1231 23:59:59", sqltype));
					return sql.toString();
				}
				else {
					return procDateToChar(field, value, "like");
				}

			}
		}
		return null;
	}

	private String procDateSqlLessEqual(ExpressionNode nd, int sqltype, String field, String value) {
		Calendar cal = StrFunc.parseCalendar(value, null);
		if (cal == null) {
			//无法获取日期对象，不优化
			return getFilterSql(nd, field, value);
		}
		/**
		 * 20090921
		 * bbq()<='2008'
		 * 以前的sql：a.bbq_<to_date('20080102','yyyymmdd');
		 * 正确的sql：a.bbq_<to_date('20090101','yyyymmdd');
		 * 
		 * bbq()<='200801'
		 * 以前的sql：a.bbq_<to_date('20080102','yyyymmdd');
		 * 正确的sql：a.bbq_<to_date('20080201','yyyymmdd');
		 * 
		 * 测试用例：
		 * 开发测试/报表模板/数据期相关/日期型数据期/B950023 日期小于等于某年或者某年月
		 */
		if (Pattern.matches("[0-9]{4}(----|0000)?", value)) {
			cal.add(Calendar.YEAR, 1);
		}
		else if (Pattern.matches("[0-9]{4}(-)?[0-9]{2}(--|00)?", value)) {
			cal.add(Calendar.MONTH, 1);
		}
		else {
			/**
			 * bbq()<='20060102'
			 * 以前翻译的sql：a.bbq_<=to_date('20060102','yyyymmdd') , 没有包含了20060102这天；
			 * 正确的sql：   a.bbq_<to_date('20060103','yyyymmdd')
			 */
			cal.add(Calendar.DATE, 1);
		}
		return getFilterSql(nd, field, funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype), "<");
	}

	private String procDateSqlGreat(ExpressionNode nd, int sqltype, String field, String value) {
		Calendar cal = StrFunc.parseCalendar(value, null);
		if (cal == null) {
			//无法获取日期对象，不优化
			return getFilterSql(nd, field, value);
		}
		/**
		 * 20090921
		 * bbq()>'2008'
		 * 以前的sql：a.bbq_>to_date('20080102','yyyymmdd');
		 * 正确的sql：a.bbq_>=to_date('20090101','yyyymmdd');
		 * 
		 * bbq()>'200801'
		 * 以前的sql：a.bbq_>to_date('20080102','yyyymmdd');
		 * 正确的sql：a.bbq_>=to_date('20080201','yyyymmdd');
		 * 
		 * 测试用例：
		 * 开发测试/报表模板/数据期相关/日期型数据期/B956023 日期大于某年或者某年月
		 */
		if (Pattern.matches("[0-9]{4}(----|0000)?", value)) {
			cal.add(Calendar.YEAR, 1);
		}
		else if (Pattern.matches("[0-9]{4}(-)?[0-9]{2}(--|00)?", value)) {
			cal.add(Calendar.MONTH, 1);
		}
		else {
			/**
			 * bbq()>'20060102'
			 * 以前翻译的sql：a.bbq_>to_date('20060102','yyyymmdd') , 包含了20060102这天；
			 * 正确的sql：   a.bbq_>=to_date('20060103','yyyymmdd')
			 */
			cal.add(Calendar.DATE, 1);
		}
		return getFilterSql(nd, field, funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype), ">=");
	}

	private String procDateToChar(String field, String value, String op) {
		StringBuffer sql = new StringBuffer(24);
		sql.append(getDialect().funcDateToChar(field, "yyyymmdd"));
		sql.append(" ").append(op).append(" '");
		value = value.replaceAll("\\*", "%");
		value = value.replaceAll("\\?", "_");
		sql.append(value).append("'");
		return sql.toString();
	}

	/**
	 * 对于将常量放到左边的比较条件，具有方向性，不能简单的把字段变量提到左边；
	 * 此方法用于还原条件表达式；
	 * 比如：@bbq<bbq() 正确的翻译：'200909--'<a.bbq_
	 * @param nd
	 * @param field
	 * @param value
	 * @return
	 */
	private String getFilterSql(ExpressionNode nd, String field, String value) {
		return getFilterSql(nd, field, value, null);
	}

	/**
	 * 有些情况下我们可能改变了表达式中的操作符，操作符就不能从nd中解析，我们可以将更改的操作符直接作为参数传入
	 * 比如：bbq()>'20060102'
	 * 以前翻译的sql：a.bbq_>to_date('20060102','yyyymmdd') , 包含了20060102这天；
	 * 正确的sql：   a.bbq_>=to_date('20060103','yyyymmdd')
	 * @param nd
	 * @param field
	 * @param value
	 * @param oper	改变后的操作符，如果此值不为null则直接用该操作符；如果此值为null则从nd中解析操作符
	 * @return
	*/
	private String getFilterSql(ExpressionNode nd, String field, String value, String oper) {
		StringBuffer sql = new StringBuffer(32);
		if (oper == null) {
			oper = formatZz_Op(nd);
		}

		if (nd.getNode(0).isConstExp()) {
			sql.append(value).append(" ").append(oper).append(" ").append(field);
		}
		else {
			sql.append(field).append(" ").append(oper).append(" ").append(value);
		}
		return sql.toString();
	}

	/**
	 * 返回月的最后一天
	 * @param month  getDay("200609")=30
	 * @return
	 */
	private int getDay(String month) {
		int y = Integer.parseInt(month.substring(0, 4));
		int m = Integer.parseInt(month.substring(4, 6));
		int d = 31;
		if (m == 4 || m == 6 || m == 9 || m == 11) {
			d = 30;
		}
		if (m == 2) {
			if (y % 4 == 0 && y % 100 != 0 || y % 400 == 0) {
				d = 29;
			}
			else {
				d = 28;
			}
		}
		return d;
	}

	private String procDateSql(ExpressionNode nd, String field, String value, int sqltype) {
		//处理多选
		String[] vv = value.split(FormatExpToSqlExp.SPLITOR);
		StringBuffer sql = new StringBuffer(128);
		if (vv.length > 1) {
			boolean mult = false;
			for (int i = 0; i < vv.length; i++) {
				String v = procDateSqlOne(nd, field, vv[i], sqltype);
				if (v != null) {
					if (sql.length() > 0)
						sql.append(" or ");
					sql.append(v);
					if (!mult)
						mult = true;
				}
			}
			if (mult) {
				sql.insert(0, '(');
				sql.append(')');
			}
			return sql.toString();
		}
		return procDateSqlOne(nd, field, value, sqltype);
	}

	private String procDateSqlOne(ExpressionNode nd, String field, String value, int sqltype) {
		StringBuffer sql = new StringBuffer(64);
		Calendar cal = StrFunc.parseCalendar(value, null);
		if (cal == null) {
			//无法获取日期对象，不优化
			return getFilterSql(nd, field, value);
		}
		if (Pattern.matches("[0-9]{4}(----|0000)?", value)) {
			sql.append("(");
			sql.append(field).append(">=");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			cal.add(Calendar.YEAR, 1);
			sql.append(" and ").append(field).append("<");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			sql.append(")");
			return sql.toString();
		}
		if (Pattern.matches("[0-9]{4}(-)?[0-9]{2}(--|00)?", value)) {
			sql.append("(");
			sql.append(field).append(">=");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			int y = Integer.parseInt(value.substring(0, 4));
			int mp = 4;
			if (value.charAt(4) == '-')
				mp = 5;
			int m = Integer.parseInt(value.substring(mp, mp + 2));
			cal.add(Calendar.DATE, getDays(y, m));
			sql.append(" and ").append(field).append("<");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			sql.append(")");
			return sql.toString();
		}
		sql.append("(");
		sql.append(field).append(">=");
		sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
		cal.add(Calendar.DATE, 1);
		sql.append(" and ").append(field).append("<");
		sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
		sql.append(")");
		return sql.toString();
	}

	public int getDays(int y, int m) {
		if (m == 2) {
			if (y % 4 == 0 && y % 100 != 0 || y % 400 == 0) {
				return 29;
			}
			else
				return 28;
		}
		if (m == 1 || m == 3 || m == 5 || m == 7 || m == 8 || m == 10 || m == 12)
			return 31;
		return 30;
	}

	private String procDateSql2(ExpressionNode nd, String field, String value, int sqltype) {
		Calendar cal = StrFunc.parseCalendar(value, null);
		if (cal == null) {
			//无法获取日期对象，不优化
			return getFilterSql(nd, field, value);
		}
		StringBuffer sql = new StringBuffer(128);
		/**
		 * 20090814 BI-2349
		 * 对于日期类型的数据期条件，<> 条件的翻译：
		 * bbq()<>'2009'翻译sql：
		 * (a.DATEBBQ<to_date('20090101 00:00:00','YYYYMMDD HH24:MI:SS') 
		 * or a.DATEBBQ>=to_date('20100101 00:00:00','YYYYMMDD HH24:MI:SS') 
		 * or a.DATEBBQ is null)
		 * bbq()<>'200802' 翻译sql：
		 * (a.DATEBBQ<to_date('20080201 00:00:00','YYYYMMDD HH24:MI:SS') 
		 * or a.DATEBBQ>=to_date('20080301 00:00:00','YYYYMMDD HH24:MI:SS') 
		 * or a.DATEBBQ is null)
		 * 
		 * 测试用例：开发测试-报表模板-数据期相关-日期型数据期-日期类型不等于条件
		 */
		if (Pattern.matches("[0-9]{4}(----|0000)?", value)) {
			sql.append("(");
			sql.append(field).append("<");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			cal.add(Calendar.YEAR, 1);
			sql.append(" or ").append(field).append(">=");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			sql.append(" or ").append(field).append(" is null");
			sql.append(")");
			return sql.toString();
		}
		if (Pattern.matches("[0-9]{4}(-)?[0-9]{2}(--|00)?", value)) {
			sql.append("(");
			sql.append(field).append("<");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			int y = Integer.parseInt(value.substring(0, 4));
			int mp = 4;
			if (value.charAt(4) == '-')
				mp = 5;
			int m = Integer.parseInt(value.substring(mp, mp + 2));
			cal.add(Calendar.DATE, getDays(y, m));
			sql.append(" or ").append(field).append(">=");
			sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
			sql.append(" or ").append(field).append(" is null");
			sql.append(")");
			return sql.toString();
		}
		sql.append("(").append(field).append("<");
		sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
		sql.append(" or ").append(field).append(">=");
		cal.add(Calendar.DATE, 1);
		sql.append(funcToDate(getDialect(), StrFunc.date2str(cal, "yyyymmdd"), sqltype));
		sql.append(" or ").append(field).append(" is null");
		sql.append(")");
		return sql.toString();
	}

	private String funcToDate(Dialect dl, String date2str, int sqltype) {
		return dl.funcToSqlConst(date2str, sqltype);
	}

	private String formatIntDateCompare(ExpressionNode nd, ExpressionNode n1, ExpressionNode n2) {
		String value = evalstr(n2);
		//获得数据库中该日期类型 数值 字段值的日期格式；
		String style = getDateStrStyle(n1);

		String field = n1.formatZz(this, true, nd);
		int t = nd.getOp().getIndex();
		switch (t) {
			case ExpFuncOp.OPINDEX_LIKE: {
				value = value.replaceAll("\\*", "%");
				value = value.replaceAll("\\?", "_");

				if (value.matches("[0-9]+%") && style != null && style.length() > 0) {
					String v = formatNumDateLike(value, style, field);
					if (v != null)
						return v;
				}
				return getDialect().funcAsStr(field) + " " + nd.getOp().toString() + " '" + value + "'";
			}
			case ExpFuncOp.OPINDEX_EQUAL: {
				//多选值
				return formatNumDate(nd, value, style, field);
			}
			default: {
				/**
				 * 处理形如xxb.bbqint>'200701--' 的条件，xxb.bbqint是int类型，格式：yyyymm
				 * 解析sql需要把右边的常量200701--转换成200701
				 */
				if (style != null)
					value = StrFunc.convertDateFormat(value, style);
				return getFilterSql(nd, field, value);
			}
		}
	}

	private String formatNumDateLike(String value, String style, String field) {
		//形如： bbq() like '2004%'
		//转换成： a.bbq_>=20040000 and a.bbq_<20049999
		int len = style.length();
		String v = value.substring(0, value.indexOf("%"));
		if (v.length() < len) {
			int p = len - v.length();
			StringBuffer formatstr = new StringBuffer(32);
			formatstr.append('(');
			formatstr.append(field);
			formatstr.append(">=").append(v);
			for (int i = 0; i < p; i++) {
				formatstr.append("0");
			}
			formatstr.append(" and ");
			formatstr.append(field).append("<").append(v);
			for (int i = 0; i < p; i++) {
				formatstr.append("9");
			}
			formatstr.append(')');
			return formatstr.toString();
		}
		else {
			return field + "=" + v.substring(0, len);
		}
	}

	private String formatNumDate(ExpressionNode nd, String value, String style, String field) {
		String bvs[] = value.split(FormatExpToSqlExp.SPLITOR);
		StringBuffer sql = new StringBuffer(64);
		boolean mult = false;
		for (int i = 0; i < bvs.length; i++) {
			String v = formatNumberDateOne(nd, field, bvs[i], style);
			if (v == null)
				continue;
			if (sql.length() > 0) {
				sql.append(" or ");
				if (!mult)
					mult = true;
			}
			sql.append(v);
		}
		if (sql.length() == 0)
			return null;
		if (mult) {
			sql.insert(0, '(');
			sql.append(')');
		}
		return sql.toString();
	}

	private String formatNumberDateOne(ExpressionNode nd, String field, String value, String style) {
		if (style == null) {
		}
		else if (style.matches("(?i)yyyy")) {
			if (value.length() >= 4) {//2007,2007----,20070000
				return field + "=" + value.substring(0, 4);
			}
		}
		else if (style.matches("(?i)yyyymm")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				StringBuffer sql = new StringBuffer(32);
				String v = value.substring(0, 4);
				sql.append("(").append(field).append(">=").append(v).append("00");
				sql.append(" and ").append(field).append("<=").append(v).append("99)");
				return sql.toString();
			}
			else if (value.matches("[0-9]{6}.*")) {
				return field + "=" + value.substring(0, 6);
			}
		}
		else if (style.matches("(?i)yyyymmdd")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				StringBuffer sql = new StringBuffer();
				String v = value.substring(0, 4);
				sql.append("(").append(field).append(">=").append(v).append("0000");
				sql.append(" and ").append(field).append("<=").append(v).append("9999)");
				return sql.toString();
			}
			else if (value.matches("[0-9]{4}(-)?[0-9]{2}(--|00)?")) {
				StringBuffer sql = new StringBuffer();
				String v = value.substring(0, 4);
				if (value.charAt(4) == '-')
					v += value.substring(5, 7);
				else
					v += value.substring(4, 6);
				sql.append("(").append(field).append(">=").append(v).append("00");
				sql.append(" and ").append(field).append("<=").append(v).append("99)");
				return sql.toString();
			}
			else if (value.matches("[0-9]{8}.*")) {
				return field + "=" + value.substring(0, 8);
			}
		}
		//将输入的日期 数值 值转换为需要的字符格式；
		if (style != null)
			value = StrFunc.convertDateFormat(value, style);
		return field + nd.getOp().toString() + value;
	}

	private String formatCharDateCompare(ExpressionNode nd, ExpressionNode n1, ExpressionNode n2) {
		String value = evalstr(n2);
		String bbqfield = n1.formatZz(this, true, nd);
		if (nd.getOp().getIndex() == ExpFuncOp.OPINDEX_LIKE) {
			value = value.replaceAll("\\*", "%");
			value = value.replaceAll("\\?", "_");
			value = value.replaceAll("----", "%");
			value = value.replaceAll("--", "%");
			return bbqfield + " " + formatZz_Op(nd) + " " + "'" + value + "'";
		}
		else {
			if (value == null || value.length() == 0)
				return bbqfield + formatZz_Op(nd) + "''";
			//获得数据库中该日期类型 字符 字段值的日期格式；
			String style = getDateStrStyle(n1);
			if (nd.getOp().getIndex() == ExpFuncOp.OPINDEX_EQUAL) {
				//多选值
				String bvs[] = value.split(FormatExpToSqlExp.SPLITOR);
				StringBuffer sql = new StringBuffer(32);
				boolean mult = false;
				for (int i = 0; i < bvs.length; i++) {
					String v = formatCharDateOne(nd, bbqfield, bvs[i], style);
					if (v == null)
						continue;
					if (sql.length() > 0) {
						sql.append(" or ");
						if (!mult)
							mult = true;
					}
					sql.append(v);
				}
				if (sql.length() > 0) {
					if (mult) {
						sql.insert(0, '(');
						sql.append(')');
					}
					return sql.toString();
				}
			}
			//将输入的日期 字符 值转换为需要的字符格式；
			if (style != null)
				value = StrFunc.convertDateFormat(value, style);
			//处理形如bbq()>'200801'的表达式

			return getFilterSql(nd, bbqfield, "'" + value + "'");
		}
	}

	/**
	 * 处理单个=表达式
	 * bbq()='2007'
	 * bbq()='2007----'
	 * bbq()='20070000'
	 *      --->  a.bbq_ like '2007%'
	 * bbq()='200701'
	 * bbq()='200701--'
	 * bbq()='20070100'
	 *      --->  a.bbq_ like '200701%'
	 * bbq()='20070101'
	 *      --->  a.bbq_='20070101'
	 * 根据bbq()的格式不同，生成sql也不同；
	 * yyyy----,yyyymm--,yyyymmdd,yyyy-mm,yyyy-mm-dd
	 * @param nd
	 * @param n1
	 * @param value
	 * @param style
	 * @return
	 */
	private String formatCharDateOne(ExpressionNode nd, String bbqfield, String value, String style) {

		if (style == null) {
		}
		else if (style.matches("(?i)yyyy")) {
			if (value.length() >= 4) {//2007,2007----,20070000
				return bbqfield + "='" + value.substring(0, 4) + "'";
			}
		}
		else if (style.matches("(?i)yyyy(----|0000)?")) {
			if (value.length() >= 4) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
		}
		else if (style.matches("(?i)yyyymm(--|00)")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
			else if (value.matches("[0-9]{6}.*")) {//200701--,200702,20070200
				return bbqfield + " like '" + value.substring(0, 6) + "%'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}.*")) {//2007-01-00,2007-01
				String v = value.substring(0, 4) + value.substring(5, 7);
				return bbqfield + " like '" + v + "%'";
			}
		}
		else if (style.matches("(?i)yyyymm")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
			else if (value.matches("[0-9]{6}.*")) {//200701--,200702,20070200
				return bbqfield + "='" + value.substring(0, 6) + "'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}.*")) {//2007-01-00,2007-01
				String v = value.substring(0, 4) + value.substring(5, 7);
				return bbqfield + "='" + v + "'";
			}
		}
		else if (style.matches("(?i)yyyy-mm")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
			else if (value.matches("[0-9]{6}.*")) {//200701--,200702,20070200
				String v = value.substring(0, 4) + "-" + value.substring(4, 6);
				return bbqfield + " like '" + v + "%'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}.*")) {//2007-01-00,2007-01
				return bbqfield + " like '" + value.substring(0, 7) + "%'";
			}
		}
		else if (style.matches("(?i)yyyymmdd")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
			else if (value.matches("[0-9]{6}(--|00)?")) {//200701--,200702,20070200
				return bbqfield + " like '" + value.substring(0, 6) + "%'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}(-00)?")) {//2007-01-00,2007-01
				String v = value.substring(0, 4) + value.substring(5, 7);
				return bbqfield + " like '" + v + "%'";
			}
			else if (value.matches("[0-9]{8}.*")) {//20070101
				return bbqfield + "='" + value.substring(0, 8) + "'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*")) {//2007-01-01
				String v = value.substring(0, 4) + value.substring(5, 7) + value.substring(8, 10);
				return bbqfield + "='" + v + "'";
			}
		}
		else if (style.matches("(?i)yyyy-mm-dd")) {
			if (value.matches("[0-9]{4}(----|0000)?")) {//2007,2007----,20070000
				return bbqfield + " like '" + value.substring(0, 4) + "%'";
			}
			else if (value.matches("[0-9]{6}(--|00)?")) {//200701--,200702,20070200
				String v = value.substring(0, 4) + "-" + value.substring(4, 6);
				return bbqfield + " like '" + v + "%'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}(-00)?")) {//2007-01-00,2007-01
				return bbqfield + " like '" + value.substring(0, 7) + "%'";
			}
			else if (value.matches("[0-9]{8}.*")) {//20070101
				String v = value.substring(0, 4) + "-" + value.substring(4, 6) + "-" + value.substring(6, 8);
				return bbqfield + "='" + v + "'";
			}
			else if (value.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*")) {//2007-01-01
				return bbqfield + "='" + value.substring(0, 10) + "'";
			}
		}
		return null;
	}

	private boolean isDate(ExpressionNode n1, ExpressionNode n2) {
		return isDateExp(n1) || isDateExp(n2);
	}

	/**
	 * 将比较符两边的类型转为一致（这里不包括日期类型），以数据库字段的类型为基准；
	 * 比如： xxb.a2<>0 xxb.a2是字符 转换为：xxb.a2<>'0'
	 *       xxb.a3>'3'  xxb.a3是数字 转换为： xxb.a3<>3
	 * @param nd
	 * @return
	 */
	private String formatDifferentTypes(ExpressionNode nd) {
		switch (nd.getOp().getIndex()) {
			case ExpFuncOp.OPINDEX_NOTEQUAL_C:
			case ExpFuncOp.OPINDEX_NOTEQUAL:
			case ExpFuncOp.OPINDEX_IS:
			case ExpFuncOp.OPINDEX_EQUAL:
			case ExpFuncOp.OPINDEX_GREAT:
			case ExpFuncOp.OPINDEX_GREATEQUAL:
			case ExpFuncOp.OPINDEX_LESS:
			case ExpFuncOp.OPINDEX_LESSEQUAL:
				/**
				 * BI-4440 20110329
				 * 对于形如 xxx.dm like @p+'%' 的表达式，处理是需要对常量参数加单引号；
				 */
			case ExpFuncOp.OPINDEX_LIKE: {
				ExpressionNode n1 = nd.getNode(0);
				ExpressionNode n2 = nd.getNode(1);
				if (!isConstNotNull(n1) && isConstNotNull(n2)) {
					return formatDifferentTypes(nd, n1, n2);
				}
				else if (isConstNotNull(n1) && !isConstNotNull(n2)) {
					return formatDifferentTypes(nd, n2, n1);
				}
			}
		}

		return null;
	}

	private String formatDifferentTypes(ExpressionNode nd, ExpressionNode n1, ExpressionNode n2) {
		char t1 = getDataType(n1);
		String n2value = evalstr(n2);

		switch (t1) {
			case ExpUtil.TOINT:
			case ExpUtil.TOFLT: {
				if (nd.getOp().getIndex() == ExpFuncOp.OPINDEX_LIKE) {
					return getDialect().funcAsStr(n1.formatZz(this, true, nd)) + " " + formatZz_Op(nd) + " '" + n2value
							+ "'";
				}
				/**
				 * 20090727
				 * 对于数值型字段的<>条件处理，同下面的字符类型字段；
				 */
				if (nd.getOp().getIndex() == ExpFuncOp.OPINDEX_NOTEQUAL
						|| nd.getOp().getIndex() == ExpFuncOp.OPINDEX_NOTEQUAL_C) {
					String field = n1.formatZz(this, true, nd);
					return "(" + field + formatZz_Op(nd) + n2value + " or " + field + " is null)";
				}

				return getFilterSql(nd, n1.formatZz(this, true, nd), n2value);
			}
			case ExpUtil.TOSTR: {
				/**
				 * 20090727
				 * 由于sql中null和任何值运算后都返回false，所以对xxb.zb<>'001'的条件来说，xxb.zb为空是不在条件允许范围内的；
				 * 这对于业务逻辑来说是不对的，空值应该也是<>的范围；
				 * 这里将<>的条件使用or加上为空的条件；
				 * 例：xxb.zb<>'001' 翻译为： xxb.zb<>'001' or xxb.zb is null
				 * 测试用例：开发测试-报表模板-过滤条件-<>条件解析
				 */
				if (nd.getOp().getIndex() == ExpFuncOp.OPINDEX_NOTEQUAL
						|| nd.getOp().getIndex() == ExpFuncOp.OPINDEX_NOTEQUAL_C) {
					String field = n1.formatZz(this, true, nd);
					return "(" + field + formatZz_Op(nd) + "'" + n2value + "' or " + field + " is null)";
				}

				return getFilterSql(nd, n1.formatZz(this, true, nd), "'" + n2value + "'");
			}
		}
		return null;
	}

	private boolean isConstNotNull(ExpressionNode nd) {
		return (nd.isConst() || nd.isConstExp()) && !nd.isConstNull();
	}

	/**
	 * '' and xxb.a1='1'  --->  xxb.a1='1'
	 * '' or xxb.a1='1'  --->  xxb.a1='1'
	 * @param nd
	 * @return
	 */
	private String formatCondition(ExpressionNode nd) {
		ExpressionNode n1 = nd.getNode(0);
		ExpressionNode n2 = nd.getNode(1);
		if ((n1.isConst() || n1.isConstExp()) && (!n2.isConst() && !n2.isConstExp())) {
			String v = evalstr(n1);
			if (v == null || v.trim().length() == 0) {
				return n2.formatZz(this, true, nd);
			}
		}
		if ((n2.isConst() || n2.isConstExp()) && (!n1.isConst() && !n1.isConstExp())) {
			String v = evalstr(n2);
			if (v == null || v.trim().length() == 0) {
				return n1.formatZz(this, true, nd);
			}
		}
		return null;
	}

	private String formatZz_op_case(ExpressionNode nd) {
		ExpressionNode n = nd.getNode(0);
		if (n.isConstExp())
			if (evalbool(n)) {
				return nd.getNode(1).formatZz(this, true, nd);
			}
			else if (nd.getNodeCount() == 3) { // case when xx then xxx else xxxx end  类似if的处理。
				return nd.getNode(2).formatZz(this, true, nd);
			}
		return null;
	}

	private String formatOpNode_WithDate_between(ExpressionNode nd) {
		ExpressionNode n1 = nd.getNode(0);
		ExpressionNode n2 = nd.getNode(1);
		ExpressionNode n3 = nd.getNode(2);
		StringBuffer sb = new StringBuffer(64);
		sb.append(n1.formatZz(this, true, nd));
		sb.append(" BETWEEN ");
		if (n2.isConst() || n2.isConstExp()) {
			sb.append(quateSqlConstDate(n2));
		}
		else if (n2.isFunc()) {
			sb.append(n2.formatZz(this, true, nd));
		}

		sb.append(" AND ");
		if (n3.isConst() || n3.isConstExp()) {
			sb.append(quateSqlConstDate(n3));
		}
		else if (n3.isFunc()) {
			sb.append(n3.formatZz(this, true, nd));
		}
		return sb.toString();
	}

	private String formatOpNode_WithDateCompare(ExpressionNode nd) {
		ExpressionNode n1 = nd.getNode(0);
		ExpressionNode n2 = nd.getNode(1);
		if (n1.isData() && n1.getReturnType() == ExpUtil.TODAT && (n2.isConst() || n2.isConstExp())) {
			return n1.formatZz(this, true, nd) + formatZz_Op(nd) + quateSqlConstDate(n2);
		}
		if (n2.isData() && n2.getReturnType() == ExpUtil.TODAT && (n1.isConst() || n1.isConstExp())) {
			return quateSqlConstDate(n1) + formatZz_Op(nd) + n2.formatZz(this, true, nd);
		}
		return null;
	}

	private String quateSqlConstDate(ExpressionNode n2) {
		Calendar c = StrFunc.parseCalendar(evalstr(n2), null);
		if (c == null)
			return null;
		return dialect.funcToDate(StrFunc.date2str(c, "yyyymmdd"));
	}

	private String formatFuncNode(ExpressionNode nd) {
		switch (nd.getFunc().getIndex()) {
			case ExpFuncOp.FUNCINDEX_LEFT: {
				return dialect.funcLeft(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_RIGHT: {
				return dialect.funcRight(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_MID: {
				return dialect.funcMid(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1),
						nd.getNodeCount() == 3 ? formatZzFuncNode(nd, 2) : null);
			}
			case ExpFuncOp.FUNCINDEX_CHAR: {
				return dialect.funcChar(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_TRIM: {
				return dialect.funcTrim(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_LOWER: {
				return dialect.funcLower(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_UPPER: {
				return dialect.funcUpper(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_SUBSTITUTE: {
				return dialect.funcWholeReplace(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1),
						formatZzFuncNode(nd, 2));
			}
			case ExpFuncOp.FUNCINDEX_IF: {
				ExpressionNode n = nd.getNode(0);
				/**
				 * 20100329 BI-3253
				 * 解析形如：if(@p=1,xxb.hy_dm like 'A%') ，由于参数不够，出现越界异常；
				 * 程序改动后，生成的sql不正确：(IF(1>2,a.HY_DM LIKE 'A%'))
				 * 原因是系统认为这个if表达式是个常量表达式；
				 * 解决办法：使用宏来写表达式：<#=if(@p=1,"xxb.hy_dm like 'A%'")#>
				 */
				if (n.isConstExp())
					return evalbool(n) ? formatZzFuncNode(nd, 1) : nd.getNodeCount() > 2 ? formatZzFuncNode(nd, 2)
							: null;
				return dialect.funcIf(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1),
						nd.getNodeCount() > 2 ? formatZzFuncNode(nd, 2) : null);
			}
			case ExpFuncOp.FUNCINDEX_PRODUCT: {
				return calOp(nd, "*");
			}
			case ExpFuncOp.FUNCINDEX_INT: {
				return dialect.funcInt(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_ABS: {
				return dialect.funcAbs(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_FIND: {
				return dialect.funcFind(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_SEARCH: {
				return dialect.funcSearch(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_LEN: {
				return dialect.funcLen(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_CODE: {
				return dialect.funcCode(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_MOD: {
				return dialect.funcMod(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_SQRT: {
				return dialect.funcSqrt(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_POWER: {
				return dialect.funcPower(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_SIGN: {
				return dialect.funcSign(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_ROUND: {
				return dialect.funcRound(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_LOG: {
				return dialect.funcLog(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_LN: {
				return dialect.funcLn(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_EXP: {
				return dialect.funcExp(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_TRUNC: {
				return dialect.funcTrunc(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_SIN: {
				return dialect.funcSin(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_TAN: {
				return dialect.funcTan(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_COS: {
				return dialect.funcCos(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_PI: {
				return dialect.funcPi();
			}
			case ExpFuncOp.FUNCINDEX_TODAY: {
				return dialect.funcToday();
			}
			case ExpFuncOp.FUNCINDEX_DATE: {
				return dialect.funcDate(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1), formatZzFuncNode(nd, 2));
			}
			case ExpFuncOp.FUNCINDEX__A: {
				return "avg(" + formatZzFuncNode(nd, 0) + ")";
			}
			case ExpFuncOp.FUNCINDEX_SUM:
			case ExpFuncOp.FUNCINDEX__S: {
				return "sum(" + calOp(nd, "+") + ")";
			}
			case ExpFuncOp.FUNCINDEX__N: {
				if (nd.getNodeCount() == 0)
					return "count(*)";
				if (nd.getNodeCount() > 0) {
					StringBuffer sql = new StringBuffer("count(");
					if (nd.getNodeCount() == 2) {
						if (evalbool(nd.getNode(1))) {
							sql.append("distinct ");
						}
					}
					sql.append(formatZzFuncNode(nd, 0)).append(")");
					return sql.toString();
				}
				break;
			}
			case ExpFuncOp.FUNCINDEX__MAX: {
				return "max(" + formatZzFuncNode(nd, 0) + ")";
			}
			case ExpFuncOp.FUNCINDEX__MIN: {
				return "min(" + formatZzFuncNode(nd, 0) + ")";
			}
			case ExpFuncOp.FUNCINDEX_YEAR: {
				ExpressionNode nd0 = nd.getNode(0);
				return dialect.funcYear(getDateFeild(nd0, nd));
			}
			case ExpFuncOp.FUNCINDEX_MONTH: {
				ExpressionNode nd0 = nd.getNode(0);
				return dialect.funcMonth(getDateFeild(nd0, nd));
			}
			case ExpFuncOp.FUNCINDEX_DAY: {
				ExpressionNode nd0 = nd.getNode(0);
				return dialect.funcDay(getDateFeild(nd0, nd));
			}
			case ExpFuncOp.FUNCINDEX_DATETOSTR: {
				String format = nd.getNodeCount() > 1 ? evalstr(nd.getNode(1)) : "YYYYMMDD";
				return dialect.funcDateToChar(formatZzFuncNode(nd, 0), format);
			}
			case ExpFuncOp.FUNCINDEX_STRTODATE: {
				return dialect.funcCharToDate(formatZzFuncNode(nd, 0), evalstr(nd.getNode(1)));
			}
			case ExpFuncOp.FUNCINDEX_DAYS: {
				ExpressionNode nd1 = nd.getNode(0);
				ExpressionNode nd2 = nd.getNode(1);
				String datestr1 = conver2date(nd, nd1);
				String datestr2 = conver2date(nd, nd2);
				/**
				 * 20091104
				 * days函数包含两头天数，sql表达式里面有+1操作，这里最好返回带括号的sql表达式；
				 * 测试用例：
				 * 开发测试专用/开发测试/报表模板/算子/B74307 Days函数解析
				 */
				return "(" + dialect.funcDays(datestr1, datestr2) + ")";
			}
			case ExpFuncOp.FUNCINDEX_SECONDS: {
				ExpressionNode nd1 = nd.getNode(0);
				ExpressionNode nd2 = nd.getNode(1);
				String datestr1 = conver2datetime(nd, nd1);
				String datestr2 = conver2datetime(nd, nd2);
				return dialect.funcSeconds(datestr1, datestr2);
			}
			case ExpFuncOp.FUNCINDEX_C: {
				//				throw new RuntimeException(" SQL 不支持组合函数：C(int,int);");
				throw new RuntimeException(I18N.getString("com.esen.jdbc.formatexptosqlexp.unsupportsql",
						"SQL 不支持组合函数：C(int,int);"));
			}
			case ExpFuncOp.FUNCINDEX_NOW: {
				return dialect.funcNow();
			}
			case ExpFuncOp.FUNCINDEX_STDEV: {
				return dialect.funcStdev(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_ASINT: {
				return dialect.funcAsInt(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_ASNUM: {
				return dialect.funcAsNum(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_ASSTR: {
				return dialect.funcAsStr(formatZzFuncNode(nd, 0));
			}
			case ExpFuncOp.FUNCINDEX_STRCAT: {
				return dialect.funcStrCat(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_IFNULL: {
				return dialect.ifNull(formatZzFuncNode(nd, 0), formatZzFuncNode(nd, 1));
			}
			case ExpFuncOp.FUNCINDEX_OD: {
				/**
				 * 	BI-5944 支持使用OD函数，但是仅支持y/m/d的+/-操作
				 */
				return formatOD(nd);
			}
		}
		return null;
	}

	private String formatOD(ExpressionNode nd) {
		String option = evalstr(nd.getNode(1));
		if (StrFunc.isNull(option)) {
			return null;
		}
		option = option.replaceAll(" ", "").replaceAll("＝", "=").toLowerCase();
		if (StrFunc.isNull(option)) {
			return null;
		}
		//BI-6475： OD 函数合法性检查
		if (option.matches("[y|m|d][+|-]?\\d+") != true) {
			//			throw new RuntimeException("OD函数仅支持ymd的+-");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.formatexptosqlexp.odymd",
					"OD函数仅支持ymd的+-"));
		}
		char ymd = 'd';
		int offset = 0;
		String offsetStr = option.substring(1);
		int index = offsetStr.startsWith("+") ? 1 : 0;
		offset = StrFunc.parseInt(offsetStr.substring(index), 0);
		if (option.charAt(0) == 'y') {
			ymd = 'y';
		}
		else if (option.charAt(0) == 'm') {
			ymd = 'm';
		}
		else if (option.charAt(0) == 'd') {
			ymd = 'd';
		}
		else {
			return null;
		}
		return dialect.formatOffsetDate(formatZzFuncNode(nd, 0), offset, ymd);
	}

	/**
	 * 对year,month,day函数翻译sql时，对应参数必须是日期类型；
	 * 这里对参数进行格式化，将非日期类型转换成日期类型；
	 * 
	 * @param nd0
	 *        需要转换的参数节点；
	 * @param pnd
	 *        year,month,day函数节点
	 * @return
	 */
	private String getDateFeild(ExpressionNode nd0, ExpressionNode pnd) {
		char ct = getDataType(nd0);
		if (ct == 'D') {
			return nd0.formatZz(this, true, pnd);
		}
		else if (ct == 'C') {
			String style = this.getDateStrStyle(nd0);
			return dialect.funcCharToDate(nd0.formatZz(this, true, pnd), style);
		}
		else if (ct == 'I' || ct == 'N') {
			String style = this.getDateStrStyle(nd0);
			String bbqfield = nd0.formatZz(this, true, pnd);
			if (ct == 'N') {
				bbqfield = dialect.funcTrunc(bbqfield, "0");
			}
			return dialect.funcCharToDate(dialect.funcAsStr(bbqfield), style);
		}
		return nd0.formatZz(this, true, pnd);
	}

	private String formatZzFuncNode(ExpressionNode nd, int i) {
		return nd.getNode(i).formatZz(this, true, nd);
	}

	private String conver2date(ExpressionNode pnd, ExpressionNode nd) {
		return conver2date(pnd, nd, Types.DATE);
	}

	private String conver2datetime(ExpressionNode pnd, ExpressionNode nd) {
		return conver2date(pnd, nd, Types.TIMESTAMP);
	}

	/**
	 * 将nd转换为指定的数据库类型t；
	 * @param pnd
	 * @param nd
	 * @param t
	 * @return
	 */
	private String conver2date(ExpressionNode pnd, ExpressionNode nd, int t) {
		if (nd.isConst() || nd.isConstExp()) {
			return dialect.funcToSqlConst(evalstr(nd), t);
		}
		else {
			int dt = getDataSqlType(nd);
			String style = getDateStrStyle(nd);
			return dialect.funcToSqlVar(nd.formatZz(this, true, pnd), dt, t, style);
		}
	}

	protected String calOp(ExpressionNode nd, String op) {
		StringBuffer s = new StringBuffer(64);
		for (int i = 0; i < nd.getNodeCount(); i++) {
			if (s.length() > 0)
				s.append(op);
			String str = nd.getNode(i).formatZz(this, true, nd);
			if (str != null && str.length() > 0)
				s.append(str);
		}
		if (s.length() > 0)
			return s.toString();
		else
			return null;
	}

	public String formatNodeItSelf(ExpressionNode nd) {
		if (nd.isOperator()) {
			switch (nd.getOp().getIndex()) {
				case ExpFuncOp.OPINDEX_AND:
					return " AND ";
				case ExpFuncOp.OPINDEX_OR:
					return " OR ";
				case ExpFuncOp.OPINDEX_STRCONCAT:
					return getStrConcatOp();
				case ExpFuncOp.OPINDEX_NOT:
				/*
				 * BUG:ESENFACE-786: modified by liujin 2014.05.20
				 * 对用表达式中的 !，转换到SQL中使用 NOT
				 */
				case ExpFuncOp.OPINDEX_NOT_C:
					return " NOT ";
				case ExpFuncOp.OPINDEX_PLUS:
					return formatNodeItSelf_plus(nd);
				case ExpFuncOp.OPINDEX_EQUAL:
					return formatNodeIsNull(nd);
				case ExpFuncOp.OPINDEX_NOTEQUAL:
				case ExpFuncOp.OPINDEX_NOTEQUAL_C:
					return "<>";
				case ExpFuncOp.OPINDEX_IS:
					return formateNodeNoNull(nd);

			}
		}
		return null;
	}

	private String getStrConcatOp() {
		DataBaseInfo db = getDialect().getDataBaseInfo();
		if (db.isMssql())
			return "+";//SQL Server 字符链接符号是 +
		/*
		 * 2013.1.7 by wandj mysql字符连接是 CONCAT函数
		 * 2013.1.16 by wandj CONCAT函数中一个值为null时,相加结果为null,改用CONCAT_WS函数
		 * */
	    /* 
	     * ESENBI-3456: modify by liujin 2014.12.15
	     * MySQL 的 concat_ws 函数的返回值存在为 blob 类型的情况，需要进行数据类型转换
	     */
		if(db.isMysql()) 
			return "CAST(CONCAT_WS('',{0},{1}) AS CHAR)";
		return "||";
	}

	/**
	 * a is b --> a=b
	 * a is not b --> a<>b
	 * @param nd
	 * @return
	 */
	private String formateNodeNoNull(ExpressionNode nd) {
		ExpressionNode n1 = nd.getNode(0);
		ExpressionNode n2 = nd.getNode(1);
		if (!n1.isConstNull() && !n2.isConstNull()) {
			if (!n2.isOperator() || n2.getFunc().getIndex() != ExpFuncOp.OPINDEX_NOT) {
				return "=";
			}
		}
		return null;
	}

	/**
	 * 格式化操作符
	 * @param nd
	 * @return
	 */
	public String formatZz_Op(ExpressionNode nd) {
		return nd.formatZz_opOrFuncName(this);
	}

	private boolean isNullValue(ExpressionNode nd) {
		return nd.isConstNull();
	}

	private String formatNodeIsNull(ExpressionNode nd) {
		ExpressionNode nd1 = nd.getNode(1);
		if (isNullValue(nd1)) {
			return " IS ";
		}
		return null;
	}

	/**
	 * 判断节点是否返回字符串,如果是操作符，只循环返回一级
	 * ISSUE:BI-8177 add by jzp 2013.3.28
	 * 此方法改为保护方法，方便继承类进行重载
	 * @param nd
	 * @return
	 */
	protected boolean isNodeRetrunString(ExpressionNode nd){
		if (nd.isReturnString()){
			return true;
		}
		if (nd.isOperator() && nd.getReturnType()==ExpUtil.TOVAR){
			for(int i=0;i<nd.getNodeCount();++i){
				ExpressionNode subnd = nd.getNode(i);
				if (subnd.isReturnString()) 
					return true;
			}
		}
		return false;
	}
	
	private String formatNodeItSelf_plus(ExpressionNode nd) {
		/**
		 * ISSUE:BI-8177 add by jzp 2013.3.5 
		 * 对排序表元A3+B3+C3 翻译错误，当+号超过2个时导致翻译的结果为A3||B3+C3
		 */
		if (isNodeRetrunString(nd.getNode(0)) || isNodeRetrunString(nd.getNode(1))) {
			return getStrConcatOp();
		}
		return null;
	}

	/**
	 * 将表达式返回类型转换成sql字段的类型；
	 * 只支持字符，数值，日期类型的转换，其他类型直接返回其类型；
	 * @param returnType
	 * @return
	 */
	public static int expType2SqlType(char returnType) {
		switch (returnType) {
			case ExpUtil.TOSTR:
				return Types.VARCHAR;
			case ExpUtil.TOFLT:
				return Types.FLOAT;
			case ExpUtil.TOINT:
				return Types.INTEGER;
			case ExpUtil.TODAT:
				return Types.DATE;
		}
		/**
		 * 20090217
		 * 原来的程序返回字符类型，当处理形如：nvl(a.field,0)>0.1 时，
		 * 会认为nvl(a.field,0)是字符类型，生成sql：nvl(a.field,0)>'0.1'，这是错误的；
		 * 原因是nvl是oracle函数，系统识别为变体函数；
		 * 所以这里不能返回字符，应该直接返回变体函数类型；
		 */
		return returnType;
	}

	/**
	 * 对表达式的数据库类型聚集成表达式的返回类型；
	 * @param nd
	 * @return
	 */
	public char getDataType(ExpressionNode nd) {
		return SqlFunc.getType(getDataSqlType(nd));
	}

	/**
	 * 获得表达式翻译成数据库表达式后返回的数据类型；
	 * 父类重载
	 * 例：
	   nd               nd.getReturnType()       getDataSqlType(nd)     getDataType(nd)
	  xxb.bbq_             'D'                   Types.Char                   'D'
	  xxb.inputedate       'D'                   Types.Date                   'D'
	  dim(bbq(),'季')      '*'                    Types.Int                    'I'
	  dim(xxb.hy_dm,'hy_dl')   '*'              Types.Int(hy_dl对于字段类型)     'I'
	  ...
	 * @param nd 
	 * @return
	 */
	public int getDataSqlType(ExpressionNode nd) {
		if (nd.isFunc()) {
			int t = nd.getFunc().getIndex();
			switch (t) {
				case ExpFuncOp.FUNCINDEX_MONTH:
				case ExpFuncOp.FUNCINDEX_DAY:
				case ExpFuncOp.FUNCINDEX_YEAR: {
					/**
					 * 20100906
					 * 对于year,month,day函数，翻译sql的返回值类型，除了oracle是通过to_char实现是字符类型外，其他数据库都有同名函数，返回整型；
					 */
					if (this.getDialect().getDataBaseInfo().isOracle()) {
						return Types.VARCHAR;
					}
					else {
						return Types.INTEGER;
					}
				}
			}
		}
		return SqlFunc.expType2SqlType(nd.getReturnType());
	}

	/**
	 * 判断表达式是否是日期表达式
	 * 例：bbq(),xxb.bbq_
	 * 它对应的数据库类型可能是字符，数字，日期类型
	 * 父类重载
	 * @param nd
	 * @return
	 */
	public boolean isDateExp(ExpressionNode nd) {
		return nd.getReturnType() == ExpUtil.TODAT;
	}

	/**
	 *  返回日期表达式对应数据库字段的值的 日期样式
	 *  例：xxb.bbq_   值： 200709--  返回样式：yyyymm--
	 *  无法确定返回空
	 *  父类重载
	 * @param nd
	 * @return
	 */
	public String getDateStrStyle(ExpressionNode nd) {
		return null;
	}

	/**
	 * 除法，如果除数为0，是否返回无穷大；
	 * true 用case when 处理；
	 * false 不处理；
	 * @return
	 */
	public boolean isDivzeroReturnInfinity() {
		return true;
	}

	/**
	 * 判断数据类型null是否当0处理；
	 * 计数统计对于null是不参与计数的，所以先转成0再统计。
	 * 还有null值参与的运算都返回null，也需要先转换成0；
	 * 对于除数为0的时候，返回null，如果这个值也参与运算，也需要转换成0；
	 * @return
	 */
	public boolean isNullToZero() {
		return false;
	}

	/**
	 * 这个ExpEvaluateHelper可以帮助在生成sql时计算类似drillcell.leftcell这样的表达式
	 */
	public ExpEvaluateHelper getExpEvaluateHelperForCalcConstInSql() {
		return null;
	}

	protected boolean evalbool(ExpressionNode n) {
		return n.evaluateBoolean(getExpEvaluateHelperForCalcConstInSql());
	}

	protected String evalstr(ExpressionNode n) {
		return n.evaluateString(getExpEvaluateHelperForCalcConstInSql());
	}

	protected int evalint(ExpressionNode n) {
		return (int) n.evaluateInt(getExpEvaluateHelperForCalcConstInSql());
	}

	/**
	 * 抽象出就算数组常量的方法；
	 */
	public String[] getArrayValues(ExpressionNode n2) {
		String[] vs = null;
		if (n2.isReturnArray())
			vs = n2.evaluateArray(getExpEvaluateHelperForCalcConstInSql()).toStringArray(
					getExpEvaluateHelperForCalcConstInSql());
		else {
			String v = evalstr(n2);
			if (v != null && v.length() > 0) {
				vs = v.split(FormatExpToSqlExp.SPLITOR);
			}
		}
		return vs;
	}
}
