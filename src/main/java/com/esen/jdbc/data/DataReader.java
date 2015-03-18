package com.esen.jdbc.data;

import com.esen.jdbc.AbstractMetaData;

/**
 * 读取一个结果集
 * 来源可能是数据库，文件流(*.db,*.dbf,*.cvs,...)
 * @author dw
 *
 */
public interface DataReader {
  /**
   * 获取表结构
   * @return
   * @throws Exception
   */
  public AbstractMetaData getMeta() throws Exception;
  /**
   * 获得结果集行数
   * @return
   * @throws Exception
   */
  public int getRecordCount() throws Exception;
  /**
   * 是否还有下一行数据
   * @return
   * @throws Exception
   */
  public boolean next() throws Exception;
  /**
   * 读取一行数据中的每个字段的值；
   * 支持反复调用；
   * @param i从0开始
   * @return
   * @throws Exception
   */
  public Object getValue(int i) throws Exception;
  /**
   * 关闭数据库链接或者文件流
   * @throws Exception
   */
  public void close() throws Exception;
}
