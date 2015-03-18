package com.esen.jdbc.backup;

import java.io.InputStream;

import com.esen.jdbc.ConnectionFactory;
import com.esen.util.IProgress;

public abstract class RestoreDb {

  /**
   * 将stm中的内容读出并写入到数据库
   * @param fn          文件名，数据源从文件中获取
   * @throws Exception 
   */
  public abstract String restoreFrom(String fn) throws Exception;

  /**
   * 将stm中的内容读出并写入到数据库
   * @param stm         数据源
   */
  public abstract String restoreFrom(InputStream stm) throws Exception;

  /**
   * 设置数据库连接工厂
   * @param cfct
   */
  public abstract void setConnectionFactory(ConnectionFactory cfct);

  /**
   * 设置保存的数据库表名
   * @param tablename
   */
  public abstract void setTableName(String tablename);

  /**
   * 设置忽略字段，格式为："field1,field2"
   * @param fileds
   */
  public abstract void setIgnoreFields(String fields);

  /**
   * 设置保存数据前是否清空数据
   * @param cleardata
   */
  public abstract void setClearData(boolean cleardata);
  
  /**
   * 设置每次提交的记录行数
   */
  public abstract void setACommitCount(int acommitcount); 

  /**
   * 设置进度
   * @param pro
   */
  public abstract void setProgress(IProgress pro);
}
