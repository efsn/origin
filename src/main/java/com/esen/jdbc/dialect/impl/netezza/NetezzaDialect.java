package com.esen.jdbc.dialect.impl.netezza;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.sql92.SQL92Dialect;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class NetezzaDialect extends SQL92Dialect {

	public String funcAsInt(String v) {
		if(!StrFunc.isNumber(v)){
			return "cast ("+v+" as Int)";
		}
		return super.funcAsInt(v);
	}

	public String funcAsNum(String v) {
		if(!StrFunc.isNumber(v)){
			return "cast ("+v+" as Int)";
		}
		return super.funcAsNum(v);
	}

	public String funcAsStr(String v) {
		
		return super.funcAsStr(v);
	}

	public String getLimitString(String querySelect, int offset, int limit) {
		if(offset<0||limit<=0)
		      return querySelect;
		return new StringBuffer(querySelect.length() + 20)
		        .append(querySelect ) //添加优先级
		        .append(" limit ").append(String.valueOf(limit))
		        .append(" offset ").append(offset)
		        .toString();
	}

	public String funcStrCat(String field1, String field2) {
		if(StrFunc.isNotEmpty(field1)){
			return this.ifNull(field1,"''")+"||"+this.ifNull(field2,"''");
		}else{
			return field2;
		}
	}

	public boolean supportsLimit() {
		return true;
	}

	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null)
			dbmd = connectionFactory != null ? new NetezzaDbMetaData(
					connectionFactory) : new NetezzaDbMetaData(con);
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new NetezzaDbMetaData(conn);
	}

	public NetezzaDialect(Object con_OR_conf) {
		super(con_OR_conf);
	}

	public DbDefiner createDbDefiner() {
		return new NetezzaDef(this);
	}

	public String funcFind(String sub, String toFind) {
		StringBuffer sBuf = new StringBuffer(100).append("(strpos(")
				.append(toFind).append(", ").append(sub).append(")-1)"); // 0表示没有找到
		return sBuf.toString();
	}

	public String funcLeft(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" substr(")
				.append(source).append(",1,").append(len).append(')');
		return sBuf.toString();
	}

	public String funcRight(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" substr(")
			.append(source).append(",").append("length(").append(source).append(")-(").append(len).append(")+1,").append(len).append(')');
		return sBuf.toString();
	}


	public String funcSearch(String sub, String toSearch) {
		StringBuffer sBuf = new StringBuffer(100).append("(strpos(lower(")
				.append(toSearch).append("), lower(")// 转化为小写进行比较，忽略大小写
				.append(sub).append("))-1)"); // 0表示没有找到
		return sBuf.toString();
	}

	public String funcWholeReplace(String source, String oldSub, String newSub) {
		return null;
	}

	public String funcYear(String datefield) {
		return "DATE_PART ('YEAR'," + datefield + ")";
	}

	public String funcMonth(String datefield) {
		return "DATE_PART ('MONTH'," + datefield + ")";
	}

	public String funcDay(String datefield) {
		return "DATE_PART ('DAY'," + datefield + ")";
	}
	
	public String funcSeconds(String datefield, String datefield2) {
		 StringBuffer sql = new StringBuffer(32);
		    sql.append("SECONDS_BETWEEN(").append(datefield2).append(',').append(datefield).append(')');
		 return sql.toString();
	}

	public String funcDays(String datefield, String datefield2) {
		StringBuffer sql = new StringBuffer(32);
	    sql.append("DAYS_BETWEEN(").append(datefield2).append(',').append(datefield).append(')');
	    return sql.toString();
	}

	public String funcFact(String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" factorial (")
				.append(d).append(')');
		return sBuf.toString();
	}

	public String funcInt(String d) {
		if(d==null){
			return "cast(null as int)";
		}
		StringBuffer sBuf = new StringBuffer(40).append(" round(").append(d)
				.append(")");// 去尾
		return sBuf.toString();
	}

	public String funcPower(String dValue, String d) {
		StringBuffer sBuf = new StringBuffer(40).append(" pow(").append(dValue)
				.append(',').append(d).append(')');
		return sBuf.toString();
	}

	public String funcToDate(String date) {
		return this.funcToDateTime(date);
	}

	public String funcToDateTime(String date) {
		if (date == null || date.length() == 0)
			return "cast(null as date)";
		StringBuffer sql = new StringBuffer(32);
		sql.append("TO_TIMESTAMP('");
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

	public String funcDateToChar(String datefield, String style) {
		StringBuffer sql = new StringBuffer(20);
		sql.append("to_char(");
		sql.append(datefield);
		sql.append(",'");
		sql.append(style);
		sql.append("')");
		return sql.toString();
	}

	public String funcCharToDate(String charfield, String style) {
		StringBuffer sql = new StringBuffer(20);
		sql.append("to_date(");
		sql.append(charfield);
		sql.append(",'");
		sql.append(style);
		sql.append("')");
		return sql.toString();
	}

	public String formatOffsetDate(String datefield, int offset, char ymd) {
		switch(ymd){
	      case 'y':
	      case 'Y':
	        return "add_months("+datefield+","+offset*12+")";
	      case 'm':
	      case 'M':
	        return "add_months("+datefield+","+offset+")";
//	      case 'd':
//	      case 'D':
//	        return "dateadd(day,"+offset+","+datefield+")";
	    }
//	    throw new RuntimeException("不支持的类型（y[y],m[M],d[D]）："+ymd);
		throw new RuntimeException(I18N.getString("JDBC.COMMON.UNSUPPORTEDTYPE", "不支持的类型（y[y],m[M],d[D]）：")+ymd);
	}
	public String ifNull(String str,String str2){
	    return "isnull("+str+","+str2+")";
	  }
	protected String getStrLengthSql(String str) {
		StringBuffer sql = new StringBuffer(32);
		sql.append("select ").append(funcLen("'" + str + "'"));
		return sql.toString();
	}

	public String funcRound(String d, String i) {
		if(d==null||d.equals("")){
			return "cast (null as integer)";
		}
		return super.funcRound(d, i);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "TRUNCATE TABLE " + tablename;
	}
}
