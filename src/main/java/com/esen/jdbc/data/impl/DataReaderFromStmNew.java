package com.esen.jdbc.data.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Types;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.esen.io.BlobFileCacher;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;
import com.esen.util.tmpfile.DefaultTempFileFactory;

/**
 * 读取新格式*.db文件；
 * 数值类型采用字符串存储方式，如果采用double方式存储，超大数据有精度损失，比如：210005000004723196D
 * blob流采用临时文件存储；
 * 遍历完数据一定要调用close方法；
 * @author dw
 *
 */
public class DataReaderFromStmNew extends AbstractDbDataIO implements DataReader {
  private InputStream in ;
  private AbstractMetaData meta;
  private int count;
  private int totalcount;
  private BlobFileCacher blobcacher;
  
  /**
   * 记录一行数据；使getValue(i)可以反复调用；
   */
  private Object[] row;
  public DataReaderFromStmNew(InputStream in ){
    this.in = in;
  }
  
  /**
   * blob字段采用临时文件存储，遍历完毕，调用此方法删除临时文件；
   */
  public void close() throws Exception {
    if(blobcacher!=null)
      blobcacher.close();
  }

  public AbstractMetaData getMeta() throws Exception {
    if(meta==null)
      init();
     return meta;
  }

  private void init() throws Exception {
    int len = StmFunc.readInt(in);
    byte[] bb = new byte[len];
    in.read(bb);
    String xmlstr = new String(bb,CHARSET);
    analyseMeta(xmlstr);
  }

  private void analyseMeta(String xmlstr) throws Exception {
    Document doc = XmlFunc.getDocument(xmlstr);
    Element ss = doc.getDocumentElement();
    totalcount = Integer.parseInt(ss.getAttribute("totalcount"));
    count = totalcount;
    
    String pkeys = ss.getAttribute("primarykey");
    String[] keys = null;
    if(pkeys!=null&&pkeys.length()>0)
      keys = pkeys.split(",");
    TableColumnMetaData[] columns = getFields(ss);
    TableIndexMetaData[] indx = getIndexes(ss);
    meta = new TableMetaDataStm(keys,columns,indx);
    
  }

  private TableColumnMetaData[] getFields(Element ss) {
    TableColumnMetaData[] columns;
    Element fields = (Element)ss.getElementsByTagName("fields").item(0);
    NodeList fds = fields.getElementsByTagName("field");
    columns = new TableColumnMetaData[fds.getLength()];
    TableColumnMetaDataImpl column;
    for(int i=0;i<fds.getLength();i++){
      Element fd = (Element)fds.item(i);
      String fdname = fd.getAttribute("fieldname");
      String fdlable = fd.getAttribute("fieldlable");
      String fddesc = fd.getAttribute("fielddesc");
      
      int sqltype = StrFunc.str2int(fd.getAttribute("sqltype"),0);
      int len = StrFunc.str2int(fd.getAttribute("len"),0);
      int maxlen = StrFunc.str2int(fd.getAttribute("maxlen"),0);
      int scale = StrFunc.str2int(fd.getAttribute("scale"),0);
      
      int autoinc = StrFunc.str2int(fd.getAttribute("autoinc"),0);
      /**
       * 20090813
       * 读取.db文件的头，获取表结构，字段的nullable属性，默认设置为可以为空；
       * 一些从sql备份的文件，可能无法获取字段是否为空信息，这样的字段总设置成可空；
       */
      int nullable = StrFunc.str2int(fd.getAttribute("nullable"),1);
      int unique = StrFunc.str2int(fd.getAttribute("unique"),0);
      
      column = new TableColumnMetaDataImpl(fdname);
      column.setLable(fdlable);
      column.setDesc(fddesc);
      column.setType(sqltype);
      column.setLength(len>maxlen?len:maxlen);
      column.setScale(scale);
      column.setAutoInc(autoinc==1);
      column.setNullable(nullable==1);
      column.setUnique(unique==1);
      columns[i] = column;
    }
    return columns;
  }

  private TableIndexMetaData[] getIndexes(Element ss) {
    Element indexes = (Element)ss.getElementsByTagName("indexes").item(0);
    if(indexes==null) return null;
    NodeList inds = indexes.getElementsByTagName("index");
    if(indexes!=null&&inds.getLength()>0){
      TableIndexMetaData[] indx = new TableIndexMetaData[inds.getLength()];
      for(int i = 0;i<inds.getLength();i++){
        Element el = (Element)inds.item(i);
        String indexname = el.getAttribute("indexname");
        int unique = StrFunc.str2int(el.getAttribute("unique"),0);
        String fds = el.getAttribute("fields");
        TableIndexMetaDataImpl ind = new TableIndexMetaDataImpl(indexname,fds,unique==1);
        indx[i] = ind;
      }
      return indx;
    }
    return null;
  }

  public int getRecordCount() throws Exception {
    if(meta==null)
      init();
    return totalcount;
  }
  /**
   * 获得每个字段的值，可以反复调用；
   */
  public Object getValue(int i) throws Exception {
    if(meta==null)
      init();
    if(count<0) return null;
    return row[i];
  }
  /**
   * 读取每个字段的值；
   * 注意：大字段值返回对象是BlobValue
   * @param i
   * @return
   * @throws IOException
   */
  private Object getColumn(int i) throws IOException {
    switch(meta.getColumnType(i)){
      case Types.TINYINT:
      case Types.SMALLINT:
      case Types.INTEGER:{
        int v = readInt(in);
        if(wasNull()){
          return null;
        }
        return new Integer(v);
      }
      case Types.BIGINT:{
        long v = readLong(in);
        if(wasNull()){
          return null;
        }
        return new Long(v);
      }
      case Types.FLOAT:
      case Types.DECIMAL:
      case Types.DOUBLE:
      case Types.NUMERIC:
      case Types.REAL:{
        //新格式double使用String存储
        return readString(in);
      }
      case Types.DATE:{
        return readDate(in);
      }
      case Types.TIME:
        return readTime(in);
      case Types.TIMESTAMP:
        return readTimestamp(in);
      case Types.BLOB:
      case Types.LONGVARBINARY:
      case Types.VARBINARY:
      case Types.BINARY:
        return readBlob();
      case Types.CLOB:
      case Types.LONGVARCHAR:
        return readClob(in);//返回字符串
      default:
        return readString(in);
    }
  }
  /**
   * 采用临时文件存储blob流；
   * 原来的程序将流全部读入内存，不妥；
   * 20090227
   * 优化了BlobFileCacher的实现，现在可以直接引用getInputStream()；
   * @return
   * @throws IOException
   */
  private InputStream readBlob() throws IOException {
    if(blobcacher==null)
      blobcacher = new BlobFileCacher(DefaultTempFileFactory.getInstance().createTempFile("blobcache"));
    /**
     * 从blobcacher获取一个写入流，写入数据；
     */
    OutputStream out = blobcacher.beginWriteBlob();
    readBlob(in,out);
    return blobcacher.endWriteBlob().getInputStream();
    //下面注释的方法也可以
    /*BlobValue blob  = blobcacher.createBlob(readBlob(in));
    return blob;*/
  }

  public boolean next() throws Exception {
    if(meta==null)
      init();
    if(count-->0){
      readRow();
      return true;
    }
    row = null;
    return false;
  }

  private Object[] readRow() throws Exception {
    if(row==null) row = new Object[meta.getColumnCount()];
    for(int i=0;i<meta.getColumnCount();i++){
      row[i] = getColumn(i); 
    }
    return row;
  }

}
