package com.esen.jdbc.etl;

import com.esen.util.StringMap;

/**
 * 定要抽取关系的类，
 * 此类表述了有多少个主题需要抽取，怎么抽取，等等。
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * @author yk
 * @version 5.0
 */
public interface EtlDefine {
  /**
   * 返回表名
   * @return
   */
  public String getName();

  public String getCaption();

  /**
   * 返回抽取的字段数目
   * @return
   */
  public int getDestFieldCount();

  /**
   * 返回抽取字段
   * @param i
   * @return
   */
  public EtlFieldDefine getDestFieldDefine(int i);

  public EtlFieldDefine getDestFieldDefine(String name);

  public StringMap getOptions();

  /**
   * 返回导入条件
   * @return
   */
  public String getImportCondition();

  /**
   * 是绝对的一对一的对应
   */
  public boolean isAbsolute();
}
