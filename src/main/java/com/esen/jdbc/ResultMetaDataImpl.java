package com.esen.jdbc;

import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * <p>Title: BI@Report</p>
 * <p>Description: 网络报表在线分析系统</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: 武汉新连线科技有限公司</p>
 * 20090813
 * 增加获取是否为空和是否唯一的实现，并优化代码；
 * @author daqun
 * @version 5.0
 */

public class ResultMetaDataImpl implements ResultMetaData {

  private int columnCount;
  /**
   * 20090813
   * 改为使用数组存储字段信息；
   */
  private String[] columnNames ;
  private int[] columnTapes ;
  private String[] columnLabel ;
  private int[] columnLens ;
  private int[] columnScale ;
  private int[] nullables;
  private int[] uniques;

  public ResultMetaDataImpl(ResultSetMetaData meta) {
    try {
      this.columnCount = meta.getColumnCount();
      columnNames = new String[columnCount];
      columnTapes = new int[columnCount];
      columnLabel = new String[columnCount];
      columnLens = new int[columnCount];
      columnScale = new int[columnCount];
      nullables = new int[columnCount];
      uniques = new int[columnCount];
      for ( int i=1;i<=this.columnCount;i++ ){
        int k = i-1;
        columnNames[k]=meta.getColumnName(i);
        columnTapes[k]=meta.getColumnType(i);
        columnLens[k]=meta.getColumnDisplaySize(i);
        columnLabel[k]=meta.getColumnLabel(i);
        columnScale[k]=meta.getScale(i);
        nullables[k] = meta.isNullable(i);
        uniques[k] = -1;//无法获取是否唯一；
      }
    }
    catch (SQLException ex) {
      ex.printStackTrace();
    }
  }

  public int getColumnCount() {
    return this.columnCount;
  }

  public String getColumnName(int i) {
    return columnNames[i];
  }

  public int getColumnType(int i) {
    return columnTapes[i];
  }

  public String getColumnDescription(int i) {
    return null;
  }

  public int getColumnLength(int i) {
    return columnLens[i];
  }

  public char getColumnTypeStr(int i) {
    return SqlFunc.getType(getColumnType(i));
  }

  public int getColumnScale(int i){
    return columnScale[i];
  }

  public String getColumnLabel(int i) {
    return columnLabel[i];
  }
  
  public int isNullable(int i){
    return nullables[i];
  }
  
  public int isUnique(int i){
    return uniques[i];
  }
  
}
