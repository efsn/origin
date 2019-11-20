package org.codeyn.util.zip;

import java.io.*;
import java.util.zip.Deflater;


/**
 * 将目录压缩成zip格式的类。
 * <p>Copyright: Copyright (c) 2006</p>
 * <p>武汉新连线科技有限公司</p>
 *
 * @author chxb
 * @version 5.0
 */
public class ZipDirectory {

    /**
     * 压缩级别，可设置为0-9,数字越高，则压缩率越高。
     */
    int level = Deflater.DEFAULT_COMPRESSION;
    /*文件过滤器。满足过滤器的文件，不对其进行压缩*/
    private FileFilter fileFilter;

    public ZipDirectory() {
        this.fileFilter = null;
    }

    public ZipDirectory(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }

    /**
     * 设置压缩级别，可传入0-9，数字越高，表示压缩率越高。
     *
     * @param level
     */
    public void setLevel(int level) {
        this.level = level;
    }

    /**
     * 压缩文件和目录，忽略空目录
     *
     * @param srcFile 源文件或目录
     * @param output  目标输出流。
     */
    public void zip(File srcFile, OutputStream output) throws Exception {
        zip(srcFile, output, true);
    }

    /**
     * 压缩文件和目录，可设置是否忽略空目录
     *
     * @param srcFile        源文件或目录
     * @param output         目标输出流
     * @param ignoreEmptyDir 是否忽略空目录
     * @throws Exception
     */
    public void zip(File srcFile, OutputStream output, boolean ignoreEmptyDir) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(output));
        out.setLevel(level);
        out.setEncoding("GBK");
        zip(out, srcFile, srcFile.getName(), ignoreEmptyDir);
        out.close();
    }

    public void zipFiles(File[] srcFiles, OutputStream output) throws Exception {
        zipFiles(srcFiles, output, true);
    }

    public void zipFiles(File[] srcFiles, OutputStream output, boolean ignoreEmptyDir) throws Exception {
        ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(output));
        out.setLevel(level);
        out.setEncoding("GBK");
        for (int i = 0; i < srcFiles.length; i++)
            zip(out, srcFiles[i], srcFiles[i].getName(), ignoreEmptyDir);
        out.close();
    }

    /**
     * 向zip流添加entry. 如果entry是目录，则递归子目录添加所有子目录下的文件。
     * 可以设置是否忽略空目录
     *
     * @param out
     * @param f
     * @param base
     * @param ignoreEmptyDir
     * @throws Exception
     */
    private void zip(ZipOutputStream out, File f, String base, boolean ignoreEmptyDir) throws Exception {
        //过滤掉满足过滤器的文件，不对其进行压缩。
        if (f.isFile() && this.fileFilter != null && this.fileFilter.accept(f))
            return;
        if (f.isDirectory()) {
            File[] fl = f.listFiles();
            /**
             * 当ignoreEmptyDir为false时，即使目录为空，也在压缩文件下添加该目录的条目。
             * 原来该方法将这两条语句注释掉了，导致遇到空目录时，会忽略掉该目录。
             * 这样导致该方法执行方式不一致，当用该方法压缩一个非空目录时，压缩文件中有该目录的条目；
             * 而压缩一个空目录时，压缩文件中却没有任何内容。
             */
            if (!ignoreEmptyDir) {
                ZipEntry entry = new ZipEntry(base + File.separator);
                out.putNextEntry(entry);
            }
            base = base.length() == 0 ? "" : base + File.separator;
            for (int i = 0; i < fl.length; i++) {
                zip(out, fl[i], base + fl[i].getName(), ignoreEmptyDir);
            }
        } else {
            out.putNextEntry(new ZipEntry(base));
            FileInputStream in = new FileInputStream(f);
            try {
                byte[] buf = new byte[1024];
                int b;
                while ((b = in.read(buf)) != -1) {
                    out.write(buf, 0, b);
                }
            } finally {
                in.close();
            }
        }
    }

    public final FileFilter getFileFilter() {
        return fileFilter;
    }

    public final void setFileFilter(FileFilter fileFilter) {
        this.fileFilter = fileFilter;
    }


} 
