package com.esen.jdbc.dialect.impl.dm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;

import com.esen.jdbc.dialect.impl.DbDef;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.util.StrFunc;

public class DM7TableMetaData extends DMTableMetaData {

	public DM7TableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	protected void initColumns() throws Exception {
		super.initColumns();

		Connection conn = this.owner.getConnection();
		String[] tbs = DbDef.getTableNameForDefaultSchema(getTableName(), owner.getDataBaseInfo());
		
		HashMap comments = new HashMap();
		try {
			Statement stmt = conn.createStatement();
			try {
				String sql = "select COLNAME, COMMENT$ from SYSCOLUMNCOMMENTS where TVNAME='" + tbs[1] + "'";
				if (!StrFunc.isNull(tbs[0])) {
					sql += " and SCHNAME = '" + tbs[0] + "'"; 
				}
				
				ResultSet rs = stmt.executeQuery(sql);
				while (rs.next()) {
					comments.put(rs.getString("COLNAME"), rs.getString("COMMENT$"));
				}
				rs.close();
			} finally {
				stmt.close();
			}
		} finally {
			this.owner.closeConnection(conn);
		}
		
		for (int i = 0; i < columnList.size(); i++) {
			TableColumnMetaDataImpl col = (TableColumnMetaDataImpl) columnList.get(i);
			String colname = col.getName();
			col.setDesc((String) comments.get(colname));
		}
	}
}
