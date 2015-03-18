package com.esen.jdbc.dialect.impl.kingbasees;

import java.sql.Connection;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.DbMetaDataImpl;

/**
 * 实现人大金仓数据库的DbMetaData接口；
 * 暂时只支持用户的默认模式：PUBLIC
 * @author dw
 *
 */
public class KingBaseESDbMetaData extends DbMetaDataImpl {

  public KingBaseESDbMetaData(ConnectionFactory dbf){
    super(dbf);
  }
  
  public KingBaseESDbMetaData(Connection con) {
    super(con);
  }
  
  protected TableMetaData createTableMetaData(String tablename){
    return new KingBaseESTableMetaData(this,tablename);
  }
}
