package com.esen.jdbc.dialect.impl.vertica;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.regex.Pattern;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * Vertica 方言
 * 
 * @author liujin
 * 
 */
public class VerticaDialect extends DialectImpl {

	public VerticaDialect(Object f) {
		super(f);
	}

	public DbDefiner createDbDefiner() {
		return new VerticaDef(this);
	}

	public String getCreateTableByQureySql(String tablename, String querysql,
			boolean istemp) {
		String sql = getCreateTableByQueryStr(tablename, istemp);
		if (!SqlFunc.isSelect(querysql)) {
			querysql = "select * from " + querysql;
		}
		sql = sql + " AS " + querysql;
		return sql;
	}

	private String getCreateTableByQueryStr(String tablename, boolean istemp) {
		if (istemp) {
			return "CREATE GLOBAL TEMPORARY TABLE " + tablename;
		}
		return "CREATE TABLE " + tablename;
	}

	/**
	 * supportsLimit
	 * 
	 * @return boolean
	 */
	public boolean supportsLimit() {
		return true;
	}

	/**
	 * supportsLimitOffset
	 * 
	 * @return boolean
	 */
	public boolean supportsLimitOffset() {
		return true;
	}

	public boolean supportsViewColumnComment() {
		return true;
	}
	
	public boolean supportsTableComment() {
		return true;
	}

	public boolean supportsTableColumnComment() {
		return true;
	}

	/**
	 * 获得字符字节的长度；
	 */
	protected String getStrLengthSql(String str) {
		StringBuffer sql = new StringBuffer(32);
		sql.append("select ").append("octet_length('").append(str).append("')");
		sql.append(" from dual");
		return sql.toString();
	}

	/**
	 * @param sql String
	 * @param offset int
	 * @param limit int
	 * @return String
	 */
	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0) {
			return querySelect;
		}
		
		return new StringBuffer(querySelect.length() + 20)
				.append("SELECT * FROM ( ")
				.append(querySelect)
				.append(" ) AS TEMP_ LIMIT ")
				.append(limit)
				.append(offset > 0 ? " OFFSET " + offset : "")
				.toString();
	}

	/**
	 * resultEndWithIndex
	 * 
	 * @return boolean
	 */
	public boolean resultEndWithIndex() {
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcChar(String ascii) {
		StringBuffer sBuf = new StringBuffer(20).append(" CHR(").append(ascii)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcFind(String sub, String toFind) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(")
				.append(toFind).append(',').append(sub).append(" , 1, 1) - 1)"); // 数据库是从1开始计算位置
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
		StringBuffer sBuf = new StringBuffer(100).append("RIGHT(")
				.append(source).append(',').append(len).append(")");
		return sBuf.toString();
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
		StringBuffer sBuf = new StringBuffer(50).append(" LOWER(")
				.append(field).append(')');
		return sBuf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public String funcUpper(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" UPPER(")
				.append(field).append(')');
		return sBuf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSearch(String sub, String toSearch) {
		StringBuffer sBuf = new StringBuffer(100)
				// 转成大写
				.append(" ( INSTR(UPPER(").append(toSearch)
				.append(") , UPPER(").append(sub).append(") , 1, 1) -1)"); // 数据库是从1开始计算位置
		return sBuf.toString();
	}
	/**
	 * {@inheritDoc}
	 */
	public String funcStrCat(String field1, String field2) {
		/*
		 * 使用 CONCAT 时，有参数为 null 时，结果就为 null，与期望不符
		 * 改为使用 COALESCE 实现所需功能
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
		StringBuffer sBuf = new StringBuffer(50).append(" TRIM(").append(field)
				.append(')');
		return sBuf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public String funcNullvalue(String field, String defaultValue) {
		StringBuffer sBuf = new StringBuffer(50).append(" NVL(").append(field)
				.append(',').append(defaultValue).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcToday() {
		//return " TO_CHAR(CURRENT_DATE, 'YYYY-MM-DD')";
		return "CURRENT_DATE";
	}

	/**
	 * {@inheritDoc}
	 */
	//time还是timestamp?
	public String funcNow() {
		//return "TO_CHAR(CURRENT_TIME, 'HH24:MI:SS')";
		return "CURRENT_TIMESTAMP";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCode(String sourece) {
		StringBuffer sBuf = new StringBuffer(50).append(" ASCII(")
				.append(sourece).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAbs(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcC(String d) {
		return ""; // 不能实现
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcCos(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSin(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcTan(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcEven(String d) {
		return ""; // 没有对应函数
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcExp(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSqrt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcFact(String d) {
		return ""; // 没有本地函数
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcInt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
				.append(", 0)");// 截整
		return sBuf.toString();

	}

	/**
	 * {@inheritDoc}
	 */
	public String funcSign(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLog(String d, String dValue) {
		StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d)
				.append(',').append(dValue).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMod(String iValue, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue)
				.append(',').append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcPi() {
		return " PI() ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcPower(String dValue, String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" POWER(")
				.append(dValue).append(',').append(d).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRand() {
		return " RANDOM() ";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRound(String d, String i) {
		if (d == null && i == null) {
			return null;
		}
		
		StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d)
				.append(',').append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcTrunc(String d, String i) {
		if (d == null && i == null) {
			return null;
		}
		
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
				.append(',').append(i).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcLn(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcRepeat(String field, String count) {
		StringBuffer sBuf = new StringBuffer(40).append(" REPEAT(").append(field)
				.append(',').append(count).append(')');
		return sBuf.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			dbmd = connectionFactory != null ? 
					new VerticaDbMetaData(connectionFactory) : new VerticaDbMetaData(con);
		}
		return dbmd;
	}

	/**
	 * {@inheritDoc}
	 */
	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new VerticaDbMetaData(conn);
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
		if (date == null || date.length() == 0) {
			return "NULL";
		}
		
		StringBuffer sql = new StringBuffer(32);
		sql.append("TO_DATE('");
		String style = "YYYYMMDD";
		if (Pattern.matches("[0-9]{8}", date)) {
		}
		if (Pattern.matches("[0-9]{8} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
			style = "YYYYMMDD HH24:MI:SS";
		}
		if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", date)) {
			style = "YYYY-MM-DD";
		}
		if (Pattern.matches(
				"[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}",
				date)) {
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
		sql.append("TO_DATE(");
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
		return "NVL(" + str + "," + str2 + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean supportRegExp() {
		return true;
	}

	/**
	 * {@inheritDoc}
	 */
	public String regExp(String field, String regexp) {
		return "REGEXP_LIKE(" + field + ",'" + regexp + "')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcToDateTime(String dtstr) {
		if (dtstr == null || dtstr.length() == 0) {
			return "NULL";
		}
		
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
		if (dtstr
				.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "YYYY-MM-DD HH24:MI:SS";
		else if (dtstr
				.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "YYYYMMDD HH24:MI:SS";
		else
			// throw new RuntimeException("格式不对："+dtstr);
			throw new RuntimeException(I18N.getString(
					"JDBC.COMMON.FORMATISWRONG", "格式不对：") + dtstr);
		StringBuffer sql = new StringBuffer(20);
		sql.append(" TO_TIMESTAMP('");
		sql.append(dtstr);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcDay(String datefield) {
		return " TO_CHAR(" + datefield + ",'DD')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcMonth(String datefield) {
		return " TO_CHAR(" + datefield + ",'MM')";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcYear(String datefield) {
		return " TO_CHAR(" + datefield + ",'YYYY')";
	}

	/**
	 * {@inheritDoc}
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
		// throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+t);
		throw new RuntimeException(I18N.getString(
				"JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：") + t);
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcDays(String datefield, String datefield2) {
		// 包含两头的天数；
		return "ABS(TRUNC(" + datefield + "-" + datefield2 + ")) + 1";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcStdev(String field) {
		return "STDDEV(" + field + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsInt(String v) {
		return "TRUNC(" + v + ", 0)";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsNum(String v) {
		return "TO_NUMBER(TO_CHAR(" + v + "))";
	}

	/**
	 * {@inheritDoc}
	 */
	public String funcAsStr(String v) {
		if (v == null) {
			return "NULL";
		}
		
		return "TO_CHAR(" + v + ")";
	}

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
	 */
	public String funcSeconds(String datefield, String datefield2) {
		StringBuffer sql = new StringBuffer(36);

		sql.append(" timestampdiff('ss', ");
		sql.append(datefield2).append(',').append(datefield);
		sql.append(')');

		return sql.toString();
	}

	public String funcLenB(String f) {
		return _funcLen(f, true);
	}

	private String _funcLen(String field, boolean isByte) {
		return new StringBuffer(50).append(isByte ? " OCTET_LENGTH(" : " LENGTH(")
				.append(field).append(')').toString();
	}

	public String funcMidB(String source, String i, String n) {
		return _funcMid(source, i, n, true);
	}

	private String _funcMid(String field, String iFrom, String len,
			boolean isByte) {
		StringBuffer sb = new StringBuffer(50)
				.append(isByte ? " SUBSTRB(" : " SUBSTR(").append(field)
				.append(","); // 系统函数从0开始
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

	public String funcLeftB(String source, String len) {
		return _funcLeft(source, len, true);
	}

	public String funcRightB(String source, String len) {
		return _funcRight(source, len, true);
	}

	private String _funcRight(String source, String len, boolean isByte) {
		StringBuffer sb = new StringBuffer(50).append(
				isByte ? " SUBSTRB(" : " SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			String ifrom = _funcLen(source, isByte) + "-" + len + "+1";
			sb.append(",").append(ifrom).append(",").append(len).append(")");
		} else {
			if (l > 0) {
				// len=0，返回空
				sb.append(",-").append(len).append(')');
			} else if (l == 0) {
				sb.append(",1,0)");
			} else {
				l = -l;
				sb.append(",1,").append(l).append(')');
			}
		}
		return sb.toString();

	}

	/**
	 * funcLeft
	 * 
	 * @param source
	 *            String
	 * @param len
	 *            int
	 * @return String
	 */
	public String _funcLeft(String source, String len, boolean isByte) {
		StringBuffer sBuf = new StringBuffer(50).append(" ")
				.append(isByte ? "SUBSTRB(" : "SUBSTR(").append(source);
		int l = StrFunc.str2int(len, Integer.MIN_VALUE);
		if (l == Integer.MIN_VALUE) {
			sBuf.append(",1,").append(len).append(')');
		} else {
			if (l >= 0) {
				sBuf.append(",1,").append(len).append(')');
			} else {
				sBuf.append(",").append(len).append(')');
			}
		}
		return sBuf.toString();
	}
}
