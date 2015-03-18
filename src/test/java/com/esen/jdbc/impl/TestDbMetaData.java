package com.esen.jdbc.impl;

import com.esen.util.ArrayFunc;

import func.FuncTestCase;

/**
 * 测试是否能够获得数据库的存储过程,触发器,同义词
 *
 * @author zhuchx
 */
public class TestDbMetaData extends FuncTestCase {
	public static void main(String[] args) throws Exception {
		TestDbMetaData t = new TestDbMetaData();
		t.test();
	}

	public void test() throws Exception {
		TestDbMetaDataAbstract[] list = getTestDbMetaData();
		int len = list == null ? 0 : list.length;
		for (int i = 0; i < len; i++) {
			TestDbMetaDataAbstract test = list[i];
			test.createTestTable();
			
			test.createProcedure();
			test.createTrigger();
			test.createSynonym();
		}
	}

	public TestDbMetaDataAbstract[] getTestDbMetaData() {
		TestDbMetaDataAbstract[] rs = new TestDbMetaDataAbstract[] { null,// 
				new TestOracleDbMetaData(),//oracle
				new TestMssql2005DbMetaData(),//mssql2005
				new TestMssqlDbMetaData(),//mssql
				new TestMysqlDbMetaData(),//mysql
				new TestDb2DbMetaData(),//db2
				new TestSybaseDbMetaData(),//sybase
		};
		return (TestDbMetaDataAbstract[]) ArrayFunc.excludeNull(rs);
	}
}
