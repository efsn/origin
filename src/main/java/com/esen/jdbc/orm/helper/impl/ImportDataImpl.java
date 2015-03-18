package com.esen.jdbc.orm.helper.impl;

import java.io.InputStream;
import java.io.Reader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.w3c.dom.Document;

import com.esen.jdbc.DataCopy;
import com.esen.jdbc.data.DataCopyForUpdateImpl;
import com.esen.jdbc.data.impl.DataReaderFromCSV;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.orm.Blob;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.FileBlob;
import com.esen.jdbc.orm.Index;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.jdbc.orm.helper.ImportDataHelper;
import com.esen.jdbc.orm.helper.SCEntityInfo;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.impl.ORMUtil;
import com.esen.jdbc.orm.impl.SessionImpl;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

public class ImportDataImpl {
	private Session session;
	
	private ImportDataHelper imhelper;
	
	private EntityInfo entityInfo;
	
	private Reader in;
	
	private boolean hasAutoInc = false;
	private boolean hasKey = false;
	private boolean hasUniqueIndex = false;
	
	//方言
	private Dialect dl;
	
	//数据库属性
	private DataBaseInfo dbinfo;
	
	//导入的数据的唯一标识字段
	private String[] keys = null;
	
	//需要导入数据的字段的字段名
	private String[] fields = null;
	
	//需要导入数据的字段在数据文件中的序号
	private int[] fieldIndexes = null;
	
	//需要导入数据的字段的对应的 Property
	private Property[] fieldProps = null;
	
	//写入的数据行数
	private int cnt = 0;

	public ImportDataImpl(Session session, EntityInfo entityInfo, ImportDataHelper imhelper, Reader in) {
		if (session == null || entityInfo == null || imhelper == null || in == null) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.importdataimpl.1","参数不能为空");
		}
		
		this.session = session;
		this.entityInfo = entityInfo;
		this.imhelper = imhelper;
		this.in = in;
		
		parseInfo();	
	}
	
	/**
	 * 分析参数中的信息，判断导入数据所需的信息是否都提供了
	 */
	private void parseInfo() {
		Map nameMapping = imhelper.getNameMapping();		
		//nameMapping 为空时，取 entityInfo 中所有字段
		if (nameMapping == null) {
			List props = entityInfo.getProperties();
			nameMapping = new HashMap();			
			for (int i = 0; i < props.size(); i++) {
				nameMapping.put(((Property)props.get(i)).getFieldName(), i);
			}
		} else {
			//将 defaultValue 中的字段名增量添加到 nameMapping 中
			Map defaultValue = imhelper.getDefaultValueMap();
			if (defaultValue != null) {
				List list = new ArrayList(defaultValue.keySet());
				for (int i = 0; list != null && i < list.size(); i++) {
					Object name = list.get(i);
					if (!nameMapping.containsKey(name)) {
						nameMapping.put(name, -1);
					}
				}
			}
		}
		
		//upids 均重置为空 
		if (entityInfo instanceof TreeEntityInfo) {
			String[] upids = StrFunc.splitByChar(((TreeEntityInfo)entityInfo).getUpidsPropertyName(), ',');
			if (upids != null && upids.length > 0) {
				for (int i = 0; i < upids.length; i++) {
					Property prop = entityInfo.getProperty(upids[i]);
					if (prop != null) {
						nameMapping.remove(prop.getFieldName());
					}
				}
			}
		}

		/*
		 * 判断写入的字段中是否包含唯一标识符（更新或者追加数据时必须有）
		 * 
		 * 1.如果是缓慢变化结构，且写入的字段中包含 key、fromdate 和  todate，则这三个字段定位唯一标识符。
		 * 2.不满足 1 的情况下，如果有主键：
		 *   2.1 主键不是自增列，将主键定为唯一标识符
		 *   2.2 主键是自增列，检查索引
		 *   	 2.2.1 有唯一索引，将唯一索引定位唯一标示符
		 *       2.2.2 没有唯一索引，将自增列定位唯一标示符
		 * 3.以上条件均不满足，且导入数据为更新或者追加方式时，报错
		 */
		Property key = entityInfo.getPrimaryKey();
		if (key != null && nameMapping.containsKey(key.getFieldName())) {
			hasKey = true;
			if (key.isAutoInc()) {
				hasAutoInc = true;
			}
		}
		
		String[] indexfields = null;
		
		//特殊处理缓慢变化的主键
		if (hasKey && (entityInfo instanceof SCEntityInfo)) {
			String fromDate = ((SCEntityInfo)entityInfo).getFromDatePropertyName();
			String toDate = ((SCEntityInfo)entityInfo).getToDatePropertyName();
			Property fromDateProp = entityInfo.getProperty(fromDate);
			Property toDateProp = entityInfo.getProperty(toDate);
			if (fromDateProp != null
					&& nameMapping.containsKey(fromDateProp.getFieldName())
					&& toDateProp != null
					&& nameMapping.containsKey(toDateProp.getFieldName())) {
				hasUniqueIndex = true;
				indexfields = new String[3];
				indexfields[0] = key.getFieldName();
				indexfields[1] = fromDateProp.getFieldName();
				indexfields[2] = toDateProp.getFieldName();
			}
		}
		
		if (!hasUniqueIndex 
				&& (!hasKey || hasAutoInc)) {
			List<Index> indexes = entityInfo.listIndexes();
			for (int i = 0; indexes != null && i < indexes.size(); i++) {
				Index ind = (Index)indexes.get(i);
				if (ind.isUnique()) {
					indexfields = StrFunc.splitByChar(ind.getIndexFields(), ',');
					int j = 0;
					for (j = 0; j < indexfields.length; j++) {
						if (!nameMapping.containsKey(indexfields[j])) {
							continue;
						}
					}
					if (j == indexfields.length && j != 0) {
						hasUniqueIndex = true;
						break;
					}
				}
			}
		}
		
		switch (imhelper.getOption()) {
		case ImportDataHelper.OPT_CLEARTABLE:
		case ImportDataHelper.OPT_ADD:
			//有自增列时，去掉自增列
			if (hasAutoInc) {
				nameMapping.remove(key.getFieldName());
				hasAutoInc = false;
			}
			break;
			
		case ImportDataHelper.OPT_APPEND:
		case ImportDataHelper.OPT_UPDATE:
			//必须有能唯一标识该行数据的信息
			if (!hasKey && !hasUniqueIndex) {
				throw new ORMException("com.esen.jdbc.orm.helper.impl.importdataimpl.2","导入的数据文件中必须包含能唯一标识该行的字段");
			}
			
			//有自增列和唯一索引，以唯一索引为唯一标识，去掉自增列
			if (hasAutoInc && hasUniqueIndex) {
				nameMapping.remove(key.getFieldName());
				hasAutoInc = false;
			}
			
			//确定数据的唯一标识
			if (hasUniqueIndex) {
				this.keys = indexfields;
			} else {
				this.keys = new String[1];
				this.keys[0] = key.getFieldName();
			}
			break;
		
		default: 
			break;			
		}
		
		//分析需要写入的字段，字段属性，字段在数据文件中的序号
		List list = new ArrayList(nameMapping.keySet());	
		this.fields = (String[])list.toArray(new String[list.size()]);
		this.fieldIndexes = new int[fields.length];
		this.fieldProps = new Property[fields.length];
		for (int i = 0; i < fields.length; i++) {
			fieldProps[i] = entityInfo.getPropertyByField(fields[i]);
			if (fieldProps[i] == null) {
				throw new ORMException("com.esen.jdbc.orm.helper.impl.importdataimpl.3","实体{0}不存在字段名为{1}的属性",new Object[]{entityInfo.getEntityName(),fields[i]});
			}
			fieldIndexes[i] = (Integer) nameMapping.get(fields[i]);
		}
		
		dl = ((SessionImpl)session).getDialect();
		dbinfo = dl.getDataBaseInfo();
	}
	
	/**
	 * 更新数据
	 * 
	 * @param tmpTable
	 */
	private void updateData(String tmpTable) {
		try {
			Connection conn = session.getConnection();
			try {
				DataCopyForUpdateImpl.updateSomeFieldData(dl, conn, 
						tmpTable, entityInfo.getTable(), keys, fields, null);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new ORMException(e);
		}
	}
	
	/**
	 * 添加数据
	 * 
	 * @param tmpTable
	 */
	private void appendData(String tmpTable) {		
		try {
			Connection conn = session.getConnection();
			try {
				DataCopyForUpdateImpl.appendData(dl, conn, 
						tmpTable, entityInfo.getTable(), keys, fields, null);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new ORMException(e);
		}

	}
	
	/**
	 * 写入数据
	 */
	public void writeData() {
		String tmptable = null;
		String tablename = entityInfo.getTable();
		int option = imhelper.getOption();
		if (option == ImportDataHelper.OPT_APPEND
				|| option == ImportDataHelper.OPT_UPDATE) {
			tablename = createTmpTable();
			tmptable = tablename;
		}

		String insertSql = getInsertSql(tablename);
		
		try {
			DataReaderFromCSV rd = new DataReaderFromCSV(in, 0, imhelper.getSeparator(), '"');
			Connection conn = session.getConnection();
			try {
				if (option == ImportDataHelper.OPT_CLEARTABLE) {
					session.delete(entityInfo.getEntityName());
				}
				
				PreparedStatement pstmt = conn.prepareStatement(insertSql);
				try {
					while (rd.next()) {
						String[] line = rd.getLineValues();
						if (isComment(line)) {
							continue;
						}
						
						writeLine(pstmt, line);
						append(pstmt, false);
					}
					append(pstmt, true);
				} finally {
					pstmt.close();
				}
	
				if (option == ImportDataHelper.OPT_APPEND) {// 写入目的表不存在的记录			
					appendData(tmptable);
				} else if (option == ImportDataHelper.OPT_UPDATE) { // 增量更新；	
					updateData(tmptable);
				}
				
				if (!StrFunc.isNull(tmptable)) {
					dropTable(tmptable);
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.importdataimpl.4","导入数据出错", e);
		}
	}
	
	/**
	 * 根据需要导入的字段创建临时表
	 * 
	 * @return 临时表名
	 */
	private String createTmpTable() {
		DbDefiner def = ((SessionImpl)session).getDbDefiner();
		
		String pkName = entityInfo.getPrimaryKey().getName();
		for (int i = 0; i < fieldProps.length; i++) {
			Property prop = fieldProps[i];
			boolean isUnique = prop.isUnique();
			if (entityInfo instanceof SCEntityInfo
					&& StrFunc.compareStr(prop.getName(), pkName)) {
				isUnique = false;
			}
			def.defineField(prop.getFieldName(), prop.getType(), prop.length(), prop.getScale(),
					null, prop.isNullable(), isUnique);
		}
		StringBuilder tmpKey = new StringBuilder();
		for (int i = 0; i < keys.length; i++) {
			if (i != 0) {
				tmpKey.append(',');
			}
			tmpKey.append(keys[i]);
		}
		def.definePrimaryKey(tmpKey.toString());
		
		Connection conn = session.getConnection();
		try {
			try {
				return def.createTable(conn, entityInfo.getTable(), false);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
	
	/**
	 * 删除创建的临时表
	 * 
	 * @param tablename
	 */
	private void dropTable(String tablename) {
		DbDefiner def = ((SessionImpl)session).getDbDefiner();
		Connection conn = session.getConnection();
		try {
			try {
				def.dropTable(conn, null, tablename);
			} finally {
				conn.close();
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
	
	/**
	 * 构造插入数据的  sql 语句
	 * 
	 * @return sql语句
	 */
	private String getInsertSql(String tablename) {
		StringBuilder str = new StringBuilder();
		str.append("insert into " + tablename + "(");
		for (int i = 0; i < fields.length; i++) {
			if (i != 0) {
				str.append(",");
			}
			str.append(fields[i]);
		}
		str.append(") values(");
		for (int i = 0; i < fields.length; i++) {
			if (i != 0) {
				str.append(", ");
			}
			str.append("?");
		}
		str.append(")");
		
		return str.toString();
	}
	
	/**
	 * 判断当前行是不是注释
	 * 
	 * @param line
	 * @return
	 */
	private boolean isComment(String[] line) {
		if (line == null || line.length <= 0 || line[0].startsWith("#")) {
			return true;
		}

		return false;
	}
	
	/**
	 * 写入当前行数据
	 * 
	 * @param line
	 * @throws Exception
	 * @throws SQLException
	 */
	private void writeLine(PreparedStatement pstmt, String[] line) throws Exception, SQLException {
		Map defaultValue = imhelper.getDefaultValueMap();
		
		for (int i = 0; i < fields.length; i++) {
			//找到指定字段对应的字段列号；
			int p = fieldIndexes[i];
			Object obj = null;
			if (p >= 0 && p < line.length) {
				obj = line[p];
			}
			
			if (StrFunc.isNull((String) obj) && defaultValue != null) {
				obj = defaultValue.get(fields[i]);
			}
			
			setObject(pstmt, i, obj);
		}
	}
	
	private void setObject(PreparedStatement pstmt, int index, Object value) {
		Property prop = fieldProps[index];
		int i = index + 1;
		
		try {
			switch (prop.getType()) {
			case Property.FIELD_TYPE_LOGIC: 
				if (value == null) {
					pstmt.setNull(i, Types.VARCHAR);
				} else { //int, boolean, String
					boolean tmp = StrFunc.parseBoolean(value, false);
					if (!tmp) {
						pstmt.setString(i, "0");
					} else {
						pstmt.setString(i, "1");
					}
				}
				break;
				
			case Property.FIELD_TYPE_STR:
				if (value == null) {
					pstmt.setNull(i, Types.VARCHAR);
				} else if (value instanceof StringBuffer) {
					pstmt.setString(i, ((StringBuffer)value).toString());
				} else if (value instanceof StringBuilder) {
					pstmt.setString(i, ((StringBuilder)value).toString());
				} else if (value instanceof Character) {
					pstmt.setString(i, value.toString());
				} else {
					pstmt.setObject(i, value);
				}
				break;
	
			case Property.FIELD_TYPE_INT:
				if (value == null) {
					pstmt.setNull(i, Types.INTEGER);
				} else {
					pstmt.setInt(i, StrFunc.parseInt(value, 0));
				}
				break;
				
			case Property.FIELD_TYPE_FLOAT:
				if (value == null) {
					pstmt.setNull(i, Types.FLOAT);
				} else {
					pstmt.setDouble(i, StrFunc.parseDouble(value, 0));
				}
				break;
	
			case Property.FIELD_TYPE_DATE:
				if (value == null) {
					pstmt.setNull(i, Types.TIMESTAMP);
				} else if (value instanceof Calendar) {
					pstmt.setTimestamp(i, ORMUtil.calendar2Timestamp((Calendar)value));
				} else {
					pstmt.setObject(i, value);
				}
				break;
				
			case Property.FIELD_TYPE_CLOB:
				if (value == null) {
					pstmt.setNull(i, Types.CLOB);
					break;
				}
				
				if (value instanceof Document) {
					pstmt.setString(i, XmlFunc.document2str((Document)value, StrFunc.UTF8));
				} else {
					pstmt.setString(i, value.toString());
				}
				
				break;
				
			case Property.FIELD_TYPE_BINARY:
				if (value == null) {
					pstmt.setNull(i, java.sql.Types.BLOB);
					return;
				}
				
				if (value instanceof FileBlob) {
					InputStream in = ((FileBlob)value).getBinaryStream();
					try {
						pstmt.setBinaryStream(i, in, (int)((FileBlob)value).length());
					} finally {
						in.close();
					}
				} else if (value instanceof Blob) {
					pstmt.setBlob(i, (java.sql.Blob) value);
				} else if (value instanceof Document) {
					pstmt.setBytes(i, XmlFunc.document2bytes((Document)value, StrFunc.UTF8));
				} else {
					pstmt.setObject(i, value);
				}
				break;
				
			default:
				pstmt.setObject(i, value);
				break;
			}
		} catch (Exception e) {
			throw new ORMException("com.esen.jdbc.orm.helper.impl.importdataimpl.5","数据文件中的数据{0}格式错误",new Object[]{value},e);
		}
	}
	
	/**
	 * 将一行数据写入batch
	 * 是否每行提交，设置多少行提交，在这里处理
	 * @throws SQLException 
	 */
	private void append(PreparedStatement pstmt, boolean finish) {
		try {
			if (!finish) {
				cnt++;
				pstmt.addBatch();
				if (cnt % DataCopy.BATCHCNT == 0) {
					pstmt.executeBatch();
				}
		
				if (dbinfo.getMaxRowsInTrans() != -1) {
					if (cnt % (dbinfo.getMaxRowsInTrans()) == 0) {
						session.commit();
					}
				}
			} else {
				if (cnt % DataCopy.BATCHCNT != 0) {
					pstmt.executeBatch();
				}
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
	}
	
}
