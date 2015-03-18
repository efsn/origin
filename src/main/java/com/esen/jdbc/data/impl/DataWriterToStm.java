package com.esen.jdbc.data.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

import com.esen.io.MyByteArrayOutputStream;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.data.DataWriter;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.ArrayFunc;
import com.esen.util.IProgress;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.i18n.I18N;
/**
 * 将数据写入旧格式*.db文件；
 * @author Administrator
 *
 */
public class DataWriterToStm implements DataWriter{
  protected OutputStream out;
  protected IProgress ipro;
  private String logmessagepre;
  protected static final int MAX_RESULT = 10000;//限制查询结果的个数，不能超过该大小

  public DataWriterToStm(OutputStream out, IProgress ipro){
    this.out = out;
    this.ipro = ipro;
  }
  /**
   * 将一个数据集写入流
   * @param rd
   * @throws Exception
   */
  public void writeData(DataReader rd) throws Exception{
    AbstractMetaData meta = rd.getMeta();
    checkCancel();
    int cnt = 0;
    long starttime = System.currentTimeMillis();
    int count = rd.getRecordCount();
    saveMeta(meta,count);
    
    
    //addLog("记录总数:" + count + ",已完成:0%,用时:" + formatTime(0));
    Object[] param=new Object[]{
    		String.valueOf(count),"0%",formatTime(0)
    };
    addLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostm.writestart", "记录总数:{0},已完成{1},用时: {2}", param));
    DecimalFormat df = new DecimalFormat("0.00%");
    while(rd.next()){
    	saveData(rd);
    	cnt++;
    	Object[] param1=new Object[]{
        		String.valueOf(count),df.format(cnt * 1.0 / count),formatFromTime(starttime)
        };
    	if (cnt % MAX_RESULT == 0) {
        checkCancel();
//        setLastLog("记录总数:" + count + ",已完成:"
//            + df.format(cnt * 1.0 / count) + ",用时:"
//            + formatFromTime(starttime));
        setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostm.writestart", "记录总数:{0},已完成{1},用时: {2}", param));
        Thread.sleep(1);
      }
    }
//    setLastLog("记录总数:" + count + ",全部完成,用时:"
//          + formatFromTime(starttime));
    Object[] param2=new Object[]{String.valueOf(count),formatFromTime(starttime)};
    setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostm.writesend", "记录总数:{0},全部完成,用时:{1};", param2));
    
  }
  protected void saveData(DataReader rd) throws Exception {
    AbstractMetaData meta = rd.getMeta();
    NumberFormat nf = NumberFormat.getNumberInstance(Locale.CHINA);
    nf.setGroupingUsed(false);
    for (int i = 0; i < meta.getColumnCount(); i++) {
      switch (meta.getColumnType(i)) {
        case Types.DATE:
          //此处获得的对象在不同数据库中获得的对象可能会不同,可能为Date也可能为Timestamp
          //oracle中获得的为Date类型
          Object oo = rd.getValue(i);
          if(oo==null)
            saveString(null);
          if (oo instanceof Date)
            saveDate((Date) oo);
          if (oo instanceof Timestamp)
            saveDate(new Date(((Timestamp) oo).getTime()));
          break;
        case Types.TIME:
          saveTime((Time) rd.getValue(i));
          break;
        case Types.TIMESTAMP:
          saveTimestamp((Timestamp) rd.getValue(i));
          break;
        case Types.BLOB:
        case Types.LONGVARBINARY:
          saveBlob((InputStream) rd.getValue(i));
          break;
        case Types.CLOB:
        case Types.LONGVARCHAR:
          saveClob((Reader) rd.getValue(i));
          break;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.BIGINT:
        case Types.INTEGER: {
          Object obj = rd.getValue(i);
          if(obj==null) {
            saveString(null);
            break;
          }
          Long d = (Long)obj;
          saveString(nf.format(d));
          break;
        }
        case Types.FLOAT:{
          Object obj = rd.getValue(i);
          if(obj==null) {
            saveString(null);
            break;
          }
          Float d = (Float)obj;
          saveString(nf.format(d));
          break;
        }
        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.REAL:{
          Object obj = rd.getValue(i);
          if(obj==null) {
            saveString(null);
            break;
          }
          if(obj instanceof Double){
            Double d = (Double)obj;
            saveString(nf.format(d));
          }else if(obj instanceof String){
            saveString((String)obj);
          }else{
            saveString(obj == null ? null : obj.toString());
          }
          break;
        }
        default:
          Object obj = rd.getValue(i);
          saveString(obj == null ? null : obj.toString());
      }
    }
  }

  private void saveString(String s) throws IOException {
    writeString(s);
  }

  private void saveDate(Date d) throws IOException {
    writeString(d == null ? null : String.valueOf(d.getTime()));
  }

  private void saveTime(Time t) throws IOException {
    writeString(t == null ? null : String.valueOf(t.getTime()));
  }

  private void saveTimestamp(Timestamp t) throws IOException {
    writeString(t == null ? null : String.valueOf(t.getTime()));
  }

  private void saveBlob(InputStream in) throws IOException {
    if (in == null) {
      writeInt(0);
    }
    else {
      MyByteArrayOutputStream bout = new MyByteArrayOutputStream();
      try {
        int len = StmFunc.stmTryCopyFrom(in, bout);
        writeInt(len);
        if (len != 0) {
          bout.writeTo(out);
        }
      }
      finally {
        bout.close();
      }
    }
  }

  /**
   * 保存clob字段
   * @param r
   * @throws IOException
   */
  private void saveClob(Reader r) throws IOException {
    if (r == null) {
      writeInt(0);
    }
    else {
      MyByteArrayOutputStream bout = new MyByteArrayOutputStream();
      OutputStreamWriter w = new OutputStreamWriter(bout);
      try {
        StmFunc.reader2writer(r, w);
        w.flush();
        int len = bout.size();
        writeInt(len);
        if (len != 0) {
          bout.writeTo(out);
        }
      }
      finally {
        w.close();
        bout.close();
      }
    }
  }
	private String formatTime(long l) {
    return StrFunc.formatTime(l);
  }

  private String formatFromTime(long l) {
    return formatTime(System.currentTimeMillis() - l);
  }
  public void setLogMessagePre(String logmessagepre) {
    this.logmessagepre = logmessagepre == null ? null : (logmessagepre + " ");
  }
  private void addLog(String log) {
    if (ipro != null) {
      ipro.addLog(this.logmessagepre == null ? log : this.logmessagepre + log);
    }
  }

  private void setLastLog(String log) {
    if (ipro != null) {
      ipro.setLastLog(this.logmessagepre == null ? log : this.logmessagepre
          + log);
    }
  }
  /**
   * 保存数据库表的结构信息
   * @param meta
   * @param count 
   * @throws Exception 
   */
  protected void saveMeta(AbstractMetaData meta, int count ) throws Exception {
    savePrimaryKey(meta);
    saveFields(meta);
    saveIndexes(meta);
    writeInt(count);//保存记录条数  
  }
  /**
   * 保存表的索引(如果是oracle还将保存自动增长序列) 
   * @throws Exception 
   */
  protected void saveIndexes(AbstractMetaData md) throws Exception {
    if(!(md instanceof TableMetaData)){
      writeInt(0);
      return;
    }
    TableMetaData tmd = (TableMetaData)md;
    TableIndexMetaData[] indexes = tmd.getIndexes();
    if (indexes == null) {
      writeInt(0);
      return;
    }
    int len = indexes.length;

    int savelen = indexes.length;
    writeInt(savelen);
    TableIndexMetaData index;
    for (int i = 0; i < len; i++) {
      index = indexes[i];
      String[] c = index.getColumns();
      writeString(index.getName());
      writeInt(index.isUnique() ? 1 : 0);
      writeString(ArrayFunc.array2Str(c, ','));
    }
  }
  /**
   * 保存表的字段结构 
   * @throws Exception 
   */
  private void saveFields(AbstractMetaData md) throws Exception {
    if (md instanceof TableMetaData) {
      TableMetaData tmd = (TableMetaData) md;
      TableColumnMetaData[] columns = tmd.getColumns();
      TableColumnMetaData c;
      writeInt(tmd.getColumnCount());//写字段个数
      for (int i = 0; i < columns.length; i++) {
        c = columns[i];
        writeString(c.getName());//名称
        writeInt(c.getLen());//长度
        writeInt(c.getScale());//小数位数
        writeInt(c.getType());//类型
        writeInt(c.isAutoInc() ? 1 : 0);//是否自动增长
        writeInt(c.isNullable() ? 1 : 0);//是否可为空
        writeInt(c.isUnique() ? 1 : 0);//是否唯一
      }
    }
    else {
      writeInt(md.getColumnCount());//写字段个数
      for (int i = 0; i < md.getColumnCount(); i++) {
        writeString(getColumnName(md, i));//名称
        writeInt(md.getColumnLength(i));//长度
        writeInt(md.getColumnScale(i));//小数位数
        writeInt(md.getColumnType(i));//类型
        writeInt(0);//是否自动增长
        writeInt(0);//是否可为空
        writeInt(0);//是否唯一
      }
    }
  }
  private String getColumnName(AbstractMetaData md, int i) throws Exception{
    String fn = md.getColumnLabel(i);
    if(fn==null||fn.length()==0)
      fn = md.getColumnName(i);
    return fn;
  }
  private void savePrimaryKey(AbstractMetaData md) throws Exception {
    String[] primarykey = null;
    if(md instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)md;
      primarykey = tmd.getPrimaryKey();
    }
    if (primarykey != null && primarykey.length != 0) {
      StringBuffer buf = new StringBuffer(primarykey.length*20);
      for (int i = 0; i < primarykey.length; i++) {
        if (i != 0) {
          buf.append(",");
        }
        buf.append(primarykey[i]);
      }
      writeString(buf.toString());
    }
    else {
      writeString(null);
    }
  }
  private void checkCancel() {
    if (ipro != null) {
      ipro.checkCancel();
    }
  }

  /**
   * 输出整形
   * @param i
   * @throws IOException
   */
  protected void writeInt(int i) throws IOException {
    StmFunc.writeInt(out, i);
  }

  /**
   * 输出字串
   * @param s
   * @throws IOException
   */
  protected void writeString(String s) throws IOException {
    if (s == null) {
      writeInt(0);
    }
    else {
      byte[] b = s.getBytes();
      writeInt(b.length);
      if (b.length != 0) {
        out.write(b);
      }
    }
  }
}
