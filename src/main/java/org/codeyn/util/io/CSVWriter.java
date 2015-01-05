package com.esen.io;

import java.io.PrintWriter;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 将数据写成csv格式流
 * @author dw
 *
 */
public class CSVWriter {
  public static final char DEFAULT_COMMENT = '#';

  private PrintWriter pw;

  private char separator;

  private char quote;

  private final String TIMESTAMP_FORMATTER = "yyyy-MM-dd HH:mm:ss";

  private final String DATE_FORMATTER = "yyyy-MM-dd";

  public CSVWriter(Writer w) {
    this(w, ',', '"');
  }
  public CSVWriter(Writer w, char separator, char quote) {
    this.pw = new PrintWriter(w);
    this.separator = separator;
    this.quote = quote;

  }

  /**
   * 写一行数据
   * Object 类型可以是：String,Number,Date,Time,Timestamp,Clob,Reader;
   * Date格式：yyyy-MM-dd
   * Time格式：HH:mm:ss
   * Timestamp格式：yyyy-MM-dd HH:mm:ss
   * Clob,Reader 将转换为字符串存储；
   * 其他类型都存储空值；
   * @param o
   * @return
   */
  public boolean writeLine(Object o[]) {
    if (o == null || o.length == 0)
      return false;
    for (int i = 0; i < o.length; i++) {
      if (i > 0)
        pw.write(separator);
      String v = getOjectStr(o[i]);
      if (v != null)
        pw.write(v);
      else pw.write("");
    }
    pw.write('\n');
    return true;
  }

	/**
	 * 写第一行注释文件
	 * @param o
	 * @return
	 */
	public boolean writeComment(Object o[]) {
		if (o == null || o.length == 0)
			return false;
		pw.write(DEFAULT_COMMENT);
		return writeLine(o);
	}
  
  private String getOjectStr(Object o) {
    if (o == null)
      return null;
    if (o instanceof String) {
      return parseValue((String) o);
    }
    else if (o instanceof Number) {
      return o.toString();
    }
    else if (o instanceof Time) {
      return o.toString();
    }
    else if (o instanceof Timestamp) {
      Timestamp t = (Timestamp) o;
      return new SimpleDateFormat(TIMESTAMP_FORMATTER).format(t);
    }
    else if (o instanceof Date) {
      Date d = (java.util.Date) o;
      return new SimpleDateFormat(DATE_FORMATTER).format(d);
    }
    else if (o instanceof Clob) {
      Clob c = (Clob) o;
      return parseValue(readClob(c));
    }
    else if (o instanceof Reader) {
      Reader r = (Reader) o;
      return parseValue(readReader(r));
    }
    return null;
  }

  private String readClob(Clob c) {
    try {
      Reader r = c.getCharacterStream();
      try{
      	return readReader(r);	
      }finally{
      	r.close();
      }
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private String readReader(Reader r) {
    try {
      StringBuffer sb = new StringBuffer();
      char[] cbuf = new char[2048];
      int n = 0;
      while ((n = r.read(cbuf, 0, cbuf.length)) != -1) {
        if (n > 0) {
          sb.append(cbuf, 0, n);
        }
      }
      return sb.toString();
    }
    catch (Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private String parseValue(String v) {
    if (v == null || v.length() == 0)
      return null;
    StringBuffer sb = new StringBuffer(v.length() + 2);
    //如果v值里面包含分隔符或者换行符，则需要用quote将值括起来；
    boolean f = haveQuote(v);
    if(f) sb.append(quote);
    for (int i = 0; i < v.length(); i++) {
      char c = v.charAt(i);
      if (f && c == quote)
        sb.append(c);
      sb.append(c);
    }
    if(f) sb.append(quote);
    return sb.toString();
  }
  /**
   * 4.字段中包含有逗号，该字段必须用双引号括起来
   * 5.字段中包含有换行符，该字段必须用双引号括起来
   * 6.字段前后包含有空格，该字段必须用双引号括起来
   * 8.字段中如果有双引号，该字段必须用双引号括起来
   * @param v
   * @return
   */
  private boolean haveQuote(String v) {
    return v.indexOf(separator)>=0||v.indexOf('\n')>=0||v.indexOf(quote)>=0
    || (v.startsWith(" ")&&v.endsWith(" "));
  }
  
  public void flush() {
    pw.flush();
  }

  public void close() {
    pw.close();
  }
}
