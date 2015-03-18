package com.esen.jdbc.data.impl;

import com.esen.jdbc.AbstractMetaData;
import com.esen.jdbc.dialect.DataBaseInfo;
import com.esen.jdbc.dialect.Dialect;

public class AbstractMetaDataForWriter implements AbstractMetaData {
  private AbstractMetaData md;
  private String[] fnlist;
  private int[] columnLens;
  /**
   * 将表结构转换成适合指定数据库的结构
   * 主要处理字段的长度
   * @param md
   * @param dl
   */
	public AbstractMetaDataForWriter(AbstractMetaData md,Dialect dl){
		fnlist = new String[md.getColumnCount()];
		columnLens = new int[md.getColumnCount()];
		this.md = md;
		DataBaseInfo dbinf = dl.getDataBaseInfo();
	  for(int i=0;i<md.getColumnCount();i++){
		  String fn = md.getColumnLabel(i);
		  if(fn==null||fn.length()==0)
		    fn = md.getColumnName(i);
		  if(fn.length()>dbinf.getMaxColumnNameLength()){
		    fn = getFormatFieldName(i,fn,dbinf.getMaxColumnNameLength());
		  }
		  
		  fnlist[i]= fn;
		  //select语句md.getColumnLength(i)获得的是在总长度；
		  /**
		   * csv导入导出后长度发生变化，此处不做处理
		   */
		  columnLens[i] = md.getColumnLength(i);
//		  if (md.getColumnScale(i) > 0)
//        columnLen = md.getColumnLength(i) - md.getColumnScale(i);
//		  else columnLen = md.getColumnLength(i);
//		  columnLens[i] = columnLen;
		  
		}
	}
	private String getFormatFieldName(int i,String fn, int maxlen) {
	  if(fn.length()>maxlen)
	    fn = fn.substring(0,maxlen);
	  while(findFieldName(i,fn)){
	    StringBuffer tm = new StringBuffer(maxlen);
	    for(int j=fn.length()-1;j>=0;j--){
	      char c = fn.charAt(j);
	      if(c>='0'&&c<='9'){
	        tm.insert(0,c);
	      }else break;
	    }
	    if(tm.length()>0){
	      int k = Integer.parseInt(tm.toString())+1;
	      String ks = String.valueOf(k);
	      fn = fn.substring(0,fn.length()-ks.length())+ks;
	    }else{
	      fn = fn.substring(0,fn.length()-1)+"0";
	    }
	  }
    return fn;
  }
	private boolean findFieldName(int k,String fn){
	  for(int i=0;i<k;i++){
	    String fni = fnlist[i];
	    if(fni.equalsIgnoreCase(fn))
	      return true;
	  }
	  for(int i=k;i<md.getColumnCount();i++){
	    String fni = md.getColumnLabel(i);
	    if(fni==null||fni.length()==0)
        fni = md.getColumnName(i);
	    if(fni.equalsIgnoreCase(fn))
        return true;
	  }
	  return false;
	}
  public int getColumnCount() {
		return md.getColumnCount();
	}

	public String getColumnDescription(int i) {
		return md.getColumnDescription(i);
	}

	public String getColumnLabel(int i) {
		return md.getColumnLabel(i);
	}

	public int getColumnLength(int i) {
    return columnLens[i];
  }
/*	public void setColumnLength(int i,int len){
	  columnLens[i]=len;
	}*/
	public String getColumnName(int i) {
		return fnlist[i];
	}

	public int getColumnScale(int i) {
	  int s = md.getColumnScale(i);
	  if(s<0){
	    //小数位数小于0，则暂时当作4位处理；
	    return 4;
	  }
		return s;
	}

	public int getColumnType(int i) {
		return md.getColumnType(i);
	}
  public int isNullable(int i) {
    return md.isNullable(i);
  }
  public int isUnique(int i) {
    return md.isUnique(i);
  }

}
