package func.jdbc;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;

import org.w3c.dom.Document;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.util.XmlFunc;

public class FuncJdbc {
	/**
	 * 清空数据库表中的数据
	 * @param fct
	 * @param tableName
	 * @throws Exception
	 */
	public static void deleteData(ConnectionFactory fct, String tableName) throws Exception {
		String sql = "delete from " + tableName;
		Connection con = fct.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				sm.executeUpdate(sql);
			}
			finally {
				sm.close();
			}
		}
		finally {
			con.close();
		}
	}

	/**
	 * 创建和修复表
	 * @param obj  和参数xml配置获得数据库表的结构
	 * @param fct  连接池
	 * @param tablename  数据库表
	 * @param xml  数据库表结构的xml文件名,与obj类在同一目录下
	 * @throws Exception
	 */
	public static void createOrRepairTable(Object obj, ConnectionFactory fct, String tablename, String xml)
			throws Exception {
		Document doc = getTableDocument(obj, xml);
		DbDefiner dd = fct.getDbDefiner();
		Connection con = fct.getConnection();
		try {
			if (dd.tableOrViewExists(con, tablename)) {
				//已经存在,则修复表
				dd.repairTable(con, doc, tablename, true);
			}
			else {//创建表
				dd.createTable(con, doc, tablename, false, true);
			}
		}
		finally {
			con.close();
		}
	}

	public static Document getTableDocument(Object obj, String xml) throws Exception {
		return getTableDocument(obj.getClass(), xml);
	}

	/**
	 * 获得xml文件对象的文档结构
	 * @param c  确定xml所在的目录
	 * @param xml  xml的文件名
	 * @return
	 * @throws Exception
	 */
	public static Document getTableDocument(Class c, String xml) throws Exception {
		InputStream in = c.getResourceAsStream(xml);
		try {
			return XmlFunc.getDocument(in);
		}
		finally {
			in.close();
		}
	}

	/**
	 * 连接池是否是可连接的
	 */
	public static boolean canConnection(ConnectionFactory fct) {
		if (fct == null)
			return false;
		try {
			Connection con = fct.getConnection();
			if (con != null)
				con.close();
		}
		catch (Exception e) {
			return false;
		}
		return true;
	}
}
