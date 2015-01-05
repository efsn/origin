package org.codeyn.util.io;

/**
 * 微软在win2000后，对notepad存储的utf-8文档，会加上BOM(Byte Order Mark, U+FEFF)，主要因为utf-8和ascii是相容的；
 * 为了避免使用这自己忘记用什么存，造成utf-8文档用ascii模式打开看倒是乱码，所以，在文档前加上BOM.
 * 先鄙视下微软，这样造成读取utf-8编码的文件，开头可能会有BOM标识，造成乱码问题；
 * 此类解决读取问题；
 * 
 * Java读取BOM（Byte Order　Mark）的问题，在使用UTF-8时，可以在文件的开始使用3个字节的"EF BB BF"来标识文件使用了UTF-8的编码，
 * 当然也可以不用这个3个字节。上面的问题应该就是因为对开头3个字节的读取导致的。开始不太相信这个是JDK的Bug，后来在多次试验后，问题依然存在，
 * 就又狗狗了一下，果然找到一个如下的Bug:Bug ID:4508058
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4508058
 * 不过在我关掉的一些页面中记得有篇文件说这个bug只在jdk1.5及之前的版本才有，说是1.6已经解决了，从目前来看1.6只是解决了读取带有BOM文件失败的问题，
 * 还是不能区别处理有BOM和无BOM的UTF-8编码的文件，从Bug ID:4508058里的描述可以看出，这个问题将作为一个不会修改的问题关闭，对于BOM编码的识别将由应用程序自己来处理，
 * 原因可从另处一个bug处查看到，因为Unicode对于BOM的编码的规定可能发生变化。也就是说对于一个UTF-8的文件，应用程序需要知道这个文件有没有写BOM，然后自己决定处理BOM的方式。 
Original pseudocode   : Thomas Weidenfeller
Implementation tweaked: Aki Nieminen

http://www.unicode.org/unicode/faq/utf_bom.html
BOMs:
  00 00 FE FF    = UTF-32, big-endian
  FF FE 00 00    = UTF-32, little-endian
  FE FF          = UTF-16, big-endian
  FF FE          = UTF-16, little-endian
  EF BB BF       = UTF-8

Win2k Notepad:
  Unicode format = UTF-16LE
***/

import java.io.*;

/**
* Generic unicode textreader, which will use BOM mark
* to identify the encoding to be used.
*/
public class UnicodeReader extends Reader {
  PushbackInputStream internalIn;

  InputStreamReader internalIn2 = null;

  String defaultEnc;

  private static final int BOM_SIZE = 4;

  /*
  Default encoding is used only if BOM is not found. If
  defaultEncoding is NULL then systemdefault is used.
  */
  public UnicodeReader(InputStream in, String defaultEnc) {
    internalIn = new PushbackInputStream(in, BOM_SIZE);
    this.defaultEnc = defaultEnc;
  }

  public String getDefaultEncoding() {
    return defaultEnc;
  }

  public String getEncoding() {
    if (internalIn2 == null)
      return null;
    return internalIn2.getEncoding();
  }

  /**
   * Read-ahead four bytes and check for BOM marks. Extra 
  bytes are
   * unread back to the stream, only BOM bytes are skipped.
   */
  protected void init() throws IOException {
    if (internalIn2 != null)
      return;

    String encoding;
    byte bom[] = new byte[BOM_SIZE];
    int n, unread;
    n = internalIn.read(bom, 0, bom.length);

    if ((bom[0] == (byte) 0xEF) && (bom[1] == (byte) 0xBB) && (bom[2] == (byte) 0xBF)) {
      encoding = "UTF-8";
      unread = n - 3;
    }
    else if ((bom[0] == (byte) 0xFE) && (bom[1] == (byte) 0xFF)) {
      encoding = "UTF-16BE";
      unread = n - 2;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE)) {
      encoding = "UTF-16LE";
      unread = n - 2;
    }
    else if ((bom[0] == (byte) 0x00) && (bom[1] == (byte) 0x00) && (bom[2] == (byte) 0xFE) && (bom[3] == (byte) 0xFF)) {
      encoding = "UTF-32BE";
      unread = n - 4;
    }
    else if ((bom[0] == (byte) 0xFF) && (bom[1] == (byte) 0xFE) && (bom[2] == (byte) 0x00) && (bom[3] == (byte) 0x00)) {
      encoding = "UTF-32LE";
      unread = n - 4;
    }
    else {
      // Unicode BOM mark not found, unread all bytes
      encoding = defaultEnc;
      unread = n;
    }
    //     System.out.println("read=" + n + ", unread=" + unread);

    if (unread > 0)
      internalIn.unread(bom, (n - unread), unread);
    else if (unread < -1)
      internalIn.unread(bom, 0, 0);

    // Use given encoding
    if (encoding == null) {
      internalIn2 = new InputStreamReader(internalIn);
    }
    else {
      internalIn2 = new InputStreamReader(internalIn, encoding);
    }
  }

  public void close() throws IOException {
    init();
    internalIn2.close();
  }

  public int read(char[] cbuf, int off, int len) throws IOException {
    init();
    return internalIn2.read(cbuf, off, len);
  }

}
