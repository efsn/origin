package org.codeyn.util.yn;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.codeyn.util.i18n.I18N;
import org.codeyn.util.io.MyByteArrayOutputStream;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XmlYn {
  private XmlYn() {
  }
  
  /**
   * 使用当前配置的参数创建一个新的 DocumentBuilder 实例
   * @return 返回DocumentBuilder实例
   * @throws Exception
   */
  public static final DocumentBuilder getDocumentBuilder() throws Exception{
    return DocumentBuilderFactory.newInstance().newDocumentBuilder();
  }
  
  /**
   * 流应该自己指明编码，比如在xml文件中指定encoding。
   * @param is
   * @return
   * @throws Exception
   */
  public static final Document getDocument(InputStream is) throws Exception{
    if ("weblogic.apache.xerces.parsers.SAXParser".equals(System.getProperty("org.xml.sax.driver"))) {//是Weblogic 系列版本的
      return getDocumentFromInputStreamWeblogic(is);
    }
    InputSource source = new InputSource(is);
    return getDocumentBuilder().parse(source);
  }
  
  /**
   * weblogic8系列版本中在英文环境中对在xml中指定encoding="GBK"的编码不支持，下面是对输入流进行正确的编码设定，以达到能够正确解析的目的
   * @param is
   * @return
   * @throws Exception
   */
  private static final Document getDocumentFromInputStreamWeblogic(InputStream is) throws Exception {
    BufferedInputStream bi = new BufferedInputStream(is);
    String charset = StrYn.UTF8;//Chardet.extractCharset(bi);
    if (!StrYn.isNull(charset) && "GBK".equalsIgnoreCase(charset)) {//encoding是GBK时，对流进行正确的编码
      //用缓冲输入流，直接使用输入流时会报:org.xml.sax.SAXParseException: Premature end of file.异常
      return getDocument(new InputStreamReader(bi, charset));
    }
    //用缓冲输入流，直接使用输入流时会报:org.xml.sax.SAXParseException: Premature end of file.异常
    InputSource source = new InputSource(bi);
    return getDocumentBuilder().parse(source);
  }
  
  public static final Document getDocument(Reader is) throws Exception{
    InputSource source = new InputSource(is);
    return getDocumentBuilder().parse(source);
  }
  
  public static final Document getDocument(String xml) throws Exception {
    InputSource source = new InputSource(new StringReader(xml));
    return getDocumentBuilder().parse(source);
  }
  
  /**
   * 从一个程序中定义的资源中构造一个dom对象
   */
  public static final Document getDocumentFrom(String resourceName, Class<?> cls) throws Exception{
    InputStream in = cls.getResourceAsStream(resourceName);
    try {
      return getDocument(in);
    }
    finally {
      in.close();
    }
  }
  
  /**
   * 创建一个根节点为rootTagName的Document对象。
   * 比如“reports”
   * 也可以是一段完整的xml："<?xml-stylesheet type=\"text/xsl\" href=\"log.xsl\"?><reports></reports>"
   * @param rootTagName
   * @return
   * @throws Exception
   */
  public static final Document createDocument(String rootTagName) throws Exception{
    if(!rootTagName.startsWith("<") || !rootTagName.endsWith(">")){
        rootTagName = "<"+rootTagName+"></"+rootTagName+">";
    }
        
        return XmlYn.getDocument(rootTagName);
  }
  
  /**
   * 不推荐使用此方法,应该明确指定编码.
   * @deprecated 使用saveDocument(Document doc, OutputStream os,String encoding)
   */
  public static final void saveDocument(Document doc, OutputStream os)
      throws Exception {
    saveDocument(doc, os, null);
  }
  /**
   * 根据指定的编码将Documant写入流中并以byte数组形式将流中的内容返回
   * @param doc Documant对象
   * @param encoding  指定的编码
   * @return  返回byte数组
   * @throws Exception
   */
  public static final byte [] document2bytes(Document doc, String encoding) throws Exception{
    MyByteArrayOutputStream buf = new MyByteArrayOutputStream(1024*16);
    saveDocument(doc, buf, encoding);
    return buf.toByteArray();
  }
  /**
   * 根据指定的编码将Document保存到流中
   * @param doc Document对象
   * @param os  流对象
   * @param encoding 指定编码
   * @throws Exception
   */
  public static final void saveDocument(Document doc, OutputStream os,String encoding) throws Exception{
    Result result =new StreamResult(os);
    saveDocument(doc, result, encoding);
  }
  
  /**
   * Mar 20, 2009 5:45:38 PM
   * @usage 这个方法会在输出UTF-8编码的XML之前先输出3
   *        个BOM字节, 这三个BOM字节分别为EF, BB, BF.
   *        在某些情况下解析XML时, 如果没有这三个BOM字节, 
   *        会导致INVALID XML错误. 比如FUSIONCHART
   *        在使用setDataURL方法获取XML的时候.
   * @param doc
   */
  public static final void saveDocument_BOM(Document doc, OutputStream os) throws Exception{
    os.write(0xEF);
    os.write(0xBB);
    os.write(0xBF);
    saveDocument(doc, os, StrYn.UTF8);
  }
  
  /**
   * indent用于控制输出的xml是否有缩进和换行，
   *       输出的xml如果有缩进有回车的话，那么在ff上childNodes数组中也包含回车元素，造成客户端装入xml时还要过滤，所以服务器生成xml最好都是没有换行符的
   */
  public static final void saveDocument(Document doc, OutputStream os,String encoding, boolean indent) throws Exception{
    Result result =new StreamResult(os);
    saveDocument(doc, result, encoding,indent);
  }
  /**
   * 将Document内容按照指定的编码写入Writer流中
   * @param doc Document对象
   * @param os  Writer流
   * @param encoding 编码
   * @throws Exception
   */
  public static final void saveDocument(Document doc, Writer os,String encoding) throws Exception{
    Result result =new StreamResult(os);
    saveDocument(doc, result, encoding);
  }
  /**
   * 将Document内容写入流中 ,indent用于控制输出的xml是否有缩进和换行
   * @param doc Document对象
   * @param os Writer流
   * @param encoding 编码
   * @param indent 为true则缩进换行
   * @throws Exception
   */
  public static final void saveDocument(Document doc, Writer os,String encoding, boolean indent) throws Exception{
    Result result =new StreamResult(os);
    saveDocument(doc, result, encoding, indent);
  }
  
  private static void saveDocument(Document doc, Result result, String encoding) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
    /**
     * 20090704 改为"默认是缩进"，方便阅读。缩进并不占用多大空间。
     */
    saveDocument(doc,result,encoding,true);
  }
  /**
   * indent用于控制输出的xml是否有缩进和换行，
   *       输出的xml如果有缩进有回车的话，那么在ff上childNodes数组中也包含回车元素，造成客户端装入xml时还要过滤，所以服务器生成xml最好都是没有换行符的
   */
  private static void saveDocument(Document doc, Result result, String encoding, boolean indent) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
        saveNode(doc, result, encoding, indent);
  }
  
  /**
   * 返回节点的路径。比如“c/b/a/root/#document/”
   * @param node
   * @return
   */
  public static String getNodePath(Node node){
      StringBuffer sb = new StringBuffer(32);
      while(node!=null){
          sb.append(node.getNodeName());
          sb.append("/");
          node = node.getParentNode();
      }
      return sb.toString();
  }

    /**
     * 检查文字或属性是否为null。
     * 碰到文字或属性为null的情况，有些xml解析器在生成xml字符串时会抛空指针异常，而且从异常堆栈很难看出是哪个地方有问题。
     * 如果遇到空指针异常，调用本方法可以查出错误位置。
     * @param node
     */
    public static void checkNullNode(Node node) {
        if (node.getNodeType() == Node.TEXT_NODE) {
            String nodeValue = node.getNodeValue();
            if (nodeValue == null) {
                //throw new RuntimeException("发现文字为null的text节点：" + getNodePath(node));
                throw new RuntimeException(I18N.getString("com.esen.util.XmlFunc.1", "发现文字为null的text节点：") + getNodePath(node));
            }
        }

        if(node.getNodeType() == Node.ELEMENT_NODE){
            int length = node.getAttributes().getLength();
            for (int i = 0; i < length; i++) {
                String attr = node.getAttributes().item(i).getNodeName();
                String value = ((Element)node).getAttribute(attr);
                if(value == null){
                    //throw new RuntimeException("发现属性"+attr+"值为null的节点：" + getNodePath(node));
                    throw new RuntimeException(I18N.getString("com.esen.util.XmlFunc.2", "发现属性{0}值为null的节点：") + getNodePath(node));
                }
            }
        }

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            checkNullNode(childNodes.item(i));
        }
    }
  
  /**
   * 支持输出一个Node节点的内容,Document也是Node节点,也可以用这个方法输出
   * 
   * 将 XML Source 转换为 Result。
   * 
   * @param node
   * @param result
   * @param encoding
   * @param indent
   * @throws TransformerConfigurationException
   * @throws TransformerFactoryConfigurationError
   * @throws TransformerException
   */
  public static void saveNode(Node node, Result result, String encoding, boolean indent) throws TransformerConfigurationException, TransformerFactoryConfigurationError, TransformerException {
    //checkNullNode(node);//如果保存xml时出现空指针异常，可以调用checkNullNode查出错误位置。

    Transformer tf = TransformerFactory.newInstance().newTransformer();
    Properties properties = tf.getOutputProperties();

    if(StrYn.isNull(encoding)) encoding = StrYn.GB2312;
    properties.setProperty(OutputKeys.ENCODING, encoding);
    properties.setProperty(OutputKeys.INDENT, indent?"yes":"no");

    //更新XSLT引擎的输出属性。
    tf.setOutputProperties(properties);
    
    Source xmlSource = new DOMSource(node);
    tf.transform(xmlSource, result);
  }

  /**
   * 不推荐把document转为string，如果要保存的话，应该保存为byte[]
   * @deprecated
   * @param doc
   * @return
   * @throws Exception
   */
  public static final String document2str(Document doc) throws Exception{
    return document2str(doc,null);
  }

  /**
   * 不推荐把document转为string，如果要保存的话，应该保存为byte[]
   * @deprecated
   * @param doc
   * @return
   * @throws Exception
   */
  public static final String document2str(Document doc, String encoding)
      throws Exception {
    ByteArrayOutputStream result = new ByteArrayOutputStream(512);
    try {
      XmlYn.saveDocument(doc, result, encoding);
    }
    finally {
      result.close();
    }
    String enc = StrYn.isNull(encoding)?StrYn.GB2312:encoding;
    return result.toString(enc);
  }
  
  /**
   * 查找node的直接子节点，如果有节点名称等于name的则返回其值，否则返回def
   * @param node 操作的节点
   * @param name 节点的名称
   * @param def 默认的返回值
   * @return
   */
  public static final String getNodeValue(Node node, String name, String def) {
    Node c = (node != null) ? node.getFirstChild() : null;
    while (c != null) {
      String s = c.getNodeName();
      if (s != null && s.compareTo(name) == 0) {
        return getNodeValue(c, def);
      }
      c = c.getNextSibling();
    }
    return def;
  }
  /**
   * 查找node的直接子节点，如果有节点名称等于name的则返回其值，否则返回def
   * @param node  操作的节点
   * @param name 节点的名称
   * @param def 缺省的返回值
   * @return
   */

  public static final boolean getNodeBoolValue(Node node, String name,boolean def){
    String s = getNodeValue(node,name,null);
    if (s==null){
      return def;
    }
    if (s.equalsIgnoreCase("true")){
      return true;
    }
    if (s.equalsIgnoreCase("false")){
      return false;
    }
    return def;
  }
  /**
   * 在node下增加子节点，这个子节点的名称为name，值为value
   * 
   * @param node 需要添加子节点的点
   * @param name 添加的字节的名称
   * @param value 添加的字节点的值
   */

  public static final void addNodeValue(Node node, String name,String value){
    if (value!=null){
      Document doc = node.getOwnerDocument();
      Element e = doc.createElement(name);
      e.appendChild(doc.createTextNode(value));
      node.appendChild(e);
    }
  }
  
  /**
   * 在node下增加节点,节点名称为name,节点下存放CDATA节点,CDATA的内容为value
   */
  public static final void addNodeCDATAValue(Node node, String name,String value){
    if (value!=null){
      Document doc = node.getOwnerDocument();
      Element e = doc.createElement(name);
      e.appendChild(doc.createCDATASection(value));
      node.appendChild(e);
    }
  }
  /**
   * 在node下增加子节点，这个子节点的名称为name，值为value
   * @param node  操作的节点
   * @param name 子节点的名称
   * @param value 子节点的值
   */

  public static final void addNodeValue(Node node, String name,boolean value){
    addNodeValue(node,name,value?"true":"false");
  }

  /**
   * 得到node对应的值。比如
   * <data>abc</data>返回abc
   * <data><![CDATA[123]]></data>返回123
   * 
   * 20081206 原来这里调用Node.getTextContent()获取值，但是这个方法在jdk1.4中没有，所以直接返回null。
   * 
   * @param node
   * @param def
   * @return
   */
    public static final String getNodeValue(Node node, String def) {
        if (node == null)
            return def;
        //如果结点已经是文本结点,则不需要从子节点中获得文本,直接返回节点的内容
        if (node.getNodeType() == Node.CDATA_SECTION_NODE || node.getNodeType() == Element.TEXT_NODE) {
            return node.getNodeValue();
        }
        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node n = childNodes.item(i);
            if (n.getNodeType() == Element.CDATA_SECTION_NODE || n.getNodeType() == Element.TEXT_NODE) {
                return n.getNodeValue();
            }
        }

        /**
         * 20081124 这里不能调用node.getNodeValue()对于一个Element对象，它的nodevalue为null。
         * TODO 这个方法getTextContent在jdk1.4中编译通不过。
         */
        return def;
    }

    /**
     * 获取某一节点下的大文本字段信息，同getNodeValue方法，但这个方法只获取CDATA_SECTION_NODE类型的文本信息
     * 20100903 如果有多个连续的CDATA子节点,将会把这些子节点连接起来.因为如果有CDATA嵌套的情况出现,会把CDATA下的CDATA分成多个CDATA.
     *    如:<test><![CDATA[a]]></test>加入到一个CDATA中后会变成<test><![CDATA[a]]]]><![CDATA[></test>]]>   
     * 
     * 这里处理的方式是:在node下搜索CDATA节点,如果没有找到,则返回def值,如果找到,则将第一个连续的CDATA的内容返回.连续的CDATA是指CDATA节点间没有其它类型的节点
     *    
     * <data><![CDATA[content]]></data>
     * @param node
     * @param def
     * @return
     */
    public static final String getItemCDATAContent(Node node, String def) {
        NodeList childNodes = node.getChildNodes();
        StringBuffer buf = null;
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.CDATA_SECTION_NODE) {
                if (buf == null) {
                    buf = new StringBuffer();
                }
                buf.append(((CDATASection) item).getData());
            }
            else if (buf != null) {
                break;//CDATA中断,不再是连续的CDATA节点
            }
        }
        return buf == null ? def : buf.toString();
    }
  
  /**
   * 设置某节点下的大文本节点内容，设置内容为 123
   * <data></data> 设置后为 <data><![CDATA[123]]></data>
   * <data><![CDATA[aaa]]></data>设置后为<data><![CDATA[123]]></data>
   * @param node
   * @param content
   */
  public static final void setItemCDATAContent(Node node, String content){
    NodeList childNodes = node.getChildNodes();
    for (int i = 0, len = childNodes == null ? 0 : childNodes.getLength();  i <len ; i++) {
      Node item = childNodes.item(i);
      if(item.getNodeType() == Node.CDATA_SECTION_NODE){
        ((CDATASection)item).setData(content);
        return ;
      }
    }
    
    /**
     * 如果节点下不存在大文本信息，那么就在该节点下添加一个大文本节点
     */
    Document doc = node.getOwnerDocument();
    CDATASection cdata = doc.createCDATASection(content);
    node.appendChild(cdata);
  }
  
  /**
   * 设置node对应的值,如果value=null则转换为value=""
   * 如:value="123" 
   *   <data>abc</data> 设置后为<data>123</data>
   *   <data><![CDATA[abc]]></data>设置后为<data><![CDATA[123]]></data>
   *   <data><innerNode>abc</innerNode></data> 设置后无效
   *   注意:<data><![CDATA[abc]]></data>设置后value=""后为<data></data>
   */
  public static final void setNodeValue(Node node, String value) {
    if (value == null)
      value = "";
    NodeList childNodes = node.getChildNodes();
    int len = childNodes == null ? 0 : childNodes.getLength();
    for (int i = 0; i < len; i++) {
      Node n = childNodes.item(i);
      n.setNodeValue(value);
    }
  }

  /**
   * 设置元素的属性，如果值为空，则不设置，那么就删除该属性
   *  
   * @param e
   * @param name
   * @param value
   */
  public static final void setElementAttribute(Element e, String name, String value){
    //wuhao 2011/11/20 空字符串的属性是有意义的，由于在clone时会调用该方法，因此此处只判断null
    if(value != null){
      e.setAttribute(name, value);
    }else{
      e.removeAttribute(name);
    }
  }
  
  private static final String JAVAX_XML_TRANSFORM_TRANSFORMER_FACTORY = "javax.xml.transform.TransformerFactory";
  
  public static final void fixTransformerFactory(){
    //如果没有下面这行代码，在jdk1.5上会出错
    try {
      if (System.getProperty("java.version").startsWith("1.5")) {
        /**
         * 在websphere上原来的javax.xml.transform.TransformerFactory就可以用，
         * 在tomcat上需要改成com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl
         */
        String oldcls = System.getProperty(JAVAX_XML_TRANSFORM_TRANSFORMER_FACTORY);
        try {
          Class.forName(oldcls);
        }
        catch (Throwable ta) {
          //ta.printStackTrace();
          if (System.getProperty("java.vendor").startsWith("IBM")) {
            System.setProperty(JAVAX_XML_TRANSFORM_TRANSFORMER_FACTORY,
                "org.apache.xalan.processor.TransformerFactoryImpl");
          } else {
            System.setProperty(JAVAX_XML_TRANSFORM_TRANSFORMER_FACTORY,
                "com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl");
          }
        }
      }
    }
    catch (Exception ex) {
      ex.printStackTrace();
    }
  }
  
    /**
     * 克隆一个节点，仅克隆该节点本身，忽略该节点下的孩子节点。
     * @param doc 用于创建新节点的Document对象
     * @param srcNode 原节点
     * @return 该方法仅支持克隆五种节点：元素节点、文本节点、注释节点、CDATA节点以及属性节点。
     */
    public static Node cloneNode(Document doc, Node srcNode) {
        return cloneNode(doc, srcNode, 0);
    }

    /**
     * 克隆一个节点
     * @param doc 用于创建新节点的Document对象
     * @param srcNode 原节点
     * @param recur 是否递归克隆子节点，该参数为true时，递归克隆其子孙节点，保持节点结构不变
     * @return
     */
    public static Node cloneNode(Document doc, Node srcNode, boolean recur) {
        int level = recur ? Integer.MAX_VALUE : 0;
        return cloneNode(doc, srcNode, level);
    }

    /**
     * 克隆一个节点
     * @param doc 用于创建新节点的Document对象
     * @param srcNode 原节点
     * @param level 递归克隆的最大层次，0表示当前层。
     * @return
     */
    public static Node cloneNode(Document doc, Node srcNode, int level) {
        int nodeType = srcNode.getNodeType();
        Node destNode = null;
        switch (nodeType) {
            case Node.ELEMENT_NODE:
                destNode = doc.createElement(srcNode.getNodeName());
                break;
            case Node.TEXT_NODE:
                destNode = doc.createTextNode(srcNode.getNodeValue());
                break;
            case Node.ATTRIBUTE_NODE:
                destNode = doc.createAttribute(srcNode.getNodeName());
                destNode.setNodeValue(srcNode.getNodeValue());
                break;
            case Node.COMMENT_NODE:
                destNode = doc.createComment(srcNode.getNodeValue());
                break;
            case Node.CDATA_SECTION_NODE:
                destNode = doc.createCDATASection(srcNode.getNodeValue());
                break;
            default:
                return null;
        }
        if (srcNode.getNodeType() == Node.ELEMENT_NODE) {
            if (srcNode.hasAttributes()) {
                NamedNodeMap nodeMap = srcNode.getAttributes();
                int len = nodeMap.getLength();
                int i = 0;
                while (i < len) {
                    Node att = nodeMap.item(i);
                    XmlYn.setElementAttribute((Element) destNode, att.getNodeName(), att.getNodeValue());
                    i++;
                }
            }
            if (level > 0 && srcNode.hasChildNodes()) {
                Node child = srcNode.getFirstChild();
                while (child != null) {
                    Node newChild = cloneNode(doc, child, level - 1);
                    if (newChild != null) {
                        destNode.appendChild(newChild);
                    }
                    child = child.getNextSibling();
                }
            }
        }
        return destNode;
    }

    /**
     * 查找当前文档下带有指定id 属性的 Element对象，深度优先查询
     * <br>
     * 注：DOM 1.0中没有真正实现getElementById方法，故可通过该方法实现。
     * @param doc
     * @param id
     * @return 查找到的第一个元素，查找不到时，返回null。
     */
    public static Element getElementByIdInDepth(Document doc, String id) {
        return getElementByAttributeInDepth(doc, "id", id);
    }
    
    /**
     * 在指定元素节点下查找带有id属性的子孙节点，深度优先查询
     * @param parent
     * @param id
     * @return 查找到的第一个元素，查找不到时，返回null。
     */
    public static Element getChildNodeByIdInDepth(Element parent, String id) {
        return getChildNodeByAttributeInDepth(parent, "id", id);
    }
    
    /**
     * 查找当前文档下具有指定属性，且该属性值与指定值相等的节点，深度优先查询
     * @param doc
     * @param attName
     * @param attValue
     * @return 查找到的第一个元素，查找不到时，返回null。
     */
    public static Element getElementByAttributeInDepth(Document doc, String attName, String attValue) {
        if (StrYn.isNull(attName) || StrYn.isNull(attValue)) {
            //throw new RuntimeException("参数不得为空");
            throw new RuntimeException(I18N.getString("com.esen.util.XmlFunc.3", "参数不得为空"));
        }
        Element elem = XmlYn.getRootElement(doc);
        if (elem == null) {
            return null;
        }
        String value = elem.getAttribute(attName);
        if (!StrYn.isNull(value) && value.equals(attValue)) {
            return elem;
        }
        else {
            return getChildNodeByAttributeInDepth(elem, attName, attValue);
        }
    }
    
    /**
     * 在指定元素节点下查找具有指定属性，且该属性值与指定值相等的节点，深度优先查询
     * @param parent
     * @param id
     * @return 查找到的第一个元素，查找不到时，返回null。
     */
    public static Element getChildNodeByAttributeInDepth(Element parent, String attName, String attValue) {
        if (StrYn.isNull(attName) || StrYn.isNull(attValue)) {
            //throw new RuntimeException("参数不得为空");
            throw new RuntimeException(I18N.getString("com.esen.util.XmlFunc.4", "参数不得为空 "));
        }
        if (!parent.hasChildNodes()) {
            return null;
        }
        Node node = parent.getFirstChild();
        while (node != null) {
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                if (node.hasAttributes()) {
                    String value = ((Element) node).getAttribute(attName);
                    if (!StrYn.isNull(value) && value.equals(attValue)) {
                        return (Element) node;
                    }
                }
                if (node.hasChildNodes()) {
                    Element elem = getChildNodeByAttributeInDepth((Element) node, attName, attValue);
                    if (elem != null) {
                        return elem;
                    }
                }
            }
            node = node.getNextSibling();
        }
        return null;
    }
  
  public static Element getRootElement(Document doc){
      Node node = doc.getFirstChild();
      while (node != null) {
          if (node.getNodeType() == Node.ELEMENT_NODE)
              return (Element) node;
          node = node.getNextSibling(); 
      }
      return null;
  }
  
    public static final List<Element> getChildElements(Element element) {
        List<Element> childElements = new ArrayList<Element>();
        NodeList childNodes = element.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node item = childNodes.item(i);
            if (item.getNodeType() == Node.ELEMENT_NODE) {
                childElements.add((Element) item);
            }
        }
        return childElements;
    }
}