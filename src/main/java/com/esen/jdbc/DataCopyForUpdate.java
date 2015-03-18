package com.esen.jdbc;

import java.sql.Connection;

import com.esen.jdbc.data.DataCopyForUpdateImpl;
import com.esen.util.IProgress;

/**
 * 20091124<br>
 * 一个用于批量更新数据的接口；<br>
 * 主要用于指标主题的物化；<br>
 * <pre>
 * 此类的主要目的是为了实现同一批数据的不同字段的批量更新，比如：
 * 有个主题表，它字段很多，由于一些原因，一次只能选择部分字段，不同期的可能还需要分期更新数据；
 * select zb1,zb2,zb3...,zb20,bbq_,userid_ from tbname where bbq_='200901--'
 * select zb21,zb12,zb13...,zb40,bbq_,userid_ from tbname where bbq_='200901--'
 * select zb1,zb2,zb3...,zb20,bbq_,userid_ from tbname where bbq_='200902--'
 * select zb21,zb12,zb13...,zb40,bbq_,userid_ from tbname where bbq_='200902--'
 * ...
 * 上面有两期数据，每期分别选择了前20个字段，和后20个字段，这些sql的数据要写入目的表tbname2中，
 * tbname2的字段结构：bbq_,userid_,zb1,zb2,....,zb40
 * 这里userid_,bbq_是主键；
 * 
 * </pre>
 * 
 * 更新过程：
 * <pre>
 * 1)源连接池与目的连接池不同，把srcsqllist中的sql或者表的数据从源连接池写入目的连接池的临时表；
 *   源连接池与目的连接池相同，把srcsqllist中sql写入临时表；
 *   这样在目的连接池中，srcsqllist中的每个sql都有对应的临时表，本身是表的除外；
 * 2)现在都在目的连接池中操作，srcsqllist中的所有sql的数据都在对应的临时表中；
 *   选取这些临时表中目的表中存在的字段组成sql，然后根据主键 进行错位full join；
 *   select sum(zb1),sum(zb2),sum(zb3),sum(zb4),sum(zb5),key1,key2
 *   from(
 *   select zb1,zb2,0 as zb3,0 as zb4,0 as zb5,key1,key2 from temp01
 *   union all
 *   select 0,0,zb3,zb4,0,key1,key2 from temp02
 *   union all
 *   select 0,0,0,0,zb5,key1,key2 from temp3
 *   ) u$_
 *   group by key1,key2
 * 3)将前面生成的unionsql写入一个新的临时表newtbname；
 * 4)如果onlyInsert=true,只写入新数据；
 *   删除newtbname中，与目的表共有的数据；
 *   将newtbname剩余数据写入目的表，这时已经没有主键冲突；
 * 5)如果onlyInsert=false, 依据主键keys，更新存在的数据，写入新数据；
 *   5.1)全字段更新：
 *       删除目的表中，与newtbname共有的数据；
 *       将newtbname数据写入目的表，这时已经没有主键冲突；
 *   5.2)部分字段更新：
 *       将目的表和newtbname共有的数据中，属于目的表的字段选出来，和newtbname融合，写入新的临时表newtbname2；
 *       这时对于newtbname2和目的表来说，变成了全字段更新；
 *       删除目的表中，与newtbname2共有的数据；
 *       将newtbname2数据写入目的表，这时已经没有主键冲突；
 * 6)删除此过程中产生的所有临时表；
 * 7)更新完成；
 * </pre>
 * 
 * 调用方法：
 * <pre>
 * ConnectionFactory conf = ...; 
 * DataCopyForUpdate du = DataCopyForUpdate.createInstance();
 * du.setIprogress(new ProgressDefault());
 * du.setSourceDataPool(conf);
 * du.addSourceSql("select zb1,zb2,zb3 from ...");
 * du.addSourceSql("select zb4,zb5,zb6... from ...");
 * ...
 * du.setTargetDataPool(conf);
 * du.setTargetTable("tbname");
 * du.setPrimaryKeys(new String[]{"userid_","bbq_"});
 * du.isOnlyInsertNewRecord(false);
 * 
 * du.executeUpdate();
 * 
 * 支持多次提交：
 * du.addSourceSql("select zb1,zb2,zb3 from ...");
 * du.addSourceSql("select zb4,zb5,zb6... from ...");
 * du.executeUpdate();
 * 
 * du.addSourceSql("select zb1,zb2,zb3 from ...");
 * du.addSourceSql("select zb4,zb5,zb6... from ...");
 * du.executeUpdate();
 * 
 * </pre>
 * <pre>
 * 做这个改动，是因为：每次更新的字段太多的话，会超过数据库的限制：
 * Oracle会出异常：ORA-01467:sort key too long
 * Cause:A DISTINCT, GROUP BY, ORDER BY, or SET operation requires a sort key longer than that supported by Oracle. Either too many columns or too many group functions were specified in the SELECT statement.
 * Action:Reduce the number of columns or group functions involved in the operation.
 * 支持多次提交，每次提交有限个字段值；
 * 
 * </pre>
 * @author dw
 */
public abstract class DataCopyForUpdate {
  
  public static DataCopyForUpdate createInstance(){
    return new DataCopyForUpdateImpl();
  }
  
  /**
   * 设置进度日志接口；
   * @param ipro
   */
  public abstract void setIprogress(IProgress ipro);
  /**
   * 设置源连接池
   * @param srcpool
   */
  public abstract void setSourceDataPool(ConnectionFactory srcpool);
  /**
   * 设置源数据库连接；
   * 优先使用连接；
   * 此连接外面代码负责关闭；
   * @param srccon
   */
  public abstract void setSourceConnection(Connection srccon);
  
  /**
   * 设置源sql或者表名；<br>
   * 可以设置多次，将sql的数据插入目的表中；<br>
   * 每次可以只写入目的表的部分字段值；<br>
   * 写入时，按sql的字段结构和目的表字段一一对应，只写入都存在的字段的值；
   * 20100902
   * 相同字段名的源表字段和目的表字段，如果它们的数据类型不一致，则该字段不能进行数据导入；
   * @param sqlortablename
   *        可以是一个查询sql，也可以是一个表名；
   */
  public abstract void addSourceSql(String sqlortablename);
  
  /**
   * 设置目标连接池
   * @param targetpool
   */
  public abstract void setTargetDataPool(ConnectionFactory targetpool);
  /**
   * 设置目的数据库连接;
   * 优先使用连接；
   * 此连接外面代码负责关闭；
   * @param targetconn
   */
  public abstract void setTargetConnection(Connection targetconn);
  
  /**
   * 设置目的表
   * 如果目的表不存在，会出异常提示；
   * @param tbname
   */
  public abstract void setTargetTable(String tbname);
  
  /**
   * 设置主键<br>
   * 用于更新数据的依据；<br>
   * 可以不设置，如果不设置则取目的表的主键或者唯一索引； <br>
   * 如果没有设置，且目的表没有主键或者唯一索引，则直接写入数据；
   * @param keys
   */
  public abstract void setPrimaryKeys(String[] keys);
  
  /**
   * 设置是否只写入新数据；<br>
   * 设置为true，将只写入新数据，不更新存在的记录，依据是主键；<br>
   * 设置为false，将写入新记录，更新存在的记录，依据是主键；<br>
   * 
   * 如果没有设置主键，且目的表没有主键或者唯一索引，则此设置不起作用，总是写入全部记录；<br>
   * 
   * 此默认值是false；
   * @param onlyInsert
   */
  public abstract void isOnlyInsertNewRecord(boolean onlyInsert);
  
  /**
   * 执行所有更新或者写入操作；
   * @throws Exception 
   */
  public abstract void executeUpdate() throws Exception;
}
