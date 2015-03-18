package com.esen.jdbc.orm;

import java.io.InputStream;

/**
 * ORM 模块中的二进制的字段，通过一个临时文件来存储二进制信息
 * 可以实用改对象的free方法来是删除临时文件
 * 文件类型的实现对象:{@see com.esen.jdbc.orm.FileBlob}
 *
 * @author wang
 */
public interface Blob {

	/**
	 * 释放资源
	 */
	void free();

	/**
	 * @param in 将in对象写写入到当前对象中
	 */
	void writeBinaryStream(InputStream in);

	/**
	 * @return 获取二进制的输出流格式
	 */
	InputStream getBinaryStream();

	/**
	 * @return 二进制字节的长度
	 */
	long length();

}
