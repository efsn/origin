package com.esen.vfs2;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSessionManager;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.TableMetaData;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.jdbc.pool.PooledSQLException;
import com.esen.util.FileFunc;
import com.esen.util.StmFunc;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.tmpfile.DefaultTempFileFactory;
import com.esen.vfs2.impl.AbstractVfs2;
import com.esen.vfs2.impl.VfsCache;
import com.esen.vfs2.impl.VfsFile2Impl;
import com.esen.vfs2.impl.VfsNode;
import com.esen.vfs2.impl.VfsNodeOper;
import com.esen.zip.ZipEntry;
import com.esen.zip.ZipOutputStream;

public class VfsNodeOperDB implements VfsNodeOper {
	private ConnectionFactory fct;

	private EsenSqlMapClient smc;

	private VfsCache cache;

	private TableMetaData tmd;

	private StringMap options;
	
	private boolean hasStartTransaction = false;
	
	private boolean autoCommit = true;

	public VfsNodeOperDB(ConnectionFactory fct, EsenSqlMapClient smc, VfsCache cache, StringMap options) {
		this.fct = fct;
		this.smc = smc;
		this.cache = cache;
		this.options = options;
		
		try {
			Connection conn = fct.getConnection();
			try {
				this.autoCommit = conn.getAutoCommit();
			} finally {
				conn.close();
			} 
		} catch (SQLException e) {
			; //不需抛出异常
		}
	}
	
	public ConnectionFactory getConnectionFactory(){
		return fct;
	}

	public VfsCache getCache() {
		return cache;
	}

	public SqlSessionManager getClient() {
		return smc.getLocalSqlMapSession();
	}

	public boolean cacheEnable(){
		return smc.cacheEnable();
	}
	
	public boolean hasStartTransaction() {
		return hasStartTransaction;
	}

	public void startTransaction() throws SQLException {
		hasStartTransaction = true;
		getClient().startManagedSession(false);
	}

	public void commitTransaction() throws SQLException {
		getClient().commit(true);
	}

	public void endTransaction() throws SQLException {
		hasStartTransaction = false;
		getClient().rollback(true); //TODO
		getClient().close();
		//结束事务时只需要关闭会话即可，不需要保留会话
		//getClient().startManagedSession(autoCommit);
	}

	public void startTransaction(boolean hasStart) throws SQLException {
		if (hasStart)
			return;
		startTransaction();
	}

	public void commitTransaction(boolean hasStart) throws SQLException {
		if (hasStart)
			return;
		commitTransaction();
	}

	public void endTransaction(boolean hasStart) throws SQLException {
		if (hasStart)
			return;
		endTransaction();
	}

	public void startBatch() throws SQLException {
		getClient().startManagedSession(ExecutorType.BATCH, false) ;
	}

	public void executeBatch() throws SQLException {
		getClient().commit(false); //TODO
	}

	/**
	 * 复制文件
	 */
	public void copyTo(VfsNode node, String parentDir, String fileName) throws SQLException {
		VfsNode extend = createExtendNode(node, parentDir, fileName);
		checkFilePath(extend.getNewParentDir(), extend.getNewFileName());

		boolean hasstart = hasStartTransaction();
		startTransaction(hasstart);
		try {
			synchronized (this) {
				if (!isNodeExist(createNode(parentDir, fileName))) {
					getClient().update("vfs-copyFile", extend);
					if (!isFile(node)) {
						getClient().update("vfs-copyChilds", extend);
					}
					commitTransaction(hasstart);
				} else {
					throw new SQLException("file or folder already exist");
				}
			}
		}
		finally {
			endTransaction(hasstart);
		}
	}

	public void createFile(VfsNode node) throws SQLException {
		checkFilePath(node.getParentDir(), node.getFileName());
		synchronized (this) {
			if (!isNodeExist(node)) {
				getClient().insert("vfs-insert", node);
			} else {
				throw new SQLException("file or folder already exist");
			}
		}
	}

	private void checkFilePath(String parentDir, String fileName) {
		VfsFile2Impl.checkFilePath(parentDir, fileName);
	}

	public VfsNode getNode(String path) throws SQLException {
		String[] formats = AbstractVfs2.sepPath(path);
		return getNode(formats[0], formats[1]);
	}

	public VfsNode getNode(String parentDir, String fileName) throws SQLException {
		return getNode(parentDir, fileName, false);
	}

	public VfsNode getNode(String parentDir, String fileName, boolean withContent) throws SQLException {
		VfsNode node = createNode(parentDir, fileName);
		String id = withContent ? "vfs-selectByPath-withContent" : "vfs-selectByPath";
		VfsNode r = (VfsNode) getClient().selectOne(id, node);
		if (r != null) {
			r.setContainContent(withContent);
		}
		return r;
	}

	public void importFiles(VfsNode dest, File parent, File[] fs, String owner, boolean deleteFirst) throws Exception {
		int len = fs == null ? 0 : fs.length;
		if (len == 0) {
			return;
		}
		ArrayList nodeList = new ArrayList(len);
		for (int i = 0; i < len; i++) {
			VfsNode node = file2Node(dest, parent, fs[i], owner);
			checkFilePath(node.getParentDir(), node.getFileName());
			nodeList.add(node);
		}

		boolean hasstart = hasStartTransaction();
		startTransaction(hasstart);
		try {
			if (deleteFirst) {
				/**
				 * 先删除,再加入,直接删除文件和文件的子文件
				 */
				/*
				 * BUG:IRPT-11069: modify by liujin 2013.09.02
				 * 修改 sql 语句结构，避免嵌套层次过深
				 */
				if (nodeList != null && nodeList.size() > 0) {
					HashMap map = new HashMap();
					startBatch();

					for (int index = 0; nodeList != null && index < nodeList.size(); index++) {
						VfsNode node =  (VfsNode) nodeList.get(index);
						map.clear();
						map.put("node", node);
						map.put("path", node.getAbsolutePath() + "/%");
						getClient().delete("vfs-deleteByPath", map);
					}
					executeBatch();
				}
			}
			else {
				/**
				 * 覆盖相同的,要注意处理导入的文件是文件,但是在vfs中对应文件是目录的情况,对这种情况的处理方式是:
				 * 判断导入的是文件还是目录,如果是文件,则删除vfs中对应文件和其子文件,如果导入 的文件是目录,只删除vfs中对应的文件本身即可
				 */
				ArrayList fileList = new ArrayList(len);
				for (int i = 0; i < len; i++) {
					VfsNode node = (VfsNode) nodeList.get(i);
					if (isFile(node)) {
						fileList.add(node);
					}
					else {
						getClient().delete("vfs-deleteByPathSelf", node);
					}
				}

				if (fileList.size() != 0) {
					HashMap map = new HashMap();
					
					/*
					 * BUG:IRPT-11069: modify by liujin 2013.09.02
					 * 修改 sql 语句结构，避免嵌套层次过深
					 */
					startBatch();
					for (int index = 0; index < fileList.size(); index++) {
						VfsNode node =  (VfsNode) nodeList.get(index);
						map.clear();
						map.put("node", node);
						map.put("path", node.getAbsolutePath() + "/%");

						getClient().delete("vfs-deleteByPath", map);
					}
					executeBatch();
				}
			}

			for (int i = 0; i < len; i++) {
				VfsNode node = (VfsNode) nodeList.get(i);
				synchronized (this) {
					if (!isNodeExist(node)) {
						getClient().insert("vfs-insert", node);
					} else {
						throw new SQLException("file or folder already exist");
					}
				}
			}

			commitTransaction(hasstart);
		}
		finally {
			endTransaction(hasstart);
		}

		//操作完成后作清理操作
		for (int i = 0; i < len; i++) {
			VfsNode node = (VfsNode) nodeList.get(i);
			node.delete();
		}
	}

	public VfsNode[] listFiles(VfsNode node) throws SQLException {
		List list = getClient().selectList("vfs-selectChilds", node);
		return list2Nodes(list);
	}

	public VfsNode[] listFiles(VfsNode node, String filters, int filterType, boolean recur) throws SQLException {
		String whereCondition = new VfsSqlCondition().getWhereCondition(fct, node, filters, filterType, recur);
		if (whereCondition == null) {
			return null;
		}
		boolean withContent = (VfsFile2.WITHCONTENT & filterType) != 0;
		String id = withContent ? "vfs-search-withcontent" : "vfs-search";
		List list = getClient().selectList(id, whereCondition);
		return list2Nodes(list, withContent);
	}

	public void remove(VfsNode node) throws SQLException {
		remove(new VfsNode[] { node });
	}

	public void remove(VfsNode[] nodes) throws SQLException {
		boolean hasstart = hasStartTransaction();
		startTransaction(hasstart);
	
		try {
			startBatch();
			
			/*
			 * BUG:IRPT-11069: modify by liujin 2013.09.02
			 * 修改 sql 语句结构，避免嵌套层次过深
			 */ 
			if (nodes != null && nodes.length > 0) {
				HashMap map = new HashMap();
				
				for (int index = 0; index < nodes.length; index++) {
					map.clear();
					map.put("node", nodes[index]);
					map.put("path", nodes[index].getAbsolutePath() + "/%");
					getClient().delete("vfs-deleteByPath", map);
				}
			}
			executeBatch();
			commitTransaction(hasstart);
		} finally {
			endTransaction(hasstart);
		}
	}

	public void renameTo(VfsNode node, String parentDir, String fileName, boolean isoverwirte) throws SQLException {
		VfsNode extend = createExtendNode(node, parentDir, fileName);
		checkFilePath(extend.getNewParentDir(), extend.getNewFileName());

		VfsNode deleNode = createNode(parentDir, fileName);
		HashMap map = new HashMap();
		map.put("node", deleNode);
		map.put("path", deleNode.getAbsolutePath() + "/%");

		boolean hasstart = hasStartTransaction();
		startTransaction(hasstart);
		try {
			getClient().delete("vfs-deleteByPath", map);
			getClient().update("vfs-renameFile", extend);
			if (!isFile(node)) {
				getClient().update("vfs-renameChilds", extend);
			}
			commitTransaction(hasstart);
		}
		finally {
			endTransaction(hasstart);
		}
	}

	public void setProperties(VfsNode node, HashMap props) throws Exception {
		VfsNode dest = map2Node(node, props);
		if (dest == null)
			return;
		setProperties(dest);
	}

	public void setProperties(VfsNode node) throws SQLException {
		getClient().update("vfs-updateProperties", node);
	}

	public boolean isFile(VfsNode node) {
		return StrFunc.parseBoolean(node.getIsFile(), false);
	}

	public String boolean2str(boolean b) {
		return b ? "1" : "0";
	}

	public void writeContent(VfsNode node) throws SQLException {
		getClient().update("vfs-updateContent", node);
	}

	public VfsNode createNode(String parentDir, String fileName, boolean isfile, String owner) {
		VfsNode n = createNode(parentDir, fileName);
		n.setIsFile(boolean2str(isfile));
		Timestamp time = new Timestamp(System.currentTimeMillis());
		n.setCreateTime(time);
		n.setLastModifyTime(time);
		n.setOwner(owner);
		n.setMender(owner);
		n.setSize(0);
		return n;
	}

	public VfsNode createNode(String path) {
		String[] formats = AbstractVfs2.sepPath(path);
		return createNode(formats[0], formats[1]);
	}

	public VfsNode createNode(String parentDir, String fileName) {
		VfsNode n = new VfsNode(getCache());
		n.setParentDir(parentDir);
		n.setFileName(fileName == null || fileName.length() == 0 ? "/" : fileName);
		return n;
	}

	public VfsNode createExtendNode(VfsNode node, String newParentDir, String newFileName) {
		VfsNode extend = new VfsNode(getCache());
		extend.setParentDir(node.getParentDir());
		extend.setFileName(node.getFileName());
		extend.setNewParentDir(newParentDir);
		extend.setNewFileName(newFileName);
		return extend;
	}

	public VfsNode file2Node(VfsNode node, File base, File file, String owner) throws Exception {
		String path = node.getParentDir() + node.getFileName() + "/"
				+ file.getAbsolutePath().substring(base.getAbsolutePath().length());
		VfsNode r = createNode(path);
		r.setIsFile(boolean2str(file.isFile()));
		r.setCreateTime(new Timestamp(System.currentTimeMillis()));
		r.setLastModifyTime(r.getCreateTime());
		r.setOwner(owner);
		r.setMender(owner);
		r.setSize(file.length());
		if (file.isFile()) {
			File tf = DefaultTempFileFactory.getInstance().createTempFile(null);
			InputStream in = new FileInputStream(file);
			try {
				InputStream gin = StmFunc.getGZIPStm(in);
				try {
					FileFunc.stm2file(gin, tf.getAbsolutePath());
				}
				finally {
					gin.close();
				}
			}
			finally {
				in.close();
			}

			r.setContent(tf);
		}
		return r;
	}

	private VfsNode[] list2Nodes(List list) {
		return list2Nodes(list, false);
	}

	private VfsNode[] list2Nodes(List list, boolean withcontent) {
		int size = list == null ? 0 : list.size();
		if (size == 0)
			return null;
		VfsNode[] ns = new VfsNode[size];
		for (int i = 0; i < size; i++) {
			ns[i] = (VfsNode) list.get(i);
			ns[i].setContainContent(withcontent);
		}
		return ns;
	}

	public VfsNode map2Node(VfsNode src, HashMap map) throws Exception {
		if (map == null)
			return null;
		VfsNode n = (VfsNode) src.clone();
		Iterator it = map.entrySet().iterator();
		while (it.hasNext()) {
			Entry entry = (Entry) it.next();
			String key = (String) entry.getKey();
			Object value = entry.getValue();
			if ("lastModifyTime".equals(key)) {
				n.setLastModifyTime((Timestamp) value);
			}
			else if ("owner".equals(key)) {
				n.setOwner((String) value);
			}
			else if ("mender".equals(key)) {
				n.setMender((String) value);
			}
			else if ("charset".equals(key)) {
				n.setCharset((String) value);
			}
			else if ("mimeType".equals(key)) {
				n.setMimeType((String) value);
			}
		}
		return n;
	}

	public void exportZipPackage(VfsNode[] nodes, OutputStream out) throws Exception {
		//TODO 优化,可能会占用非常大的内存
		ZipOutputStream zipOut = null;
		if (!(out instanceof ZipOutputStream)) {
			zipOut = new ZipOutputStream(out);
			zipOut.setEncoding(StrFunc.GBK);//因为ZipInputStream默认的解码方式为GBK,所有输出时使用GBK编码
		}
		else {
			zipOut = (ZipOutputStream) out;
		}

		int len = nodes == null ? 0 : nodes.length;
		for (int i = 0; i < len; i++) {
			VfsNode node = nodes[i];
			exportNode2ZipPackage(node, zipOut);
		}
		zipOut.flush();
		zipOut.finish();
	}

	private void exportNode2ZipPackage(VfsNode base, ZipOutputStream out) throws Exception {
		List list = getClient().selectList("vfs-selectSelfAndChildsRecurByPath", base);

		int len = list == null ? 0 : list.size();
		for (int i = 0; i < len; i++) {
			VfsNode node = (VfsNode) list.get(i);
			node2Zip(out, base, node);
		}
	}

	private void node2Zip(ZipOutputStream out, VfsNode base, VfsNode node) throws Exception {
		String pdir = base.getParentDir();
		String path = node.getParentDir() + node.getFileName();
		String name = path.substring(pdir.length());
		//如果是目录,则以"/"结尾
		if (!isFile(node)) {
			name += '/';
		}
		ZipEntry entry = new ZipEntry(name);
		out.putNextEntry(entry);
		//TODO　如果是目录设置了子节点顺序,如何处理
		if (isFile(node) && node.getSize() > 0) {
			File file = node.getContent();
			if (file != null && file.isFile() && file.length() > 0) {
				FileFunc.file2OutStm(file, out, false);
			}
		}
	}

	public HashMap array2Iterator(Object[] os) {
		List list = Arrays.asList(os);
		HashMap map = new HashMap();
		map.put("list", list);
		return map;
	}

	public static InputStream getUnGzipStm(byte[] bs) throws Exception {
		if (bs == null || bs.length == 0)
			return null;
		return StmFunc.getUnGZIPStm(new ByteArrayInputStream(bs));
	}

	public static byte[] getUnGzipBytes(byte[] bs) throws Exception {
		if (bs == null || bs.length == 0)
			return null;
		InputStream in = getUnGzipStm(bs);
		try {
			return StmFunc.stm2bytes(in);
		}
		finally {
			in.close();
		}
	}

	public static byte[] gzipBytes(byte[] bs) throws Exception {
		if (bs == null)
			return null;
		return StmFunc.gzipBytes(bs);
	}

	/**
	 * 创建临时文件
	 */
	public String createTempFile(String parentDir, String fileName, String owner, boolean isdir) throws Exception {
		String oldName = FileFunc.formatFileName(fileName);
		int fieldMaxLen = getFileNameColumnLen();

		int len = fct.getDialect().getStrLength(oldName);
		if (len > fieldMaxLen - 7) {
			oldName = oldName.substring(0, fieldMaxLen - 7);
		}
		fileName = oldName + generationTmpFileNameSuf();

		VfsNode node = createNode(parentDir, fileName, !isdir, owner);

		int retryTimes = getRetryTimes();
		int curRetryTime = 0;
		while (curRetryTime < retryTimes) {
			if (insertNode(node)) {
				return node.getFileName();
			}
			fileName = oldName + generationTmpFileNameSuf();
			node.setFileName(fileName);
			curRetryTime++;
		}
		return null;
	}

	/**
	 * 生成临时文件名的后半部分
	 */
	private String generationTmpFileNameSuf() {
		StringBuffer buf = new StringBuffer(7);
		buf.append((char) (Math.floor(Math.random() * 26) + 'A'));
		buf.append((char) (Math.floor(Math.random() * 26) + 'A'));
		buf.append((char) (Math.floor(Math.random() * 26) + 'A'));
		buf.append((int) (Math.random() * 10000));
		return buf.toString();
	}

	/**
	 * 写入数据，如果成功，返回true，失败时如果是因为主键冲突，返回false,其他情况抛出异常
	 */
	private boolean insertNode(VfsNode node) throws Exception {
		try {
			synchronized (this) {
				if (!isNodeExist(node)) {
					getClient().insert("vfs-insert", node);
				} else {
					return false; //违反唯一约束，表定义中无约束，如已存在该数据则视为违反唯一约束
				}
			}
		}
		catch (PooledSQLException e) {
			/**
			 * 使用自定义统一异常代码处理逻辑；
			 * 原来的代码使用标准SQLException接口方法，会覆盖原有的错误代码，可能对使用原始代码的程序产生影响；
			 */
			if (e.getErrorCode2() == PooledSQLException.JDBC_UNIQUE_CONSTRAINT_VIOLATED) {//违反唯一约束
				return false;
			}
			throw e;
		}
		return true;
	}

	/**
	 * 返回文件名字段的长度
	 */
	private int getFileNameColumnLen() throws SQLException {
		if (tmd == null) {
			String tableName = options.getString("tablename");
			tmd = fct.getDialect().createDbMetaData().getTableMetaData(tableName);
		}
		return tmd.getColumn("FILENAME_").getLen();
	}

	/**
	 * 生成临时文件时，重试次数
	 */
	private int getRetryTimes() {
		return options.getInt("retrytimes", 20);
	}
	
	/**
	 * IRPT-8814: 去除虚拟文件系统表的主键 
	 * 判断该文件或目录是否已存在
	 * @param node
	 * @return
	 * @throws SQLException
	 */
	private boolean isNodeExist(VfsNode node) throws SQLException {
		if ((Integer)(getClient().selectOne("vfs-count", node)) <= 0) {
			return false;
		} else {
			return true;
		}
	}
}
