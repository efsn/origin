package com.esen.jdbc.backup;

import java.io.OutputStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.Dialect;
import com.esen.util.ArrayFunc;
import com.esen.util.IProgress;
import com.esen.util.StrFunc;

/**
 * 备份数据库表
 * 备份的文件分为以下几个部分：(带长度表示在该字串或数据前带有该字串和数据的长度，如有字段名称："abc"，则输出带长度的字段名称为：3abc)
 *   1.是否压缩,1压缩，0未压缩
 *   2.字段长度
 *   3.所有字段：字段名称，字段类型，字段长度，小数位数(均带长度)
 *   4.索引个数
 *   5.索引字段名，索引字段(均带长度)
 *   6.数据长度
 *   7.所有数据(按字段的顺序输出每条数据，带长度)
 * 
 * @author zz
 */
public abstract class BackupDb {
  /**
   * 将表tablename的结构和内容保存到stm中
   * @param fn          文件名，保存的内容写入该文件
   * @throws Exception 
   */
  public abstract void saveTo(String fn) throws Exception;

  /**
   * 将表tablename的结构和内容保存到stm中,本方法不关闭流
   * @param stm         保存的内容写入该流
   */
  public abstract void saveTo(OutputStream stm) throws Exception;

  /**
   * 设置查询条件，如果sql为null，则查询所有的结果
   * @param sql
   */
  public abstract void setWhereSql(String sql);

  /**
   * 设置不保存哪些字段，fields的格式为"field1,field2"
   * @param fields
   */
  public abstract void setIgnoreColumns(String fields);

  /**
   * 设置数据库连接工厂
   * @param cfct
   */
  public abstract void setConnectionFactory(ConnectionFactory cfct);

  /**
   * 设置导出的数据库表名
   * @param tablename
   */
  public abstract void setTableName(String tablename);

  /**
   * 设置是否压缩后保存
   * @param iszip
   */
  public abstract void setZip(boolean iszip);

  /**
   * 设置进度
   * @param pro
   */
  public abstract void setPregress(IProgress pro);

}
