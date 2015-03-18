package com.esen.jdbc.dialect.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.SqlFunc;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.Dialect;
import com.esen.jdbc.dialect.TableColumnMetaData;
import com.esen.jdbc.dialect.TableIndexMetaData;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.util.ArrayFunc;
import com.esen.util.StrFunc;
import com.esen.util.XmlFunc;

/**
 * 此类提供通过xml格式的表结构，创建并修复表；
 * @author dw
 *
 */
public class EasyDbDefiner {
  public EasyDbDefiner() {
  }
  
  public Document getTableMetaData(Dialect dl ,String tablename) throws SQLException{
    DialectImpl dl2 = (DialectImpl)dl;
    Connection conn = dl2.getConnection();
    try{
      /**
       * 20090304re
       * 这里支持获取视图的结构；
       */
      if(!dl.createDbDefiner().tableOrViewExists(conn,  tablename))
        return null;
    }finally{
      dl2.closeConnection(conn);
    }
    TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tablename);
    return getTableMetaData(tmd);
  }
  
  public Document getTableMetaData(AbstractMetaData tmd) throws SQLException {
    DocumentBuilder builder = null;
	try {
		builder = XmlFunc.getDocumentBuilder();
	}
	catch (Exception e) {
		SQLException se = new SQLException(e.getMessage());
		se.setStackTrace(e.getStackTrace());
		throw se;
	}
    Document doc = builder.newDocument();
    Element ss = doc.createElement("tablemeta");
    saveMeta(ss,tmd); //保存表结构到内存
    doc.appendChild(ss);
    return doc;
  }
  
  
  private void saveMeta(Element ss, AbstractMetaData meta) {
    if(meta instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)meta;
      XmlFunc.setElementAttribute(ss,"tablename", String.valueOf(tmd.getTableName()));
    }
    Document doc = ss.getOwnerDocument();
    Element fields = doc.createElement("fields");
    if(meta instanceof TableMetaData){
      TableMetaData tmd = (TableMetaData)meta;
      String[] primarykey = tmd.getPrimaryKey();
      if (primarykey != null && primarykey.length != 0) {
        ss.setAttribute("primarykey", array2str(primarykey));
      }
      TableColumnMetaData[] columns = tmd.getColumns();
      if(columns!=null){
      for(int i=0;i<columns.length;i++){
        Element field = doc.createElement("field");
        TableColumnMetaData c = columns[i];
        field.setAttribute("fieldname", c.getName());
        XmlFunc.setElementAttribute(field,"fieldlable", c.getLabel());
        XmlFunc.setElementAttribute(field,"fielddesc", c.getDesc());
        field.setAttribute("sqltype", String.valueOf(SqlFunc.getSubsectionType(c.getType())));
        field.setAttribute("len", String.valueOf(c.getLen()));
        field.setAttribute("scale", String.valueOf(c.getScale()));
        field.setAttribute("autoinc", c.isAutoInc()?"1":"0");
        field.setAttribute("nullable", c.isNullable()?"1":"0");
        field.setAttribute("unique", c.isUnique()?"1":"0");
        XmlFunc.setElementAttribute(field,"defaultvalue", c.getDefaultValue());
        fields.appendChild(field);
      }
      }
      Element indexes = doc.createElement("indexes");
      TableIndexMetaData[] indx = tmd.getIndexes();
      if(indx!=null){
        for(int i=0;i<indx.length;i++){
          TableIndexMetaData dx = indx[i];
          Element index = doc.createElement("index");
          index.setAttribute("indexname", dx.getName());
          index.setAttribute("unique", dx.isUnique()?"1":"0");
          index.setAttribute("fields", array2str(dx.getColumns()));
          indexes.appendChild(index);
        }
      }
      ss.appendChild(indexes);
    }
    else {
      for (int i = 0; i < meta.getColumnCount(); i++) {
        Element field = doc.createElement("field");
        field.setAttribute("fieldname", meta.getColumnName(i));
        XmlFunc.setElementAttribute(field,"fieldlable", meta.getColumnLabel(i));
        XmlFunc.setElementAttribute(field,"fielddesc", meta.getColumnDescription(i));
        //保存系统的字段类型，而不是SqlType的int类型；
        field.setAttribute("sqltype", String.valueOf(SqlFunc.getSubsectionType(meta.getColumnType(i))));
        field.setAttribute("len", String.valueOf(meta.getColumnLength(i)));
        field.setAttribute("scale", String.valueOf(meta.getColumnScale(i)));
        fields.appendChild(field);
      }
    }
    ss.appendChild(fields);
    
  }
  private String array2str(String[] primarykey) {
    if(primarykey==null) return null;
    StringBuffer buf = new StringBuffer(primarykey.length*20);
    for (int i = 0; i < primarykey.length; i++) {
      if (i != 0) {
        buf.append(",");
      }
      buf.append(primarykey[i]);
    }
    return buf.toString();
  }
  
  /**
   * 根据xml定义的表结构创建表；
   * @param conn
   * @param xml
   * @param tablename   
   *        指定的表名，如果为空，则使用xml中定义的表名；
   * @param autoChangeTableName
   *        如果表名存在，或者表名不合法，是否自动生成一个合法的表名；   
   * @param autoAdjustFieldLength
   *        表示是否需要根据数据库的限制，自动调整字段长度，比如：主键列组合长度超过限制等；
   * @return 创建的表名；
   * @throws Exception
   */
  public String createTable(Connection conn, Document xml, String tablename, boolean autoChangeTableName, boolean autoAdjustFieldLength) throws SQLException {
    Dialect dl = SqlFunc.createDialect(conn);
    DbDef def = (DbDef)dl.createDbDefiner();

    TableMetaDataHelper tmdh = new TableMetaDataHelper(tablename,xml);
    /**
     * 
     */
    if(autoAdjustFieldLength)
      def.checkKeysAndIndexesMaxLength(tmdh);
    return createTable(conn,tmdh,def,autoChangeTableName, autoAdjustFieldLength);
  }
  
  /**
   * 根据xml定义的表结构，创建或者修复表结构；
   * @param conn
   * @param xml
   * @param tablename
   *        指定的表名，如果为空，则使用xml中定义的表名；
   * @param autoAdjustFieldLength
   *        表示是否需要根据数据库的限制，自动调整字段长度，比如：主键列组合长度超过限制等；
   * @return 创建的表名；
   * @throws Exception
   */
  public String repairTable(Connection conn, Document xml ,String tablename, boolean autoAdjustFieldLength) throws SQLException {
    TableMetaDataHelper tmdh = new TableMetaDataHelper(tablename,xml);
    String tbname = tmdh.getTableName();
    
    Dialect dl = SqlFunc.createDialect(conn);
    DbDef def = (DbDef)dl.createDbDefiner();
    if(autoAdjustFieldLength)
      def.checkKeysAndIndexesMaxLength(tmdh);
    /**
     * 20090716 BI-2164
     * 判断是否是已存在的表或者视图，如果存在，则试图修改结构；
     * 修改结构会判断字段的属性，和原属性是否一致，不一致才修改；
     * 当然如果是视图，不一致的属性修改会报错；
     * 原来的代码只判断了是不是表，如果是存在的视图，将会创建新表，这时报“已存在的数据库对象”的异常；
     * 
     */
    if(def.tableOrViewExists(conn, tbname)){
      //修改表结构,暂时只修改字段属性，索引和主键不修改, 暂时不支持增加自动增长字段；
      TableMetaData tmd = dl.createDbMetaData().getTableMetaData(tbname);
      return repairTable(conn,tmd,tmdh,def);
    }else{
      //创建表
      return createTable(conn,tmdh,def,true, autoAdjustFieldLength);
    }
  }
  
  private String createTable(Connection conn, TableMetaDataHelper tmdh, DbDefiner def, 
       boolean autoChangeTableName, boolean autoAdjustFieldLength) throws SQLException {
    def.clearDefineInfo();
    TableColumnMetaData[] fields = tmdh.getColumns();
    for(int i=0;i<fields.length;i++){
      TableColumnMetaData fd = fields[i];
      if(fd.isAutoInc()){
        def.defineAutoIncField(fd.getName(), 1, fd.getDesc());
      }else{
        def.defineField(fd.getName(), (char)fd.getType(), fd.getLen(), fd.getScale()
            , fd.getDefaultValue(), fd.isNullable(), fd.isUnique(), fd.getDesc());
      }
    }
    String[] pkeys = tmdh.getPrimaryKey();
    //可能没有设置主键
    if(pkeys!=null)
      def.definePrimaryKey(ArrayFunc.array2Str(pkeys, ','));
    TableIndexMetaData[] indexes = tmdh.getIndexes();
    for(int i=0;indexes!=null && i<indexes.length;i++){
      TableIndexMetaData ind = indexes[i];
      def.defineIndex(ind.getName(), getIndexColumns(ind.getColumns()), ind.isUnique());
    }
    return def.createTable(conn, tmdh.getTableName(),!autoChangeTableName,autoAdjustFieldLength);
  }
  private String getIndexColumns(String cols[]) {
    StringBuffer indxcol = new StringBuffer(cols.length*20);
    indxcol.append('(');
    for(int i=0;i<cols.length;i++){
      if(i>0) indxcol.append(',');
      indxcol.append(cols[i]);
    }
    indxcol.append(')');
    return indxcol.toString();
  }
  

  /*
   * tmd 是原表定义
   * tmdh 是目的定义
   */
  private String repairTable(Connection conn, TableMetaData tmd, TableMetaDataHelper tmdh, DbDefiner def) throws SQLException {
    String tbname = tmd.getTableName();
    TableColumnMetaData[] destcols = tmd.getColumns();
    int clen = destcols.length;
    /**
     * 使用map来处理目标数据表的字段结构，将与源数据表结构比较:
     * 按源结构修改表结构，不在源结构中的字段要删除；
     * 这个过程使用map，效率提高了数倍，字段越多提高越明显；
     */
    HashMap colmap = new HashMap(destcols.length);
    for(int i=0;i<clen;i++){
    	/**
    	 * 先参考源表结构，删除目的表中多余的字段；
    	 * 这样改动的目的是：
    	 * 可能目的表有很多字段，而源表也有很多字段，且源表的很多字段是目的表没有的，
    	 * 如果先增加这些目的表中没有的字段，可能造成超过1000个字段的限制，无法继续的问题；
    	 */
    	String colNamei = destcols[i].getName();
    	TableColumnMetaData coli = tmdh.getColumn(colNamei);
    	/**
         * 最后一个字段保留，即使要删除也要等到增加了新的字段在删；
         * 原因是：如果源表结构和目标表的结构字段完全不一样，目的表要保留一个字段否则出异常，数据表必须有一个字段；
         */
        if (coli == null && i<clen-1) {
    		def.dropColumn(conn, tbname, colNamei);
    	}else{
    		colmap.put(colNamei.toUpperCase(), destcols[i]);
    	}
    }
    
    TableColumnMetaData[] srcfields = tmdh.getColumns();
    int length = srcfields.length;
	for(int i=0;i<length;i++){
      TableColumnMetaDataHelper fld = (TableColumnMetaDataHelper)srcfields[i];
      String fdname = fld.getName();
      TableColumnMetaData col = (TableColumnMetaData) colmap.remove(fdname.toUpperCase());
      if(col==null){
        //add column
        addColumn(conn,tbname,fld,def);
      }else{
        //modify column
        modifyColumn(conn,tbname,tmdh,fld,col,def);
      }
    }
	//删除可能多于的字段；
    Iterator it = colmap.keySet().iterator();
    while(it.hasNext()){
      String colNamei = (String)it.next();
      //有些数据库字段名是区分大小写的；
      TableColumnMetaData tcoli = (TableColumnMetaData)colmap.get(colNamei);
      def.dropColumn(conn, tbname, tcoli.getName());
    }
    return tbname;
  }
  private void modifyColumn(Connection conn, String tbname, TableMetaDataHelper tmdh, TableColumnMetaDataHelper fld, TableColumnMetaData col, DbDefiner def) throws SQLException {
    //判断是否需要修改字段属性；
    boolean f = checkModify(conn,tmdh,fld,col);
    if(f){
      /**
       * 20090722 BIDEV-714 
       * 3.Oracle修改字段名，同时修改字段描述，报：java.lang.RuntimeException: java.lang.ArrayIndexOutOfBoundsException: -1 
       * at com.esen.jdbc.metadata.impl.OracleTableColumnMetaDataImpl.getDesc(OracleTableColumnMetaDataImpl.java:60) 
       * at com.esen.jdbc.dialect.impl.EasyDbDefiner.modifyColumn(EasyDbDefiner.java:238) 异常；
       * 原因：这是由于Oracle获取字段描述是不同步的，只有在访问了col.getDesc()才会获取，这时如果修改了字段名，再掉用就会报这个异常；
       *       因为原来的字段已经不存在了；
       * 解决：修改字段前，先初始化字段描述信息； 
       */
      col.getDesc();
      def.modifyColumn(conn, tbname, fld.getName(),fld.getNewColumnName(),(char)fld.getType()
          , fld.getLen(), fld.getScale(), fld.getDefaultValue(), fld.isUnique(), fld.isNullable());
    }
    //判断是否需要修改字段描述信息；
    /**
     * 20090722 BIDEV-714 
     * 1.将已有的描述值设置为空；
     * 2.修改字段名，同时修改字段描述；
     * 解决：
     * 1.判断是否需要修改字段描述的代码问题；
     * 2.如果修改了字段名，修改字段描述时应该使用新的字段名；
     */
    String fddesc = fld.getDesc();
    String coldesc = col.getDesc();
    if(!compareDescStr(fddesc,coldesc)){
      String fieldname = StrFunc.isNull(fld.getNewColumnName())?fld.getName():fld.getNewColumnName();
      def.modifyColumnForDesc(conn, tbname, fieldname, fddesc);
    }
  }
  
  private boolean compareDescStr(String fddesc,String coldesc){
    //都为空或者空串，返回true;
    if(StrFunc.isNull(fddesc)&&StrFunc.isNull(coldesc)){
      return true;
    }
    return StrFunc.compareStr(fddesc, coldesc);
  }
  private boolean checkModify(Connection conn, TableMetaDataHelper tmdh, TableColumnMetaDataHelper fld, TableColumnMetaData col) {
    Dialect dl = SqlFunc.createDialect(conn);
    DbDef dbf = (DbDef)dl.createDbDefiner();
    
    DataBaseInfo db = dl.getDataBaseInfo();
    boolean  f = db.isFieldCaseSensitive();
    if(fld.getNewColumnName()!=null&&fld.getNewColumnName().length()>0){
      //判断需要更改字段名，有些数据库字段区分大小写，比如sybase，有的不区分，比如Oracle，DB2,mysql
      if(f && !fld.getNewColumnName().equals(fld.getName()))
        return true;
      if(!f && !fld.getNewColumnName().equalsIgnoreCase(fld.getName()))
        return true;
    }
    char fdsqltype = (char)fld.getType();
    if(fdsqltype!=SqlFunc.getSubsectionType(col.getType())){
      if(fdsqltype=='D'&&SqlFunc.getType(col.getType())=='D'){
        /**
         * 如果是mssql， 由于不区分D,T,P 不做修改；
         */
        if(!db.isMssql()){
          return true;
        }
      } else if (fdsqltype == 'L' && SqlFunc.getType(col.getType()) == 'C') { //逻辑型和字符类型视为相同，不修改
    	  return false;
      } else if ((fdsqltype == 'I' || (fdsqltype == 'N' && fld.getScale() == 0))
    		  && (SqlFunc.getType(col.getType()) == 'I' || (SqlFunc.getType(col.getType()) == 'N' && col.getScale() == 0))) { 
    	  // 整型和标度为 0 的浮点型，长度相同时数据类型相同，长度需要扩充时，类型改变。
    	  int len1 = fld.getLen();
    	  int len2 = col.getLen();
    	  if (len1 > 38) {
    		  len1 = 38;
    	  }
    	  if (len2 > 38) {
    		  len2 = 38;
    	  }
    	  if (len1  <= len2) {
    		  return false;
    	  } else if (len1 > len2) {
    		  return true;
    	  }
      } else
      //类型改变
      return true;
    }

    /*
     * 此处的修改目的是为了修改维表结构时能将字段长度改小
     * 修改以前：表中的字符类型的字段，长度可以增加，但不能改小
     * 修改以后：表中的子都类型的字段，长度可以改大或者改小
     * 
     * TODO：
     * 1.不确定是不是所有数据库这样修改都没有问题
     * 2.不确定会不会对其他模块有影响  
     */
    //if (fdsqltype == 'C' && fld.getLen() > col.getLen()) {
	if (fdsqltype == 'C' && fld.getLen() != col.getLen()) { 
		// 长度扩充
		return true;
	}

	if (fdsqltype == 'I' || fdsqltype == 'N') {
		int newlen = fld.getLen();
		if (newlen > 38) {
			newlen = 38;
		}

		if (newlen > col.getLen()) {
			return true;
		}

		/*
		 * BUG:ESENFACE-655: modified by liujin 2014.05.09
		 * 在精度发生变化时（包括变大和变小），都需要修改字段定义
		 */
		if (fdsqltype == 'N' && fld.getScale() != col.getScale()) {
			return true;
		}
	}

    /**
     * 这里不判断是否修改过默认值，在字段修改方法会判断，这里加判断有问题：
     * col.getDefaultvalue()在Oracle实现是访问到才去数据库查，
     * 如果该字段已经被改名了，下面的代码不会执行，判断下一个字段时，执行到这里再去查，原来的字段就不存在，出异常；
     * 这种情况只有在第一个字段被改名，才会发生；
     * 所以去掉下面的判断代码；
     */
    /*if (fld.getSqltype() != 'M' && fld.getSqltype() != 'X') {
      if (StrFunc.isNull(fld.getDefaultvalue()) && !StrFunc.isNull(col.getDefaultValue())
          || !StrFunc.isNull(fld.getDefaultvalue()) && StrFunc.isNull(col.getDefaultValue())
          || !StrFunc.isNull(fld.getDefaultvalue()) && !StrFunc.isNull(col.getDefaultValue())
          && !StrFunc.compareStr(fld.getDefaultvalue(), col.getDefaultValue())) {
        return true;
      }
    }*/
    /**
     * 去掉上面的判断，就无法修改默认值了；
     * 这里判断如果doc中设置了默认值，就进行修改；
     */
    /**
     * ISSUE:BI-8044 bi维表引用视图，视图原表是i的服务器代码组，创建时报错
     * 有默认值，并且默认值与源表的默认值不同时，进行修改
     */
    if(!StrFunc.isNull(fld.getDefaultValue())&&!fld.getDefaultValue().equals(col.getDefaultValue())){
      return true;
    }
    if(fld.isUnique()!=col.isUnique())
      return true;
    if(fld.isNullable()!=col.isNullable())
      return true;
    return false;
  }
  
  private void addColumn(Connection conn, String tbname, TableColumnMetaData fld, DbDefiner def) throws SQLException {
    def.addColumn(conn, tbname, fld.getName(), (char)fld.getType()
        , fld.getLen(), fld.getScale(), fld.getDefaultValue(), fld.isNullable(), fld.isUnique());
    if(fld.getDesc()!=null&&fld.getDesc().length()>0){
      def.modifyColumnForDesc(conn, tbname, fld.getName(), fld.getDesc());
    }
  }
  
}