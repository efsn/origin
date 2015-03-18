package com.esen.jdbc.data.impl;

import java.io.Writer;

import com.esen.io.CSVWriter;
import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.data.DataWriter;

/**
 * 写入csv格式文件；
 * @author dw
 *
 */
public class DataWriteToCSV implements DataWriter{
 
  private CSVWriter csvw;


  public DataWriteToCSV(Writer out) {
    this(out, ',', '"');
  }

  public DataWriteToCSV(Writer out, char separator) {
    this(out, separator, '"');
  }

  public DataWriteToCSV(Writer out, char separator, char quote) {
    csvw = new CSVWriter(out,separator,quote);
  }


  /*
   * 将DataReader接口中的数据写入文件；
   */
  public void writeData(DataReader r) throws Exception {
    if (r instanceof DataReaderFromCSV) {
      DataReaderFromCSV rc = (DataReaderFromCSV) r;
      while (rc.next()) {
        String[] ls = rc.getLineValues();
        csvw.writeLine(ls);
      }
    }
    else {
      AbstractMetaData md = r.getMeta();
      int fieldcount = md.getColumnCount();
      /**
       * BI-5927 备份成csv时，第一行为字段名，并带上类型和长度，只支持字符、数值、日期类型；且在第一行开头加上'#',表示为注释；
       * 格式：#bbq(C|10),name(C|100),tzze(N|15|2),cnt(I),date(D),... 
       */
      String[] fdnames = new String[md.getColumnCount()];
      for(int i=0;i<md.getColumnCount();i++){
    	 TableColumnMetaDataForWriter f = new TableColumnMetaDataForWriter();
    	 f.setName(md.getColumnName(i));
    	 f.setType(md.getColumnType(i));
    	 f.setLength(md.getColumnLength(i));
    	 f.setScale(md.getColumnScale(i));
    	 fdnames[i] = f.toString();
      }
      csvw.writeComment(fdnames);
      //第二行开始写数据
      while (r.next()) {
        Object[] values = new Object[fieldcount];
        for (int i = 0; i < fieldcount; i++) {
          values[i] = r.getValue(i);
        }
        csvw.writeLine(values);
      }
    }
  }


  public void close() {
  }

  public void flush() {
    csvw.flush();
  }
}
