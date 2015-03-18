package com.esen.jdbc.dialect.impl.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.Statement;

import com.esen.jdbc.dialect.impl.DbMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataImpl;

public class MysqlTableMetaData extends TableMetaDataImpl {

	public MysqlTableMetaData(DbMetaDataImpl owner, String tablename) {
		super(owner, tablename);
	}

	/**
	 * 初始化列
	 * @throws Exception
	 */
	protected void initColumns() throws Exception {
		super.initColumns();
		
		//获取是否为自增列的信息
		analyseMeta();
	}
	
	private void analyseMeta() throws Exception {
		Connection con = this.owner.getConnection();
		try {
			Statement sm = con.createStatement();
			try {
				ResultSet rs = sm.executeQuery("select * from " + tablename + " where 1>2");
				try {
					ResultSetMetaData md = rs.getMetaData();
					for (int i = 1; i <= md.getColumnCount(); i++) {
						String colname = md.getColumnName(i);

						TableColumnMetaDataImpl column = (TableColumnMetaDataImpl) super.getColumn(colname);
						boolean autoinc = md.isAutoIncrement(i);
						if (autoinc == true) {
							column.setAutoInc(autoinc);
							return;
						}
					}
				} finally {
					rs.close();
				}
			} finally {
				sm.close();
			}
		} finally {
			this.owner.closeConnection(con);
		}
	}
}
