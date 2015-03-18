package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;

/**
 * AS400,i5OS系统，db2数据库的Dialect实现；
 * 
 * @author dw
 * 
 */
public class DB2AS400Dialect extends DB2Dialect {

	public DB2AS400Dialect(Object f) {
		super(f);
	}

	public DbDefiner createDbDefiner() {
		return new DB2AS400Def(this);
	}

	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			dbmd = connectionFactory != null ? new DB2AS400DbMetaData(connectionFactory)
			    : new DB2AS400DbMetaData(con);
		}
		return dbmd;
	}

	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new DB2AS400DbMetaData(conn);
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
	 * 覆盖父类的方法,DB2的分页查询SQL在AS00数据库上不适用, 查询出来的结果集没有排序
	 * AS400数据库分页排序查询必须先分页再排序, 不能先排序后分页
	 * 所以必须先对SQL参数querySelect进行解析, 截取order by子句部分放到最后
	 */
	public String getLimitString(String querySelect, int offset, int limit) {
		if (offset < 0 || limit <= 0)
			return querySelect;
		int startOfSelect = querySelect.toLowerCase().indexOf("select");
		if (startOfSelect > 0) {
			querySelect = querySelect.substring(startOfSelect); // 去掉/*commaen*/
		}
		String orderby = null;
		int startOfOrderby = querySelect.toLowerCase().lastIndexOf("order by");
		if (startOfOrderby > 0) {
			orderby = querySelect.substring(startOfOrderby); 
			/** 
			 * 先判断下后面的SQL字符串中的括弧是否成对出现, 如果不是则认为这一段不是整个查询order by子句
			 * 也有可能是诸如row_number() over(order by ...)这样的句子，这里仍然有些问题, 比如:
			 * 1. order by row_number() over(order by...) 这个没有问题，因为这个SQL语句AS400本来就不支持
			 * 2. select a.id, a.name from a order a.age, 再包装分页后a.age丢失，好在我们的分析表查询大都没有这种
			 */
			if (isBracketsMatched(orderby)) {
				// 截掉order by子句
				querySelect = querySelect.substring(0, startOfOrderby); 
			}
			else {
				orderby = null;
			}
		}

		// 分页处理
		StringBuffer pagingSelect = new StringBuffer(querySelect.length() + 120);
		pagingSelect.append("select * from ( select row_.*, rownumber() over() as rownum_ from ( ");

		pagingSelect.append(querySelect);
		if (offset > 0) {
			pagingSelect.append(" ) row_ ) row0_ where rownum_<= " + (offset + limit) + " and rownum_ > " + offset);
		}
		else {
			pagingSelect.append(" ) row_ ) row0_  where rownum_<= " + limit);
		}
		// 排序处理，将之前截取掉的order by子句再装上
		if (orderby != null) {
			pagingSelect.append(" ").append(orderby);
		}
		return pagingSelect.toString();
	}
}
