package func.mapping;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;

/**
 * 描述:
 *   实现将数据库表中的数据转换为java bean对象
 *   实现将一个java bean对象保存到数据库表中
 *   数据库表的结构要与java bean对象的方法相同
 * @author zhuchx
 *
 */
public class DbTableMapping {

	private ConnectionFactory confct;

	private String tablename;

	private ArrayList tableFieldsNameList;

	private HashMap tableFields;

	private String insertSql;

	private String selectSql;

	public DbTableMapping(ConnectionFactory fct, String tablename) {
		this.confct = fct;
		this.tablename = tablename;
		initFields();
		createInsertSql();
		createSelectSql();
	}

	private String getTableName() {
		return this.tablename;
	}

	private ConnectionFactory getConFct() {
		return this.confct;
	}

	/**
	 * 初始化字段信息
	 */
	private void initFields() {
		try {
			ConnectionFactory fct = getConFct();
			TableColumnMetaData[] cols;
			cols = fct.getDbMetaData().getTableMetaData(getTableName()).getColumns();
			int len = cols == null ? 0 : cols.length;
			tableFields = new HashMap(len);
			tableFieldsNameList = new ArrayList(len);
			for (int i = 0; i < len; i++) {
				TableColumnMetaData col = cols[i];
				tableFields.put(col.getName(), col);
				tableFieldsNameList.add(col.getName());
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private HashMap getTableFields() {
		return tableFields;
	}

	/**
	 * 创建写入sql
	 */
	private void createInsertSql() {
		HashMap fields = getTableFields();
		StringBuffer buf = new StringBuffer(fields.size() * 15 + 100);
		buf.append("insert into ");
		buf.append(getTableName());
		buf.append("(");
		for (int i = 0; i < fields.size(); i++) {
			if (i != 0) {
				buf.append(",");
			}
			buf.append(tableFieldsNameList.get(i));
		}
		buf.append(") values(");
		for (int i = 0; i < fields.size(); i++) {
			if (i != 0) {
				buf.append(",");
			}
			buf.append("?");
		}
		buf.append(")");

		insertSql = buf.toString();
	}

	/**
	 * 创建查询sql
	 */
	private void createSelectSql() {
		HashMap fields = getTableFields();
		StringBuffer buf = new StringBuffer(fields.size() * 15 + 100);
		buf.append("select ");
		for (int i = 0; i < fields.size(); i++) {
			if (i != 0) {
				buf.append(",");
			}
			buf.append(tableFieldsNameList.get(i));
		}
		buf.append(" from ");
		buf.append(getTableName());

		selectSql = buf.toString();
	}

	/**
	 * 清空所有数据
	 * @throws SQLException 
	 */
	public void deleteData() throws SQLException {
		SqlExecute se = SqlExecute.getInstance(getConFct());
		se.removeAllData(getTableName());
		
	}

	/**
	 * 写入对象
	 * @param obj
	 * @throws Exception
	 */
	public void insert(Object obj) throws Exception {
		HashMap objMethods = getClassGetMethods(obj.getClass());
		SqlExecute se = SqlExecute.getInstance(getConFct());

		Object[] objs = new Object[tableFieldsNameList.size()];
		for (int i = 0; i < tableFieldsNameList.size(); i++) {
			String fieldName = (String) tableFieldsNameList.get(i);
			Object o = getObjectFieldValue(obj, fieldName, objMethods);
			objs[i] = o;
		}
		se.executeUpdate(insertSql, objs);
		
	}

	/**
	 * 在数据库中查询表,返回结果,以Class c对应的实例数组返回
	 * @param c
	 * @return
	 * @throws Exception
	 */
	public Object[] getObject(Class c) throws Exception {
		ArrayList list = new ArrayList();
		HashMap objMethods = getClassSetMethods(c);
		String[] methodNames = (String[]) ArrayFunc.list2array(objMethods.keySet());

		String[] fieldNames = getDbTableFieldMapping(methodNames);
		Connection conn = getConFct().getConnection();
		try {
			Statement stt = conn.createStatement();
			try {
				ResultSet rs = stt.executeQuery(selectSql);
				try{
					while (rs.next()) {
						Object obj = c.newInstance();
						for (int i = 0; i < methodNames.length; i++) {
							String methodName = methodNames[i];
							String fieldName = fieldNames[i];
							if (StrFunc.isNull(fieldName)) {
								continue;
							}
							Object r = rs.getObject(fieldName);
							Method m = (Method) objMethods.get(methodName);
							m.invoke(obj, new Object[] { r });
						}
						list.add(obj);
					}
				}finally{
					rs.close();
				}
			} finally {
				stt.close();
			}
		} finally {
			conn.close();
		}

		return ArrayFunc.list2array(list, c);
	}

	/**
	 * 每个method都会与一个field对应,获得对应method的字段集
	 * 
	 * 如果返回的数组中有null,表示没有与此method对应的字段
	 * @param methodNames
	 * @return
	 */
	private String[] getDbTableFieldMapping(String[] methodNames) {
		String[] fieldNames = new String[methodNames.length];
		for (int i = 0; i < methodNames.length; i++) {
			String methodName = methodNames[i];
			for (int j = 0; j < tableFieldsNameList.size(); j++) {
				String fieldName = (String) tableFieldsNameList.get(j);
				if (methodName.equalsIgnoreCase(fieldName)) {
					fieldNames[i] = fieldName;
					continue;
				}
				if (fieldName.endsWith("_")) {
					String fname = fieldName.substring(0, fieldName.length() - 1);
					if (methodName.equalsIgnoreCase(fieldName)) {
						fieldNames[i] = fieldName;
						continue;
					}
				}
			}
		}
		return fieldNames;
	}

	/**
	 * obj中的每个属性对应数据库表的一个字段,获得对应字段fieldName的属性值
	 * fieldName是数据库表的字段,与obj的get方法或is方法的方法名可能相同,也可能多一个"_"后缀
	 * @param obj
	 * @param fieldName
	 * @param objMethods
	 * @return
	 * @throws Exception
	 */
	private Object getObjectFieldValue(Object obj, String fieldName, HashMap objMethods) throws Exception {
		String lowerFieldName = fieldName.toLowerCase();
		Method m = (Method) objMethods.get(lowerFieldName);
		if (m == null && lowerFieldName.endsWith("_")) {
			lowerFieldName = lowerFieldName.substring(0, lowerFieldName.length() - 1);
			m = (Method) objMethods.get(lowerFieldName);
		}
		if (m != null) {
			return m.invoke(obj, null);
		}
		return null;
	}

	/**
	 * 获得Class c中的所有get方法和is方法,get和is方法不能跟参数
	 * @param obj
	 * @return
	 */
	private HashMap getClassGetMethods(Class c) {
		Method[] ms = c.getMethods();
		int len = ms == null ? 0 : ms.length;
		HashMap map = new HashMap(len);
		for (int i = 0; i < len; i++) {
			Method m = ms[i];
			Class[] ps = m.getParameterTypes();
			if (ps == null || ps.length == 0) {
				String name = m.getName();//方法名
				if (name.startsWith("get")) {
					String field = name.substring(3);//去除前面的"get"
					map.put(field.toLowerCase(), m);
				}
				else if (name.startsWith("is")) {
					String field = name.substring(2);//去除前面的"is"
					map.put(field.toLowerCase(), m);
				}
			}
		}
		return map;
	}

	/**
	 * 获得Class c中的set方法,set方法只能跟一个参数
	 * @param c
	 * @return
	 */
	private HashMap getClassSetMethods(Class c) {
		Method[] ms = c.getMethods();
		int len = ms == null ? 0 : ms.length;
		HashMap map = new HashMap(len);
		for (int i = 0; i < len; i++) {
			Method m = ms[i];
			Class[] ps = m.getParameterTypes();
			if (ps != null && ps.length == 1) {
				String name = m.getName();//方法名
				if (name.startsWith("set")) {
					String field = name.substring(3);//去除前面的"set"
					map.put(field.toLowerCase(), m);
				}
			}
		}
		return map;
	}
}
