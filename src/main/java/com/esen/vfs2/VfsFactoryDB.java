package com.esen.vfs2;

import java.util.Properties;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.ibatis.SqlMapClientFactory;
import com.esen.jdbc.ibatis.SqlMapConfigParser_For_Esen;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * vfs工厂,用于创建数据库的vfs实例
 * changelog 2012.9.12国际化
 *
 * @author zhuchx
 */
public class VfsFactoryDB {
	public static Vfs2 createVfs(ConnectionFactory fct, String tableName, Properties props) {
		if (StrFunc.isNull(tableName)) {
			throw new RuntimeException(I18N.getString("com.esen.vfs2.VfsFactoryDB.java.1",
					"数据库表名不能为空"));
		}
		if (!SqlFunc.isValidSymbol(tableName)) {
			throw new RuntimeException(I18N.getString("com.esen.vfs2.VfsFactoryDB.java.2",
					"不是合法的数据库表名{0}", new Object[] { tableName }));
		}
		Properties sqlMapProps = new Properties();
		sqlMapProps.setProperty("VFSTABLENAME", tableName);

		Properties conf = new Properties();
		conf.setProperty(SqlMapConfigParser_For_Esen.CACHEENABLE, "false");
		EsenSqlMapClient smc = SqlMapClientFactory.getInstance().createSqlMapClient(fct, sqlMapProps,
				"com/esen/vfs2/sqlmapconfig-vfs.xml", conf);

		return new Vfs2DB(fct, tableName, props, smc);
	}
}
