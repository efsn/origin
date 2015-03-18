package com.esen.vfs2;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.SimpleConnectionFactory;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

import func.jdbc.FuncConnectionFactory;

public class VfsDBTable {
  private ConnectionFactory dbfct;

  private ConnectionFactory getConnectionFactory() {
    if (dbfct == null) {
      dbfct = FuncConnectionFactory.getMysqlCustomConnectionFactory();
    }
    return dbfct;
  }

  public static void main(String[] args) throws Exception {
    VfsDBTable t = new VfsDBTable();
        t.createTable();
        String xml = t.getTableMetaDataXml();
        System.out.println(xml);
//    t.createTable("vfstable.xml");
  }

  public void createTable() throws Exception {
    ConnectionFactory fct = getConnectionFactory();
    Connection con = fct.getConnection();
    try {
      DbDefiner dd = fct.getDbDefiner();
      if (dd.tableOrViewExists(con, getTableName()))
        return;
      //      dd.defineStringField("PARENTDIR+", 1024, null, true, false);
      //      dd.defineStringField("FILENAME+", 255, null, true, false);
      //      dd.defineStringField("ISFILE+", 1, null, true, false);
      //      dd.defineTimeStampField("CREATETIME+", null, true, false);
      //      dd.defineTimeStampField("MODIFYTIME+", null, true, false);
      //      dd.defineStringField("OWNER_", 50, null, true, false);
      //      dd.defineStringField("MENDER_", 50, null, true, false);
      //      dd.defineStringField("CHARSET_", 20, null, true, false);
      //      dd.defineStringField("MIMETYPE_", 30, null, true, false);
      //      dd.defineFloatField("SIZE_", 12, 0, null, true, false);
      //      dd.defineBlobField("CONTENT_", null, true, false);
      //      dd.definePrimaryKey("PARENTDIR,FILENAME");

      dd.defineStringField("INDEX_", 1024, null, true, false);
      dd.defineStringField("STR_", 1024, null, true, false);
      dd.defineIntField("INT_", 10, null, true, false);
      dd.defineFloatField("FLOAT_", 20, 2, null, true, false);
      dd.defineStringField("LOGIC_", 1, null, true, false);
      dd.defineDateField("DATE_", null, true, false);
      dd.defineTimeField("TIME_", null, true, false);
      dd.defineTimeStampField("TIMESTAMP_", null, true, false);
      dd.defineClobField("CLOB_", null, true, false);
      dd.defineBlobField("BLOB_", null, true, false);
      dd.defineMemoField("MEMO_", null, true, false);
      dd.createTable(con, null, getTableName());
    }
    finally {
      con.close();
    }
  }

  private String getTableMetaDataXml() throws Exception {
    return getMeta(getConnectionFactory(), getTableName());
  }

  private String getTableName() {
    return "vfs_table";
  }

  private void createTable(String filename) throws Exception {
    InputStream in = this.getClass().getResourceAsStream(filename);
    try {
      Document doc = XmlFunc.getDocument(in);
      ConnectionFactory fct = getConnectionFactory();
      Connection con = fct.getConnection();
      try {
        fct.getDbDefiner().repairTable(con, doc);
      }
      finally {
        con.close();
      }
    }
    finally {
      in.close();
    }
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
