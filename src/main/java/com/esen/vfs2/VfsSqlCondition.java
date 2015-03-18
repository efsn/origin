package com.esen.vfs2;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.StrFunc;
import com.esen.vfs2.impl.VfsNode;

/**
 * 用于生成vfs中比较复杂的sql条件
 * VfsFile.listFiles在搜索文件时因为生成的sql比较复杂,暂时在此类中实现,以后改为在ibatis中实现
 * @author zhuchx
 */
public class VfsSqlCondition {

	/**
	 * 定义过滤条件中的通配符,*表示匹配长度任意字符,?表示匹配一个任意字符
	 */
	private static final char[] LIKEWORDS = { '*', '?' };

	private static final String ESCEPTROOT = " FILENAME_ <> '/' ";

	public String getWhereCondition(ConnectionFactory fct, VfsNode node, String filters, int filterType, boolean recur) {
		String filterSql = getFilterSql(fct, "FILENAME_", filters, filterType);
		if (filterSql == null)
			return null;

		String nodeCondition = getNodeCondition(node, recur, fct.getDialect().getDataBaseInfo().isTeradata());
		if (filterSql.length() == 0) {
			return nodeCondition;
		}
		return StrFunc.isNull(nodeCondition) ? filterSql : filterSql + " and " + nodeCondition;
	}

	private String getNodeCondition(VfsNode node, boolean recur, boolean isTeradata) {
		if ("/".equalsIgnoreCase(node.getFileName())) {//根目录
			if (recur) {
				return ESCEPTROOT;
			}
			return "PARENTDIR_ = '/' and " + ESCEPTROOT;
		}
		else {
			if (recur) {
				return "PARENTDIR_ like '" + node.getParentDir() + node.getFileName() + "/%' and " 
						+ ESCEPTROOT;
			}
			return "PARENTDIR_ = '" + node.getParentDir() + node.getFileName() + "/' and " 
						+ ESCEPTROOT;
		}
	}

	private String getFilterSql(ConnectionFactory fct, String field, String filters, int filterType) {
		boolean filterFile = (VfsFile2.FILTERFILE & filterType) != 0;
		boolean filterFolder = (VfsFile2.FILTERFOLDER & filterType) != 0;
		boolean reserveFile = (VfsFile2.RESERVEFILE & filterType) != 0;
		boolean reserveFolder = (VfsFile2.RESERVEFOLDER & filterType) != 0;

		String[] fs = filters == null || filters.length() == 0 ? null : filters.split(",");
		String filterstr = getFilterStr(fct, field, fs);
		if (filterstr == null) {
			if (reserveFile && reserveFolder) {// 均保留
				return "";
			}
			if (reserveFile) {// 保留文件
				return isfile2str(true);
			}
			if (reserveFolder) {// 保留文件夹
				return isfile2str(false);
			}
			return null;// 均不保留，任何文件没有返回
		}
		else {// 保留优先级高于过滤
			if (reserveFile && reserveFolder) {// 都保留
				return "";
			}
			if (reserveFile) {// 保留文件
				if (filterFolder) {// 过滤文件夹
					return " (" + isfile2str(true) + " or (" + isfile2str(false) + " and " + filterstr + "))";
				}
				return isfile2str(true);
			}
			if (reserveFolder) {// 保留文件夹
				if (filterFile) {// 过滤文件
					return " (" + isfile2str(false) + " or (" + isfile2str(true) + " and " + filterstr + "))";
				}
				return isfile2str(false);
			}
			if (filterFile && filterFolder) {// 都过滤
				return filterstr;
			}
			if (filterFile) {// 过滤文件
				return " (" + isfile2str(true) + " and " + filterstr + ")";
			}
			if (filterFolder) {// 过滤文件夹
				return " (" + isfile2str(false) + " and " + filterstr + ")";
			}
			return null;// 都不过滤
		}
	}

	private String isfile2str(boolean isfile) {
		return "ISFILE_='" + (isfile ? "1" : "0") + "'";
	}

	private String getFilterStr(ConnectionFactory fct, String field, String[] filters) {
		int len = filters == null ? 0 : filters.length;
		if (len == 0)
			return null;
		Dialect dialect = fct.getDialect();
		StringBuffer buf = new StringBuffer(len * 30);
		buf.append("(");
		for (int i = 0; i < len; i++) {
			if (i != 0)
				buf.append(" or ");
			String value = filters[i];
			if (StrFunc.indexOf(value, LIKEWORDS) == -1) {
				buf.append("(").append(field).append("='").append(dialect.formatConstStr(value)).append("')");
			}
			else {
				buf.append("(").append(field).append(" like ").append(formatCondition(dialect, value)).append(")");
			}
		}
		buf.append(")");
		return buf.toString();
	}

	private String formatCondition(Dialect dialect, String value) {
		return dialect.formatLikeCondition(value);
	}
}
