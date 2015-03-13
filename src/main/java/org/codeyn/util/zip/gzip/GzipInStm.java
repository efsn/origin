package org.codeyn.util.zip.gzip;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPOutputStream;

import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.io.MyByteArrayOutputStream;
import org.codeyn.util.yn.StrmUtil;

/**
 * 通过此类封装的流对象,读取的内容为Gzip压缩后内容
 * 即此类的等价效果为:
 *     InputStream src = ....
 *     byte[] b = StmFunc.gzipStm(src);
 *     return new ByteArrayInputStream(b);
 *     
 * 如果已经确定流的内容不会很大,可以直接用上面的方式转换,这样处理的限制是流的内容不能很大,否则会出现内存溢出
 * 如果流的大小不确定,则需要用此类来处理.此类不会因为流的内容很大,而会出现内存溢出的问题
 * 
 * 此类的处理方式:
 *    从此类读取内容时,始终是从内存中读取的,如果内存中没有内容,会从原始流中读取一部分内容放到内存中
 *    这样就避免了一次读取到内存的内容太多导致的内存溢出
 * @author zcx
 */
public class GzipInStm extends InputStream {
  private static final int BUF_SIZE = 8 * 1024;//每次从原始流中读取的内容大小

  private InputStream oriin;//原始流

  private InputStream rin;//结果流,存放的是gzip压缩后的内容,只能只是全部内容的一部分,读取压缩后的内容就是从此流中读取的.当这部分被读取完后,会再次将后面的内容存放到此流中

  private GZIPOutputStream gout;//原始流的内容通过此压缩流压缩

  private MyByteArrayOutputStream bout;//压缩后的内容会存放在此流中,然后会将此流的内容转给结果流

  private boolean init = false;//是否初始化参数

  private boolean isend = false;//是否读取完成.在判断是否结束时不能以从结果流中读取到-1来判断,因为结果流中可能只有一部分数据,所以增加此变量

  private Exception ex;//异常信息

  public GzipInStm(InputStream in) {
    this.oriin = in;
  }

  public int read() throws IOException {
    if (this.oriin == null)
      return -1;
    initStm();
    int c = this.rin.read();
    while (c == -1 && !this.isend) {
      readNextData();
      c = this.rin.read();
    }
    return c;
  }

  public int read(byte b[], int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0)) {
      throw new IndexOutOfBoundsException();
    }
    else if (len == 0) {
      return 0;
    }
    if (this.oriin == null)
      return -1;
    initStm();
    int rlen = 0;
    while (true) {//循环读取内容
      rlen += StrmUtil.stmTryRead(this.rin, b, off + rlen, len - rlen);
      if (rlen == len || this.isend)
        break;
      readNextData();
    }
    if (rlen == 0)
      return -1;//数据读完了.前面已经判断了len=0的情况,所以这里如果rlen=0,表示数据已经读完
    return rlen;
  }

  public long skip(long n) throws IOException {
    //支持此方法无意义
    throw new IOException("skip not supported");
  }

  /**
   * 初始化参数,并压缩一部分内容放在结果流中
   */
  private void initStm() throws IOException {
    if (init == true)
      return;
    bout = new MyByteArrayOutputStream(BUF_SIZE);
    gout = new GZIPOutputStream(bout);
    //此处不能调用readNextData,因为readNextData中会重置bout
    StrmUtil.stmCopyFrom(this.oriin, gout, BUF_SIZE);
    this.rin = bout.asInputStream();
    init = true;
  }

  /**
   * 读取一部分内容放到结果流中
   */
  private void readNextData() throws IOException {
    bout.reset();
    long r = 0;
    while (bout.size() == 0) {
      r = StrmUtil.stmCopyFrom(this.oriin, gout, BUF_SIZE);
      if (r == 0) {
        gout.finish();
        gout.close();
        isend = true;
        break;
      }
    }
    this.rin = bout.asInputStream();
  }

  /**
   * 关闭流
   */
  public void close() throws IOException {
    if (gout != null) {
      try {
        gout.close();
      }
      catch (Exception e) {
        addException(e);
      }
    }
    if (oriin != null) {
      try {
        oriin.close();
      }
      catch (Exception e) {
        addException(e);
      }
    }
    if (this.ex != null) {
      ExceptionHandler.rethrowRuntimeException(this.ex);
    }
  }

  private void addException(Exception ex) {
    if (this.ex == null)
      this.ex = ex;
    ex.printStackTrace();
  }
}
