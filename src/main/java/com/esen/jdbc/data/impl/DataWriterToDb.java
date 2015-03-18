package com.esen.jdbc.data.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.data.DataCopyForUpdateImpl;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.data.DataWriter;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ExceptionHandler;
import com.esen.util.IProgress;
import com.esen.util.MathUtil;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;

/**
 * 从DataReader接口读取数据写入数据库；
 * 此对象持有一个数据库链接；
 * @author Administrator
 *
 */
public class DataWriterToDb implements DataWriter {

	private String logmessagepre;

	private IProgress pro;//进度

	/**
	 * 导入方式；
	 */
	protected int import_opt;

	protected Connection conn;

	protected Dialect dl;

	private DataBaseInfo dbinfo;

	private boolean batchCommit;

	protected String tablename;

	private PreparedStatement pstat;

	protected AbstractMetaData meta;

	private int cnt;
	
	/*
	 * ISSUE:BI-8147: added by liujin 2013.04.02
	 * 复制数据过程时，在事务中执行修改表定义的操作出错会导致数据丢失，将丢失的数据行数记录到日志中。
	 */
	/**
	 *  记录导入过程中丢失的数据行数
	 */
	private int cnt_fail = 0;
	
	/**
	 * 记录导入过程中已导入的数据的当前位置
	 */
	private int cnt_succ = 0;

	private int autoIncColumnIndex = -1;

	protected int skip;//忽略前几行

	private boolean haveBlobOrClob;//是否有大字段；

	private int[] columnLens;//字段长度缓存；

	/**
	 * 记录目的表是否存在；存在=true；
	 */
	private boolean desttableExist;

	/**
	 * 记录需要写入的字段，可能只写入源表的部分字段值到目的表；
	 */
	private String[] importfields;

	/**
	 * 记录指定导入字段在每行数据字段列的编号；
	 * 就是将指定的字段和数据列对应起来；
	 */
	protected int[] fieldIndex;

	/**
	 * 目的表的主键；
	 */
	private String[] destkeys;

	/**
	 * 源表的主键；
	 */
	private String[] srckeys;

	/**
	 * 是否写入全部字段；
	 */
	private boolean importAllFild;

	/**
	 * 20091026
	 * 导入前是否需要清空数据；
	 * 加这个参数，是为了解决当设置DataCopy.OPT_CLEARTABLE参数时，清空数据和写入数据不再同一个事务中，
	 * 导致如果写入出异常，却把原来表中的数据清空了的问题；
	 * 通过此参数，将清空和写入数据置于一个事务中，写入出现异常，则回滚操作；
	 * 
	 */
	protected boolean needClearTable;

	/**
	 * 界面进度刷新时间, 每更新2000条数据进行一次刷新这个在某些数据库环境下可能会比较慢
	 * 增加这一参数, 如果一段时间内更新记录没有超过2000条也自动也强制刷新
	 */
	private long refreshTime;

	public DataWriterToDb(Connection conn) {
		this.conn = conn;
		this.dl = SqlFunc.createDialect(this.conn);
		dbinfo = dl.getDataBaseInfo();
		batchCommit = isbatchCommit();
		skip = 0;
		haveBlobOrClob = false;
	}

	/**
	 * 是否需要批处理写入数据；
	 * @return
	 */
	private boolean isbatchCommit() {
		//sybase12.5也不需要批量提交，逐行提交速度快；
		//达梦数据库不支持批量提交；
		return !dbinfo.isSybaseIQ() && !dbinfo.isSybase() && !dbinfo.isDM_DBMS();
	}

	/**
	 * 某些参数可能同时起作用；
	 * 便于以后扩展使用；
	 * @param opt
	 */
	public void setImportOpt(int opt) {
		import_opt = import_opt | opt;
	}

	/**
	 * 设置进度
	 * @param pro
	 */
	public void setProgress(IProgress pro) {
		this.pro = pro;
	}

	public void setLogMessagePre(String logmessagepre) {
		this.logmessagepre = logmessagepre == null ? null : (logmessagepre + " ");
	}

	protected void addLog(String log) {
		if (pro != null) {
			/*
			 * 将addLog改成addLogWithTime 
			 * 与数据库管理其他地方的日志记录保持一致
			 */
			pro.addLogWithTime(this.logmessagepre == null ? log : this.logmessagepre + log);
		}
	}

	protected void setLastLog(String log) {
		if (pro != null) {
			pro.setLastLogWithTime(this.logmessagepre == null ? log : this.logmessagepre + log);
		}
	}

	protected void checkCancel() {
		if (pro != null) {
			pro.checkCancel();
		}
	}

	/**
	 * 获取写入的数据库表名，跟给定的表可能不一样；
	 * @return
	 */
	public String getTableName() {
		return tablename;
	}

	/**
	 * 写入数据前，创建表；
	 * @return
	 * @throws Exception 
	 */
	public void createTable(AbstractMetaData md, String tbname) throws Exception {
		if (this.tablename != null)
			return;
		checkDestTable(tbname);

		if ((import_opt & DataCopy.OPT_CREATENEWTABLE) == DataCopy.OPT_CREATENEWTABLE) {
			//创建新表；
			getDestMeta(md);
			tablename = generationTable(tbname);
		}
		else if ((import_opt & DataCopy.OPT_OVERWRITER) == DataCopy.OPT_OVERWRITER) {
			//删除已存在的表；
			if (desttableExist) {
				dropTable(tablename);
			}
			//根据源创建新表；
			getDestMeta(md);
			generationTable(tbname);
		}
		else if ((import_opt & DataCopy.OPT_CLEARTABLE) == DataCopy.OPT_CLEARTABLE) {
			getDestMeta(md);
			if (desttableExist) {
				checkMeta();
				//这里设置是否需要清空数据参数，去除直接清除数据的代码，以保证和写入数据处于同一个事务中；
				needClearTable = true;
			}
			else {
				generationTable(tbname);
			}
		}
		else if ((import_opt & DataCopy.OPT_APPEND) == DataCopy.OPT_APPEND
				|| (import_opt & DataCopy.OPT_UPDATE) == DataCopy.OPT_UPDATE) {
			initAppendAndUpdate(md, tbname);
		}
		else {
			/**
			 * 没有指定导入参数，直接追加记录，如果目的表不存在，先创建；
			 */
			getDestMeta(md);
			if (desttableExist) {
				checkMeta();
			}
			else {
				generationTable(tbname);
			}
		}
	}

	/**
	 * 返回目的表的表结构；
	 * 如有需要，此方法必须在执行createTable(...)后调用；
	 * @return
	 */
	public AbstractMetaData getDestMeta() {
		return meta;
	}

	private void initAppendAndUpdate(AbstractMetaData md, String tbname) throws Exception {
		if (!desttableExist) {
			//目的表不存在，先创建；
			getDestMeta(md);
			generationTable(tbname);
		}
		else {
			/**
			 * 表存在，比较目的表和源表的表结构，找出目的表和源表都有的字段；
			 * 并记录每个字段在读取源表数据时的位置（遍历顺序）；
			 * 检查目的表有没有主键，或者唯一约束字段；这里主键或唯一约束字段不能是自动增长字段；
			 */
			findImportFields(md);
			if (importfields == null || importfields.length == 0) {
				throw new Exception(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.samestt",
						"目的表字段和源表字段没有相同的字段，不能写入"));
			}
			/**
			 * importAllFild参数的判断，移到findImportFields(md)方法中；
			 */
			if (importAllFild) {
				//写入全部字段
				getDestMeta(md);
			}
			else {
				//写入部分字段
				getDestMeta(new TableMetaDataImport(tablename, md, importfields));
			}
		}
	}

	protected void checkDestTable(String tbname) throws Exception {
		this.tablename = tbname;
		desttableExist = isTableExist(tbname);
	}

	/**
	 * 找出目的表和源表都有的字段；
	 * 并记录每个字段在读取源表数据时的位置（遍历顺序）；
	 * 检查目的表有没有主键，或者唯一约束字段；这里主键或唯一约束字段不能是自动增长字段；
	 * @throws Exception 
	 */
	protected void findImportFields(AbstractMetaData md) throws Exception {
		TableMetaData desttmd = this.getRealMeta(tablename);//目的表结构
		List cl = new ArrayList();
		//找出共有字段；
		for (int i = 0; i < md.getColumnCount(); i++) {
			String cname = md.getColumnName(i);
			if (getFieldIndex(cname, desttmd) >= 0) {
				cl.add(cname);
			}
		}
		importfields = new String[cl.size()];
		cl.toArray(importfields);
		//获取这些字段在源表中的遍历位置；
		fieldIndex = new int[cl.size()];
		for (int i = 0; i < importfields.length; i++) {
			fieldIndex[i] = getFieldIndex(importfields[i], md);
		}
		/**
		 * BI-4857 20100531
		 * 判断是不是源记录的所有字段都可以写入目的表；
		 * 这个判断移到这里是为了兼容csv格式源的导入；
		 */
		importAllFild = importfields.length == md.getColumnCount();
		//检查目的表有没有主键，或者唯一约束字段；这里主键或唯一约束字段不能是自动增长字段；
		destkeys = analyseMetaDataKeys(desttmd);
		//分析源表主键；
		srckeys = analyseMetaDataKeys(md);
	}

	private String[] analyseMetaDataKeys(AbstractMetaData md) {
		if (md instanceof TableMetaData) {
			TableMetaData tmd = (TableMetaData) md;
			String keys[] = tmd.getPrimaryKey();
			//这里主键或唯一约束字段不能是自动增长字段;
			if (keys != null && !isAutoIncField(keys, tmd)) {
				return keys;
			}
			//没有主键；
			if (keys == null) {
				/*
				 * 在索引中找唯一约束索引；
				 * 顺序遍历所有索引，最先找到满足条件的就返回；
				 */
				TableIndexMetaData[] inds = tmd.getIndexes();
				if (inds == null)
					return null;
				for (int i = 0; i < inds.length; i++) {
					TableIndexMetaData indx = inds[i];
					if (indx.isUnique()) {
						//唯一索引
						String[] fields = indx.getColumns();
						if (!isAutoIncField(fields, tmd)) {
							//唯一约束字段不能是自动增长字段；
							return fields;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * 判断给定的字段数组，是否包含自动增长字段；
	 * @param keys
	 * @param tmd
	 * @return
	 */
	private boolean isAutoIncField(String[] keys, TableMetaData tmd) {
		for (int i = 0; i < keys.length; i++) {
			if (isAutoIncField(keys[i], tmd))
				return true;
		}
		return false;
	}

	private boolean isAutoIncField(String field, TableMetaData tmd) {
		TableColumnMetaData[] cols = tmd.getColumns();
		for (int i = 0; i < cols.length; i++) {
			TableColumnMetaData col = cols[i];
			if (field.equalsIgnoreCase(col.getName()) && col.isAutoInc()) {
				return true;
			}
		}
		return false;
	}

	private int getFieldIndex(String cname, AbstractMetaData meta2) {
		for (int i = 0; i < meta2.getColumnCount(); i++) {
			String cn = meta2.getColumnName(i);
			if (cn.equalsIgnoreCase(cname))
				return i;
		}
		return -1;
	}

	protected void initWriterData(String tbname) throws Exception {
		String insertsql = getInsertSql(tbname);
		if (pstat == null) {
			//是否批量写入，跟事物无关；设置false，以便出现异常rollback;
			conn.setAutoCommit(false);
			pstat = conn.prepareStatement(insertsql);
		}
		cnt = 0;
		refreshTime = System.currentTimeMillis();
		//初始化字段长度缓存；
		initColumnLength();
	}

	private String getInsertSql(String tbname) {
		StringBuffer sql = new StringBuffer(512);
		sql.append("insert into ").append(tbname);
		sql.append(" (");
		int k = 0;
		DbDefiner def = dl.createDbDefiner();
		for (int i = 0; i < meta.getColumnCount(); i++) {
			if (i == autoIncColumnIndex)
				continue;
			if (k > 0)
				sql.append(",");
			//写入时，数据源可能是不同的数据库，如果目标数据库是Oracle，而源数据库是Sybase，
			//当源表有字段：name，注意是小写，到Oracle会把这个关键字用引号扩起来"name"；
			//oracle建表时却将其转换成"NAME"，这里生成insert语句，会报找不到字段"name"的错误；
			sql.append(SqlFunc.getColumnName(dl, def.formatFieldName(meta.getColumnName(i), true)));
			k++;
		}
		sql.append(")values(");
		int fieldCount = autoIncColumnIndex == -1 ? meta.getColumnCount() : (meta.getColumnCount() - 1);
		for (int i = 0; i < fieldCount; i++) {
			if (i > 0)
				sql.append(",");
			sql.append("?");
		}
		sql.append(")");
		return sql.toString();
	}

	/**
	 * 清空数据
	 * @throws Exception
	 */
	protected void clearData() throws Exception {
		addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.deldata", "删除数据"));
		long starttime = System.currentTimeMillis();
		String sql = "delete from " + this.getTableName();
		Statement sm = conn.createStatement();
		try {
			sm.executeUpdate(sql);
		}
		finally {
			sm.close();
		}
		setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.delsuc", "删除数据完成,用时:{0}",
				new Object[] { formatFromTime(starttime) }));
	}

	private String formatTime(long l) {
		return StrFunc.formatTime(l);
	}

	private String formatFromTime(long l) {
		return formatTime(System.currentTimeMillis() - l);
	}

	private void dropTable(String tbname) throws Exception {
		DbDefiner dbf = dl.createDbDefiner();
		if (dbf.tableExists(conn, null, tbname))
			dbf.dropTable(conn, null, tbname);
	}

	/**
	 * 检查表的结构
	 * 这里用于导入清空数据后，插入前检查目的表和源表结构是否一致；
	 * 只需要检查字段, 不用检查索引；
	 * 字段只检查字段名是否一致，类型是否一致；
	 * 20090429
	 * 因为如果目的表存在，且和导入流中的字段结构一样，就可以导入；
	 * @param meta 
	 * @throws Exception
	 */
	private void checkMeta() throws Exception {
		TableMetaData realmeta = getRealMeta(tablename);
		this.checkFields(realmeta);

	}

	/**
	 * 检测字段信息
	 * 只检查字段名是否一致，类型是否一致；
	 * @param meta 
	 * @throws Exception
	 */
	private void checkFields(TableMetaData realmeta) throws Exception {
		int count = meta.getColumnCount();
		//允许只插入部分字段的值；
		TableColumnMetaData[] scolumns = null;
		if (meta instanceof TableMetaData) {
			scolumns = ((TableMetaData) meta).getColumns();
		}
		String name;
		TableColumnMetaData column;
		int len = realmeta.getColumnCount();
		TableColumnMetaData[] tcolumns = realmeta.getColumns();
		List realcols = new ArrayList();
		for (int i = 0; i < count; i++) {
			name = meta.getColumnName(i);
			column = null;
			for (int j = 0; j < len; j++) {
				if (tcolumns[j].getName().equalsIgnoreCase(name)) {
					column = tcolumns[j];
					break;
				}
			}
			if (column == null) {
				//        throw new RuntimeException("恢复备份时输入流中的字段信息与表的字段信息不一致,字段(" + name
				//            + ")不存在\n表名:" + this.getTableName());
				throw new RuntimeException(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.unmatch",
						"恢复备份时输入流中的字段信息与表的字段信息不一致,字段({0})不存在\n表名:{1}", new Object[] { name, this.getTableName() }));
			}
			else if (scolumns != null) {
				checkField(column, scolumns[i]);
				/**
				 * 设置cleardata参数时，检查表结构，这里设置增长字段的序号；
				 */
				if (column.isAutoInc()) {
					autoIncColumnIndex = i;
				}
			}

			/**
			 * 20090430
			 * 上次20090429改动问题，meta变量有可能不是个TableMetaData，而是AbstractMetaData；
			 * 代码没有看清楚，现在改正确；通过测测试单元；
			 */
			realcols.add(column);
		}
		/**
		 * 20090429
		 * 使用数据库表的结构做插入标准；
		 * 做这个改动是因为，如果目的表的字段属性长度小于备份流中的字段属性长度，
		 * 在插入时如果还以备份流中的长度做标准，自动扩充字段长度的代码会不起作用，报插入值长度过大的数据库异常；
		 */
		TableColumnMetaData[] rcols = new TableColumnMetaData[realcols.size()];
		realcols.toArray(rcols);
		meta = new TableMetaDataStm(null, rcols, null);
	}

	/**
	 * 比较两个字段是否相等
	 * 只检查字段名是否一致，类型是否一致；
	 * 
	 * 20100325
	 * 现在只需要字段名相同就可以了；
	 * 如果字段类型不一致，程序将根据目的表字段的类型，将值转换成相应的类型，再写入；
	 * 
	 * 这样做的目的是为了兼容有些情况，
	 * 比如BI升级时，某些表EBI_SYS21_ROLE到EBI_SYS22_ROLE
	 * 的字段DESCRIPTION 从字符转换成clob，整型转换成double等；
	 * @param src
	 * @param dest
	 */
	private void checkField(TableColumnMetaData src, TableColumnMetaData dest) {
		if (!src.getName().equalsIgnoreCase(dest.getName())
		/*|| !equalsType(src.getType(), dest.getType())*/) {
			//      throw new RuntimeException("恢复备份时输入流中的字段信息与表的字段信息不一致,\n表名:"
			//          + this.getTableName() + "\n表字段(名称:" + src.getName() + ",类型:" + src.getType()
			//          +  "),\n流字段(名称:" + dest.getName() + ",类型:"
			//          + dest.getType() + ")");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.unmatchstm",
					"恢复备份时输入流中的字段信息与表的字段信息不一致,\n表名:{0}\n表字段(名称:{1},类型:{2}),\n流字段(名称:{3},类型:{4})",
					new Object[] { this.getTableName(), src.getName(), String.valueOf(src.getType()), dest.getName(),
							String.valueOf((dest.getType())) }));

		}
	}

	static private boolean equalsType(int type1, int type2) {
		if (type1 == type2) {
			return true;
		}
		return SqlFunc.getType(type1) == SqlFunc.getType(type2);
	}

	private String generationTable(String tbname) {
		checkCancel();
		if (this.meta instanceof TableMetaData) {
			tbname = createNicetyTable(tbname);
		}
		else {
			tbname = createSimpleTable(tbname);
		}
		//this.addLog("成功创建表"+tbname);
		//this.addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.createsuc",
		//		JdbcResourceBundleFactory.class) + tbname);
		this.addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.createsuc",
				"成功创建表{0}", new Object[] { tbname }));
		return tbname;
	}

	private String createNicetyTable(String tbname) {
		DbDefiner definer = dl.createDbDefiner();
		definer.clearDefineInfo();
		TableMetaDataForWriter tmd = (TableMetaDataForWriter) getDestMeta(meta);
		defineNicetyColumn(definer, tmd);
		definePk(definer, tmd.getPrimaryKey());
		defineIndex(definer, tmd);
		try {
			/**
			 * 如果指定tbname存在，更改表名
			 * 20091021
			 * 定义表结构时，检查表的索引、主键的字段总长度是否超过数据库限制，如果超过，则自动更改字段长度；
			 */
			return definer.createTable(conn, tbname, false, true);
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
			return null;
		}
	}

	private void defineNicetyColumn(DbDefiner definer, TableMetaDataForWriter tmd) {
		TableColumnMetaData[] tcmds = tmd.getColumns();
		if (tcmds == null || tcmds.length == 0)
			return;

		for (int i = 0; i < tcmds.length; i++) {
			TableColumnMetaData column = tcmds[i];
			String columnName = column.getName();
			boolean isAutoInc = column.isAutoInc();
			if (!isAutoInc) {
				/**
				 * 20090610
				 * 创建表区分date,time,timestamp统一通过SqlFunc.getSubsectionType()来处理；
				 * 因为有的数据库比如SybaseIQ的date,time,timestamp类型的值与其他数据库不一致；
				 */
				char columnType = SqlFunc.getSubsectionType(column.getType());
				int len = column.getLen();
				/**
				 * 20090624 BI-2038
				 * 字段长度可能为0，造成建表失败；
				 * 字段长度为0的原因是：这个表可能是个视图表，且创建时用null值代替该字段，比如：
				 * create view v_t_str (str_,str2_) as select str_,null from t_str
				 * 这里str2_字段就是一个长度为0 的字段；
				 * 解决：如果为0，给个初始值；
				 * 
				 * 这里只需要判断是字符类型的才需要指定长度，
				 * 如果是数值类型，允许长度为0，表示不知道精度，定义的时候可以不指定精度，防止由于指定精度造成的数据不准确。
				 * 20120228 dw
				 */
				if (columnType == 'C' && len == 0) {
					len = 100;
				}
				int dec = column.getScale();
				boolean nullable = column.isNullable();
				boolean unique = column.isUnique() && !primaryKey(tmd, column);
				definer.defineField(columnName, columnType, len, dec, null, nullable, unique);
			}
			else {
				definer.defineAutoIncField(columnName, 1, column.getDesc());
				autoIncColumnIndex = i;
			}
		}
	}

	private boolean primaryKey(TableMetaDataForWriter tmd, TableColumnMetaData column) {
		String pk[] = tmd.getPrimaryKey();
		if (pk == null || pk.length == 0)
			return false;
		String nm = column.getName();
		for (int i = 0; i < pk.length; i++) {
			if (nm.equalsIgnoreCase(pk[i]))
				return true;
		}
		return false;
	}

	private void defineIndex(DbDefiner definer, TableMetaDataForWriter tmd) {
		TableIndexMetaData[] timds = tmd.getIndexes();
		String[] pk = tmd.getPrimaryKey();
		if (timds == null || timds.length == 0)
			return;
		List indlist = new ArrayList(timds.length);
		for (int i = 0; i < timds.length; i++) {
			TableIndexMetaData index = timds[i];
			String indexName = index.getName();
			String[] indexColumns = index.getColumns();
			if (checkIndexIsPk(indexColumns, pk))
				continue;
			boolean unique = index.isUnique();
			if (checkIndexIsUniqField(tmd, index))
				continue;
			//如果有重复的索引定义，忽略
			if (containsIndexFields(indlist, index))
				continue;
			String indexExp = getIndexExp(indexColumns);
			definer.defineIndex(indexName, indexExp, unique);
			indlist.add(index);
		}
	}

	/**
	 * 20090717
	 * 判断index是否是重复的索引定义；
	 * 做这个判断的原因是：有Mysql的数据表，允许重复的定义索引，只要索引名不同，字段集合可以相同；
	 * 这样造成从mysql备份的表，导入Oracle 时，Oracle为相同的字段不允许重复创建索引；报报ORA-01408: 此列列表已编制索引 的异常；
	 * @param indlist  需要定义索引的集合
	 * @param index
	 * @return
	 */
	private boolean containsIndexFields(List indlist, TableIndexMetaData index) {
		for (int i = 0; i < indlist.size(); i++) {
			TableIndexMetaData indexi = (TableIndexMetaData) indlist.get(i);
			String[] coli = indexi.getColumns();
			String[] col = index.getColumns();
			if (equals(col, coli)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 判断两个数组的值是不是相同的，里面值的位置可以不一样；
	 * @param col
	 * @param coli
	 * @return
	 */
	private boolean equals(String[] col, String[] coli) {
		if (col.length != coli.length)
			return false;
		for (int i = 0; i < col.length; i++) {
			String fieldi = col[i];
			if (!contains(fieldi, coli)) {
				return false;
			}
		}

		return true;
	}

	private boolean contains(String fieldi, String[] coli) {
		for (int j = 0; j < coli.length; j++) {
			String fieldj = coli[j];
			if (fieldi.equalsIgnoreCase(fieldj)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 如果索引是对单个字段，该字段却被定义成unique字段，则不需要在定义索引；
	 * 20090717 BI-709 
	 * 这里对单个字段的索引，不用管是不是唯一，即使不是唯一索引，但是定义了unique字段，就不用再定义索引了；
	 * 否者Oracle 报ORA-01408: 此列列表已编制索引 的异常；
	 * 所以将是否是唯一索引的判断去掉；
	 * @param tmd
	 * @param index
	 * @return
	 */
	private boolean checkIndexIsUniqField(TableMetaDataForWriter tmd, TableIndexMetaData index) {
		//if(index.isUnique()){
		String[] indexColumns = index.getColumns();
		if (indexColumns.length == 1) {
			String field = indexColumns[0];
			TableColumnMetaData[] cols = tmd.getColumns();
			for (int i = 0; i < cols.length; i++) {
				String col = cols[i].getLabel();
				if (col == null)
					col = cols[i].getName();
				if (col.equalsIgnoreCase(field) && cols[i].isUnique())
					return true;
			}
		}
		// }
		return false;
	}

	private boolean checkIndexIsPk(String[] indexColumns, String[] pk) {
		if (pk == null || indexColumns.length != pk.length)
			return false;
		for (int i = 0; i < indexColumns.length; i++) {
			if (!isColumnEqual(indexColumns[i], pk))
				return false;
		}
		return true;
	}

	private boolean isColumnEqual(String column, String[] pk) {
		for (int j = 0; j < pk.length; j++) {
			if (column.equalsIgnoreCase(pk[j]))
				return true;
		}
		return false;
	}

	private String getIndexExp(String[] indexColumns) {
		if (indexColumns == null || indexColumns.length == 0)
			return null;
		StringBuffer sb = new StringBuffer(indexColumns.length * 20).append("(");
		sb.append(indexColumns[0]);
		for (int i = 1; i < indexColumns.length; i++) {
			sb.append(',');
			sb.append(indexColumns[i]);
		}
		sb.append(')');
		return sb.toString();
	}

	private void definePk(DbDefiner definer, String[] pk) {
		if (pk == null || pk.length == 0)
			return;
		StringBuffer sb = new StringBuffer(pk.length * 20).append(pk[0]);
		for (int i = 1; i < pk.length; i++) {
			sb.append(',');
			sb.append(pk[i]);
		}
		definer.definePrimaryKey(sb.toString());
	}

	private String createSimpleTable(String tbname) {
		DbDefiner definer = dl.createDbDefiner();
		definer.clearDefineInfo();
		AbstractMetaData tmd = getDestMeta(meta);
		int columnCount = tmd.getColumnCount();
		for (int i = 0; i < columnCount; i++) {
			/**
			 * BI-5553
			 * 定义字段名的时候，如果是关键字，也不用转换；
			 * 在生成sql的时候才需要转；
			 * 如果定义的时候就转，会造成根据字段名在定义字段列表中找字段对象，可能找不到，
			 * 有些地方就会出现空指针异常；
			 */
			String columnName = tmd.getColumnName(i);
			char columnType = SqlFunc.getType(tmd.getColumnType(i));
			int len = tmd.getColumnLength(i);
			int dec = tmd.getColumnScale(i);
			if (columnType == 'D') {
				switch (tmd.getColumnType(i)) {
					case Types.DATE: {
						definer.defineDateField(columnName, null, true, false);
						break;
					}
					case Types.TIME: {
						definer.defineTimeField(columnName, null, true, false);
						break;
					}
					case Types.TIMESTAMP: {
						definer.defineTimeStampField(columnName, null, true, false);
						break;
					}
				}
			}
			else
				definer.defineField(columnName, columnType, len, dec, null, true, false);
		}
		try {
			/**
			 * 20091021
			 * 定义表结构时，检查表的索引、主键的字段总长度是否超过数据库限制，如果超过，则自动更改字段长度；
			 */
			return definer.createTable(conn, tbname, false, true);
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
			return null;
		}
	}

	private boolean isTableExist(String tablename) throws Exception {
		/**
		 * 20090707
		 * 判断目的表是否存在，这里需要判断是否是数据库的对象，包含视图；
		 * 因为后面根据这个判断创建表，如果目的表名是个视图，创建就会包重名异常；
		 */
		return dl.createDbDefiner().tableOrViewExists(conn, tablename);
	}

	private TableMetaData getRealMeta(String tbname) throws Exception {
		return dl.createDbMetaData().getTableMetaData(tbname);
	}

	/**
	 * 将源表结构转换成目的数据库支持的表结构
	 * @param md
	 * @return
	 */
	protected AbstractMetaData getDestMeta(AbstractMetaData md) {
		if (meta == null) {
			if (md instanceof TableMetaData) {
				TableMetaData tmd = (TableMetaData) md;
				meta = new TableMetaDataForWriter(tablename, tmd, dl);
			}
			else
				meta = new AbstractMetaDataForWriter(md, dl);
		}
		return meta;
	}

	/**
	 * 写数据
	 * @return
	 * @throws Exception 
	 */
	public void writeData(DataReader rd) throws Exception {
		// addLog("开始写入"+tablename+"数据；");
		addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.startwritedata",
				"开始写入{0}数据；", new Object[] { tablename }));
		long l = System.currentTimeMillis();
		if (import_opt == DataCopy.OPT_APPEND || import_opt == DataCopy.OPT_UPDATE) {
			if (destkeys == null) {//目的表没有主键，直接写入；
				writeDataNormal(rd, tablename);
			}
			else {//目的表有主键；

				//检查源表的写入字段是否包含目的表的主键，如果不包含出异常；
				checkPrimaryKey();
				//创建临时表；
				String tmpname = this.generationTable(tablename);
				try {
					//将数据写入临时表；
					//addLog("将数据写入临时表："+tmpname);
					addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.writedata2temptable",
							"将数据写入临时表") + tmpname);
					writeDataNormal(rd, tmpname);
					//检查源表数据，是否符合写入；
					checkData(tmpname);
					if (import_opt == DataCopy.OPT_APPEND) {
						//写入目的表不存在的记录
						appendData(tmpname);
					}
					else {
						//增量更新；
						updateData(tmpname);
					}
				}
				finally {
					//删除临时表
					// addLog("删除临时表："+tmpname);
					addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.deltmp",
							"删除临时表：") + tmpname);
					dropTable(tmpname);
				}
			}
		}
		else {
			writeDataNormal(rd, tablename);
		}
		//addLog("写入完成，共计耗时："+StrFunc.formatTime(System.currentTimeMillis()-l));
		addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.writesucmsg", "写入完成，共计耗时：")
				+ StrFunc.formatTime(System.currentTimeMillis() - l));
	}

	/**
	 * 检查源表数据，是否符合写入；
	 * 这里主要检查源表如果没有主键，是否是按 destkeys 唯一分组的，如果不是出异常；
	 * @param tmpname
	 * @throws SQLException 
	 */
	private void checkData(String tmpname) throws Exception {
		if (srckeys != null) {
			//源表有主键，这里不用检查，前面判断过了；
			return;
		}
		StringBuffer checksql = new StringBuffer(64);
		checksql.append("select ");
		for (int i = 0; i < destkeys.length; i++) {
			if (i > 0)
				checksql.append(',');
			checksql.append(destkeys[i]);
		}
		checksql.append(" from ").append(tmpname);
		checksql.append(" group by ");
		for (int i = 0; i < destkeys.length; i++) {
			if (i > 0)
				checksql.append(',');
			checksql.append(destkeys[i]);
		}
		checksql.append(" having count(*)>1");
		//用一个sql查询检查源表的数据是否是按destkeys唯一分组的；
		StringBuffer info = new StringBuffer(32);
		Statement stat = conn.createStatement();
		try {
			ResultSet rs = stat.executeQuery(checksql.toString());
			int k = 0;
			while (rs.next()) {
				if (k >= 100)//日志有限，只记录前100条；
					break;
				for (int i = 0; i < destkeys.length; i++) {
					if (i > 0)
						info.append(",");
					info.append(rs.getString(i + 1));
				}
				info.append("\n");
				k++;
			}
		}
		finally {
			stat.close();
		}
		if (info.length() > 0) {
			//不是按destkeys唯一分组，有相同的记录，出异常；
			// throw new Exception("导入的数据不是按目的表："+tablename+"的主键分组的，相同的前100条记录为：\n"+info.toString());
			throw new Exception(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.importerr",
					"导入的数据不是按目的表：{0}的主键分组的，相同的前100条记录为：\n{1}", new Object[] { tablename, info.toString() }));
		}
	}

	private void checkPrimaryKey() throws Exception {
		//检查源表的写入字段是否包含目的表的主键，如果不包含出异常；
		for (int i = 0; i < destkeys.length; i++) {
			if (!checkPrimaryKey(destkeys[i])) {
				//throw new Exception("需要写入的字段不包含主键；");
				throw new Exception(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.noprikey",
						"需要写入的字段不包含主键；"));
			}
		}
		//如果源表也有主键，应该和目的表主键一致，否则出异常；
		if (srckeys != null && srckeys.length > 0) {
			if (srckeys.length != destkeys.length)
				//throw new Exception("目的表主键和源表主键不一致；");
				throw new Exception(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.unmatchs2",
						"目的表主键和源表主键不一致；"));
			for (int i = 0; i < destkeys.length; i++) {
				if (!checkSrcKeys(destkeys[i])) {
					//throw new Exception("目的表主键和源表主键不一致；");
					throw new Exception(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.unmatchs2",
							"目的表主键和源表主键不一致；"));

				}
			}
		}
	}

	private boolean checkSrcKeys(String key) {
		for (int i = 0; i < srckeys.length; i++) {
			if (key.equalsIgnoreCase(srckeys[i]))
				return true;
		}
		return false;
	}

	private boolean checkPrimaryKey(String keyfield) {
		for (int i = 0; i < meta.getColumnCount(); i++) {
			if (meta.getColumnName(i).equalsIgnoreCase(keyfield)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 将临时表的数据增量写入目的表；
	 * @param tmpname
	 * @throws SQLException 
	 */
	private void updateData(String tmpname) throws Exception {
		if (importAllFild) {
			DataCopyForUpdateImpl.updateAllFieldData(dl, conn, tmpname, tablename, destkeys, importfields, pro);
		}
		else {
			DataCopyForUpdateImpl.updateSomeFieldData(dl, conn, tmpname, tablename, destkeys, importfields, pro);
		}
	}

	/**
	 * 将临时表的在目的表中不存在的记录写入目的表；
	 * 不用考虑写入的是部分字段还是全部字段；
	 * 先删除临时表中与目的表共有的数据，再将临时表数据写入目的表；
	 * @param tmpname
	 * @throws SQLException 
	 */
	private void appendData(String tmpname) throws Exception {
		DataCopyForUpdateImpl.appendData(dl, conn, tmpname, tablename, destkeys, importfields, pro);
	}

	/**
	 * 将数据写入指定的表
	 * @param rd
	 * @param tbname
	 * @param needlog
	 *        是否记录日志
	 * @throws Exception
	 */
	public void writeDataNormal(DataReader rd, String tbname) throws Exception {
		try {
			checkBlobOrClob(rd.getMeta());
			if (needClearTable) {
				//在同一事务中，先清空数据；
				clearData();
			}
			checkCancel();
			long n = 0;
			long l = System.currentTimeMillis();
			initWriterData(tbname);
			checkStartProcess(rd.getRecordCount());
			while (rd.next()) {
				n++;
				//System.out.println(n);
				if (n <= skip)
					continue;//忽略前几行
				/**
				 * 20090710
				 * 每写入100行检查是否取消写入；
				 * 以前的代码每行都判断，会占用部分cpu资源；
				 */
				if (cnt % 100 == 0) {
					checkCancel();
				}
				int col = 1;
				for (int i = 0; i < meta.getColumnCount(); i++) {
					if (autoIncColumnIndex == i) {
						rd.getValue(i);
						continue;
					}
					//写入的字段有可能只是源表的一部分；
					Object obj = rd.getValue(fieldIndex == null || fieldIndex.length == 0 ? i : fieldIndex[i]);
					checkFieldLength(i, obj);
					setObject(col++, obj);
				}
				/**
				 * rd.getRecordCount()有时=-1，比如csv格式，不读完不知道有多少行；
				 */
				if ((cnt % 2000 == 0 || (System.currentTimeMillis() - refreshTime) > 30000) && rd.getRecordCount() > 0) {
					// setLastLog("写入完成" + StrFunc.double2str(cnt * 1.0 / rd.getRecordCount() * 100, 0, 2, false) + "%;");
					setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.writing",
							"写入完成")
							+ StrFunc.double2str(cnt * 1.0 / rd.getRecordCount() * 100, 0, 2, false) + "%;");
					refreshTime = System.currentTimeMillis();
				}
				append();
			}
			/**
			 * 20090721 去除写入数据时，如果碰到异常不处理，继续写入的代码；
			 * 原因是：如果碰到异常，每行都是一样的，本来没写入的最后报导入100％的信息；
			 */
			commit();
			/**
			 * 20090507 BI-1640
			 * 这里已经导入完毕，将最后一次记录的日志记录为完成100%；
			 * 上面每2000写次导入进度百分比，可能最后没有2000条记录，还显示前一次的进度，不符合实际情况；
			 */
			if (pro != null) {
				/**
				 * 这里有两个变量，n,cnt都是增量计数，但是有区别；
				 * n是每遍历一条记录+1；
				 * cnt是每写入条记录+1；
				 * 如果skip=0 , n和cnt是同步的；skip>0,遍历完后，n=cnt+skip
				 * 由于前面设置总进度条数是rd.getRecordCount()，这里遍历完毕，设置n到进度条，保证最后是100%；
				 */
				pro.setPosition((int) n);
				setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertodb.written", "写入完成100%;"));
			}
			//addLog("写入" + tbname + "完成，共条" + cnt + "记录，耗时：" + StrFunc.formatTime(System.currentTimeMillis() - l));
			addLog(I18N.getString(
					"com.esen.jdbc.data.impl.datawritertodb.writesucmsg2",
					"写入{0}完成，共{1}条记录，耗时：{2}",
					new Object[] { tbname, String.valueOf(cnt),
							String.valueOf(StrFunc.formatTime(System.currentTimeMillis() - l)) }));
			
			/*
			 * ISSUE:BI-8147: added by liujin 2013.04.02
			 * 复制数据过程时，在事务中执行修改表定义的操作出错会导致数据丢失，将丢失的数据行数记录到日志中。
			 * 日志信息为：其中共 xx 行数据丢失
			 */
			if (cnt_fail > 0) {
				addLog(I18N.getString(
						"com.esen.jdbc.data.impl.datawritertodb.writefailmsg",
						"其中共{0}行数据丢失；",
						new Object[] {String.valueOf(cnt_fail)}));
			}
		}
		catch (Exception e) {
			//写入出现异常，回滚操作；
			conn.rollback();
			throw e;
		}
		finally {
			close();
		}
	}

	/**
	 * 记录写入总行数到进度监视器pro;
	 * @param recordCount
	 */
	private void checkStartProcess(int recordCount) {
		if (pro != null) {
			pro.setProgress(0, recordCount, 1);
		}

	}

	/**
	 * 20090903 初始化字段长度；
	 * 原来的代码获取.db文件中的表结构，可能在创建新表时，字段长度需要调整，比如超过了索引最大长度等；
	 * 改为读取写入表的表结构；
	 * @throws Exception
	 */
	private void initColumnLength() throws Exception {
		TableMetaData newmeta = dl.createDbMetaData(conn).getTableMetaData(getTableName());
		if (columnLens == null)
			columnLens = new int[newmeta.getColumnCount()];
		for (int i = 0; i < newmeta.getColumnCount(); i++) {
			columnLens[i] = newmeta.getColumnLength(i);
		}
	}

	private void checkBlobOrClob(AbstractMetaData meta2) {
		//读取csv格式没有表结构；
		if (batchCommit && meta2 != null) {
			for (int i = 0; i < meta2.getColumnCount(); i++) {
				int t = meta2.getColumnType(i);
				char tc = SqlFunc.getType(t);
				if (tc == DbDefiner.FIELD_TYPE_BINARY || tc == DbDefiner.FIELD_TYPE_CLOB) {
					haveBlobOrClob = true;
					break;
				}
			}
			if (haveBlobOrClob)
				batchCommit = false;
		}
	}

	/**
	 * 20090407
	 * 如果要写入的值的长度是否大于表定义的字段长度，则增大该字段的长度；
	 * 原来的代码只对字符类型字段有效；
	 * 现在改过之后对字符和数值都有效；
	 * 做这样的改动是因为*.dbf格式文件导入时，对与数值一般记为(8,2)，而实际值往往超过此限制，也需要修改；
	 * @param i
	 * @param obj
	 */
	protected void checkFieldLength(int i, Object obj) {
		/**
		 * SybaseIQ也不能在写入时修改结构；
		 */
		if (dbinfo.isDM_DBMS() || dbinfo.isSybaseIQ() || dbinfo.isGBase()) {
			// 达梦数据库不支持在写入时，修改表对象；
			// GBASE数据库在写入时修改表对象，会报异常
			return;
		}
		/**
		 * 20100118
		 * 如果设置了“导入前清空数据”，则不给字段扩容，如果写入有异常，直接中断导入，抛出异常，并回滚事务；
		 * 做这个控制原因是：
		 * 1）修改字段长度是包含隐性提交事务的，造成如果写入过程中修改了字段长度，且导入设置了写入前清空参数，
		 *    则会隐性的提前提交事务，在后面的写入过程中如果出现写入的异常，比如数值字段，写入非数值字符串，导致写入中断，
		 *    由于前面修改字段长度提交了事务，事务无法回滚到清空数据之前；
		 *    所以出现写入数据出了异常，原来的数据却被删除了，这是不被允许的；
		 * 20100126 
		 * 晕，逻辑弄反了，测试用例中，测试前没有删除使用的测试表，造成测试通过...
		 */
		if ((import_opt & DataCopy.OPT_CLEARTABLE) == DataCopy.OPT_CLEARTABLE) {
			return;
		}
		if (obj == null)
			return;
		char coltype = SqlFunc.getType(meta.getColumnType(i));
		if (coltype == 'C') {
			checkFieldLength_char(i, obj, coltype);
		}
		else if (coltype == 'N' || coltype == 'I') {
			checkFieldLength_number(i, obj, coltype);
		}
	}

	private void checkFieldLength_char(int i, Object obj, char coltype) {
		/**
		 * 20090428
		 * 对于中文字符，数据库在插入时取的是字节长度；
		 * 20090810
		 * 根据数据库中一个中文字符的长度，来计算整个字符串的长度；
		 */
		int len = StrFunc.strByteLength(obj.toString(), dbinfo.getNCharLength());
		if (len >= columnLens[i]) {
			//如果值的长度超过，表定义的长度，则扩大字段的长度；
			//之所以相等也改，是因为如果一个字段长度为5，有个值是两个半汉字，getBytes()=5,但是插入时oracle报长度为6超过字段长度5的异常
			len = len + (len + 2) / 3;
			ensureFieldLength(i, coltype, len);
		}
	}

	private void checkFieldLength_number(int i, Object obj, char coltype) {
		/**
		 * 对于数值类型，如果其长度为0，表示不知道其精度，这种情况这里不需要修改其精度。
		 * 修改了反而会丢失数据精度。
		 * 20120228 dw
		 */
		if (meta.getColumnLength(i) == 0) {
			return;
		}
		/**
		 * 20090421
		 * 求字符串或者数值的长度；
		 * 如果数值是科学计数法表示的，需要将其转换成非科学计数法的表示；
		 * 比如：3.3E7   转换成 33000000
		 * 这里需要求得数值的整数部分的长度；
		 **/
		double d = StrFunc.parseDouble(obj, Double.NaN);
		int len = getDoubleIntLength(d);
		if (compareNumberFieldLength(len, i)) {
			len = len + (len + 2) / 3;
			//由于len是整数部分长度，这里还要加上字段属性的小数位数；
			len = len + meta.getColumnScale(i);
			ensureFieldLength(i, coltype, len);
		}
	}

	/**
	 * 求得数值的整数部分的长度；
	 * 如果是NaN或者无穷大，返回-1；
	 * 算法: 先求绝对值，再求10的对数，然后取整（去除小数部分），最后+1；
	 * @param d
	 * @return
	 */
	protected static int getDoubleIntLength(double d) {
		if (Double.isNaN(d))
			return -1;
		if (d == Double.NEGATIVE_INFINITY)
			return -1;
		if (d == Double.POSITIVE_INFINITY)
			return -1;
		if (d == 0)
			return 1;
		return (int) Math.floor(MathUtil.log10(Math.abs(d))) + 1;
	}

	/**
	 * 20090421
	 * 数据库中数值型字段的长度是包含小数位数的，这样导致对长度的判断出现问题：
	 * 比如：字段长度是：(8,2) ,而数值是一个7位数的整数，7<8,但是插入时报错：超过定义的精度；
	 * 这里该为将字段长长度减去小数位数后再和数值长度比较；
	 * @param len
	 * @param i
	 * @param coltype
	 * @return
	 */
	private boolean compareNumberFieldLength(int len, int i) {
		int s = meta.getColumnScale(i);
		if (s > 0) {
			return len >= columnLens[i] - s;
		}
		return len >= columnLens[i];
	}

	private void ensureFieldLength(int i, char coltype, int len) {
		String cn = meta.getColumnName(i);
		int scale = meta.getColumnScale(i);
		try {
			/**
			 * 20090227
			 * 这里如果修改不成功，记录其日志，不影响插入数据；
			 * 20090721 使用新的方法只修改原字段的长度或者精度；
			 */
			dl.createDbDefiner().modifyColumn(conn, tablename, cn, coltype, len, scale);

			/*
			 * ISSUE:BI-8147: added by liujin 2013.04.02
			 * 复制数据过程时，在事务中执行修改表定义的操作出错会导致数据丢失，将丢失的数据行数记录到日志中。
			 * 修改表定义成功以后，会提交事务，记录当前已成功提交的数据的位置
			 */
			if (batchCommit) {
				cnt_succ = (cnt / DataCopy.BATCHCNT) * DataCopy.BATCHCNT;
			}
		}
		catch (Exception e) {
			addLog(e.getMessage());

			/*
			 * ISSUE:BI-8147: added by liujin 2013.04.02
			 * 复制数据过程时，在事务中执行修改表定义的操作出错会导致数据丢失，将丢失的数据行数记录到日志中。
			 * 修改表定义失败以后，当前事务中未提交的数据全部丢失。
			 */
			if (batchCommit) {
				cnt_fail = cnt_fail + ((cnt / DataCopy.BATCHCNT) * DataCopy.BATCHCNT - cnt_succ);
			}
		}
		/**
		 * 对于表名或者select * from tablename 为源的datacopy, 修改结构后要对内存中的表结构对象做相应的改动；要不然每次都要字段修改长度；
		 */
		columnLens[i] = len;
	}

	/**
	 * 根据表结构写一行数据
	 * @param i
	 * @param v
	 * @throws SQLException 
	 */
	public void setObject(int i, Object v) throws SQLException {
		/**
		 * 优化了优化了BlobFileCacher的实现，这里不需要特殊处理；
		 */
		pstat.setObject(i, v);
	}

	/**
	 * 将一行数据写入batch
	 * 是否每行提交，设置多少行提交，在这里处理
	 * @throws SQLException 
	 */
	public void append() throws SQLException {
		cnt++;
		if (pro != null) {
			/**
			 * 记录当前写入多少行数据；
			 */
			pro.setPosition(cnt);
		}
		if (!batchCommit) {
			//如果有大字段 batchCommit=false，必须每行提交；
			pstat.executeUpdate();
		}
		else {
			pstat.addBatch();
			if (cnt % DataCopy.BATCHCNT == 0)
				pstat.executeBatch();
		}
		
		/*
		 * ISSUE:BI-8594: add by liujin 2013.07.10 
		 * Teradata 在一个事务中插入的数据行数有限制。
		 * 修改为指定行数做一次 commit()。
		 * 
		 * 存在一个问题：出现异常以后，事务回滚，会有脏数据
		 */
		if (dbinfo.getMaxRowsInTrans() != -1) {
			// add by jzp 如果为-1表示事物 提交行数没有限制
			if (cnt % (dbinfo.getMaxRowsInTrans()) == 0) {
				conn.commit();
			}
		}
	}

	/**
	 * 提交最后所有的数据行；
	 * @throws SQLException 
	 */
	public void commit() throws SQLException {
		if (batchCommit) {
			if (cnt % DataCopy.BATCHCNT != 0)
				pstat.executeBatch();
		}
		conn.commit();
		conn.setAutoCommit(true);//提交完毕还原
	}

	/**
	 * 关闭类中的PreparedStatement
	 * @throws SQLException
	 */
	public void close() throws SQLException {
		if (pstat != null)
			pstat.close();
	}
}
