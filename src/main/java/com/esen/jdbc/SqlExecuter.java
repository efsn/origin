package com.esen.jdbc;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.jdbc.dialect.impl.ResultSetForLimit;
import com.esen.mdx.LevelMember;
import com.esen.mdx.Mdx;
import com.esen.mdx.Member;
import com.esen.olap2j.OlapDatabaseMetaData;
import com.esen.olap2j.OlapException;
import com.esen.olap2j.metadata.Cube;
import com.esen.olap2j.metadata.CubeField;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 20091118
 * 此类提供执行sql的通用方法；<br>
 * 可以执行select、update、create等sql，也可以执行存储过程，但现在还不支持返回存储过程返回的结果集
 * 示例
 * <pre>
 *   SqlExecuter sqlExe = SqlExcuter.getInstance(conf);
 *   try{
 *     ResultSet rs = (ResultSet)sqlExe.executeSql("select * from tbname ");
 *     // to do ...//
 *     //此处可以调用rs.close，也可以不调用，sqlExe会保证它被关闭
 *     
 *     rs = (ResultSet)sqlExe.executeSql("select * from tbname ");
 *     // to  do
 *     
 *     sqlExe.executeSql("craete table tbname ...");
 *     sqlExe.executeSql("alter table tbname modify ...");
 *     sqlExe.executeSql("update tbname set field='a' where ...");
 *     sqlExe.executeSql("drop table tbname");
 *     sqlExe.executeSql("{call proceduce_name }");
 *   }finally{
 *     sqlExe.close();
 *   }
 * </pre>
 * 
 * 此类是非线程同步的，获取SqlExecuter实例后，可以执行多个sql，<br>
 * 这些sql公用一个数据库连接，执行完毕必须调用close()方法；
 * @author wdeng
 *
 */
public class SqlExecuter {

	private static final Logger log = LoggerFactory.getLogger(SqlExecuter.class);

	private ConnectionFactory conf;

	/**
	 * 下面三个参数在调用executeSql(conf,"select ...")执行查询时会持有；
	 * 调用close()方法，将它们释放；
	 */
	private Connection conn;

	private Statement stat;

	private SqlExecuter(ConnectionFactory conf) {
		this.conf = conf;
	}

	public static SqlExecuter getInstance(ConnectionFactory conf) {
		return new SqlExecuter(conf);
	}

	/**
	 * 在连接池conf中执行sql语句；
	 * <pre>
	 * 执行的sql语句，可以是三种sql：
	 * 1)查询sql，返回ResultSet结果集；
	 * 2)调用存储过程的sql，支持参数；
	 *   调用格式：{call proc_name()}
	 * 3)操作型sql，比如：create,alter,drop,delete,update等；不支持参数；
	 * 注：此方法不支持带参数输入的sql；
	 * </pre>
	 * @param conf   连接池
	 * @param sql  sql语句
	 * @throws SQLException 
	 */
	public final Object executeSql(String sql) throws SQLException {
		return executeSql(sql, 0, 0);
	}

	/**
	 * 用于查询sql返回结果集；
	 * 兼容执行返回结果集的存储过程；
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	public final ResultSet executeQuery(String sql, int start, int limit) throws SQLException {
		Object o = executeSql(sql, start, limit);
		if (o instanceof ResultSet) {
			return (ResultSet) o;
		}
		/**
		 * 对于存储过程返回的结果集，由于可能是多个输出参数，有多个返回值，
		 * 返回第一个遍历到的结果集对象；
		 */
		if (o instanceof Object[]) {
			Object[] oo = (Object[]) o;
			for (int i = 0; i < oo.length; i++) {
				Object oi = oo[i];
				if (oi instanceof ResultSet) {
					return (ResultSet) oi;
				}
			}
		}
		return null;
	}

	/**
	 * 增加了分页参数，只对查询sql其作用；<br>
	 * executeSql(sql,0,-1)=executeSql(sql) <br>
	 * executeSql(sql,0,100) 返回前100行数据；
	 * @param sql
	 * @param start  查询起行数；
	 * @param limit  查询行数；
	 * @return
	 * @throws SQLException
	 */
	public final Object executeSql(String sql, int start, int limit) throws SQLException {
		closeStatement();
		if (conf == null || StrFunc.isNull(sql)) {
			return null;
		}
		if (SqlFunc.isMdx(conf)) {
			if (SqlFunc.isSelect(sql)) {
				String cubeName = SqlFunc.getTablename(sql);
				if (cubeName != null)
					sql = getMdx(cubeName);
			}
			return excuteQurey(sql, start, limit);
		}
		else
		/**
		 * 如果是mdx服务器，总是执行查询
		 */
		if (SqlFunc.isSelect(sql)) {
			return excuteQurey(sql, start, limit);
		}
		else if (SqlFunc.isCallalbe(sql)) {
			return callProcedure(sql, start, limit);
		}
		return excuteUpdate(sql);
	}

	private String getMdx(String cubeName) throws SQLException {
		Mdx mdx = new Mdx();
		mdx.setCubeName(cubeName);
		Connection con = conf.getConnection();
		try {
			OlapDatabaseMetaData dbmd = (OlapDatabaseMetaData) con.getMetaData();
			Cube cube = dbmd.getCube(null, cubeName, false);
			if (cube == null) {
				//			throw new OlapException("不存在的Cube模型：" + cubeName);
				throw new OlapException(I18N.getString("com.esen.jdbc.sqlexecuter.unexistcube",
						"不存在的Cube模型：{0}", new Object[] { cubeName }));
			}
			for (int i = 0; i < cube.getCubeFieldCount(); i++) {
				CubeField cbfd = cube.getCubeField(i);
				if (cbfd.isDimension()) {
					LevelMember lm = new LevelMember(cbfd.getName(), 0);
					mdx.getRowAxis().addMdxElement(lm);
				}
				else {
					Member m = new Member(cbfd.getOwner(), cbfd.getName());
					mdx.getColumnAxis().addMdxElement(m);
				}
			}
		}
		finally {
			con.close();
		}
		return mdx.toString();
	}

	private Object excuteUpdate(String sql) throws SQLException {
		Statement stat = getConnection().createStatement();
		try {
			int k = stat.executeUpdate(sql);
			return new Integer(k);
		}
		finally {
			if (stat != null)
				stat.close();
		}

	}

	private Object callProcedure(String sql, int start, int limit) throws SQLException {
		if (conf.getDbType().isOracle()) {
			return callOracleProcedure(sql, start, limit);
		}
		return callProcedure(getConnection(), sql, start, limit);
	}

	/**
	 * 20100303
	 * 支持返回结果集的存储过程的调用，如果没有结果集返回，则返回null；
	 * 比如：
	 * {call proc_testquery()}   --不带参数
	 * {call proc_testquery2('TESTCASE')}  --带参数
	 * 
	 * 20100318
	 * 返回值定义：
	 * 
	 * 总返回一个Object[]数组；
	 * 
	 * 第一个Object总是个结果集ResultSet，如果是返回多个结果集则是个ResultSet[]，
	 * 这个结果集主要用于非Oracle数据库，结果集返回不在输出参数中；
	 * 对于Oracle 数据库此Object总是null；
	 * 
	 * 第二个Object 是一个整型，返回cstat.getUpdateCount() 的值， 表示受存储过程影响的行计数；
	 * 
	 * 从第三个Object开始，顺序表示输出参数的值；
	 * 
	 * @param conn
	 * @param sql
	 * @param start
	 * @param limit
	 * @return
	 * @throws SQLException
	 */
	private Object[] callProcedure(Connection conn, String sql, int start, int limit) throws SQLException {
		FormatCallableSql fs = new FormatCallableSql(sql.trim());
		String csql = fs.getCallableSql();
		CallableParam[] params = fs.getParams();

		stat = conn.prepareCall(csql);
		CallableStatement cstat = (CallableStatement) stat;

		setParams(cstat, params);
		cstat.execute();
		return getReturnValues(cstat, params, start, limit);

	}

	private Object[] getReturnValues(CallableStatement cstat, CallableParam[] params, int start, int limit)
			throws SQLException {
		//获取结果集
		Object rs = getResultSet(cstat, start, limit);
		//获取受存储过程影响的行计数
		int updateCount = cstat.getUpdateCount();
		//获得输出参数的个数；
		int outnum = getOutParamNumber(params);
		Object[] os = new Object[outnum + 2];
		os[0] = rs;
		os[1] = new Integer(updateCount);
		if (outnum > 0) {
			int k = 2;
			//获取输出参数的值；
			for (int i = 0; i < params.length; i++) {
				CallableParam pmi = params[i];
				if (pmi.getType() == CallableParam.CALLABLE_TYPE_OUT
						|| pmi.getType() == CallableParam.CALLABLE_TYPE_INOUT) {
					Object ov = getOutParamValue(pmi, cstat, i + 1, start, limit);
					os[k++] = ov;
				}
			}
		}
		return os;
	}

	private Object getOutParamValue(CallableParam pmi, CallableStatement cstat, int i, int start, int limit)
			throws SQLException {
		switch (pmi.getSqlType()) {
			case CallableParam.SQLTYPE_NUMBER: {
				double d = cstat.getDouble(i);
				if (cstat.wasNull())
					return null;
				return new Double(d);
			}
			case CallableParam.SQLTYPE_CHAE: {
				return cstat.getString(i);
			}
			case CallableParam.SQLTYPE_DATE: {
				return cstat.getDate(i);
			}
			case CallableParam.SQLTYPE_RESULTSET: {
				ResultSet rs = (ResultSet) cstat.getObject(i);
				return new ResultSetForLimit(rs, start, limit);
			}
		}
		return null;
	}

	private int getOutParamNumber(CallableParam[] params) {
		if (params == null)
			return 0;
		int n = 0;
		for (int i = 0; i < params.length; i++) {
			CallableParam pmi = params[i];
			if (pmi.getType() == CallableParam.CALLABLE_TYPE_OUT || pmi.getType() == CallableParam.CALLABLE_TYPE_INOUT) {
				n++;
			}
		}
		return n;
	}

	private Object getResultSet(CallableStatement cstat, int start, int limit) throws SQLException {
		List rslist = new ArrayList(10);
		ResultSet rs = cstat.getResultSet();
		if (rs != null) {
			rslist.add(new ResultSetForLimit(rs, start, limit));
		}
    /**
     * 在sybase ase数据库上 如果存储过程有返回结果集，那么调用getMoreResults 后会导致之前获取的结果集关闭了
     * 限定一下 sybase 上只支持返回一个结果集 add by jzp 2012-09-19 ISSUE:BI-7056
     */
    if (!(conf.getDbType().isSybaseIQ()||conf.getDbType().isSybase())){
	    while(cstat.getMoreResults()){
	      ResultSet mrs = cstat.getResultSet();
	      if (mrs != null) {
	        rslist.add(new ResultSetForLimit(mrs, start, limit));
	      }
	    }
		}
		int size = rslist.size();
		if (size == 0)
			return null;
		if (size == 1) {
			return rslist.get(0);
		}
		else {
			ResultSet[] rss = new ResultSet[size];
			rslist.toArray(rss);
			return rss;
		}
	}

	private void setParams(CallableStatement cstat, CallableParam[] params) throws SQLException {
		if (params == null || params.length == 0) {
			return;
		}
		for (int i = 0; i < params.length; i++) {
			CallableParam pmi = params[i];
			switch (pmi.getType()) {
				case CallableParam.CALLABLE_TYPE_IN: {
					setInParams(cstat, i + 1, pmi);
					break;
				}
				case CallableParam.CALLABLE_TYPE_OUT: {
					setOutParams(cstat, i + 1, pmi);
					break;
				}
				case CallableParam.CALLABLE_TYPE_INOUT: {
					setInOutParams(cstat, i + 1, pmi);
					break;
				}
			}
		}
	}

	private void setInOutParams(CallableStatement cstat, int i, CallableParam pmi) throws SQLException {
		switch (pmi.getSqlType()) {
			case CallableParam.SQLTYPE_NUMBER: {
				Double d = (Double) pmi.getValue();
				cstat.setDouble(i, d.doubleValue());
				cstat.registerOutParameter(i, Types.DOUBLE);
				break;
			}
			case CallableParam.SQLTYPE_CHAE: {
				String v = (String) pmi.getValue();
				cstat.setString(i, v);
				cstat.registerOutParameter(i, Types.VARCHAR);
				break;
			}
			case CallableParam.SQLTYPE_DATE: {
				Calendar v = (Calendar) pmi.getValue();
				cstat.setDate(i, new java.sql.Date(v.getTimeInMillis()));
				cstat.registerOutParameter(i, Types.DATE);
				break;
			}

		}
	}

	private void setOutParams(CallableStatement cstat, int i, CallableParam pmi) throws SQLException {
		switch (pmi.getSqlType()) {
			case CallableParam.SQLTYPE_NUMBER: {
				cstat.registerOutParameter(i, Types.DOUBLE);
				break;
			}
			case CallableParam.SQLTYPE_CHAE: {
				cstat.registerOutParameter(i, Types.VARCHAR);
				break;
			}
			case CallableParam.SQLTYPE_DATE: {
				cstat.registerOutParameter(i, Types.DATE);
				break;
			}
			case CallableParam.SQLTYPE_RESULTSET: {
				if (conf.getDbType().isOracle()) {//输出结果集参数，只有Oracle有；
					cstat.registerOutParameter(i, oracle.jdbc.OracleTypes.CURSOR);
				}
				else {
					//        	throw new SQLException("不支持游标参数；");
					throw new SQLException(I18N.getString("com.esen.jdbc.sqlexecuter.unablecursor",
							"不支持游标参数；"));
				}
				break;
			}
		}
	}

	private void setInParams(CallableStatement cstat, int i, CallableParam pmi) throws SQLException {
		switch (pmi.getSqlType()) {
			case CallableParam.SQLTYPE_NUMBER: {
				Double d = (Double) pmi.getValue();
				cstat.setDouble(i, d.doubleValue());
				break;
			}
			case CallableParam.SQLTYPE_CHAE: {
				String v = (String) pmi.getValue();
				cstat.setString(i, v);
				break;
			}
			case CallableParam.SQLTYPE_DATE: {
				Calendar v = (Calendar) pmi.getValue();
				cstat.setDate(i, new java.sql.Date(v.getTimeInMillis()));
				break;
			}
		}

	}

	/**
	 * Oralce 执行存储过程，可能会遇到：
	 * ORA-04068: existing state of packages has been discarded.
	 * 这个异常，官方定义：
	 * 04068, 00000, "existing state of packages%s%s%s has been discarded"
	 * Cause: One of errors 4060 - 4067 when attempt to execute a stored procedure.
	 * Action: Try again after proper re-initialization of any application's state.
	 * 这个错误显示执行包的现有状态被另一个会话的一个动作无效化了。
	 * 这个“状态”涉及包在规范或体中定义的任何全局变量(包括常量)。
	 * 引起这个错误的动作一般是(但不局限于此)在得到了发生错误的会话所使用的连接之后包的重新编译。
	 * Oracle 建议的动作是重新初始化应用程序状态以调整包的新状态后重新尝试。
	 * 
	 * 原因：Oralce在调用存储过程包时，如果存储过程包中有全局变量，且包中的存储过程被另一个会话修改，并编译，
	 * 那么连接池中先前创建的连接再调用此存储过程包，就会出现ORA-04068: existing state of packages has been discarded.异常；
	 * 解决办法：不使用连接池中的连接，执存储过程总是创建新的连接执行；
	 * @param conf
	 * @param sql
	 * @return
	 * @throws SQLException
	 */
	private Object callOracleProcedure(String sql, int start, int limit) throws SQLException {
		/**
		 * 由于执行oracle存储过程，需要重新获取新连接，原有的连接必须先关闭，否则会嵌套获取连接；
		 */
		close();
		conn = conf.getNewConnection();
		return callProcedure(conn, sql, start, limit);

	}

	private ResultSet excuteQurey(String sql, int start, int limit) throws SQLException {
		conn = getConnection();
		stat = conn.createStatement();
		if (start <= 0 && limit <= 0) {//不分页
			return stat.executeQuery(sql);
		}
		else {
			return conf.getDialect().queryLimit(stat, sql, start, limit);
		}
	}

	public Connection getConnection() throws SQLException {
		if (conn == null) {
			/**
			 * mdx数据源总是从连接池重新获取连接，否则会出现：
			 * 无法激活多维数据集"zdsy2/new2) x_"，因为它们在多维数据集"zdsy2/new2"上已经处于活动状态。请先清除活动的多维数据集，然后重试。
			 * 的异常；
			 */
			if (SqlFunc.isMdx(conf)) {
				conn = conf.getNewConnection();
			}
			else {
				conn = conf.getConnection();
			}
		}
		return conn;
	}

	public void close() throws SQLException {
		try {
			closeStatement();
			if (conn != null)
				conn.close();
		}
		finally {
			conn = null;
		}
	}

	private void closeStatement() throws SQLException {
		try {
			if (stat != null)
				stat.close();
		}
		catch (Exception e) {
			log.warn(e.getMessage(), e);
		}
		finally {
			stat = null;
		}
	}
}
