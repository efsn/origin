package com.esen.db.sql;

import com.esen.db.sql.SelectTableUtil;

import junit.framework.TestCase;

public class SelectTableUtilTest extends TestCase {

	public void testGetLeftJoinConditionForOracle() {
		assertEquals("A.ID=B.ID(+)", SelectTableUtil.getLeftJoinConditionForOracle("a.id=b.id", "b"));
		assertEquals("(A.USERID_=B.USERID_(+)) AND (A.BBQ_=B.BBQ_(+))", SelectTableUtil.getLeftJoinConditionForOracle("a.userid_=b.userid_ and a.bbq_=b.bbq_", "b"));
		assertEquals("(SUBSTING(A.USERID_,1,4)=SUBSTRING(B.USERID_(+),1,4)) AND (A.BBQ_=SUBSTRING(B.BBQ_(+),1,4))", 
				SelectTableUtil.getLeftJoinConditionForOracle("substing(a.userid_,1,4)=substring(b.userid_,1,4) and a.bbq_=substring(b.bbq_,1,4)", "b"));
		assertEquals("(A.USERID_=B.USERID_(+)) AND (A.BBQ_=TO_CHAR(ADD_MONTHS(TO_DATE(B.BBQ_(+),'yyyymm--'),12),'yyyymm--')) AND (A.BTYPE_=B.BTYPE_(+))", 
				SelectTableUtil.getLeftJoinConditionForOracle("(a.USERID_=b.USERID_ and a.BBQ_=to_char(add_months(to_date(b.BBQ_,'yyyymm--'),12),'yyyymm--') and a.BTYPE_=b.BTYPE_)", "b"));
	}
	
}
