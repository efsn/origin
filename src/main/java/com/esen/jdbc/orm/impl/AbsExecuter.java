package com.esen.jdbc.orm.impl;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Document;

import com.esen.jdbc.orm.Blob;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.FileBlob;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.Session;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;
import com.esen.util.exp.Expression;
import com.esen.util.exp.ExpressionCompiler;

/**
 * ORM里面真正执行查询，更新，删除操作的对象接口
 * 该抽象类主要是提供query和update的公共方法
 * @author wang
 */
public abstract class AbsExecuter<T> {
	
	/**
	 * 会话
	 */
	protected Session session;

	/**
	 * 实体对象
	 */
	protected EntityInfo entity;

	/**
	 * 属性
	 */
	protected Property[] properties;
	
	/**
	 * 
	 */
	ORMExpCompilerHelper h;
	
	
	ExpressionCompiler compiler = new OrmExpressionCompiler();
	
	/**
	 * 
	 */
	ORMIFormatZz formatzz;

	/**
	 * 创建一个 PreparedStatement
	 * 
	 * @param sql SQL语句
	 * @return PreparedStatement
	 * @throws SQLException
	 */
	protected PreparedStatement createPreparedStatement(String sql) throws SQLException {
		Connection conn = this.session.getConnection();
		if (conn == null) {
			return null;
		}
		
		return conn.prepareStatement(sql);
	}

	public AbsExecuter(Session session, EntityInfo entity) {
		if (session == null
				|| entity == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.absexecuter.1","参数不能为空");
		}

		this.session = session;
		this.entity = entity;
		this.h = new ORMExpCompilerHelper(entity);
		this.formatzz = new ORMIFormatZz(entity, ((SessionImpl)(this.session)).getDialect());
	}

	/**
	 * 属性字段转化为SQL字段
	 * @param properties
	 * @return
	 */
	protected String[] propertiesToSQLField(String[] properties) {
		if (properties != null && properties.length > 0) {
			String[] fields = new String[properties.length];
			this.properties = new Property[properties.length];
			for (int i = 0; i < properties.length; i++) {
				this.properties[i] = entity.getProperty(properties[i]);
				if (this.properties[i] == null) {
					throw new ORMException("com.esen.jdbc.orm.impl.absexecuter.2","属性名{0}不存在",new Object[]{properties[i]});
				}
				fields[i] = this.properties[i].getFieldName();
			}
			return fields;
		}
	
		//properties 为空，使用全表字段
		List props = entity.getProperties();
		if (props == null || props.size() <= 0) {
			throw new ORMException("com.esen.jdbc.orm.impl.absexecuter.3","获取实体字段列表出错");
		}
		
		int size = props.size();
		String[] fields = new String[size];
		this.properties = new Property[size];
		for (int i = 0; i < size; i++) {
			this.properties[i] = (Property) props.get(i);
			fields[i] = (this.properties[i]).getFieldName();
		}
		return fields;
	}

	/**
	 * 在 sql 语句中添加 order by 子句
	 * 
	 * @param orderbyProperties 排序属性
	 * 				有序的键值对格式的porp1=true,prop2=false,false升序，true是降序
	 * @param sql sql语句
	 */
	protected void appendOrderbySQL(String orderbyProperties, StringBuilder sql) {
		String str = getSql(orderbyProperties);
		if (!StrFunc.isNull(str)) {
			sql.append(" order by ");
			sql.append(str);
		}
	}

	protected void appendConditionSQL(Expression condition, StringBuilder str) {
		String sql = getSql(condition);
		if (!StrFunc.isNull(sql)) {
			str.append(" where ");
			str.append(sql);
		}
	}

	protected T toObject(ResultSet rs) {
		return (T) ORMUtil.rs2Object(rs, entity);
	}
	
	protected Property getPrimaryKey() {
		return ORMUtil.getPrimaryKey(entity);
	}
	
	/**
	 * 设置  PreparedStatement 上的参数值
	 * 
	 * @param pstmt
	 * @param i
	 * @param property
	 * @param value
	 * @throws SQLException 
	 */
	protected void setStatementValue(PreparedStatement pstmt, int i, Property property, Object value) throws SQLException {
		char type = 0;
		
		//没有给定 property 时，直接使用 value 的数据类型
		if (property == null) {
			if (value == null) {
				pstmt.setObject(i, null);
				return;
			}
			
			if (value instanceof Calendar) {
				pstmt.setTimestamp(i, ORMUtil.calendar2Timestamp((Calendar)value));
				return;
			}
			
			type = ORMUtil.getPropertyDefType(value.getClass());			
		} else {
			type = property.getType();
		}
		
		try {
			switch (type) {
			case Property.FIELD_TYPE_LOGIC: 
				if (value == null) {
					pstmt.setNull(i, Types.VARCHAR);
				} else { //int, boolean
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
				} else if (value instanceof String) {
          pstmt.setString(i, value.toString());
        } else {
					pstmt.setObject(i, value);
				}
				break;
	
			case Property.FIELD_TYPE_INT:
				if (value == null) {
					pstmt.setNull(i, Types.INTEGER);
				} else {
					pstmt.setLong(i, (long) StrFunc.parseDouble(value, 0));
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
			throw new ORMException("com.esen.jdbc.orm.impl.absexecuter.4","第{0}个参数的值错误",new Object[]{i});
		}

	}
	
	/**
	 * 根据表达式获取相应的  sql 语句
	 * 
	 * @param exp
	 * @return
	 */
	protected String getSql(Expression exp) {
		//编译
		if (exp != null) {
			exp.compile(h, compiler);
			return exp.formatZz(formatzz);
		}
		
		return null;		
	}
	
	/**
	 * 根据排序条件获取相应的  sql 子句
	 * 
	 * @param orderbyProperties 排序属性
	 * 				有序的键值对格式的porp1=true,prop2=false,false升序，true是降序

	 * @return
	 */
	protected String getSql(String orderbyProperties) {
		StringBuilder sql = new StringBuilder();
		
		if (!StrFunc.isNull(orderbyProperties)) {
			String[] values = orderbyProperties.split(",");
			for (int i = 0; i < values.length; i++) {
				if (i != 0) {
					sql.append(",");
				}
				
				String[] tmp = values[i].split("=");
				Property prop = entity.getProperty(tmp[0].trim());
				if (prop == null) {
					throw new ORMSQLException("com.esen.jdbc.orm.impl.absexecuter.5","排序属性{0}不存在",new Object[]{tmp[0]});
				}
				sql.append(prop.getFieldName());
				if (tmp.length >= 2) {
					if (tmp[1].compareToIgnoreCase("false") == 0) {
						sql.append(" asc");
					} 
					if (tmp[1].compareToIgnoreCase("true") == 0) {
						sql.append(" desc");
					}
				}
			}
		}
		
		return sql.toString();
	}

	/**
	 * 获取连接池的名字
	 * 
	 * @return 连接池的名字
	 */
	public String getConnName() {
		return ((SessionImpl)session).getConnName();
	}
}
