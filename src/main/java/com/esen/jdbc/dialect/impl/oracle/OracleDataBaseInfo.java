package com.esen.jdbc.dialect.impl.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;
import com.esen.util.i18n.I18N;

public class OracleDataBaseInfo extends AbstractDataBaseInfo {

  public OracleDataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn,defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_ORACLE;
    this.testSql = "select 1 from dual";
    initOracle(conn);
  }
  
  private void initOracle(Connection conn) throws SQLException {
    /**
    * 初始化字符集, 如果字符集参数没有设置，则根据数据库的字符集自动设置；
    */
    Statement stat = conn.createStatement();
    try {
      /**
       * "select userenv('language') from dual"获得字符集字符串
       * AMERICAN_AMERICA.ZHS16GBK 中文字符集
       * AMERICAN_AMERICA.US7ASCII 英文字符集
       */
      ResultSet rs = stat.executeQuery("select userenv('language') from dual");
      if (rs.next()) {
        characterEncoding = rs.getString(1);
      }
      rs.close();
    }
    finally {
      stat.close();
    }

  }
  
  protected void initDefaultSchema(Connection conn, String defaultSchema) {
    super.initDefaultSchema(conn, defaultSchema);
    default_schema = default_schema.toUpperCase();
    /**
     * 20091117
     * Oracle的用户名通常就是shcemaname，这个schemaname在获取索引，调用meta.getIndexInfo(..)时要用到；
     * 问题：当用户名中间出现空格，比如"bi user" , 在调用meta.getIndexInfo(..)时如果直接使用该用户名，会出现：
     *       ORA-01490: invalid ANALYZE command 
     * 解决办法：如果用户名有空格，则使用引号将其扩起；
     */
    if(default_schema.indexOf(' ')>=0){
      default_schema = "\""+default_schema+"\"";
    }
  }
  protected String getDefaultSchema(Connection conn) {
	  return null;
  }
  protected String getNCharByteLengthSQL() {
    return "select lengthb('中') from dual";
  }
  
  protected String getNCharLengthSQL() {
    /**
     * 20090817
     * Oracle中，varchar2(n) 类型，长度n是字节长度；
     * 即：gbk字符集，一个汉字占两个长度；
     * 这里使用lengthb函数返回字节长度；
     */
    return "select lengthb('中') from dual";
  }

  /**
   * 20091102
   * 创建表时，索引的列组合最大长度是6397
   * 但是在修改索引列的长度时，列组合长度最大长度是4500-5000之间；
   * 否则出现：ORA-01404: ALTER COLUMN 将使索引过大 异常；
   * 这里取4500做Oralce的索引列组合最大长度；
   */
  public int getMaxKeyOfFieldsLength() {
    return 4500;
  }

  public int getMaxIndexOfFieldsLength() {
    return 4500;
  }

  public int getMaxVarcharFieldLength() {
    return 4000;
  }
  
  /**
   * Oracle的jdbc驱动推荐使用10.0.2
   */
  public String check(){
    if(this.driverMajorVersion<10){
      StringBuffer log = new StringBuffer(32);
//      log.append("Oracle数据库驱动版本：").append(this.driverVersion);
//      log.append("; 版本过低，请使用10g的驱动；");
      log.append(I18N.getString("com.esen.jdbc.dialect.impl.oracle.oracledatabaseinfo.versionold", "Oracle数据库驱动版本：{0}; 版本过低，请使用10g的驱动；\");", new Object[]{this.driverVersion}));
      return log.toString();
    }
    return null;
  }
  
}
