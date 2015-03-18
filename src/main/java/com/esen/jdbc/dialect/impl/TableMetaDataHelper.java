package com.esen.jdbc.dialect.impl;

import java.sql.*;
import java.util.*;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;
import com.esen.util.exp.Expression;
import com.esen.util.i18n.I18N;

/**
 * 通过已有的信息构造表对象；
 * 所有TableMetaData的实现都继承此类；
 * 
 * 比如将xml格式的表结构，转换成TableMetaData接口实现；
 * DbDefiner定义表结构，也使用此对象；
 * @author dw
 *
 */
public class TableMetaDataHelper implements TableMetaData {
  
  /**
   * 存储字段类TableColumnMetaDataHelper的集合
   */
  protected List columnList;
  
  /**
   * 存储(字段名，字段属性类)的名字对，用于快速根据字段名找到字段属性类；
   * 这里不区分大小写；
   */
  private HashMap colmap;
 
  protected String tablename;
  /**
   * 索引集合
   */
  protected List indexlist;
  /**
   * 主键
   */
  private String[] keys;
  
  public TableMetaDataHelper() {
    
  }

  public TableMetaDataHelper(String tablename){
    this.tablename = tablename;
  }
  
  public TableMetaDataHelper(String tablename,String[] keys,TableColumnMetaData[] meta,TableIndexMetaData[] indexes){
    this(tablename);
    this.setPrimaryKey(keys);
    for(int i=0;meta!=null&&i<meta.length;i++){
      this.addColumn(meta[i]);
    }
    for(int i=0;indexes!=null&&i<indexes.length;i++){
      this.addIndexMeta(indexes[i]);
    }
  }
  
  /**
   * 将xml格式的表结构，转换成TableMetaData接口实现；
   * xml格式参照 DbDefiner.repairTable(Connection conn,Document xml);
   * @pname tablename
   *        指定表名，如果为空，则从xml里面读取表名；
   * @param xml
   */
  public TableMetaDataHelper(String tablename,Document xml){
    this(tablename);
    Element ss = xml.getDocumentElement();
    if(StrFunc.isNull(tablename))
      this.tablename = ss.getAttribute("tablename");
    if(StrFunc.isNull(this.tablename)){
//      throw new RuntimeException("没有指定表名；");
    	 throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.tablemetadatahelper.notabledef", "没有指定表名；"));
    }
    //读取主键
    String pkeys = ss.getAttribute("primarykey");
    if(pkeys!=null&&pkeys.length()>0){
      setPrimaryKey(pkeys.split(","));
    }
    //读取索引
    Element indexes = (Element)ss.getElementsByTagName("indexes").item(0);
    NodeList inds = indexes==null?null:indexes.getElementsByTagName("index");
    if(indexes!=null&&inds.getLength()>0){
      for(int i = 0;i<inds.getLength();i++){
        Element el = (Element)inds.item(i);
        String indexname = el.getAttribute("indexname");
        int unique = StrFunc.str2int(el.getAttribute("unique"),0);
        String indexfields = el.getAttribute("fields");
        TableIndexMetaDataImpl ind = new TableIndexMetaDataImpl(indexname,indexfields,unique==1);
        addIndexMeta(ind);
      }
    }
    //读取字段
    Element fields = (Element)ss.getElementsByTagName("fields").item(0);
    NodeList fds = fields.getElementsByTagName("field");
    for(int i=0;i<fds.getLength();i++){
      Element fd = (Element)fds.item(i);
      String fdname = fd.getAttribute("fieldname");
      String fddesc = fd.getAttribute("fielddesc");
      
      char sqltype = fd.getAttribute("sqltype").charAt(0);
      //len参数值可能是个数学表达式
      int len = calcLen(fd.getAttribute("len"));
      int scale = StrFunc.str2int(fd.getAttribute("scale"),0);
      
      int autoinc = StrFunc.str2int(fd.getAttribute("autoinc"),0);
      int nullable = StrFunc.str2int(fd.getAttribute("nullable"),1);
      int unique = StrFunc.str2int(fd.getAttribute("unique"),0);
      
      String defaultvalue = fd.getAttribute("defaultvalue");
      /**
       * 从xml读取的表字段结构，默认值和字段描述如果没有定义，应该是null，但是从xml读取的却是空串，这里做了转换；
       * 原因是''和null是不同的，可能造成在修改表结构时，由于默认值定义的不同（其实都是null），却多执行了sql语句。
       */
      if(defaultvalue!=null&&defaultvalue.length()==0){
    	  defaultvalue = null;
      }
      if(fddesc!=null&&fddesc.length()==0){
    	  fddesc = null;
      }
      String newfdname = fd.getAttribute("newfieldname");
      
      /**
       * 如果字段定义不唯一，但是缺是单字段的主键或者唯一索引，则认为也唯一；
       * 原因是：根据xml文件repairTable时，xml没有定义字段唯一，却有索引或者主键确定该字段唯一，
       * 从数据库读取该表结构时，会读到该字段唯一，但是xml里面定义的是不唯一，
       * 造成根据xml创建的表的结构和从数据库读取的可能不一致，repairTable总是修改字段结构；
       */
      if(unique!=1){
        //分析主键和索引，确定该字段是否唯一；
        if(isUnique(fdname)){
          unique = 1;
        }
      }
      /**
       * 这里需要注意，xml格式中的字段类型是char(C,N,I,D,...)
       * TableColumnMetaData接口中getType()返回int，是为了获取数据库中的类型；
       * 所以从xml格式获取的TableColumnMetaData接口实例，获取字段类型getType()直接转成char，而不是调用Sqltype.getType()进行转换；
       */
      TableColumnMetaDataHelper col = new TableColumnMetaDataHelper(fdname,fdname,sqltype,len,scale,autoinc,nullable,unique,defaultvalue,fddesc,newfdname);
      addColumn(col);
    }
    
  }
  
  	/**
  	 * 将表结构转换成xml格式；
  	 * @return
  	 * @throws Exception
  	 */
	public Document getXml() throws Exception {
		EasyDbDefiner edb = new EasyDbDefiner();
		return edb.getTableMetaData(this);
	}
  

  private boolean isUnique(String fdname) {
    if(keys!=null&&keys.length==1){
      if(fdname.equalsIgnoreCase(keys[0]))
        return true;
    }
    if(indexlist!=null&&indexlist.size()>0){
      for(int i=0;i<indexlist.size();i++){
        TableIndexMetaDataImpl indx = (TableIndexMetaDataImpl)indexlist.get(i);
        String[] indcols = indx.getColumns();
        if (indx.isUnique()) {//bug：必须是唯一索引才返回true
          if (indcols.length == 1 && fdname.equalsIgnoreCase(indcols[0])) {
            return true;
          }
        }
      }
    }
    return false;
  }

  /**
   * 计算len参数值；
   * 可能是个算术表达式；
   * @param lenstr
   * @return
   */
  private int calcLen(String lenstr) {
    int len = 0;
    try{
      len = Integer.parseInt(lenstr);
    }catch(NumberFormatException nx){
      Expression lenexp = new Expression(lenstr);
      lenexp.compile(null);
      len = (int)lenexp.evaluateInt(null);
    }
    return len;
  }
  
  public void addColumn(TableColumnMetaData col){
    if(col==null)
      return;
    if(colmap==null){
      colmap = new HashMap();
    }
    colmap.put(col.getName().toUpperCase(), col);
    if(columnList==null){
      columnList = new ArrayList();
    }
    columnList.add(col);
  }
  
  public void addIndexMeta(TableIndexMetaData indx){
    if(indx==null)
      return;
    if(indexlist==null)
      indexlist = new ArrayList(5);
    indexlist.add(indx);
  }
  
  public void setPrimaryKey(String keys[]){
    this.keys = keys;
  }
  
  public void setTableName(String tablename){
    this.tablename = tablename;
  }
  
  public void clear(){
    if(colmap!=null)
      colmap.clear();
    if(columnList != null)
      columnList.clear();
    tablename = null;
    if(indexlist!=null){
      indexlist.clear();
    }
    keys = null;
  }
  
  public TableColumnMetaData[] getColumns() {
    if (columnList == null)
      return null;

    TableColumnMetaData cols2[] = new TableColumnMetaData[columnList.size()];
    columnList.toArray(cols2);

    return cols2;
  }

  public TableIndexMetaData[] getIndexes() {
    if(indexlist==null)
      return null;
    TableIndexMetaData indexes[] = new TableIndexMetaData[indexlist.size()];
    indexlist.toArray(indexes);
    return indexes;
  }

  public String[] getPrimaryKey() {
    return keys;
  }

  public String getTableName() {
    return tablename;
  }

  public int getColumnCount() {
    return columnList==null?-1:columnList.size();
  }
  
  public TableColumnMetaData getColumn(int i){
    if(columnList==null)
      return null;
    return (TableColumnMetaData)columnList.get(i);
  }
  
  public TableColumnMetaData getColumn(String colname){
    if(colname==null||colname.length()==0)
      return null;
    if(colmap==null)
      return null;
    return (TableColumnMetaData)colmap.get(colname.toUpperCase());
  }

	public String getColumnDescription(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return null;
		return coli.getDesc();
	}

	public String getColumnLabel(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return null;
		return coli.getLabel();
	}

	public int getColumnLength(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return -1;
		return coli.getLen();
	}

	public String getColumnName(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return null;
		return coli.getName();
	}

	public int getColumnScale(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return -1;
		return coli.getScale();
	}

	public int getColumnType(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return -1;
		return coli.getType();
	}

	public int isNullable(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return -1;
		return coli.isNullable() ? 1 : 0;
	}

	public int isUnique(int i) {
		TableColumnMetaData coli = getColumn(i);
		if (coli == null)
			return -1;
		return coli.isUnique() ? 1 : 0;
	}
  
  /**
   * 创建表需要处理以下情况：
   * <pre>
   * 1)检查主键，如果是单字段主键，那么该字段不能设置唯一，否则建表出错；
   *   CREATE TABLE EBI_SYS22_LOGOPERDIM(ID VARCHAR2(10)  UNIQUE  NOT NULL ,OBJTYPE VARCHAR2(4)  UNIQUE ,OPER VARCHAR2(4) ,OPERDESC VARCHAR2(50) ,"DESC" VARCHAR2(255) ,LOGTYPE VARCHAR2(2) ,PRIMARY KEY (ID))
   *   ORA-02261: 表中已存在这样的唯一关键字或主键
   * 2)检查索引，如果索引的字段（含多个字段），是主键，则忽略此索引；
   *   因为主键会默认定义索引，避免重复定义；
   * 3)检查索引，如果是单字段索引，且该字段定义了唯一属性，则该索引忽略，以免重复创建；
   *   因为数据库中字段的唯一属性是通过唯一索引实现的；
   * </pre>
   */
  public void formatTableMetaDataForCreateTable(){
    if(keys!=null&&keys.length==1){
      TableColumnMetaDataHelper keycol = (TableColumnMetaDataHelper)getColumn(keys[0]);
      if(keycol.isUnique()){
        keycol.setUnique(false);
      }
    }
    
    /*
     * BUG:ESENFACE-1081: modify by liujin 2014.07.21
     * 作为主键的字段，不允许为空
     */
	if (keys != null && keys.length > 0) {
	  for (int i = 0; i < keys.length; i++) {
		  TableColumnMetaDataHelper col = (TableColumnMetaDataHelper)getColumn(keys[i]);
		  if (col != null && col.isNullable()) {
			  col.setNullable(false);
		  }
	  }
	}

    if(indexlist!=null){
      for(int i=indexlist.size()-1;i>=0;i--){
        TableIndexMetaData indxi = (TableIndexMetaData)indexlist.get(i);
        String[] indxcols = indxi.getColumns();
        
        if(isPrimaryKeys(indxcols)){
          indexlist.remove(i);
          continue;
        }
        
        if(indxcols.length==1){
          TableColumnMetaData col = getColumn(indxcols[0]);
          if(col.isUnique()){
            indexlist.remove(i);
            continue;
          }
        }
      }
    }
  }

  private boolean isPrimaryKeys(String[] indxcols) {
    if(keys==null||keys.length==0)
      return false;
    if(keys.length!=indxcols.length)
      return false;
    for(int i=0;i<indxcols.length;i++){
      int p = ArrayFunc.find(keys, indxcols[i], true, -1);
      if(p<0){
        return false;
      }
    }
    return true;
  }

	public int getFieldSqlType(String field) {
		if(StrFunc.isNull(field)){
//			throw new RuntimeException("字段名为空；");
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.tablemetadatahelper.fieldnull", "字段名为空；"));
		}
		TableColumnMetaData col = getColumn(field);
		if (col == null) {
//			throw new RuntimeException("表" + tablename + "没有这个字段：" + field);
			Object[] param=new Object[]{tablename,field};
			throw new RuntimeException(I18N.getString("com.esen.jdbc.dialect.impl.tablemetadatahelper.nosuchfieldt", "表{0}没有这个字段：{1}",param));
		}
		return col.getType();
	}

	public char getFieldType(String field) {
		return SqlFunc.getType(getFieldSqlType(field));
	}

	public String getRealFieldName(String field) {
		TableColumnMetaData col = this.getColumn(field);
		if (col != null) {
			return col.getName();
		}
		return null;
	}

	public boolean haveField(String field) {
		return getColumn(field) != null;
	}

	/**
	 * 子类实现
	 */
	public Object[] getFieldSample(String field, int howToSample) throws SQLException {
		return null;
	}

	
}
