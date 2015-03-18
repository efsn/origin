package com.esen.jdbc.table;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Random;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * AlterTable的实现类
 * 
 * 如果增删改的操作：
 * 个数为0：不作任何修改
 * 个数为1：直接在原表上修改
 * 个数大于1：创建一个与原表结构相同的临时表，并将数据写入到临时表中。
 *           在临时表中执行增删改的操作，如果执行完成，则用临时表替换原表
 *
 * @author zhuchx
 */
public class AlterTableImpl implements AlterTable {

	/**
	 * 对数据库表进行的操作
	 * OPER_ADDFIELD	增加字段
	 * OPER_DROPFIELD	删除字段
	 * OPER_MODIFYFIELD	修改字段
	 */
	public static final int OPER_ADDFIELD = 1;

	public static final int OPER_DROPFIELD = 2;

	public static final int OPER_MODIFYFIELD = 3;

	/**
	 * 数据库连接池
	 */
	private ConnectionFactory fct;

	/**
	 * 数据库表名
	 */
	private String tableName;

	/**
	 * 对表的修改列表。保存的对象为AlterTableOpr
	 */
	private ArrayList oprList = new ArrayList();

	/**
	 * 在commit时，将oprList中的操作全部转移到此对象中，再修改
	 * 这样做可以再commit后继续修改表，而不用重新创建此对象
	 */
	private ArrayList destOprList = new ArrayList();

	public AlterTableImpl(ConnectionFactory fct, String tableName) {
		this.fct = fct;
		this.tableName = tableName;
	}

	public void addAutoIncField(String fieldName, int step) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(fieldName);
		column.setAutoInc(true);
		column.setStep(1);
		oprList.add(new AlterTableColumnOpr(OPER_ADDFIELD, column));
	}

	public void addField(String fieldName, char type, int len, int sacle, String defaultvalue, boolean nullable,
			boolean unique) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(fieldName);
		column.setType(type);
		column.setLen(len);
		column.setScale(sacle);
		column.setDefaultValue(defaultvalue);
		column.setNullable(nullable);
		column.setUnique(unique);
		oprList.add(new AlterTableColumnOpr(OPER_ADDFIELD, column));
	}

	public void dropField(String fieldName) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(fieldName);
		oprList.add(new AlterTableColumnOpr(OPER_DROPFIELD, column));
	}

	public void modifyField(String oldFieldName, String newFieldName, char type, int len, int sacle) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(oldFieldName, newFieldName);
		column.setType(type);
		column.setLen(len);
		column.setScale(sacle);
		oprList.add(new AlterTableColumnOpr(OPER_MODIFYFIELD, column));
	}

	public void modifyField(String oldFieldName, String newFieldName, char type, int len, int sacle,
			String defaultvalue, boolean nullable, boolean unique) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(oldFieldName, newFieldName);
		column.setType(type);
		column.setLen(len);
		column.setScale(sacle);
		column.setDefaultValue(defaultvalue);
		column.setNullable(nullable);
		column.setUnique(unique);
		oprList.add(new AlterTableColumnOpr(OPER_MODIFYFIELD, column));
	}

	public void modifyFieldDesc(String fieldName, String desc) {
		TableColumnMetaDataChange column = new TableColumnMetaDataChange(fieldName);
		column.setDesc(desc);
		oprList.add(new AlterTableColumnOpr(OPER_MODIFYFIELD, column));
	}

	public void commit() throws Exception {
		destOprList.clear();
		destOprList.addAll(oprList);
		oprList.clear();
		int size = destOprList.size();
		if (size == 0) {
			return;
		}

		if (size == 1) {
			AlterTableColumnOpr ato = (AlterTableColumnOpr) destOprList.get(0);
			Connection con = fct.getConnection();
			try {
				DbDefiner ddl = fct.getDbDefiner();
				modifyTableColumn(tableName, ato, con, ddl);
			}
			finally {
				con.close();
			}
		}
		else {
			/**
			 * 如果有多个操作需要执行，则复制源表生成一个临时表changeTable，所有的修改在changeTable上进行，修改完成后重命名为源表即可
			 */
			DbDefiner ddl = fct.getDbDefiner();
			Connection con = fct.getConnection();
			try {
				//复制源表
				DataCopy dc = DataCopy.createInstance();
				String changeTable = createTempTable(con, ddl);
				changeTable = dc.selectInto(con, tableName, con, changeTable, DataCopy.OPT_CREATENEWTABLE);
				try {
					//修改表的结构
					for (int i = 0; i < size; i++) {
						AlterTableColumnOpr ato = (AlterTableColumnOpr) destOprList.get(i);
						modifyTableColumn(changeTable, ato, con, ddl);
					}

					//将修改后的表重命名
					renameTable(tableName, changeTable, con, ddl);
				}
				finally {
					if (!StrFunc.isNull(changeTable)) {
						SqlFunc.dropTable(fct, changeTable);
					}
				}
			}
			finally {
				con.close();
			}
		}

		/**
		 * 清空缓存的数据库表结构。在修改完成表结构后如果不调用此方法，再次获得表结构可能还是以前的表结构
		 */
		fct.getDbMetaData().reset();
	}

	/**
	 * 将修改完成的表重命名为原表
	 * @param tableName
	 * @param changeTable
	 * @param con
	 * @throws Exception 
	 */
	private void renameTable(String tableName, String changeTable, Connection con, DbDefiner ddl) throws Exception {
		String tempTable = createTempTable(con, ddl);
		ddl.lockNewTableName(con, tempTable);
		try {
			ddl.renameTable(con, tableName, tempTable);
			try {
				ddl.renameTable(con, changeTable, tableName);
			}
			catch (Exception e) {
				//如果重命名异常，还原原表
				ddl.renameTable(con, tempTable, tableName);
				throw e;
			}
			ddl.dropTable(con, null, tempTable);
		}
		finally {
			ddl.unlockNewTableName(con, tempTable);
		}
	}

	/**
	 * 创建一个临时表名
	 * @param con
	 * @param ddl
	 * @return
	 * @throws Exception
	 */
	private String createTempTable(Connection con, DbDefiner ddl) throws Exception {
		int r = new Random().nextInt(10000);
		return ddl.getCreateTableName(con, "alter", String.valueOf(r));
	}

	/**
	 * 获得表字段的结构
	 * @return
	 * @throws Exception
	 */
	private TableColumnMetaData getTableColumnMetaData(String table, String columnName) throws Exception {
		return fct.getDialect().getTableColumnMetaData(table, columnName);
	}

	/**
	 * 修改表的字段
	 * @param table 表名
	 * @param ato 所要做的修改
	 * @param con 数据库连接
	 * @throws Exception 
	 */
	private void modifyTableColumn(String table, AlterTableColumnOpr ato, Connection con, DbDefiner ddl)
			throws Exception {
		int oper = ato.getOper();
		TableColumnMetaDataChange column = ato.getColumnChange();
		String fieldName = column.getName();

		/**
		 * 需要每次都重新获得字段信息，因为字段修改后，以前获得的字段信息就可能不正确了
		 */
		TableColumnMetaData c = getTableColumnMetaData(table, fieldName);
		if (oper == OPER_ADDFIELD) {
			if (c != null) {
//				throw new RuntimeException("不能增加字段，表“" + tableName + "”中已经存在字段:" + fieldName);
				Object[] param=new Object[]{tableName,fieldName};
				throw new RuntimeException(I18N.getString("com.esen.jdbc.table.altertableimpl.addexistfiled", "不能增加字段，表“{0}\"”中已经存在字段:{1}", param));
			}
			if (column.isAutoInc()) {
//				throw new RuntimeException("不能增加字段，不支持增加自动增长字段");
				throw new RuntimeException(I18N.getString("com.esen.jdbc.table.altertableimpl.addzzzfield", "不能增加字段，不支持增加自动增长字段"));
			}
			else {
				ddl.addColumn(con, table, fieldName, column.getType(), column.getLen(), column.getScale(),
						column.getDefaultValue(), column.isNullable(), column.isUnique());
			}
			if (column.isSetDesc()) {
				ddl.modifyColumnForDesc(con, table, fieldName, column.getDesc());
			}
		}
		else if (oper == OPER_DROPFIELD) {
			if (c == null) {
//				throw new RuntimeException("不能删除字段，表“" + tableName + "”中没有字段:" + fieldName);
				Object[] param=new Object[]{tableName,fieldName};
				throw new RuntimeException(I18N.getString("com.esen.jdbc.table.altertableimpl.deleunexistfield", "不能删除字段，表“{0}\"”中没有字段:{1}", param));
			}
			ddl.dropColumn(con, table, fieldName);
		}
		else if (oper == OPER_MODIFYFIELD) {
			if (c == null) {
//				throw new RuntimeException("不能修改字段，表“" + tableName + "”中没有字段:" + fieldName);
				Object[] param=new Object[]{tableName,fieldName};
				throw new RuntimeException(I18N.getString("com.esen.jdbc.table.altertableimpl.editunexistfield", "不能修改字段，表“{0}”中没有字段:{1}", param));
			}
			String newFieldName = column.getNewName();
			if (StrFunc.isNull(newFieldName)) {
				newFieldName = fieldName;
			}

			char type = column.isSetType() ? column.getType() : SqlFunc.getSubsectionType(c.getType());
			int len = column.isSetLen() ? column.getLen() : c.getLen();
			int scale = column.isSetScale() ? column.getScale() : c.getScale();
			String defalutValue = column.isSetDefaultValue() ? column.getDefaultValue() : c.getDefaultValue();
			boolean isUnique = column.isSetUnique() ? column.isUnique() : c.isUnique();
			boolean isNullable = column.isSetNullable() ? column.isNullable() : c.isNullable();

			ddl.modifyColumn(con, table, fieldName, newFieldName, type, len, scale, defalutValue, isUnique, isNullable);

			if (column.isSetDesc()) {
				ddl.modifyColumnForDesc(con, table, newFieldName, column.getDesc());
			}
		}
	}

}
