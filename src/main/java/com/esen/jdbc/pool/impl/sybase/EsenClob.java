package com.esen.jdbc.pool.impl.sybase;

import java.io.CharArrayReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.Clob;
import java.sql.SQLException;

import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

public class EsenClob implements Clob {
	
	private char[] chars;
	public EsenClob(Reader rr) throws SQLException{
		try {
			if(rr!=null)
				chars = StmFunc.reader2chars(rr,true);
		}
		catch (IOException e) {
			SQLException se = new SQLException(e.getMessage());
			se.setStackTrace(e.getStackTrace());
			throw se;
		}
	}
	
	private void throwNoSupportException() throws SQLException{
//		throw new SQLException("不支持的方法；");
		throw new SQLException(I18N.getString("JDBC.COMMON.DOESNOTSUPPORTMETHOD", "不支持的方法；"));
	}
	
	public long length() throws SQLException {
		return chars==null?-1:chars.length;
	}

	public String getSubString(long pos, int length) throws SQLException {
		if(pos<1) return null;
		char[] cc = new char[length];
		System.arraycopy(chars, 0, cc, (int)pos-1, length);
		return new String(cc);
	}

	public Reader getCharacterStream() throws SQLException {
		if(chars==null){
			return null;
		}
		return new CharArrayReader(chars);
	}

	public InputStream getAsciiStream() throws SQLException {
		throwNoSupportException();
		return null;
	}

	public long position(String searchstr, long start) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public long position(Clob searchstr, long start) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public int setString(long pos, String str) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public int setString(long pos, String str, int offset, int len) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public OutputStream setAsciiStream(long pos) throws SQLException {
		throwNoSupportException();
		return null;
	}

	public Writer setCharacterStream(long pos) throws SQLException {
		throwNoSupportException();
		return null;
	}

	public void truncate(long len) throws SQLException {
		throwNoSupportException();

	}

}
