package org.svip.util;

public class RegExUtilTest {

    public static void main(String[] args) {

        System.out.println("Tag:" + RegExUtil.TAG.matches("<ehcache>sss</ehcache>"));

        System.out.println(RegExUtil.FL_BLANK.matches(" xx "));


    }

}
