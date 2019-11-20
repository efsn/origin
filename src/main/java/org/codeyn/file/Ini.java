package org.codeyn.file;

import org.codeyn.util.yn.StrUtil;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Arthur
 * @version 1.0
 */
public class Ini {

    private Map<String, Map<String, String>> sections = new HashMap<String, Map<String, String>>();

    /**
     * 创建Ini对象，Ini中的sections从给定的流中加载
     *
     * @param stm 保存了ini格式文本的流
     * @throws IOException
     */
    public Ini(InputStream stm) throws IOException {
        load(stm);
    }

    /**
     * 创建Ini对象，Ini中的sections从给定的流中加载，在加载文本时采用指定字符集
     *
     * @param stm     保存了ini格式文本的流
     * @param charset 加载文本时所用的字符集
     * @throws IOException
     */
    public Ini(InputStream stm, String charset) throws IOException {
        load(stm, charset);
    }

    /**
     * 创建Ini对象，Ini中的sections从给定的Reader对象中加载
     *
     * @param reader 保存了ini格式文本的流
     * @throws IOException
     */
    public Ini(Reader reader) throws IOException {
        load(reader);
    }

    public Ini(String path) throws IOException {
        load(path);
    }

    public Ini() {

    }

    private Map<String, String> esureSectionExist(final String key) {
        if (StrUtil.isNull(key)) {
            return null;
        }
        Map<String, String> section = this.getSection(key);
        if (section == null) {
            section = new HashMap<String, String>();
            sections.put(key.toLowerCase(), section);
        }
        return section;
    }

    /**
     * 将指定section中的指定属性值设置为给定的double型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     *
     * @param section  section名
     * @param propName 属性名
     * @param v
     * @return
     */
    public double setDouble(final String section, final String propName, double v) {
        this.esureSectionExist(section).put(propName, Double.toString(v));
        return v;
    }

    /**
     * 将指定section中的指定属性值设置为给定的int型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     */
    public int setInt(final String section, final String propName, int v) {
        this.esureSectionExist(section).put(propName, String.valueOf(v));
        return v;
    }

    /**
     * 将指定section中的指定属性值设置为给定的boolean型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     */
    public boolean setBool(final String section, final String propName, boolean v) {
        esureSectionExist(section).put(propName, String.valueOf(v));
        return v;
    }

    /**
     * 将指定section中的指定属性值设置为给定的String型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     */
    public String setValue(final String section, final String propName,
                           final String v) {
        esureSectionExist(section).put(propName, v);
        return v;
    }

    /**
     * 将指定section中的指定属性值设置为给定的double型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     *
     * @param section  section名
     * @param propName 属性名
     * @param v
     * @return
     */
    public double setValue(final String section, final String propName,
                           final double v) {
        return setDouble(section, propName, v);
    }

    /**
     * 将指定section中的指定属性值设置为给定的int型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     *
     * @param section  section名
     * @param propName 属性名
     * @param v
     * @return
     */
    public int setValue(final String section, final String propName, final int v) {
        return setInt(section, propName, v);
    }

    /**
     * 将指定section中的指定属性值设置为给定的boolean型值
     * 如果指定的section或属性不存在，则在section列表中添加新的值
     *
     * @param section  section名
     * @param propName 属性名
     * @param v
     * @return
     */
    public boolean setValue(final String section, final String propName,
                            final boolean v) {
        return setBool(section, propName, v);
    }

    /**
     * 将sections中的内容保存到ini文本文件中
     *
     * @param fn ini文本文件路径
     * @throws Exception
     */
    public void save(String fn) throws Exception {
        FileOutputStream os = new FileOutputStream(fn);
        try {
            save(os);
        } finally {
            os.close();
        }
    }

    /**
     * 将sections中的内容保存到给定输出流中
     *
     * @param out 保存sections的输出流
     * @throws IOException
     */
    public void save(OutputStream out) throws IOException {
        out.write(toString().getBytes());
    }

    /**
     * 将sections中的内容保存到给定输出流中，并用指定编码保存文本内容
     */
    public void save(OutputStream out, String charset) throws IOException {
        out.write(toString().getBytes(charset));
    }

    /**
     * 将sections中的内容转化成ini格式的字符串
     */
    public String toString() {
        StringBuffer str = new StringBuffer(1024);
        String[] secs = getSections();
        for (int i = 0; i < secs.length; i++) {
            str.append("[");
            str.append(secs[i]);
            str.append("]\r\n");
            Map<String, String> p = this.getSection(secs[i]);
            if (p != null) str.append(p.toString());
            str.append("\r\n");
        }
        return str.toString();
    }

    /**
     * 清空sections中的所有section
     */
    public void clearAll() {
        this.sections.clear();
    }

    /**
     * 从给定的Reader对象中加载文本内容，并将文本内容解析成sections
     * 将解析后的内容存入sections中
     *
     * @param reader 保存了ini格式文本的Reader对象
     * @throws IOException
     */
    public void load(Reader reader) throws IOException {
        if (reader instanceof BufferedReader)
            load((BufferedReader) reader);
        else
            load(new BufferedReader(reader));
    }

    /**
     * 从BufferedReader对象中加载文本内容，并将文本内容解析成sections
     * 将解析后的内容存入section列表中
     */
    public void load(BufferedReader reader) throws IOException {
        String l = reader.readLine();
        Map<String, String> p = null;
        int idx;
        while (l != null) {
            l = l.trim();
            if ((l.startsWith("[") && (l.endsWith("]")))) {
                String sectionName = l.substring(1, l.length() - 1);
                p = this.esureSectionExist(sectionName);
            } else if ((idx = l.indexOf('=')) > 0) {
                if (p == null) {
                    p = new HashMap<String, String>();
                    sections.put(null, p);
                }
                p.put(l.substring(0, idx), l.substring(idx + 1));
            }
            l = reader.readLine();
        }
    }

    /**
     * 从给定流中加载文本内容，并将文本内容解析成sections
     *
     * @param stm 保存了ini格式文本的流
     * @throws IOException
     */
    public void load(InputStream stm) throws IOException {
        load(new BufferedReader(new InputStreamReader(stm)));
    }

    /**
     * 从给定流中加载文本内容，并将文本内容解析成sections，加载文本时采用指定字符集
     *
     * @param stm     保存了ini格式文本的流
     * @param charset 加载文本时所用的字符集
     * @throws IOException
     */
    public void load(InputStream stm, String charset) throws IOException {
        load(new BufferedReader(new InputStreamReader(stm, charset)));
    }

    /**
     * 从给定路径的ini文件中加载文本，并解析出sections
     *
     * @param path ini文件路径
     * @throws IOException
     */
    public void load(String path) throws IOException {
        FileInputStream is = new FileInputStream(path);
        try {
            load(is);
        } finally {
            is.close();
        }
    }

    public String[] getSections() {
        return sections.keySet().toArray(new String[0]);
    }

    /**
     * 从指定的section中取出指定属性的值，如果确定指定的属性值是boolean型，则可以调用此方法
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public boolean getBool(final String section, final String propName,
                           boolean def) {
        String v = getValue(section, propName, null);
        return v == null ? def : (v.equalsIgnoreCase("true") || v.equals("1"));
    }

    /**
     * 从指定的section中取出指定属性的值，如果确定指定的属性值是int型，则可以调用此方法
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public int getInt(final String section, final String propName, int def) {
        String v = getValue(section, propName, null);
        try {
            return v == null ? def : Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 从指定的section中取出指定属性的值，如果确定指定的属性值是double型，则可以调用此方法
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public double getDouble(final String section, final String propName,
                            double def) {
        String v = getValue(section, propName, null);
        try {
            return v == null ? def : Double.parseDouble(v);
        } catch (Exception e) {
            return def;
        }
    }

    /**
     * 从指定的section中取出指定属性的值，获取的值为String类型
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public final String getValue(final String section, final String propName,
                                 String def) {
        Map<String, String> s = getSection(section);
        return s == null ? def : s.get(propName);
    }

    /**
     * 从指定的section中取出指定属性的值，获取的值为boolean类型
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public final boolean getValue(final String section, final String propName, boolean def) {
        return getBool(section, propName, def);
    }

    /**
     * 从指定的section中取出指定属性的值，获取的值为int类型
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public final int getValue(final String section, final String propName, int def) {
        return getInt(section, propName, def);
    }

    /**
     * 从指定的section中取出指定属性的值，获取的值为double类型
     *
     * @param section  section名称
     * @param propName 属性名称
     * @param v        如果要获取的属性值不存在，则返回该值，这是自定义的
     * @return
     */
    public final double getValue(final String section, final String propName, double def) {
        return getDouble(section, propName, def);
    }

    public Map<String, String> getSection(final String sectionName) {
        return sectionName == null ? null : sections.get(sectionName.toLowerCase());
    }

    public Map<String, String> deleteSection(final String sectionName) {
        return sectionName == null ? null : sections.remove(sectionName.toLowerCase());
    }

    public boolean sectionExists(final String sectionName) {
        return sections.containsKey(sectionName);
    }
}
