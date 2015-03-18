package com.esen.jdbc.impl;

import java.sql.SQLException;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SqlExecute;
import com.esen.jdbc.SqlFunc;
import com.ibm.db2.jcc.a.SqlException;

import func.FuncTestCase;

public abstract class TestDbMetaDataAbstract extends FuncTestCase {
	public abstract void createProcedure() throws SQLException;

	public abstract void createTrigger() throws SQLException;

	public abstract void createSynonym() throws SQLException;

	public abstract ConnectionFactory getConnectionFactory();

	public String getProcedureName() {
		return "test_metadata_procedure";
	}

	public String getTriggerName() {
		return "test_metadata_trigger";
	}

	public String getSynonymName() {
		return "test_metadata_synonym";
	}

	public void deleteProcedure() {
		//TODO
	}

	public boolean isProcedureExist() {
		ConnectionFactory fct = getConnectionFactory();
		
		return false;
	}

	public void createTestTable() throws Exception {
		ConnectionFactory fct = getConnectionFactory();
		SqlExecute se = SqlExecute.getInstance(fct);
		se.dropTable(getTestTable());
		
		SqlFunc.createTable(fct, getTestTable(), this.getClass(), "test-dbmetadata-table.xml");
	}

	public String getTestTable() {
		return "TEST_DB_METADATA";
	}

}
