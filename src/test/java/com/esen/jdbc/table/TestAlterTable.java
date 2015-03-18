package com.esen.jdbc.table;

import java.sql.Connection;

import junit.framework.TestCase;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.table.AlterTable;
import com.esen.jdbc.table.AlterTableFactory;

import func.jdbc.FuncConnectionFactory;

/**
 * 测试AlterTable的实现类
 * 
 * 测试的源表结构如下：
 * index_ C(20) 
 * autoinc_ 自动增长字段
 * str_ C(20) 字符型
 * int_ I(10) 整形
 * float_ N 浮点型
 * logic_ L 布尔型
 * date_ D 日期型
 * time_ T 时间型
 * temestamp_ P 时间戳类型
 * clob_ M 字符串大字段
 * blob_ X 二进制大字段
 *
 * @author zhuchx
 */
public class TestAlterTable extends TestCase {
	private static final String[] OLDFIELDNAMES = { "str_", "int_", "float_", "logic_", "date_", "time_", "timestamp_",
			"clob_", "blob_" };

	private static final String[] NEWFIELDNAMES = { "newstr_", "newint_", "newfloat_", "newlogic_", "newdate_",
			"newtime_", "newtimestamp_", "newclob_", "newblob_" };

	private static final char[] FIELD_TYPES = { DbDefiner.FIELD_TYPE_STR, DbDefiner.FIELD_TYPE_INT,
			DbDefiner.FIELD_TYPE_FLOAT, DbDefiner.FIELD_TYPE_LOGIC, DbDefiner.FIELD_TYPE_DATE,
			DbDefiner.FIELD_TYPE_TIME, DbDefiner.FIELD_TYPE_TIMESTAMP, DbDefiner.FIELD_TYPE_CLOB,
			DbDefiner.FIELD_TYPE_BINARY };

	private ConnectionFactory dbfct;

	private String getTableName() {
		return "test_alert_table";
	}

	private ConnectionFactory getConnectionFactory() {
		if (dbfct == null) {
			dbfct = FuncConnectionFactory.getOracleCustomConnectionFactory();
		}
		return dbfct;
	}

	private void createTestTable() throws Exception {
		ConnectionFactory fct = getConnectionFactory();
		//先删除表
		SqlFunc.dropTable(fct, getTableName());
		//再创建表
		DbDefiner ddl = fct.getDbDefiner();
		ddl.clearDefineInfo();
		ddl.defineStringField("index_", 20, null, true, false);
		ddl.defineAutoIncField("autoinc_", 1);
		ddl.defineStringField("str_", 20, null, true, false);
		ddl.defineIntField("int_", 10, null, true, false);
		ddl.defineFloatField("float_", 20, 2, null, true, false);
		ddl.defineLogicField("logic_", null, true, false);
		ddl.defineDateField("date_", null, true, false);
		ddl.defineTimeField("time_", null, true, false);
		ddl.defineTimeStampField("timestamp_", null, true, false);
		ddl.defineClobField("clob_", null, true, false);
		ddl.defineBlobField("blob_", null, true, false);

		Connection con = fct.getConnection();
		try {
			ddl.createTable(con, null, getTableName());
		}
		finally {
			con.close();
		}
	}

	/**
	 * 增加字段。增加一个字段类型为type的字段
	 * @param at
	 * @param field
	 * @param type
	 */
	private void addField(AlterTable at, String field, char type) {
		switch (type) {
			case DbDefiner.FIELD_TYPE_STR:
				at.addField(field, type, 50, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_INT:
				at.addField(field, type, 12, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_FLOAT:
				at.addField(field, type, 32, 4, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_LOGIC:
				at.addField(field, type, 1, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_DATE:
				at.addField(field, type, 0, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_TIME:
				at.addField(field, type, 0, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_TIMESTAMP:
				at.addField(field, type, 0, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_CLOB:
				at.addField(field, type, 0, 0, null, true, false);
				break;
			case DbDefiner.FIELD_TYPE_BINARY:
				at.addField(field, type, 0, 0, null, true, false);
				break;
		}
	}

	/**
	 * 修改字段。将oldField的类型修改为type,字段名重命名为newField。
	 * @param at
	 * @param oldField
	 * @param newField
	 * @param type
	 */
	private void modifyField(AlterTable at, String oldField, String newField, char type) {
		switch (type) {
			case DbDefiner.FIELD_TYPE_STR:
				at.modifyField(oldField, newField, type, 50, 0);
				break;
			case DbDefiner.FIELD_TYPE_INT:
				at.modifyField(oldField, newField, type, 12, 0);
				break;
			case DbDefiner.FIELD_TYPE_FLOAT:
				at.modifyField(oldField, newField, type, 32, 4);
				break;
			case DbDefiner.FIELD_TYPE_LOGIC:
				at.modifyField(oldField, newField, type, 1, 0);
				break;
			case DbDefiner.FIELD_TYPE_DATE:
				at.modifyField(oldField, newField, type, 0, 0);
				break;
			case DbDefiner.FIELD_TYPE_TIME:
				at.modifyField(oldField, newField, type, 0, 0);
				break;
			case DbDefiner.FIELD_TYPE_TIMESTAMP:
				at.modifyField(oldField, newField, type, 0, 0);
				break;
			case DbDefiner.FIELD_TYPE_CLOB:
				at.modifyField(oldField, newField, type, 0, 0);
				break;
			case DbDefiner.FIELD_TYPE_BINARY:
				at.modifyField(oldField, newField, type, 0, 0);
				break;
		}
	}

	private AlterTable createAlterTable() throws Exception {
		createTestTable();
		return AlterTableFactory.createAlterTable(getConnectionFactory(), getTableName());
	}

	private TableColumnMetaData getColumnMetaData(String column) throws Exception {
		ConnectionFactory fct = getConnectionFactory();
		return fct.getDialect().getTableColumnMetaData(getTableName(), column);
	}

	/**
	 * 测试修改字段，多种操作混合
	 * @throws Exception 
	 */
	public void testChangeTable() throws Exception {
		_testFieldRename();
		//测试一次的操作
		{//增加
			//测试增加不存在的字段
			_test1AddNotExistField();
			//测试增加已经存在的字段
			_test1AddExistField();
		}
		{//删除
			//测试删除存在的字段
			_test1DropExistField();
			//测试删除不存在的字段
			_test1DropNotExistField();

		}
		{//修改
			//测试修改存在的字段
			_test1ModifyExistField();
			//测试修改不存在的字段
			_test1ModifyNotExistField();

		}
		//测试两次操作
		{
			{//以增加开头
				//测试多次增加相同名称的字段
				_test2AddAdd();
				//测试增加后删除
				_test2AddDrop();
				//测试增加后修改
				_test2AddModify();
			}
			{//以删除开头
				//测试删除后增加
				_test2DropAdd();
				//测试多次删除相同的字段
				_test2DropDrop();
				//测试删除后修改
				_test2DropModify();
			}
			{//以修改开头
				//测试修改后增加
				_test2ModifyAdd();
				//测试修改后删除
				_test2ModifyDrop();
				//测试多次修改相同的字段
				_test2ModifyModify();
			}
		}
		//测试三次操作
		{
			{//以增加开头
				//测试增加，删除，增加
				_test3AddDropAdd();
				//测试增加，删除，修改
				_test3AddDropModify();
				//测试增加，修改，增加
				_test3AddModifyAdd();
				//测试增加，修改，删除
				_test3AddModifyDrop();
				//测试增加，修改，修改
				_test3AddModifyModify();
			}
			{//以删除开头
				//测试删除，增加，删除
				_test3DropAddDrop();
				//测试删除，增加，修改
				_test3DropAddModify();
				//测试删除，修改，增加
				_test3DropModifyAdd();
				//测试删除，修改，修改
				_test3DropModifyModify();
			}
			{//以修改开头
				//测试修改，增加，删除
				_test3ModifyAddDrop();
				//测试修改，删除，增加
				_test3ModifyDropAdd();
				//测试修改，修改，删除
				_test3ModifyModifyDrop();
				//测试修改，修改，修改
				_test3ModifyModifyModify();
			}
		}
		//测试四次操作
		{
			{//以增加开头
				//测试增加，删除，增加，删除
				_test4AddDropAddDrop();
				//测试增加，删除，增加，修改
				_test4AddDropAddModify();
				//测试增加，修改，删除，增加
				_test4AddModifyDropAdd();
				//测试增加，修改，删除，修改
				_test4AddModifyDropModify();
			}
			{//以删除开头
				//测试删除，增加，删除，增加
				_test4DropAddDropAdd();
				//测试删除，增加，修改，删除
				_test4DropAddMoidfyDrop();
				//测试删除，增加，修改，修改
				_test4DropAddModifyModify();
			}
			{//以修改开头
				//测试修改，删除，增加，删除
				_test4ModifyDropAddDrop();
				//测试修改，删除，增加，修改
				_test4ModifyDropAddModify();
				//测试修改，修改，删除，增加
				_test4ModifyModifyDropAdd();
			}
		}
		//测试更多操作
		{
		}
	}

	/**
	 * 测试字段的重命名
	 * @throws Exception 
	 */
	private void _testFieldRename() throws Exception {
		/**
		 * 1.将index_字段重命名为a_
		 * 2.将str_字段重命名为b_
		 * 3.将a_字段重命名为str_
		 */
		AlterTable at = createAlterTable();
		modifyField(at, "index_", "a_", DbDefiner.FIELD_TYPE_STR);
		modifyField(at, "str_", "b_", DbDefiner.FIELD_TYPE_STR);
		modifyField(at, "a_", "str_", DbDefiner.FIELD_TYPE_STR);
		at.commit();

		assertNotNull(getColumnMetaData("str_"));
		assertNotNull(getColumnMetaData("b_"));
	}

	/**
	 * 测试四次操作：修改，修改，删除，增加
	 * @throws Exception 
	 */
	private void _test4ModifyModifyDropAdd() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			at.dropField("M" + newfields[i]);
			addField(at, newfields[i], types[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试四次操作：修改，删除，增加，修改
	 * @throws Exception 
	 */
	private void _test4ModifyDropAddModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			at.dropField(newfields[i]);
			addField(at, newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData("M" + newfields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试四次操作：修改，删除，增加，删除
	 * @throws Exception 
	 */
	private void _test4ModifyDropAddDrop() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			at.dropField(newfields[i]);
			addField(at, newfields[i], types[i]);
			at.dropField(newfields[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertNull(column);

		}
	}

	/**
	 * 测试四次操作：删除，增加，修改，修改
	 * @throws Exception 
	 */
	private void _test4DropAddModifyModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldfields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			addField(at, oldfields[i], types[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData("M" + newfields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试四次操作：删除，增加，修改，删除
	 * @throws Exception 
	 */
	private void _test4DropAddMoidfyDrop() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldfields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			addField(at, oldfields[i], types[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);
			at.dropField(newfields[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(oldfields[i]);
			assertNull(column);
		}
	}

	/**
	 * 测试四次操作：删除，增加，删除，增加
	 * @throws Exception 
	 */
	private void _test4DropAddDropAdd() throws Exception {
		String[] fields = OLDFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
			addField(at, fields[i], types[i]);
			at.dropField(fields[i]);
			addField(at, fields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(fields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试四次操作：增加，修改，删除，修改
	 * @throws Exception 
	 */
	private void _test4AddModifyDropModify() throws Exception {
		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = oldfields.length;
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				at.dropField(newfields[i]);
				modifyField(at, newfields[i], "M" + newfields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				at.dropField("M" + fields[i]);
				modifyField(at, "M" + fields[i], "MM" + fields[i], types[i]);
				try {
					at.commit();
					fail("字段已经不存在，不能再修改");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能修改字段"));
				}
			}
		}
	}

	/**
	 * 测试四次操作：增加，修改，删除，增加
	 * @throws Exception 
	 */
	private void _test4AddModifyDropAdd() throws Exception {
		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = oldfields.length;
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				at.dropField(newfields[i]);
				addField(at, newfields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				at.dropField("M" + fields[i]);
				addField(at, fields[i], types[i]);
				at.commit();

				TableColumnMetaData column = getColumnMetaData(fields[i]);
				assertNotNull(column);
				assertColumnType(column, types[i]);
			}
		}
	}

	/**
	 * 测试四次操作：增加，删除，增加，修改
	 * @throws Exception 
	 */
	private void _test4AddDropAddModify() throws Exception {
		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = oldfields.length;
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				at.dropField(oldfields[i]);
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				at.commit();

				TableColumnMetaData column = getColumnMetaData("M" + fields[i]);
				assertNotNull(column);
				assertColumnType(column, types[i]);
			}
		}
	}

	/**
	 * 测试四次操作：增加，删除，增加，删除
	 * @throws Exception 
	 */
	private void _test4AddDropAddDrop() throws Exception {
		{//已经存在的字段
			String[] fields = OLDFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				addField(at, fields[i], types[i]);
				addField(at, fields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				addField(at, fields[i], types[i]);
				addField(at, fields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
	}

	/**
	 * 测试三次操作：修改，修改，修改
	 * @throws Exception 
	 */
	private void _test3ModifyModifyModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			modifyField(at, "M" + newfields[i], "MM" + newfields[i], types[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData("MM" + newfields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试三次操作：修改，修改，删除
	 * @throws Exception 
	 */
	private void _test3ModifyModifyDrop() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			at.dropField("M" + newfields[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertNull(column);
		}
	}

	/**
	 * 测试三次操作：修改，删除，增加
	 * @throws Exception 
	 */
	private void _test3ModifyDropAdd() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			at.dropField(newfields[i]);
			addField(at, newfields[i], types[i]);

			at.commit();
			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertNotNull(column);
			assertColumnType(column, types[i]);

		}
	}

	/**
	 * 测试三次操作：修改，增加，删除
	 * @throws Exception 
	 */
	private void _test3ModifyAddDrop() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			addField(at, newfields[i], types[i]);
			at.dropField(newfields[i]);

			try {
				at.commit();
				fail("字段已经存在，不能再增加");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能增加字段"));
			}
		}
	}

	/**
	 * 测试三次操作：删除，修改，修改
	 * @throws Exception 
	 */
	private void _test3DropModifyModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldfields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			try {
				at.commit();
				fail("字段已经不存在，不能再修改");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能修改字段"));
			}
		}

	}

	/**
	 * 测试三次操作：删除，修改，增加
	 * @throws Exception 
	 */
	private void _test3DropModifyAdd() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldfields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);
			addField(at, oldfields[i], types[i]);
			try {
				at.commit();
				fail("字段已经不存在，不能再修改");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能修改字段"));
			}
		}
	}

	/**
	 * 测试三次操作：删除，增加，修改
	 * @throws Exception 
	 */
	private void _test3DropAddModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldfields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			addField(at, oldfields[i], types[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试三次操作：删除，增加，删除
	 * @throws Exception 
	 */
	private void _test3DropAddDrop() throws Exception {
		String[] fields = OLDFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
			addField(at, fields[i], types[i]);
			at.dropField(fields[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(fields[i]);
			assertNull(column);
		}
	}

	/**
	 * 测试三次操作：增加，修改，修改
	 * @throws Exception 
	 */
	private void _test3AddModifyModify() throws Exception {

		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = oldfields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				modifyField(at, newfields[i], "M" + newfields[i], types[i]);

				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = fields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				modifyField(at, "M" + fields[i], "MM" + fields[i], types[i]);
				at.commit();

				TableColumnMetaData column = getColumnMetaData("MM" + fields[i]);
				assertNotNull(column);
				assertColumnType(column, types[i]);
			}
		}
	}

	/**
	 * 测试三次操作：增加，修改，删除
	 * @throws Exception 
	 */
	private void _test3AddModifyDrop() throws Exception {
		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = oldfields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				at.dropField(newfields[i]);

				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = fields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				at.dropField("M" + fields[i]);

				at.commit();

				assertNull(getColumnMetaData("M" + fields[i]));
			}
		}
	}

	/**
	 * 测试三次操作：增加，修改，增加
	 * @throws Exception 
	 */
	private void _test3AddModifyAdd() throws Exception {

		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = oldfields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				addField(at, oldfields[i], types[i]);

				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = fields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				addField(at, fields[i], types[i]);
				at.commit();

				TableColumnMetaData column1 = getColumnMetaData(fields[i]);
				assertNotNull(column1);
				assertColumnType(column1, types[i]);
				TableColumnMetaData column2 = getColumnMetaData("M" + fields[i]);
				assertNotNull(column2);
				assertColumnType(column2, types[i]);
			}
		}
	}

	/**
	 * 测试三次操作：增加，删除，修改
	 * @throws Exception 
	 */
	private void _test3AddDropModify() throws Exception {

		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = oldfields.length;
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				at.dropField(oldfields[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);

				try {
					at.commit();
					fail("字段不存在，不能再修改");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能修改字段"));
				}
			}
		}
	}

	/**
	 * 测试三次操作：增加，删除，增加
	 * @throws Exception 
	 */
	private void _test3AddDropAdd() throws Exception {

		{//已经存在的字段
			String[] fields = OLDFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				addField(at, fields[i], types[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				addField(at, fields[i], types[i]);
				at.commit();

				TableColumnMetaData column = getColumnMetaData(fields[i]);
				assertNotNull(column);
				assertColumnType(column, types[i]);
			}
		}
	}

	/**
	 * 测试两次操作：修改后删除
	 * @throws Exception 
	 */
	private void _test2ModifyDrop() throws Exception {
		String[] oldFields = OLDFIELDNAMES;
		String[] newFields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = oldFields.length;

		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			modifyField(at, oldFields[i], newFields[i], types[i]);
			at.dropField(newFields[i]);
			at.commit();

			assertNull(getColumnMetaData(oldFields[i]));
			assertNull(getColumnMetaData(newFields[i]));
		}
	}

	/**
	 * 测试两次操作：修改后增加
	 * @throws Exception 
	 */
	private void _test2ModifyAdd() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			modifyField(at, oldfields[i], newfields[i], types[i]);
			addField(at, newfields[i], types[i]);

			try {
				at.commit();
				fail("字段已经存在，不能再增加");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能增加字段"));
			}
		}
	}

	/**
	 * 测试两次操作：删除后修改
	 * @throws Exception 
	 */
	private void _test2DropModify() throws Exception {
		String[] oldfields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		AlterTable at = createAlterTable();
		int len = oldfields.length;
		for (int i = 0; i < len; i++) {
			at.dropField(oldfields[i]);
			modifyField(at, oldfields[i], newfields[i], types[i]);

			try {
				at.commit();
				fail("字段已经不存在，不能再修改");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能修改字段"));
			}
		}
	}

	/**
	 * 测试两次操作：删除后增加
	 * @throws Exception 
	 */
	private void _test2DropAdd() throws Exception {
		String[] fields = OLDFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
			addField(at, fields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(fields[i]);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试两次操作：增加后修改
	 * @throws Exception 
	 */
	private void _test2AddModify() throws Exception {
		{//已经存在的字段
			String[] oldfields = OLDFIELDNAMES;
			String[] newfields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = oldfields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, oldfields[i], types[i]);
				modifyField(at, oldfields[i], newfields[i], types[i]);

				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			int len = fields.length;
			AlterTable at = createAlterTable();
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				modifyField(at, fields[i], "M" + fields[i], types[i]);
				at.commit();

				assertNotNull(getColumnMetaData("M" + fields[i]));
			}
		}
	}

	/**
	 * 测试两次操作：增加后删除
	 * @throws Exception 
	 */
	private void _test2AddDrop() throws Exception {
		{//已经存在的字段
			String[] fields = OLDFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				try {
					at.commit();
					fail("字段已经存在，不能再增加");
				}
				catch (Exception e) {
					String msg = e.getMessage();
					assertTrue(msg.startsWith("不能增加字段"));
				}
			}
		}
		{//不存在的字段
			String[] fields = NEWFIELDNAMES;
			char[] types = FIELD_TYPES;
			AlterTable at = createAlterTable();
			int len = fields.length;
			for (int i = 0; i < len; i++) {
				addField(at, fields[i], types[i]);
				at.dropField(fields[i]);
				at.commit();
			}
		}
	}

	/**
	 * 测试增加字段：每种类型的字段都增加一个
	 * @throws Exception 
	 */
	private void _test1AddNotExistField() throws Exception {
		String[] fields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;

		AlterTable at = createAlterTable();
		int len = fields.length;
		for (int i = 0; i < len; i++) {
			addField(at, fields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData(fields[i]);
			assertColumnType(column, types[i]);
		}

	}

	/**
	 * 测试增加已经存在的字段
	 * @throws Exception 
	 */
	private void _test1AddExistField() throws Exception {
		String[] fields = OLDFIELDNAMES;
		char[] types = FIELD_TYPES;

		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			addField(at, fields[i], types[i]);
			try {
				at.commit();
				fail("字段已经存在，不能再增加");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能增加字段"));
			}
		}
	}

	/**
	 * 测试多次增加相同名称的字段
	 * @throws Exception 
	 */
	private void _test2AddAdd() throws Exception {
		String[] fields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;
		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			addField(at, fields[i], types[i]);
			addField(at, fields[i], types[i]);

			try {
				at.commit();
				fail("字段已经存在，不能再增加");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能增加字段"));
			}
		}
	}

	/**
	 * 删除字段
	 * @throws Exception 
	 */
	private void _test1DropExistField() throws Exception {
		String[] fields = OLDFIELDNAMES;

		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
		}
		at.commit();

		for (int i = 0; i < len; i++) {
			TableColumnMetaData column = getColumnMetaData(fields[i]);
			assertNull(column);
		}
	}

	/**
	 * 测试删除不存在的字段
	 * @throws Exception 
	 */
	public void _test1DropNotExistField() throws Exception {
		String[] fields = NEWFIELDNAMES;

		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
			try {
				at.commit();
				fail("字段不存在，不能再删除");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能删除字段"));
			}
		}
	}

	/**
	 * 测试多次删除相同的字段
	 * @throws Exception 
	 */
	private void _test2DropDrop() throws Exception {
		String[] fields = OLDFIELDNAMES;
		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			at.dropField(fields[i]);
			at.dropField(fields[i]);
			try {
				at.commit();
				fail("字段已经不存在，不能再删除");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能删除字段"));
			}
		}

	}

	/**
	* 修改字段 
	 * @throws Exception 
	*/
	private void _test1ModifyExistField() throws Exception {
		String[] oldFields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;

		int len = oldFields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			modifyField(at, oldFields[i], newfields[i], types[i]);
		}
		at.commit();

		for (int i = 0; i < len; i++) {
			TableColumnMetaData column = getColumnMetaData(newfields[i]);
			assertColumnType(column, types[i]);
		}
	}

	/**
	 * 测试修改不存在的字段
	 * @throws Exception 
	 */
	public void _test1ModifyNotExistField() throws Exception {
		String[] fields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;

		int len = fields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			modifyField(at, fields[i], null, types[i]);
			try {
				at.commit();
				fail("字段不存在，不能再修改");
			}
			catch (Exception e) {
				String msg = e.getMessage();
				assertTrue(msg.startsWith("不能修改字段"));
			}
		}
	}

	/**
	 * 测试多次修改相同的字段
	 * @throws Exception 
	 */
	private void _test2ModifyModify() throws Exception {
		String[] oldFields = OLDFIELDNAMES;
		String[] newfields = NEWFIELDNAMES;
		char[] types = FIELD_TYPES;

		int len = oldFields.length;
		AlterTable at = createAlterTable();
		for (int i = 0; i < len; i++) {
			modifyField(at, oldFields[i], newfields[i], types[i]);
			modifyField(at, newfields[i], "M" + newfields[i], types[i]);
			at.commit();

			TableColumnMetaData column = getColumnMetaData("M" + newfields[i]);
			assertNotNull(column);
		}
	}

	private void assertColumnType(TableColumnMetaData column, char fieldtype) {
		char type = SqlFunc.getSubsectionType(column.getType());
		if (type == fieldtype) {
			return;
		}
		/**
		 * 在oralce中
		 * 整形是用number表示的,
		 * 日期是用timestamp表示的，
		 * 时间是用timestamp表示的，
		 * 这里要兼容
		 */
		ConnectionFactory fct = getConnectionFactory();
		if (fct.getDbType().isOracle()) {
			if (type == DbDefiner.FIELD_TYPE_FLOAT && fieldtype == DbDefiner.FIELD_TYPE_INT) {
				return;
			}
			if (type == DbDefiner.FIELD_TYPE_TIMESTAMP && fieldtype == DbDefiner.FIELD_TYPE_DATE) {
				return;
			}
			if (type == DbDefiner.FIELD_TYPE_TIMESTAMP && fieldtype == DbDefiner.FIELD_TYPE_TIME) {
				return;
			}
		}
		/**
		 * 因为布尔型使用整形表示的，这里也要兼容
		 */
		if (type == DbDefiner.FIELD_TYPE_STR && fieldtype == DbDefiner.FIELD_TYPE_LOGIC) {
			return;
		}
		assertEquals(type, fieldtype);
	}
}
