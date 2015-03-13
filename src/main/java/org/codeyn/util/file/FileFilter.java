package org.codeyn.util.file;

import java.io.File;
import java.io.FilenameFilter;

import org.codeyn.util.yn.StrUtil;

/**
 * Filter Files except Directory which via suffix
 * <p>ext:.bmp|.bin|.exe...etc
 * 
 * @author Codeyn
 * @version 1.0
 */
public class FileFilter implements FilenameFilter{

    private String ext;

    public FileFilter(String ext){
        this.ext = ext;
    }

    /**
     * if none filter all
     */
    public boolean accept(File dir, String fn){
        if (StrUtil.isNull(this.ext)) return true;
        String ext = FileUtil.extractFileExt(fn);
        if (ext == null) {
            File[] f = dir.listFiles();
            for (int i = 0; i < f.length; i++) {
                if ((f[i].isDirectory()) && (f[i].getName().equals(fn)))
                    return true;
            }
            return false;
        }
        return this.ext.indexOf(ext) > -1;
    }
}