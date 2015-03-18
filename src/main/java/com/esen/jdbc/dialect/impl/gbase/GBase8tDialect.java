package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * @author liujin
 */
public class GBase8tDialect extends DialectImpl {

	public GBase8tDialect(Object f) {
		super(f);
	}

	public DbDefiner createDbDefiner() {
		return new GBase8tDef(this);
	}

	/**
	 * 增加下面二个方法, 用于获取GBASE数据库下表结构、库结构信息对象
	 */
	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			if (connectionFactory != null) {
				dbmd = new GBase8tDbMetaData(connectionFactory);
			} else {
				dbmd = new GBase8tDbMetaData(con);
			}
		}
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new GBase8tDbMetaData(conn);
	}

	public int getMaxColumn() {
		return 1000;
	}

	protected String getStrLengthSql(String str) {
		StringBuffer sql = new StringBuffer(32);
		sql.append("SELECT ").append(funcLen("'" + str + "'"))
				.append(" FROM SYSMASTER:SYSSHMVALS");
		return sql.toString();
	}

	public String addQuote(String fieldname) {
		return "`" + fieldname + "`";
	}

	public boolean supportsLimit() {
		return true;
	}

	public boolean supportsLimitOffset() {
		return true;
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0)
			return querySelect;

		StringBuffer sql = new StringBuffer(querySelect.length() + 20);
		sql.append("SELECT ");
		if (offset > 0) {
			sql.append("SKIP " + offset);
		}
		sql.append(" LIMIT " + limit);
		sql.append(" * FROM (");
		sql.append(querySelect).append(")");
		return sql.toString();
	}

	public boolean resultEndWithIndex() {
		return false;
	}

	public String funcToSql(String LocalFunc, ArrayList params) {
		return "";
	}

	public String funcChar(String ascii) {
		StringBuffer sBuf = new StringBuffer(20).append(" CHAR(").append(ascii)
				.append(')');
		return sBuf.toString();
	}

	public String funcFind(String sub, String toFind) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(")
				.append(toFind).append(", ").append(sub).append(") - 1)");
		return sBuf.toString();
	}

	public String funcLeft(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" SUBSTR(")
				.append(source).append(", 0, ").append(len).append(')');
		return sBuf.toString();

	}

	public String funcRight(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" RIGHT(")
				.append(source).append(',').append(len).append(')');
		return sBuf.toString();
	}

	// 注意：SUBSTR 和 SBUSTRING 的语法不相同
	public String funcMid(String field, String iFrom, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" SUBSTR(")
				.append(field).append(" ,");
		if (StrFunc.isNumber(iFrom)) {
			int start = Integer.parseInt(iFrom);
			sBuf.append(start + 1);
		} else if (iFrom == null || iFrom.equalsIgnoreCase("null")) {
			sBuf.append(1);
		} else {
			sBuf.append("(").append(iFrom).append(") + 1");
		}
		sBuf.append(',').append(len).append(')');
		return sBuf.toString();

	}

	public String funcLen(String field) {
		if (field == null || field.equalsIgnoreCase("null")) {
			field = "CAST (NULL AS CHAR)";
		}
		StringBuffer sBuf = new StringBuffer(50).append(" CHAR_LENGTH(")
				.append(field).append(')');
		return sBuf.toString();
	}

	public String funcLower(String field) {
		if (field == null || field.equalsIgnoreCase("null")) {
			field = "CAST (NULL AS CHAR)";
		}
		StringBuffer sBuf = new StringBuffer(50).append(" LOWER(")
				.append(field).append(')');
		return sBuf.toString();

	}

	public String funcUpper(String field) {
		if (field == null || field.equalsIgnoreCase("null")) {
			field = "CAST (NULL AS CHAR)";
		}
		StringBuffer sBuf = new StringBuffer(50).append(" UPPER(")
				.append(field).append(')');
		return sBuf.toString();
	}

	public String funcSearch(String sub, String toSearch) {
		// 转成大写
		StringBuffer sBuf = new StringBuffer(100)
				.append("(INSTR(UPPER(").append(toSearch).append("), UPPER(")
				.append(sub).append("), 1, 1) -1)"); // 数据库是从1开始计算位置
		return sBuf.toString();
	}

	public String funcWholeReplace(String source, String oldSub, String newSub) {
		StringBuffer sBuf = new StringBuffer(50).append(" REPLACE(")
				.append(source).append(',').append(oldSub).append(',')
				.append(newSub).append(')');
		return sBuf.toString();
	}

	public String funcTrim(String field) {
		if (field == null || field.equalsIgnoreCase("null")) {
			field = "CAST (NULL AS CHAR)";
		}
		StringBuffer sBuf = new StringBuffer(50).append(" TRIM(")
				.append(field).append(')');
		return sBuf.toString();
	}

	public String funcNow() {
		return "CURRENT HOUR TO SECOND";
	}

	public String funcNullvalue(String field, String defaultValue) {
		StringBuffer sBuf = new StringBuffer(50).append(" ISNULL(")
				.append(field).append(',').append(defaultValue).append(')');
		return sBuf.toString();
	}

	public String ifNull(String str, String str2) {
		return "NVL(" + str + "," + str2 + ")";
	}

	public String funcToday() {
		// return " TODAY "; //只有日期，没有时间
		return " CURRENT YEAR TO SECOND ";
	}

	public String funcCode(String sourece) {
		return null;
	}

	public String funcAbs(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d)
				.append(')');
		return sBuf.toString();

	}

	public String funcC(String d) {
		return null;
	}

	public String funcCos(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcSin(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcTan(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcEven(String d) {
		return "";
	}

	public String funcExp(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcSqrt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcFact(String d) {
		return "";
	}

	public String funcInt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
				.append(", 0)");
		return sBuf.toString();
	}

	public String funcSign(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d)
				.append(')');
		return sBuf.toString();
	}

	public String funcLog(String d, String dValue) {
		StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d)
				.append(", ").append(dValue).append(')');
		return sBuf.toString();
	}

	public String funcMod(String iValue, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue)
				.append(", ").append(i).append(')');
		return sBuf.toString();
	}

	public String funcPi() {
		return String.valueOf(Math.PI);
	}

	public String funcPower(String dValue, String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" POWER(")
				.append(dValue).append(", ").append(d).append(')');
		return sBuf.toString();

	}

	public String funcRand() {
		return String.valueOf(Math.random()); // TODO
	}

	public String funcRound(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d)
				.append(", ").append(i).append(')');
		return sBuf.toString();
	}

	public String funcTrunc(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNC(").append(d)
				.append(", ").append(i).append(')');
		return sBuf.toString();
	}

	public String funcLn(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcRepeat(String field, String count) {
		return field; // TODO
		// StringBuffer sBuf = new
		// StringBuffer(100).append(" REPEAT(").append(field).append(", ").append(count).append(')');
		// return sBuf.toString();
	}

	public String funcIf(String b, String t, String f) { // TODO

		StringBuffer sBuf = new StringBuffer(100).append(" DECODE(").append(b)
				.append(", TRUE, ").append(t).append(", FALSE, ").append(f)
				.append(')');
		return sBuf.toString();
	}

	public String funcToDate(String date) {
		if (date == null || date.length() == 0)
			return "CAST(NULL AS DATE)";
		StringBuffer sql = new StringBuffer(32);
		sql.append("TO_DATE('");
		if (Pattern.matches("[0-9]{8}.*", date)) {
			sql.append(date.substring(0, 8));
		} else if (Pattern.matches("[0-9]{6}--", date)
				|| Pattern.matches("[0-9]{6}", date)) {
			sql.append(date.substring(0, 6)).append("01");
		} else if (Pattern.matches("[0-9]{4}----", date)
				|| Pattern.matches("[0-9]{4}", date)) {
			sql.append(date.substring(0, 4)).append("0101");
		} else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", date)) {
			sql.append(date.substring(0, 4));
			sql.append(date.substring(5, 7));
			sql.append(date.substring(8, 10));
		} else {
			sql.append(date);
		}
		sql.append("','%Y%m%d')");
		return sql.toString();
	}

	public String funcCharToDate(String charfield, String style) {
		if (style == null || style.length() == 0)
			return "TO_DATE(" + charfield + ",'%Y%m%d')";
		if (style.matches("[yY]{4}[mM]{2}[dD]{2}")) {
			return "TO_DATE(" + charfield + ",'%Y%m%d')";
		}
		if (style.matches("[yY]{4}[mM]{2}.*")) {
			StringBuffer sql = new StringBuffer(32);
			sql.append("str_to_date(concat(");
			sql.append(funcLeft(charfield, String.valueOf(6)));
			sql.append(",'01')");
			sql.append(",'%Y%m%d')");
			return sql.toString();
		}
		if (style.matches("[yY]{4}.*")) {
			StringBuffer sql = new StringBuffer(32);
			sql.append("TO_DATE(concat(");
			sql.append(funcLeft(charfield, String.valueOf(4)));
			sql.append(",'0101')");
			sql.append(",'%Y%m%d')");
			return sql.toString();
		}
		return "TO_DATE(" + charfield + ",'%Y%m%d')";
	}

	public String funcDateToChar(String datefield, String style) {
		if (style == null || style.length() == 0) {
			style = "YYYYMMDD";
		}
		style = style.toUpperCase();

		String mystyle = "%Y%m%d";
		if (style.matches("[yY]{4}[mM]{2}[dD]{2}")) {
		}
		if (style.matches("[yY]{4}[mM]{2}[dD]{2} HH24:MI:SS")) {
			mystyle = "%Y%m%d %H:%i:%s";
		}
		if (style.matches("[yY]{4}[mM]{2}--")) {
			mystyle = "%Y%m--";
		}
		if (style.matches("[yY]{4}[mM]{2}")) {
			mystyle = "%Y%m";
		}
		if (style.matches("[yY]{4}----")) {
			mystyle = "%Y----";
		}
		if (style.matches("[yY]{4}")) {
			return "YEAR(" + datefield + ")";
		}
		if (style.matches("[mM]{2}")) {
			return "MONTH(" + datefield + ")";
		}
		if (style.matches("[dD]{2}")) {
			return "DAY(" + datefield + ")";
		}
		if (style.matches("[qQ]{1,2}")) {
			return "TRUNC(" + funcMonth(datefield) + " / 3, 0) + 1";
		}
		// 不支持周
		// if (style.matches("[wW]{1,2}")) {
		// return "WEEK(" + datefield + ")";
		// }

		if (style.matches("[yY]{4}[qQ]{1,2}")) {
			return "YEAR(" + datefield + ")*10 + TRUNC(("
					+ funcMonth(datefield) + "- 1) / 3, 0) + 1";
		}

		StringBuffer sql = new StringBuffer(32);
		sql.append("TO_CHAR(");
		sql.append(datefield);
		sql.append(", '").append(mystyle).append("')");
		return sql.toString();
	}

	public boolean supportRegExp() {
		return true;
	}

	public String regExp(String field, String regexp) {
		return field + " regexp '" + regexp + "'";
	}

	public String funcToDateTime(String dtstr) {
		if (dtstr == null || dtstr.length() == 0)
			return "CAST(NULL AS DATETIME YEAR TO SECOND)";
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
		if (dtstr
				.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "%Y-%m-%d %H:%M:%S";
		else if (dtstr
				.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "%Y%m%d %H:%M:%S";
		else
			// throw new RuntimeException("格式不对：" + dtstr);
			throw new RuntimeException(I18N.getString(
					"JDBC.COMMON.WRONGFORMAT", "格式不对：{0}",
					new Object[] { dtstr }));
		StringBuffer sql = new StringBuffer(32);
		sql.append("TO_DATE('");

		sql.append(dtstr);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	public String formatOffsetDate(String datefield, int offset, char t) {
		switch (t) {
		case 'y':
		case 'Y':
			return "(" + datefield + " + INTERVAL (" + offset + ") YEAR TO YEAR)";
		case 'm':
		case 'M':
			return "(" + datefield + " + INTERVAL (" + offset + ") MONTH TO MONTH)";
		case 'd':
		case 'D':
			return "(" + datefield + " + INTERVAL (" + offset + ") DAY TO DAY)";
		}
		// throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）：" + t);
		throw new RuntimeException(I18N.getString(
				"com.esen.jdbc.dialect.impl.gbase.gbasedialect.unsupportkind",
				"不支持的类型（y[y],m[M],d[D]）：{0}",
				new Object[] { String.valueOf(t) }));
	}

	public String funcDay(String datefield) {
		return "DAY(" + datefield + ")";
	}

	public String funcMonth(String datefield) {
		return "MONTH(" + datefield + ")";
	}

	public String funcYear(String datefield) {
		return "YEAR(" + datefield + ")";
	}

	public String funcDays(String datefield, String datefield2) {
		return "EXTEND(" + datefield + ", YEAR TO DAY) " + "- EXTEND("
				+ datefield2 + ", YEAR TO DAY) " + "+ INTERVAL (1) DAY TO DAY";
	}

	public String getDeleteSql(String tbname, String tbname2, String[] keys,
			String[] keys2) {
		StringBuffer delsql = new StringBuffer(64);
		delsql.append("delete t1 from ").append(tbname).append(" as t1,");
		delsql.append(tbname2).append(" as t2 \n");
		delsql.append("where ");
		for (int i = 0; i < keys.length; i++) {
			if (i > 0)
				delsql.append(" and ");
			delsql.append("t1.").append(keys[i]).append("=t2.")
					.append(keys2[i]);
		}
		return delsql.toString();
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
		case Types.CHAR:
		case Types.LONGVARCHAR: {
			return funcAsStr(null);
		}
		case Types.TIMESTAMP:
		case Types.TIME: {
			return funcToDateTime(null);
		}
		case Types.DATE: {
			return funcToDate(null);
		}
		case Types.CLOB:
			return " CAST(NULL AS CLOB) ";

		case Types.BLOB:
			return " CAST(NULL AS BLOB) ";

		case Types.VARBINARY:
		case Types.BINARY:
		case Types.LONGVARBINARY:
		default:
			return null;
		}
	}

	public String funcAsNum(String v) {
		return "CAST(" + v + " AS DOUBLE PRECISION)";
	}

	public String funcAsStr(String v) {
		if (v == null || v.equalsIgnoreCase("null")) {
			return "CAST(NULL AS CHAR)";
		}
		return v;
	}

	public String funcSeconds(String datefield, String datefield2) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("EXTEND(").append(datefield).append(", YEAR TO SECOND)")
				.append(" - ").append("EXTEND(").append(datefield2)
				.append(", YEAR TO SECOND)")
				.append(" + INTERVAL (0) SECOND TO SECOND"); 
		// TODO 结果还是INTERVAL day to second
		return sql.toString();
	}
}
