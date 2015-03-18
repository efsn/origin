package com.esen.jdbc.dialect.impl.teradata;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

/**
 * Teradata 数据库表的元信息
 *
 * @author liujin
 */
public class TeradataTableMetaData extends TableMetaDataImpl {

	/**
	 * 构造方法
	 * @param owner 数据库结构对象
	 * @param tablename 表名
	 */
	public TeradataTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}
	
	/**
	 * 获取表的索引信息
	 * 存在索引的名字为空的情况，与其它数据库不相同，作特殊处理
	 * {@inheritDoc}
	 */
	protected void initIndexes() throws Exception {
		super.initCols();
		Connection con = owner.getConnection();

		try {
			DatabaseMetaData dbmd = con.getMetaData();
			String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
			ResultSet rs = dbmd.getIndexInfo(null, tbs[0], tbs[1], false, false);
			HashMap pvs = new HashMap();
			try {
				while (rs.next()) {
					String[] pv = new String[3];
					pv[0] = rs.getString(TableMetaDataImpl.INDEX_NAME); //可以为 null
					pv[1] = rs.getString(TableMetaDataImpl.INDEX_NON_UNIQUE);
					pv[2] = rs.getString(TableMetaDataImpl.INDEX_COLUMN_NAME);

					//检查索引字段是不是表字段；
					if (!checkIndexField(pv[2]))
						continue;
					Object o = pvs.get(pv[0]);
					if (o == null) {
						List l = new ArrayList();
						l.add(pv);
						pvs.put(pv[0], l);
					} else {
						List l = (List) o;
						l.add(pv);
					}
				}
			} finally {
				rs.close();
			}

			Set keys = pvs.keySet();
			Iterator it = keys.iterator();
			while (it.hasNext()) {
				String indexname = (String) it.next();
				List l = (List) pvs.get(indexname);
				
				//没有定义名字的索引，当做不同的索引
				if (indexname == null) { 
					for (int i = 0; i < l.size(); i++) {
						String[] pv = (String[]) l.get(i);
						boolean unique = !Boolean.valueOf(pv[1]).booleanValue();
						TableIndexMetaDataImpl imd = new TableIndexMetaDataImpl(null, pv[2], unique);
						this.addIndexMeta(imd);
					}
					continue;
				}
				
				String cols[] = new String[l.size()];
				boolean unique = false;
				for (int i = 0; i < l.size(); i++) {
					String[] pv = (String[]) l.get(i);
					if (i == 0) {
							unique = !Boolean.valueOf(pv[1]).booleanValue();
					}
					cols[i] = pv[2];
				}
				
				TableIndexMetaDataImpl imd = new TableIndexMetaDataImpl(indexname, cols, unique);
				this.addIndexMeta(imd);
			}
		} finally {
			owner.closeConnection(con);
		}
	}
}
