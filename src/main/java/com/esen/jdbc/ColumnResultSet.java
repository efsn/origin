package com.esen.jdbc;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

import com.esen.util.ExceptionHandler;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 多个结果集合并的类；
 * 根据relation提供的信息进行列合并；
 * 列合并规则：每个结果集去掉排序字段，按结果集顺序排列其他字段，最后加上排序字段；
 * 合并后的结果集只支持按序号遍历字段值；
 * @author user
 *
 */
public class ColumnResultSet implements ResultSet {
	private ResultSetUnit[] rsus;

	private int[] hasNext;

	private int[] relation;

	private boolean desc;

	private String[][] orderby;//记录每个结果集中，某一行每个排序字段的值；

	private int markIndex;//每次next()后，确定排序字段在哪个结果集中；

	private ColumnResultSet(ResultSet[] rss, int relation[], boolean desc) {
		this.relation = relation;
		this.desc = desc;
		this.rsus = new ResultSetUnit[rss.length];
		for (int i = 0; i < rsus.length; i++)
			rsus[i] = new ResultSetUnit(rss[i]);
		hasNext = new int[rsus.length];

		orderby = new String[rsus.length][relation.length];
	}

	/**
	 * relation值是对应rss的排序字段位置；
	 * relation元素的格式：
	 * {-2,-1} {1,2} {1} {-1}
	 * 负数表示从末尾开始的第几列；
	 * 新的结果集，其他字段按数组顺序排列，最后加上排序字段；
	 * desc排序方式，所有rs排序方式都必须一致，要么都降序，要么都升序；
	 * desc=true 降序；
	 * @param rss
	 * @param relation
	 * @return
	 */
	public static final ResultSet getInstance(ResultSet[] rss, int relation[], boolean desc) {
		if (rss == null || rss.length == 0) {
			//      throw new RuntimeException("结果集为空！");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.columnresultset.resultnull",
					"结果集为空！"));
		}
		return new ColumnResultSet(rss, relation, desc);
	}

	public boolean next() throws SQLException {
		boolean flag = true;
		for (int i = 0; i < hasNext.length; i++) {
			hasNext[i] = rsus[i].next2();
			flag = flag && hasNext[i] < 0;
			for (int j = 0; j < relation.length; j++) {
				if (hasNext[i] > 0) {
					orderby[i][j] = rsus[i].getString(rsus[i].getOrderbyIndex()[j]);
				}
				if (hasNext[i] == 0)//到最后一行
					orderby[i][j] = null;
			}
		}

		if (flag)
			return !flag;
		String[] markOrderbyStr = getMarkOrderbyStr();
		if (isNull(markOrderbyStr))
			return false;
		for (int i = 0; i < orderby.length; i++) {
			if (hasNext[i] == 0) {
				rsus[i].setNext(false);
				continue;
			}
			String temp[] = orderby[i];
			boolean next = true;
			for (int j = 0; j < temp.length; j++) {
				next = next && StrFunc.compareStr(temp[j], markOrderbyStr[j]);
			}
			rsus[i].setNext(next);
		}
		return !flag;
	}

	private boolean isNull(String[] strs) {
		if (strs == null)
			return true;
		boolean f = true;
		for (int i = 0; i < strs.length; i++) {
			f = f && strs[i] == null;
		}
		return f;
	}

	/**
	 * 获得排序基准值；
	 * 升序取最小值，降序取最大值；
	 * @return
	 */
	private String[] getMarkOrderbyStr() {
		String[] markOrderbyStr = orderby[0];
		markIndex = 0;
		for (int i = 1; i < orderby.length; i++) {
			String temp[] = orderby[i];
			if (temp == null)
				continue;
			if (markOrderbyStr == null) {
				markOrderbyStr = temp;
				markIndex = i;
				continue;
			}
			for (int j = 0; j < temp.length; j++) {
				if (temp[j] == null)
					continue;
				if (markOrderbyStr[j] == null)
					continue;
				if (desc) {//降序
					if (temp[j].compareTo(markOrderbyStr[j]) <= 0) {
						break;
					}
					markOrderbyStr = temp;
					markIndex = i;
				}
				else {//升序
					if (temp[j].compareTo(markOrderbyStr[j]) >= 0) {
						break;
					}
					markOrderbyStr = temp;
					markIndex = i;
				}
			}
		}
		return markOrderbyStr;
	}

	public void close() throws SQLException {
		for (int i = 0; i < rsus.length; i++) {
			if (rsus[i] != null)
				rsus[i].close();
		}
	}

	public boolean wasNull() throws SQLException {
		for (int i = 0; i < rsus.length; i++) {
			if (rsus[i].wasNull())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * 根据columnIndex返回一个二元数组；
	 * 第一个元素表示在哪个结果集；
	 * 第二个元素表示字段在确定的结果集的第几个位置；
	 * @param columnIndex
	 * @return
	 */
	private int[] getColumnIndex(int columnIndex) {
		int[] index = new int[2];
		int x = columnIndex;
		for (int i = 0; i < rsus.length; i++) {
			index[0] = i;
			int len = rsus[i].getColumnCount() - relation.length;
			if (x <= len) {
				index[1] = getIndex(i, x);
				return index;
			}
			x = x - len;
		}
		if (x > 0) {//遍历排序字段
			if (x > relation.length)
				//        throw new RuntimeException("超出了遍历范围："+columnIndex);
				throw new RuntimeException(I18N.getString("com.esen.jdbc.columnresultset.outofindex",
						"超出了遍历范围：{0}", new Object[] { String.valueOf(columnIndex) }));
			index[0] = markIndex;
			index[1] = rsus[markIndex].getOrderbyIndex()[x - 1];
		}
		return index;
	}

	private int getIndex(int i, int index) {
		int pos = index;
		int orderbyIndex[] = rsus[i].getOrderbyIndex();
		for (int k = 0; k < orderbyIndex.length; k++) {
			if (orderbyIndex[k] <= index)
				pos++;
		}
		return pos;
	}

	public String getString(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getString(index[1]);
	}

	public boolean getBoolean(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getBoolean(index[1]);
	}

	public byte getByte(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getByte(index[1]);
	}

	public short getShort(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getShort(index[1]);
	}

	public int getInt(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getInt(index[1]);
	}

	public long getLong(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getLong(index[1]);
	}

	public float getFloat(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getFloat(index[1]);
	}

	public double getDouble(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getDouble(index[1]);
	}

	public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getBigDecimal(index[1], scale);
	}

	public byte[] getBytes(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getBytes(index[1]);
	}

	public Date getDate(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getDate(index[1]);
	}

	public Time getTime(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getTime(index[1]);
	}

	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getTimestamp(index[1]);
	}

	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getAsciiStream(index[1]);
	}

	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getUnicodeStream(index[1]);
	}

	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getBinaryStream(index[1]);
	}

	public String getString(String columnName) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean getBoolean(String columnName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public byte getByte(String columnName) throws SQLException {
		//throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public short getShort(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public int getInt(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public long getLong(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public float getFloat(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public double getDouble(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public byte[] getBytes(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public Date getDate(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public Time getTime(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public Timestamp getTimestamp(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public InputStream getAsciiStream(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public InputStream getUnicodeStream(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public InputStream getBinaryStream(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public SQLWarning getWarnings() throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public void clearWarnings() throws SQLException {
		for (int i = 0; i < rsus.length; i++)
			rsus[i].clearWarnings();
	}

	public String getCursorName() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public ResultSetMetaData getMetaData() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Object getObject(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getObject(index[1]);
	}

	public Object getObject(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public int findColumn(String columnName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Reader getCharacterStream(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getCharacterStream(index[1]);
	}

	public Reader getCharacterStream(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getBigDecimal(index[1]);
	}

	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		// throw new UnsupportedOperationException("不支持字段名取值！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportnamefield",
				"不支持字段名取值！"));
	}

	public boolean isBeforeFirst() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean isAfterLast() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean isFirst() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean isLast() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void beforeFirst() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void afterLast() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean first() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean last() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public int getRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean absolute(int row) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean relative(int rows) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean previous() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void setFetchDirection(int direction) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public int getFetchDirection() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void setFetchSize(int rows) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public int getFetchSize() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public int getType() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public int getConcurrency() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean rowUpdated() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean rowInserted() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public boolean rowDeleted() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateNull(int columnIndex) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateByte(int columnIndex, byte x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateShort(int columnIndex, short x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateInt(int columnIndex, int x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateLong(int columnIndex, long x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateFloat(int columnIndex, float x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateDouble(int columnIndex, double x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateString(int columnIndex, String x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateDate(int columnIndex, Date x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateTime(int columnIndex, Time x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateObject(int columnIndex, Object x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateNull(String columnName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBoolean(String columnName, boolean x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateByte(String columnName, byte x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateShort(String columnName, short x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateInt(String columnName, int x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateLong(String columnName, long x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateFloat(String columnName, float x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateDouble(String columnName, double x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateString(String columnName, String x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBytes(String columnName, byte[] x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateDate(String columnName, Date x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateTime(String columnName, Time x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateObject(String columnName, Object x, int scale) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateObject(String columnName, Object x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void insertRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void deleteRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void refreshRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void cancelRowUpdates() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void moveToInsertRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void moveToCurrentRow() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Statement getStatement() throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Object getObject(int i, Map map) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Ref getRef(int i) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Blob getBlob(int i) throws SQLException {
		int index[] = getColumnIndex(i);
		return rsus[index[0]].getBlob(index[1]);
	}

	public Clob getClob(int i) throws SQLException {
		int index[] = getColumnIndex(i);
		return rsus[index[0]].getClob(index[1]);
	}

	public Array getArray(int i) throws SQLException {
		int index[] = getColumnIndex(i);
		return rsus[index[0]].getArray(index[1]);
	}

	public Object getObject(String colName, Map map) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Ref getRef(String colName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Blob getBlob(String colName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Clob getClob(String colName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Array getArray(String colName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getDate(index[1], cal);
	}

	public Date getDate(String columnName, Calendar cal) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getTime(index[1], cal);
	}

	public Time getTime(String columnName, Calendar cal) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getTimestamp(index[1], cal);
	}

	public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public URL getURL(int columnIndex) throws SQLException {
		int index[] = getColumnIndex(columnIndex);
		return rsus[index[0]].getURL(index[1]);
	}

	public URL getURL(String columnName) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateRef(int columnIndex, Ref x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateRef(String columnName, Ref x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateBlob(String columnName, Blob x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateClob(int columnIndex, Clob x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateClob(String columnName, Clob x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateArray(int columnIndex, Array x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	public void updateArray(String columnName, Array x) throws SQLException {
		//    throw new UnsupportedOperationException("不支持的方法！");
		throw new UnsupportedOperationException(I18N.getString("com.esen.jdbc.columnresultset.unsupportmethod",
				"不支持的方法！"));
	}

	class ResultSetUnit implements ResultSet {
		private ResultSet rs;

		private boolean needNext;//是否需要next，初始值为true，获得第一条记录；

		private int[] orderbyIndex;//记录排序字段的位置

		private int columnCount;

		public ResultSetUnit(ResultSet rs) {
			this.rs = rs;
			needNext = true;
			orderbyIndex = new int[relation.length];
			try {
				columnCount = getMetaData().getColumnCount();
				for (int i = 0; i < orderbyIndex.length; i++) {
					if (relation[i] > 0)
						orderbyIndex[i] = relation[i];
					if (relation[i] < 0) {
						orderbyIndex[i] = columnCount + relation[i] + 1;
					}
				}
			}
			catch (SQLException se) {
				ExceptionHandler.rethrowRuntimeException(se);
			}
		}

		public int[] getOrderbyIndex() {
			return orderbyIndex;
		}

		public int getColumnCount() {
			return columnCount;
		}

		public void setNext(boolean next) {
			needNext = next;
		}

		public boolean getNext() {
			return needNext;
		}

		public boolean next() throws SQLException {
			return false;
		}

		public int next2() throws SQLException {
			if (needNext)
				return rs.next() ? 1 : 0;
			return -1;
		}

		public void close() throws SQLException {
			rs.close();
		}

		public boolean wasNull() throws SQLException {
			return rs.wasNull();
		}

		public String getString(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getString(columnIndex);
			return null;
		}

		public boolean getBoolean(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getBoolean(columnIndex);
			return false;
		}

		public byte getByte(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getByte(columnIndex);
			return 0;
		}

		public short getShort(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getShort(columnIndex);
			return 0;
		}

		public int getInt(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getInt(columnIndex);
			return 0;
		}

		public long getLong(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getLong(columnIndex);
			return 0;
		}

		public float getFloat(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getFloat(columnIndex);
			return 0;
		}

		public double getDouble(int columnIndex) throws SQLException {
			if (needNext) {
				double v = rs.getDouble(columnIndex);
				if (rs.wasNull())
					return Double.NaN;
				return v;
			}
			return Double.NaN;
		}

		public BigDecimal getBigDecimal(int columnIndex, int scale) throws SQLException {
			if (needNext)
				return rs.getBigDecimal(columnIndex);
			return null;
		}

		public byte[] getBytes(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getBytes(columnIndex);
			return null;
		}

		public Date getDate(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getDate(columnIndex);
			return null;
		}

		public Time getTime(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getTime(columnIndex);
			return null;
		}

		public Timestamp getTimestamp(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getTimestamp(columnIndex);
			return null;
		}

		public InputStream getAsciiStream(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getAsciiStream(columnIndex);
			return null;
		}

		public InputStream getUnicodeStream(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getUnicodeStream(columnIndex);
			return null;
		}

		public InputStream getBinaryStream(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getBinaryStream(columnIndex);
			return null;
		}

		public String getString(String columnName) throws SQLException {
			if (needNext)
				return rs.getString(columnName);
			return null;
		}

		public boolean getBoolean(String columnName) throws SQLException {
			if (needNext)
				return rs.getBoolean(columnName);
			return false;
		}

		public byte getByte(String columnName) throws SQLException {
			if (needNext)
				return rs.getByte(columnName);
			return 0;
		}

		public short getShort(String columnName) throws SQLException {
			if (needNext)
				return rs.getShort(columnName);
			return 0;
		}

		public int getInt(String columnName) throws SQLException {
			if (needNext)
				return rs.getInt(columnName);
			return 0;
		}

		public long getLong(String columnName) throws SQLException {
			if (needNext)
				return rs.getLong(columnName);
			return 0;
		}

		public float getFloat(String columnName) throws SQLException {
			if (needNext)
				return rs.getFloat(columnName);
			return 0;
		}

		public double getDouble(String columnName) throws SQLException {
			if (needNext)
				return rs.getDouble(columnName);
			return 0;
		}

		public BigDecimal getBigDecimal(String columnName, int scale) throws SQLException {
			if (needNext)
				return rs.getBigDecimal(columnName);
			return null;
		}

		public byte[] getBytes(String columnName) throws SQLException {
			if (needNext)
				return rs.getBytes(columnName);
			return null;
		}

		public Date getDate(String columnName) throws SQLException {
			if (needNext)
				return rs.getDate(columnName);
			return null;
		}

		public Time getTime(String columnName) throws SQLException {
			if (needNext)
				return rs.getTime(columnName);
			return null;
		}

		public Timestamp getTimestamp(String columnName) throws SQLException {
			if (needNext)
				return rs.getTimestamp(columnName);
			return null;
		}

		public InputStream getAsciiStream(String columnName) throws SQLException {
			if (needNext)
				return rs.getAsciiStream(columnName);
			return null;
		}

		public InputStream getUnicodeStream(String columnName) throws SQLException {
			if (needNext)
				return rs.getUnicodeStream(columnName);
			return null;
		}

		public InputStream getBinaryStream(String columnName) throws SQLException {
			if (needNext)
				return rs.getBinaryStream(columnName);
			return null;
		}

		public SQLWarning getWarnings() throws SQLException {

			return rs.getWarnings();
		}

		public void clearWarnings() throws SQLException {
			rs.clearWarnings();

		}

		public String getCursorName() throws SQLException {
			return rs.getCursorName();
		}

		public ResultSetMetaData getMetaData() throws SQLException {
			return rs.getMetaData();
		}

		public Object getObject(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getObject(columnIndex);
			return null;
		}

		public Object getObject(String columnName) throws SQLException {
			if (needNext)
				return rs.getObject(columnName);
			return null;
		}

		public int findColumn(String columnName) throws SQLException {
			return rs.findColumn(columnName);
		}

		public Reader getCharacterStream(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getCharacterStream(columnIndex);
			return null;
		}

		public Reader getCharacterStream(String columnName) throws SQLException {
			if (needNext)
				return rs.getCharacterStream(columnName);
			return null;
		}

		public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getBigDecimal(columnIndex);
			return null;
		}

		public BigDecimal getBigDecimal(String columnName) throws SQLException {
			if (needNext)
				return rs.getBigDecimal(columnName);
			return null;
		}

		public boolean isBeforeFirst() throws SQLException {
			return rs.isBeforeFirst();
		}

		public boolean isAfterLast() throws SQLException {
			return rs.isAfterLast();
		}

		public boolean isFirst() throws SQLException {
			return rs.isFirst();
		}

		public boolean isLast() throws SQLException {
			return rs.isLast();
		}

		public void beforeFirst() throws SQLException {
			rs.beforeFirst();
		}

		public void afterLast() throws SQLException {
			rs.afterLast();
		}

		public boolean first() throws SQLException {
			return rs.first();
		}

		public boolean last() throws SQLException {
			return rs.last();
		}

		public int getRow() throws SQLException {
			return rs.getRow();
		}

		public boolean absolute(int row) throws SQLException {
			return rs.absolute(row);
		}

		public boolean relative(int rows) throws SQLException {
			return rs.relative(rows);
		}

		public boolean previous() throws SQLException {
			return rs.previous();
		}

		public void setFetchDirection(int direction) throws SQLException {
			rs.setFetchDirection(direction);
		}

		public int getFetchDirection() throws SQLException {
			return rs.getFetchDirection();
		}

		public void setFetchSize(int rows) throws SQLException {
			rs.setFetchSize(rows);
		}

		public int getFetchSize() throws SQLException {
			return rs.getFetchSize();
		}

		public int getType() throws SQLException {
			return rs.getType();
		}

		public int getConcurrency() throws SQLException {
			return rs.getConcurrency();
		}

		public boolean rowUpdated() throws SQLException {
			return rs.rowUpdated();
		}

		public boolean rowInserted() throws SQLException {
			return rs.rowInserted();
		}

		public boolean rowDeleted() throws SQLException {
			return rs.rowDeleted();
		}

		public void updateNull(int columnIndex) throws SQLException {
			rs.updateNull(columnIndex);
		}

		public void updateBoolean(int columnIndex, boolean x) throws SQLException {
			rs.updateBoolean(columnIndex, x);
		}

		public void updateByte(int columnIndex, byte x) throws SQLException {
			rs.updateByte(columnIndex, x);
		}

		public void updateShort(int columnIndex, short x) throws SQLException {
			rs.updateShort(columnIndex, x);
		}

		public void updateInt(int columnIndex, int x) throws SQLException {
			rs.updateInt(columnIndex, x);
		}

		public void updateLong(int columnIndex, long x) throws SQLException {
			rs.updateLong(columnIndex, x);
		}

		public void updateFloat(int columnIndex, float x) throws SQLException {
			rs.updateFloat(columnIndex, x);
		}

		public void updateDouble(int columnIndex, double x) throws SQLException {
			rs.updateDouble(columnIndex, x);
		}

		public void updateBigDecimal(int columnIndex, BigDecimal x) throws SQLException {
			rs.updateBigDecimal(columnIndex, x);
		}

		public void updateString(int columnIndex, String x) throws SQLException {
			rs.updateString(columnIndex, x);
		}

		public void updateBytes(int columnIndex, byte[] x) throws SQLException {
			rs.updateBytes(columnIndex, x);
		}

		public void updateDate(int columnIndex, Date x) throws SQLException {
			rs.updateDate(columnIndex, x);
		}

		public void updateTime(int columnIndex, Time x) throws SQLException {
			rs.updateTime(columnIndex, x);
		}

		public void updateTimestamp(int columnIndex, Timestamp x) throws SQLException {
			rs.updateTimestamp(columnIndex, x);
		}

		public void updateAsciiStream(int columnIndex, InputStream x, int length) throws SQLException {
			rs.updateAsciiStream(columnIndex, x, length);
		}

		public void updateBinaryStream(int columnIndex, InputStream x, int length) throws SQLException {
			rs.updateBinaryStream(columnIndex, x, length);
		}

		public void updateCharacterStream(int columnIndex, Reader x, int length) throws SQLException {
			rs.updateCharacterStream(columnIndex, x, length);
		}

		public void updateObject(int columnIndex, Object x, int scale) throws SQLException {
			rs.updateObject(columnIndex, x, scale);
		}

		public void updateObject(int columnIndex, Object x) throws SQLException {
			rs.updateObject(columnIndex, x);
		}

		public void updateNull(String columnName) throws SQLException {
			rs.updateNull(columnName);
		}

		public void updateBoolean(String columnName, boolean x) throws SQLException {
			rs.updateBoolean(columnName, x);
		}

		public void updateByte(String columnName, byte x) throws SQLException {
			rs.updateByte(columnName, x);
		}

		public void updateShort(String columnName, short x) throws SQLException {
			rs.updateShort(columnName, x);
		}

		public void updateInt(String columnName, int x) throws SQLException {
			rs.updateInt(columnName, x);
		}

		public void updateLong(String columnName, long x) throws SQLException {
			rs.updateLong(columnName, x);
		}

		public void updateFloat(String columnName, float x) throws SQLException {
			rs.updateFloat(columnName, x);
		}

		public void updateDouble(String columnName, double x) throws SQLException {
			rs.updateDouble(columnName, x);
		}

		public void updateBigDecimal(String columnName, BigDecimal x) throws SQLException {
			rs.updateBigDecimal(columnName, x);
		}

		public void updateString(String columnName, String x) throws SQLException {
			rs.updateString(columnName, x);
		}

		public void updateBytes(String columnName, byte[] x) throws SQLException {
			rs.updateBytes(columnName, x);
		}

		public void updateDate(String columnName, Date x) throws SQLException {
			rs.updateDate(columnName, x);
		}

		public void updateTime(String columnName, Time x) throws SQLException {
			rs.updateTime(columnName, x);
		}

		public void updateTimestamp(String columnName, Timestamp x) throws SQLException {
			rs.updateTimestamp(columnName, x);
		}

		public void updateAsciiStream(String columnName, InputStream x, int length) throws SQLException {
			rs.updateAsciiStream(columnName, x, length);
		}

		public void updateBinaryStream(String columnName, InputStream x, int length) throws SQLException {
			rs.updateBinaryStream(columnName, x, length);
		}

		public void updateCharacterStream(String columnName, Reader reader, int length) throws SQLException {
			rs.updateCharacterStream(columnName, reader, length);
		}

		public void updateObject(String columnName, Object x, int scale) throws SQLException {
			rs.updateObject(columnName, x, scale);
		}

		public void updateObject(String columnName, Object x) throws SQLException {
			rs.updateObject(columnName, x);
		}

		public void insertRow() throws SQLException {
			rs.insertRow();
		}

		public void updateRow() throws SQLException {
			rs.updateRow();
		}

		public void deleteRow() throws SQLException {
			rs.deleteRow();
		}

		public void refreshRow() throws SQLException {
			rs.refreshRow();
		}

		public void cancelRowUpdates() throws SQLException {
			rs.cancelRowUpdates();
		}

		public void moveToInsertRow() throws SQLException {
			rs.moveToInsertRow();
		}

		public void moveToCurrentRow() throws SQLException {
			rs.moveToCurrentRow();
		}

		public Statement getStatement() throws SQLException {
			return rs.getStatement();
		}

		public Object getObject(int i, Map map) throws SQLException {
			if (needNext)
				return rs.getObject(i, map);
			return null;
		}

		public Ref getRef(int i) throws SQLException {
			if (needNext)
				return rs.getRef(i);
			return null;
		}

		public Blob getBlob(int i) throws SQLException {
			if (needNext)
				return rs.getBlob(i);
			return null;
		}

		public Clob getClob(int i) throws SQLException {
			if (needNext)
				return rs.getClob(i);
			return null;
		}

		public Array getArray(int i) throws SQLException {
			if (needNext)
				return rs.getArray(i);
			return null;
		}

		public Object getObject(String colName, Map map) throws SQLException {
			if (needNext)
				return rs.getObject(colName, map);
			return null;
		}

		public Ref getRef(String colName) throws SQLException {
			if (needNext)
				return rs.getRef(colName);
			return null;
		}

		public Blob getBlob(String colName) throws SQLException {
			if (needNext)
				return rs.getBlob(colName);
			return null;
		}

		public Clob getClob(String colName) throws SQLException {
			if (needNext)
				return rs.getClob(colName);
			return null;
		}

		public Array getArray(String colName) throws SQLException {
			if (needNext)
				return rs.getArray(colName);
			return null;
		}

		public Date getDate(int columnIndex, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getDate(columnIndex, cal);
			return null;
		}

		public Date getDate(String columnName, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getDate(columnName, cal);
			return null;
		}

		public Time getTime(int columnIndex, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getTime(columnIndex, cal);
			return null;
		}

		public Time getTime(String columnName, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getTime(columnName, cal);
			return null;
		}

		public Timestamp getTimestamp(int columnIndex, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getTimestamp(columnIndex, cal);
			return null;
		}

		public Timestamp getTimestamp(String columnName, Calendar cal) throws SQLException {
			if (needNext)
				return rs.getTimestamp(columnName, cal);
			return null;
		}

		public URL getURL(int columnIndex) throws SQLException {
			if (needNext)
				return rs.getURL(columnIndex);
			return null;
		}

		public URL getURL(String columnName) throws SQLException {
			if (needNext)
				return rs.getURL(columnName);
			return null;
		}

		public void updateRef(int columnIndex, Ref x) throws SQLException {
			rs.updateRef(columnIndex, x);
		}

		public void updateRef(String columnName, Ref x) throws SQLException {
			rs.updateRef(columnName, x);
		}

		public void updateBlob(int columnIndex, Blob x) throws SQLException {
			rs.updateBlob(columnIndex, x);
		}

		public void updateBlob(String columnName, Blob x) throws SQLException {
			rs.updateBlob(columnName, x);
		}

		public void updateClob(int columnIndex, Clob x) throws SQLException {
			rs.updateClob(columnIndex, x);
		}

		public void updateClob(String columnName, Clob x) throws SQLException {
			rs.updateClob(columnName, x);
		}

		public void updateArray(int columnIndex, Array x) throws SQLException {
			// TODO Auto-generated method stub

		}

		public void updateArray(String columnName, Array x) throws SQLException {
			// TODO Auto-generated method stub

		}

	}
}
