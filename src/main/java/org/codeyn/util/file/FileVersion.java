package org.codeyn.util.file;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import org.codeyn.util.i18n.I18N;

public class FileVersion{
    private static long rva; // 记录偏移的位置
    private final static String[] fixs = {"exe", "ocx", "dll", "res"};
    private static long resPos; // 资源节开始的位置//v-rva+resPos数据所在的位置
    private static boolean isRes; // 设置是否资源文件标签
    private static long curpos;
    private static long verPos;
    private static int nameEntryCount, numEntryCount; // 资源的入口数,在其中寻找 0X10
                                                      // //版本资源
    private static RandomAccessFile stm;
    private static long fpos = 0;
    private static boolean hasVer;

    public FileVersion(){
    }

    private static void setCur() throws IOException{
        stm.seek(curpos);
    }

    private static int byteToInt(byte b){
        return ((b & 0x80) << 1) + b;
    }

    private static int bytesToInt(byte[] b, int offset, int len){
        int i, temp, result = 0;
        for (i = 0; i < len; i++) {
            temp = ((b[i + offset] & 0x80) << 1) + b[i + offset];
            result += temp << ((len - i - 1) * 8);
        }
        return result;
    }

    private static long bytesToLong(byte[] b, int offset, int len){
        int i;
        long temp, result = 0;
        for (i = 0; i < len; i++) {
            temp = ((b[i + offset] & 0x80) << 1) + b[i + offset];
            result += temp << ((len - i - 1) * 8);
        }
        return result;
    }

    /**
     * Obtain a word(double byte)
     * 
     * @throws IOException
     */
    private static int getWord() throws IOException{
        byte[] word = new byte[2];
        byte bt = stm.readByte();
        word[1] = bt;
        bt = stm.readByte();
        word[0] = bt;
        return bytesToInt(word, 0, 2);
    }

    /**
     * 改写一个字,注意写的位置
     * 
     * @param value
     *            int
     * @throws IOException
     */
    private static void setWord(int value) throws IOException{
        byte[] word = new byte[2];
        short b = (short) (value % 0x10000);
        word[0] = (byte) (b % 0x100);
        word[1] = (byte) (b / 0x100);
        stm.write(word);
    }

    private static long getalWord() throws IOException{
        byte aByte;
        byte[] alWord = new byte[4];
        aByte = stm.readByte();
        alWord[3] = aByte;
        aByte = stm.readByte();
        alWord[2] = aByte;
        aByte = stm.readByte();
        alWord[1] = aByte;
        aByte = stm.readByte();
        alWord[0] = aByte;
        return bytesToLong(alWord, 0, 4);
    }

    private static int getaiWord() throws IOException{
        byte aByte;
        byte[] alWord = new byte[4];
        aByte = stm.readByte();
        alWord[3] = aByte;
        aByte = stm.readByte();
        alWord[2] = aByte;
        aByte = stm.readByte();
        alWord[1] = aByte;
        aByte = stm.readByte();
        alWord[0] = aByte;
        return bytesToInt(alWord, 0, 4);
    }

    private static boolean comstr(String str) throws IOException{
        int len;
        char aChar, bChar;
        len = str.length();
        str = str.toLowerCase();
        for (int i = 0; i < len; i++) {
            aChar = str.charAt(i);
            bChar = (char) (getaByte());
            if (aChar != bChar) {
                return false;
            }
        }
        return true;
    }

    public static void pln(String msg){
        System.out.println("result is (" + msg + ")");
    }

    /**
     * 是否为资源文件 exe ,res,ocx,dll 三种文件，否则报异常
     * 
     * @return boolean
     */
    private static boolean isResFile(String fn) throws Exception{
        int pos = fn.lastIndexOf(".");
        if (pos == -1) {
            throw new Exception(fn + I18N.getString("com.esen.util.FileVersion.1", "不是可以处理版本的文件类型(exe,dll,ocx,res)"));
        }
        String fix = fn.substring(pos + 1, fn.length());
        boolean checkFix = false;
        for (int i = 0; i < fixs.length; i++) {
            if (fix.compareToIgnoreCase(fixs[i]) == 0) {
                checkFix = true;
                break;
            }
        }
        if (!checkFix) {
            throw new Exception(fn + I18N.getString("com.esen.util.FileVersion.2", "不是可以处理版本的文件类型(exe,dll,ocx,res)"));
        }
        return fix.compareToIgnoreCase("res") == 0;
    }

    /**
     * 创建文件处理的流对象
     */
    private static void init(String fn) throws Exception{
        try {
            stm = new RandomAccessFile(fn, "rw"); // 读写的方式
            stm.seek(0);
            isRes = isResFile(fn);
        } catch (FileNotFoundException ex) {
            stm = null;
            throw new Exception("file" + fn + "not find");
        }
    }

    private static void close() throws IOException{
        if (stm != null) {
            stm.close(); // 关闭文件流
        }
    }

    static void step(int len) throws IOException{
        long pos;
        pos = stm.getFilePointer() + len;
        stm.seek(pos);
    }

    private static long gotoResData() throws IOException{
        long aWord, numSection;
        byte aByte;
        long pePos, secPos, aDword;
        // 节表位置//节表数
        boolean hasRes = false;
        // 在其中遍历整个资源
        step((16 + 4 + 10) * 2); // dos_head
        pePos = getalWord(); // e_lfanew, File address of new exe header
        stm.seek(pePos); // PE Header
        getCur();
        step(4 + 2);
        numSection = getWord();
        step(4 * 3);
        aWord = getWord(); // option的长度
        setCur();
        secPos = pePos + aWord + 4 + 20; // peHead的长度
        stm.seek(secPos); // 到节表所在的位置
        // 每一个节表项为40字节
        for (int i = 0; i < numSection; i++) {
            getCur();
            if (comstr(".rsrc")) {
                hasRes = true;
                setCur();
                step(3 * 4);
                rva = getalWord(); // 虚拟地址
                step(1 * 4);
                resPos = getalWord(); // PointerToRawData
                stm.seek(resPos); // 到达资源节数据所在的位置
                return resPos;
            } else {
                setCur();
                step(40); // 每节40
            }
        }
        return 0;
    }

    /**
     * 通过文件获得版本
     * 
     * @param fileName
     *            String 文件不存在，不是可以处理的文件
     * @throws Exception
     * @return String 没有版本信息返回""
     */
    public static String getVersion(String fileName) throws Exception{
        String result = ""; // 通过文件的绝对路径获得版本
        String[] sver = new String[4];
        long adWord;
        try {
            init(fileName);
            if (isRes) {
                adWord = findResVer();
            } else {
                adWord = findExeVer();
            }
            if (adWord != 0) { // 存在版本
                adWord = getFverPos();
                gotoPos(adWord);
                for (int i = 0; i < 4; i++) {
                    sver[i] = String.valueOf(getWord());
                }
                result = sver[1] + "." + sver[0] + "." + sver[3] + "."
                        + sver[2];
            }
        } finally {
            close();
        }
        return result;

    }

    /**
     * 到达资源部分开始位置
     * @return long 返回文件版本所在位置
     */
    private static long getFverPos() throws IOException{
        // 资源数据开始的位置
        // VS_VERSIONINFO
        step(2 * 3 + (15 + 1) * 2); // Contains the Unicode string
                                    // "VS_VERSION_INFO".
        goBoundary(); // a
        // VS_FIXEDFILEINFO
        step(4 * 2);
        fpos = getPos();
        return fpos;
    }

    private static void gotoPos(long adWord) throws IOException{
        stm.seek(adWord);
    }

    private static void goBoundary() throws IOException{
        int add = 0;
        add = (int) (stm.getFilePointer() % 4);
        step(add);
    }

    private static long getPos() throws IOException{
        return stm.getFilePointer();
    }

    /**
     * 获得资源文件版本开始的地方
     */
    private static int findResVer() throws IOException{
        int dsize, hSize;
        short aword;
        boolean hasVer;
        hasVer = false;
        isres(); // 跳过资源文件的开头
        while (stm.getFilePointer() + 1 < stm.length()) {
            getCur();
            dsize = getaiWord();
            hSize = getaiWord();
            if (getWord() == 0XFFFF) {
                if (getWord() == 0X0010) {
                    hasVer = true;
                    break;
                } else {
                    setCur();
                    step(dsize + hSize);
                }
            }
        }
        if (hasVer) {
            step(4 + 4 + 2 + 2 + 4 + 4); // 头type之后的部分
            return (int) stm.getFilePointer();
        }
        return 0;
    }

    private static long findExeVer() throws IOException{
        long aint;
        aint = gotoResData();
        if (aint == 0) {
            return 0;
        } else {
            return gotoVerData();
        }
    }

    private static void getCur() throws IOException{
        curpos = stm.getFilePointer();
    }

    private static int getaByte() throws IOException{
        byte aByte;
        aByte = stm.readByte();
        return byteToInt(aByte);
    }

    // ////////////////////////////////
    private static long go(long dentry) throws IOException{
        // 遍历找到第一个语言版本, 当32位为1指向IMAGE_RESOURCE_DIRECTORY,
        // o指向数据结构（16位）
        long aDword;
        if ((stm.getFilePointer() + 1) >= stm.length()) {
            return 0; // 防止错误的结构成死循环
        }
        if (dentry >= 0X80000000L) { // 入口(16)
            aDword = dentry - 0X80000000L;
            aDword = aDword + resPos;
            gotoPos(aDword);
            step(4 * 3);
            nameEntryCount = getWord();
            numEntryCount = getWord();
            /*
             * System.out.print(nameEntryCount); System.out.print("-");
             * System.out.print(numEntryCount); System.out.print("-");
             * System.out.print(stm.getFilePointer());
             */
            if (numEntryCount == 0) {
                return 0;
            } else {
                step(nameEntryCount * 8 + 4);
                aDword = getalWord();
                return go(aDword); // 递归
            }
        } else { // 数据(16)
            step(-4); // 读的正好是偏移地址
            aDword = getalWord(); // 结构偏移
            aDword = aDword + resPos;
            gotoPos(aDword); // 结构位置
            aDword = getalWord(); // 数据虚拟地址
            aDword = aDword - rva + resPos;
            gotoPos(aDword); // 版本资源的开始位置
            verPos = aDword;
            return aDword;
        }
        // return 0;
    }

    // /////////////////////////////////////
    private static long gotoVerData() throws IOException{
        long aDword = 0; // 遍历整个资源节，获得版本信息资源的位置
        hasVer = false;
        getCur(); // 到达RESOURCE_DIRECTORY所在的位置
        step(4 * 3);
        nameEntryCount = getWord();
        nameEntryCount = (getWord() + nameEntryCount);
        // System.out.println(nameEntryCount);
        // 下面的几个资源分类列表
        // directory_entry结构,8字节长
        for (int i = 0; i < nameEntryCount; i++) {
            aDword = getalWord();
            if (aDword == 0X10) {
                aDword = getalWord(); // 版本的入口
                hasVer = true;
                break;
            } else {
                step(4);
            }
        }
        if (hasVer) {
            hasVer = false;
            verPos = 0;
            aDword = go(aDword);
            return verPos;
        } else {
            return 0;
        }
    }

    public static final void setVersion(String fn, String version)
            throws Exception{
        String[] source;
        if (version.indexOf(",") != -1) {
            source = version.split(",");
        } else {
            if (version.indexOf(".") != -1) {
                source = version.split("\\.");
            } else {
                source = new String[1];
                source[0] = version;
            }
        } // 解析版本信息
        String[] sver = {"0", "0", "0", "0"}; // 解析版本
        for (int i = 0; i < source.length; i++) {
            sver[i] = source[i]; // 设置版本值和默认值
        }
        int aInt;
        for (int j = 0; j < sver.length; j++) {
            try {
                aInt = Integer.parseInt(sver[j]);
            } catch (Exception e) {
                // throw new Exception(version + "不是一个有效的版本号");
                throw new Exception(version
                        + I18N.getString("com.esen.util.FileVersion.3",
                                "不是一个有效的版本号"));
            }
        } // 版本号合法
        long adWord;
        try {
            init(fn);
            if (isRes) {
                adWord = findResVer();
            } else {
                adWord = findExeVer();
            }
            if (adWord != 0) { // 存在版本
                adWord = getFverPos();
                gotoPos(adWord);
                setWord(Integer.parseInt(sver[1]));
                setWord(Integer.parseInt(sver[0]));
                setWord(Integer.parseInt(sver[3]));
                setWord(Integer.parseInt(sver[2]));
            }
        } finally {
            close();
        }
    }

    // 1,2.3,4-->1.2.3.5
    /**
     * @param fn
     *            String
     * @param part
     *            int 1.2.3.4标志四个位置
     * @param value
     *            int 为负数表示减版本号
     * @throws Exception
     * @return String 版本的完整信息 如 1.0.0.0没有版本号返回空
     */
    public static final String incVersion(String fn, int part, int value)
            throws Exception{
        String result = "";
        String[] sver = new String[4];
        if (part < 1 || part > 4) part = 4;
        switch (part) {
            case 4:
                part = 3;
                break;
            case 3:
                part = 4;
                break;
            case 2:
                part = 1;
                break;
            case 1:
                part = 2;
                break;
        }
        int verSour = 0;
        long adWord;
        try {
            init(fn);
            if (isRes) {
                adWord = findResVer();
            } else {
                adWord = findExeVer();
            }
            if (adWord != 0) { // 存在版本
                adWord = getFverPos();
                gotoPos(adWord);
                step((part - 1) * 2);// 到达指定版本
                verSour = getWord();
                step(-2);// 回到当前指针
                setWord(verSour + value);
                step(part * (-2));// 回到版本开始的地方
                for (int i = 0; i < 4; i++) {
                    sver[i] = String.valueOf(getWord());
                }
                result = sver[1] + "." + sver[0] + "." + sver[3] + "."
                        + sver[2];
            }
        } finally {
            close();
        }
        return result;
    }

    /**
     * 跳过资源文件开头的32个标志性字节（内容是固定的）
     * 
     * @throws IOException
     */
    private static void isres() throws IOException{
        step(32);
    }
}
