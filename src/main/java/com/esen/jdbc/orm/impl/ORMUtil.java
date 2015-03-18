package com.esen.jdbc.orm.impl;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.w3c.dom.Document;

import com.esen.jdbc.orm.BeanBuilder;
import com.esen.jdbc.orm.EntityInfo;
import com.esen.jdbc.orm.FileBlob;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.ORMSQLException;
import com.esen.jdbc.orm.Property;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.util.ArrayFunc;
import com.esen.util.FileFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;
import com.esen.util.reflect.DynaBean;
import com.esen.util.reflect.ReflectionUtils;

/**
 * ORM 模块里面公共方法
 * 
 * @author wang
 */
public final class ORMUtil {

	public static boolean isLogicTypeClass(Class clazz) {
		return clazz == boolean.class || clazz == Boolean.class
				|| clazz == int.class || clazz == Integer.class;
	}

	public static boolean isIntTypeClass(Class clazz) {
		return clazz == int.class || clazz == Integer.class
				|| clazz == long.class || clazz == Long.class
				|| clazz == byte.class || clazz == Byte.class;
	}

	public static boolean isStrTypeClass(Class clazz) {
		return clazz == String.class || clazz == StringBuffer.class
				|| clazz == StringBuilder.class
				|| clazz == char.class || clazz == Character.class;
	}

	public static boolean isFloatTypeClass(Class clazz) {
		return clazz == float.class || clazz == Float.class
				|| clazz == double.class || clazz == Double.class;
	}

	public static boolean isClobTypeClass(Class clazz) {
		return clazz == String.class || clazz == StringBuffer.class
				|| clazz == StringBuilder.class
				|| clazz == Document.class;
	}

	public static boolean isDateTypeClass(Class clazz) {
		return clazz == Calendar.class || clazz == java.util.Date.class
				|| clazz == java.sql.Date.class
				|| clazz == java.sql.Timestamp.class;
	}

	public static boolean isBinaryTypeClass(Class clazz) {
		return clazz == com.esen.jdbc.orm.Blob.class
				|| clazz == Document.class
				|| ReflectionUtils
						.isSubclassOf(clazz, com.esen.jdbc.orm.Blob.class);
	}
	
	/**
	 * 根据 clazz 获取该整型的最大长度
	 * @param clazz
	 * @return
	 */
	public static int getITypeLength(Class clazz) {
		if (clazz == int.class || clazz == Integer.class) {
			return 10;
		}
		
		if (clazz == long.class || clazz == Long.class) {
			return 19;
		}
		
		if (clazz == byte.class || clazz == Byte.class) {
			return 3;
		}
		
		return 0;
	}

	/**
	 * 根据对象的java类型获取默认的Property类型
	 * 
	 * @param javaType
	 * @return
	 */
	public static char getPropertyDefType(Class clazz) {
		if (isIntTypeClass(clazz)) {
			return Property.FIELD_TYPE_INT;
		} else if (isLogicTypeClass(clazz)) {
			return Property.FIELD_TYPE_LOGIC;
		} else if (isFloatTypeClass(clazz)) {
			return Property.FIELD_TYPE_FLOAT;
		} else if (isStrTypeClass(clazz)) {
			return Property.FIELD_TYPE_STR;
		} else if (isDateTypeClass(clazz)) {
			return Property.FIELD_TYPE_DATE;
		} else if (isClobTypeClass(clazz)) {
			return Property.FIELD_TYPE_CLOB;
		} else if (isBinaryTypeClass(clazz)) {
			return Property.FIELD_TYPE_BINARY;
		}
		return 0;
	}

	public static final boolean isCompatibleType(char type, Object obj) {
		if (obj == null)
			return true;
		Class clazz = obj.getClass();
		switch (type) {
		case Property.FIELD_TYPE_LOGIC:
			return isLogicTypeClass(clazz);
		case Property.FIELD_TYPE_INT:
			return isIntTypeClass(clazz);
		case Property.FIELD_TYPE_FLOAT:
			return isFloatTypeClass(clazz);
		case Property.FIELD_TYPE_STR:
			return isStrTypeClass(clazz);
		case Property.FIELD_TYPE_DATE:
			return isDateTypeClass(clazz);
		case Property.FIELD_TYPE_BINARY:
			return isBinaryTypeClass(clazz);
		default:
			throw new ORMException("com.esen.jdbc.orm.impl.ormutil.1","未知类型：{0}",new Object[]{type});
		}
	}

	/**
	 * 返回用户定义类型，以后可按需求更改 如果javaType和customType不匹配，抛出异常
	 * 
	 * @param javaType
	 * @param customType
	 * @return
	 */
	public static char checkPropertyType(Class javaType, char customType) {
		if (getPropertyDefType(javaType) != customType) {
			throw new ORMException("com.esen.jdbc.orm.impl.ormutil.2","javaType 和 customType 不匹配!");
		}
		return customType;
	}

	/**
	 * @param entity
	 *            获取一个实体对象的主键
	 * @return 如果为空将抛出异常
	 */
	public static Property getPrimaryKey(EntityInfo entity) {
		if (entity == null) {
			throw new ORMException("JDBC.COMMON.SCANNOTBEEMPTY","参数 {0} 不能为空!",new Object[]{"entity"});
		}
		Property primaryKey = entity.getPrimaryKey();
		if (primaryKey == null) {
			throw new ORMException("com.esen.jdbc.orm.impl.ormutil.4","实体对象({0})主键PK没有设置",new Object[]{entity.getEntityName()});
		}
		return primaryKey;
	}

	/**
	 * 获取对象的主键的值
	 * 
	 * @param object
	 *            对象实例
	 * @param entity
	 *            实体对象
	 * @return 主键的值
	 */
	public static Object getPrimaryKeyValue(DynaBean<?> bean, EntityInfo entity) {
		Property primaryKey = getPrimaryKey(entity);
		return primaryKey == null ? null : bean.getValue(primaryKey.getName());
	}

	/**
	 * 根据对象的属性获取值
	 * 
	 * @param object
	 *            对象实例
	 * @param entity
	 *            实体对象
	 * @param propertyName
	 *            属性
	 * @return 该属性的值
	 */
	public static Object getPropertyValue(EntityInfo entity, DynaBean<?> bean, String propertyName) {
		Class<?> filedType = bean.getFiledType(propertyName);
		if (filedType != null) {
			return bean.getValue(propertyName);
		}
		if (entity instanceof TreeEntityInfo) {
			String[] upids = StrFunc.analyzeStr(((TreeEntityInfo) entity).getUpidsPropertyName());
			if (upids != null) {
				int find = ArrayFunc.find(upids, propertyName);
				if (find > -1) {
					Object value = bean.getValue(TreeEntityInfo.UpidsPropName);
					try {
						return Array.get(value, find);
					} catch (Exception e) {
						return null;
					}
				}
			}
		}
		//扩展字段里面取
		return bean.getValue(propertyName);
	}

	public static final String Object2Str(Object obj) {
		if (obj == null) {
			return null;
		}
		
		if (isStrTypeClass(obj.getClass())) {
			return obj.toString();
		}
		
		throw new ORMException("com.esen.jdbc.orm.impl.ormutil.5","无法将对象类型{0}转成字符串",new Object[]{(obj.getClass())});
	}

	/**
	 * 将 Timestamp, Date, Time 类型对象转换为 Calendar 类型
	 * 
	 * @param tp
	 * @return
	 */
	public static Calendar date2Calendar(Object obj) {
		if (obj == null) {
			return null;
		}

		long date = 0;
		Calendar cal = Calendar.getInstance();
		if (obj instanceof java.sql.Time) {
			date = ((java.sql.Time) obj).getTime();
		} else if (obj instanceof java.sql.Timestamp) {
			date = ((java.sql.Timestamp) obj).getTime();
		} else if (obj instanceof java.sql.Date) {
			date = ((java.sql.Date) obj).getTime();
		} else if (obj instanceof java.util.Date) {
			date = ((java.util.Date) obj).getTime();
		}
		cal.setTimeInMillis(date);
		return cal;
	}

	/**
	 * 将 Calendar 类型对象转换为 Timestamp 类型
	 * 
	 * @param cal
	 * @return
	 */
	public static Timestamp calendar2Timestamp(Calendar cal) {
		if (cal == null) {
			return null;
		}

		Timestamp tp = new Timestamp(cal.getTimeInMillis());
		return tp;
	}

	/**
	 * 根据给定的结果集和实体信息，构建一个实例
	 * 
	 * @param rs
	 *            结果集
	 * @param entityInfo
	 *            实体信息
	 * @return 实例
	 */
	public static Object rs2Object(ResultSet rs, EntityInfo entityInfo) {
		if (rs == null || entityInfo == null) {
			return null;
		}

		//所有需要获取值的属性列表
		List<Property> properties = entityInfo.getProperties();
		if (properties == null || properties.size() <= 0) {
			return null;
		}
		
		//Tree 结构的 upids
		String[] upids = null;
		Object[] upidsValue = null;
		if (entityInfo instanceof TreeEntityInfo) {
			upids = StrFunc.analyzeStr(((TreeEntityInfo)entityInfo).getUpidsPropertyName());
			if (upids != null) {
				upidsValue = new Object[upids.length];
			}
		}
		
		/*
		 * 确定创建对象的方法
		 * 1.定义了 BeanBuilder 时，
		 * 	  用  BeanBuilder 中指定的  create 方法创建对象
		 *   再设置其他属性的值
		 * 
		 * 2.没有定义 BeanBuilder 时，
		 *   创建 javabean 对象实例，再设置对象的属性值来创建对象
		 */
		boolean createByMethod = false;
		BeanBuilder beanBuilder = entityInfo.getBeanBuilder();
		
		//存放参数值的数组
		Object[] argumentObject = null;
		
		//存放参数类型的数组
		Class[] argumentClass = null;
		
		//存放参数对应的属性的数组
		List<Property> argumentProps = null;
		
		if (beanBuilder != null) {
			createByMethod = true;
			
			String[] arguments = beanBuilder.getArguments();
			
			argumentProps = new ArrayList<Property>();
			for (int i = 0; i < arguments.length; i++) {
				argumentProps.add(entityInfo.getProperty(arguments[i]));
			}
			
			argumentObject = new Object[argumentProps.size()];
			argumentClass = new Class[argumentProps.size()];
		}
		
		// 将所有需要获取值的属性列表 properties 中属于 argumentProps 的属性去除
		if (argumentProps != null && argumentProps.size() > 0) {
			for (int i = 0; i < argumentProps.size(); i++) {
				properties.remove(argumentProps.get(i));
			}
		}

		try {
			Class clazz = entityInfo.getBean();
			DynaBean bean = DynaBean.getDynaBean(clazz.newInstance());
			ResultSetMetaData md = rs.getMetaData();
			
			//使用指定的 create 方法创建对象
			if (createByMethod) {
				int size = argumentProps == null ? 0 : argumentProps.size();
				
				//先读取所需的参数的值
				for (int i = 0; i < size; i++) {
					Property prop = argumentProps.get(i);
					String fieldname = prop.getFieldName();
					
					/* 
					 * 根据 java bean 中的数据类型准确判断返回值类型
					 * 要求不存在为空的情况
					 */
					Class type = bean.getFiledType(prop.getName());
					argumentClass[i] = type;
					
					//结果集中不存在该字段，不需要读取数据
					if (!hasField(md, fieldname)) {
						continue;
					}
					
					//从结果集中读取数据
					argumentObject[i] = getFieldValue(fieldname, prop.getType(), rs, type);			
				}
				
				
				//使用  BeanBuilder 中指定的方法创建对象
				Class cls = beanBuilder.getBuilder();				
				try {
					Method method = cls.getMethod(beanBuilder.getMethod(), argumentClass);
					Object newObj = method.invoke(cls.newInstance(), argumentObject);
					if (newObj != null) {
						bean = DynaBean.getDynaBean(newObj);
					}
				} catch (Exception e) {
					throw new ORMException("com.esen.jdbc.orm.impl.ormutil.5", "实体对象 {0} 定义的 BeanBuilder 有误", new Object[]{entityInfo.getEntityName()}, e);
				}
			}

			//获取其他的属性的数据
			int size = properties.size();
			for (int i = 0; i < size; i++) {
				Property prop = properties.get(i);
				String fieldname = prop.getFieldName();
				
				if (!hasField(md, fieldname)) {
					continue;
				}
				
				Class type = bean.getFiledType(prop.getName());

				Object obj = getFieldValue(fieldname, prop.getType(), rs, type);
				if (obj != null) {
					/*
					 * 标识该值是 upids 的值还是其他属性的
					 * 为 false 表示是其他属性的值
					 * 为 true 表示是 upids 中的一个值
					 */
					boolean flag = false; 
					for (int index = 0; upids != null && index < upids.length; index++) {
						if (prop.getName().compareToIgnoreCase(upids[index]) == 0) {
							upidsValue[index] = obj;
							flag = true;
							break;
						}
					}

					if (flag == false) {
						bean.setValue(prop.getName(), obj);
					}
				}
			}
			
			if (upids != null) {
				bean.setValue(TreeEntityInfo.UpidsPropName, upidsValue);
			}
		
			return bean.getBean();
		} catch (Exception e) {
			ORMException ormex = new ORMException(e.getCause());
			ormex.setStackTrace(e.getStackTrace());
			throw ormex;
		}
	}
	
	/**
	 * 根据要获取的字段的信息从结果集中获取数据
	 * 
	 * @param fieldname 字段名
	 * @param fieldType 从配置信息中获取的数据库的字段的类型
	 * @param rs 结果集
	 * @param type 从 JavaBean 对象的成员信息中获取的类型
	 * @return 数据对象
	 * @throws Exception
	 */
	private static Object getFieldValue(String fieldname, char fieldType, ResultSet rs, Class type) throws Exception {
		Object obj = null;
		
		try {
			switch (fieldType) {
				case Property.FIELD_TYPE_STR:
					String tmp = rs.getString(fieldname);
					if (tmp == null) {
						break;
					}

					if (type == StringBuffer.class) {
						obj = new StringBuffer(tmp);
					} else if (type == StringBuilder.class) {
						obj = new StringBuilder(tmp);
					} else if (type == char.class || type == Character.class) {
						obj = tmp.charAt(0);
					} else {
						obj = tmp;
					}
					break;

				case Property.FIELD_TYPE_LOGIC:
					if (rs.getObject(fieldname) == null) {
						break;
					}
					//int
					if (type == Integer.class || type == int.class) {
						obj = rs.getInt(fieldname);
						break;
					}

					//bool
					String str = rs.getString(fieldname);
					if (StrFunc.isNull(str)) {
						obj = null;
					} else if (str.compareTo("1") == 0) {
						obj = Boolean.TRUE;
					} else {
						obj = Boolean.FALSE;
					}
					break;

				case Property.FIELD_TYPE_INT:
					if (rs.getObject(fieldname) != null) {
						if (type == Byte.class || type == byte.class) {
							obj = rs.getByte(fieldname);
						} else if (type == Long.class || type == long.class) {
							obj = rs.getLong(fieldname);
						} else {
							obj = rs.getInt(fieldname);
						}
					}
					break;

				case Property.FIELD_TYPE_FLOAT:
					if (rs.getObject(fieldname) != null) {
						if (type == Float.class || type == float.class) {
							obj = rs.getFloat(fieldname);
						} else {
							obj = rs.getDouble(fieldname);
						}
					}
					break;

				case Property.FIELD_TYPE_DATE:
					if (type == java.util.Calendar.class) {
						Timestamp value = rs.getTimestamp(fieldname);
						if (value != null) {
							obj = Calendar.getInstance();
							((Calendar) obj).setTime(value);
						}
//					} else if (type == java.util.Date.class || type == java.sql.Date.class) {
//						obj = rs.getDate(fieldname);
					} else {
						obj = rs.getTimestamp(fieldname);
					}
					break;

				case Property.FIELD_TYPE_CLOB:
					if (type == java.sql.Clob.class) {
						obj = rs.getClob(fieldname); // TODO 是否需要存为临时文件
					} else if (type == Document.class) {
						Clob tmpClob = rs.getClob(fieldname);
						if (tmpClob == null || tmpClob.length() == 0) {
							break;
						}
						obj = XmlFunc.getDocument(tmpClob.getCharacterStream());
					} else {
						String value = rs.getString(fieldname);
						if (value == null) {
							break;
						}

						if (type == StringBuffer.class) {
							obj = new StringBuffer(value);
						} else if (type == StringBuilder.class) {
							obj = new StringBuilder(value);
						} else if (type == Character.class || type == char.class) {
							obj = value.charAt(0);
						} else {
							obj = value;
						}
						break;
					}
					break;

				case Property.FIELD_TYPE_BINARY:
					if (type == Document.class) {
						Blob tmpBlob = rs.getBlob(fieldname);
						if (tmpBlob == null || tmpBlob.length() == 0) {
							break;
						}
						obj = XmlFunc.getDocument(tmpBlob.getBinaryStream());
					} else if (type == com.esen.jdbc.orm.Blob.class) {
						Blob tmpBlob = rs.getBlob(fieldname);
						if (tmpBlob == null || tmpBlob.length() == 0) {
							break;
						}
						FileBlob fblob = new FileBlob(FileFunc.createTempFileObj(null, null, false, true));
						fblob.writeBinaryStream(tmpBlob.getBinaryStream());
						obj = fblob;
					} else {
						obj = rs.getAsciiStream(fieldname); // TODO 是否需要存为临时文件
					}
					break;

				default:
					obj = rs.getObject(fieldname);
					break;
			}

		} catch (SQLException sqlexp) {
			; // 存在结果集中不存在该列的值的情况，此时会报错，为正常情况
		}

		return obj;
	}
	
	/**
	 * 判断结果集中是否有该字段
	 * 
	 * @param rs
	 * @param fieldname
	 * @return
	 */
	private static boolean hasField(ResultSetMetaData md, String fieldname) {
		if (md == null || StrFunc.isNull(fieldname)) {
			return false;
		}
		
		try {
			int count = md.getColumnCount();
			for (int i = 1; i <= count; i++) {
				if (StrFunc.compareStr(fieldname, md.getColumnName(i))) {
					return true;
				}
			}
		} catch (SQLException e) {
			throw new ORMSQLException(e);
		}
		
		return false;
	}
}
