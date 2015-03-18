package com.esen.jdbc.dialect;

import java.sql.Connection;
import java.sql.SQLException;

import org.w3c.dom.Document;


/**
 * 数据定义接口
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */
public interface DbDefiner {

  public static final char FIELD_TYPE_INT  = 'I';
  public static final char FIELD_TYPE_STR  = 'C';
  public static final char FIELD_TYPE_DATE = 'D';
  /**
   * Clob字段也可以用getString()读取；
   */
  public static final char FIELD_TYPE_CLOB = 'M';
  /**
   * 长字符串类型；可以用setString(),getString()读取；
   * Oracle:varchar2(4000); Mysql:text; DB2:long varchar;
   * Sybase:text; Mssql:text
   * 
   * @deprecated
   */
  public static final char FIELD_TYPE_memo = 'm';

  public static final char FIELD_TYPE_FLOAT = 'N';
  public static final char FIELD_TYPE_LOGIC = 'L';
  public static final char FIELD_TYPE_BINARY = 'X';
  public static final char FIELD_TYPE_TIME = 'T';
  public static final char FIELD_TYPE_TIMESTAMP = 'P';
  
  public static final char FIELD_TYPE_OTHER = 'U';//无法识别的类型；
  
  /**
   * 从数据库获取一个新的表名，供（建表、视图）使用；
   * 此方法支持多线程同步，集群也支持，一定要和unlockNewTableName()方法配合使用；
   * <pre>
   * 例：
   * String newname = lockNewTableName(conn,tablename);
   * try{
   *   todo...
   * }fainlly{
   *    unlockNewTableName(conn,newname);
   * }
   * </pre>
   * 
   * @param conn
   * @param tablename
   * @return
   * @throws SQLException
   */
  public String lockNewTableName(Connection conn, String tablename) throws SQLException;
  
  /**
   * 配合lockNewTableName()实现多线程下获取新的表名；
   * @param conn
   * @param tablename
   * @throws SQLException
   */
  public void unlockNewTableName(Connection conn,String tablename) throws SQLException;

  /**
   * 创建指定的数据库目录
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  //public void createCatalog(Connection conn,String catalog)throws Exception;
  /**
   * 创建表结构；
   * 根据指定的值（表名，索引名）创建表；
   * 如果表名存在或者超出长度范围，则抛出异常提示；
   * 对索引名如果存在或者超出长度范围，自动更名，使创建成功；
   * 返回创建的表名，调用getIndexNames()获得其索引名集合；
   * @param conn Connection
   * @param tblname String
   * @throws Exception
   * return table name
   */
  public  String createTable(Connection conn,String catalog, String tblname) throws SQLException;
  /**
   * 创建表结构；
   * ifTableNameExistThrowException=true   如果表名存在或者超出长度范围，则抛出异常提示；
   * ifTableNameExistThrowException=false  如果表名存在或者超出长度范围，则改变其值，使创建表成功；
   * 
   * 对索引名如果存在或者超出长度范围，自动更名，使创建成功；---这个过程现在是线程同步的；
   * 
   * 返回创建的表名，调用getIndexNames()获得其索引名集合；
   * @param conn
   * @param catalog
   * @param tblname
   * @param ifTableIndexNameExistThrowException
   * @return
   * @throws Exception
   */
  public String createTable(Connection conn, String tblname,boolean ifTableNameExistThrowException) throws SQLException;
  
  /**
   * 20091021
   * 增加了参数：checkIndexLength
   * 用于检查表的索引、主键的字段总长度是否超过数据库限制；
   * checkIndexLength=true 如果超过,自动更改字段长度；
   * checkIndexLength=false 如果超过,不自动更改字段长度，出异常；
   * 此参数默认为false；
   * 
   * 上面的createTable 方法都采用checkIndexLength=false ；
   * @param conn
   * @param tblname
   * @param ifTableNameExistThrowException
   * @param checkIndexLength
   * @return
   * @throws Exception
   */
  public String createTable(Connection conn, String tblname,boolean ifTableNameExistThrowException,boolean checkIndexLength) throws SQLException;
 
  /**
   * 调用createTable(...)后，起作用，返回创建表的索引名集合；
   * 顺序和defineIndex(...)定义的顺序一致；
   * 调用clearDefineInfo()时清除； 
   * @return
   */
  public String[] getIndexNames();
  
  /**
   * 此函数可以根据一个xml的格式描述在指定的数据库中创建表，或者修复指定的表使其结构和xml中保持一致
   * <p/>
   * xml文档格式：
   * <pre><textarea cols="90" rows="15">
   * 
   * <?xml version="1.0" encoding="utf-8"?>
   * <tablemeta tablename="" primarykey="userid_,bbq_" >
   *   <fields>
   *     <field fieldname="userid_" fielddesc="" sqltype="C" len="30" scale="0" 
   *        autoinc="0" nullalbe="1" unique="0" defaultvalue="" newfieldname="" />
   *     <!-- 字段名字，字段描述，字段数据库类型，字段长度，小数位数，
   *     是否自动增长(0,1) ，是否可以为空(0,1)，是否唯一(0,1)，缺省值，新的字段名…-->
   *   </fields>
   *   <indexes>
   *     <index indexname="index_name" unique="1" fields="userid_,bbq_,btype_"/>
   *     <!--索引名，是否唯一索引(0,1)，索引字段逗号分割…-->
   *   </indexes>
   * </tablemeta>
   * 
   * </textarea></pre>
   * 
   * <b>如果xml中指定的表名在数据库中不存在，那么此函数会试图根据xml的描述创建数据库表</b><br>
   * 
   * <p/>
   * <b>如果xml中指定的表名已在数据库中存在，那么此函数会试图根据xml的描述修复数据库表，修复规则：</b><br>
   * 表中存在但xml中不存在的字段将会被删除<br>
   * 表中存在且xml中存在的字段则对比属性，不一致则修改<br>
   * 如果xml中的field tag定义了属性newfieldname，那么将会被字段重新命名为newfieldname
   * <p/>
   *    
   * @param conn 使用这个数据库连接创建或修复表
   * @param xml xml的Document对象，其结构应该符合上面注释中的描述
   * @return 创建或修复的报表的表名
   * @throws Exception 如果修复或创建不成功，那么抛出异常
   */
  public String repairTable(Connection conn,Document xml) throws SQLException;
  
  /**
   * 此函数可以根据一个xml的格式描述在指定的数据库中创建表，更多信息可参见上面的函数的注释<p/>
   * 
   * @param tableName 如果传递了此参数那么就用此参数当做数据库的表名，否则取xml中的表名信息
   * @param autoChangeTableName 表示如果数据库已经存在了一个相同的表名了，是否修改传入的tableName，找
   *   到一个不冲突的新表名继续创建表，新的表名会作为函数的参数返回，如果此参数为false且表名冲突，那么抛出异常
   * @param autoAdjustFieldLength 表示是否需要根据数据库的限制，自动调整字段长度，比如：主键列组合长度超过限制等；
   * 
   * @return 返回创建的数据库表的表名
   * 
   * @throws 由于任何原因没有执行创建表，那么就抛出异常
   */
  public String createTable(Connection conn, Document xml, String tableName, boolean autoChangeTableName,
      boolean autoAdjustFieldLength) throws SQLException;
  
  /**
   * 此函数可以根据一个xml的格式描述在指定的数据库中创建表，或者修复指定的表使其结构和xml中保持一致，更多信息可参见上面的函数的注释<p/>
   * 
   * <b>如果指定的表名在数据库中不存在，那么此函数会试图根据xml的描述创建数据库表</b><br>
   * 
   * <p/>
   * <b>如果指定的表名已在数据库中存在，那么此函数会试图根据xml的描述修复数据库表，修复规则：</b><br>
   * 表中存在但xml中不存在的字段将会被删除<br>
   * 表中存在且xml中存在的字段则对比属性，不一致则修改<br>
   * 如果xml中的field tag定义了属性newfieldname，那么将会被字段重新命名为newfieldname
   * <p/>
   * 
   * @param tableName 如果传递了此参数那么就用此参数当做数据库的表名，否则取xml中的表名信息
   * @param autoAdjustFieldLength 
   *        表示是否需要根据数据库的限制，自动调整字段长度，比如：主键列组合长度超过限制等；
   *        此参数默认值是false
   * @return 返回创建的数据库表的表名
   * @throws 由于任何原因没有执行创建表或修复表，那么就抛出异常
   */
  public String repairTable(Connection conn, Document xml, String tableName, boolean autoAdjustFieldLength) throws SQLException;

  /**
   * 获取指定表名的表结构（xml格式）
   * 如果con为null，会自动获取连接，查数据库，使用时注意；
   * 表不存在，返回null;
   * @param tbname
   * @return
   * @throws Exception
   */
  public Document getTableMetaData(Connection con ,String tbname) throws SQLException;
  /**
   * 创建视图
   * 这里将不判断视图名是否存在
   * create view viewname [(columns)] as sql ;
   * columns 可以为空；
   * 返回true创建成功
   */
  public void createView(Connection conn,String viewname,String columns,String sql) throws SQLException;
  
  /**
   * 创建视图
   * 这里将不判断视图名是否存在
   * create view viewname [(columns)] as sql ;
   * columns 可以为空；
   * colComments 为对应的每个字段的注释
   * columns 和  colComments 均不为空时两者长度应一致。
   */
  public void createView(Connection conn,String viewname,String[] columns, String[] colComments, String sql) throws SQLException;

  /**
   * 删除视图
   * @param conn
   * @param viewname
   */
  public void dropView(Connection conn,String viewname)throws SQLException;
  
  /**
   * 判断视图是否存在
   * 如果不存在视图viewname，但是数据库中有viewname的事实表，
   * 通过此函数判断不存在视图名viewname,再创建该视图就会出错：已存在的对象名异常；
   * 因此，使用是应注意；
   * @param conn
   * @param viewname
   * @return
   */
  public boolean viewExists(Connection conn,String viewname)throws SQLException;
  
  /**
   * 验证视图是否有效
   * BI-2143 提供验证视图有效的方法。
   * @param conn
   * @param viewname
   * @return
   * @throws SQLException
   */
  public boolean viewValidate(Connection conn,String viewname)throws SQLException;
  /**
   * 判断给定表名是否是事实表或者视图表；
   * 先判断是否是事实表，如果是 返回 true；
   * 如果不是 再判断是否是视图表，如果是 返回true，否则返回false
   * @param conn
   * @param tvname
   * @return
   * @throws Exception
   */
  public boolean tableOrViewExists(Connection conn,String tvname)throws SQLException;
  /**
   * 创建临时表
   * @param conn Connection
   * @param tablename String
   * @throws Exception
   */
  public void createTempTable(Connection conn,String catalog,String tablename)throws SQLException;

  /**
   * 删除表
   * @param conn Connection
   * @param table String
   * @throws Exception
   */
  public void dropTable(Connection conn,String catalog, String table) throws SQLException;
  /**
   * 删除临时表
   * @param conn Connection
   * @param table String
   * @throws Exception
   */
  public void dropTempTable(Connection conn,String catalog,String table)throws SQLException;
  /**
   * 删除数据目录
   * @param conn Connection
   * @param catalog String
   * @throws Exception
   */
  //public void dropCatalog(Connection conn,String catalog)throws Exception;
  /**
   * 更改表名
   * @param conn Connection
   * @param oldname String
   * @param newname String
   * @throws Exception
   */
  public void renameTable(Connection conn, String oldname, String newname) throws SQLException;

  /**
   * 定义自动增长字段
   * 每张表只有一个自增长字段，如果重复定义，只取第一次定义的值；
   * @param thisField String
   * @param gap int
   * @return String
   */
  public void defineAutoIncField(String thisField, int gap);

  public void defineAutoIncField(String thisField, int gap, String desc);
  
  /**
   * 定义各类型字段
   * @param thisField String 字段名
   * @param FieldType char   字段类型
   * @param len int   字段长度
   * @param dec int   浮点型小数位数
   * @param String defaultvalue  默认值
   * @param nullable boolean  是否可以为空
   * @param unique boolean    是否唯一
   */
  public void defineField(String thisField, char FieldType, int len,int dec, String defaultvalue,
                          boolean nullable,boolean unique);
  /**
   * 增加字段属性说明的定义
   * oracle
   *   创建表后执行如下语句
   *   COMMENT ON COLUMN "table_name"."field_name" IS 'desc'
   *   修改表里字段的定义描述
　　     ALTER TABLE表名 MODIFY 字段名 字段名描述;
   * mysql
   *   create table table_name (field_name varchar(10) comment '描述')
   * db2
   *   COMMENT ON COLUMN "DIM_T_HY"."HY_DM" IS '行业代码2';
   *   
   * @param thisField
   * @param FieldType
   * @param len
   * @param dec
   * @param defaultvalue
   * @param nullable
   * @param unique
   * @param desc
   */
  public void defineField(String thisField, char FieldType, int len,int dec, String defaultvalue,
      boolean nullable,boolean unique,String desc);
  public void defineStringField(String thisField,int len,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineIntField(String thisField,int len,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineDateField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineClobField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineMemoField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineFloatField(String thisField,int len,int dec,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineLogicField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineBlobField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineTimeField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  public void defineTimeStampField(String thisField,String defaultvalue,
                          boolean nullable,boolean unique);
  
  /**
   * 20091221
   * 根据字段描述接口创建字段；
   * @param col
   */
  public void defineField(TableColumnMetaData col);
  
  
  /**
   * 定义的字段是否有重复
   * @param field
   * @return
   */
  public boolean haveField(String field);
  /**
   * 定义主键
   * @param fieldNames String "fldname1,fldname2,..."
   *            如果是多个字段，则是字段以','分割的字符串；
   */
  public void definePrimaryKey(String fieldNames);

  /**
   * 定义索引
   * @param indexName String
   * @param expression String
   *          字段的集合，格式："(field1,field2,...)",注意是带括号的；
   *          现在也支持不带括号；
   * @param unique boolean
   */
  public void defineIndex(String indexName, String expression, boolean unique);

  /**
   * 清除定义
   */
  public void clearDefineInfo();

  /**
   * 判断是否存在字段
   * @param conn Connection
   * @param tablename String
   * @param colname String
   * @throws Exception
   * @return boolean
   */
  public boolean colExists(Connection conn, String tablename,
                           String colname) throws SQLException;
  /**
   * 判断是否存在表
   * 经常在创建一个表前调用此函数来判断创建的表是否存在；
   * 这里只判断表名;
   * 例：数据库中是不存在t_name表，但是存在视图t_name，此函数返回false
   * 如果需要广义上的判断，请调用tableOrViewExists(...)方法；
   * @param conn Connection
   * @param tablename String
   * @throws Exception
   * @return boolean
   */
  public boolean tableExists(Connection conn,String catalog, String tablename) throws
  SQLException;

  /**
   * 增加字段
   * @param conn Connection
   * @param tablename String
   * @param newcol String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param nullable boolean
   * @throws Exception
   */
  public void addColumn(Connection conn, String tablename, String newcol,
                        char coltype,
                        int len, int dec,String defaultvalue, boolean nullable,boolean unique) throws SQLException;
  
  /**
   * 提供简单快速的修改字段类型，长度，精度的方法；
   * 这个方法不判断原来的字段类型是什么，直接修改，如果异常直接抛出；
   * 注意：数值精度，如果该列字段上有值，则不支持改小精度，比如：(8,2)->(8,4) 支持改大：：(8,2)->(12,4) 
   *                如果该列是空的，则支持改大或者改小；
   * 此方法还支持date类型转timestamp类型；
   * 不支持字符类型转clob类型；
   * 
   * 对于db2数据库只支持字符类型的增大长度，其他任何修改都不支持；
   * @param conn
   * @param tablename
   * @param fieldname
   * @param coltype
   * @param len
   * @param scale
   * @throws Exception
   */
  public void modifyColumn(Connection conn, String tablename,String fieldname,char coltype, int len,int scale) throws SQLException;
  /**
   * 更改字段属性
   * new_col 为空则不更改列名
   * @param conn Connection
   * @param tablename String
   * @param coltype char
   * @param len int
   * @param dec int
   * @param nullable TODO
   * @param col String
   * @throws Exception
   */
  public void modifyColumn(Connection conn, String tablename, String old_col, String new_col, char coltype, int len,
      int dec, String defaultvalue, boolean unique, boolean nullable) throws SQLException;
  /**
   * 兼容旧方法，旧方法不能修改空还是可空属性；
   * @deprecated
   * @param conn
   * @param tablename
   * @param old_col
   * @param new_col
   * @param coltype
   * @param len
   * @param dec
   * @param defaultvalue
   * @param unique
   * @throws Exception
   */
  public void modifyColumn(Connection conn, String tablename, String old_col, String new_col, char coltype, int len,
      int dec, String defaultvalue, boolean unique) throws SQLException;

  /**
   * 修改字段描述信息
   * @param conn
   * @param tablename
   * @param fieldname
   * @param desc
   * @throws Exception 
   */
  public void modifyColumnForDesc(Connection conn, String tablename,String fieldname,String desc) throws SQLException;
  /**
   * 删除表指定字段
   * @param conn Connection
   * @param tablename String
   * @param col String
   * @throws Exception
   */
  public void dropColumn(Connection conn,String tablename,String col) throws SQLException;
  /**
   * 是否可以将数值型当作字符串来匹配
   * 建议不要使用
   * @return boolean
   */
  public boolean canSetNumAsStr();
  /**
   * 返回一个数据库中不存在的表名；
   * 如果表名：prefix_suffix 缓存中不存在且数据库中不存在，则加入缓存并返回该值；
   * 反之，则换一个名字prefix(1,2,3....)_suffix再重新检测；
   * 如果suffix为空,去掉"_suffix";
   * @param prefix String 不能为空
   * @param suffix String 可以为空
   * @return String "prefix_suffix"
   */
  public String getCreateTableName(Connection conn,String prefix,String suffix)throws SQLException;

  /**
   * 返回指定表中的索引是否存在
   * @param conn
   * @param tablename
   * @param indexname
   * @return
   * @throws Exception
   */
  public boolean indexExists(Connection conn,String tablename, String indexname) throws SQLException;

  /**
   * 返回指定表中的字段上是否存在索引
   * @param conn
   * @param tablename
   * @param fields
   * @return
   * @throws SQLException
   */
  public boolean indexExists(Connection conn, String tablename, String[] fields) throws SQLException;

  /**
   * 创建索引
   * 如果指定索引名不合法或者存在，生成一个合法的索引名，创建成功后返回索引名；
   * 索引会自动更名，这个过程现在是线程同步的；
   * @param conn 
   * @param tablename 表名
   * @param indexname 索引名，因为可能重名，索引创建的不一定是指定的名字，可以为空
   * @param fields 字段数组
   * @param unique 是否唯一
   * @return
   * @throws Exception
   */
  public String createIndex(Connection conn,String tablename,String indexname, String[] fields,boolean unique) throws SQLException;
  /**
   * 创建表索引；
   * ifIndexNameExistThrowException=true   如果指定索引名不合法或者存在，则抛出异常提示；
   * ifIndexNameExistThrowException=false  如果指定索引名不合法或者存在，则改变其值，使创建成功，返回创建的索引名；----这个过程现在是线程同步的；
   * @param conn
   * @param tablename
   * @param indexname
   * @param fields
   * @param unique
   * @param ifIndexNameExistThrowException
   * @throws Exception
   */
  public String createIndex(Connection conn,String tablename,String indexname, String[] fields,boolean unique,boolean ifIndexNameExistThrowException) throws SQLException;
  
  /**
   * 返回数据库中的字段名；
   * 例：oracle数据库fdname="afieldname" ,oracle自动转化为大写，返回"AFIELDNAME"
   * throwsException=true,则检查字段名的合法性，不合法抛异常；
   * =false, 返回合法的字段名，不抛出异常；
   * @param fdname
   */
  public String formatFieldName(String fdname,boolean throwsException);
  
  /**
   * 用DataBaseInfo.getMaxTableLength()代替
   * 兼容1.5版本的方法；
   * @deprecated
   * @return
   */
  public int getMaxTableLength();
}
