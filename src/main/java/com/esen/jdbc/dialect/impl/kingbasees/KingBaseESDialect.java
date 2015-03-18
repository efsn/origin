package com.esen.jdbc.dialect.impl.kingbasees;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.sql92.SQL92Dialect;
import com.esen.util.i18n.I18N;

/**
 * 实现人大金仓数据库Dialect接口；
 * @author dw
 *
 */
public class KingBaseESDialect extends SQL92Dialect {

  public KingBaseESDialect(Object conORConf) {
    super(conORConf);
  }
  
  public DbDefiner createDbDefiner() {
    return new KingBaseESDef(this);
  }
  
  public DbMetaData createDbMetaData() throws SQLException {
    if(dbmd==null){
      dbmd = connectionFactory!=null?new KingBaseESDbMetaData(connectionFactory):new KingBaseESDbMetaData(con);
    }
    return dbmd;
  }
  
  public DbMetaData createDbMetaData(Connection conn) throws SQLException {
    return new KingBaseESDbMetaData(conn);
  }
  
  public boolean supportsViewColumnComment() {
	return true; 
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
  
  public String funcToDate(String date) {
    if(date==null||date.length()==0)
      return "cast(null as date)";
    StringBuffer sql = new StringBuffer(32);
    sql.append("to_date('");
    String style = "YYYYMMDD";
    if (Pattern.matches("[0-9]{8}", date)) {
    }
    if (Pattern.matches("[0-9]{8} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      style="YYYYMMDD HH24:MI:SS";
    }
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", date)) {
      style = "YYYY-MM-DD";
    }
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2} [0-9]{2}:[0-9]{2}:[0-9]{2}", date)) {
      style = "YYYY-MM-DD  HH24:MI:SS";
    }
    if (Pattern.matches("[0-9]{4}----", date)) {
      date = date.substring(0,4)+"0101";
      style = "YYYYMMDD";
    }
    if (Pattern.matches("[0-9]{4}", date)) {
      date = date+"0101";
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

  public String funcToDateTime(String dtstr) {
    if(dtstr==null||dtstr.length()==0)
      return "cast(null as timestamp)";
    if (Pattern.matches("[0-9]{8}", dtstr)) {
      dtstr = dtstr+" 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}", dtstr)) {
      dtstr = dtstr+" 00:00:00";
    }else 
    if (Pattern.matches("[0-9]{4}----", dtstr)) {
      dtstr = dtstr.substring(0,4)+"0101 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}", dtstr)) {
      dtstr = dtstr+"0101 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}[0-9]{2}--", dtstr)) {
      dtstr = dtstr.substring(0,6)+"01 00:00:00";
    }else
    if (Pattern.matches("[0-9]{4}[0-9]{2}", dtstr)) {
      dtstr = dtstr+"01 00:00:00";
    }
    String style = null;
    //  匹配日期时间 "2001-01-01 00:00:00"
    if(dtstr.matches("[0-9]{4}-[0-9]{2}-[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
        style = "YYYY-MM-DD HH24:MI:SS";
    else if(dtstr.matches("[0-9]{4}[0-9]{2}[0-9]{2} [0-9]{2}:[0-9]{2}:[0-9]{2}"))
      style = "YYYYMMDD HH24:MI:SS";
    else
//      throw new RuntimeException("格式不对："+dtstr);
    	 throw new RuntimeException(I18N.getString("JDBC.COMMON.WRONGFORMAT", "格式不对：{0}", new Object[]{dtstr}));
    StringBuffer sql = new StringBuffer(20);
    sql.append("to_timestamp('");
    sql.append(dtstr);
    sql.append("','").append(style).append("')");
    return sql.toString();
  }
  public String getLimitString(String querySelect, int offset, int limit) {
    if(offset<0||limit<=0)
      return querySelect;
    StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 100);
    pagingSelect.append("select * from ( select row$_.*, rownum rownum$_ from ( ");
    pagingSelect.append(querySelect);
    pagingSelect.append(" ) row$_ ) where ");
    if (offset > 0) {
      pagingSelect.append(" rownum$_ > ").append(offset);
      pagingSelect.append(" and ");
    }
    pagingSelect.append(" rownum$_ <= ").append(offset + limit);
    return pagingSelect.toString();
  }
  
	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return "TRUNCATE TABLE " + tablename;
	}
}
