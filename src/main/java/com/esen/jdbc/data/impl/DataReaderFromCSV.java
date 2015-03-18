package com.esen.jdbc.data.impl;

import java.io.Reader;

import com.esen.io.CSVReader;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.data.DataReader;

/**
 * 读取csv格式文件；
 * 默认分隔符：,
 * 默认引用符："
 * 
 * 原来的程序将所有数据行读入内存，用以确定最大列数和每列的长度 ，浪费内存，效率不高；
 * 
 * 改为一行一行的读，每次只保存一行数据值，需要将文件读到末尾才能知道有多少行数据；
 * 
 * 列数以第一行列数为准，少于的当空值，多余的列值将被忽略；
 * 每列的长度初始都定义成20个长度，写入时根据值长度动态改变字段长度；
 * 
 * 
 * @author dw
 *
 */
public class DataReaderFromCSV implements DataReader {
  private CSVReader csv;
  private String[] row;

  public DataReaderFromCSV(Reader r) {
    this(r,0, ',', '"');
  }

  /**
   * 指定分隔符
   * @param r
   * @param separator
   */
  public DataReaderFromCSV(Reader r, char separator) {
    this(r,0, separator, '"');
  }
  public DataReaderFromCSV(Reader r, char separator, char quote) {
    this(r,0,separator,quote);
  }
  /**
   * 指定分隔符，引用符
   * @param r
   * @param separator
   * @param quote
   */
  public DataReaderFromCSV(Reader r,int skipline, char separator, char quote) {
    csv = new CSVReader(r,separator,quote,skipline);
  }

  /**
   * 返回null
   */
  public AbstractMetaData getMeta() throws Exception {
    return null;
  }

  /**
   * 返回每一行上，每个字段的值；
   */
  public Object getValue(int i) throws Exception {
    if(row!=null&&i<row.length){
      return row[i];
    }
    return null;
  }

  public void close() throws Exception {
    
  }

  /**
   * 不读完，不知道数据行数；
   */
  public int getRecordCount() throws Exception {
    return -1;
  }

  public boolean next() throws Exception {
    row = csv.readNext();
    while(row!=null){
      /*
       * 如果有空行，或者全是空格、不可见字符等，忽略该行；
       * 20090711 BIDEV-669
       * 如果每个字段都是空值，也忽略该行；
       * 比如
       * 1) 以'\t'为分隔符，连续多个\t符的行；
       * 2) 以','为分隔符，全部都是','的行，比如：",,,," 
       *    这样的行以','分割，每个项都是空值，也被认为是空行； 
       */
      if(isBlack(row)){
        row = csv.readNext();
        continue;
      }
      return true;
    }
    return false;
  }
  
  private boolean isBlack(String[] r){
    if(r==null) return true;
    for(int i=0;i<r.length;i++){
      String ri = r[i];
      /**
       * 20090713
       * 优化判断ri值是否是可见字符，如果是返回false;
       * Asc码：
       * 第0～32号及第127号(共34个)是控制字符或通讯专用字符，
       * 如控制符：LF（换行）、CR（回车）、FF（换页）、DEL（删除）、BEL（振铃）等；通讯专用字符：SOH（文头）、EOT（文尾）、ACK（确认）等；
　　         * 第33～126号(共94个)是字符，其中第48～57号为0～9十个阿拉伯数字；
       * 65～90号为26个大写英文字母，97～122号为26个小写英文字母，其余为一些标点符号、运算符号等。
       * 
       *  第32号是空格字符；
       */
      if(r!=null&&ri.length()>0&&(ri.charAt(0)>' '||ri.trim().length()>0)){
        return false;
      }
    }
    return true;
  }

  public String[] getLineValues() {
    return row;
  }

}
