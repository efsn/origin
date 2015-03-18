package com.esen.jdbc.orm;

import java.io.FileInputStream;
import org.w3c.dom.Document;
import junit.framework.TestCase;
import com.esen.jdbc.orm.ORMException;
import com.esen.jdbc.orm.helper.SCTreeEntityInfo;
import com.esen.jdbc.orm.helper.SCTreeEntityInfoParser;
import com.esen.util.XmlFunc;

/**
 * xml解析单元测试
 *
 * @author wangshzh
 */
public class TestSCTreeEntityInfoParser extends TestCase {
	
	public void testParse() {
		//完全正确的xml格式
		SCTreeEntityInfo scTreeEntityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_mapping_right.xml");
		assertEquals("fromDate", scTreeEntityInfo.getFromDatePropertyName());
		assertEquals("toDate", scTreeEntityInfo.getToDatePropertyName());
		
		// 检查边界问题，包括代码里相应加入：fromDate,toDate必须能找到对应属性
		try{
			scTreeEntityInfo = makeTestInstance("test/com/esen/jdbc/orm/fruit_erro_no_fromDateProperty.xml");
			fail();
		}catch(Exception e){
			assertEquals(SCTreeEntityInfoParser.TAG_SLOWLYCHANGE+"的"+SCTreeEntityInfoParser.ATTRIBUTE_FROMDATE+"属性值没有对应的property！",
					e.getMessage());
		}
	}
	
	public SCTreeEntityInfo makeTestInstance(String filePath) {
		Document doc = null;
		SCTreeEntityInfoParser entityInfoParser = null;
		try {
			doc = XmlFunc.getDocument(new FileInputStream(filePath));
		} catch (Exception e) {
			throw new ORMException(e);
		}
		entityInfoParser = new SCTreeEntityInfoParser(doc);
		return (SCTreeEntityInfo)entityInfoParser.parse();
	}
}
