package org.codeyn.util.io.file.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import org.codeyn.util.io.file.BlobValue;
import org.codeyn.util.yn.StrmUtil;

/**
 * 通过临时文件存储字节流的实现；
 */
public final class BlobValueImpl implements BlobValue { 
  private RandomAccessFile rf;
  private long startPoint;
  private long len;
  
  public BlobValueImpl(RandomAccessFile rf,long startPoint,long len){
    this.rf = rf;
    this.startPoint = startPoint;
    this.len = len;
  }
  public BlobValueImpl(InputStream data, RandomAccessFile rf) {
    this.rf = rf;
    write(data);
  }

  private void write(InputStream data) {
    try {
      startPoint = rf.getFilePointer();
      len = StrmUtil.stmTryCopyFrom(data, rf);
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * 获取一个输入的读入接口，此函数可以调用多次。
   * @return
   */
  public InputStream getInputStream() {
    //优化获取流的实现，可以在任意时刻获取此对象；
    return new RandomInputStream(rf, len, startPoint);
  }

  public long getLength() {
    return len;
  }

  public String toString() {
    return "Blob(" + len / 1024 + "Kb)";
  }
}

class RandomInputStream extends InputStream {
  private RandomAccessFile rf;
  /**
   * 读取流的长度
   */
  private long len; 
  /**
   * 读取起始位置；
   */
  private long startPoint; 
  
  /**
   * 已经读取了多长；
   */
  private long p;
  /**
   * 优化获取流的实现，记录该流的起始位置，每次读取都将指针移到正确的读取位置；
   * @param rf
   * @param len
   * @param startPoint
   */
  public RandomInputStream(RandomAccessFile rf, long len, long startPoint) {
    this.rf = rf;
    this.len = len;
    this.startPoint = startPoint;
    this.p = 0;
  }

  /**
   * 实现父类的方法，告诉外界此流还有多少数据，有利于优化
   */
  public int available() throws IOException {
    return (int) (len-p);
  }

  public int read() throws IOException {
    checkPoint();
    if (p++ < len)
      return rf.read();
    return -1;
  }
  /**
   * 判断读取位置是否是将要读取的位置，如果不是，设置到正确的读取位置；
   * @throws IOException
   */
  private void checkPoint() throws IOException {
    if (rf.getFilePointer() != startPoint+p)
      rf.seek(startPoint+p);
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
    checkPoint();
    if (p+length <= len) {
      int readlen = rf.read(b, off, length);
      p+=readlen;
      return readlen;
    }
    else {
      int l = (int) (len-p);
      if (l > 0){
        int readlen = rf.read(b, off, l);//bug，这里应该是off而不是0
        p+=readlen;
        return readlen;
      }else
        return -1;
    }
  }
}
