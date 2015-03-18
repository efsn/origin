package com.esen.jdbc;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

/**
 * 获得指定数据库表的结构,用Document表示
 * @author zhuchx
 */
public class TestGetTableMetaDataDoc {

  public static void main(String[] args) throws Exception {
    TestGetTableMetaDataDoc t = new TestGetTableMetaDataDoc();
    System.out.println(getMeta(getConnectionFactory(), getTableName()));
  }

  private static ConnectionFactory getConnectionFactory() {
    //    String driver = "com.mysql.jdbc.Driver";
    //    String url = "jdbc:mysql://localhost:3306/vfs?useUnicode=true&characterEncoding=utf8";
    String driver = "oracle.jdbc.driver.OracleDriver";
    String url = "jdbc:oracle:thin:@192.168.1.105:1521:test";
    String user = "bi21";
    String pw = "bi21";
    SimpleConnectionFactory fct = new SimpleConnectionFactory(driver, url, user, pw, "debug");
    ConnectFactoryManagerImpl fm = new ConnectFactoryManagerImpl();
    fm.setConnectionFactory("*", fct);
    DefaultConnectionFactory.set(fm);
    return fct;
  }

  private static String getTableName() {
    return "EBI_SYS21_LOG_ATTACH";
  }

  public static String getMeta(ConnectionFactory fct, String tablename) throws Exception {
    TableMetaData meta = fct.getDialect().createDbMetaData().getTableMetaData(tablename);
    Document doc = saveMeta(meta);
    return XmlFunc.document2str(doc, StrFunc.UTF8);
  }

  public static Document saveMeta(TableMetaData meta) throws Exception {
    Document doc = XmlFunc.createDocument("tablemeta");
    Element ss = doc.getDocumentElement();
    XmlFunc.setElementAttribute(ss, "tablename", String.valueOf(meta.getTableName()));
    Element fields = doc.createElement("fields");
    TableMetaData tmd = (TableMetaData) meta;
    String[] primarykey = tmd.getPrimaryKey();
    if (primarykey != null && primarykey.length != 0) {
      ss.setAttribute("primarykey", ArrayFunc.array2Str(primarykey, ','));
    }
    TableColumnMetaData[] columns = tmd.getColumns();
    if (columns != null) {
      for (int i = 0; i < columns.length; i++) {
        Element field = doc.createElement("field");
        TableColumnMetaData c = columns[i];
        field.setAttribute("fieldname", c.getName());
        //        XmlFunc.setElementAttribute(field, "fieldlable", c.getLabel());
        //        XmlFunc.setElementAttribute(field, "fielddesc", c.getDesc());

        char type = SqlFunc.getSubsectionType(c.getType());
        field.setAttribute("sqltype", String.valueOf(type));
        if (type == DbDefiner.FIELD_TYPE_INT || type == DbDefiner.FIELD_TYPE_FLOAT || type == DbDefiner.FIELD_TYPE_STR)
          field.setAttribute("len", String.valueOf(c.getLen()));
        if (type == DbDefiner.FIELD_TYPE_FLOAT) {
          field.setAttribute("scale", String.valueOf(c.getScale()));
        }
        if (c.isAutoInc()) {
          field.setAttribute("autoinc", c.isAutoInc() ? "1" : "0");
        }
        if (!c.isNullable()) {
          field.setAttribute("nullable", c.isNullable() ? "1" : "0");
        }
        if (c.isUnique()) {
          field.setAttribute("unique", c.isUnique() ? "1" : "0");
        }
        //        XmlFunc.setElementAttribute(field, "defaultvalue", c.getDefaultValue());
        fields.appendChild(field);
      }
    }
    Element indexes = doc.createElement("indexes");
    TableIndexMetaData[] indx = tmd.getIndexes();
    if (indx != null) {
      for (int i = 0; i < indx.length; i++) {
        TableIndexMetaData dx = indx[i];
        Element index = doc.createElement("index");
        //        index.setAttribute("indexname", dx.getName());
        index.setAttribute("unique", dx.isUnique() ? "1" : "0");

        index.setAttribute("fields", ArrayFunc.array2Str(dx.getColumns(), ','));
        indexes.appendChild(index);
      }
    }
    ss.appendChild(indexes);

    ss.appendChild(fields);
    return doc;
  }
}
