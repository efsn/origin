package com.esen.jdbc.data.impl;


import java.io.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.text.NumberFormat;
import java.util.Locale;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.data.DataWriter;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.IProgress;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;
import com.esen.util.i18n.I18N;
/**
 * 将数据写入新格式*.db文件；
 * 备份文件存储格式：

里面字符编码统一使用utf8

ESEN-DB 2      //以ESEN-DB格式开头，空格，在存个int表示版本号
2345      //表结构长度int，下面的xml格式字符串长度
<?xml version="1.0" encoding="utf-8"?>
<tablemeta primarykey=”userid_,bbq_” totalcount=”1090000”> //已一个xml格式，保存表结构
<fields>                               //字段列表
<field fieldname=”userid_” fieldlable="userid_" fielddesc="" sqltype=”12” len=”30” maxlen=”20” scale=”0” autoinc=”0” nullalbe=”1” unique=”0”/>
  //字段名字，字段数据库类型，字段长度，值最大长度，小数位数，是否自动增长(0,1) ，是否可以为空(0,1)，是否唯一(0,1)
…
</fields>
<indexes>
  <index indexname=”index_name” unique=”1” fields=” userid_,bbq_,btype_”/>
//索引名，是否唯一索引(0,1)，索引字段逗号分割
…
</indexes>
</ tablemeta>
顺序存储字段的值：
数值类型直接存储；
字段值存储方法：
先存一个标志位表示(0 空,1 非空,2 Double.NaN)是否为空；
  int,long类型： 直接存值
  double类型：   由于有精度损失，当作字符存储； 
  日期类型：      存储long
  字符、clob，blob类型： 先存utf-8字符集的bytes长度，再存byte[]值
         


 * @author dw
 *
 */
public class DataWriterToStmNew extends AbstractDbDataIO implements  DataWriter{
  private OutputStream out;
  private IProgress ipro;
  private NumberFormat nf ;
  int[] maxlens;//保存每个字段对应的最大长度
  private static final int MAX_RESULT = 300;
  public static final String TITLE = "ESEN-DB ";
  private String logmessagepre;
  public DataWriterToStmNew(OutputStream out, IProgress ipro) {
    this.out = out;
    this.ipro = ipro;
  }

  private void saveTitle() throws IOException {
    out.write(TITLE.getBytes());
    StmFunc.writeInt(out,2);//版本号
  }

  public void writeData(DataReader rd) throws Exception {
    saveTitle();
    AbstractMetaData meta = rd.getMeta();
    int count = rd.getRecordCount();
    
    DocumentBuilder builder = XmlFunc.getDocumentBuilder();
    Document doc = builder.newDocument();
    Element ss = doc.createElement("tablemeta");
    saveMeta(ss,meta,count); //保存表结构到内存
    doc.appendChild(ss);
    
    long starttime = System.currentTimeMillis();
    maxlens = new int[meta.getColumnCount()];
    File f = File.createTempFile("esen-db", "2");
    try{
      //将数据写到临时文件，并获取每个字段对应的最大长度；
      saveDataToTempFile(f,rd);
      //将字段最大长度写入表结构；
      changeMeta(ss);
      //将表结构，数据写入指定流；
      saveData(f,doc);
    }finally{
      f.delete();
    }
//    setLastLog("记录总数:" + count + ",全部完成,用时:"
//            + formatFromTime(starttime));
    setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostmnew.logtotal", "记录总数:{0},全部完成,用时:{1}", new Object[]{String.valueOf(count),formatFromTime(starttime)}));
  }

  private void saveData(File f, Document doc) throws Exception {
    String metaxml = XmlFunc.document2str(doc, CHARSET);
    byte[] bb = metaxml.getBytes(CHARSET);
    StmFunc.writeInt(out, bb.length);
    out.write(bb);
    BufferedInputStream in = new BufferedInputStream(new FileInputStream(f));
    try{
      StmFunc.stmTryCopyFrom(in, out);
    }finally{
      in.close();
    }
  }

  private void changeMeta(Element ss) {
    NodeList ndlist = ss.getElementsByTagName("fields").item(0).getChildNodes();
    for(int i=0;i<ndlist.getLength();i++){
      Node item = ndlist.item(i);
      if(item.getNodeType() == Node.ELEMENT_NODE){
        ((Element)item).setAttribute("maxlen", String.valueOf(maxlens[i]));
      }
    }
  }

  private void saveDataToTempFile(File f, DataReader rd) throws Exception {
    BufferedOutputStream w = new BufferedOutputStream(new FileOutputStream(f));
    int count = rd.getRecordCount();
    //写入总行数到进度监视器pro;
    if(ipro!=null){
      ipro.setProgress(0, count, 1);
    }
    try{
      int cnt = 0;
      long starttime = System.currentTimeMillis();
      while(rd.next()){
        saveData(w,rd);
        cnt++;
        if (cnt % MAX_RESULT == 0) {
          checkCancel();
          /**
           * 20090507
           * 记录写入文件的进度；
           * 每MAX_RESULT条写次进度；
           */
          if (ipro != null) {
            ipro.setPosition(cnt);
           // setLastLog("记录总数:" + count + ",已完成:" + StrFunc.double2str(cnt * 1.0 / count * 100, 0, 2, false) + "%,用时:" + formatFromTime(starttime));
           
            setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostmnew.logcount1", "记录总数:{0},已完成:{1}%,用时:{2}", new Object[]{String.valueOf(count),StrFunc.double2str(cnt * 1.0 / count * 100, 0, 2, false),formatFromTime(starttime)}));
          }
          Thread.sleep(1);
        }
      }
      w.flush();
      if (ipro != null) {
        ipro.setPosition(cnt);
        //setLastLog("记录总数:" + count + ",已完成:100% ,用时:" + formatFromTime(starttime));
        Object[] param=new Object[]{
      		  String.valueOf(count),formatFromTime(starttime)
         };
          setLastLog(I18N.getString("com.esen.jdbc.data.impl.datawritertostmnew.logcount2", "记录总数:{0},已完成:100% ,用时:{1}", param));
      }
    }finally{
      w.close();
    }
  }
  private String formatTime(long l) {
    return StrFunc.formatTime(l);
  }

  private String formatFromTime(long l) {
    return formatTime(System.currentTimeMillis() - l);
  }
  private void checkCancel() {
    if (ipro != null) {
      ipro.checkCancel();
    }
  }
  private void setLastLog(String log) {
    if (ipro != null) {
      ipro.setLastLogWithTime(this.logmessagepre == null ? log : this.logmessagepre
          + log);
    }
  }
  private void saveData(BufferedOutputStream w, DataReader rd) throws Exception {
    AbstractMetaData meta = rd.getMeta();
    for (int i = 0; i < meta.getColumnCount(); i++) {
      Object o = rd.getValue(i);
      switch (meta.getColumnType(i)) {
        case Types.DATE:
          //此处获得的对象在不同数据库中获得的对象可能会不同,可能为Date也可能为Timestamp
          //oracle中获得的为Date类型
          if(o==null)
            saveDate(w,null);
          if (o instanceof Date)
            saveDate(w,(Date) o);
          if (o instanceof Timestamp)
            saveDate(w,new Date(((Timestamp) o).getTime()));
          break;
        case Types.TIME:
          saveDate(w,(Time)o);
          break;
        case Types.TIMESTAMP:
          saveDate(w,(Timestamp) o);
          break;
        case Types.BLOB:
        case Types.LONGVARBINARY:
        case Types.VARBINARY:
        case Types.BINARY:
          saveBlob(w,(InputStream)o);
          break;
        case Types.CLOB:
        case Types.LONGVARCHAR:
          saveClob(w,(Reader)o);
          break;
        case Types.TINYINT:
        case Types.SMALLINT:
        case Types.INTEGER: {
          saveInt(w,(Number)o);
          break;
        }
        case Types.BIGINT:{
          saveLong(w,(Number)o);
          break;
        }
        case Types.FLOAT:
        case Types.DECIMAL:
        case Types.DOUBLE:
        case Types.NUMERIC:
        case Types.REAL:{
          saveDouble(w,o);
          break;
        }
        default:
          saveString(w,o == null ? null : o.toString(),i);
      }
    }
  }
  /**
   * double使用String来存储，原因是超大数值转换double有精度损失；
   * @param w
   * @param o
   * @throws IOException 
   */
  private void saveDouble(BufferedOutputStream w, Object o) throws IOException {
    if(o==null){
      saveString(w,null);
      return;
    }
    if(o instanceof BigDecimal){
      BigDecimal bd = (BigDecimal)o;
      saveString(w,bd.toString());
    }else if(o instanceof String){
      saveString(w,(String)o);
    }else if(o instanceof Number){
      Number n = (Number)o;
      double d = n.doubleValue();
      NumberFormat nf = getNumberFormat();
      saveString(w,nf.format(d));
    }else{
      //throw new RuntimeException("无法识别的数值："+o);
    	throw new RuntimeException(I18N.getString("com.esen.jdbc.data.impl.datawritertostmnew.unknownnum", "无法识别的数值：")+o);
    }
  }
  private NumberFormat getNumberFormat(){
    if(nf==null){
      nf= NumberFormat.getNumberInstance(Locale.CHINA);
      nf.setGroupingUsed(false);
    }
    return nf;
  }

  private void saveString(BufferedOutputStream w, String v, int i) throws IOException {
    int len = saveString(w,v);
    if(maxlens[i]<len)
      maxlens[i] = len;
  }

  private void saveMeta(Element ss, AbstractMetaData meta, int count) throws DOMException, Exception {
    ss.setAttribute("totalcount", String.valueOf(count));
    Document doc = ss.getOwnerDocument();
    Element fields = doc.createElement("fields");
    if(meta instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)meta;
      String[] primarykey = tmd.getPrimaryKey();
      if (primarykey != null && primarykey.length != 0) {
        ss.setAttribute("primarykey", array2str(primarykey));
      }
      int colLen = tmd.getColumnCount();
      for(int i=0;i<colLen;i++){
        Element field = doc.createElement("field");
        TableColumnMetaData c = tmd.getColumn(i);
        field.setAttribute("fieldname", c.getName());
        XmlFunc.setElementAttribute(field,"fieldlable", c.getLabel());
        XmlFunc.setElementAttribute(field,"fielddesc", c.getDesc());
        field.setAttribute("sqltype", String.valueOf(c.getType()));
        field.setAttribute("len", String.valueOf(c.getLen()));
        field.setAttribute("scale", String.valueOf(c.getScale()));
        field.setAttribute("autoinc", c.isAutoInc()?"1":"0");
        field.setAttribute("nullable", c.isNullable()?"1":"0");
        field.setAttribute("unique", c.isUnique()?"1":"0");
        fields.appendChild(field);
      }
      Element indexes = doc.createElement("indexes");
      TableIndexMetaData[] indx = tmd.getIndexes();
      if(indx!=null){
        for(int i=0;i<indx.length;i++){
          TableIndexMetaData dx = indx[i];
          Element index = doc.createElement("index");
          index.setAttribute("indexname", dx.getName());
          index.setAttribute("unique", dx.isUnique()?"1":"0");
          index.setAttribute("fields", array2str(dx.getColumns()));
          indexes.appendChild(index);
        }
      }
      ss.appendChild(indexes);
    }
    else {
      for (int i = 0; i < meta.getColumnCount(); i++) {
        Element field = doc.createElement("field");
        field.setAttribute("fieldname", meta.getColumnName(i));
        XmlFunc.setElementAttribute(field,"fieldlable", meta.getColumnLabel(i));
        XmlFunc.setElementAttribute(field,"fielddesc", meta.getColumnDescription(i));
        field.setAttribute("sqltype", String.valueOf(meta.getColumnType(i)));
        field.setAttribute("len", String.valueOf(meta.getColumnLength(i)));
        field.setAttribute("scale", String.valueOf(meta.getColumnScale(i)));
        /**
         * 20090813
         * 从sql备份的.db文件，也要保存是否为空或者是否唯一信息；
         */
        field.setAttribute("nullable", String.valueOf(meta.isNullable(i)));
        field.setAttribute("unique", String.valueOf(meta.isUnique(i)));
        fields.appendChild(field);
      }
    }
    ss.appendChild(fields);
    
    
  }


  private String array2str(String[] primarykey) {
    if(primarykey==null) return null;
    StringBuffer buf = new StringBuffer(primarykey.length*20);
    for (int i = 0; i < primarykey.length; i++) {
      if (i != 0) {
        buf.append(",");
      }
      buf.append(primarykey[i]);
    }
    return buf.toString();
  }

}
