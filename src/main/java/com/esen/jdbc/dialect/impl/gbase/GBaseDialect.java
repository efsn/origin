package com.esen.jdbc.dialect.impl.gbase;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * @author zhaowb
 */
public class GBaseDialect extends DialectImpl {

	public GBaseDialect(Object f) {
		super(f);
	}

	public DbDefiner createDbDefiner() {
		return new GBaseDef(this);
	}
	
	/**
	 * 增加下面二个方法, 用于获取GBASE数据库下表结构、库结构信息对象
	 */
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new GBaseDbMetaData(connectionFactory):new GBaseDbMetaData(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new GBaseDbMetaData(conn);
  }

	public int getMaxColumn() {
		return 1000;
	}

	protected String getStrLengthSql(String str) {
		StringBuffer sql = new StringBuffer(32);
		sql.append("select ").append(funcLen("'" + str + "'"));
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
		/**
		 * GBASE数据库分页不能使用SELECT * FROM (SELECT A1,A2 FROM A ORDER BY A1) B LIMIT 100这种结构
		 * ORDER BY不能放在里面
		 */
		return new StringBuffer(querySelect.length() + 20).append(querySelect)
		.append(" limit ").append(offset > 0 ? String.valueOf(offset) + ',' + limit : String.valueOf(limit)).toString();
	}

	public boolean resultEndWithIndex() {
		return false;
	}

	public String funcToSql(String LocalFunc, ArrayList params) {
		return "";
	}

	public String funcChar(String ascii) {
		StringBuffer sBuf = new StringBuffer(20).append(" CHAR(").append(ascii).append(')');
		return sBuf.toString();
	}

	public String funcFind(String sub, String toFind) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(").append(toFind).append(", binary ").append(sub).append(
				")-1)");
		return sBuf.toString();
	}

	public String funcLeft(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" LEFT(").append(source).append(',').append(len).append(')');
		return sBuf.toString();

	}

	public String funcRight(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" RIGHT(").append(source).append(',').append(len).append(')');
		return sBuf.toString();
	}

	public String funcMid(String field, String iFrom, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" SUBSTRING(").append(field).append(" ,");
		if (StrFunc.isNumber(iFrom)) {
			int start = Integer.parseInt(iFrom);
			sBuf.append(start + 1);
		}
		else
			sBuf.append("(").append(iFrom).append(")+ 1");
		sBuf.append(',').append(len).append(')');
		return sBuf.toString();

	}

	public String funcLen(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" CHAR_LENGTH(").append(field).append(')');
		return sBuf.toString();
	}

	public String funcLower(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" LOWER(").append(field).append(')');
		return sBuf.toString();

	}

	public String funcUpper(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" UPPER(").append(field).append(')');
		return sBuf.toString();
	}

	public String funcSearch(String sub, String toSearch) {
		StringBuffer sBuf = new StringBuffer(100).append("(INSTR(").append(toSearch).append(",").append(sub).append(
				")-1) ");
		return sBuf.toString();
	}

	public String funcWholeReplace(String source, String oldSub, String newSub) {
		StringBuffer sBuf = new StringBuffer(50).append(" REPLACE(").append(source).append(',').append(oldSub).append(
				',').append(newSub).append(')');
		return sBuf.toString();
	}

	public String funcTrim(String field) {
		StringBuffer sBuf = new StringBuffer(50).append(" TRIM(").append(field).append(')');
		return sBuf.toString();
	}

	public String funcNow() {
		return "CURRENT_TIMESTAMP()";
	}

	public String funcNullvalue(String field, String defaultValue) {
		StringBuffer sBuf = new StringBuffer(50).append(" IFNULL(").append(field).append(',').append(defaultValue).append(
				')');
		return sBuf.toString();
	}

	public String funcToday() {
		return " CURDATE() ";
	}

	public String funcCode(String sourece) {
		StringBuffer sBuf = new StringBuffer(20).append(" ASCII(").append(sourece).append(')');
		return sBuf.toString();
	}

	public String funcAbs(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" ABS(").append(d).append(')');
		return sBuf.toString();

	}

	public String funcC(String d) {
		return "";
	}

	public String funcCos(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" COS(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcSin(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIN(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcTan(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TAN(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcEven(String d) {
		return "";
	}

	public String funcExp(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" EXP(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcSqrt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SQRT(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcFact(String d) {
		return "";
	}

	public String funcInt(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNCATE(").append(d).append(",0)");
		return sBuf.toString();
	}

	public String funcSign(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" SIGN(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcLog(String d, String dValue) {
		StringBuffer sBuf = new StringBuffer(40).append(" LOG(").append(d).append(',').append(dValue).append(')');
		return sBuf.toString();
	}

	public String funcMod(String iValue, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" MOD(").append(iValue).append(',').append(i).append(')');
		return sBuf.toString();
	}

	public String funcPi() {
		return " PI() ";
	}

	public String funcPower(String dValue, String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" POWER(").append(dValue).append(',').append(d).append(')');
		return sBuf.toString();

	}

	public String funcRand() {
		return " RAND() ";
	}

	public String funcRound(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" ROUND(").append(d).append(',').append(i).append(')');
		return sBuf.toString();
	}

	public String funcTrunc(String d, String i) {
		StringBuffer sBuf = new StringBuffer(40).append(" TRUNCATE(").append(d).append(',').append(i).append(')');
		return sBuf.toString();
	}

	public String funcLn(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" LN(").append(d).append(')');
		return sBuf.toString();
	}

	public String funcRepeat(String field, String count) {
		StringBuffer sBuf = new StringBuffer(100).append(" REPEAT(").append(field).append(',').append(count).append(')');
		return sBuf.toString();
	}

	public String funcIf(String b, String t, String f) {
	
		StringBuffer sBuf = new StringBuffer(100).append(" if(").append(b).append(',').append(t).append(',').append(f).append(
				')');
		return sBuf.toString();
	}

	public String funcToDate(String date) {
		if (date == null || date.length() == 0)
			return "cast(null as date)";
		StringBuffer sql = new StringBuffer(32);
		sql.append("str_to_date('");
		if (Pattern.matches("[0-9]{8}.*", date)) {
			sql.append(date.substring(0, 8));
		}
		else if (Pattern.matches("[0-9]{6}--", date) || Pattern.matches("[0-9]{6}", date)) {
			sql.append(date.substring(0, 6)).append("01");
		}
		else if (Pattern.matches("[0-9]{4}----", date) || Pattern.matches("[0-9]{4}", date)) {
			sql.append(date.substring(0, 4)).append("0101");
		}
		else if (Pattern.matches("[0-9]{4}-[0-9]{2}-[0-9]{2}.*", date)) {
			sql.append(date.substring(0, 4));
			sql.append(date.substring(5, 7));
			sql.append(date.substring(8, 10));
		}
		else {
			sql.append(date);
		}
		sql.append("','%Y%m%d')");
		return sql.toString();
	}

	public String funcCharToDate(String charfield, String style) {
		if (style == null || style.length() == 0)
			return "str_to_date(" + charfield + ",'%Y%m%d')";
		if (style.matches("[yY]{4}[mM]{2}[dD]{2}")) {
			return "str_to_date(" + charfield + ",'%Y%m%d')";
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
			sql.append("str_to_date(concat(");
			sql.append(funcLeft(charfield, String.valueOf(4)));
			sql.append(",'0101')");
			sql.append(",'%Y%m%d')");
			return sql.toString();
		}
		return "str_to_date(" + charfield + ",'%Y%m%d')";
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
			return "year(" + datefield + ")";
		}
		if (style.matches("[mM]{2}")) {
			return "month(" + datefield + ")";
		}
		if (style.matches("[dD]{2}")) {
			return "day(" + datefield + ")";
		}
		if (style.matches("[qQ]{1,2}")) {
			return "TRUNCATE(" + funcMonth(datefield) + "/3,0) +1";
		}
		if (style.matches("[wW]{1,2}")) {
			return "week(" + datefield + ")";
		}

		if (style.matches("[yY]{4}[qQ]{1,2}")) {
			return "year(" + datefield + ")*10+TRUNCATE((" + funcMonth(datefield) + "-1)/3,0) +1";
		}

		StringBuffer sql = new StringBuffer(32);
		sql.append("date_format(");
		sql.append(datefield);
		sql.append(",'").append(mystyle).append("')");
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
			return "cast(null as datetime)";
		if (Pattern.matches("[0-9]{8}", dtstr)) {
			dtstr = dtstr + " 00:00:00";
		}
		else if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", dtstr)) {
			dtstr = dtstr + " 00:00:00";
		}
		else if (Pattern.matches("[0-9]{4}----", dtstr)) {
			dtstr = dtstr.substring(0, 4) + "0101 00:00:00";
		}
		else if (Pattern.matches("[0-9]{4}", dtstr)) {
			dtstr = dtstr + "0101 00:00:00";
		}
		else if (Pattern.matches("[0-9]{4}[0-9]{2}--", dtstr)) {
			dtstr = dtstr.substring(0, 6) + "01 00:00:00";
		}
		else if (Pattern.matches("[0-9]{4}[0-9]{2}", dtstr)) {
			dtstr = dtstr + "01 00:00:00";
		}
		String style = null;
		if (dtstr.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "%Y-%m-%d %H:%i:%s";
		else if (dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "%Y%m%d %H:%i:%s";
		else
//			throw new RuntimeException("格式不对：" + dtstr);
			throw new RuntimeException(I18N.getString("JDBC.COMMON.WRONGFORMAT", "格式不对：{0}", new Object[]{dtstr}));
		StringBuffer sql = new StringBuffer(32);
		sql.append("str_to_date('");

		sql.append(dtstr);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	public String formatOffsetDate(String datefield, int offset, char t) {
		switch (t) {
			case 'y':
			case 'Y':
				return "date_add(" + datefield + ",interval " + offset + " year)";
			case 'm':
			case 'M':
				return "date_add(" + datefield + ",interval " + offset + " month)";
			case 'd':
			case 'D':
				return "date_add(" + datefield + ",interval " + offset + " day)";
		}
//		throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）：" + t);
		throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.gbase.gbasedialect.unsupportkind", "不支持的类型（y[y],m[M],d[D]）：{0}", new Object[]{String.valueOf(t)}));
	}

	public String funcDay(String datefield) {
		return "day(" + datefield + ")";
	}

	public String funcMonth(String datefield) {
		return "month(" + datefield + ")";
	}

	public String funcYear(String datefield) {
		return "year(" + datefield + ")";
	}

	public String funcDays(String datefield, String datefield2) {
		return "abs(to_days(" + datefield + ")-to_days(" + datefield2 + "))+1";
	}

	public String formatConstStr(String value) {
		if (value != null)
			value = value.replaceAll("\\\\", "\\\\\\\\");
		return super.formatConstStr(value);
	}

	public String getDeleteSql(String tbname, String tbname2, String[] keys, String[] keys2) {
		StringBuffer delsql = new StringBuffer(64);
		delsql.append("delete t1 from ").append(tbname).append(" as t1,");
		delsql.append(tbname2).append(" as t2 \n");
		delsql.append("where ");
		for (int i = 0; i < keys.length; i++) {
			if (i > 0)
				delsql.append(" and ");
			delsql.append("t1.").append(keys[i]).append("=t2.").append(keys2[i]);
		}
		return delsql.toString();
	}

	public String funcSeconds(String datefield, String datefield2) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("UNIX_TIMESTAMP(").append(datefield).append(")-");
		sql.append("UNIX_TIMESTAMP(").append(datefield2).append(")");
		return sql.toString();
	}
}
