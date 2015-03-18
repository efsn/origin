package com.esen.jdbc.data.impl;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.esen.jdbc.data.DataReader;
import com.esen.jdbc.i18n.JdbcResourceBundleFactory;
import com.esen.util.StmFunc;
import com.esen.util.i18n.I18N;

/*
 * 读取新旧格式*.db文件的工厂类；
 */
public class DataReaderFromStmFactory {
	private static DataReaderFromStmFactory _instance;

	private DataReaderFromStmFactory() {

	}

	public static DataReaderFromStmFactory getInstance() {
		if (_instance == null) {
			synchronized (DataReaderFromStmFactory.class) {
				if (_instance == null) {
					_instance = new DataReaderFromStmFactory();
				}
			}
		}
		return _instance;
	}

	public DataReader createDataReader(InputStream in) {
		if (!in.markSupported()) {
			in = new BufferedInputStream(in);
		}
		int len = DataWriterToStmNew.TITLE.getBytes().length;
		try {
			in.mark(len);//做标记以便如果不是新格式，还原
			String tt = StmFunc.readFix(in, len);
			if (tt.equals(DataWriterToStmNew.TITLE)) {
				int b = StmFunc.readInt(in);
				if (b == 2) {
					return new DataReaderFromStmNew(in);//新的db文件格式
				}
				throw new RuntimeException(I18N.getString(
						"com.esen.jdbc.data.impl.datareaderfromstmfactory.unsupportfilepattern",
						"不支持的文件格式；"));
			}
			in.reset();//还原读取流
		}
		catch (IOException e) {
			throw new RuntimeException(e);
		}

		return new DataReaderFromStm(in);//旧的db格式文件
	}
}
