package org.codeyn.util.file;

import java.io.File;
import java.io.FilenameFilter;

//文件过滤类，子目录不受后缀过滤
public class FileFilter implements FilenameFilter{
    private String _ext;

    // ext:.bmp|.bin|.exe...
    public FileFilter(String ext){
        _ext = ext;
    }

    public boolean accept(File dir, String fn){
        if (_ext == null || _ext.length() == 0) return true;// no filter, all
                                                            // accept
        String ext = FileYn.extractFileExt(fn);
        if (ext == null) {
            // judge fn is a dir or file
            File[] f = dir.listFiles();
            for (int i = 0; i < f.length; i++) {
                if ((f[i].isDirectory()) && (f[i].getName().equals(fn)))
                    return true;// accept dir
            }
            return false;
        }
        if ((ext == null) || (_ext.indexOf(ext) == -1))
            return false;
        else
            return true;
    }
}