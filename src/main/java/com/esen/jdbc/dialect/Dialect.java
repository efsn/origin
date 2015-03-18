package com.esen.jdbc.dialect;

import java.sql.*;

import com.esen.jdbc.ResultMetaData;

/**
 * <p>Title: Esensoft JDBC</p>
 * <p>Description: DataSource,DbDefiner</p>
 * <p>Copyright: Copyright (c) 2005</p>
 * <p>Company: Esensoft</p>
 * @author dw
 * @version 1.0
 */

public interface Dialect {

  public DbDefiner createDbDefiner();
  /**
   * 获得数据库类型
   * @return
   */
  public DataBaseInfo getDataBaseInfo();
  
  /**
   * 获得一个访问数据库结构信息的接口；
   * 20090713
   * 此方法内部会获取连接，如果外部还有个连接没有关闭，就会造成嵌套连接的情况；
   * 通过SqlFunc.createDialct(conn).createDbMetaData()可以解决这个问题，这里conn是外部连接；
   * 或者调用下面的createDbMetaData(conn)，也可以解决这个问题；
   * @throws Exception 
   */
  public DbMetaData createDbMetaData() throws SQLException;
  
  /**
   * 20090713
   * 获得一个访问数据库结构信息的接口;
   * 通过外部连接获取DbMetaData接口的实例；
   * 增加这个方法是为了解决在一个连接内部调用createDbMetaData()方法后获取表结构产生的嵌套获取连接问题；
   * 
   * 此方法总返回DbMetaData的新实例；
   * @param conn
   * @return
   * @throws Exception
   */
  public DbMetaData createDbMetaData(Connection conn) throws SQLException;
  
  /**
   * 获得指定表名的指定字段的结构信息；
   * 找不到该字段，返回null;
   * 20090713
   * 此方法内部需要获取指定表结构，需要调用连接；
   * 如果在一个外部连接没有关闭时调用，会造成嵌套获取连接问题；
   * 这时请使用下面的getTableColumnMetaData(conn,tbname,columnname)方法；
   * @param tbname
   * @param columnname
   * @return
   */
  public TableColumnMetaData getTableColumnMetaData(String tbname,String columnname) throws SQLException;
  
  /**
   * 获得指定表名的指定字段的结构信息；
   * 找不到该字段，返回null;
   * conn是外部连接，此方法解决嵌套获取连接问题；
   * @param conn
   * @param tbname
   * @param columnname
   * @return
   * @throws Exception
   */
  public TableColumnMetaData getTableColumnMetaData(Connection conn,String tbname,String columnname) throws SQLException;
  
  /**
   * 使用快速的方法获得一个sql返回的结果集的结构信息，最好不用执行sql语句
   * 如果sql不是一个合法的sql此函数触发异常。
   */
  public ResultMetaData getQueryResultMetaData(String sql) throws SQLException;
  
  /**
   * 分页查询的结果集中最后一行是否自动添加上了序号
   * 添加了的话,序号从1开始
   * @return boolean
   */
  public boolean resultEndWithIndex();
  /**
   * 是否支持查询数据库的前 n 行数据
   * 是的话只返回前n行数据
   * @return boolean
   */
  public boolean supportsLimit();

  /**
   * 是否支持查询 一个区间中的数据
   * 支持的话返回的结果集只含该区间的数据
   * @return boolean
   */
  public boolean supportsLimitOffset();

  /**
   * 是否支持在视图的列上创建注释
   * @return boolean
   */
  public boolean supportsViewColumnComment();
  
  /**
   * 是否支持在表上创建注释
   * @return boolean
   */
  public boolean supportsTableComment();
  
  /**
   * 是否支持在表的列上创建注释
   * @return boolean
   */
  public boolean supportsTableColumnComment();


  /**
   * 返回从offset开始的limit行数据的sql语句；
   * 比如 offset=2,limit=3 将返回数据库 [1..10] 中的 3,4,5这三条数据
   * 如果一个数据库不支持 查询一个区间的数据,可以取 offset=0 ,返回的将是取前 limit行数据的语句
   * @param querySelect String
   * @param offset int
   * @param limit int
   * @return String
   */
  public String getLimitString(String querySelect, int offset, int limit);

  /**
   * 根据传入的sql语句querySelect构造一个可以查询出此sql语句可以返回多少条记录的sql语言
   * 如如果传入select * from xxb 则返回 select count(*) from xxb
   * 如果不能成功转换则触发RuntimeException异常，并在异常的message中说明原因
   * ......
   */
  public String getCountString(String querySelect);

  /**
   * 返回从offset开始的limit行数据的有限结果集；
   * 比如 offset=2,limit=3 将返回数据库 [1..10] 中的 3,4,5这三条数据
   * 比如 offset=0,limit=3 将返回数据库 [1..10] 中的 1,2,3这三条数据
   * 使用ResultSet结果集遍历来实现；
   * 此函数会先将querySelect调用分页函数和计数函数获取对应的sql执行，如果出异常，则改为直接执行querySelect，通过结果集遍历进行分页；
   * 然后根据offset起始位置，limit返回行数进行限制；
   * 返回一个ResultSet，行数<=limit
   * 遍历完后需要关闭这个ResultSet;
   * @param querySelect
   * @param offset
   * @param limit
   * @return
   *
   */
  public ResultSet queryLimit(Statement stmt, String querySelect, int offset,
                              int limit) throws SQLException;

  /**
   * 返回支持的最大字段数
   * @return int
   */
  public int getMaxColumn();

  /**
   * 是否可以将数值型当作字符串来匹配
   * 建议不要使用
   * @return boolean
   */
  public boolean canSetNumAsStr();

  /**
   * 获取成批从一个报表复制数据到另个报表的sql语句
   * @param selClause String
   * @param fromClause String
   * @param whereClause String
   * @param intoClause String
   * @return String
   */
  public String getInsertSelectSql(String selClause, String fromClause,
                                   String whereClause, String intoClause);
  /**
   * 以下提供的是本地函数转变为数据库函数的转换函数,结果如果是""空 表示不支持,
   * 否则返回 相应的字符串,都是原子函数
   * 所有的返回结果前后都有空格,因此 组合成sql语句是不需要添加额外的空格
   * 返回结果中 oracle的汉字长度视为一,而mySql中为二
   */
  //字符函数
  public String funcChar(String ascii);
  public String funcCode(String sourece);
  /**
   * 20090904  
   * FIND(C1,C2)  
   * 在串C2中查找子串C1，并返回子串第一次出现的位置，（0代表第一个字符)；
   * 如果没找到，则返回-1。与Search不同，FIND区分大小写。  
   * 参数  
   * C1、C2  
   * 均表示字符串，其中C1表示要查找的子串  
   * 示例  
   * 在串C2中查找子串C1，并返回子串第一次出现的位置，返回值为整数  
   * FIND("AB","AAABBBBB")=2
   * FIND("Ab","AAABBBBB")=-1
   * @param sub
   * @param toFind
   * @return
   */
  public String funcFind(String c1,String c2);
  /**
   * 同mid函数
   * 
   * 20090711
   * len=0,返回空串；
   * len<0,不支持
   * @param source
   * @param len
   * @return
   */
  public String funcLeft(String source,String len);
  /**
   * 同mid函数
   * len=0,返回空串；
   * len<0,不支持
   * @param source
   * @param len
   * @return
   */
  public String funcRight(String source,String len);
  /**
   * 取指定字符字段从iFrom开始len长的字符串值；
   * iFrom,len可以是字段变量
   * @param field
   * @param iFrom
   *        从0开始，0表示第一个字符 
   * @param len
   *        这里的长度len，和funcLen(field)获取的长度一致；
   *        这样无论数据库是什么字符集，下面的公式总是成立的：
   *        mid('中国a',0,len('中'))='中'
   *        len=0,返回空串；
   *        len<0,不支持
   * @return
   */
  public String funcMid(String field,String iFrom,String len);
  /**
   * 获取指定参数在数据库中的长度；
   * 返回的长度由数据库字符集来确定；
   * 如果是中文字符集，一个汉字是一个长度，即：len('中国a')=3
   * 如果是非中文字符集，返回的是字节长度，由字符集决定，即：len('中国a')=5
   *       这里返回5是假定该字符集一个中文占2个字节；
   * @param field
   * @return
   */
  public String funcLen(String field);
  public String funcLower(String field);
  public String funcUpper(String field);
  /**
   * 20090823
   * 查找字串sub在toSearch中的位置；
   * 注意：不区分大小写；
   * 0 表示第一个位置
   * -1 表示没有找到
   * 例：search('a','abc')=0
   *     search('b','abc')=1
   *     search('ac','abc')=-1
   * @param sub
   * @param toSearch
   * @return
   */
  public String funcSearch(String sub,String toSearch);
  public String funcRepeat(String field,String count);
  /**
   * oracle 不用判断传入的为null,回处理成 ""
   */
  public String funcStrCat(String field1,String field2);
  public String funcWholeReplace(String source,String oldSub,String newSub);
  public String funcTrim(String field);
  // null转为默认值的函数
  //public String funcNullvalue(String field ,String defaultValue);
  //时间函数
  public String funcToday();//当前时间(完整的日期+24时
  public String funcDate(String yy,String mm,String dd);
  public String funcYear(String datefield);
  public String funcMonth(String datefield);
  public String funcDay(String datefield);
  /**
   * 返回系统当前时间串，返回值为字符串：HH:MM:SS。
   */
  public String funcNow();
  /**
   * 求两个日期间的天数；
   * 两个参数必须是日期类型；
   * @param datefield
   * @param datefield2
   * @return
   */
  public String funcDays(String datefield,String datefield2);
  
  /**
   * 求两个日期间的秒数(datefield - datefield2)；
   * 对于db2数据库参数必须是timestamp类型；
   * 其他数据库date, timestamp都可以
   * 
   * @param datefield
   * @param datefield2
   * @return
   */
  public String funcSeconds(String datefield,String datefield2);
  
  //数学函数
  public String funcAbs(String d);
  public String funcC(String d);
  public String funcCos(String d);
  public String funcSin(String d);
  public String funcTan(String d);
  public String funcEven(String d);
  public String funcExp(String d);
  public String funcSqrt(String d);
  public String funcFact(String d);
  public String funcInt(String d);
  public String funcSign(String d);
  public String funcLn(String d);
  public String funcLog(String d,String dValue);
  public String funcMod(String iValue,String i);
  public String funcPi();
  public String funcPower(String dValue,String d);
  
  /**
   * 获取随机数
   * 取值范围为 0.0  到 1.0
   * 
   * @return
   */
  public String funcRand();
  
  public String funcRound(String d,String i);
  public String funcTrunc(String d,String i);
  //其他函数
  public String funcIf(String b,String t,String f);//条件
  //类型转换函数
  public String funcAsInt(String v);
  public String funcAsNum(String v);
  public String funcAsStr(String v);
  
  /**
   * 将日期字符串转化为Date类型，用于sql日期比较；
   * 日期字符串格式：yyyymmdd
   * 前四位必须是数字，后面可以为'--'，处理时把'--'转化为'01'
   * 以oracle 为例：
   * funcToDate('200605--') 返回：to_date('20060501','YYYYMMDD')
   * date也可以为空值，表示取个日期的空值
   * @param date
   * @return
   */
  public String funcToDate(String date);
  /**
   * 将日期（带时间）的字符转化为sql时间类型;
   * dtstr格式为：yyyy-dd-mm hh:mi:ss
   * @param dtstr
   * @return
   */
  public String funcToDateTime(String dtstr);
  /**
   * 将date字段类型值转化为日期字符串
   * 以Oracle的语法为标准，统一其他数据库的格式；
   * style为转化格式：YYYY(年),MM(月01-12),DD(月日01-31),D(周日1-7),DDD(年日001-356),Q(季),WW(年周01-53),W(月周1-5)
   *      HH(小时01-12),HH24(小时01-23),MI(分01-59),SS(秒01-59)          
   * 组合格式：YYYY-MM-DD HH24:MI:SS
   * orcale为例：to_char(datefield,'YYYYMMDD')
   * 如果style为空，默认取年月日(YYYYMMDD)；
   * style不区分大小写；
   * @param datefield
   * @return
   */
  public String funcDateToChar(String datefield,String style);
  /**
   * 将字符类型字段，转换成日期类型；
   * style为转化格式：YYYYMMDD,YYYYMM,YYYY
   * @param charfield
   * @param style
   * @return
   */
  public String funcCharToDate(String charfield,String style);
  
  /**
   * 将常量转换成指定的数据库类型
   * String   ->  I,N,D,P
   * Ineger,Long
   * Double,Float
   * java.util.Date
   * java.sql.Date
   * java.sql.Timestamp
   * java.util.Calendar
   * @param o
   * @param destsqltype
   * @return
   */
  public String funcToSqlConst(Object o, int destsqltype );
  
  /**
   * 将一个字段转换成需要的字段类型；
   * I  ->  N,C,D,P
   * N  ->  I,C,D,P
   * C  ->  I,N,D,P
   * D  ->  I,N,C,P
   * P  ->  I,N,C,D
   * @param var 变量，多数时候是字段名
   * @param srcSqlType  指定变量的数据库字段类型
   * @param destsqltype 要转换的目的数据库字段类型
   * @param style   转换格式，主要用于date类型转换，可以为空
   * @return
   */
  public String funcToSqlVar(String var ,int srcSqlType, int destsqltype ,String style);
  
  /**
   * 如果字段str的值为空，则取str2
   * @param str 一般是字段名
   * @param str2  可以是字段名，也可以是常量；
   * @return
   */
  public String ifNull(String str,String str2);
  /**
   * 是否支持正则表达式匹配查找
   * @return
   */
  public boolean supportRegExp();
  /**
   * 将正则表达式匹配 转化为sql语句；
   * @param field  字段名
   * @param regexp 匹配的正则表达式
   * @return
   */
  public String regExp(String field,String regexp);

  /**
   * 日期加减函数；
   * 'y[Y]' 年加减 求前几年，后几年
   * 'm[M]' 月加减 求前几月，后几月
   * 'd[D]' 日加减 求前几天，后几天
   * @param datefield 日期类型字段，如果是字符类型，先转换成日期型
   * @param offset
   * @param ymd  日期单位
   * @return  日期类型
   */
  public String formatOffsetDate(String datefield,int offset,char ymd);
  /**
   * 获得varchar字段的最大长度
   * @return
   */
  public int getMaxVarCharLength();
  
  /**
   * 转义通配符的转义字符；
   */
  public static final char ESCAPECHAR = '$';
  /**
   * 当需要对查找的值，精确匹配时，调用此函数；<br>
   * 精确匹配通配符(%,_)和默认转义字符($)，单引号(')也能匹配；<br>
   * value中的?,*转换成sql中的通配符_,%；<br>
   * 
   * 主要用于like条件表达式；<br>
   * a'b_c%$d  -->  'a''b$_c$%$$d' escape '$'  <br>
   * 对?,*匹配成sql中的通配符_,%：<br>
   * a'b_c%$d?*e  -->  'a''b$_c$%$$d_%e' escape '$'<br>
   * @param value
   * @return 
   */
  public String formatLikeCondition(String value);
  
  /**
   * 精确匹配通配符(%,_)和指定的转义字符(escape)，单引号(')也能匹配；<br>
   * escapeWildcard=true  则?,*转换成sql中的通配符_,%；<br>
   * escapeWildcard=false 则?,*也精确匹配；<br>
   * @param value
   * @param escape
   * @param escapeWildcard
   * @return
   */
  public String formatLikeCondition(String value,char escape,boolean escapeWildcard);
  
  /**
   * 精确匹配通配符(%,_)和默认转义字符($)，单引号(')也能匹配；
   * value中的?,*转换成sql中的通配符_,%；
   * @return
   */
  public String formatLikeCondition( String prefix,String value, String sufix);
  
  /**
   * 精确查找匹配的字符串，加前缀后缀;
   * 指定转义字符escape
   * 主要用于like条件表达式；
   * formatCondition(null,"a'b_c%$d","%");  --> 'a''b$_c$%$$d%' escape '$'
   * formatCondition("%'*",null,null);      --> '%''%' 
   * formatCondition("%'*","a*",null);      --> '%''%a%' 
   * formatCondition("%'*","a*%",null);      --> '%''%a%$%'  escape '$'
   * formatCondition("%'*","a*%","b*");      --> '%''%a%$%b%'  escape '$'
   * @param prefix
   *        字符串value的前缀，前缀不处理通配符'%','_'和转义字符，但是处理单引号'\''；
   *        ?,*总是转换成sql中的通配符_,%;
   *        prefix可以为空；
   * @param value  
   *        需要精确查找的字符串，包括通配符'%','_',还有转义字符escape，单引号'\''这里也能匹配；
   *        escapeWildcard=true 则?,*转换成sql中的通配符_,%;
   *        escapeWildcard=false 则?,*也精确匹配；
   *        value可以为空；
   * @param sufix
   *        字符串value的后缀，后缀不处理通配符'%','_'和转义字符，但是处理单引号'\''；
   *        ?,*总是转换成sql中的通配符_,%;
   *        sufix可以为空；
   * @param escape
   *        指定转义字符；
   * @param escapeWildcard
   *        是否对精确查找的字符串value中的?,*进行转义；
   *        true ：转义成_,% ;
   *        false：不转义；
   *        prefix,sufix不受此参数影响；
   * @return
   */
  public String formatLikeCondition(String prefix, String value, String sufix,char escape,boolean escapeWildcard);
  
  /**
   * 对特殊字符，比如： ''' 的处理；
   * mysql需要处理 '\' ,其他数据库不用处理
   * 
   * 用于生产sql的条件时，比如： "field like '"+dl.formatValueSql(value)+"'"
   * 生成insert语句时，比如： "insert into testtb (field1,field2)value('aa','"+dl.formatValueSql(value)+"')"
   * 用预处理PreparedStatement.setString(1,value) 不用处理，jdbc里面处理了；
   * 
   * @param value
   * @return
   */
  public String formatConstStr(String value);
  
  /**
   * 给数据库对象名，比如字段名，加上双引号。
   * 当对象名是关键字时，必须加上引号。
   * 一般情况下使用双引号，特别的mysql使用'`',sqlserver使用'[',']'
   * @param fieldname
   * @return
   */
  public String addQuote(String fieldname);
  /**
   * 用数据库函数计算标准差；
   * @param field
   * @return
   */
  public String funcStdev(String field);
  
  /**
   * 获得指定字符串在数据库中的长度；
   * 此方法要连接数据库
   * @param str
   * @return
   */
  public int getStrLength(String str) throws SQLException ;
  
  /**
   * 返回删除tbname中和tbname2共有的数据的sql;
   * tbname,tbname2中的主键个数必须一致，数组中的顺序一一对应；
   * @param tbname
   * @param tbname2
   * @param keys
   *          tbname中的主键
   * @param keys2
   *          tbname2中的主键
   * @return
   */
  public String getDeleteSql(String tbname,String tbname2 ,String[] keys, String[] keys2);
  
  /**
   * 从一个查询语句创建表结构，并写入数据；
   * CREATE [ [ LOCAL ] { TEMPORARY | TEMP } ] TABLE table_name [ (column_name [, ...] ) ]
   * AS query
   * 描述
   * CREATE TABLE AS 创建一个表并且用来自 SELECT 命令计算出来的数据填充该表． 该表的字段和 SELECT 输出字段 的名字及类型相关．(只不过你可以通过明确地给出一个字段名字 列表来覆盖 SELECT 的字段名)．
   * 
   * 除DB2外，其他数据库都可以创建表同时插入数据；
   * DB2 返回的sql语句只能创建表，不能插入数据；
   * DB2 调用此函数时，请注意；
   * 还有Oracle 如果创建临时表，也不能插入数据；
   * @param tablename
   * @param querysql
   * @param istemp 是否是临时表；
   * @throws Exception
   */
  public String getCreateTableByQureySql(String tablename,String querysql,boolean istemp);
  
  /**
   * 根据数据库直接位数来截取一个串
   * 每个字符集占用的长度可能都不一样，例如：在GBK字符集的数据库里面一个汉字占用2个字节，UTF8下面一个汉字占三个字节
   * @param source
   * @param len
   * @return
   */
  public String funcLeftB(String source,String len);
  public String funcRightB(String source,String len);
  public String funcMidB(String source,String i,String n);
  
  /**
   * @param f 获取字段的字节长度
   * @return
   */
  public String funcLenB(String f);
  
	/*
	 * BUG:BI-8606: add by liujin 2013.06.27 
	 * Teradata 数据库不支持 dense_rank 函数。
	 */
	/**
	 * 是否支持 dense_rank() 函数
	 * 
	 * @return boolean
	 */
	public boolean supportsDenseRank();

	/**
	 * 是否支持full join连接方式 这里的支持，
	 * 是指支持得较好 如在我们的系统中认为Oracle/Mysql等对Full
	 * join支持都不好，都是改用Union/Union all来实现类似效果的 因此默认返回false
	 * 
	 * @return boolean 是否支持full join
	 */
	public boolean supportsFullJoin();

	/**
	 * COALESCE ( expression [ ,...n ] ) 函数
	 * 返回表达式中第一个非空表达式
	 * 
	 * @param expressions
	 *            字段名、常量等任何类型表达式字符串
	 * @return 相应的数据库函数的字符串
	 */
	public String funcCoalesce(String[] expressions);
	
	/**
	 * 获取清空表数据的 sql 语句
	 * 
	 * @param tablename
	 * @return
	 */
	public String getTruncateTableSql(String tablename);
	
	/**
	 * 是否支持在事务中创建表
	 * 
	 * @return 支持返回 true，否则返回 false
	 */
	public boolean supportCreateTableInTransaction();

}
