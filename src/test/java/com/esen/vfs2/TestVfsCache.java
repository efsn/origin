package com.esen.vfs2;

import java.io.Serializable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.esen.util.StrFunc;
import com.esen.util.cluster.Cluster;
import com.esen.util.cluster.ClusterAddress;
import com.esen.util.cluster.ClusterMessage;
import com.esen.util.cluster.ClusterMessageFactory;
import com.esen.util.cluster.ClusterMessageListener;
import com.esen.vfs2.impl.VfsCacheImpl;
import com.esen.vfs2.impl.VfsNode;

/**
 * 测试时需要用到的缓存管理器类
 *
 * @author zhuchx
 */
public class TestVfsCache extends VfsCacheImpl implements ClusterMessageListener {

	private static Cluster ctrlabs;

	private static final long serialVersionUID = -5001618329026586644L;

	private static final Logger log = LoggerFactory.getLogger(TestVfsCache.class);

	private Cluster clusterctrl;

	private ClusterMessageFactory factory;

	private String client;

	public TestVfsCache() {
		this.client = StrFunc.MD5(this.getClass().toString());
		this.clusterctrl = ctrlabs;
		factory = this.clusterctrl == null ? null : this.clusterctrl.regMessageListener(client, this);
	}

	public static void setClusterCtrlAbs(Cluster ctrl) {
		ctrlabs = ctrl;
	}

	/**
	 * 将文件节点加入到缓存
	 * <br>
	 * 如果缓存中已经存在相同文件路径的节点,是否需要更新结点是根据文件的最后修改时间和有无内容来判断.
	 * <br>
	 * 如果最后修改时间不同或者新传入的节点中有内容,则需要更新结点
	 * @param node
	 */
	public void put(VfsNode node) {
		if (node == null)
			return;
		synchronized (nodesmap) {
			_put(node);
		}
	}

	/**
	 * 同时将多个节点加入到缓存中,注释见put(VfsNode node)
	 * @param nodes
	 */
	public void put(VfsNode[] nodes) {
		int len = nodes == null ? 0 : nodes.length;
		if (len == 0)
			return;
		synchronized (nodesmap) {
			for (int i = 0; i < len; i++) {
				put(nodes[i]);
			}
		}
	}

	/**
	 * 将文件节点加入到缓存,注释见put(VfsNode node)
	 * @param node
	 */
	private void _put(VfsNode node) {
		if (node == null)
			return;
		String key = node.getAbsolutePath();
		VfsNode oldNode = (VfsNode) nodesmap.get(key);
		if (oldNode == null) {
			nodesmap.put(key, node);
		}
		else if (oldNode.getLastModifyTime().getTime() != node.getLastModifyTime().getTime()) {
			//对象已经改变
			notifyMessage(node.getAbsolutePath(), false);
			nodesmap.put(key, node);
		}
		else if (node.getContainContent()) {
			nodesmap.put(key, node);
		}
	}

	public void remove(String key, boolean recur) {
		remove(key, recur, true);
	}

	/**
	 * 删除节点,如果是一个目录,会删除目录和目录下文件的所有缓存
	 * @param key
	 * @param recur 如果为true,则当作一个目录来处理,如果为false,则只删除key表示的节点
	 * @param notify 是否通知集群环境下的其它节点.当此节点是接收到的其它节点的信息时,传入的notify需要为false
	 */
	public void remove(String key, boolean recur, boolean notify) {
		super.remove(key, recur);
		if (notify) {
			notifyMessage(key, recur);
		}
	}

	/**
	 * 发送信息给集群中的其它结点,通知节点删除缓存中的信息
	 * @param key
	 * @param recur
	 */
	private void notifyMessage(String key, boolean recur) {
		sendMessage(ClusterMessage.OPER_DELETE, key, Boolean.valueOf(recur));
	}

	public void sendMessage(int oper, String objid, Serializable var1) {
		/**
		 * 发送信息到其它服务器,删除缓存
		 */
		if (factory == null)
			return;
		ClusterMessage msg = factory.createClusterUpdateMessage(oper, objid, var1);
		clusterctrl.getMessageSender().postMessage(msg);
	}

	public void handleMessage(ClusterMessage message, ClusterAddress sender) throws Throwable {

		switch (message.getOper()) {
			case ClusterMessage.OPER_DELETE: {
				/**
				 * 接收到删除缓存的信息,从VfsCache中删除缓存信息
				 */
				String key = message.getObjectId();
				System.out.println("++++++++++++++++++++++++++++++" + key);
				boolean recur = ((Boolean) message.getVar1()).booleanValue();
				remove(key, recur, false);
			}
		}
	
	}
}
