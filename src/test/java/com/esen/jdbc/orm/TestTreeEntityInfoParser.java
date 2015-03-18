package com.esen.jdbc.orm;

import java.io.FileInputStream;

import org.w3c.dom.Document;

import junit.framework.TestCase;

import com.esen.jdbc.orm.helper.SCTreeEntityInfoParser;
import com.esen.jdbc.orm.helper.TreeEntityInfo;
import com.esen.jdbc.orm.helper.TreeEntityInfoParser;
import com.esen.util.XmlFunc;

/**
 * xml解析单元测试
 *
 * @author wangshzh
 */
public class TestTreeEntityInfoParser extends TestCase {

	public void testParse() {
		//完全正确的xml格式
		TreeEntityInfo treeEntityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_mapping_right.xml");
		assertEquals("-", treeEntityInfo.getRootUpid());
		assertEquals("id", treeEntityInfo.getIdPropertyName());
		assertEquals("upid", treeEntityInfo.getUpIdPropertyName());
		assertEquals("btype", treeEntityInfo.getBtypePropertyName());
		assertEquals("upid0,upid1,upid2,upid3,upid4,upid5,upid6,upid7", treeEntityInfo.getUpidsPropertyName());
		
		//TODO 检查边界问题，包括代码里相应加入：upids中的必须能找到对应属性，其余的也要能找到对应属性
		try{
			treeEntityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_lackofupids.xml");
			fail();
		}catch(Exception e){
			//e.printStackTrace();
			if(e.getMessage().indexOf(SCTreeEntityInfoParser.ATTRIBUTE_UPIDS)<0){
				fail();
			}
		}
	}

	public TreeEntityInfo makeTestInstance(String filePath) {
		Document doc = null;
		TreeEntityInfoParser treeEntityInfoParser = null;
		try {
			doc = XmlFunc.getDocument(new FileInputStream(filePath));
		} catch (Exception e) {
			throw new ORMException(e);
		}
		treeEntityInfoParser = new TreeEntityInfoParser(doc);
		return (TreeEntityInfo) treeEntityInfoParser.parse();
	}
}
