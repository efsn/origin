package com.esen.jdbc.dialect.impl.teradata;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Pattern;

import com.esen.db.sql.analyse.SQLAnalyse;
import com.esen.exception.RuntimeException4I18N;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * Teradata 数据库方言
 *
 * @author liujin
 */
public class TeradataDialect extends DialectImpl {

	/**
	 * 构造方法
	 * @param f 数据库连接
	 */
	public TeradataDialect(Object f) {
		super(f);
	}

	/**
	 * {@inheritDoc}
	 */
	public DbDefiner createDbDefiner() {
		return new TeradataDef(this);
	}

	/**
	 * {@inheritDoc}
	 */
	public int getMaxColumn() {
		return 2048;
	}

	/**
	 * {@inheritDoc}
	 */
	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd != null) {
			return dbmd;
		}
		
		if (connectionFactory != null) {
			dbmd = new TeradataDbMetaData(connectionFactory);
		}	else {
			dbmd = new TeradataDbMetaData(con);
		}

		return dbmd;
	}

	/**
	 * {@inheritDoc}
	 */
	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new TeradataDbMetaData(conn);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getCreateTableByQureySql(String tablename, String querysql, boolean istemp) {
		if (StrFunc.isNull(tablename))
			return "";

		tablename = tablename.toUpperCase();
		StringBuffer sql = new StringBuffer("CREATE TABLE " + tablename);
	
		if (!SqlFunc.isSelect(querysql))
			querysql = "SELECT * FROM " + querysql;
		sql.append(" AS ( ").append(querysql).append(" ) ");
		sql.append(" WITH DATA ");

		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportsLimit() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportsLimitOffset() {
		/*
		 * 不支持  offset。 
		 * modify by liujin 2013.06.27
		 */
		return false;
	}
	
	public boolean supportsTableComment() {
		return true;
	}

	public boolean supportsTableColumnComment() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getStrLengthSql(String str) {
		StringBuffer sql = new StringBuffer(32);
		sql.append("SELECT ").append("LENGTH('").append(str).append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0)
			return querySelect;

		int startOfSelect = querySelect.toLowerCase().indexOf("select");
		if (startOfSelect > 0) {
			querySelect = querySelect.substring(startOfSelect); // 去掉 /* common */
		}

		if (offset < 0 || limit <= 0) {
			return querySelect;
		}

		String orderby = null;
		boolean isOrderByFunc = false;
		
		/* 
		 * ISSUE:BI-8606: add by liujin 2013.07.09
		 * order by 中使用 rank() 函数时排序结果不正确
		 * 
		 * Teradata 的子查询中不能带 order by 子句，所以不能使用直接添加外层查询，在外层查询中加入行号限制的形式分页。
		 * 参考 DB2AS400 实现，把 Order by 子句提取出来做特殊处理。
		 * 
		 * 1.判断是否有 order by rank() over (...) 和  order by row_number() over(...) 
		 *   这两种情况下，改为  qualify row_number() over(...) 处理	。
		 *   存在以下问题：
		 *   (1) 如果 order by 条件不止一个，会出错
		 * 2.如果不存在情况 1，判断 SQL 字符串中是否带排序关键字  order by，
		 *   如果有，然后检查字符串中的括弧是否成对出现, 如果不是则认为这一段不是整个查询 order by 子句
		 *   也有可能是诸如row_number() over(order by ...)这样的句子。
		 *   存在以下问题：
		 *   (1) select a.id, a.name from a order by a.age, 再包装分页后a.age丢失，好在我们的分析表查询大都没有这种
			 */

		//找 order by rank() over()
		int startOfOrderby = querySelect.toLowerCase().lastIndexOf("order by rank() over");
		if (startOfOrderby > 0) {
			orderby = "row_number() " + querySelect.substring(startOfOrderby + "order by rank() ".length()); 
			if (isBracketsMatched(orderby)) {
				querySelect = querySelect.substring(0, startOfOrderby); 
				isOrderByFunc = true;
			} else {
				orderby = null;
			}
		} 

		//找 order by row_number() over
		if (orderby == null) {
			startOfOrderby = querySelect.toLowerCase().lastIndexOf("order by row_number() over");
			if (startOfOrderby > 0) {
				orderby = querySelect.substring(startOfOrderby + "order by".length()); 
				if (isBracketsMatched(orderby)) {
					querySelect = querySelect.substring(0, startOfOrderby); 
					isOrderByFunc = true;
				} else {
				orderby = null;
			}
		}
		}
		
		if (orderby == null){
			startOfOrderby = querySelect.toLowerCase().lastIndexOf("order by");
			if (startOfOrderby > 0) {
				orderby = querySelect.substring(startOfOrderby); 
				if (isBracketsMatched(orderby)) {
					querySelect = querySelect.substring(0, startOfOrderby); 
				} else {
					orderby = null;
				}
			}
		}

		StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 120);
		/*
		 * BUG:BI-8600: modify by liujin
		 * 子查询中不支持 order by，使用 row_number() over() 函数实现排序。
		 */
		pagingSelect.append(querySelect);
		String rowNum = null;
		if (orderby == null) {
			orderby = "order by 1";
		}
		
		if (isOrderByFunc)	{
			rowNum = orderby;	
		} else {
			rowNum = " row_number() over(" + orderby + ")";
		}
		
		if (offset > 0) {
			pagingSelect.append(" qualify ").append(rowNum).append(" <= ").append(offset + limit);
			pagingSelect.append(" and ").append(rowNum).append(" > ").append(offset);
		} else {
			pagingSelect.append(" qualify ").append(rowNum).append(" <= ").append(limit);
		}
		return pagingSelect.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	protected String getResultMetaData_Sql(String sql) {
		if (SqlFunc.isCallalbe(sql)) {
			return sql;
		}

		/*
		 * Teradata 不能加外层查询，否则无法获取到准确的结果集元信息
		 */
		SQLAnalyse sqla = new SQLAnalyse(sql);
		sql = sqla.getNoOrderBySQl();
		sql = sql.replaceAll("\\?", "''");

		return sql;
	}

	/**
	 * 用于判断一个字符串中的括弧字符()是否成对出现
	 */
	private static final boolean isBracketsMatched(String str) {
		int stack = 0;
		int count = str != null ? str.length() : 0;
		for (int i = 0; i < count; i++) {
			switch (str.charAt(i)) {
				case '(':
					stack++;
					break;
				case ')':
					stack--;
					break;
				default:
					break;
			}
		}
		return stack == 0;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean resultEndWithIndex() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcChar(String ascii) {
		StringBuffer sBuf = new StringBuffer(20).append(" CHR(").append(ascii).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcFind(String sub, String toFind) {
		StringBuffer sBuf = new StringBuffer(100)
						.append("(INSTR(").append(toFind).append(", ")
						.append(sub).append(" , 1, 1) - 1)");
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLeft(String source, String len) {
		return _funcLeft(source, len, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRight(String source, String len) {
		return _funcRight(source, len, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMid(String field, String iFrom, String len) {
		return _funcMid(field, iFrom, len, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLen(String field) {
		return _funcLen(field, false);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLower(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" LOWER(").append(field).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcUpper(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" UPPER(").append(field).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSearch(String sub, String toSearch) {
		StringBuffer sBuf = new StringBuffer(100).append(" ( INSTR(UPPER(")
					.append(toSearch).append(") , UPPER(").append(sub)
					.append(") , 1, 1) - 1)");
		return sBuf.toString();
	}

	/**
	 * Teradata 中，有  null 或  '' 参与连接操作结果都为空，修改其实现
	 * 
	 * {@inheritDoc}
	 */
	public String funcStrCat(String field1, String field2) {
		/*
		 * BUG:BI-8617: modify by liujin 2013.06.26 
		 * 改写该函数形式，使用 函数 COALESCE实现较为方便
		 */
		StringBuffer sBuf = new StringBuffer(128).append(" COALESCE( ")
				.append("(").append(field1).append(")").append(" || ")
				.append("(").append(field2).append(")").append(", ")
				.append(field1).append(", ").append(field2).append(") ");
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcWholeReplace(String source, String oldSub, String newSub) {
		StringBuffer sBuf = new StringBuffer(50).append(" REPLACE(")
						.append(source).append(',').append(oldSub).append(',')
						.append(newSub).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcTrim(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" TRIM(").append(field).append(')');
		return sBuf.toString();
	}

	/**
	 * funcNullvalue
	 * 不支持  date 类型
	 * 
	 * @param field 字段名
	 * @param defaultValue 默认值
	 * @return String sql语句
	 */
	public String funcNullvalue(String field, String defaultValue) {
		/*
		 * ISSUE:BI-8611 add by liujin 2013.06.24 
		 * NVL 对于 NUMBER 类型无效，改为使用 函数 COALESCE 实现
		 */
		StringBuffer sBuf = new StringBuffer(50).append(" COALESCE(")
				.append(field).append(',').append(defaultValue).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcToday() {
		return " CURRENT_DATE ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcNow() {
		return " CURRENT_TIMESTAMP ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCode(String sourece) {
		StringBuffer sBuf = new StringBuffer(50).append(" ASCII(").append(sourece).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAbs(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 * Teradata 不支持该函数
	 */
	public String funcC(String d) {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCos(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSin(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcTan(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 * Teradata 不支持该函数
	 */
	public String funcEven(String d) {
		return ""; 
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcExp(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSqrt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 * Teradata 不支持该函数
	 */
	public String funcFact(String d) {
		return "";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcInt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" CAST(").append(d).append(" AS INT) ");
		return sBuf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSign(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLog(String d, String dValue) {
		StringBuffer sBuf = new StringBuffer(40).append("(LOG(").append(dValue)
				.append(") / LOG(").append(d).append("))");
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMod(String iValue, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" (").append(iValue).append(" MOD ").append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcPi() {
		return " 3.14159266359 ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcPower(String dValue, String d) {
		if (null == dValue && null == d)
			return null;
		StringBuffer sBuf = new StringBuffer(40).append(" POWER(").append(dValue).append(',').append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRand() {
		//取值范围为 0.0  到 1.0
		return " (RANDOM(0, 2147483647) * 1.0/2147483647) ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRound(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d).append(',').append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcTrunc(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d).append(", ").append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLn(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 * Teradata 不支持该函数
	 */
	public String funcRepeat(String field, String count) {
		return field;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String timestamp2date(String var) {
		return "to_date(to_char(" + var + ",'yyyymmdd'),'yyyymmdd')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcToDate(String date) {
		if (date == null || date.length() == 0)
			return " null ";
		StringBuffer sql = new StringBuffer(32);
		sql.append("to_date('");
		String style = "YYYYMMDD";
		if (Pattern.matches("[0-9]{8}", date)) {
		}
		if (Pattern.matches("[0-9]{8} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
			style = "YYYYMMDD HH24:MI:SS";
		}
		if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", date)) {
			style = "YYYY-MM-DD";
		}
		if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
			style = "YYYY-MM-DD  HH24:MI:SS";
		}
		if (Pattern.matches("[0-9]{4}----", date)) {
			date = date.substring(0, 4) + "0101";
			style = "YYYYMMDD";
		}
		if (Pattern.matches("[0-9]{4}", date)) {
			date = date + "0101";
			style = "YYYYMMDD";
		}
		if (Pattern.matches("[0-9]{4}[0-9]{2}--", date)) {
			style = "YYYYMM--";
		}
		if (Pattern.matches("[0-9]{4}[0-9]{2}", date)) {
			style = "YYYYMM";
		}
		sql.append(date);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcDateToChar(String datefield, String style) {
		StringBuffer sql = new StringBuffer(20);
		sql.append("to_char(");
		sql.append(datefield);
		sql.append(",'");
		sql.append(style);
		sql.append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCharToDate(String charfield, String style) {
		StringBuffer sql = new StringBuffer(20);
		sql.append("to_date(");
		sql.append(charfield);
		sql.append(",'");
		sql.append(style);
		sql.append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String ifNull(String str, String str2) {
		/*
		 * BUG:BI-8611： modify by liujin 2013.06.26 NVL 
		 * 对于 NUMBER 类型无效，改为使用 函数  COALESCE 实现
		 */
		return "COALESCE(" + str + "," + str2 + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportRegExp() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcToDateTime(String dtstr) {
		if (dtstr == null || dtstr.length() == 0)
			return "to_timestamp(null)";
		if (Pattern.matches("[0-9]{8}", dtstr)) {
			dtstr = dtstr + " 00:00:00";
		} else if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", dtstr)) {
			dtstr = dtstr + " 00:00:00";
		} else if (Pattern.matches("[0-9]{4}----", dtstr)) {
			dtstr = dtstr.substring(0, 4) + "0101 00:00:00";
		} else if (Pattern.matches("[0-9]{4}", dtstr)) {
			dtstr = dtstr + "0101 00:00:00";
		} else if (Pattern.matches("[0-9]{4}[0-9]{2}--", dtstr)) {
			dtstr = dtstr.substring(0, 6) + "01 00:00:00";
		} else if (Pattern.matches("[0-9]{4}[0-9]{2}", dtstr)) {
			dtstr = dtstr + "01 00:00:00";
		}
		String style = null;
		// 匹配日期时间 "2001-01-01 00:00:00"
		if (dtstr.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")) {
			style = "YYYY-MM-DD HH24:MI:SS";
		} else if (dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}")) {
			style = "YYYYMMDD HH24:MI:SS";
		} else {
			//格式不对
			throw new RuntimeException(I18N.getString("JDBC.COMMON.FORMATISWRONG", "格式不对：")+dtstr);
		}
		StringBuffer sql = new StringBuffer(20);
		sql.append("to_timestamp('");
		sql.append(dtstr);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcDay(String datefield) {
		return "to_char(" + datefield + ",'DD')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMonth(String datefield) {
		return "to_char(" + datefield + ",'MM')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcYear(String datefield) {
		return "to_char(" + datefield + ",'YYYY')";
	}

	/**
	 * 对天数采用标准加减，而使用ADD_MONTHS 函数加减月数和年数
	 * 1 select hiredate-5 as hd_minus_5D, 
	 * 2 hiredate+5 as hd_plus_5D, 
	 * 3 add_months(hiredate,-5) as hd_minus_5M, 
	 * 4 add_months(hiredate,5) as hd_plus_5M, 
	 * 5 add_months(hiredate,-5*12) as hd_minus_5Y, 
	 * 6 add_months(hiredate,5*12) as hd_plus_5Y 
	 * 7 from emp
	 */
	public String formatOffsetDate(String datefield, int offset, char t) {
		switch (t) {
			case 'y':
			case 'Y':
				return "add_months(" + datefield + "," + (offset * 12) + ")";
			case 'm':
			case 'M':
				return "add_months(" + datefield + "," + offset + ")";
			case 'd':
			case 'D':
				return datefield + (offset < 0 ? "" : "+") + offset;
		}
		throw new RuntimeException4I18N("JDBC.COMMON.UNSUPPORTEDTYPE","不支持的类型（y[y],m[M],d[D]）：{0}",new Object[]{t});
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcDays(String datefield, String datefield2) {
		// 包含两头的天数；
		return "abs(trunc(" + datefield + "-" + datefield2 + "))+1";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcStdev(String field) {
		return "stddev_pop(" + field + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsInt(String v) {
		return " CAST (" + v + " AS INT) ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsNum(String v) {
		if (null == v)
			return null;

		/*
		 * BUG:BI-8611： modify by liujin 2013.06.26 
		 * 将 NUMBER 类型改为 NUMERIC 类型
		 */
		return "CAST (" + v + " AS NUMERIC) ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsStr(String v) {
		if (v == null)
			return "null";
		return "to_char(" + v + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	protected String funcCastNull(int type) {
		switch (type) {
			case Types.INTEGER:
			case Types.SMALLINT:
			case Types.TINYINT:
			case Types.BIGINT: {
				return funcAsInt(null);
			}
			case Types.FLOAT:
			case Types.DOUBLE:
			case Types.NUMERIC:
			case Types.BIT:
			case Types.REAL:
			case Types.DECIMAL: {
				return funcAsNum(null);
			}
			case Types.VARCHAR:
			case Types.CHAR: {
				return funcAsStr(null);
			}
			case Types.TIMESTAMP:
			case Types.TIME: {
				return funcToDateTime(null);
			}
			case Types.DATE: {
				return funcToDate(null);
			}
			case Types.BLOB:
			case Types.VARBINARY:
			case Types.BINARY:
			case Types.LONGVARBINARY:// sybase
			case Types.LONGVARCHAR:
				// DB2,Mssql,Oracle
			case Types.CLOB: // Mysql,Sybase
			case Types.BOOLEAN:
			case Types.ARRAY:
			case Types.DATALINK:
			case Types.JAVA_OBJECT:
			case Types.NULL:
			case Types.OTHER:
			case Types.REF:
			case Types.STRUCT:
			default:
				return null;
		}
	}

	/**
	 * {@inheritDoc}
	 * Teradata 不支持该函数
	 */
	public String funcSeconds(String datefield, String datefield2) {
		return ""; //不支持
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLenB(String f) {
		return _funcLen(f, true);
	}

	/**
	 * 获取字段长度的sql语句
	 * 
	 * @param field 字段名
	 * @param isByte 是否为字节数
	 * @return sql语句
	 */
	private String _funcLen(String field, boolean isByte) {
		//TODO
		if (StrFunc.isNull(field))
			return " 0 ";
		return new StringBuffer(50).append(isByte ? " LENGTH(" : " CHARACTER_LENGTH(").append(field).append(')').toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMidB(String source, String i, String n) {
		return _funcMid(source, i, n, true);
	}

	/**
	 * 获取字段的指定起始位置和长度的字符串的 sql语句
	 * 
	 * @param field 字段名
	 * @param iFrom 起始位置
	 * @param len 长度
	 * @param isByte 长度是否为字节数
	 * @return sql语句
	 */
	private String _funcMid(String field, String iFrom, String len, boolean isByte) {
		//TODO
		StringBuffer sb = new StringBuffer(50).append(isByte ? " SUBSTRB(" : " SUBSTR(").append(field).append(","); // 系统函数从0开始
		int ifrm = StrFunc.str2int(iFrom, Integer.MIN_VALUE);
		if (ifrm == Integer.MIN_VALUE) {
			sb.append(iFrom).append("+1");
		} else {
			sb.append(ifrm + 1);
		}
		if (len != null && len.length() > 0) {
			sb.append(",").append(len);
		}
		sb.append(')');
		return sb.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLeftB(String source, String len) {
		return _funcLeft(source, len, true);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRightB(String source, String len) {
		return _funcRight(source, len, true);
	}

	/**
	 * 获取字段从右侧起指定长度的字符串的sql语句
	 * 
	 * @param source 字段名
	 * @param len 长度
	 * @param isByte 是否为字节数
	 * @return sql语句
	 */
	private String _funcRight(String source, String len, boolean isByte) {
		StringBuffer sb = new StringBuffer(50).append(isByte ? " SUBSTRB(" : " SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			/*
			 * 如果len是个表达式，通过substr三个参数，计算出起位置，来获取需要的值；
			 */
			String ifrom = _funcLen(source, isByte) + "- (" + len + ")+1";
			sb.append(",").append(ifrom).append(",").append(len).append(")");
		} else {
			if (l > 0) {
				sb.append(", character_length(").append(source).append(") + 1 - ").append(len).append(')');
			} else if (l == 0) { // len = 0，返回空
				sb.append(",1,0)");
			} else {
				//len为负数，则相当于 left；
				l = -l;
				sb.append(",1,").append(l).append(')');
			}
		}
		return sb.toString();

	}

	/**
	 * 获取字段左起指定长度的字符串的sql语句
	 * 
	 * @param source 字段名
	 * @param len 长度
	 * @param isByte 是否是字节数
	 * @return sql语句
	 */
	public String _funcLeft(String source, String len, boolean isByte) {
		//TODO
		StringBuffer sBuf = new StringBuffer(50).append(" ").append(isByte ? "SUBSTR(" : "SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			sBuf.append(", 1, ").append(len).append(')');
		} else if (l >= 0) {
			sBuf.append(", 1, ").append(len).append(')');
		} else {
			sBuf.append(", ").append(len).append(')');
		}

		return sBuf.toString();
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean supportsDenseRank() {
		/*
		 * BUG:BI-8606: add by liujin 2013.06.27 
		 * Teradata 数据库不支持 dense_rank 函数。
		 */
		return false;
	}

	/**
	 * 是否支持full join连接方式
	 * 
	 * @return boolean 是否支持full join
	 */
	public boolean supportsFullJoin() {
		/*
		 * ISSUE:BI-8606/BI-8596 
		 * Teradata支持Full join连接查询语句写法
		 */
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "DELETE " + tablename + " ALL";
	}

}
