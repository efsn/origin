package org.codeyn.util;

import java.io.IOException;
import java.io.InputStream;

import org.codeyn.util.yn.ArrayUtil;
import org.codeyn.util.yn.StrmUtil;

/**
 * 将多个InputStream流看作一个InputStream
 * 1.构造函数的参数in不能为null，长度不能为0，不能所有的元素（in[i]）都为null
 * 2.不支持mark
 * 3.关闭该流时将关闭所有的流元素（in[i]）
 * @author zcx
 *
 */
public class UnionInputStream extends InputStream {
  private int pos = 0;//当前流读到第几个InputStream

  private InputStream[] ins = null;

  public UnionInputStream(InputStream[] in) throws IOException {
    ins = (InputStream[]) ArrayUtil.excludeNull(in);
  }

  /**
   * 从输入流读取下一个数据字节。返回 0 到 255 范围内的 int 字节值。
   * 如果因已到达流末尾而没有可用的字节，则返回值 -1。在输入数据可用、检测到流的末尾或者抛出异常前，此方法一直阻塞
   */
  public int read() throws IOException {
    InputStream in = this.getInputStream();
    if (in == null) {
      return -1;//流已全部读完
    }

    int r = in.read();
    while (r == -1) {
      in = this.getNextStm();
      if (in == null) {
        return -1;//流已全部读完
      }
      r = in.read();
    }
    return r;
  }

  /**
   * 从输入流中读取一定数量的字节并将其存储在缓冲区数组 b 中
   * @throws IOException 
   */
  public int read(byte[] b) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    return this.read(b, 0, b.length);
  }

  /**
   * 将输入流中最多 len 个数据字节读入字节数组
   * @throws IOException 
   */
  public int read(byte[] b, int off, int len) throws IOException {
    if (b == null) {
      throw new NullPointerException();
    }
    if ((off < 0) || (off > b.length) || (len < 0) || (off + len > b.length) || (off + len < 0)) {
      throw new IndexOutOfBoundsException();
    }
    if (len == 0) {
      return 0;
    }
    InputStream in = this.getInputStream();
    if (in == null) {
      return -1;//流已全部读完
    }
    int l = len - off;
    int r = this.stmTryRead(in, b, off, len);//读取数据的长度,如果获得的长度小于len-off表示该当前流已结束
    if (r == l) {
      return r;
    }

    //没有读取到足够的数据，说明该流已读取完
    while (true) {
      in = this.getNextStm();
      if (in == null) {
        //r已读取的数据长度,为0，表明流已读取完
        return (r == 0) ? -1 : r;
      }
      r += this.stmTryRead(in, b, off + r, len - r);
      if (r == l) {
        return r;
      }
    }

  }

  public long skip(long n) throws IOException {
    if (n < 0) {
      throw new IllegalArgumentException("negative skip length");
    }
    InputStream in = this.getInputStream();
    if (in == null) {
      return 0;//流已结束
    }
    long r = in.skip(n);
    if (r == n) {
      return r;
    }
    //此次流已结束
    while (true) {
      in = this.getNextStm();
      if (in == null) {
        return r;
      }
      r += in.skip(n - r);
      if (r == n) {
        return r;
      }
    }
  }

  /**
   * 返回此输入流方法的下一个调用方可以不受阻塞地从此输入流读取（或跳过）的字节数
   */
  public int available() throws IOException {
    return 0;
  }

  /**
   * 测试此输入流是否支持 mark 和 reset 方法。
   */
  public boolean markSupported() {
    return false;
  }

  /**
   * 在此输入流中标记当前的位置。
   */
  public void mark(int readlimit) {
    return;
  }

  /**
   * 将此流重新定位到对此输入流最后调用 mark 方法时的位置。
   */
  public void reset() throws IOException {
    throw new IOException("mark/reset not supported");
  }

  /**
   * 关闭此输入流并释放与该流关联的所有系统资源
   */
  public void close() throws IOException {
    if (ins == null) {
      return;
    }
    for (int i = 0; i < ins.length; i++) {
      ins[i].close();
    }
  }

  /**
   * 返回当前流
   * @return
   */
  private InputStream getInputStream() {
    if (ins == null) {
      return null;
    }
    if (pos >= ins.length) {
      return null;
    }
    return ins[pos];
  }

  /**
   * 返回下一个流,如果返回null，表明流已全部读完
   * @return
   */
  private InputStream getNextStm() {
    if (ins == null) {
      return null;
    }
    pos++;
    return this.getInputStream();
  }

  /**
   * 从流中读取数据，读取的长度为len-off,返回读入了流的长度，最小为0
   */
  private int stmTryRead(InputStream in, byte[] b, int off, int len)
      throws IOException {
    return StrmUtil.stmTryRead(in, b, off, len);
  }
}