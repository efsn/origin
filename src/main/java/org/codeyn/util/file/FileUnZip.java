//Source file: D:\\i-report-server\\src\\com\\sanlink\\util\\FileUnZip.java

package org.codeyn.util.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.codeyn.util.Maps;
import org.codeyn.util.yn.StmYn;

/**
 * 压缩包解压缩类，能解压缩delphi类压缩的文件包
 */
public class FileUnZip{
    protected String _dest;
    private String _head;
    private int _count;
    private FileInputStream fs;
    protected InputStream _in;
    final static String ERRZIPFILE = "Invalid zip file!";

    protected long curSize;

    /**
     * @param fn
     *            file to unzip
     * @throws java.io.FileNotFoundException
     * @throws FileNotFoundException
     */
    public FileUnZip(String fn) throws FileNotFoundException, Exception{
        fs = new FileInputStream(fn);
        try {
            init(fs);
        } catch (Exception ex) {
            fs.close();
            fs = null;
            throw ex;
        }
    }

    public FileUnZip(InputStream in) throws Exception{
        init(in);
    }

    /**
     * 由于zip 类型与zipid 并不是一一对应所以 提供此方法供子类继承
     * 
     * @param in
     * @return
     */
    protected String readZipId(InputStream in) throws Exception{
        return StmYn.readFix(in, FileZip.ZIPID.length());
    }

    /**
     * 根据zipid 获得zip 类型
     * 
     * @param zipId
     * @return
     * @throws Exception
     */
    protected int getZipType(String zipId) throws Exception{
        if (FileZip.ZIPID.equals(zipId)) {
            return FileZip.ZIP_ZIP;
        } else if (FileZip.GZIPID.equals(zipId)) {
            return FileZip.ZIP_GZIP;
        } else if (FileZip.ID.equals(zipId)) {
            return FileZip.ZIP_DONT;
        } else {
            return FileZip.ZIP_UNKNOWN;
        }
    }

    private void init(InputStream in) throws Exception{
        if (in == null) {
            throw new Exception("in is null");
        }
        in = new BufferedInputStream(in);
        String id = readZipId(in);
        int zipType = getZipType(id);
        switch (zipType) {
            case FileZip.ZIP_DONT:
                _in = new BufferedInputStream(in);
                break;
            case FileZip.ZIP_ZIP:
                _in = new InflaterInputStream(in);
                break;
            case FileZip.ZIP_GZIP:
                _in = new GZIPInputStream(in);
                break;
            default:
                throw new Exception(ERRZIPFILE);
        }
        checkHeader(_in);
    }

    void checkHeader(InputStream in) throws Exception{
        _head = StmYn.readString(in);
        String s = getOptions(in); // option
        Map<String, String> m = Maps.toMap(s, "=", ";");
        String cstr = m.get("count");
        _count = Integer.parseInt(cstr == null ? "0" : cstr);
    }

    protected String getOptions(InputStream in) throws Exception{
        return StmYn.readString(in);
    }

    public int getCount(){
        return _count;
    }

    /**
     * 解压缩文件到指定的目录下
     * 
     * @param destdir
     * @throws IOException
     */
    public void unzip(String destdir) throws IOException, Exception{

        File dir = new File(destdir);
        if (!dir.exists()) dir.mkdirs();

        BufferedOutputStream out = null;
        try {
            _dest = destdir;
            // unzip a file
            for (int i = 0; i < _count; i++) {
                File f = getFileInfo(_in);
                if (f == null) break;
                if (!f.isDirectory()) {
                    out = new BufferedOutputStream(new FileOutputStream(f));
                    try {
                        StmYn.stmCopyFrom(_in, out, curSize);
                        Thread.yield();
                    } finally {
                        out.close();
                    }
                } else {
                    f.mkdirs();
                }
            }
        } finally {
            close();
        }
    }

    public void close() throws Exception{
        if (fs != null) {
            fs.close();
        }
    }

    private File f;

    File getFileInfo(InputStream in) throws Exception{
        /**
         * BI-5464 etl导入主题集后，维表是乱码 原因：i压缩时是使用的GBK编码，BI解压时使用的是UTF-8,所以出现了乱码问题。
         * 这里指定采用GBK解码即可
         */
        String s = StmYn.readString(in, "GBK");
        if (s == null) return null;
        Map<String, String> m = Maps.toMap(s, "=", ";");
        String fn = m.get("fn");
        if (File.separatorChar != '\\') {
            fn = fn.replace('\\', File.separatorChar);
            // unix separator & fixed char in script\
        }
        fn = _dest + File.separator + fn;
        String dir = m.get("dir");
        String ab = m.get("ab");
        curSize = Long.parseLong(m.get("sz"));
        f = new File(fn);
        if (dir.equals("true")) {
            if (!f.exists()) {
                f.mkdirs();
            }
        } else {
            if (ab.indexOf("R") > -1) {
                f.setReadOnly(); // what about hidden file attribute,ignore at
                                 // this moment
            }
            if (f.exists()) {
                f.delete(); // 如果已经存在，先删除！
            }
            FileYn.createDirsOfFile(f);
        }
        return f;
    }

    /**
     * @return String
     */
    public String getHeader(){
        return _head;
    }

    public static void main(String[] args){

        try {
            // FileUnZip uz = new FileUnZip("F:\\恢复异常\\2007-10-31-1-9.SSB");
            FileUnZip uz = new FileUnZip(
                    "C:\\Documents and Settings\\wakeup\\桌面\\我的备份\\20071127_091727.SSB2");
            System.out.println(uz.getHeader());
            uz.unzip("f:\\234");
            uz.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
