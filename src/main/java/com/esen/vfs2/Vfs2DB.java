package com.esen.vfs2;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.esen.jdbc.ConnectionFactory;
import com.esen.jdbc.dialect.DbDefiner;
import com.esen.jdbc.ibatis.EsenSqlMapClient;
import com.esen.util.MiniProperties;
import com.esen.util.ObjectFactory;
import com.esen.util.StrFunc;
import com.esen.util.StringMap;
import com.esen.util.XmlFunc;
import com.esen.vfs2.impl.AbstractVfs2;
import com.esen.vfs2.impl.VfsCache;
import com.esen.vfs2.impl.VfsCacheImpl;
import com.esen.vfs2.impl.VfsCharsetMgr;
import com.esen.vfs2.impl.VfsMimeType;
import com.esen.vfs2.impl.VfsNodeOper;
import com.esen.vfs2.impl.VfsOperatorImpl;

/**
 * vfs的数据库实现
 * @author zhuchx
 */
public class Vfs2DB extends AbstractVfs2 {

	private static final Logger log = LoggerFactory.getLogger(Vfs2DB.class);

	public static final String VFS_TABLE_XML_NAME = "table-vfs.xml";//数据库表结构对应的配置文件

	public static final String MIMETYPEFILE = "mimetype.txt";//系统默认的MimeType配置文件

	public static final String CHARSETFILE = "charset.txt";//系统默认的编码配置文件

	/**
	 * 在vfs.properties中VFS_CONF指定的值就是vfs的默认的内部配置文件的路径，vfs对象默认的从中读取选项用于控制vfs的一些特性
	 */
	public static final String VFS_CONF = "vfs.conffile";

	/**
	 * 定义缓存管理器的实现类.缓存管理器处理以下缓存任务:
	 * <ol>
	 *   <li>缓存VfsNode对象,并提供获得,删除的方法
	 *   <li>将文件内容写入到数据库前,文件内容通过此类缓存
	 *   <li>从数据库读取出文件内容后,文件内容通过此类缓存
	 * </ol>
	 */
	public static final String VFS_CACHE = "vfs.cache";

	/**
	 * vfs中的缓存刷新时间间隔
	 */
	public static final String VFS_FLUSHINTERVAL = "vfs.flushinterval";

	public static final long VFS_FLUSHINTERVAL_DEFAULT = 24 * 60 * 60 * 1000;//一天

	private VfsNodeOperDB nodeOper;

	public Vfs2DB(ConnectionFactory fct, String tableName, Properties props, EsenSqlMapClient client) {
		super(getCache(props, VFS_CACHE), getFlushCacheInterval(props));
		init(fct, tableName, props, client);
	}

	private static long getFlushCacheInterval(Properties props) {
		return props == null ? VFS_FLUSHINTERVAL_DEFAULT : StrFunc.str2long(props.getProperty(VFS_FLUSHINTERVAL),
				VFS_FLUSHINTERVAL_DEFAULT);
	}

	/**
	 * 获得缓存管理器
	 * 如果从配置文件中没有获得缓存管理器,则使用默认的缓存管理器
	 * <br>
	 * 配置文件中获得是缓存管理器的具体实现的类名
	 * @param props
	 * @param cacheName
	 * @return
	 */
	private static VfsCache getCache(Properties props, String cacheName) {
		String className = props == null ? null : props.getProperty(cacheName);
		if (!StrFunc.isNull(className)) {
			try {
				Class c = Class.forName(className);
				if (VfsCache.class.isAssignableFrom(c)) {
					return (VfsCache) c.newInstance();
				}
				else if (ObjectFactory.class.isAssignableFrom(c)) {
					ObjectFactory fct = (ObjectFactory) c.newInstance();
					Object obj = fct.createObject();
					if (obj instanceof VfsCache) {
						return (VfsCache) obj;
					}
				}
			}
			catch (Exception e) {
				log.warn(e.getMessage(), e);
			}
		}
		return new VfsCacheImpl();
	}

	/**
	 * 初始化必要的信息
	 * <ol>
	 * <li>创建表</li>
	 * <li>初始化VfsNodeOper</li>
	 * <li>初始化根目录</li>
	 * <li>设置vfs的属性</li>
	 * <li>初始化MimeType和Charset</li>
	 * </ol>
	 */
	private void init(ConnectionFactory fct, String tableName, Properties props, EsenSqlMapClient client) {
		try {
			//创建表
			createTable(this, fct, tableName, VFS_TABLE_XML_NAME);
			//创建VfsNodeOper
			_initNodeOper(fct, tableName, client, props);
			//初始化Root
			this._initRoot();
			//设置属性
			Properties ps = loadConf(props);
			this.setProperties(ps);
			//载入MimeType
			this.loadMimeType(props);
			//载入charset
			this.loadCharsetMgr(props);
		}
		catch (Exception e) {
			throw new VfsException(e);
		}
	}

	private void _initNodeOper(ConnectionFactory fct, String tableName, EsenSqlMapClient client, Properties props) {
		client.setResultObjectFactory(new ResultObjectFactoryDB(getCache()));
		StringMap options = new StringMap();
		options.put("tablename", tableName);
		if (props != null) {
			options.putAll(props);
		}
		this.nodeOper = new VfsNodeOperDB(fct, client, getCache(), options);
	}

	/**
	 * 确保根目录存在
	 */
	private void _initRoot() throws Exception {
		VfsFile2 root = getVfsFile(null, new VfsOperatorImpl("admin", true));
		root.ensureExists(true);
	}

	public VfsNodeOper getNodeOper() {
		return nodeOper;
	}

	public void commitTransaction() throws VfsException {
		try {
			nodeOper.commitTransaction();
		}
		catch (Exception e) {
			throw new VfsException(e);
		}
	}

	public void endTransaction() throws VfsException {
		try {
			nodeOper.endTransaction();
		}
		catch (Exception e) {
			throw new VfsException(e);
		}
	}

	public void startTransaction() throws VfsException {
		try {
			nodeOper.startTransaction();
		}
		catch (Exception e) {
			throw new VfsException(e);
		}
	}

	/**
	 * 创建物理表
	 */
	public static void createTable(Class c, ConnectionFactory fct, String tablename, String xml) throws Exception {
		Document doc = getTableDocument(c, xml);
		DbDefiner dd = fct.getDbDefiner();
		Connection con = fct.getConnection();
		try {
			if (dd.tableOrViewExists(con, tablename)) {
				//已经存在,则修复表
				dd.repairTable(con, doc, tablename, true);
			}
			else {//创建表
				dd.createTable(con, doc, tablename, false, true);
			}
		}
		finally {
			con.close();
		}
	}

	public static void createTable(Object obj, ConnectionFactory fct, String tablename, String xml) throws Exception {
		createTable(obj.getClass(), fct, tablename, xml);
	}

	public static Document getTableDocument(Object obj, String xml) throws Exception {
		return getTableDocument(obj.getClass(), xml);
	}

	public static Document getTableDocument(Class c, String xml) throws Exception {
		InputStream in = c.getResourceAsStream(xml);
		try {
			return XmlFunc.getDocument(in);
		}
		finally {
			in.close();
		}
	}

	/**
	 * 从confpath中载入vfs的属性
	 * @param props
	 * @return
	 * @throws Exception
	 */
	private Properties loadConf(Properties props) throws Exception {
		if (props == null)
			return null;
		String path = props.getProperty(VFS_CONF);
		if (StrFunc.isNull(path))
			return null;
		VfsFile2 file = getVfsFile(path, createAdminOperator());
		if (file == null || !file.isFile())
			return null;

		if (!file.exists())
			return null;
		InputStream in = file.getInputStream();
		try {
			MiniProperties ps = new MiniProperties();
			ps.load(in, StrFunc.UTF8);
			return ps;
		}
		finally {
			in.close();
		}
	}

	/**
	 * 载入文件MimeType的配置
	 */
	private void loadMimeType(Properties props) throws Exception {
		MiniProperties pp = loadProps(props, MIMETYPEFILE, "filemimepath", "sysmimepath");
		this.setMimeType(new VfsMimeType(pp));
	}

	/**
	 * 载入文件编码的配置
	 */
	private void loadCharsetMgr(Properties props) throws Exception {
		MiniProperties pp = loadProps(props, CHARSETFILE, "filecharsetpath", "syscharsetpath");
		this.setCharsetMgr(new VfsCharsetMgr(pp));
	}

	/**
	 * 载入配置的属性,按下面的方式载入:
	 * 1.从sysFilePath中载入
	 * 2.从osFilePath中载入
	 * 3.从vfsFilePath中载入
	 * 
	 * 目前用于载入文件的MimeType和编码
	 * @param sysFilePath  属性配置文件在系统中的路径
	 * @param osFilePath  属性配置文件在操作中的路径
	 * @param vfsFilePath  属性配置文件在vfs中的路径
	 */
	private MiniProperties loadProps(Properties props, String sysFilePath, String osFilePath, String vfsFilePath)
			throws Exception {
		MiniProperties pp = new MiniProperties();
		InputStream in = this.getClass().getResourceAsStream(sysFilePath);
		try {
			pp.load(in, StrFunc.UTF8);
		}
		finally {
			in.close();
		}
		if (props == null)
			return pp;
		String mimepath = props.getProperty(osFilePath);
		File file = mimepath == null ? null : new File(mimepath);
		if (file != null && file.isFile()) {
			InputStream fin = new FileInputStream(file);
			try {
				pp.load(fin);//因为是本地文件,采用本地编码,不用传入编码,另外,如果确实需要与本地文件的编码不同,可以在文件的第一行加入"#charset UTF-8",MiniProperties会自动识别
			}
			finally {
				fin.close();
			}
		}
		mimepath = props.getProperty(vfsFilePath);
		VfsFile2 vf = getVfsFile(mimepath, createAdminOperator());
		if (vf.isFile()) {
			InputStream fin = vf.getInputStream();
			try {
				pp.load(fin);
			}
			finally {
				fin.close();
			}
		}
		return pp;
	}

	protected VfsOperator createAdminOperator() {
		return new VfsOperatorImpl("admin", true);
	}
}
