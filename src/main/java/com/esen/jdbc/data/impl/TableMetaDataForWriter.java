package com.esen.jdbc.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.dialect.impl.TableColumnMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableIndexMetaDataImpl;
import com.esen.jdbc.dialect.impl.TableMetaDataHelper;
import com.esen.util.StrFunc;

/**
 * 在DataCopy中，不同数据库间copy表数据，检查源表结构是否适合目的数据库，如果不适合做兼容处理；
 * 1）如果表字段名的长度超过了目的数据库的限制，将会更改字段名；
 * 2）某些索引包含表达式，目前不支持这种转换，将会忽略这种索引；
 * @author dw
 *
 */
public class TableMetaDataForWriter extends TableMetaDataHelper {
  private TableMetaData otmd;

  /**
   * 用来存放新旧字段名的对应关系，key为旧字段名，value为截取后的字段名
   */
  private HashMap newColNameMap ;
  
  private Dialect dl;

  private DataBaseInfo dbinf;

  /**
   * 将表结构转换成适合指定数据库的结构
   * 只处理字段；
   * 索引名称的长度在创建表是自动处理，这里不做处理；
   * @param tmd
   * @param ld
   */
  public TableMetaDataForWriter(String tbname,TableMetaData otmd, Dialect dl) {
    super(tbname);
    this.otmd = otmd;
    this.dl = dl;
    dbinf = dl.getDataBaseInfo();
    initColumns();
    initIndexs();
    initPk();
  }
  
  private void initColumns(){
    int colLen = otmd.getColumnCount();
    int colNameMaxLen = dbinf.getMaxColumnNameLength();
    List maxlencols = new ArrayList(5);
    //将源表字段读取出来，写入目的表结构类；
    for (int i = 0; i < colLen; i++) {
      TableColumnMetaData oldColumn = otmd.getColumn(i);
      String columnName = oldColumn.getName();
      int colNameLen = columnName.length();
      if(colNameLen>colNameMaxLen){
        maxlencols.add(columnName);
      }
      addColumn(createTableColumnMetaDataImpl(columnName,oldColumn));
    }
    //检查目的表结构，字段长度大于目的数据库允许长度的，截取处理；
    for (int i = 0; i < maxlencols.size(); i++) {
      String columnName = (String)maxlencols.get(i);
      TableColumnMetaDataImpl coli = (TableColumnMetaDataImpl)getColumn(columnName);
      int colNameLen = columnName.length();
      if(colNameLen>colNameMaxLen){
        coli.setName(getNewColName(columnName,colNameMaxLen));
      }
    }
  }
  
  /**
   * 如果当前字段名长度超过了数据库支持的最大字段名长度maxLen，则截取maxLen-4位，后面添加上0-9999的数字
   */
  private String getNewColName(String columnName,int colNameMaxLen){
    int len = colNameMaxLen-4;
    String subColName = columnName.substring(0, len);
    String newColName = subColName+"0";
    while(getColumn(newColName)!=null){
      int p = StrFunc.str2int(subColName.substring(len),0);
      p++;
      newColName = subColName+p;
    }
    if(newColNameMap==null){
      newColNameMap = new HashMap();
    }
    newColNameMap.put(columnName, newColName);
    return newColName;
    
  }
  
  private TableColumnMetaDataImpl createTableColumnMetaDataImpl(String columnName,TableColumnMetaData oldColumn){
    TableColumnMetaDataImpl newColumn = new TableColumnMetaDataImpl(columnName);
    newColumn.setLable(oldColumn.getLabel());
    newColumn.setDesc(oldColumn.getDesc());
    newColumn.setType(oldColumn.getType());
    //这里不判断精度，创建表时会根据各个数据库的不同做相应的调整；
    newColumn.setLength(oldColumn.getLen());
    if(oldColumn.getScale()<0)
      newColumn.setScale(4);//小数位数小于0，暂时当作4位处理；
    else newColumn.setScale(oldColumn.getScale());
    newColumn.setAutoInc(oldColumn.isAutoInc());
    newColumn.setNullable(oldColumn.isNullable());
    newColumn.setUnique(oldColumn.isUnique());
    newColumn.setDefaultValue(oldColumn.getDefaultValue());
    return newColumn;
  }

  private void initIndexs(){
    TableIndexMetaData[] otimd = otmd.getIndexes();
    if(otimd==null||otimd.length==0) return;
    for(int i=0;i<otimd.length;i++){
      TableIndexMetaData oldIndex = otimd[i];
      String indexName = oldIndex.getName();
      /*
       * ISSUE:BI-8630: added by liujin 2013.06.24
       * Teradata 中有索引名为空的情况
       */
      if(indexName != null && indexName.equalsIgnoreCase("primary")){
        //mysql的主键名字是primary,在其他数据库上是关键字；
        indexName = indexName+getTableName();
      }

      String[] newIndexColumns = getIndexField(oldIndex);
      boolean unique = oldIndex.isUnique();
      /**
       * 20091026 BI-2614
       * 对于有些数据库表，建立了包含函数的索引，比如：create index xxxx on xxb (substr(xxb.lxdm,0,4),xxb.bbq)
       * 由于各个数据库的语法个不一致，要实现不同数据库间的表复制，这样的索引很难“复制”；
       * 现在暂不支持，“复制”时，会过虑掉这些索引；
       */
      boolean val = checkIndexFields(newIndexColumns);
      if(val){
        this.addIndexMeta(createTableIndexMetaDataImpl(indexName,newIndexColumns,unique));
      }
    }

    
  }
  
  private boolean checkIndexFields(String[] newIndexColumns) {
    for(int i=0;i<newIndexColumns.length;i++){
      if(!checkIndexField(newIndexColumns[i])){
        return false;
      }
    }
    return true;
  }

  private boolean checkIndexField(String field) {
    return this.getColumn(field)!=null;
  }


  
  private String[] getIndexField(TableIndexMetaData index){
    String[] oldIndexColumns = index.getColumns();
    if(oldIndexColumns==null||oldIndexColumns.length==0) return null;
    int columnCount = oldIndexColumns.length;
    String[] newIndexColumns = new String[columnCount];
    for(int i=0;i<newIndexColumns.length;i++){
      Object newColumnName = newColNameMap==null?null:newColNameMap.get(oldIndexColumns[i]);
      newIndexColumns[i] = newColumnName==null? oldIndexColumns[i]:newColumnName.toString();
    }
    return newIndexColumns;
  }
  
  private TableIndexMetaDataImpl createTableIndexMetaDataImpl(String newIndexName,String[] newIndexColumns,boolean unique){
    TableIndexMetaDataImpl newIndex = new TableIndexMetaDataImpl(newIndexName,newIndexColumns,unique);
    return newIndex;
  }
  
  private void initPk(){
    String[] oldPk = otmd.getPrimaryKey();
    if(oldPk==null||oldPk.length==0) return;
    String[] npk = new String[oldPk.length];
    for(int i=0;i<npk.length;i++){
      Object pkFieldName = newColNameMap==null?null:newColNameMap.get(oldPk[i]);
      npk[i] = pkFieldName==null? oldPk[i]:pkFieldName.toString();
    }
    this.setPrimaryKey(npk);
  }

 

}
