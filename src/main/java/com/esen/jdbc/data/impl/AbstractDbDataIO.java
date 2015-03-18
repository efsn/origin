package com.esen.jdbc.data.impl;

import java.io.*;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;

import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

/**
 * 此类是读取和写入*.db文件格式的基类；
 * 用于读写字段值；
 * @author dw
 *
 */
public class AbstractDbDataIO {
  /**
   * 字符串存储时都转换成utf8编码；
   */
  public static final String CHARSET = "UTF-8";
  /**
   * 读写blob流的缓存；
   */
  private byte buf[];
  /**
   * 读写blob流的缓存大小；
   */
  protected final int bufSize = 1024 * 32;
  /**
   * 判断读取的字段值是否为空；
   * 类似ResultSet的同名方法；
   */
  private boolean wasNull;

  public AbstractDbDataIO() {
    super();
  }
  /**
   * 最后读取的字段值是否为空；
   * 调用readXXX(in)后，判断该值是否为空；
   * @return
   */
  protected boolean wasNull(){
    return wasNull;
  }
  /**
   * 写数值类型字段值；
   * 先写标志位，0表示空，1表示有值，2表示NaN
   * 标志位=1，再写该数值；
   * @param w
   * @param o
   * @throws IOException
   */
  protected void saveDouble(OutputStream w, Number o)
      throws IOException {
    if (o == null) {
      writeFlag(w,0);
    }
    else {
      double v = o.doubleValue();
      if (Double.isNaN(v)) {
        writeFlag(w,2);//表示NaN
      }
      else {
        writeFlag(w,1);
        StmFunc.writeDouble(w, v);
      }
    }
  }
  /**
   * 设置标志位；
   * 连续写两个标志位，用于校验文件是否正确；
   * @param w
   * @param b
   * @throws IOException
   */
  private void writeFlag(OutputStream w,int b) throws IOException{
    w.write(b);
    w.write(b);
  }
  /**
   * 读取标志位，并校验正确性；
   * @param in
   * @return
   * @throws IOException 
   */
  private int readFlag(InputStream in) throws IOException{
    int c = in.read();
    int c2 = in.read();
    if(c!=c2)
      throwException();
    return c;
  }
  /**
   * 根据数值写入规则读取数值字段值；
   * @param in
   * @return
   * @throws IOException
   */
  protected double readDouble(InputStream in)throws IOException {
    int c = readFlag(in);
    switch(c){
      case 0:{
        wasNull = true;
        return 0;
      }
      case 1:{
        wasNull = false;
        return StmFunc.readDouble(in);
      }
      case 2:{
        wasNull = false;
        return Double.NaN;
      }
    }
    throwException();
    return 0;
  }

  private void throwException() throws IOException {
    throw new IOException(I18N.getString("com.esen.jdbc.data.impl.abstractdbDataio.errfilepptern", "错误的文件格式；"));
  }
  /**
   * 写入长整型；
   * 先写标志位，0表示空，1表示非空；
   * =1再写该值；
   * @param w
   * @param o
   * @throws IOException
   */
  protected void saveLong(OutputStream w, Number o) throws IOException {
    if (o == null) {
      writeFlag(w,0);
    }
    else {
      writeFlag(w,1);//非空
      StmFunc.writeLong(w, o.longValue());
    }
  }
  /**
   * 读取长整型；
   * @param in
   * @return
   * @throws IOException
   */
  protected long readLong(InputStream in)throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return 0;
    }else if(c==1){
      wasNull = false;
      return StmFunc.readLong(in);
    }
    throwException();
    return 0;
  }
  /**
   * 写入整型；
   * 先写标志位，0表示空，1表示非空；
   * =1再写该值；
   * @param w
   * @param o
   * @throws IOException
   */
  protected void saveInt(OutputStream w, Number o) throws IOException {
    if (o == null) {
      writeFlag(w,0);//int类型为空，使用标志位=0表示空，后面是下一个字段值，非0表示后面存储的是int值；
    }
    else {
      writeFlag(w,1);//非空
      StmFunc.writeInt(w, o.intValue());
    }
  }
  /**
   * 读取整型；
   * @param in
   * @return
   * @throws IOException
   */
  protected int readInt(InputStream in)throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return 0;
    }else if(c==1){
      wasNull = false;
      return StmFunc.readInt(in);
    }
    throwException();
    return 0;
  }
  /**
   * 写blob值；
   * 先写标志位，0表示空，1表示非空；
   * 非空再写流；
   * 可能不知道blob流的长度；
   * 每次读取blob流bufSize大小的buf值，先写读取的长度len，再写读取的buf，值到len<bufSize表示读取完毕；
   * @param w
   * @param o
   * @throws IOException
   */
  protected long saveBlob(OutputStream w, InputStream o) throws IOException {
    if (o == null) {
      writeFlag(w,0);
      return 0;
    }
    else {
      writeFlag(w,1);
      if (buf == null)
        buf = new byte[bufSize];
      int len = 0;
      long l = 0;
      while (true) {
        len = o.read(buf);
        /**
         * 20090514 
         * 对于一个空的流，读取时 这里 len=-1 ，造成后面读取-1长度的流出异常；
         * 解决办法：这里读取的len<=0都表示读取完毕，写标志长度为0，后面读取到长度为0的标志，就不读取后面的流了；
         */
        if(len<=0) {
          StmFunc.writeInt(w, 0);
          break;
        }
        l+=len;
        StmFunc.writeInt(w, len);
        w.write(buf, 0, len);
        if (len < bufSize)
          break;
      }
      return l;
    }
  }
  /**
   * 读取blob流；
   * 根据写入规则将blob流读取，并写入指定的out中；
   * @param in
   * @param out
   * @return
   * @throws IOException
   */
  protected int readBlob(InputStream in,OutputStream out) throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return 0;
    }else if(c==1){
      wasNull = false;
      int len = StmFunc.readInt(in);
      int total = 0;
      if(len>0){
        while(true){
          total+=len;
          StmFunc.stmCopyFrom(in, out, len);
          if(len<bufSize) break;
          len = StmFunc.readInt(in);
        }
        return total;
      }
      return 0;
    }else{
      /**
       * 20091121
       * blob流的长度可能为零或者-1，在saveBlob()中，保持长度标识为0;
       * 这里读取时长度为0的blob时出现了“格式不正确”异常，是程序的问题；
       * 解决：这个异常显然应该是验证标识不对是才抛出；
       * 其他调用throwException()可能也有这个问题，这次一并改之；
       */
      throwException();
    }
    return 0;
  }
  /**
   * 读取blob流；
   * 返回一个供读取blob值的流对象，该流对象与流文件关联，必须读取完毕才能读取后面的值；
   * 此方法实现的有些复杂，以后如需要在优化；
   * @param in
   * @return
   * @throws IOException
   */
  protected InputStream readBlob(InputStream in) throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return null;
    }else if(c==1){
      wasNull = false;
      return new BlobInputStream(in,bufSize);
    }
    return null;
  }
  
  /**
   * 写clob值；
   * 先写标志位，0表示空，1表示非空；
   * 非空再写值；
   * 先转换成字符串，再将其转换成CHARSET编码的byte,再写入流，先写长度再写byte值；
   * @param w
   * @param o
   * @throws IOException
   */
  protected void saveClob(OutputStream w, Reader o) throws IOException {
    if (o == null) {
      writeFlag(w,0);
    }
    else {
      writeFlag(w,1);
      String v = StmFunc.reader2str(o);
      byte[] bb = v.getBytes(CHARSET);
      int len = bb.length;
      writeLength(w, len);
      if (len != 0)
        w.write(bb);
    }
  }
  /**
   * 读取clob
   * @param in
   * @return
   * @throws IOException
   */
  protected String readClob(InputStream in) throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return null;
    }else if(c==1){
      wasNull = false;
      int len = StmFunc.readInt(in);
      byte[] bb = new byte[len];
      in.read(bb);
      return new String(bb,CHARSET);
    }else{
      throwException();
    }
    return null;
  }
  /**
   * 写字符串值；
   * 先写标志位，0表示空，1表示非空；
   * 非空再写值；
   * 将字符串转换成CHARSET编码的btye， 先写长度再写btye值；
   * @param w
   * @param v
   * @return
   * @throws IOException
   */
  protected int saveString(OutputStream w, String v) throws IOException {
    if (v == null){
      writeFlag(w,0);
      return 0;
    }
    else {
      writeFlag(w,1);//非空， 后面len=0就表示空串；
      byte[] bb = v.getBytes(CHARSET);
      int len = bb.length;
      writeLength(w, len);
      if (len != 0)
        w.write(bb);
      return len;
    }
  }
  /**
   * 读取字符串值；
   * @param in
   * @return
   * @throws IOException
   */
  protected String readString(InputStream in) throws IOException {
    int c = readFlag(in);
    if(c==0){
      wasNull = true;
      return null;
    }else if(c==1){
      wasNull = false;
      int len = StmFunc.readInt(in);
      if(len==0) return "";
      byte[] bb = new byte[len];
      in.read(bb);
      return new String(bb,CHARSET);
    }
    throwException();
    return null;
  }
  private void writeLength(OutputStream w, int len) throws IOException {
      StmFunc.writeInt(w, len);
  }
  /**
   * 写日期；
   * 先写标志位，0表示空，1表示非空；
   * 非空再写值；
   * 将日期转换成long值写入；
   * 这里的Date值可以是java.sql.Date,Time,Timestamp
   * @param w
   * @param t
   * @throws IOException
   */
  protected void saveDate(OutputStream w, java.util.Date t) throws IOException {
    if (t == null) {
      writeFlag(w,0);
    }
    else {
      writeFlag(w,1);//非空
      StmFunc.writeLong(w, t.getTime());
    }
  }
  /**
   * 读取日期；
   * @param in
   * @return
   * @throws IOException
   */
  protected Date readDate(InputStream in) throws IOException {
    long t = readLong(in);
    if(wasNull) return null;
    return new Date(t);
  }
  /**
   * 读取时间值；
   * @param in
   * @return
   * @throws IOException
   */
  protected Time readTime(InputStream in) throws IOException {
    long t = readLong(in);
    if(wasNull) return null;
    return new Time(t);
  }
  /**
   * 读取timestmap
   * @param in
   * @return
   * @throws IOException
   */
  protected Timestamp readTimestamp(InputStream in) throws IOException {
    long t = readLong(in);
    if(wasNull) return null;
    return new Timestamp(t);
  }
}
class BlobInputStream extends InputStream{
  private InputStream in;
  private int bufsize;
  private int p;
  private int len;
  /**
   * 将保存在*.db这里是in流的 bolb值，读取出来；
   * 保存格式：bolb流按bufSize长度分段，每段先保存bufSize值，在保存bufSize长度的流，
   * 最后一段流长度必小于bufSize；
   * 通过判断读取每段开头的长度len<bufSize来确定是否终止读取；
   * @param in
   * @param bufsize
   * @throws IOException 
   * @throws EOFException 
   */
  protected BlobInputStream(InputStream in,int bufsize) throws EOFException, IOException{
    this.in = in;
    this.bufsize = bufsize;
    this.len = StmFunc.readInt(in);
    this.p = 0;
  }
  public int read() throws IOException {
    if (p++ < len)
      return in.read();
    if(len<bufsize){
      return -1;
    }
    len = StmFunc.readInt(in);
    p = 0;
    return read();
  }
  public int read(byte b[]) throws IOException {
    return read(b, 0, b.length);
  }
  public int read(byte b[], int off, int length) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    else if ((off < 0) || (off > b.length) || (length < 0) || ((off + length) > b.length)) {
      throw new IndexOutOfBoundsException();
    }
    else if (length == 0) {
      return 0;
    }
    if (p == -1)
      return -1;
    if (bufsize < length) {
      int l = len - p;
      if (l > 0) {
        int l2 = 0;//记录写b数组的起始位置；
        while (l <= length) {
          int l3 = in.read(b, off + l2, l - l2);
          if (len < bufsize) {
            p = -1;//读取完毕；
            return l3;
          }
          len = StmFunc.readInt(in);
          l2 += l3;
          l += len;
        }
        int k = len - (l - length);
        if (k == 0)
          return l2;
        if (k > 0) {
          p = in.read(b, off + l2, k);
          return p + l2;
        }
      }
    }else{
      int l = len-p;
      if(l>length){
        int l2 = in.read(b, off , length);
        p+=l2;
        return l2;
      }else{
        int k2 = in.read(b, off , l);
        if(len<bufsize){
          p = -1;
          return k2;
        }
        len = StmFunc.readInt(in);
        int k = length-l;
        p = in.read(b, off+k2 , k);
        return k2+p;
      }
    }
    return -1;
  }
  
  
}
