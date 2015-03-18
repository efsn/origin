package com.esen.db.sql;

/**
 * 检查sql中的源表个数、外连接个数等；
 * 
 *
 * @author dw
 */
public class CheckSelectTable {
	private SelectTable st;
	int tableCount;
	int outJoinCount;
	public CheckSelectTable(SelectTable st){
		this.st = st;
	}
	
	/**
	 * 执行检查
	 */
	public void check(){
		check(st);
	}
	
	private void check(SelectTable st2) {
		if(st2 instanceof SelectUnionTable){
			SelectUnionTable su = (SelectUnionTable)st2;
			SelectTable[] sts = su.getSelectTables();
			if(sts!=null){
				for(int i=0;i<sts.length;i++){
					check(sts[i]);
				}
			}
		}
		for(int i=0;i<st2.getTableCount();i++){
			BaseTable ti = st2.getTable(i);
			if(ti instanceof SelectTable){
				check((SelectTable)ti);
			}else if(ti instanceof RealTable){
				tableCount++;
			}
			if(i>0&&ti.getJoinType()>0){
				outJoinCount++;
			}
		}
	}

	/**
	 * 返回源表个数
	 * @return
	 */
	public int getTableCount(){
		return tableCount;
	}
	
	/**
	 * 返回外连接数
	 * @return
	 */
	public int getOutJoinCount(){
		return outJoinCount;
	}
}