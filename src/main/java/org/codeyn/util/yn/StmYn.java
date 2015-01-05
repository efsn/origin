package org.codeyn.util.yn;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.CharArrayWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.i18n.I18N;
import org.codeyn.util.io.MyByteArrayInputStream;
import org.codeyn.util.io.UnicodeReader;
import org.codeyn.util.zip.gzip.GzipInStm;

import com.esen.io.MyByteArrayOutputStream;


public final class StmYn {
  public static final byte[] NULLBYTE = new byte[0];
  
  private StmYn() {
  }
  
  /**
   * 获取一个文件的Reader,自动猜测其编码；
   * @param filepath
   * @return
   * @throws IOException
   */
  public static Reader toReader(String filepath) throws IOException{
    return toReader(filepath,null);
  }
  /**
   * 获取一个文件的Reader，如果指定的编码charset为空，则猜测其编码；
   * 如果是utf8的编码，此方法能够处理微软的bom标示；
   * 主要用于读取csv格式文件；
   * @param filepath
   *       文件路径，如果不是文件，比如目录，则返回null；
   * @param charset
   *       指定文件编码，可以为空；
   * @return 
   *      返回的是InputStreamReader实现；
   * @throws IOException
   */
  public static Reader toReader(String filepath,String charset) throws IOException{
    File f = new File(filepath);
    if(!f.exists()||!f.isFile()){
      return null;
    }
    /**
     * 20090722 BI-597
     * 导入的csv格式文件比较通用，这里需要确定文件的编码，要不然就会有乱码问题；
     */
    if(charset==null||charset.length()==0)
       charset = StrYn.UTF8;//Chardet.detCharset(filepath);
    Reader in = null;
    if(charset!=null){
      /**
       * 20090820 BI-2310
       * 如果分析文件的编码是gb2312，则最好将其转换成gbk；
       * 因为有些中文字gb2312无法表示，是乱码，比如“李宝璠” 里面的“璠” 字，用gb2312表示是乱码；
       * gbk的中文容量要大写，能够正确的表示；
       * 这个帖子最后复测时，原因就是碰到“李宝璠” ,使用gb2312是乱码，写入sybasease报
       * Error converting characters into server's character set. Some character(s) could not be converted 异常；
       * 改为gbk编码后能正常导入；
       */
      if(charset.equalsIgnoreCase("GB2312")){
        charset = "GBK";
      }
      /**
       * 20090807 BI-2310
       * csv格式文件的编码可能是utf-8 , 微软对utf-8编码的文本文件在文件头加了BOM标识；
       * 原来的代码读取文件后，第一个字段的值的开头包含了BOM标识，是乱码，导入Oracle时显示? ， 
       * 导入SybaseASE，出现Error converting characters into server's character set. Some character(s) could not be converted. 的异常；
       * 解决： 通过UnicodeReader来读取，过滤BOM标识；
       */
      in = new UnicodeReader(new FileInputStream(f),charset);
    }else{
            /*
             * ESENFACE-1079: add by liujin 2014.08.28 
             * 因为该 bug 的修改，导出的 UTF-8 编码的 CSV 文件中添加了文件头 '\ufeff' 以便 excel、记事本等工具识别文件编码。
             * 但如果该文件中开始一段特定长度的内容中没有中文时，在此处会无法识别文件编码 针对这种情况的文件当做 UTF-8 编码做特殊处理
             */
            FileInputStream fi = new FileInputStream(filepath);
            try {
                byte[] buf = new byte[3];
                int len = 0;

                len = fi.read(buf, 0, buf.length);
                if (len != 0 && buf[0] == (byte) 0xEF && buf[1] == (byte) 0xBB
                        && buf[2] == (byte) 0xBF) {
                    charset = "UTF-8";
                }
            } finally {
                fi.close();
            }

            if (charset != null) {
                in = new UnicodeReader(new FileInputStream(f), charset);
            } else {
                // 无法获取编码，使用默认的编码；
                in = new InputStreamReader(new FileInputStream(f));
            }
    }
    return in;
  }
  /**
   * 读取一个以零字符结尾的字符串
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param in
   * @return
   * @throws IOException
   */
  static public final String readString(InputStream in) throws IOException {
    return readString(in, 50, 50);
  }
  
    /**
     * 使用指定编码读取一个以零字符结尾的字符串
     * @param in  
     * @param charset  设定编码
     * @return
     * @throws IOException
     */
    static public final String readString(InputStream in, String charset) throws IOException {
        return readString(in, 50, 50, charset);
    }

  /**
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param in  读取的流
   * @param bufsize  读取的字节的大小
   * @param step 每次读取多少个字节
   * @return
   * @throws IOException
   */
    static public final String readString(InputStream in, int bufsize, int step) throws IOException {
        return readString(in, bufsize, step, null);
    }

    /**
     * 使用指定编码读取流中的信息，以byte数组形式读取，可以设置byte数组读取的位置
     * @param in 读取的流
     * @param bufsize 读取的字节的大小
     * @param step 每次读取多少个字节
     * @param charset 编码
     * @return
     * @throws IOException
     */
    static public final String readString(InputStream in, int bufsize, int step, String charset) throws IOException {
        int rd = in.read();
        if (rd == -1)
            return null;
        byte r = (byte) rd;
        int i = 0;
        int l = bufsize;
        byte[] buf = new byte[l];
        while (r != 0) {
            if (i >= l - 1) {
                l += step;
                byte[] old = buf;
                buf = new byte[l];
                System.arraycopy(old, 0, buf, 0, old.length);
            }
            buf[i++] = r;
            rd = in.read();
            if (rd == -1)
                break;
            r = (byte) rd;
        }
        return StrYn.isNull(charset) ? new String(buf, 0, i) : new String(buf, 0, i, charset);
    }
  
  /**
   * 从流中读字符时，应该指定编码
   * TODO 增加encoding参数。
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param in 读取的流
   * @param bufsize 读取的字节的大小
   * @param rate 读取的尺度
   * @return
   * @throws IOException
   */
  static public final String readString(InputStream in, int bufsize, double rate)
      throws IOException {
    int rd = in.read();
    if (rd == -1)
      return null;
    byte r = (byte) rd;
    int i = 0;
    int l = bufsize;
    byte[] buf = new byte[l];
    while (r != 0) {
      if (i >= l - 1) {
        l = (int) Math.floor(l * rate);
        byte[] old = buf;
        buf = new byte[l];
        System.arraycopy(old, 0, buf, 0, old.length);
      }
      buf[i++] = r;
      rd = in.read();
      if (rd == -1)
        break;
      r = (byte) rd;
    }
    return new String(buf, 0, i);
  }

  /**
   * 向流里写一个以零字符结尾的字符串
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param out 
   * @param s 写入的字符串
   * @throws IOException
   */
  static public final void writeString(OutputStream out, String s)
      throws IOException {
    if (s == null) {
      out.write(0);
      return;
    }
    byte[] ba = s.getBytes();
    out.write(ba);
    out.write(0); //??为什么要加个0，谁能解释下？
  }

  static public final String readPassword(OutputStream out, InputStream in)
      throws IOException {
    int rd = in.read();
    if (rd == -1)
      return null;
    byte r = (byte) rd;
    int i = 0;
    int l = 50;
    byte[] buf = new byte[l];
    while (r != '\n') {
      if (i >= l - 1) {
        l += 50;
        byte[] old = buf;
        buf = new byte[l];
        System.arraycopy(old, 0, buf, 0, old.length);
      }
      if (r != '\r') {
        buf[i++] = r;
        out.write('\b');
        out.write('*');
        out.flush();
      }
      rd = in.read();
      if (rd == -1)
        break;
      r = (byte) rd;
    }
    return new String(buf, 0, i);
  }
  
  /**
   * 将对象obj序列化到输出流o中，obj必须是支持序列化的
   */
  public static void writeObject(OutputStream o, Object obj) throws IOException {
    ObjectOutputStream oo = new ObjectOutputStream(o);
    try {
      oo.writeObject(obj);
    }
    finally {
      oo.close();
    }
  }
  
  public static Object readObject(InputStream o) throws IOException, ClassNotFoundException {
    ObjectInputStream oo = new ObjectInputStream(o);
    try {
      return oo.readObject();
    }
    finally {
      oo.close();
    }
  }
  
  /**
   * 从文件中反序列化对象并返回，如果出现异常转换为runtime异常抛出
   */
  public static Object file2obj(String fn) {
    try {
      InputStream f = new BufferedInputStream(new FileInputStream(fn));
      try {
        return stm2obj(f);
      }
      finally {
        f.close();
      }
    }
    catch (Exception ex1) {
      ExceptionHandler.rethrowRuntimeException(ex1);
      return null;
    }
  }

  public static Object stm2obj(InputStream f) throws IOException, ClassNotFoundException {
    ObjectInputStream oo = new ObjectInputStream(f);
    try{
      return oo.readObject();
    }finally{
      oo.close();
    }
  }
  
  /**
   * 将对象o序列化到文件fn中，如果出现异常转换为runtime异常抛出
   */
  public static void obj2file(String fn, Object o) {
    try {
      OutputStream fo = new BufferedOutputStream(new FileOutputStream(fn));
      try {
        obj2stm(o, fo);
      }
      finally {
        fo.close();
      }
    }
    catch (IOException ex1) {
      ExceptionHandler.rethrowRuntimeException(ex1);
    }
  }

  public static void obj2stm(Object o, OutputStream fo) throws IOException {
    ObjectOutputStream oo = new ObjectOutputStream(fo);
    try{
        oo.writeObject(o);
    }catch(Exception e){
        /**
         * 20100903 又发现了非预期的异常：
         *              java.lang.RuntimeException: com.esen.bi.dw.meta.impl.DWSubject_Alias_Temp
                        Caused by: java.io.NotSerializableException: com.esen.bi.dw.meta.impl.DWSubject_Alias_Temp
                    不晓得是哪个对象引用了com.esen.bi.dw.meta.impl.DWSubject_Alias_Temp。
                    这里在打印出出异常的对象，方便查问题。
         *          
         */
        //ExceptionHandler.rethrowRuntimeException(e, "在序列化对象【"+o.getClass()+"】时发生异常："+StrFunc.exceptionMsg2str(e));
        ExceptionHandler.rethrowRuntimeException(e, I18N.getString("com.esen.util.stmfunc.exp", "在序列化对象【{0}】时发生异常：{1}",new Object[]{o.getClass(),StrYn.exceptionMsg2str(e)}));
    }
    oo.close();
  }

    /**
     * read fix length bytes from inputstream
     * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
     * @param in
     * @param fix
     * @return
     * @throws IOException
     */
    static public final String readFix(InputStream in, int fix) throws IOException {
        if (fix <= 0)
            return null;
        byte[] bb = new byte[fix];
        stmTryRead(in, bb, fix);
        return new String(bb);
    }
    
    /**
     * read fix length bytes from inputstream
     * @param in
     * @param fix
     * @return
     * @throws IOException
     */
    static public final String readFix(InputStream in, int fix, String charset) throws IOException {
        if (fix <= 0)
            return null;
        byte[] bb = new byte[fix];
        stmTryRead(in, bb, fix);
        return new String(bb, charset);
    }
    
  /**
   * 从流中读取一行,采用操作系统默认编码对字节编码
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param in
   * @return
   * @throws IOException
   */
  static public final String readLine(InputStream in) throws IOException {
    return readLine(in, null);
  }
  
  /**
   * 从流中读取一行
   * @param in
   * @param charset 以charset对编码为字符串,如果为空,则采用操作系统默认编码
   * @return
   * @throws IOException
   */
  static public final String readLine(InputStream in,String charset) throws IOException {
    int rd = in.read();
    if (rd == -1)
      return null;
    byte r = (byte) rd;
    int i = 0;
    int l = 50;
    byte[] buf = new byte[l];
    while (r != '\n') {
      if (i >= l - 1) {
        l += 50;
        byte[] old = buf;
        buf = new byte[l];
        System.arraycopy(old, 0, buf, 0, old.length);
      }
      if (r != '\r')
        buf[i++] = r;
      rd = in.read();
      if (rd == -1)
        break;
      r = (byte) rd;
    }
    if (charset == null) {
      return new String(buf, 0, i);
    }
    return new String(buf, 0, i, charset);
  }
  
  /**
   * 向流里写一行，即以回车换行为结尾
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param out
   * @param s
   * @throws IOException
   */
  static public final void writeLine(OutputStream out, String s)
      throws IOException {
    writeFix(out, s);
    out.write('\r');
    out.write('\n');
  }

  /**
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
 * @param out
 * @param s
 * @throws IOException
 */
  static public final void writeFix(OutputStream out, String s)
      throws IOException {
    if (s == null || s.length() == 0)
      return;
    byte[] b = s.getBytes();
    out.write(b);
  }
  
  static public final void writeLine(OutputStream out, String s, String charsetName) throws IOException {
    writeFix(out, s, charsetName);
    out.write('\r');
    out.write('\n');
  }
  
  /**
   * 按指定编码写字符串的内容,如果编码为null这使用系统缺省编码
   */
  static public final void writeFix(OutputStream out, String s, String charsetName)
      throws IOException {
    if (s == null || s.length() == 0)
      return;
    byte[] b = charsetName!=null?s.getBytes(charsetName):s.getBytes();
    out.write(b);
  }
  
  //write fix length string to outputstream
  static public final void writeFix(OutputStream out, String s, int fix)
      throws IOException {
    if (fix <= 0)
      return;
    byte[] b = s.getBytes();
    if (b.length < fix)
      throw new IOException();
    out.write(b, 0, fix);
  }

  /**
   * 读取行，直到读到某行为止,以操作系统默认编码对字节编码
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param br
   * @param ln
   * @return
   * @throws Exception
   */
  static public final String readLinesUntil(InputStream br, String ln) throws Exception {
    return readLinesUntil(br, ln, null);
  }
  
  /**
   * 读取行，直到读到某行为止
   * 以指定编码解码,如果为空,则以系统默认编码解码
   * 此问题主要是解决读取npf文件时必须以gbk解码的问题
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param br
   * @param ln
   * @return
   * @throws Exception
   */
  static public final String readLinesUntil(InputStream br, String ln, String charset) throws Exception {
    StringBuffer result = new StringBuffer();
    String s = readLine(br, charset);
    while ((s != null) && !s.equals(ln)) {
      result.append(s);
      s = readLine(br, charset);
      if ((s != null) && !s.equals(ln))
        result.append("\r\n");
    }
    return result.toString();
  }

  /**
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param br
   * @param ln
   * @throws Exception
   */
  static public final void skipLinesUntil(InputStream br, String ln)
      throws Exception {
    String s = readLine(br);
    while ((s != null) && !s.equals(ln)) {
      s = readLine(br);
    }
  }

  static public final int readInt(InputStream i) throws IOException,
      EOFException {
    InputStream in = i;
    int ch1 = in.read();
    int ch2 = in.read();
    int ch3 = in.read();
    int ch4 = in.read();
    if ((ch1 | ch2 | ch3 | ch4) < 0)
      throw new EOFException();
    return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
  }

  static public final long readLong(InputStream i) throws IOException {
    return ((long) (readInt(i)) << 32) + (readInt(i) & 0xFFFFFFFFL);
  }

  static public final double readDouble(InputStream i) throws IOException {
    return Double.longBitsToDouble(readLong(i));
  }

  static public final float readFloat(InputStream i) throws IOException {
    return Float.intBitsToFloat(readInt(i));
  }

  static public final void writeDouble(OutputStream o, double v)
      throws IOException {
    writeLong(o, Double.doubleToLongBits(v));
  }

  static public final void writeInt(OutputStream o, int v) throws IOException {
    OutputStream out = o;
    out.write((v >>> 24) & 0xFF);
    out.write((v >>> 16) & 0xFF);
    out.write((v >>> 8) & 0xFF);
    out.write((v >>> 0) & 0xFF);
  }

  static public final void writeFloat(OutputStream o, float v)
      throws IOException {
    writeInt(o, Float.floatToIntBits(v));
  }

  static public final void writeLong(OutputStream o, long v) throws IOException {
    OutputStream out = o;
    out.write((int) (v >>> 56) & 0xFF);
    out.write((int) (v >>> 48) & 0xFF);
    out.write((int) (v >>> 40) & 0xFF);
    out.write((int) (v >>> 32) & 0xFF);
    out.write((int) (v >>> 24) & 0xFF);
    out.write((int) (v >>> 16) & 0xFF);
    out.write((int) (v >>> 8) & 0xFF);
    out.write((int) (v >>> 0) & 0xFF);
  }

  static public final int stmCopyFrom(String fn, OutputStream out)
      throws IOException {
    InputStream in = new FileInputStream(fn);
    try {
      return stmTryCopyFrom(in, out);
    }
    finally {
      in.close();
    }
  }

  /**
   * 从一个流中复制指定的长度的类容到另一个流中,如果从源流中不能再读入数据则返回复制了的数据的字节数
   */
  static private final int BUF_SIZE = 1024 * 8;

  static public final long stmCopyFrom(InputStream in, OutputStream out, long sz)
      throws IOException {
    byte[] buf = new byte[BUF_SIZE];
    long rst = 0;
    int r;
    while (sz > 0) {
      r = (int) (sz > BUF_SIZE ? BUF_SIZE : sz);
      r = in.read(buf, 0, r);
      if (r < 1)
        break;
      sz -= r;     
      out.write(buf, 0, r);
      rst += r;
    }
    return rst;
  }

  /**
   * 将流中的所有信息读出并以byte数组的形式返回
   */
  static public final byte[] stm2bytes(InputStream in) throws IOException {
    if (in instanceof MyByteArrayInputStream){
      MyByteArrayInputStream min = (MyByteArrayInputStream)in;
      byte[] r = new byte[min.available()];
      System.arraycopy(min.getBuf(),min.getPos(),r,0,r.length);
      return r;
    }
    int available = in.available();
    if (available<=0) available = 5*1024;
    MyByteArrayOutputStream out = new MyByteArrayOutputStream(available);
    stmTryCopyFrom(in, out);
    //如果out.getBuf().length==out.size()那么直接返回buf即可，不必再次复制内存块
    return out.getBuf().length==out.size()?out.getBuf():out.toByteArray();
  }

  /**
   * 将流中的所有信息读出并以字符串的形式返回
   * @deprecated 此函数使用系统的编码，不稳定，应该指定明确的编码
   * @param in
   * @return
   * @throws IOException
   */
  static public final String stm2Str(InputStream in) throws IOException {
    return new String(stm2bytes(in));
  }

  static public final String stm2Str(InputStream in, String charsetName) throws IOException {
    return new String(stm2bytes(in), charsetName);
  }

  static public final int stmTryCopyFrom(InputStream in, OutputStream out)
      throws IOException {
    if (in instanceof MyByteArrayInputStream){
      MyByteArrayInputStream min = (MyByteArrayInputStream)in;
      out.write(min.getBuf(), min.getPos(), min.available());
      return min.available();
    }
    byte[] buf = new byte[BUF_SIZE];
    int sz = 0;
    int r;
    while ((r = in.read(buf)) != -1) {
      sz += r;
      out.write(buf, 0, r);
    }
    return sz;
  }
  
  /**
   * 将流reader的内容拷贝到writer中
   * @param reader
   * @param writer
   * @return
   * @throws IOException
   */
  static public final int stmTryCopyFrom(Reader reader, Writer writer) throws IOException {
    char[] buffer = new char[BUF_SIZE];
    int mark = 0;
    int size = 0;
    while ((mark = reader.read(buffer)) != -1) {
      size += mark;
      writer.write(buffer, 0, mark);
    }
    return size;
  }
  
  static public final long stmTryCopyFrom(InputStream in, RandomAccessFile out) throws IOException {
    if (in instanceof MyByteArrayInputStream) {
      MyByteArrayInputStream min = (MyByteArrayInputStream) in;
      out.write(min.getBuf(), min.getPos(), min.available());
      return min.available();
    }
    byte[] buf = new byte[BUF_SIZE];
    long sz = 0;
    int r;
    while ((r = in.read(buf)) != -1) {
      sz += r;
      out.write(buf, 0, r);
    }
    return sz;
  }
  /**
   * 判断给定的流是否是gzip格式的流，如果in是markSupported的，那么此函数不会改变in的当前位置。
   */
  static public final boolean isGzipStm(InputStream in) throws IOException{
    boolean ms = in.markSupported();
    if (ms) in.mark(10);
    int b1 = in.read();
    int b2 = in.read();
    if (ms) in.reset();
    return ((b2 << 8 | b1) == GZIPInputStream.GZIP_MAGIC);
  }

  /**
   * 将bytes用gzip压缩并返回压缩后的byte，byte如果是null则触发空指针异常
   * @throws IOException 
   */
  static public final byte[] gzipBytes(byte[] bytes) throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    GZIPOutputStream gout = new GZIPOutputStream(out);
    try {
      gout.write(bytes);
    }
    finally {
      gout.close();
    }
    return out.toByteArray();
  }

  /**
   * 将bytes用gzip解压缩并返回解压缩后的byte，byte如果是null则触发空指针异常
   * @throws IOException 
   */
  static public final byte[] ungzipBytes(byte[] bytes) throws IOException {
    ByteArrayInputStream in = new ByteArrayInputStream(bytes);
    GZIPInputStream gin = new GZIPInputStream(in);
    try {
      return stm2bytes(gin);
    }
    finally {
      gin.close();
    }
  }

  /**
   * 将in中的类容用gzip压缩并返回压缩后的byte，in如果是null则触发空指针异常
   * @throws IOException 
   */
  static public final byte[] gzipStm(InputStream in) throws IOException {
    MyByteArrayOutputStream out = new MyByteArrayOutputStream(
        in.available() > 1 ? in.available() : 1024);
    GZIPOutputStream gout = new GZIPOutputStream(out);
    try {
      StmYn.stmTryCopyFrom(in, gout);
    }
    finally {
      gout.close();
    }
    return out.toByteArray();
  }

  /**
   * 将in中的类容用gzip解压缩并返回解压缩后的byte，in如果是null则触发空指针异常
   * @throws IOException 
   */
  static public final byte[] ungzipStm(InputStream in) throws IOException {
    return ungzipBytes(stm2bytes(in));
  }

  public static void main(String[] args) {
    java.io.FileOutputStream fs = null;
    try {
      fs = new java.io.FileOutputStream("c:/1.txt");
      byte[] aa = "abcdefg".getBytes();
      //      bytesToHexString(fs,aa);
      //      writeString(fs,"sdfasdfgsdfffffff你好fdhdfghhhhhhhhhhhhhhhhhhhhhdfghdf");
      fs.close();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
    finally {

    }
  }

  public static final int stmTryRead(InputStream in, byte[] bb)
      throws IOException {
    if ((in == null) || (bb == null) || (bb.length == 0))
      return 0;
    return stmTryRead(in, bb, bb.length);
  }

  public static final int stmTryRead(InputStream in, byte[] bb, int len)
      throws IOException {
    return stmTryRead(in, bb, 0, len);
  }

  public static int stmTryRead(InputStream in, byte[] bb, int off, int len)
      throws IOException {
    int r;
    int l = 0;
    int t = off;
    while ((r = in.read(bb, t, len - l)) >= 0) {
      t += r;
      l += r;
      if (l == len) {
        break;
      }
    }
    return l;
  }

  /**
   * 返回一个没有压缩过的流，由调用处关闭连接，必须关闭，否则可能会出现异常。
   * @param in
   * @return
   * @throws IOException
   */
  public static final InputStream getUnGZIPStm(InputStream in) throws IOException {
    if (in == null) {
      return null;
    }
    in = new BufferedInputStream(in); //支持mark操作
    int GZIP_MAGIC = 0x8b1f;//gizp
    in.mark(10);
    int ch1 = in.read();//如果流中没有数据了，会返回-1不会抛出异常
    int ch2 = in.read();
    in.reset();
    if ((ch2 << 8) + (ch1 << 0) == GZIP_MAGIC) {
      /**
       * 在1.4中GZIPInputStream的mark实现的问题,情况如下(不考虑流的关闭问题):
    ByteArrayInputStream bin = new ByteArrayInputStream(StmFunc.gzipBytes("abc123".getBytes()));
    InputStream in = new GZIPInputStream(new BufferedInputStream(bin));
    if (!in.markSupported()) {
      in = new BufferedInputStream(in);
    }
    in.mark(10);
    byte[] bs = new byte[3];
    in.read(bs);
    in.reset();
    byte[] rs = StmFunc.stm2bytes(in);
    System.out.println(rs.length + "(" + new String(rs) + ")");
       * 
       * 最后输出的结果为3(123)
       * 
       * 在1.4中in.markSupported()返回true,1.5中是返回false
       * 在1.4中最后输出是3(),1.5中返回的是6(abc123)
       * 
       * 这里对GZIPInputStream进行封装,在外部调用可以直接调用mark,不用再次封装
       * 此问题是在weblogic81下载入主题表时发现的,载入时出现的异常为:invalid distance too far back
       */
      return new BufferedInputStream(new GZIPInputStream(in));
    }
    return in;
  }
  
  /**
   * 返回一个压缩过的流,由调用处关闭连接,必须关闭,否则可能会出现异常
   * @param in
   * @return
   * @throws IOException
   */
  public static final InputStream getGZIPStm(InputStream in) throws IOException {
    if (in == null) {
      return null;
    }
    if (!in.markSupported()) {
      in = new BufferedInputStream(in);//支持mark操作
    }
    int GZIP_MAGIC = 0x8b1f;//gizp
    in.mark(10);
    int ch1 = in.read();//如果流中没有数据了，会返回-1不会抛出异常
    int ch2 = in.read();
    in.reset();
    if ((ch2 << 8) + (ch1 << 0) == GZIP_MAGIC) {
      return in;//已经是gzip
    }
    return new GzipInStm(in);
  }

  //reader
  public static Reader str2reader(String str) {
    if (str == null) {
      return null;
    }
    return new CharArrayReader(str.toCharArray());
  }
  public static String reader2str(Reader reader,boolean needCloseStm) throws IOException {
    if (reader == null) {
      return null;
    }
    return new String(reader2chars(reader,needCloseStm));
  }
  public static String reader2str(Reader reader) throws IOException {
    return reader2str(reader,true);
  }

  /**
   * 将Reader中的信息读出,并以char数组的形式返回
   * 
   * @param reader
   * @param needCloseStm 
   * @return
   * @throws IOException
   */
  public static char[] reader2chars(Reader reader, boolean needCloseStm) throws IOException {
    CharArrayWriter writer = new CharArrayWriter();
    reader2writer(reader, writer);
    if(needCloseStm)
      reader.close();
    return writer.toCharArray();
  }

  public static int reader2writer(Reader reader, Writer writer)
      throws IOException {
    char[] buf = new char[BUF_SIZE];
    int sz = 0;
    int r;
    while ((r = reader.read(buf)) != -1) {
      sz += r;
      writer.write(buf, 0, r);
    }
    return sz;
  }

  public static final int readerTryRead(Reader reader, char[] cc, int len)
      throws IOException {
    int r;
    int t = 0;
    while ((r = reader.read(cc, t, len - t)) >= 0) {
      t += r;
      if (t == len) {
        break;
      }
    }
    return t;
  }

  public static int readerTryRead(Reader reader, char[] cc) throws IOException {
    if (reader == null || cc == null || cc.length == 0) {
      return 0;
    }
    return readerTryRead(reader, cc, cc.length);
  }

    /**
     * 跳过指定字节读取文件
     * @param in 输入的流
     * @param len 跳过的字节长度
     * @return
     * @throws IOException
     */
  public static long skip(InputStream in, long len) throws IOException {
    if (len <= 0) {
      return 0;
    }
    long r;
    long t = 0;
    while ((r = in.skip(len - t)) != 0) {
      t += r;
      if (t == len) {
        break;
      }
    }
    return t;
  }
  
  static public final void writeInt(final Writer writer, int i) throws IOException {
    //原来用write(String.valueOf(i));，但是连续写入很多整形时，很耗费内存
    if (i<0) writer.write('-');
    i = Math.abs(i);
    if (i<10){
      writer.write((char)(i+'0'));
      return;
    }
    for (int j = StrYn.TEN_POWERS.length-1; j >=0 ; j--) {
      int v = i/StrYn.TEN_POWERS[j];
      if (v>0){
        writer.write((char)(v%10+'0'));
      }
    }
  }
  
  
  /**
   * 将escape内容直接编码到writer中，避免浪费内存，类似StrFunc.escape函数，但此函数的实现不会产生临时对象
   */
  public static int escape(final CharSequence s, Writer out) throws IOException {
    if (s == null || s.length() == 0) {
      return 0;
    }
    int len = s.length();
    int r = 0;
    for (int i = 0; i < len; i++) {
      char ch = s.charAt(i);
      if (StrYn.needEscape(ch)){
        out.write((char) ch);
        r++;
      }
      else if (ch <= 0x007F) { // other ASCII : map to %XX
        out.write('%');
        out.write(StrYn.HEX_0_FF[ch]);
        r+=3;
      }
      else { // unicode : map to %uXXXX
        out.write('%');
        out.write('u');
        out.write(StrYn.HEX_0_FF[(ch >>> 8)]);
        out.write(StrYn.HEX_0_FF[(0x00FF & ch)]);
        r+=5;
      }
    }
    return r;
  }
}