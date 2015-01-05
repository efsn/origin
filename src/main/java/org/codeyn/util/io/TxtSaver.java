package org.codeyn.util.io;

import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.esen.codec.Base64;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;

/**
 * 此类负责生成类似NPF格式的文件
 * 
 * 基本用法：先创建TxtSaver对象，再prepareWrite，调用write*方法写入需要的内容，
 * 最后一定要调用flush方法才能写入最初指定的输出流中，具体例子见TestTxtSaver.java
 *  
 * @author yk
 */
public class TxtSaver {
  private OutputStream stm;
  protected Writer writer;
  private String charsetName;

  public TxtSaver() {
  }

  public final void prepareWrite(OutputStream stm) throws IOException {
    prepareWrite(stm,null);
  }

  public final void prepareWrite(OutputStream stm, String charsetName) throws IOException {
    this.charsetName = charsetName;
    this.stm = stm;
    this.writer = new BufferedWriter(charsetName ==null ? new OutputStreamWriter(stm): new OutputStreamWriter(stm, charsetName));
  }

  public final void writeLine() throws IOException {
    write('\r');
    write('\n');
  }

  public final void writeLine(final String s) throws IOException {
    write(s);
    writeLine();
  }

  public final void writeValueln(final String nm, final String value) throws IOException {
    /*
		 * 20081008 如果值为空，那么不输出这个属性。之前的做法是输出"color="这样的字符串。两者等效，前者效率更高。
		 */
    if(value==null || value.length()==0) return;
    write(nm);
    write('=');
    write(value);
    writeLine();
  }
  
  /**
   * 如果value含有回车换行，或者以引号开头，那么就将其转义后再输出，这样输出的内容可以给StringMap对象读取
   */
  public final void writeValueln(String nm, String value, boolean adjustquote) throws IOException {
    if(value==null || value.length()==0) return;
    if(value.charAt(0)==StringMap.QUOTE||value.indexOf('\r')>-1||value.indexOf('\n')>-1){
      value = StrYn.quotedStr(value, StringMap.QUOTE);
    }
    writeValueln(nm, value);
    writeLine();
  }

  public final void writeValueln(final String nm, final int value) throws IOException {
    writeValue(nm, value);
    writeLine();
  }

  public final void writeValueln(final String nm, final boolean value) throws IOException {
    writeValue(nm, value);
    writeLine();
  }

  public final void writeValueln(final String nm, final double value) throws IOException {
    writeValue(nm, value);
    writeLine();
  }

  public final void writeValueln(final String nm, final char value) throws IOException {
    writeValue(nm, value);
    writeLine();
  }
	
	/*20081008 新增写对象的方法。如果对象有toString方法，那么最好调用本方法。*/
  public final void writeValueObjln(final String nm, final Object v) throws IOException{
    if(v!=null){
      writeValueObj(nm, v);
      writeLine();
    }
  }

  public final void writeValue(final String nm, final String value) throws IOException {
    /*
		 * 20081008 如果值为空，那么不输出这个属性。之前的做法是输出"color="这样的字符串。两者等效，前者效率更高。
		 */
		if(value==null || value.length()==0) return;
    write(nm);
    write('=');
    write(value);
  }

  public final  void writeValue(final String nm, final StringBuffer value) throws IOException {
    if(value==null || value.length()==0) return;
    write(nm);
    write('=');
    write(value);
  }


  public final void writeValue(final String nm, final boolean value) throws IOException {
    write(nm);
    write('=');
    write(value ? "true" : "false");
  }

  public final void writeValue(final String nm, final int value) throws IOException {
    write(nm);
    write('=');
    write(value);
  }

  public final void writeValue(final String nm, final double value) throws IOException {
    write(nm);
    write('=');
    write(value);
  }

  public final void writeValue(final String nm, final char value) throws IOException {
    write(nm);
    write('=');
    write(value);
  }
  
  /**
   * 写一个标签起始行
   */
  public final void writeTagStart(final String nm) throws IOException {
    write('<');
    write(nm);
    write('>');
    writeLine();
  }
  
  /**
   * 写一个标签结束行
   */
  public final void writeTagEnd(final String nm) throws IOException {
    write('<');
    write('/'); 
    write(nm);
    write('>');
    writeLine();
  }
  
	/*20081008 新增写对象的方法。如果对象有toString方法，那么最好调用本方法。*/
  public final void writeValueObj(final String nm, final Object v) throws IOException{
    if(v!=null){
      write(nm);
      write('=');
      write(v.toString());
    }
  }

  public final void write(final byte[] s) throws IOException {
    writer.flush();
    stm.write(s);
  }

  public final void write(final String s) throws IOException {
    if (s!=null)
      writer.write(s);
  }
  
  public void write(final StringBuffer value) throws IOException {
    for (int i = 0; i < value.length(); i++) {
      write(value.charAt(i));
    }
  }

  public final void write(int i) throws IOException {
    //原来用write(String.valueOf(i));，但是连续写入很多整形时，很耗费内存
    StmFunc.writeInt(writer, i);
  }

  public final void write(final char c) throws IOException {
    writer.write(c);
  }

  public final void write(final double f) throws IOException {
    write(Double.toString(f));
  }

  public final void write(final byte b) throws IOException {
    write((int)b);
  }

  public final void write(final Color c) throws IOException {
    write( (c.getRGB() & 0xFFFFFF));
  }
  
  public final void write(final StringMap sm) throws IOException {
    sm.writeTo(writer, null);
  }

  public final void writeLine(final StringMap sm) throws IOException {
    /**
     * 20100326 如果StringMap为空，那么连换行符都不输出。
     *          之前发现NPF中有很多空行，就是因为表元的options为空。
     */
    if(sm.size() == 0) return;
    
    sm.writeTo(writer, null);
    writeLine();
  }
  
  /**
   * 编码，format可能是base64或空
   */
  public final String encode(final String s, String format){
    if(format==null || format.length()==0 || s==null || s.length()==0){
      return s;
    }
    if (format.equalsIgnoreCase("base64")){
      return new String(Base64.encodeBase64(s.getBytes()));
    }
    return s;
  }

  public void flush() throws IOException {
    writer.flush();
  }

  protected Writer getWriter(){
    return this.writer;
  }
}
