package org.svip.pool.db;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.svip.util.StrUtil;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Blues
 * @version 1.0
 * Created on 2014/8/24
 */
public class ConnPoolMgr {

    private Map<String, ConnPool> pools;

    private ConnPoolMgr() {
        try {
            pools = new HashMap<String, ConnPool>();
            this.load(ConnPool.class.getResourceAsStream("jdbc.propertise"), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static ConnPoolMgr getInstance() {
        return Singleton.instance;
    }

    public ConnPool getPool() {
        return pools.get("pro");
    }

    public ConnPool getPool(String id) {
        return pools.get(id);
    }

    public void load(String path) throws Exception {
        this.load(new File(path));
    }

    public void load(File file) throws Exception {
        this.load(new BufferedInputStream(new FileInputStream(file)), file.getName().endsWith(".xml"));
    }

    private void load(InputStream in, boolean isXml) throws Exception {
        if (isXml) {
            SAXReader sax = new SAXReader();
            Document doc = sax.read(in);
            Element root = doc.getRootElement();
            for (Object obj : root.elements("pool")) {
                Element element = (Element) obj;
                String key = element.attributeValue("id");
                if (StrUtil.isNull(key)) {
                    throw new DbPoolException("PoolParam Element has no id attribute.");
                }
                pools.put(key, new ConnPool(new PoolParam(element)));
            }
        } else {
            Properties pro = new Properties();
            pro.load(in);
            pools.put("pro", new ConnPool(new PoolParam(pro)));
        }
        in.close();
    }

    private static class Singleton {
        private static ConnPoolMgr instance = new ConnPoolMgr();
    }

}
