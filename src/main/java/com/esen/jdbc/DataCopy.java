package com.esen.jdbc;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Connection;

import com.esen.jdbc.data.DataCopyImpl;
import com.esen.util.IProgress;

/**
 * 数据复制
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 * @author yk
 * @version 5.0
 */

public abstract class DataCopy{
  /**
   * 创建新表，即使存在给定的表名，也创建新表；
   */
  public static final int OPT_CREATENEWTABLE = 0x1;
  /**
   *  覆盖,删除原表并创建新表；
   */
  public static final int OPT_OVERWRITER = 0x2;
  
  /**
   * 先清空表再插入数据。
   * 如果需要清空，先判断表结构是否相同，结构不同报异常，相同则清空记录；   
   * 20100118
   * 设置这个参数后，字段不允许自动扩容；
   * 原因是，这个写入过程是一个事务，如果修改字段长度，会隐性提交事务，而写入前是清空了所有数据的，
   * 造成如果后面的写入有异常，会终止导入，数据无法回滚到清空前的状态；
   * 出现的结果就是：如果写入过程出了异常，可能原先的数据全部丢失，这是不被允许的；
   */
  public static final int OPT_CLEARTABLE = 0x4;
  
  /**
   * 20090820 BI-2387 
   * 批处理行数不能太大，如果表字段很多，Oralce可能出如下异常：
   * java.lang.ArrayIndexOutOfBoundsException: -32533
   * at oracle.jdbc.driver.OraclePreparedStatement.setupBindBuffers(OraclePreparedStatement.java:2673)
   * at oracle.jdbc.driver.OraclePreparedStatement.executeBatch(OraclePreparedStatement.java:10689)
   * 
   * 这是oracle10g 驱动的bug： 
   * 4688040 JDBC does not support batch size > 65535 in 10g 
   * 目前BI使用的Oracle驱动是：ojdbc14.jar for jdk1.4版本； 

   * 此BUG在11g的新驱动，中修复： 
   * Oracle Database 11g Release 1 (11.1.0.7.0) JDBC Drivers 
   * ojdbc5.jar (1,890,499 bytes) - Classes for use with JDK 1.5. It contains the JDBC driver classes, except classes for NLS support in Oracle Object and Collection types. 
   * 
   * http://www.oracle.com/technology/software/tech/java/sqlj_jdbc/htdocs/111070_readme.html
   * BUG-6396242 
   * ArrayOutOfBoundsException while using Jdbc with a big batch. 

   * 但是11g的驱动只有jdk1.5和1.6的版本，BI是要支持jdk1.4的，所以现在只有把batch size改小： 
   * 以前是2000行一提交，现在改成200行提交；
   * 
   * 20090907 BI-2481 BI-2387 BI-2497 
   * batch size =200 还是有可能出上面的异常；
   * 比如：zdsy_b1表，557个字段，大部分字段是number(18,2)类型，batch size只能设置成30，再大就出异常；
   * 这里改为30行提交数据；
   * 
   * 20091023
   * 又出现此问题，Oralce中一个有928个字段的表，大部分是数值型字段，写入时每30行提交，也报这个异常；
   * 现在改为每10行提交数据；
   */
  public static final int BATCHCNT = 10;//批量提交行数
  
  /**
   * 追加记录；
   * 如果目的表不存在，则创建表，再写入数据；
   * 如果目的表存在，先比较目的表和源表表结构，按两表都存在的字段写入记录，其他不同的字段值忽略；
   * 写入时直接写入，不进行任何判断；
   * 如果目的表有主键或者唯一约束字段，碰到已存在的记录时，抛出违反唯一约束异常，日志记录这些异常，继续写入数据；
   * 也就是说如果目的表存在主键，将只插入新记录，已存在的记录被忽略；
   */
  public static final int OPT_APPEND = 0x8;
  
  /**
   * 更新记录；
   * 如果目的表不存在，则创建表，再写入数据；
   * 如果目的表存在，先比较目的表和源表表结构，按两表都存在的字段写入记录，其他不同的字段值忽略；
   * 如果目的表没有主键或者唯一约束，直接写入；
   * 如果目的表有主键或者唯一约束，写入新记录，更新存在的记录；
   * 20091026
   * 十六进制参数，进行 | & 操作，原来的数值不对；
   */
  public static final int OPT_UPDATE = 0x10;// 十六进制 0x10=16
  

  /**
   * 选择一个sql的结果到一个表
   * srcpool:表示源连接池名
   * srcsql:表示源sql,可以是表名
   * destpool:表示目标连接池名
   * desttable:表示目标表
   * 默认规则：
   * 目的表 --> 不存在-->则创建表
   *      |-->存在  --> 则与源表比较结构 --> 结构不同 --> 出异常提示;
   *                                 |--> 结构相同 --> 追加记录；
   * 规则不对请使用带option参数的selectInto方法；
   * 20090803 此方法也应该返回表名，同其他同名方法selectInto；
   */
  public abstract String selectInto(String srcpool, String srcsql, String destpool, String desttable);
  /**
   * 使用数据库连接做参数；
   * @param srccon
   * @param srcsql
   * @param destcon
   * @param desttable
   * @return
   */
  public abstract String selectInto(Connection srccon, String srcsql, Connection destcon, String desttable);

  /**
   * 增加参数option：
   * OPT_CREATENEWTABLE 是否需要创建新表，即使存在给定的表名，也创建新表；
   * OPT_OVERWRITER     如果表存在，且不需要创建新表，判断是否需要覆盖存在的表；
   *                    如果覆盖则删除原表并创建新表；
   * OPT_CLEARTABLE     如果表存在，且不需要创建新表，且不需要覆盖原表，则判断是否需要清空原表记录；
   *                    如果需要清空，则先判断表结构是否相同，结构不同报异常，相同则清空记录；      
   * @param srcpool
   * @param srcsql
   * @param destpool
   * @param desttable
   * @param option
   * @return
   */
  public abstract String selectInto(String srcpool, String srcsql, String destpool, String desttable, int option);
  public abstract String selectInto(ConnectionFactory srccf, String srcsql, ConnectionFactory destcf,
      String desttable, int option);
  
  /**
   * 使用数据库连接做参数；
   * @param srccon
   * @param srcsql
   * @param destcon
   * @param desttable
   * @param option
   * @return
   */
  public abstract String selectInto(Connection srccon, String srcsql, Connection destcon, String desttable, int option);

  /**
   * 将一个sql查询的结果当作一个表的结构和源数据
   * 类似oracle的create table xxx as select xxx from xxx
   * 返回创建的表的表名，可能和给定的表名不一至；
   * 因为给定表名可能超出了数据库对表名的限制长度；
   */
  public abstract String createTableAsSelect(String srcpool, String srcsql, String destpool, String desttable);
  public abstract String createTableAsSelect(ConnectionFactory srcpool, String srcsql,
		  ConnectionFactory destpool, String desttable);
  public abstract String createTableAsSelect(Connection srcconn, String srcsql, Connection destconn, String desttable);
  
  /**
   * 从指定的链接池中备份一张表或者一个sql查出来的数据到文件流；
   * 输出流是 *.db 格式的文件；
   * @param srcpool
   * @param srcsql
   * @param out
   */
  public abstract void exportData(String srcpool,String srcsql,OutputStream out) throws Exception;

  /**
   * 从指定的数据库连接，备份一张表或者一个sql结果集 到文件流；
   * @param conn
   * @param srcsql  可以是表名，或者一个查询sql
   * @param out
   * @throws Exception
   */
  public abstract void exportData(Connection conn, String srcsql, OutputStream out) throws Exception;
  /**
   * 从文件流中恢复数据到数据库中，返回表名；
   * 流格式是 *.db 格式的文件；
   * @param in
   * @param destpool
   * @param tbname
   * @return
   */
  public abstract String importData(InputStream in, String destpool,String tbname,int option) throws Exception;
  /**
   * 从文件流中恢复数据到数据库中，返回表名；
   * 流格式是 *.db 格式的文件；
   * @param in
   * @param conn
   * @param tbname
   * @param option
   * @return
   * @throws Exception
   */
  public abstract String importData(InputStream in, Connection conn, String tbname, int option) throws Exception;
  /**
   * 从dbf文件流恢复数据到数据库；
   * @param file
   * @param destpool
   * @param tbname
   * @param option
   * @return
   * @throws Exception
   */
  public abstract String importDataFromDbf(String file, String destpool,String tbname,int option) throws Exception;
  public abstract String importDataFromDbf(String file, Connection conn, String tbname, int option) throws Exception;
  /**
   * 从指定的链接池中备份一张表或者一个sql查出来的数据到文件流；
   * 输出流是 *.csv 格式的文件；
   * 默认格式： aaa,123,"ald,22",""""
   * @param srcpool
   * @param srcsql
   * @param out
   * @throws Exception
   */
  public abstract void exportDataToCSV(String srcpool,String srcsql,Writer out) throws Exception;
  public abstract void exportDataToCSV(Connection conn,String srcsql,Writer out) throws Exception;
  public abstract void exportDataToCSV(String srcpool,String srcsql,Writer out,char separator, char quote) throws Exception;
  public abstract void exportDataToCSV(Connection conn,String srcsql,Writer out,char separator, char quote) throws Exception;

  /**
   * 从文件流中恢复数据到数据库中，返回导入的表名；
   * 流格式是 *.csv 格式的文件；
   * 标准格式如下：
1.每条记录占一行
2.以逗号为分隔符
3.逗号前后的空格会被忽略
4.字段中包含有逗号，该字段必须用双引号括起来
5.字段中包含有换行符，该字段必须用双引号括起来
6.字段前后包含有空格，该字段必须用双引号括起来
7.字段中的双引号用两个双引号表示
8.字段中如果有双引号，该字段必须用双引号括起来
9.第一条记录，可以是字段名

这里进行了扩展，分隔符(separator)可以指定，双引号也可以用指定的字符(quote)替换；

fields参数用于指定字段名，如果不为空，导入的列数由fields的length决定，少的插入空，多余的列将被忽略；

tbname 指定导入的表名，如果为空出异常提示；
当opt==OPT_CREATENEWTABLE时：
如果tbname存在，则创建一个新表，不存在则创建tbname表，fields参数不为空，则根据其作为字段，类型都为字符串型来创建表，
导入时判断第一行是不是字段名，是字段名则按照指定的列导入数据，不是字段名则当数据导入；
fields参数为空，则根据第一行的字段列数，创建表，字段名随机(field0...9...x),这种情况第一行当做数据导入表；
后面如果有多于第一行的列数，将被忽略；

当opt==OPT_OVERWRITER时：
如果tbname存在，删除之，创建新表tbname，创建规则同上；

当opt==OPT_CLEARTABLE时：
如果tbname不存在，创建新表tbname，创建规则同上；
如果tbname存在，删除其数据，导入数据时，
fields参数不为空（这里要判断fields字段是否和tbname字段一致，如果不一致出异常提示），则根据指定的字段顺序导入列值；
fields参数为空，则不指定导入顺序；
导入时判断第一行是不是字段名，是字段名则按照指定的列导入数据，不是字段名则当数据导入；
后面如果由多余的列值，将被忽略；

   * @param in
   * @param destpool
   * @param tbname
   * @return
   */
  public abstract String importDataFromCSV(Reader in, String destpool,String tbname,String[] fields,int opt) throws Exception;
  public abstract String importDataFromCSV(Reader in, Connection conn,String tbname,String[] fields,int opt) throws Exception;
  public abstract String importDataFromCSV(Reader in, String destpool,String tbname,String[] fields,int opt,int skipline,char separator, char quote) throws Exception;
  public abstract String importDataFromCSV(Reader in, Connection conn,String tbname,String[] fields,int opt,int skipline,char separator, char quote) throws Exception;

  /**
   * 设置进度显示和取消接口
   * 使用ipro记录导入导出的进度；
   * ipro.setProgress(int min, int max, int step)
   * min=0, max=总行数, step=1;
   * 总行数可能为-1，表示不知道总行数；
   * 每写入或者导出BATCHCNT行,设置进度ipro.setPosition(BATCHCNT);
   * ipro.isCancel()会进行判断是否需要取消导入或者导出；
   */
  public abstract void setIprogress(IProgress ipro);
  
  public static DataCopy createInstance(){
    return new DataCopyImpl();
  }
}