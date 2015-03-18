package com.esen.jdbc.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.esen.db.sql.Field;
import com.esen.db.sql.SelectTable;
import com.esen.db.sql.SelectUnionTable;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DataCopyForUpdate;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.IProgress;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

public class DataCopyForUpdateImpl extends DataCopyForUpdate {
	private List srcsqllist;//保存srcsql参数

	private List srctemptbs;//每个srcsql写入目的表的临时表列表；

	private int[] tempFlag;//记录对应序号的srctemptbs中，保存的是不是生成的临时表；

	private List srcTmpMeta;//记录srctemptbs中表的表结构；

	private boolean onlyInsert;

	private String[] keys;

	private ConnectionFactory srcpool;

	private ConnectionFactory targetpool;

	private Connection srcconn;

	private Connection targetconn;

	private boolean srcConnFromPool;

	private boolean targetConnFromPool;

	private String targetTable;

	private TableMetaData targetMeta;//记录目的表表结构；

	private IProgress ipro;

	public DataCopyForUpdateImpl() {
		this.srcsqllist = new ArrayList();
	}

	/**
	 * 更新过程，参考DataCopyForUpdate类的说明；
	 * 
	 */
	public void executeUpdate() throws Exception {
		//addLog(ipro,"开始执行更新；");
		addLog(ipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.startupdate",
				"开始执行更新；"));
		long l = System.currentTimeMillis();

		//检查参数的有效性；
		checkParams();

		//检查主键，初始化目的表结构targetMeta
		initKeys();

		try {
			//将srcsql的数据写入目的池的临时表；
			impDataToTargetPoolForTempTable();

			//如果设置了主键,检查这些临时表中有没有设置的主键字段，如果没有，出异常提示；
			checkTempTableForKeys();

			if (isNoKeys()) {
				executeUpdateForNoKeys();
			}
			else {
				executeUpdateForKeys();
			}

		}
		finally {
			dropTempTableForSrcSql();
			//执行更新完成后，清空addSourceSql中的sql，便于多次执行executeUpdate();
			clearSrcSql();
		}
		//addLog(ipro,"执行更新总耗时"+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(ipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.updatecost",
						"执行更新总耗时") + StrFunc.formatTime(System.currentTimeMillis() - l));
	}

	/**
	 * 对没有主键的情况，写入数据；
	 * 直接写入目的表和源表都存在的字段的值；
	 * @throws SQLException 
	 */
	private void executeUpdateForNoKeys() throws SQLException {
		Dialect dl = getTargetDialect();
		Connection conn = this.getTargetConnection();
		long l;
		try {
			for (int i = 0; i < srctemptbs.size(); i++) {
				String fromtable = (String) srctemptbs.get(i);
				TableMetaData tmdi = (TableMetaData) srcTmpMeta.get(i);
				String[] importfields = getSameFieldsForImport(tmdi, targetMeta);
				l = System.currentTimeMillis();
				String insertsql = getInsertSql(dl, targetTable, fromtable, importfields);
				//addLog(ipro, "将表" + fromtable + "数据写入目的表" + targetTable + ":");
				Object[] param = new Object[] { fromtable, targetTable };
				addLog(ipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.t2t",
						"将表{0}数据写入目的表{1}:", param));
				addLog(ipro, insertsql);
				excuteSql(conn, insertsql);
				// addLog(ipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
				addLog(ipro,
						I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
								"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
			}
		}
		finally {
			this.closeTargetConnection(conn);
		}
	}

	/**
	 * 返回两个表都有的字段列表；
	 * @param tmdi
	 * @return
	 */
	private String[] getSameFieldsForImport(TableMetaData tmdi, TableMetaData tgmeta) {
		int colCount = tmdi.getColumnCount();
		List fields = new ArrayList(colCount);
		for (int i = 0; i < colCount; i++) {
			TableColumnMetaData scoli = tmdi.getColumn(i);
			TableColumnMetaData tcoli = tgmeta.getColumn(scoli.getName());
			/**
			 * 相同字段名的源表字段和目的表字段，如果它们的数据类型不一致，则该字段不能进行数据导入；
			 */
			if (tcoli != null && scoli.getType() == tcoli.getType()) {
				fields.add(scoli.getName());
			}
		}
		String cols[] = new String[fields.size()];
		fields.toArray(cols);
		return cols;
	}

	/**
	   * 对设置了主键的情况，写入数据库；
	   * @throws Exception
	   */
	private void executeUpdateForKeys() throws Exception {
		//通过union将所有srcsql的数据，全链接到一起；
		String unionsql = getUnionSqlForTempTable();

		//将这个unionsql数据，写入目的表；
		Dialect dl = getTargetDialect();
		DbDefiner dbdef = dl.createDbDefiner();
		Connection conn = this.getTargetConnection();
		String newtbname = dbdef.lockNewTableName(conn, "TMP_" + targetTable);
		try {
			conn.setAutoCommit(false);
			//insertTableByQuery(dl,conn,newtbname,unionsql,ipro,"将所有源sql的数据写入一个临时表：");
			insertTableByQuery(dl, conn, newtbname, unionsql, ipro, I18N.getString(
					"com.esen.jdbc.data.datacopyforupdateimpl.importall2tmp", "将所有源sql的数据写入一个临时表："));

			String[] importfields = getImportfields(dl, conn, newtbname);
			if (onlyInsert) {//只写入新数据
				appendData(dl, conn, newtbname, targetTable, keys, importfields, ipro);
			}
			else {//更新数据
				if (targetMeta.getColumnCount() == importfields.length) {
					//全字段更新
					updateAllFieldData(dl, conn, newtbname, targetTable, keys, importfields, ipro);
				}
				else {
					//部分字段更新
					updateSomeFieldData(dl, conn, newtbname, targetTable, keys, importfields, ipro);
				}
			}
			conn.commit();
		}
		catch (Exception ex) {
			conn.rollback();
			throw ex;
		}
		finally {
			if (conn != null) {
				//          /addLog(ipro,"删除临时表："+newtbname);
				addLog(ipro,
						I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.deltmp",
								"删除临时表：") + newtbname);
				try {
					//删除临时表出异常，需要关闭连接；
					dropTable(dl, conn, newtbname);
					dbdef.unlockNewTableName(conn, newtbname);
				}
				finally {
					this.closeTargetConnection(conn);
				}
			}
		}
	}

	/**
	 * 20091204
	 * 在执行更新后调用，用于清空此次更新的源sql；
	 * 便于多次更新：
	 * <pre>
	 * DataCopyForUpdate du = DataCopyForUpdate.createInstance();
	 * ...
	 * du.addSourceSql("select zb1,zb2,zb3 from ...");
	 * du.addSourceSql("select zb4,zb5,zb6... from ...");
	 * du.executeUpdate();
	 * 
	 * du.addSourceSql("select zb1,zb2,zb3 from ...");
	 * du.addSourceSql("select zb4,zb5,zb6... from ...");
	 * du.executeUpdate();
	 * 
	 * </pre>
	 * <pre>
	 * 做这个改动，是因为：每次更新的字段太多的话，会超过数据库的限制：
	 * Oracle会出异常：ORA-01467:sort key too long
	 * Cause:A DISTINCT, GROUP BY, ORDER BY, or SET operation requires a sort key longer than that supported by Oracle. Either too many columns or too many group functions were specified in the SELECT statement.
	 * Action:Reduce the number of columns or group functions involved in the operation.
	 * 
	 * </pre>
	 */
	private void clearSrcSql() {
		this.srcsqllist.clear();
		this.srctemptbs.clear();
		tempFlag = null;
		srcTmpMeta.clear();
		targetMeta = null;
	}

	/**
	 * 将一个sql的数据写入一个临时表；
	 * @param dl
	 * @param conn
	 * @param tbname
	 *        临时表表名生成的依据；
	 * @param sql
	 *        一个查询sql；
	 * @param logipro
	 *        记录日志的接口；
	 * @param logstr
	 *        日志内容；
	 * @return
	 * @throws Exception
	 */
	private static void insertTableByQuery(Dialect dl, Connection conn, String tbname, String sql, IProgress logipro,
			String logstr) throws Exception {
		long l = System.currentTimeMillis();
		//使用物理表，如果用临时表，Oracle需要先创建表，再插入数据，麻烦；
		String createsql = dl.getCreateTableByQureySql(tbname, sql, false);
		addLog(logipro, logstr);
		addLog(logipro, createsql);
		
		/*
		 * BUG:ESENFACE-1081: modify by liujin 2014.06.19
		 * 事务中不支持创建表时，需要修改连接的自动提交属性。
		 */
		if (!dl.supportCreateTableInTransaction()
				&& !conn.getAutoCommit()) {
			conn.commit();
			conn.setAutoCommit(true);
			excuteSql(conn, createsql);
			conn.setAutoCommit(false);
		} else {
			excuteSql(conn, createsql);
		}
		//如果目标表在db2上，为生成的临时表插入数据；
		insertDataForDB2(dl, conn, tbname, sql);
		//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(logipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
						"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
	}

	private String[] getImportfields(Dialect dl, Connection conn, String newtbname) throws Exception {
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(newtbname);
		String[] flds = new String[tmd.getColumnCount()];
		for (int i = 0; i < tmd.getColumnCount(); i++) {
			flds[i] = tmd.getColumnName(i);
		}
		return flds;
	}

	private void initKeys() throws Exception {
		Connection conn = this.getTargetConnection();
		try {
			DbMetaData dbmd = getTargetDialect().createDbMetaData(conn);
			targetMeta = dbmd.getTableMetaData(targetTable);
			if (isNoKeys()) {
				//没有指定主键，使用目的表的主键或者唯一索引；
				String[] pkeys = targetMeta.getPrimaryKey();
				if (pkeys == null || pkeys.length == 0) {
					//目的表没有主键，找唯一索引，如果有多个，找到第一个就返回；
					TableIndexMetaData[] indes = targetMeta.getIndexes();
					for (int i = 0; indes != null && i < indes.length; i++) {
						if (indes[i].isUnique()) {
							keys = indes[i].getColumns();
							break;
						}
					}
					/**
					 * 不设置keys也是有意义的，直接写入数据；
					 */
					/*if(keys==null||keys.length==0){
					  throw new Exception("没有指定主键，目的表也没有主键或者唯一索引；");
					}*/
				}
				else {
					keys = pkeys;
				}
			}
			else {
				checkKeysForTable(targetMeta);

			}
		}
		finally {
			this.closeTargetConnection(conn);
		}
	}

	private boolean isNoKeys() {
		return keys == null || keys.length == 0;
	}

	private void checkKeysForTable(TableMetaData tmd) throws Exception {
		if (isNoKeys()) {
			return;
		}
		//指定了主键，检查主键是否是目的表的字段；
		for (int i = 0; i < keys.length; i++) {
			String keyfieldi = keys[i];
			if (!isField(tmd, keyfieldi)) {
				//throw new Exception("指定的主键字段"+keyfieldi+"不是表"+tmd.getTableName()+"的字段；");
				Object[] param = new Object[] { keyfieldi, tmd.getTableName() };
				throw new Exception(I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.unmatchfield",
						"指定的主键字段{0}不是表{1}的字段；", param));
			}
		}
		//指定的主键，是不是目的表的主键；
		//TODO

	}

	private boolean isField(TableMetaData tmd, String keyfieldi) {
		return tmd.getColumn(keyfieldi) != null;
	}

	/**
	 * 
	 * @return
	 * @throws Exception 
	 */
	private String getUnionSqlForTempTable() throws Exception {
		List srcFields = new ArrayList(targetMeta.getColumnCount());//存储所有srcsql中，对应目的表字段的列表；
		for (int i = 0; i < srcTmpMeta.size(); i++) {
			TableMetaData tmdi = (TableMetaData) srcTmpMeta.get(i);
			for (int j = 0; j < tmdi.getColumnCount(); j++) {
				TableColumnMetaData srccolij = tmdi.getColumn(j);
				TableColumnMetaData colij = getFieldMeta(targetMeta, srccolij);
				if (colij != null) {
					if (!contains(srcFields, colij.getName())) {
						srcFields.add(colij);
					}
				}
			}
		}
		SelectTable sts[] = new SelectTable[srcTmpMeta.size()];
		Dialect dialect = getTargetDialect();
		for (int i = 0; i < srcTmpMeta.size(); i++) {
			TableMetaData tmdi = (TableMetaData) srcTmpMeta.get(i);
			SelectTable sti = new SelectTable();
			sti.addTable(tmdi.getTableName());
			for (int j = 0; j < srcFields.size(); j++) {
				TableColumnMetaData coli = (TableColumnMetaData) srcFields.get(j);
				String coliname = coli.getName();
				TableColumnMetaData colij = getFieldMeta(tmdi, coli);
				if (colij != null) {
					sti.addField(new Field(coliname));
				}
				else {
					Field f = null;
					int dt = coli.getType();
					char type = SqlFunc.getType(dt);
					switch (type) {
						case 'I':
						case 'N': {
							f = new Field("0", coliname);
							break;
						}
						case 'D': {
							f = new Field(dialect.funcToSqlConst(null, dt), coliname);
							break;
						}
						default:
							f = new Field(dialect.funcToSqlConst(null, dt), coliname);
					}
					sti.addField(f);
				}
			}
			for (int j = 0; j < keys.length; j++) {
				sti.addField(new Field(keys[j]));
			}
			sts[i] = sti;
		}
		if (sts.length == 1) {
			return sts[0].getSql(dialect);
		}
		SelectTable st = new SelectUnionTable(sts, SelectUnionTable.UNION_ALL, false);
		for (int j = 0; j < srcFields.size(); j++) {
			TableColumnMetaData coli = (TableColumnMetaData) srcFields.get(j);
			int dt = coli.getType();
			char type = SqlFunc.getType(dt);
			String fs = coli.getName();
			String fieldstr = null;
			switch (type) {
				case 'I':
				case 'N': {
					fieldstr = "sum(" + fs + ")";
					break;
				}
				default: {
					fieldstr = "max(" + fs + ")";
				}
			}
			st.addField(new Field(fieldstr, fs));
		}
		for (int j = 0; j < keys.length; j++) {
			st.addField(new Field(keys[j]));
			st.addGroupBy(keys[j]);
		}
		return st.getSql(dialect);
	}

	private boolean contains(List srcFields, String fieldij) {
		for (int i = 0; i < srcFields.size(); i++) {
			TableColumnMetaData fieldi = (TableColumnMetaData) srcFields.get(i);
			if (fieldij.equalsIgnoreCase(fieldi.getName())) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 指定字段名是目的表的字段，且不是主键字段;
	 * @param fieldij
	 * @return
	 */
	private TableColumnMetaData getFieldMeta(TableMetaData meta, TableColumnMetaData fieldij) {
		TableColumnMetaData col = meta.getColumn(fieldij.getName());
		if (col != null) {
			//是目的表的字段
			for (int j = 0; j < keys.length; j++) {
				if (col.getName().equalsIgnoreCase(keys[j])) {
					//是主键字段，返回false
					return null;
				}
			}
			/**
			 * 相同字段名的源表字段和目的表字段，如果它们的数据类型不一致，则该字段不能进行数据导入；
			 */
			if (col.getType() == fieldij.getType())
				return col;
		}
		return null;
	}

	private void checkTempTableForKeys() throws Exception {
		srcTmpMeta = new ArrayList(srctemptbs.size());
		Connection conn = this.getTargetConnection();
		try {
			DbMetaData dbmd = this.getTargetDialect().createDbMetaData(conn);
			for (int i = 0; i < srctemptbs.size(); i++) {
				String tmptbi = (String) srctemptbs.get(i);
				TableMetaData tmdi = dbmd.getTableMetaData(tmptbi);
				checkKeysForTable(tmdi);
				srcTmpMeta.add(tmdi);
			}
		}
		finally {
			this.closeTargetConnection(conn);
		}
	}

	private void dropTempTableForSrcSql() throws Exception {
		Connection conn = this.getTargetConnection();
		DbDefiner def = getTargetDialect().createDbDefiner();
		try {
			for (int i = 0; i < srctemptbs.size(); i++) {
				String tmptbi = (String) srctemptbs.get(i);
				if (tmptbi == null)
					continue;
				if (tempFlag[i] == 1) {
					try {
						//addLog(ipro,"删除临时表："+tmptbi);
						addLog(ipro,
								I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.deltmp",
										"删除临时表：") + tmptbi);
						def.dropTable(conn, null, tmptbi);
						def.unlockNewTableName(conn, tmptbi);
					}
					catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		finally {
			this.closeTargetConnection(conn);
		}
	}

	/**
	 * 源连接池与目的连接池不同，把srcsqllist中的sql或者表的数据从源连接池写入目的连接池的临时表；<br>
	 * 源连接池与目的连接池相同，把srcsqllist中sql写入临时表；
	 * @throws Exception 
	 */
	private void impDataToTargetPoolForTempTable() throws Exception {
		srctemptbs = new ArrayList(srcsqllist.size());
		tempFlag = new int[srcsqllist.size()];
		if (compareDataBase()) {
			//相同的连接池
			Dialect dl = this.getTargetDialect();
			DbDefiner dbdef = dl.createDbDefiner();
			Connection conn = this.getTargetConnection();
			try {
				for (int i = 0; i < srcsqllist.size(); i++) {
					String sqli = (String) srcsqllist.get(i);
					if (SqlFunc.isValidSymbol(sqli)) {
						//直接是个表名，不用将数据写入新的临时表；
						srctemptbs.add(sqli);
					}
					else {
						String tbi = SqlFunc.getTablename(sqli);
						if (!StrFunc.isNull(tbi)) {
							//类似select * from tbname 的sql，直接使用tbname，不需要生成临时表；
							srctemptbs.add(tbi);
						}
						else {
							String newtbname = dbdef.lockNewTableName(conn, "TMP_" + targetTable);
							//insertTableByQuery(dl,conn,newtbname,sqli,ipro,"将源sql的数据写入临时表：");
							insertTableByQuery(dl, conn, newtbname, sqli, ipro, I18N.getString(
									"com.esen.jdbc.data.datacopyforupdateimpl.wsql2tmp",
									"将源sql的数据写入临时表："));
							srctemptbs.add(newtbname);
							tempFlag[i] = 1;
						}
					}
				}
			}
			finally {
				this.closeTargetConnection(conn);
			}
		}
		else {
			//不同的连接池，先读取源连接池的数据，再写入目的池；
			DataCopy dataCopy = DataCopy.createInstance();
			dataCopy.setIprogress(ipro);
			Connection scon = this.getSourceConnection();
			try {
				Connection tcon = this.getTargetConnection();
				try {
					for (int i = 0; i < srcsqllist.size(); i++) {
						String sqli = (String) srcsqllist.get(i);
						String tmpname = "TMP_" + targetTable;
						String tbi = dataCopy.selectInto(scon, sqli, tcon, tmpname, DataCopy.OPT_CREATENEWTABLE);
						srctemptbs.add(tbi);
						tempFlag[i] = 1;
					}
				}
				finally {
					this.closeTargetConnection(tcon);
				}
			}
			finally {
				this.closeSourceConnection(scon);
			}
		}
	}

	private void checkParams() throws Exception {
		if ((this.srcconn == null || this.targetconn == null) && (this.srcpool == null || this.targetpool == null)) {
			//throw new Exception("如果使用连接池，请设置源连接池和目的连接池；如果使用连接，请设置源连接和目的连接；");
			throw new Exception(I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.connpoolex",
					"如果使用连接池，请设置源连接池和目的连接池；如果使用连接，请设置源连接和目的连接；"));
		}
		if (srcconn == null) {
			//源连接为空，表示需要从源连接池获取连接；
			this.srcConnFromPool = true;
		}
		if (targetconn == null) {
			//目的连接为空，表示需要从目的连接池获取连接；
			this.targetConnFromPool = true;
		}
		if (srcsqllist == null || srcsqllist.size() == 0) {
			//      /throw new Exception("请使用addSourceSql(sql)方法增加源sql；");
			throw new Exception(I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlex1",
					"请使用addSourceSql(sql)方法增加源sql；"));
		}
		if (targetTable == null || targetTable.length() == 0 || !SqlFunc.isValidSymbol(targetTable)) {
			// throw new Exception("目的表不能为空，或者目的表不是一个正确的表名；");
			throw new Exception(I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.tartablenull",
					"目的表不能为空，或者目的表不是一个正确的表名；"));
		}
		Connection conn = this.getTargetConnection();
		try {
			boolean tf = getTargetDialect().createDbDefiner().tableExists(conn, null, targetTable);
			if (!tf) {
				// throw new Exception("目的表"+targetTable+"不存在；");
				throw new Exception(I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.tartableno",
						"目的表{0}不存在；", new Object[] { targetTable }));
			}
		}
		finally {
			this.closeTargetConnection(conn);
		}
	}

	public void addSourceSql(String sqlortablename) {
		srcsqllist.add(sqlortablename);
	}

	public void isOnlyInsertNewRecord(boolean onlyInsert) {
		this.onlyInsert = onlyInsert;
	}

	public void setPrimaryKeys(String[] keys) {
		this.keys = keys;
	}

	public void setSourceDataPool(ConnectionFactory srcpool) {
		this.srcpool = srcpool;
	}

	public void setSourceConnection(Connection srcconn) {
		this.srcconn = srcconn;
	}

	public void setTargetDataPool(ConnectionFactory targetpool) {
		this.targetpool = targetpool;
	}

	public void setTargetConnection(Connection targetconn) {
		this.targetconn = targetconn;
	}

	public void setTargetTable(String tbname) {
		this.targetTable = tbname;
	}

	public void setIprogress(IProgress ipro) {
		this.ipro = ipro;
	}

	private Connection getSourceConnection() throws SQLException {
		if (srcconn != null)
			return srcconn;
		return srcpool.getConnection();
	}

	private void closeSourceConnection(Connection con) throws SQLException {
		if (this.srcConnFromPool) {
			con.close();
		}
	}

	private Connection getTargetConnection() throws SQLException {
		if (targetconn != null)
			return targetconn;
		return targetpool.getConnection();
	}

	private void closeTargetConnection(Connection con) throws SQLException {
		if (this.targetConnFromPool) {
			con.close();
		}
	}

	private Dialect getTargetDialect() {
		if (targetconn != null) {
			return SqlFunc.createDialect(targetconn);
		}
		return this.targetpool.getDialect();
	}

	/**
	 * 判断源连接池和目的连接是否相同、源连接和目的连接所在连接池是否相同；
	 * @return
	 */
	private boolean compareDataBase() {
		if (this.srcconn != null && this.targetconn != null) {
			return SqlFunc.compareConnection(srcconn, targetconn);
		}
		if (this.srcpool != null && this.targetpool != null) {
			return srcpool.compareDataBaseTo(targetpool);
		}

		return false;
	}

	/**
	 * 将连接conn中的fromtable的数据写入totable;
	 * 根据主键fieldkeys,更新totable中的数据，即：fieldkeys存在的更新，不存在的写入；
	 * importfields 需要写入的字段列表；
	 * 如果只是写入totable的部分字段，调用此方法；
	 * @param dl
	 * @param conn
	 * @param fromtable
	 * @param totable
	 * @param fieldkeys
	 * @param importfields
	 * @param logipro
	 * @throws Exception
	 */
	public static void updateSomeFieldData(Dialect dl, Connection conn, String fromtable, String totable,
			String[] fieldkeys, String[] importfields, IProgress logipro) throws Exception {
		//写入部分字段；
		//获取目的表结构
		TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(totable);
		
		/*
		 * 将临时表的字段按照目的表的非写入字段补全，生成一个sql；
		 * 
		 * BUG:ESENFACE-1081: modify by liujin 2014.07.22
		 * 创建临时表的数据中如果有自增列的值时，在将临时表的数据插入目的表时，对于不支持插入自增列数据操作的数据库，会报错。
		 * 解决方法：
		 * 1.修改参数 SET IDENTITY_INSERT tablename ON 以后，允许插入自增列的值。
		 *   但如果自增列有空值，依然报错，自增列不允许为空值。
		 * 2.创建临时表时，不允许使用自增列。
		 */
		String querysql = getCreateNewTmpTableSql(totable, fromtable, importfields, fieldkeys, tmd);

		DbDefiner dbdef = dl.createDbDefiner();
		//将补全后的sql，写入一个新的临时表；
		String newtbname = dbdef.lockNewTableName(conn, "TMP_" + totable);
		//insertTableByQuery(dl,conn,newtbname,querysql,logipro,"将临时表"+fromtable+"的字段按照目的表"+totable+"的非写入字段补全，写入另一个临时表:");
		Object[] param = new Object[] { fromtable, totable };
		insertTableByQuery(dl, conn, newtbname, querysql, logipro, I18N.getString(
				"com.esen.jdbc.data.datacopyforupdateimpl.tmpfull", "将临时表{0}的字段按照目的表{1}的非写入字段补全，写入另一个临时表:", param));

		try {
			//删除目的表中，和新临时表的交集数据；
			long l = System.currentTimeMillis();
			String delsql = getDeleteSql(dl, totable, newtbname, fieldkeys);
			// addLog(logipro,"删除目的表"+totable+"中，和新临时表"+newtbname+"的交集数据:");
			Object[] param1 = new Object[] { totable, newtbname };
			addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.deltmpsame",
					"删除目的表{0}中，和新临时表{1}的交集数据:", param1));
			addLog(logipro, delsql);
			excuteSql(conn, delsql);
			//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
			addLog(logipro,
					I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
							"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
			
			//将新临时表数据插入目的表；
			String[] flds_tmp = new String[tmd.getColumnCount()];
			int i = 0, j = 0;
			for (; i < tmd.getColumnCount(); i++) {
				if (!tmd.getColumn(i).isAutoInc()) {
					flds_tmp[j] = tmd.getColumnName(i);
					j++;
				}
			}
			
			//不带自增列
			String[] flds = null;
			if (i == j) {
				flds = flds_tmp;
			} else {
				flds = new String[i - 1];
				for (i = 0; i < j; i++) {
					flds[i] = flds_tmp[i];
				}
			}
			
			l = System.currentTimeMillis();
			String insertsql = getInsertSql(dl, totable, newtbname, flds);
			// addLog(logipro,"将新临时表"+newtbname+"数据插入目的表"+totable+":");
			Object[] paramn = new Object[] { newtbname, totable };
			addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.ntmp2tar",
					"将新临时表{0}数据插入目的表{1}", paramn));
			addLog(logipro, insertsql);
			excuteSql(conn, insertsql);
			// addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
			addLog(logipro,
					I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
							"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
		}
		finally {
			//删除新临时表；
			//addLog(logipro,"删除新临时表："+newtbname+";");
			addLog(logipro,
					I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.delntmp",
							"删除新临时表") + newtbname + ";");
			dropTable(dl, conn, newtbname);
			dbdef.unlockNewTableName(conn, newtbname);
		}
	}

	private static void insertDataForDB2(Dialect dl, Connection conn, String newtbname, String querysql)
			throws Exception {
		DataBaseInfo db = dl.getDataBaseInfo();
		if (db.isDb2()) {
			TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(newtbname);
			StringBuffer sql = new StringBuffer(64);
			sql.append("insert into ").append(newtbname).append(" (");
			for (int i = 0; i < tmd.getColumnCount(); i++) {
				if (i > 0)
					sql.append(',');
				sql.append(tmd.getColumnName(i));
			}
			sql.append(") \n(");
			sql.append(querysql);
			sql.append(")");
			excuteSql(conn, sql.toString());
		}

	}

	private static void dropTable(Dialect dl, Connection conn, String tbname) throws Exception {
		DbDefiner dbf = dl.createDbDefiner();
		if (!StrFunc.isNull(tbname) && dbf.tableExists(conn, null, tbname)) {
			dbf.dropTable(conn, null, tbname);
		}
	}

	private static String getCreateNewTmpTableSql(String totable, String fromtable, String[] importfields,
			String[] fieldkeys, TableMetaData tmd) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("select ");
		for (int i = 0; i < importfields.length; i++) {
			if (i > 0)
				sql.append(",");
			sql.append("b.").append(importfields[i]);
		}
		for (int i = 0; i < tmd.getColumnCount(); i++) {
			String cn = tmd.getColumnName(i);
			/*
			 * BUG:ESENFACE-1081: modify by liujin 2014.07.22
			 * 不需要自增列的值
			 */
			if (!hasField(cn, importfields) && !tmd.getColumn(i).isAutoInc()) {
				sql.append(",a.").append(cn);
			}
		}
		sql.append(" from ").append(fromtable).append(" b \n");
		sql.append("left join ").append(totable).append(" a\n");
		sql.append("on ");
		for (int i = 0; i < fieldkeys.length; i++) {
			if (i > 0)
				sql.append(" and ");
			sql.append("b.").append(fieldkeys[i]).append("=a.").append(fieldkeys[i]);
		}
		return sql.toString();
	}

	private static boolean hasField(String cn, String[] importfields) {
		for (int i = 0; i < importfields.length; i++) {
			if (cn.equalsIgnoreCase(importfields[i]))
				return true;
		}
		return false;
	}

	/**
	 * 将连接conn中的fromtable的数据写入totable;
	 * 根据主键fieldkeys,更新totable中的数据，即：fieldkeys存在的更新，不存在的写入；
	 * importfields 需要写入的字段列表；
	 * 如果是写入totable的所有字段，调用此方法；
	 * @param dl
	 * @param conn
	 * @param fromtable
	 * @param totable
	 * @param fieldkeys
	 * @param importfields
	 * @param logipro
	 * @throws Exception
	 */
	public static void updateAllFieldData(Dialect dl, Connection conn, String fromtable, String totable,
			String[] fieldkeys, String[] importfields, IProgress logipro) throws Exception {
		//删除目的表中与临时表共有的数据
		long l = System.currentTimeMillis();
		String delsql = getDeleteSql(dl, totable, fromtable, fieldkeys);
		//addLog(logipro, "删除目的表" + totable + "中与临时表" + totable + "共有的数据:");
		Object[] param = new Object[] { totable, totable };
		addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.delsamefs2t",
				"删除目的表{0}中与临时表{1}共有的数据:", param));
		addLog(logipro, delsql);
		excuteSql(conn, delsql);
		//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(logipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
						"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));

		//将临时表数据写入目的表
		l = System.currentTimeMillis();
		String insertsql = getInsertSql(dl, totable, fromtable, importfields);
		//addLog(logipro, "将临时表" + fromtable + "数据写入目的表" + totable + ":");
		Object[] param1 = new Object[] { fromtable, totable };
		addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.wft2t",
				"将临时表{0}数据写入目的表{1}:", param1));
		addLog(logipro, insertsql);
		excuteSql(conn, insertsql);
		//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(logipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
						"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
	}

	/**
	 * 将连接conn中的fromtable的数据写入totable;
	 * 只写入fromtable中没有的数据，keys是主键依据；
	 * importfields 是写入的字段列表；
	 * @param dl
	 * @param conn
	 * @param fromtable
	 * @param totable
	 * @param keys
	 * @param importfields
	 * @throws Exception 
	 */
	public static void appendData(Dialect dl, Connection conn, String fromtable, String totable, String[] fieldkeys,
			String[] importfields, IProgress logipro) throws Exception {
		//删除临时表中与目的表共有的数据
		long l = System.currentTimeMillis();
		String delsql = getDeleteSql(dl, fromtable, totable, fieldkeys);

		// addLog(logipro, "删除临时表" + fromtable + "中与目的表" + totable + "共有的数据:");
		Object[] param = new Object[] { fromtable, totable };
		addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.delsameft2t",
				"删除临时表{0}中与目的表{1}共有的数据:", param));
		addLog(logipro, delsql);
		excuteSql(conn, delsql);
		//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(logipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
						"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));

		//将临时表数据写入目的表
		l = System.currentTimeMillis();
		String insertsql = getInsertSql(dl, totable, fromtable, importfields);
		//    addLog(logipro, "将临时表" + fromtable + "数据写入目的表" + totable + ":");
		Object[] param1 = new Object[] { fromtable, totable };
		addLog(logipro, I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.wft2t",
				"将临时表{0}数据写入目的表{1}:", param1));
		addLog(logipro, insertsql);
		excuteSql(conn, insertsql);
		//addLog(logipro,"执行sql耗时："+StrFunc.formatTime(System.currentTimeMillis() - l));
		addLog(logipro,
				I18N.getString("com.esen.jdbc.data.datacopyforupdateimpl.sqlcost",
						"执行sql耗时：") + StrFunc.formatTime(System.currentTimeMillis() - l));
	}

	private static void excuteSql(Connection conn, String sql) throws SQLException {
		Statement stat = conn.createStatement();
		try {
			stat.executeUpdate(sql);
		}
		finally {
			stat.close();
		}

	}

	private static String getInsertSql(Dialect dl, String totable, String fromtable, String[] flds) {
		StringBuffer sql = new StringBuffer(64);
		sql.append("insert into ").append(totable);
		if (dl.getDataBaseInfo().isOracle()) {
			//oracle加此参数不记录日志，会很快；
			sql.append(" nologging");
		}
		sql.append(" (");
		for (int i = 0; i < flds.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append(flds[i]);
		}
		sql.append(") \n(");
		sql.append("select ");
		for (int i = 0; i < flds.length; i++) {
			if (i > 0)
				sql.append(',');
			sql.append("b.").append(flds[i]);
		}
		sql.append(" from ").append(fromtable).append(" b)");
		return sql.toString();
	}

	private static void addLog(IProgress logipro, String log) {
		if (logipro != null) {
			logipro.addLogWithTime(log);
		}

	}

	private static String getDeleteSql(Dialect dl, String fromtable, String totable, String[] fieldkeys) {
		return dl.getDeleteSql(fromtable, totable, fieldkeys, fieldkeys);
	}

}
