package com.esen.jdbc.pool.impl.sybase;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.SQLException;

import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

public class EsenBlob implements Blob {
	
	private byte[] bb;
	public EsenBlob(InputStream in) throws SQLException{
		if(in == null){
			throw new SQLException("Cannot instantiate a Blob " +
	                 "object with a null InputStream object");
		}
		try {
			bb = StmFunc.stm2bytes(in);
		}
		catch (IOException e) {
			SQLException se = new SQLException(e.getMessage());
			se.setStackTrace(e.getStackTrace());
			throw se;
		}
	}

	public long length() throws SQLException {
		return bb==null?-1:bb.length;
	}
	
	private void throwNoSupportException() throws SQLException{
//		throw new SQLException("不支持的方法；");
		throw new SQLException(I18N.getString("JDBC.COMMON.DOESNOTSUPPORTMETHOD", "不支持的方法；"));
	}

	public byte[] getBytes(long pos, int length) throws SQLException {
		throwNoSupportException();
		return null;
	}

	public InputStream getBinaryStream() throws SQLException {
		return new ByteArrayInputStream(bb);
	}

	public long position(byte[] pattern, long start) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public long position(Blob pattern, long start) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public int setBytes(long pos, byte[] bytes) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public int setBytes(long pos, byte[] bytes, int offset, int len) throws SQLException {
		throwNoSupportException();
		return 0;
	}

	public OutputStream setBinaryStream(long pos) throws SQLException {
		throwNoSupportException();
		return null;
	}

	public void truncate(long len) throws SQLException {
		throwNoSupportException();
	}

}
