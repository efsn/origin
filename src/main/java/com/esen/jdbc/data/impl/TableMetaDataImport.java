package com.esen.jdbc.data.impl;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.i18n.I18N;

/**
 * 从一个表中，选取指定字段，构造表结构；
 * 如果源表的主键和索引中的字段，包含没有选择的字段，则新的表结构将不创建这种索引或者主键；
 * 
 * @author Administrator
 *
 */
public class TableMetaDataImport extends TableMetaDataHelper {
  private AbstractMetaData meta;
  private int[] importfieldindex;
  private String[] importfields;
  /**
   * 描述只写入指定字段的表结构；
   * @param meta
   * @param importfields
   *        指定的字段列表，不能为空；
   */
  public TableMetaDataImport(String tbname,AbstractMetaData meta, String[] importfields) {
    super(tbname);
    this.meta = meta;
    this.importfields = importfields;
    
    this.importfieldindex = new int[importfields.length];
    for(int i=0;i<importfields.length;i++){
      int index = getFieldIndex(importfields[i],meta);
      if(index<0)
        //throw new RuntimeException("指定的字段名"+importfields[i]+"不存在；");
    	  throw new RuntimeException(I18N.getString("com.esen.jdbc.data.impl.tablemetadataimport.fielddoesnotexist", "指定的字段名{0}不存在；", new Object[]{importfields[i]}));
      importfieldindex[i] = index;
    }
    initColumns();
    initPKeys();
    initIndexes();
  }
  
  private void initIndexes() {
    if(meta instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)meta;
      TableIndexMetaData[] indexes = tmd.getIndexes();
      if(indexes==null){
        return;
      }
      for(int i=0;i<indexes.length;i++){
        TableIndexMetaData indxi = indexes[i];
        String[] indxfields = indxi.getColumns();
        if(!inImportFields(indxfields)){
          continue;
        }
        this.addIndexMeta(indxi);
      }
    }
    
  }
  
  private boolean inImportFields(String[] indxfields) {
    for(int i=0;i<indxfields.length;i++){
      if(!inImportField(indxfields[i])){
        return false;
      }
    }
    return true;
  }

  private void initPKeys() {
    if (meta instanceof TableMetaData) {
      TableMetaData tmd = (TableMetaData) meta;
      String[] pkeys = tmd.getPrimaryKey();
      if (pkeys == null) {
        return;
      }
      for (int i = 0; i < pkeys.length; i++) {
        if (!inImportField(pkeys[i])) {
          return;
        }
      }
      this.setPrimaryKey(pkeys);
    }
  }
  
  private boolean inImportField(String keyfield) {
    for(int i=0;i<importfields.length;i++){
      if(keyfield.equalsIgnoreCase(importfields[i])){
        return true;
      }
    }
    return false;
  }

  private void initColumns() {
    if(meta instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)meta;
      for(int i=0;i<importfieldindex.length;i++){
        TableColumnMetaData coli = tmd.getColumn(importfieldindex[i]);
        this.addColumn(coli);
      }
    }else{
      for(int i=0;i<importfieldindex.length;i++){
        int p = importfieldindex[i];
        TableColumnMetaDataImpl columni = new TableColumnMetaDataImpl(meta.getColumnName(p),meta.getColumnLabel(p),meta.getColumnType(p),
            meta.getColumnLength(p),meta.getColumnScale(p));
        this.addColumn(columni);
      }
    }
    
  }
  
  private int getFieldIndex(String cname, AbstractMetaData meta2) {
    for(int i=0;i<meta2.getColumnCount();i++){
      String cn = meta2.getColumnName(i);
      if(cn.equalsIgnoreCase(cname))
        return i;
    }
    return -1;
  }

}
