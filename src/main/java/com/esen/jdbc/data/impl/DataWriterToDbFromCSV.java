package com.esen.jdbc.data.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Types;

import com.esen.io.CSVWriter;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.DataCopy;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.jdbc.sql.parser.SqlFunc;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;


/**
 * 将csv格式文件写入数据库
 * fields参数用于指定字段名，如果不为空，导入的列数由fields的length决定，少的插入空，多余的列将被忽略；

tbname 指定导入的表名，如果为空出异常提示；
当opt==OPT_CREATENEWTABLE时：
如果tbname存在，则创建一个新表，不存在则创建tbname表，fields参数不为空，则根据其作为字段，类型都为字符串型来创建表，
导入时判断第一行是不是字段名，是字段名则按照指定的列导入数据，不是字段名则当数据导入；
fields参数为空，则根据第一行的字段列数，创建表，字段名随机(field0...9...x),这种情况第一行当做数据导入表；
后面如果有多于第一行的列数，将被忽略；

当opt==OPT_OVERWRITER时：
如果tbname存在，删除之，创建新表tbname，创建规则同上；

当opt==OPT_CLEARTABLE时：
如果tbname不存在，创建新表tbname，创建规则同上；
如果tbname存在，删除其数据，导入数据时，
fields参数不为空（这里要判断fields字段是否和tbname字段一致，如果不一致出异常提示），则根据指定的字段顺序导入列值；
fields参数为空，则不指定导入顺序；
导入时判断第一行是不是字段名，是字段名则按照指定的列导入数据，不是字段名则当数据导入；
后面如果由多余的列值，将被忽略；

 * @author dw
 *
 */
public class DataWriterToDbFromCSV extends DataWriterToDb {
  public static final int DEFAULT_LENGTH = 100;
  public static final int DEFAULT_TYPE = Types.VARCHAR;
  private String[] fields;//设置写入的字段集合；
  private DataReaderFromCSV rd;
  /**
   * 存储第一行数据；
   */
  private String[] firstLine;
  /**
   * 判断第一行是不是字段名；
   */
  private boolean isfirstField;
  
  private boolean isFirstComment;

  public DataWriterToDbFromCSV(Connection conn) {
    super(conn);
  }
  public void setFields(String[] fds){
    this.fields = fds;
  }

  
  public void writeData(DataReader rd) throws Exception {
    DataReaderFromCSV csvrd = (DataReaderFromCSV)rd;
    try {
      checkCancel();
      addLog(I18N.getString("com.esen.jdbc.data.impl.dataWritertodbfromcsv.log",
				"开始写入{0}数据；", new Object[] { tablename }));
      //addLog("开始写入"+tablename+"数据；");
      long n = 0;
      long l = System.currentTimeMillis();
      initWriterData(tablename);
      if(needClearTable){
        //与写入数据同一个事务中，执行清除数据操作，便于出异常回滚；
        clearData();
      }
      if(!isfirstField){
        //第一行不是字段行，写入表；
    	  /**
    	   * BI-4265
    	   * 第一行也可能为空；比如BI中编辑维表，删除维表内容保存；
    	   * 20110316 dw
    	   */
    	if(firstLine!=null&&firstLine.length>0){
    		writeLine(firstLine);
    		n++;
            append();
    	}
      }
      while (csvrd.next()) {
        n++;
        if(n%100==0)
          checkCancel();
        String[] line = csvrd.getLineValues();
        writeLine(line);
        append();
        if(n%1000==0)
          //setLastLog("写入完成"+n+"行;");
        	setLastLog(I18N.getString("com.esen.jdbc.data.impl.dataWritertodbfromcsv.writen", "写入完成{0}行;",new Object[]{String.valueOf(n)}));
      }
      commit();
      Object[] param=new Object[]{
    		  String.valueOf(n),StrFunc.formatTime(System.currentTimeMillis()-l)
      };
      //addLog("写入完成，共条"+n+"记录，耗时："+StrFunc.formatTime(System.currentTimeMillis()-l));
    addLog(I18N.getString("com.esen.jdbc.data.impl.dataWritertodbfromcsv.writend", "写入完成，共条{0}记录，耗时：{1}", param));
    }catch (Exception e) {
      //写入出异常，回滚操作，保证将清空的数据还原；
      conn.rollback();
      throw e;
    }
    finally {
      close();
    }
  }
  private void writeLine(String[] line) throws Exception, SQLException {
    int col = 1;
    for (int i = 0; i < meta.getColumnCount(); i++) {
      /**
       * 找到指定字段对应的字段列号；
       */
      int p = fieldIndex[i];
      Object obj = null;
      if(p>=0&&p<line.length)
        obj = line[p];
      checkFieldLength(i, obj);
      setObject(col++,obj);
    }
  }
  
  protected AbstractMetaData getDestMeta(AbstractMetaData md) {
    if (meta == null) {
      try {
          /**
           * BI-5927 备份成csv时，第一行为字段名，并带上类型和长度，只支持字符、数值、日期类型；且在第一行开头加上'#',表示为注释；
           * 格式：#bbq(C|10),name(C|100),tzze(N|15|2),cnt(I),date(D),... 
           * 恢复csv格式时，读取第一行： 
           * 是注释，解析字段名、类型、类型、长度，当然可能这个csv格式不是通过BI备份的，是外来的，只有字段名，类型全部为字符串。 
           * 不是注释，则创建field1...fieldn字段名，类型全部为字符串； 
           */
        readFirstLine();
        /**
         * 导入csv时，如果指定的导入表存在，则读取表结构与csv第一行比较，判断第一行是不是数据库字段，
         * 是字段名，忽略第一行，将后面的数据根据字段匹配导入表中；
         * 如果指定的表不存在，则无法确定第一行是不是字段行；
         */
        if(dl.createDbDefiner().tableExists(conn, null, tablename)){
          TableMetaData tmd = dl.createDbMetaData(conn).getTableMetaData(tablename);
          isfirstField = isFirstComment;
          AbstractMetaData amd = procMetaData(tmd);
          meta =new AbstractMetaDataForWriter( amd,dl);
        }else{
        	TableColumnMetaDataForWriter[] parseFields = parseFields(fields);
			if(isFirstComment){
            	isfirstField = true;
            	meta = new AbstractMetaDataForWriter( new AbstractMetaDataForCSV(firstLine.length,parseFields==null?firstLineFields:parseFields),dl);
            }else{
            	isfirstField = false;
            	meta = new AbstractMetaDataForWriter( new AbstractMetaDataForCSV(firstLine.length,parseFields==null?firstLineFields:parseFields),dl);
            }
        }
        fieldIndex = new int[meta.getColumnCount()];
        for(int i=0;i<fieldIndex.length;i++){
          if(isfirstField){
            String fdname = meta.getColumnName(i);
            fieldIndex[i] = findIndex(fdname);
          }else{
            fieldIndex[i]=i;
          }
        }
      }
      catch (Exception e) {
        throw new RuntimeException(e);
      }
    }
    return meta;
  }
  
  /**
   * csv格式导入，无法读取表结构，md参数为null；
   * 需要先根据目的表结构和csv第一行数据分析出csv数据的结构，然后确定追加记录有那些字段可以写入目的表；
   */
  protected void findImportFields(AbstractMetaData md) throws Exception {
	  getDestMeta(md);
	  super.findImportFields(meta);
  }
  
  /**
   * 如果第一行是字段行，此方法找到给定的字段在第一行字段值对应的列号；
   * @param fdname
   * @return
   */
  private int findIndex(String fdname) {
    for(int i=0;i<this.firstLineFields.length;i++){
      if(fdname.equalsIgnoreCase(firstLineFields[i].getName()))
        return i;
    }
    return -1;
  }
  /**
   * 如果每个指定的字段名，都可以在第一行列值中找到，返回true；
   * @param fds
   * @return
   */
  private boolean isFirstFields(String[] fds) {
    if(fds==null||fds.length==0)
      return false;
    for(int i=0;i<fds.length;i++){
      if(!isField(fds[i])){
        return false;
      }
    }
    return true;
  }
  
  private boolean isField(String fd) {
    for(int i=0;i<this.firstLine.length;i++){
      if(fd!=null&&fd.equalsIgnoreCase(firstLine[i]))
        return true;
    }
    return false;
  }
  
  private TableColumnMetaDataForWriter[] firstLineFields;

	private void readFirstLine() throws Exception {
		if (rd.next()) {
			String[] line = rd.getLineValues();
			/**
			 * 根据第一行的字符分析是否为注释
			 */
			if (line != null && line.length > 0 && line[0] != null && line[0].length() > 0) {
				isFirstComment = (line[0].charAt(0) == CSVWriter.DEFAULT_COMMENT);
			}
			if (isFirstComment) {
				line[0] = line[0].substring(1);
				firstLineFields = parseFields(line);
			}
			this.firstLine = line;

		}
	}
  /**
   * 根据参数判断如何创建表
   * @param tbname
   * @throws Exception
   */
  public void createTable(String tbname, DataReaderFromCSV rd) throws Exception {
    this.rd = rd;
    createTable(null, tbname);
  }

  /**
   * 根据指定的字段，生成表结构；
   * @param tmd 
   * @param isfirstField 
   * @param meta2
   * @param fields2
   * @return
   * @throws Exception 
   */
  private AbstractMetaData procMetaData(TableMetaData tmd) throws Exception {
    //指定的导入字段集合不为空
    if (fields != null && fields.length > 0) {
      if(this.import_opt==DataCopy.OPT_OVERWRITER||this.import_opt==DataCopy.OPT_CREATENEWTABLE){
        //建新表和覆盖目的表都与目的表表结构无关；
        return new AbstractMetaDataForCSV(fields.length,parseFields(fields));
      }else return new AbstractMetaDataForFields(tmd, fields);
    }else{
      //指定导入字段为空，则分析rd的第一行数据，如果与指定的导入表的字段吻合，则视为字段集合；
      if(isfirstField){
    	  String[] firstLineFieldsName = new String[firstLineFields.length];
    	  for(int i=0;i<firstLineFields.length;i++){
    		  firstLineFieldsName[i]=firstLineFields[i].getName();
    	  }
        return new AbstractMetaDataForFields(tmd, firstLineFieldsName);
      }
    }
    return tmd;
  }

  /**
   * 指定的导入字段：
   * 则判断导入的字段是否和第一行字段值一致，或者是其一部分，是则返回ture；
   * 没有指定导入字段：
   * 如果目的表存在meta!=null, 则判断第一行字段值是否和目的表字段一致或者是期一部分，是则返回true；
   * 其他情况都返回false；
   * @param meta
   * @return
   * @throws Exception
   */
  private boolean isFirstFields(AbstractMetaData meta) throws Exception {
    if(firstLine==null)
      return false;
    if(fields!=null&&fields.length>0){
      return isFirstFields(fields);
    }
    if(meta==null) return false;
    for(int i=0;i<firstLine.length;i++){
      String fn = firstLine[i];
      if(!isField(meta,fn)){
        return false;
      }
    }
    return true;
  }
  private boolean isField(AbstractMetaData meta, String fn) {
    for(int i=0;i<meta.getColumnCount();i++){
      String fni = meta.getColumnName(i);
      if(fni.equalsIgnoreCase(fn))
        return true;
    }
    return false;
  }
  
	private TableColumnMetaDataForWriter[] parseFields(String[] fields) {
		if (fields == null || fields.length == 0) {
			return null;
		}
		TableColumnMetaDataForWriter[] fs = new TableColumnMetaDataForWriter[fields.length];
		for (int i = 0; i < fs.length; i++) {
			fs[i] = new TableColumnMetaDataForWriter(fields[i]);
		}
		return fs;
	}

}

class AbstractMetaDataForCSV implements AbstractMetaData{
	private int len;
	  private TableColumnMetaDataForWriter[] fds;
	  
	  public AbstractMetaDataForCSV(int len,TableColumnMetaDataForWriter[] fds) {
	    this.len = len;
	    this.fds  = fds;
	  }

	  public int getColumnCount() {
	    if(fds!=null&&fds.length>0)
	      return fds.length;
	    return len;
	  }

	  public String getColumnDescription(int i) {
	    return null;
	  }

	  public String getColumnLabel(int i) {
	    return getColumnName(i);
	  }

	  public int getColumnLength(int i) {
		if(fds!=null&&fds.length>0)
			return fds[i].getLen();
		return DataWriterToDbFromCSV.DEFAULT_LENGTH;
	  }

	  public String getColumnName(int i) {
		if(fds!=null&&fds.length>0)
			  return fds[i].getName()==null?"FIELD"+i:fds[i].getName();
		return "FIELD"+i;
	  }

	public int getColumnScale(int i) {
		if (fds != null && fds.length > 0)
			return fds[i].getScale();
		return 0;
	}

	  public int getColumnType(int i) {
		if(fds!=null&&fds.length>0)
			 return fds[i].getType();
		return DataWriterToDbFromCSV.DEFAULT_TYPE;
	  }
	  
	  /**
	   * csv格式文件导入新表，字段默认可以为空；
	   */
	  public int isNullable(int i) {
	    return 1;
	  }

	  public int isUnique(int i) {
	    return 0;
	  }
	  
}
