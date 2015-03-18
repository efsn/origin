package com.esen.jdbc.dialect.impl.db2;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.SqlConst;
import com.esen.jdbc.dialect.impl.AbstractDataBaseInfo;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.i18n.I18N;

public class DB2DataBaseInfo extends AbstractDataBaseInfo {

  public DB2DataBaseInfo(Connection conn, String defaultSchema) throws SQLException {
    super(conn,defaultSchema);
    this.dbtype = SqlConst.DB_TYPE_DB2;
    this.testSql = "select 1 from sysibm.sysdummy1";
    maxIndexNameLength = 18;
  }
  
  protected void initDefaultSchema(Connection conn,String defaultSchema) {
    super.initDefaultSchema(conn, defaultSchema);
    /**
     * 20090814
     * DB2的schema是用户名的大写；
     */
    default_schema = default_schema.toUpperCase();
  }

  protected String getDefaultSchema(Connection conn) {
  	return null;
  }

	protected String getNCharByteLengthSQL() {
    return "select length('中') from sysibm.sysdummy1";
  }
  
  protected String getNCharLengthSQL() {
    return "select length('中') from sysibm.sysdummy1";
  }

  public int getMaxKeyOfFieldsLength() {
    return 1000;
  }

  public int getMaxIndexOfFieldsLength() {
    return 1000;
  }
  /**
   * 20090922
   * DB2 字符类型最大长度现在改为2000；
   * 原因是：数据库页大小如果是4k， 表字段的总长度不能超过4k，使用原来的3813会很容易超过4k，建表会出异常；
   */
  /**
   * 20090922
   * DB2 字符类型最大长度现在改为2000；
   * 原因是：数据库页大小如果是4k， 表字段的总长度不能超过4k，使用原来的3813会很容易超过4k，建表会出异常；
   * 
   * 创建表时可能会调用这个方法，获取字符字段允许的定义长度；
   * 这里给一个一般情况下，比较大的字符字段定义长度，而不是数据库允许的最大长度；
   * 原因是如果这个值设置的很大，尽管数据允许，但是和表的其他字段长度加起来可能超过页大小限制；
   */
  public int getMaxVarcharFieldLength() {
    return 2000;
  }
  
  public String check(){
    if(this.driverMajorVersion<3){
      StringBuffer log = new StringBuffer(32);
//      log.append("DB2数据库驱动版本：").append(this.driverVersion);
//      log.append("; 版本过低，请使用9.5的驱动；");
      log.append(I18N.getString("com.esen.jdbc.dialect.impl.db2.db2databaseinfo.versionold", "DB2数据库驱动版本：{0}; 版本过低，请使用9.5的驱动；", new Object[]{this.driverVersion}));
      return log.toString();
    }
    return null;
  }
  
}
