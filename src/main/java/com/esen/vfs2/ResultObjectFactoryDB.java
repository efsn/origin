package com.esen.vfs2;

import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import com.esen.vfs2.impl.VfsCache;
import com.esen.vfs2.impl.VfsNode;

/**
 * 创建VfsNode节点时,需要传入一些参数.用此类创建VfsNode对象
 * 
 *
 * @author zhuchx
 */
public class ResultObjectFactoryDB extends DefaultObjectFactory {

	private VfsCache cache;

	public Object create(Class type) {
		if (type == VfsNode.class) {
			return new VfsNode(this.cache);
		} else {
			return super.create(type, null, null);
		}
	}

	public ResultObjectFactoryDB(VfsCache cache) {
		this.cache = cache;
	}

}
