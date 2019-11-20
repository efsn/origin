package org.codeyn.util;

import org.codeyn.util.file.FileUtil;
import org.codeyn.util.yn.ArrayUtil;
import org.codeyn.util.yn.StrmUtil;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 获取本机网卡物理地址
 */

public final class MacAddress {
    /**
     * windows: Physical Address. . . . . . . . . : 00-13-02-9A-A2-3B
     * <p>
     * linux : eth0 Link encap:Ethernet HWaddr 00:10:5C:FC:B2:7C BROADCAST
     * MULTICAST MTU:1500 Metric:1 RX packets:0 errors:0 dropped:0 overruns:0
     * frame:0 TX packets:0 errors:0 dropped:0 overruns:0 carrier:0 collisions:0
     * txqueuelen:1000 RX bytes:0 (0.0 b) TX bytes:0 (0.0 b) Base address:0xd880
     * Memory:fe980000-fe9a0000
     * <p>
     * Solaris/SunOS
     * 在Solaris和SunOS系统中，以太网的设备一般被称为le0或ie0。为了找到以太网设备的MAC地址，首先你必须成为超级用户
     * ，即root，可以通过使用su命令实现。然后键入ifconfig –a并查看返回信息。例如： # ifconfig -a le0:
     * flags=863<UP,BROADCAST,NOTRAILERS,RUNNING,MULTICAST> mtu 1500 inet
     * 131.225.80.209 netmask fffff800 broadcast 131.225.87.255 ether
     * 8:0:20:10:d2:ae
     * 注意：在Solaris和SunOS系统中，默认是去掉了MAC地址各个字段中的排在前面的0。在上面的例子中，这台机器的实际MAC地址应该为
     * ：08:00:20:10:d2:ae。
     * <p>
     * aix: # netstat -in Name Mtu Network Address Ipkts Ierrs Opkts Oerrs Coll
     * en0 1500 link#2 0.11.25.8.1d.81 21609570 0 3880375 3 0 en0 1500
     * 150.100.16 150.100.16.183 21609570 0 3880375 3 0 lo0 16896 link#1 95267 0
     * 95830 0 0 lo0 16896 127 127.0.0.1 95267 0 95830 0 0 lo0 16896 ::1 95267 0
     * 95830 0 0
     * <p>
     * FreeBSD 在一个FreeBSD系统中，使用dmesg命令将显示本机的MAC地址和其他一些信息。
     * <p>
     * HP 在HP系统中，以太网的地址被典型的称为lan0。通过键入lanscan并查看返回信息就可以得到MAC地址。例如： $ lanscan
     * Hardware Station Dev Hardware Net-Interface NM Encapsulation Mjr Path
     * Address lu State NameUnit State ID Methods Num 2.0.2 0x08000935C99D 0 UP
     * lan0 UP 4 ETHER 52
     * 注意：HP系统中，默认是去掉了MAC地址各个字段的分割符“：”。在上面的例子中，这台机器的实际MAC地址应该为
     * ：08:00:09:35:C9:9D。 在农发行HP-UX上 crsapp1#[/]lanscan Hardware Station Crd
     * Hdw Net-Interface NM MAC HP-DLPI DLPI Path Address In# State NamePPA ID
     * Type Support Mjr# 0/0/12/0/0/0/0/4/0/0/0 0xD8D385F667D3 2 UP lan2 snap2 3
     * ETHER Yes 119 0/0/12/0/0/0/0/4/0/0/1 0xD8D385F667D2 3 UP lan3 snap3 4
     * ETHER Yes 119 0/0/14/0/0/0/0/2/0/0/0 0x3C4A923B191C 4 UP lan4 snap4 5
     * ETHER Yes 119 0/0/14/0/0/0/0/2/0/0/1 0x3C4A923B191D 5 UP lan5 snap5 6
     * ETHER Yes 119 1/0/0/1/0 0x1CC1DE104797 6 UP lan6 snap6 7 ETHER Yes 119
     * 1/0/12/0/0/0/0/4/0/0/0 0xD8D385F67027 9 UP lan9 snap9 10 ETHER Yes 119
     * 1/0/12/0/0/0/0/4/0/0/1 0xD8D385F67026 10 UP lan10 snap10 11 ETHER Yes 119
     * 1/0/14/0/0/0/0/2/0/0/0 0x3C4A923B19C0 11 UP lan11 snap11 12 ETHER Yes 119
     * 1/0/14/0/0/0/0/2/0/0/1 0x3C4A923B19C1 12 UP lan12 snap12 13 ETHER Yes 119
     * LinkAgg0 0xD8D385F667D1 900 UP lan900 snap900 15 ETHER Yes 119 LinkAgg1
     * 0xD8D385F667D0 901 UP lan901 snap901 16 ETHER Yes 119 LinkAgg2
     * 0x000000000000 902 DOWN lan902 snap902 17 ETHER Yes 119 LinkAgg3
     * 0x000000000000 903 DOWN lan903 snap903 18 ETHER Yes 119 LinkAgg4
     * 0x000000000000 904 DOWN lan904 snap904 19 ETHER Yes 119
     */
    static private List<String> collectHWaddrByCMD(List<String> list) {
        if (list == null) list = new ArrayList<String>(3);
        try {
            boolean linux = File.separatorChar == '/';
            String cmd = null;
            if (linux) {
                cmd = "/sbin/ifconfig -a";
                String osname = System.getProperty("os.name");
                if (osname != null) {
                    osname = osname.toUpperCase();
                    if (osname.indexOf("AIX") >= 0) {
                        cmd = "netstat -i";
                    } else if (osname.indexOf("BSD") >= 0) {
                        cmd = "dmesg";// 未经测试
                    }
                }
            } else {
                cmd = "ipconfig /all";
            }
            Process p = Runtime.getRuntime().exec(cmd);
            if (p == null) {
                return list;
            }
            InputStream i = p.getInputStream();
            if (i == null) {
                return list;
            }
            try {
                String s = StrmUtil.stm2Str(i);
                collectMacAddressFromString(list, s);
            } finally {
                i.close();
            }
            return list;
        } catch (Throwable ex) {
            return list;
        }
    }

    static private List<String> collectHWaddr_ByConfigFile(List<String> list) {
        if (list == null) list = new ArrayList<String>(3);
        try {
            boolean linux = File.separatorChar == '/';
            if (!linux) return list;
            java.io.File[] cfgfns = FileUtil.listFiles(
                    "/etc/sysconfig/network-scripts/", "ifcfg-*", 0);
            if (cfgfns == null || cfgfns.length == 0) {
                return list;
            }
            for (int i = 0; i < cfgfns.length; i++) {
                try {
                    String s = FileUtil.file2str(cfgfns[i].getAbsolutePath());
                    collectMacAddressFromString(list, s);
                } catch (Exception ex) {
                }
            }
        } catch (Exception e) {
        }
        return list;
    }

    private static void collectMacAddressFromString(List<String> r, String s) {
        Pattern pattern = Pattern.compile("([1234567890ABCDEFabcdef]{1,2}(\\-|\\:|\\.)){5}[1234567890ABCDEFabcdef]{1,2}");
        Matcher mt = pattern.matcher(s);
        int end = 0;
        while (mt.find(end)) {
            end = mt.end();
            String addr = mt.group().toUpperCase().replace('-', ':').replace('.', ':');
            if ("00:00:00:00:00:00".equals(addr)) // 我的win7上有蛮多这个信息，去掉他们
                continue;
            /**
             * 00:50:56:C0:00 排除以该网址打头的地址，这些地址应该都是虚拟网卡生成的。
             */
            if (addr == null || addr.startsWith("00:50:56:C0:00")) {
                continue;
            }
            String[] ss = addr.split("\\:");
            addr = "";
            for (int i = 0; i < ss.length; i++) {
                String a = ss[i];
                if (a.length() == 1) {
                    a = "0" + a;
                }
                if (addr.length() > 0) addr = addr + ":";
                addr = addr + a;
            }
            if (!r.contains(addr)) r.add(addr);
        }
    }

    public static final List<String> collectHWaddr() {
        List<String> list = collectHWaddr_ByConfigFile(null);
        list = collectHWaddrByCMD(list);
        return list;
    }

    /**
     * 返回找到的物理地址，可能返回长度为0的数组，数组的内容形如：00:15:58:2E:46:B7
     */
    public static final String[] getMacAddresses() {
        List<String> list = collectHWaddr();
        return list.toArray(new String[0]);
    }

    /**
     * 返回第一个物理地址，如果不能找到物理地址，则返回""
     */
    public static final String getMacAddress() {
        String[] l = getMacAddresses();
        return l != null && l.length > 0 ? l[0] : "";
    }

    public static void main(String[] args) {
        System.out.println(ArrayUtil.array2displaystr(getMacAddresses()));
        System.out.println(ArrayUtil.array2displaystr(getMacAddresses()));
    }
}
