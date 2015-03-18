package com.esen.jdbc.dialect.impl.greenplum;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.sql92.SQL92Dialect;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class GreenplumDialect extends SQL92Dialect {
	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0)
			return querySelect;
		return new StringBuffer(querySelect.length() + 20).append(querySelect) // 添加优先级
		        .append(" limit ").append(String.valueOf(limit)).append(" offset ").append(offset).toString();
	}

	public GreenplumDialect(Object conORConf) {
		super(conORConf);
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

	/**
	 * ISSUE:BI-7890 计算报表报错：计算浮动表元A2(FSZHZB.GBXYZXY)时出错：ERROR: column "month" does not exist
	 * 修改日期计算所用的函数
	 */
	public String formatOffsetDate(String datefield, int offset, char t) {
		switch (t) {
		case 'y':
		case 'Y':
			return datefield + "+ interval'" + offset + "year'";
		case 'm':
		case 'M':
			return datefield + "+ interval'" + offset + "month'";
		case 'd':
		case 'D':
			return datefield + "+ interval'" + offset + "day'";
		}
		throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.sql92dialect.unsupportkind",
		        "不支持的类型（y[y],m[M],d[D]）：") + t);
	}

	/**
	 * ISSUE:BI-7880 计算分析表（参数为时间类型），无结果
	 */
	public String funcToDateTime(String dtstr) {
		if (dtstr == null || dtstr.length() == 0)
			return "cast(null as datetime)";
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
		if (dtstr.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "YYYY-MM-DD HH24:MI:SS";
		else if (dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
			style = "YYYYMMDD HH24:MI:SS";
		else
			// throw new RuntimeException("格式不对："+dtstr);
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.mysqldialect.wrongfor",
			        "格式不对：") + dtstr);
		StringBuffer sql = new StringBuffer(32);
		sql.append("to_date('");

		sql.append(dtstr);
		sql.append("','").append(style).append("')");
		return sql.toString();
	}

	public String funcToDate(String date) {
		if (date == null || date.length() == 0)
			return "cast(null as date)";
		StringBuffer sql = new StringBuffer(32);
		sql.append("to_date('");
		if (Pattern.matches("[0-9]{8}.*", date)) {
			sql.append(date.substring(0, 8));
		} else if (Pattern.matches("[0-9]{6}--", date) || Pattern.matches("[0-9]{6}", date)) {
			sql.append(date.substring(0, 6)).append("01");
		} else if (Pattern.matches("[0-9]{4}----", date) || Pattern.matches("[0-9]{4}", date)) {
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

	/**
	 * ISSUE:BI-7878 分析表表元datetostr函数，计算报错
	 */
	public String funcDateToChar(String datefield, String style) {

		StringBuffer sql = new StringBuffer(32);
		sql.append("to_char(");
		sql.append(datefield);
		sql.append(",'").append(style).append("')");
		return sql.toString();
	}

	/**
	 * ISSUE:BI-7868 分析表表元中含有类型转换，计算报错 修改取整的函数
	 */
	public String funcTrunc(String d, String i) {
		if (!StrFunc.isNull(d) && !StrFunc.isNull(i)) {
			StringBuffer sBuf = new StringBuffer(40).append(" CAST(").append(d).append(" as int)");
			return sBuf.toString();
		} else {
			return super.funcTrunc(d, i);
		}
	}

	public DbDefiner createDbDefiner() {
		return new GreenplumDef(this);
	}

	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null)
			dbmd = connectionFactory != null ? new GreenplumDbMetaData(connectionFactory)
			        : new GreenplumDbMetaData(con);
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new GreenplumDbMetaData(conn);
	}

	public String funcLeft(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" substring(").append(source).append(",1,").append(len)
		        .append(')');
		return sBuf.toString();
	}

	public String funcRight(String source, String len) {
		StringBuffer sBuf = new StringBuffer(50).append(" substring(").append(source).append(",").append("length(")
		        .append(source).append(")-(").append(len).append(")+1,").append(len).append(')');
		return sBuf.toString();
	}

	public String funcSearch(String sub, String toSearch) {
		StringBuffer sBuf = new StringBuffer(100).append("(strpos(lower(").append(toSearch).append("), lower(")// 转化为小写进行比较，忽略大小写
		        .append(sub).append("))-1)"); // 0表示没有找到
		return sBuf.toString();
	}

	public String ifNull(String str, String str2) {
		return "COALESCE(" + str + "," + str2 + ")";
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "TRUNCATE TABLE " + tablename;
	}
}
