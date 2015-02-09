package org.codeyn.util.io.file;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;

import org.codeyn.util.i18n.I18N;
import org.codeyn.util.io.file.impl.BlobValueImpl;

/**
 * 有些时候需要同事持有很多大数据量的对象，但往往对这些对象是进行只读的操作，如果我们将这些大对象读到内存中，那会占用很多内存
 * 如果一个对象用一个临时文件，那也会占用很多临时文件......
 * 用BlobFileCacher类，可以让使用者在一个临时文件中缓存大量的大对象，每个对象都可以以流的形式被访问
 * 通过函数createBlob可以缓存一个大对象，createBlob会返回一个BlobValue接口，通过这个接口调用者可以随时的多次的访问自己缓存的数据
 * 注意，从BlobFileCacher访问大对象时要一个一个的读取，即不要同时获得多个大对象的流，一会读一下这个，一会读一下那个交替读取
 * 
 * 20090227
 * 优化了BlobValue的实现，使之可以任意的调用getInputStream()；
 * 使用更加方便；
 */
public final class BlobFileCacher {
  private File tmpf;

  private RandomAccessFile rf;
  private BlobOutputStream bout;
  
  public BlobFileCacher(File tempfile) {
    try {
      tmpf = tempfile;
      rf = new RandomAccessFile(tmpf, "rw");
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  public BlobFileCacher() throws IOException {
    this(File.createTempFile("olap", "temp"));
  }
  /**
   * 根据data中的内容创建一个BlobValue对象，内容自动存储起来
   * 如果data流获取比较困难，可以使用beginWriteBlob()和endWriteBlob()方法；
   * @param data
   * @return
   */
  public BlobValue createBlob(InputStream data) {
    return new BlobValueImpl(data, rf);
  }
  
  /**
   * 有时候获取一个读取的流比较困难，但是将其写入一个流比较容易；
   * 这里提供一个可以写入的流，写入即存储，写入完毕调用endWriteBlob()获取可供读取的流对象；
   * 如果createBlob(data)中的data流获取比较困难，可以使用此方法；
   * @return
   * @throws IOException 
   */
  public OutputStream beginWriteBlob() throws IOException {
    if(bout==null)
      bout = new BlobOutputStream(rf);
    /**
     * 设置开始写入状态，记录起始位置
     */
    bout.begin();
    return bout;
  }
  /**
   * 调用beginWriteBlob()获取写入流，写入完毕，再调用此方法；
   * 获取可供读取的流对象；
   * @return
   * @throws IOException 
   */
  public BlobValue endWriteBlob() throws IOException{
    if(bout==null||bout.getStartPoint()==-1){
      //throw new RuntimeException("请调用先beginWriteBlob()方法获取一个写入流，写入数据；");
      throw new RuntimeException(I18N.getString("com.esen.io.blobfilecacher.exp", "请调用先beginWriteBlob()方法获取一个写入流，写入数据；"));
    }
    long startPoint = bout.getStartPoint();
    long len = bout.getLength();
    bout.end();
    return new BlobValueImpl(rf,startPoint,len);
  }

  /**
   * 使用完此对象后必须调用此方法释放资源，调用完此方法后，所有的blobvalue都不再能访问
   */
  public void close() {
    try {
      if(bout!=null){
        bout.close();
        bout = null;
      }
      if (rf != null)
        rf.close();
      if (tmpf != null)
        tmpf.delete();
      rf = null;
      tmpf = null;
    }
    catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
}
class BlobOutputStream extends OutputStream{
  private RandomAccessFile rf;
  private long startPoint;//记录起始位置
  protected BlobOutputStream(RandomAccessFile rf){
    this.rf=rf;
    end();
  }
  public void begin() throws IOException{
    this.startPoint = rf.getFilePointer();
  }
  public void end(){
    this.startPoint = -1;
  }
  public long getStartPoint(){
    return this.startPoint;
  }
  public long getLength() throws IOException{
    return rf.getFilePointer()-startPoint;
  }
  public void write(int b) throws IOException {
    rf.write(b);
  }
  public void write(byte b[]) throws IOException {
    rf.write(b); 
  }
  public void write(byte b[], int off, int len) throws IOException {
    rf.write(b, off, len);
  }
}
