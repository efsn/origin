package com.esen.jdbc.orm;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.esen.util.FileFunc;
import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

/**
 * 基于文件存储的Blob实现类
 *
 * @author wang
 */
public class FileBlob implements Blob {

	private File file;

	private volatile int refcount;
	
	private volatile boolean  freed;
	
	private void checkFreed() {
		if (freed) {
			throw new ORMException("com.esen.jdbc.orm.fileblob.1","当前Blob已经释放，无法继续操作");
		}
	}

	public synchronized long length() {
		checkFreed();
		return file.exists() ? file.length() : 0;
	}

	public FileBlob(File file) {
		this.file = file;
	}

	/**
	 * @return 获取二进制的输出流格式
	 */
	public synchronized InputStream getBinaryStream() {
		checkFreed();
		if (!file.exists()) {
			throw new ORMException("com.esen.jdbc.orm.fileblob.2","没有任何二进制内容");
		}
		try {
			InputStream in = new FileInputStream(file);
			this.refcount++;
			return new BlobInputStream(this, in);
		} catch (FileNotFoundException e) {
			throw new ORMException("com.esen.jdbc.orm.fileblob.2","没有任何二进制内容");
		}
	}
	
	
	/**
	 * @param in 将in对象写写入到当前对象中
	 */
	public synchronized void writeBinaryStream(InputStream in) {
		try {
			OutputStream out = new FileOutputStream(file);
			try {
				StmFunc.stmTryCopyFrom(in, out);
			} finally {
				out.close();
			}
		} catch (Exception e) {
			throw new ORMException(e);
		}
	}

	/**
	 * 释放资源，删除临时文件
	 */
	public synchronized void free() {
		if (refcount > 0) {
			throw new ORMException("com.esen.jdbc.orm.fileblob.3","无法释放当前Blob应用，因为还存在{0}引用!",new Object[]{refcount});
		}
		FileFunc.remove(file);
		freed = true;
	}

	private static class BlobInputStream extends InputStream {

		private FileBlob blob;

		public BlobInputStream(FileBlob blob, InputStream in) {
			super();
			this.blob = blob;
			this.in = in;
		}

		private InputStream in;

		public int hashCode() {
			return in.hashCode();
		}

		public int read(byte[] b) throws IOException {
			return in.read(b);
		}

		public boolean equals(Object obj) {
			return in.equals(obj);
		}

		public int read(byte[] b, int off, int len) throws IOException {
			return in.read(b, off, len);
		}

		public long skip(long n) throws IOException {
			return in.skip(n);
		}

		public String toString() {
			return in.toString();
		}

		public int available() throws IOException {
			return in.available();
		}

		public void close() throws IOException {
			synchronized (blob) {
				blob.refcount--;
				in.close();
			}
		}

		public void mark(int readlimit) {
			in.mark(readlimit);
		}

		public void reset() throws IOException {
			in.reset();
		}

		public boolean markSupported() {
			return in.markSupported();
		}

		public int read() throws IOException {
			return in.read();
		}

	}

}
