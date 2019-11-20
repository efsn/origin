package org.codeyn.util;

import java.io.InputStream;

/**
 * 此函数为了兼容ireport而留下，其它地方不要调用 yk
 */

public final class LocalMachine {
    private LocalMachine() {
    }

    /**
     * 此函数为了兼容ireport而留下，其它地方不要调用
     */
    static public String getHWaddr() {
        String add = isLinux() ? "HWaddr " : "Physical Address. . . . . . . . . : ";
        return getInfo(add);
    }

    static public String getIp() {
        if (isLinux()) {
            //TODO linux下面叫什么？
            return "127.0.0.1";
        }
        String add = "IP Address. . . . . . . . . . . . : ";
        return getInfo(add);
    }

    static private boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().indexOf("win") == -1;
    }

    static private String getInfo(String name) {
        String r = "";
        try {
            String os = System.getProperty("os.name");
            boolean linux = os.toLowerCase().indexOf("win") == -1;
            String cmd = linux ? "/sbin/ifconfig -a" : "ipconfig /all";
            Process p = Runtime.getRuntime().exec(cmd);
            if (p == null) {
                return r;
            }
            InputStream i = p.getInputStream();
            byte[] bs = new byte[1024 * 8];
            int l = i.read(bs);
            String s = new String(bs, 0, l);
            int k = s.indexOf(name);
            if (k == -1) {
                return r;
            }
            int m = s.indexOf("\n", k);
            if (m == -1) {
                return r;
            }
            r = s.substring(k + name.length(), m);
            r = r.replace('-', ':');
            return r.trim();
        } catch (Exception ex) {
            return r;
        }
    }

    public static void main(String[] args) {
        System.out.println(LocalMachine.getHWaddr());
        System.out.println(LocalMachine.getIp());
    }
}