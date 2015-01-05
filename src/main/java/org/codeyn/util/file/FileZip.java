//Source file: D:\\i-report-server\\src\\com\\sanlink\\util\\FileZip.java

package org.codeyn.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

import org.codeyn.util.Maps;
import org.codeyn.util.i18n.I18N;
import org.codeyn.util.yn.StmYn;
import org.codeyn.util.yn.StrYn;

/**
 * 文件压缩类，所生成的文件能被FileUnZip解压缩，也可以被delphi类解压缩 压缩文件的结构:固定长度的uncompressed zipid +
 * 所有文件的压缩流
 */
public class FileZip{
    final static String ZIPID = "{EB71174D-FD89-468E-B932-42A8286CC745}";// 压缩文件头标识
    final static String GZIPID = "{E1A571E4-DDB4-4DE2-82A1-F1263B15B7E6}";// 压缩文件头标识
    final static String ID = "{DF3D6B55-6BC0-4E7C-A307-23503B12767F}";// 压缩文件头标识
    // final static String ERR_INVALIDFILE = "无效的文件或目录";
    // final static String ERR_INVALIDDESTFILE = "无效的目标文件或目录";
    // final static String ERR_NOFILE = "没有要压缩的文件或目录";

    public static final int ZIP_DONT = 1;
    public static final int ZIP_ZIP = 2;
    public static final int ZIP_GZIP = 3;
    public static final int ZIP_UNKNOWN = 100;// 未知压缩格式

    private ArrayList _files = new ArrayList();// 所有需要压缩的文件列表；
    private ArrayList _destNames = new ArrayList();// 所有压缩文件对应的解压文件名；
    protected OutputStream _out; // 压缩目的文件流
    private int _totalSize;
    private String _fn;// zip file name
    private String _header = "";// 压缩包标识
    private String _option;// 压缩文件大小和个数
    private int _count; // 个数
    private int zip;

    protected static String getERR_INVALIDFILE(){

        return I18N.getString("com.esen.util.FileZip.1", "无效的文件或目录");

    }

    protected static String getERR_INVALIDDESTFILE(){

        return I18N.getString("com.esen.util.FileZip.2", "无效的目标文件或目录");

    }

    protected static String getERR_NOFILE(){

        return I18N.getString("com.esen.util.FileZip.3", "没有要压缩的文件或目录");

    }

    /**
     * @param fn
     *            压缩包文件名，绝对路径
     */
    public FileZip(String fn) throws Exception{
        _fn = fn;
        this.zip = ZIP_ZIP;
    }

    /**
     *
     * @param fn
     * @param zip
     * @throws java.lang.Exception
     */
    public FileZip(OutputStream o, int zip) throws Exception{
        _out = o;
        this.zip = zip;
    }

    public FileZip(String fn, int zip) throws Exception{
        _fn = fn;
        this.zip = zip;
    }

    /**
     * 在压缩时使用者可以指定一个文字串作为此压缩包的标志，以供解压缩时判断是否是所需要 类型的压缩包。
     * 
     * @param s
     */
    public void addHeader(String s){
        _header = s;
    }

    /**
     * @param fn
     * @param destfn
     *            ,相对路径
     */
    public void addFile(String fn, String destfn) throws Exception{
        File f = new File(fn);
        if (_files.contains(f)) return;// 如果一个文件压缩两次
        if (destfn.length() == 0)
            destfn = fn;
        else {
            destfn = FileYn.delDriver(destfn);
            if (destfn == null) destfn = fn; // 如果目的文件名空，目的文件名就是原文件名
        }
        setZipFile(f, destfn);
    }

    void setZipFile(File f, String destfn){
        _totalSize += f.length();
        _count++;
        _files.add(f);
        _destNames.add(destfn);
    }

    /**
     * @param dir
     *            要压缩的指定目录
     * @param destdir
     *            解压缩此目录时生成的目的目录，相对路径
     * @param exts
     *            只压缩扩展名在exts中的文件，如果exts为空则压缩所有文件，exts的形式为“.txt|.bin| .bmp”
     * @param recur
     *            是否压缩子目录
     */
    public void addDir(String dir, String destdir, String exts, boolean recur)
            throws Exception{
        if (destdir == null) destdir = "";
        if (destdir.length() != 0) {
            destdir = FileYn.delDriver(destdir);
        }
        File path = new File(dir);
        if (path.getParent() == null) return;// dir not exists
        String desf;
        File[] files = path
                .listFiles(new org.codeyn.util.file.FileFilter(exts));
        for (int i = 0; (files != null) && (i < files.length); i++) {// if a
                                                                     // subdir????
            desf = destdir + path.separator + files[i].getName();
            if (files[i].isDirectory()) {
                // first add the dir file into _files
                setZipFile(files[i], desf);
                // recursively
                if (recur) addDir(files[i].getPath(), desf, exts, recur);
            } else
                setZipFile(files[i], desf);
        }
    }

    protected String getZipId(int zipType){
        // zip 不是一定跟zipid一一对应的,所以增加一个属性
        switch (zipType) {
            case ZIP_DONT:
                return ID;
            case ZIP_ZIP:
                return ZIPID;
            case ZIP_GZIP:
                return GZIPID;
            default:
                return null;
        }
    }

    /**
     * 是否过滤掉需要压缩的文件 ,子类可继承该方法 对不需要的压缩的 “文件目录” 进行过滤
     * 
     * @param name
     * @param file
     * @return true 表示需要压缩此文件，false 表示不需要压缩
     */
    protected boolean ZipFileFilter(String name, File file){
        return true;
    }

    protected void writeZipId(OutputStream out, String zipId) throws Exception{
        if (!StrYn.isNull(zipId)) {
            StmYn.writeFix(out, zipId, zipId.length());
        }
    }

    /*
     * zipid={EB71174D-FD89-468E-B932-42A8286CC745}
     * 
     * header=用户自定义的一个字符串 option=size=2423;count=20
     */
    public void startZip() throws Exception{
        boolean closestm = false;
        if (_out == null) {
            File f = new File(_fn);
            f = f.getParentFile();
            f.mkdirs();
            _out = new FileOutputStream(_fn);
            closestm = true;
        }
        try {
            // zip 不是一定跟zipid一一对应的,所以方法供子类继承
            String zipId = getZipId(zip);
            writeZipId(_out, zipId);

            switch (zip) {
                case ZIP_DONT:
                    _out = new BufferedOutputStream(_out);
                    break;
                case ZIP_ZIP:
                    _out = new DeflaterOutputStream(_out);
                    break;
                case ZIP_GZIP:
                    _out = new GZIPOutputStream(_out);
                    break;
            }
            BufferedInputStream in = null;

            // write zippkg header: zipId + "#0"+header+"#0"+
            genZipHead();
            // zip each file
            for (int i = 0; i < _count; i++) {
                File f = (File) _files.get(i);
                String fn = (String) _destNames.get(i);
                // 对于file 是文件目录的 在解压时直接过滤掉了 这里为不影响现有的逻辑 加一个过滤函数给子类继承
                if (!ZipFileFilter(fn, f)) continue;
                // 获得需要压缩文件的一些属性
                genFileInfo(fn, f);
                if (!f.isDirectory()) {
                    in = new BufferedInputStream(new FileInputStream(f));
                    try {
                        StmYn.stmCopyFrom(in, _out, in.available());
                        Thread.yield();
                    } finally {
                        in.close();
                    }
                }
            }
        } finally {
            _out.flush();
            if (closestm) {
                _out.close();
            } else {
                /**
                 * 如果是压缩流，必须调用finish来往流里面写入数据，flush的方式不起作用，故使用 FileZip(out, xx)
                 * 创建的zip对象时，就会导致压缩流没有完全写入文件
                 */
                if (zip == ZIP_ZIP || zip == ZIP_GZIP) {
                    DeflaterOutputStream deflaterOut = ((DeflaterOutputStream) _out);
                    deflaterOut.finish();
                }
            }
        }
    }

    void genZipHead() throws IOException{
        Map<String, String> m = new HashMap<String, String>();
        m.put("size", new Integer(_totalSize).toString());
        m.put("count", new Integer(_count).toString());
        _option = Maps.toString(m, "=", ";");
        StmYn.writeString(_out, _header);
        StmYn.writeString(_out, _option);
    }

    /*
     * 文件信息＝fn=fn1;ab=RS;sz=234;dir=true|false
     * //fn:文件解压缩后的文件名，ab:文件属性，sz:文件大小，dir:是不是目录 文件属性由0到三个字符表示分别为：RSH，表示只读，系统，隐藏
     * #0??really realized by write string?? How about System file attributes
     */

    protected void genFileInfo(String name, File file) throws IOException{
        String ab = "", dir = "false";
        if (!file.canWrite()) ab += "R";
        if (file.isHidden()) ab += "H";
        if (file.isDirectory()) dir = "true";
        Map<String, String> m = new HashMap<String, String>();
        m.put("fn", name);
        m.put("ab", ab);
        m.put("sz", new Long(file.length()).toString());
        m.put("dir", dir);
        StmYn.writeString(_out, Maps.toString(m, "=", ";"));
    }

}
