package org.codeyn.util;

import org.codeyn.util.exception.ExceptionHandler;
import org.codeyn.util.i18n.I18N;
import org.codeyn.util.yn.StrUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public final class SysUtil {

    private static Map<String, String> envmap = null;

    private SysUtil() {
    }

    /**
     * @return logic cpu count, one core a cpu in multi-core
     */
    public static int getAvailableProcessors() {
        Runtime rt = Runtime.getRuntime();
        int r = rt.availableProcessors();
        return r < 1 ? 1 : r;
    }

    /**
     * Obtain environment variable of Option system
     */
    public static Map<String, String> getenv() {
        try {
            return new HashMap<String, String>(System.getenv());
        } catch (Throwable ex) {
            return getenv_from_cmd();
        }
    }

    private static Map<String, String> getenv_from_cmd() {
        synchronized (SysUtil.class) {
            if (envmap != null) return envmap;
            Map<String, String> map = new HashMap<String, String>(20);
            Process p = null;
            try {
                p = Runtime.getRuntime().exec(File.separatorChar == '\\' ? "cmd /c set" : "env");
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    int i = line.indexOf('=');
                    if (i >= 0) {
                        map.put(line.substring(0, i), line.substring(i + 1));
                    }
                }
                br.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            envmap = map;
            return map;
        }
    }

    public static String getenv(String name, String defvalue) {
        Map<String, String> env = getenv();
        if (env.containsKey(name)) {
            return env.get(name);
        }
        return defvalue;
    }

    public static String getenvIgnoreCase(String name, String defvalue) {
        Map<String, String> env = getenv();
        Set<Map.Entry<String, String>> set = env.entrySet();
        Iterator<Map.Entry<String, String>> itr = set.iterator();
        while (itr.hasNext()) {
            Map.Entry<String, String> entry = itr.next();
            if (StrUtil.compareText(entry.getKey(), name)) {
                return entry.getValue();
            }
        }
        return defvalue;
    }

    public static String getenv(String name) {
        return getenv(name, null);
    }

    /**
     * @return used memory, unit MB
     */
    public static long getUsedMem_mb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / StrUtil.MB;
    }

    public static String getMemInfo() {
        Runtime rt = Runtime.getRuntime();
        StringBuffer r = new StringBuffer(64);
        r.append("FREE=");
        r.append(StrUtil.formatSize(rt.freeMemory()));
        r.append(" TOTAL=");
        r.append(StrUtil.formatSize(rt.totalMemory()));
        r.append(" MAX=");
        r.append(StrUtil.formatSize(rt.maxMemory()));
        return r.toString();
    }

    public static void showMemInfo() {
        Runtime rt = Runtime.getRuntime();
        System.out.print(I18N.getString("com.esen.util.SysFunc.1", "空闲:"));
        System.out.print(StrUtil.formatSize(rt.freeMemory()));
        System.out.print(I18N.getString("com.esen.util.SysFunc.2", "已分配:"));
        System.out.print(StrUtil.formatSize(rt.totalMemory()));
        System.out.print(I18N.getString("com.esen.util.SysFunc.3", "最大可分配: "));
        System.out.println(StrUtil.formatSize(rt.maxMemory()));
    }

    /**
     * 在所有活动线程堆栈中获取指定线程的当前状态，返回值wait为当前线程正处于等待中，
     * sleep为当前线程正处于睡眠中，run为当前线程正处于运行状态中
     *
     * @param ast 所有活动线程的堆栈
     * @param t   要获取状态的线程
     * @return
     */
    public static String getThreadState(Map<Thread, StackTraceElement[]> ast, Thread t) {
        StackTraceElement[] ste = ast.get(t);
        if (ste.length > 0 && ste[0].isNativeMethod()) {
            String s = ste[0].toString();
            if (s.indexOf("Object.wait") > 0)
                return "wait";
            if (s.indexOf("Thread.sleep") > 0 || s.indexOf("sun.misc.Unsafe.park") >= 0) {
                return "sleep";
            }
        }
        return "run";
    }

    public static String getThreadStateCaption(String state) {
        // if ("wait".equals(state)) return "等待中";
        if ("wait".equals(state))
            return I18N.getString("com.esen.util.SysFunc.4", "等待中 ");
        // if ("sleep".equals(state)) return "睡眠中";
        if ("sleep".equals(state))
            return I18N.getString("com.esen.util.SysFunc.5", "睡眠中");
        // if ("run".equals(state)) return "运行中";
        if ("run".equals(state))
            return I18N.getString("com.esen.util.SysFunc.6", "运行中");
        return null;
    }

    /**
     * 打印输出所有的线程dump
     */
    public static void printAllStackTraces() {
        Map<Thread, StackTraceElement[]> tste = Thread.getAllStackTraces();
        if (tste == null) {
            System.out.print(I18N.getString("com.esen.util.SysFunc.7", "需要JDK 1.5或其它高版本JDK的支持！"));
            return;
        }
        for (Iterator<Map.Entry<Thread, StackTraceElement[]>> iter = tste.entrySet().iterator(); iter.hasNext(); ) {
            Map.Entry<Thread, StackTraceElement[]> entry = iter.next();
            Thread t = entry.getKey();
            StackTraceElement[] ste = entry.getValue();
            System.out.println("Thread: " + t.toString());
            for (int i = 0; i < ste.length; i++) {
                System.out.print('\t');
                System.out.println(ste[i]);
            }
        }
    }

    public static void showUsedMem() {
        Runtime rt = Runtime.getRuntime();
        System.out.print(I18N.getString("com.esen.util.SysFunc.8", "使用内存="));
        System.out.println(StrUtil.formatSize(rt.totalMemory() - rt.freeMemory()));
    }

    public static String formatBytes(long bts) {
        long bt = bts % 1024;
        long kb = (bts / 1024) % 1024;
        long mb = bts / (1024 * 1024);
        return ((mb > 0) ? (mb + "MB,") : "") + ((kb > 0) ? (kb + "KB,") : "")
                + ((bt > 0) ? (bt + "BYTES") : "");
    }

    public static boolean isClass(Object o, String cls) {
        if (o == null || cls == null) {
            throw new java.lang.NullPointerException();
        }
        return o.getClass().getName().equals(cls);
    }

    public static Object getSuperClassDeclaredField(Object o, String fld)
            throws Exception {
        if (o == null || fld == null) {
            throw new java.lang.NullPointerException();
        }
        Class cls = o.getClass().getSuperclass();
        Field f = cls.getDeclaredField(fld);
        if (f != null) {
            f.setAccessible(true);
            Object r = f.get(o);
            f.setAccessible(false);
            return r;
        } else {
            return null;
        }
    }

    public static Object getSuperSuperClassDeclaredField(Object o, String fld)
            throws Exception {
        if (o == null || fld == null) {
            throw new java.lang.NullPointerException();
        }
        Class cls = o.getClass().getSuperclass().getSuperclass();
        Field f = cls.getDeclaredField(fld);
        if (f != null) {
            f.setAccessible(true);
            Object r = f.get(o);
            f.setAccessible(false);
            return r;
        } else {
            return null;
        }
    }

    public static String getSuperClasses(Object o) throws Exception {
        Class cls = o.getClass();
        StringBuffer sb = new StringBuffer(cls.getName());
        while (cls != null && !cls.getName().equals("Object")) {
            cls = cls.getSuperclass();
            if (cls != null) {
                sb.append("<<");
                sb.append(cls.getName());
            }
        }
        return sb.toString();
    }

    public static String getDeclaredFields(Object o) throws Exception {
        return o.getClass().getName() + "\r\n"
                + _getDeclaredFields(0, o.getClass());
    }

    private static String _getDeclaredFields(int level, Class cls)
            throws Exception {
        if (cls == null) {
            return null;
        }
        Field[] fs = cls.getDeclaredFields();
        if (fs != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < fs.length; i++) {
                for (int j = 0; j < level; j++) {
                    sb.append(" ");
                }
                sb.append(fs[i].getName());
                sb.append(":");
                Class clsfs = fs[i].getType();
                String s = clsfs.getName();
                sb.append(s);
                sb.append("\r\n");
                if (!s.startsWith("java.") && !s.startsWith("javax.")
                        && level < 20) {
                    String sss = _getDeclaredFields(level + 1, clsfs);
                    if (sss != null) {
                        sb.append(sss);
                    }
                }
            }
            return sb.toString();
        } else {
            return null;
        }
    }

    /**
     * 返回类o中方法p的值，如果方法不存在，返回null。
     *
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     */
    public static Object getMethodValueIgnoreCase(Object o, String p,
                                                  Object defvalue) throws IllegalArgumentException,
            IllegalAccessException {
        if (p == null || p.length() == 0) {
            return defvalue;
        }
        Method m = getMethodIgnoreCaseWithNoParams(o, p);
        if (m != null) try {
            return m.invoke(o, null);
        } catch (InvocationTargetException e) {
            /**
             * 20090320
             * 通过反射调用函数时，如果函数抛异常，那么虚拟机会用InvocationTargetException对异常进行包装
             * 。这会导致原始的异常信息没法抛到客户端。 这里作一个处理，抛出原始异常。
             */
            ExceptionHandler.rethrowRuntimeException(e.getCause());
        }

        return defvalue;
    }

    /**
     * 获得o中的方法p，活略大小写，且方法是不带参数的。
     */
    public static Method getMethodIgnoreCaseWithNoParams(Object o, String p) {
        Class cls = o.getClass();
        Method[] methods = cls.getMethods();
        int point = 0;
        Method r = null;
        for (int i = 0; i < methods.length; i++) {
            Method m = methods[i];
            if (m.getName().equalsIgnoreCase(p)
                    && m.getParameterTypes().length == 0) {
                return m;
            }
        }
        return null;
    }

    public static Object getDeclaredField(Object o, String fld)
            throws Exception {
        if (o == null || fld == null) {
            throw new java.lang.NullPointerException();
        }
        Class cls = o.getClass();
        Field f = cls.getDeclaredField(fld);
        if (f != null) {
            f.setAccessible(true);
            Object r = f.get(o);
            f.setAccessible(false);
            return r;
        } else {
            return null;
        }
    }

    /**
     * 获取一个类所在的class文件所在的路径，例如：SysFunc.getClassUrl(
     * "oracle.jdbc.driver.OracleDriver")返回的是：
     * jar:file:/D:/eclipse/workspace/jars
     * /bi_lib/branches/2.2/lib/oracle_ojdbc14
     * -1.0.0.jar!/oracle/jdbc/driver/OracleDriver.class 如果类不存在，那么返回null
     */
    public static URL getClassUrl(String clsname) {
        // get class
        String classname_resource = "/" + clsname.replace('.', '/') + ".class";

        // get class URL
        URL url = SysUtil.class.getResource(classname_resource);

        return url;
        // String urlPath = url.getPath();
    }

    /**
     * 判断当前jvm是否是64bit的jvm
     */
    public static boolean is64bitJVM() {
        // 参考util工程下的doc目录下的AIX5.3-IBMJDK-PPT.txt和WIN7-JROCKET1.6-PPT.txt
        String s = System.getProperty("sun.arch.data.model");// sun、jrocket都支持这个变量，ibm不支持
        if (s != null) {
            return "64".equals(s);
        }
        s = System.getProperty("com.ibm.vm.bitmode");// ibm jdk
        return "64".equals(s);
    }
}
