package com.esen.jdbc.etl;

import java.sql.Connection;

import com.esen.jdbc.etl.impl.EasyEtlImpl;
import com.esen.util.IProgress;

public abstract class EasyEtl {
  public static final EasyEtl createInstance(){
    return new EasyEtlImpl();
  }

  /**
   * 导入dbf，如果table不存在则创建，存在则追加数据，传入的con由传入者自己关闭
   */
  public abstract void importDbf(String dbf, Connection conn, String table);
  
  /**
   * 导入文本文件，如果table不存在则创建，存在则追加数据，传入的con由传入者自己关闭
   * 回车换行表示一行，colsep表示字段与字段的分割符
   */
  public abstract void importText(String fn, Connection conn, String table, char colsep);
  
  /**
   * 设置进度显示和取消接口
   */
  public abstract void setIprogress(IProgress ipro);
  
  
  /**
   * 指定的表不存在时是否自动创建,缺省不创建，如果又不创建又不存在，则触发异常
   * @param v
   */
  public abstract void setAutoCreateTable(boolean v);
  
  /**
   * 导入数据之前是否先清空数据，缺省不清空
   * @param v
   */
  public abstract void setClearTableFirst(boolean v);
  
}
