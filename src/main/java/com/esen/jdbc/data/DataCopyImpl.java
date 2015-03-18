package com.esen.jdbc.data;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;
import java.sql.Statement;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.DataCopyForUpdate;
import com.esen.jdbc.DefaultConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.data.impl.DataReaderFromCSV;
import com.esen.jdbc.data.impl.DataReaderFromDb;
import com.esen.jdbc.data.impl.DataReaderFromDbf;
import com.esen.jdbc.data.impl.DataReaderFromStmFactory;
import com.esen.jdbc.data.impl.DataWriteToCSV;
import com.esen.jdbc.data.impl.DataWriterToDb;
import com.esen.jdbc.data.impl.DataWriterToDbFromCSV;
import com.esen.jdbc.data.impl.DataWriterToStmNew;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ExceptionHandler;
import com.esen.util.IProgress;
import com.esen.util.i18n.I18N;

public class DataCopyImpl extends DataCopy {
  private IProgress ipro;

  public String createTableAsSelect(String srcpool, String srcsql,
      String destpool, String desttable) {
    return selectInto(srcpool, srcsql, destpool, desttable, 0, false);
  }
  public String createTableAsSelect(ConnectionFactory srcpool, String srcsql,
		  ConnectionFactory destpool, String desttable) {
	  return  selectInto(srcpool, srcsql, destpool, desttable, 0, false);
  }

  public void exportData(String srcpool, String srcsql, OutputStream out)
      throws Exception {
    if (srcsql == null || srcsql.length() == 0 || out == null)
      return;
    ConnectionFactory cf = DefaultConnectionFactory.get(srcpool, true);
    Connection conn = cf.getConnection();
    try {
      exportData(conn, srcsql, out);
    }
    finally {
      conn.close();
    }
    
  }

  public void exportData(Connection conn, String srcsql, OutputStream out) throws Exception {
    //addLog("开始备份表：" + srcsql);
	  addLog(I18N.getString("com.esen.jdbc.data.datacopyimpl.startbackupt", "开始备份表") + srcsql);
    DataReaderFromDb drfd = new DataReaderFromDb(conn, srcsql);
    DataWriterToStmNew dwts = new DataWriterToStmNew(out, ipro);//使用新的存储格式
    try {
      dwts.writeData(drfd);
    }
    finally {
      drfd.close();
    }//addLog("成功备份表：" + srcsql);
    addLog(I18N.getString("com.esen.jdbc.data.datacopyimpl.endbackupt", "成功备份表：") + srcsql);
  }

  public String importData(InputStream in, String destpool, String tbname, int option) throws Exception {
    if (in == null || tbname == null || tbname.length() == 0)
      return null;
    ConnectionFactory cf = DefaultConnectionFactory.get(destpool, true);
    Connection conn = cf.getConnection();
    try {
      return importData(in, conn, tbname, option);
    }
    finally {
      conn.close();
    }
  }

  public String importData(InputStream in, Connection conn, String tbname, int option) throws Exception {
    DataReader drfs = DataReaderFromStmFactory.getInstance().createDataReader(in);
    return importData(conn, tbname, option, drfs);
  }

  private String importData(Connection conn, String tbname, int option, DataReader drfs) throws Exception {
    AbstractMetaData amd = drfs.getMeta();
    DataWriterToDb dw = new DataWriterToDb(conn);
    if (ipro != null)
      dw.setProgress(ipro);
    setOption(dw, option);
    dw.createTable(amd, tbname);
    try {
      dw.writeData(drfs);
    }
    finally {
      drfs.close();
    }
    return dw.getTableName();

  }

  public String importDataFromDbf(String file, String destpool, String tbname,
      int option) throws Exception {
    if (file == null || tbname == null || tbname.length() == 0)
      return null;
    ConnectionFactory cf = DefaultConnectionFactory.get(destpool, true);
    Connection conn = cf.getConnection();
    try {
      return importDataFromDbf(file, conn, tbname, option);
    }finally {
      conn.close();
    }
  }

  public String importDataFromDbf(String file, Connection conn, String tbname, int option) throws Exception {
    DataReader drfs = new DataReaderFromDbf(file);
    return importData(conn, tbname, option, drfs);
  }

  public String selectInto(String srcpool, String srcsql, String destpool,
      String desttable, int option) {
    return selectInto(srcpool, srcsql, destpool, desttable, option, true);
  }
  public String selectInto(ConnectionFactory srccf, String srcsql, ConnectionFactory destcf,
      String desttable, int option) {
    return selectInto(srccf, srcsql, destcf, desttable, option, true);
  }

  private String selectInto(String srcpool, String srcsql, String destpool,
      String desttable, int option, boolean needCopyData) {
    if (srcsql == null || srcsql.length() == 0 || desttable == null
        || desttable.length() == 0)
      return null;
    ConnectionFactory srccf = DefaultConnectionFactory.get(srcpool, true);
    ConnectionFactory destcf = DefaultConnectionFactory.get(destpool, true);
    return selectInto(srccf,srcsql,destcf,desttable,option,needCopyData);
  }
  
  protected String selectInto(ConnectionFactory srccf, String srcsql, ConnectionFactory destcf,
      String desttable, int option, boolean needCopyData) {
	  if(needCopyData&&(option==DataCopy.OPT_APPEND||option==DataCopy.OPT_UPDATE)){
		  if(!tableExists(destcf,desttable)){
			  createTableAsSelect(srccf, srcsql, destcf, desttable);
		  }
		  DataCopyForUpdate dataUpdata = DataCopyForUpdate.createInstance();
		  dataUpdata.setSourceDataPool(srccf);
		  dataUpdata.setTargetDataPool(destcf);
		  dataUpdata.addSourceSql(srcsql);
		  dataUpdata.setTargetTable(desttable);
		  dataUpdata.isOnlyInsertNewRecord(option==DataCopy.OPT_APPEND);
		  try {
			dataUpdata.executeUpdate();
		  }
		  catch (Exception ex) {
			  ExceptionHandler.rethrowRuntimeException(ex);
		  }
		  return desttable;
	  }
    if (srccf.compareDataBaseTo(destcf))
      return copyDataSameSource(srccf, srcsql, desttable, option, needCopyData);
    return copyTableDifferentSource(srccf, srcsql, destcf, desttable, option,
        needCopyData);
  }
  
	private boolean tableExists(ConnectionFactory destcf, String desttable) {
		try {
			Connection conn = destcf.getConnection();
			try {
				return destcf.getDbDefiner().tableExists(conn, null, desttable);
			}
			finally {
				conn.close();
			}
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
		}
		return false;
	}
/**
   * 接受两个连接，实现从源连接copy数据到目的连接；
   * @param srccon
   * @param srcsql
   * @param destcon
   * @param desttable
   * @param option
   * @param needCopyData
   * @return
   */
	private String selectInto(Connection srccon, String srcsql, Connection destcon, String desttable, int option,
			boolean needCopyData) {
		if (srcsql == null || srcsql.length() == 0 || desttable == null || desttable.length() == 0)
			return null;
		if (needCopyData && (option == DataCopy.OPT_APPEND || option == DataCopy.OPT_UPDATE)) {
			if(!tableExists(destcon,desttable)){
				createTableAsSelect(srccon, srcsql, destcon, desttable);
			}
			DataCopyForUpdate dataUpdata = DataCopyForUpdate.createInstance();
			dataUpdata.setSourceConnection(srccon);
			dataUpdata.setTargetConnection(destcon);
			dataUpdata.addSourceSql(srcsql);
			dataUpdata.setTargetTable(desttable);
			dataUpdata.isOnlyInsertNewRecord(option == DataCopy.OPT_APPEND);
			try {
				dataUpdata.executeUpdate();
			}
			catch (Exception ex) {
				ExceptionHandler.rethrowRuntimeException(ex);
			}
			return desttable;
		}
		try {
			if (SqlFunc.compareConnection(srccon, destcon))
				return copyDataSameSource(srccon, srcsql, desttable, option, needCopyData);
			return copyTableDifferentSource(srccon, srcsql, destcon, desttable, option, needCopyData);
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
			return null;
		}
	}

	private boolean tableExists(Connection destcon, String desttable) {
		Dialect dl = SqlFunc.createDialect(destcon);
		try {
			return dl.createDbDefiner().tableExists(destcon, null, desttable);
		}
		catch (Exception ex) {
			ExceptionHandler.rethrowRuntimeException(ex);
		}
		return false;
	}
	
private String copyDataSameSource(ConnectionFactory cf, String srcsql, String desttable, int option,
      boolean needCopyData) {
    try {
      Connection conn = cf.getConnection();
      try {
        return copyDataSameSource(conn, srcsql, desttable, option, needCopyData);
      }
      finally {
        conn.close();
      }
    }
    catch (Exception ex) {
      ExceptionHandler.rethrowRuntimeException(ex);
      return null;
    }
  }
  
  private String copyDataSameSource(Connection srccon, String srcsql, String desttable, int option, boolean needCopyData)
      throws Exception {
    DataReaderFromDb drfd = new DataReaderFromDb(srccon, srcsql);
    AbstractMetaData amd = null;
    try {
      amd = drfd.getMeta();
    }
    finally {
      drfd.close();
    }
    DataWriterToDb dw = new DataWriterToDb(srccon);
    if (ipro != null)
      dw.setProgress(ipro);
    setOption(dw, option);
    dw.createTable(amd, desttable);
    String tname = dw.getTableName();
    if (needCopyData){
      insertTable(srccon, srcsql, tname, amd);
    }
    return tname;

  }

  private void insertTable(Connection conn,
      String srcsql, String desttable, AbstractMetaData amd) throws Exception {
    Statement stat = null;
    Dialect srcdl = SqlFunc.createDialect(conn);
    try {
      stat = conn.createStatement();
      String createsql = getInsertSql(srcdl, srcsql, amd, desttable);
      stat.execute(createsql);
    }
    finally {
      if (stat != null)
        stat.close();
      /**
       * conn作为参数，这里不能被关闭，它由外面的调用方法负责；
       */
    }
  }

  private String getInsertSql(Dialect dl, String srcsql, AbstractMetaData amd,
      String desttable) {
    boolean isTable = SqlFunc.isValidSymbol(srcsql);
    /**
     * 20090801 BIDEV-758
     * 对形如：select * from tbname 进行结果集复制到自身连接池时，如果tbname有自增长字段，使用insert into 会报错：
     * insert into aaa nologging (USERID,USERNAME,"PASSWORD",DEPARMENT,EMAIL,FAX,TEL1,TEL2,TITLE,ENABLED,OPTION_,MEMO,LASTCHANGETIME) select * from EBI_SYS21_USER
     * 出现异常:ORA-00913: 值过多 
     * 原因是  EBI_SYS21_USER 有个自增长字段index_;
     * 解决办法：
     * 这里把形如select * from tbname的查询结果集，解析里面的表名，当作表来处理，相当于表的复制，这样生成的sql：
     * insert into aaa nologging (USERID,USERNAME,"PASSWORD",DEPARMENT,EMAIL,FAX,TEL1,TEL2,TITLE,ENABLED,OPTION_,MEMO,LASTCHANGETIME) select USERID,USERNAME,"PASSWORD",DEPARMENT,EMAIL,FAX,TEL1,TEL2,TITLE,ENABLED,OPTION_,MEMO,LASTCHANGETIME from EBI_SYS21_USER
     * 就不会有这个问题了；
     * 
     */
    if (!isTable) {
      String tbn = SqlFunc.getTablename(srcsql);
      if (tbn != null) {
        isTable = true;
        srcsql = tbn;
      }
    }
    StringBuffer sql = new StringBuffer(512);
    sql.append("insert into ").append(desttable);
    if (dl.getDataBaseInfo().isOracle())
      sql.append(" nologging ");
    sql.append('(');
    sql.append(getFieldstr(dl, amd));
    sql.append(") ");
    if (isTable) {
      sql.append("select ");
      sql.append(getFieldstr(dl, amd));
      sql.append(" from ");
    }
    sql.append(srcsql);
    return sql.toString();
  }

  private String getFieldstr(Dialect dl, AbstractMetaData amd) {
    StringBuffer sb = new StringBuffer(512);
    if (amd instanceof TableMetaData) {
    	TableMetaData tbmeta = ((TableMetaData) amd);
      int colLen = tbmeta.getColumnCount();
      int k = 0;
      for (int i = 0; i < colLen; i++) {
    	  TableColumnMetaData coli = tbmeta.getColumn(i);
        if (coli.isAutoInc())
          continue;
        if (k > 0)
          sb.append(',');
        sb.append(SqlFunc.getColumnName(dl, coli.getName()));
        k++;
      }
    }
    else {
      int columnCount = amd.getColumnCount();
      for (int i = 0; i < columnCount; i++) {
        if (i > 0)
          sb.append(',');
        sb.append(SqlFunc.getColumnName(dl, amd.getColumnName(i)));
      }
    }
    return sb.toString();
  }

  private String copyTableDifferentSource(ConnectionFactory srccf, String srcsql, ConnectionFactory destcf,
      String desttable, int option, boolean needCopyData) {
	  /**
	   * BI-5008 20110620
	   * 对于带前缀的表名，从一个数据库复制到另一个数据库，前缀要去掉，因为目的数据库中不一定有相同前缀名的schema；
	   * 默认复制到默认数据库的默认schema下，表名不带schema。
	   */
	  String desttable2 = desttable;
	  int k = desttable.indexOf(".");
	  if(k>0){
	    desttable2 = desttable.substring(k+1);
	  }
    try {
      Connection srcconn = srccf.getConnection();
      try {

        Connection destconn = destcf.getConnection();
        try {
          return copyTableDifferentSource(srcconn, srcsql, destconn, desttable2, option, needCopyData);
        }
        finally {
          destconn.close();
        }
      }
      finally {
        srcconn.close();
      }
    }
    catch (Exception ex) {
      ExceptionHandler.rethrowRuntimeException(ex);
      return null;
    }
  }
  private String copyTableDifferentSource(Connection srcconn, String srcsql, Connection destconn, String desttable,
      int option, boolean needCopyData) throws Exception {
    DataReaderFromDb drfd = new DataReaderFromDb(srcconn, srcsql);
    AbstractMetaData amd = drfd.getMeta();
    DataWriterToDb dw = new DataWriterToDb(destconn);
    if (ipro != null)
      dw.setProgress(ipro);
    setOption(dw, option);
    dw.createTable(amd, desttable);
    try {
      if (needCopyData)
        dw.writeData(drfd);
    }
    finally {
      drfd.close();
    }
    return dw.getTableName();
  }
  
  private void setOption(DataWriterToDb dw, int option) {
    /**
     * 这次改动，将option参数直接传给dw;
     */
    dw.setImportOpt(option);
  }

  public void setIprogress(IProgress ipro) {
    this.ipro = ipro;
  }
  public IProgress getIprogress(){
    return ipro;
  }
  private void addLog(String logStr) {
    if (ipro != null)
      ipro.addLogWithTime(logStr);
  }

  public String selectInto(String srcpool, String srcsql, String destpool,
      String desttable) {
    return selectInto(srcpool, srcsql, destpool, desttable, 0);
  }

  public void exportDataToCSV(String srcpool, String srcsql, Writer out)
      throws Exception {
    ConnectionFactory srcft = DefaultConnectionFactory.get(srcpool, true);
    exportDataToCSV(srcft,srcsql,out);
  }
  public void exportDataToCSV(String srcpool, String srcsql, Writer out,
      char separator, char quote) throws Exception {
    ConnectionFactory srcft = DefaultConnectionFactory.get(srcpool, true);
    exportDataToCSV(srcft, srcsql, out,separator,quote);
  }
  private void exportDataToCSV(ConnectionFactory srcft, String srcsql,
      Writer out,char separator, char quote) throws Exception {
    Connection conn = srcft.getConnection();
    try {
      exportDataToCSV(conn,srcsql, out,separator,quote);
    }
    finally {
      conn.close();
    }
  }
  private void exportDataToCSV(ConnectionFactory srcft, String srcsql,
      Writer out) throws Exception {
    Connection conn = srcft.getConnection();
    try {
      exportDataToCSV(conn,srcsql, out);
    }
    finally {
      conn.close();
    }
  }
  public void exportDataToCSV(Connection conn, String srcsql, Writer out)
      throws Exception {
    DataWriteToCSV dwts = new DataWriteToCSV(out);
    exportDataToCSV(conn, srcsql, dwts);
  }
  public void exportDataToCSV(Connection conn, String srcsql, Writer out,
      char separator, char quote) throws Exception {
    DataWriteToCSV dwts = new DataWriteToCSV(out, separator, quote);
    exportDataToCSV(conn, srcsql, dwts);
  }
  private void exportDataToCSV(Connection conn,String srcsql,DataWriteToCSV dwts)
      throws Exception {
    //addLog("开始备份表：" + srcsql);
	  addLog(I18N.getString("com.esen.jdbc.data.datacopyimpl.startbackupt", "开始备份表") + srcsql);
    DataReaderFromDb drfd = new DataReaderFromDb(conn, srcsql);
    try {
      dwts.writeData(drfd);
      dwts.flush();
    }
    finally {
      drfd.close();
    }
   // addLog("成功备份表：" + srcsql);
    addLog(I18N.getString("com.esen.jdbc.data.datacopyimpl.endbackupt", "成功备份表：") + srcsql);
  }

  public String importDataFromCSV(Reader in, String destpool,
      String tbname, String[] fields, int opt) throws Exception {
    ConnectionFactory destft = DefaultConnectionFactory.get(destpool, true);
     return importDataFromCSV(in,destft,tbname,fields,opt);
  }
  public String importDataFromCSV(Reader in, String destpool,
      String tbname, String[] fields, int opt,int skipline,char separator, char quote) throws Exception {
    ConnectionFactory destft = DefaultConnectionFactory.get(destpool, true);
     return importDataFromCSV(in,destft,tbname,fields,opt,skipline,separator,quote);
  }
  private String importDataFromCSV(Reader in, ConnectionFactory destft,
      String tbname, String[] fields, int opt,int skipline,char separator, char quote) throws Exception {
    Connection conn = destft.getConnection();
    try {
       return importDataFromCSV(in,conn,tbname,fields,opt,skipline,separator,quote);
    }
    finally {
      conn.close();
    }
  }
  private String importDataFromCSV(Reader in, ConnectionFactory destft,
      String tbname, String[] fields, int opt) throws Exception {
    Connection conn = destft.getConnection();
    try {
       return importDataFromCSV(in,conn,tbname,fields,opt);
    }
    finally {
      conn.close();
    }
  }
  public String importDataFromCSV(Reader in, Connection conn,
      String tbname, String[] fields,int opt) throws Exception {
    DataReaderFromCSV rd = new DataReaderFromCSV(in);
    return importDataFromCSV(rd,conn,tbname,fields,opt);
  }
  public String importDataFromCSV(Reader in, Connection conn,
      String tbname, String[] fields,int opt,int skipline,char separator, char quote) throws Exception {
    DataReaderFromCSV rd = new DataReaderFromCSV(in,skipline,separator,quote);
    return importDataFromCSV(rd,conn,tbname,fields,opt);
  }
  private String importDataFromCSV(DataReaderFromCSV rd, Connection conn,
      String tbname, String[] fields,int opt) throws Exception {
    DataWriterToDbFromCSV dwtdb = new DataWriterToDbFromCSV(conn);
    dwtdb.setFields(fields);
    setOption(dwtdb,opt);
    /**
     * 20090810
     * 导入csv格式，支持导入过程监控；
     */
    if (ipro != null)
      dwtdb.setProgress(ipro);
    try{
      dwtdb.createTable(tbname,rd);
      dwtdb.writeData(rd);
    }finally{
      dwtdb.close();
    }
    return dwtdb.getTableName();
  }


  public String selectInto(Connection srccon, String srcsql, Connection destcon, String desttable) {
    return selectInto(srccon, srcsql, destcon, desttable, 0);
  }

  public String selectInto(Connection srccon, String srcsql, Connection destcon, String desttable, int option) {
    return selectInto(srccon, srcsql, destcon, desttable, option, true);
  }

  public String createTableAsSelect(Connection srcconn, String srcsql, Connection destconn, String desttable) {
    return selectInto(srcconn, srcsql, destconn, desttable, 0, false);
  }
}
