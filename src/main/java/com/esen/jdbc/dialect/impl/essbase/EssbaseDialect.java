package com.esen.jdbc.dialect.impl.essbase;

import java.sql.Connection;
import java.sql.SQLException;

import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.dialect.DbMetaData;
import com.esen.jdbc.dialect.impl.DialectImpl;

public class EssbaseDialect extends DialectImpl {

	public EssbaseDialect(Object con_OR_conf) {
		super(con_OR_conf);
	}
	

	public DbDefiner createDbDefiner() {
		return new EssbaseDef(this);
	}
	
	public DbMetaData createDbMetaData() throws SQLException {
		if (dbmd == null) {
			dbmd = connectionFactory != null ? new EssbaseDbMetaData(connectionFactory) : new EssbaseDbMetaData(con);
		}
		return dbmd;
	}
	
	public DbMetaData createDbMetaData(Connection conn) throws SQLException {
		return new EssbaseDbMetaData(conn);
	}
	
	public String funcChar(String ascii) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcCode(String sourece) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcFind(String c1, String c2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcLeft(String source, String len) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcRight(String source, String len) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcMid(String field, String iFrom, String len) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcLen(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcLower(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcUpper(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcSearch(String sub, String toSearch) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcRepeat(String field, String count) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcWholeReplace(String source, String oldSub, String newSub) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcTrim(String field) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcToday() {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcYear(String datefield) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcMonth(String datefield) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcDay(String datefield) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcNow() {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcSeconds(String datefield, String datefield2) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcAbs(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcC(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcCos(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcSin(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcTan(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcEven(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcExp(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcSqrt(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcFact(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcInt(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcSign(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcLn(String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcLog(String d, String dValue) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcMod(String iValue, String i) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcPi() {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcPower(String dValue, String d) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcRand() {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcRound(String d, String i) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcTrunc(String d, String i) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcToDate(String date) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcToDateTime(String dtstr) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcDateToChar(String datefield, String style) {
		// TODO Auto-generated method stub
		return null;
	}

	public String funcCharToDate(String charfield, String style) {
		// TODO Auto-generated method stub
		return null;
	}

	public String formatOffsetDate(String datefield, int offset, char ymd) {
		// TODO Auto-generated method stub
		return null;
	}

	protected String getStrLengthSql(String str) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getTruncateTableSql(String tablename) {
		return null;
	}
}
