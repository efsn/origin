package org.svip.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Common regEx enumeration
 *
 * @author Codeyn
 */
public enum RegExUtil {

    /**
     * Below is security XSS script attack
     */


    Integer("-?[1-9]\\d*|0"),
    FLOAT("-?([1-9]\\d*|0)\\.\\d*"),
    INLAND_PHONE("\\d{3}-\\d{8}|\\d{4}-\\d{7,8}"),
    //begin from 10000
    QQ("[1-9]\\d{4,}", true),
    POST_CODE("[1-9]\\d{5}(?!\\d)"),
    CARD_18("(\\d{6})(\\d{4})(\\d{2})(\\d{3})(\\d|X)"),
    //2012-12-12
    Y_M_D("(\\d{3}[1-9]|\\d{2}[1-9]\\d{1}|\\d{1}[1-9]\\d{2}|[1-9]\\d{3})-(((0[13578]|1[02])-(0[1-9]|[12]\\d|3[01]))|((0[469]|11)-(0[1-9]|[12]\\d|30))|(02-(0[1-9]|[1]\\d|2[0-8])))"),


    IP("((2[0-4]\\d|25[0-5]|[01]?\\d\\d?)\\.){3}(2[0-4]\\d|25[0-5]|[01]?\\d\\d?)"),
    EMAIL("[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?"),
    //    TAG("(?<=<(\\w+)>).*(?=<///1>)"),
    TAG("<(\\w+)>[^<]*</\\1>"),
    URL("[a-zA-z]+://[^\\s]*"),

//    TAG("<(\\S*?)[^*]*>.*?|<.*?/>"),


    //Chinese character
    CHINESE("[\\u4e00-\\u9fa5]"),
    DOUBLE_BYTES_CHAR("[^\\x00-\\xff]"),

    //special character such as ^%&',;=?$"
    INVALID_CHAR("[^%&',;=?$\\x22]+"),


    BLANK_LINE("\\n\\s*\\r"),

    //front last blank
    FL_BLANK("\\s*|\\s*", true),


    //start with letter 5-16 allow letter, number or _
    USERNAME("[a-zA-Z][a-zA-Z0-9_]{4,15}", true),
    PASSWORD("[a-zA-Z][\\w{5,17}", true);

    private static final char ENTIRE_PRE = '^';
    private static final char ENTIRE_SUF = '$';

    private String regEx;
    private boolean isEntire;

    RegExUtil(String regEx, boolean isEntire) {
        this(regEx);
        this.isEntire = isEntire;
    }

    RegExUtil(String regEx) {
        this.regEx = regEx;
    }

    public boolean matches(CharSequence input) {
        return Pattern.matches(getEntireRegEx(), input);
    }

    public Matcher matcher(CharSequence input) {
        return Pattern.compile(getEntireRegEx()).matcher(input);
    }

    public void setIsEntire(boolean isEntire) {
        this.isEntire = isEntire;
    }

    private String getEntireRegEx() {
        StringBuffer sb = new StringBuffer(regEx);
        if (isEntire) {
            sb.setCharAt(0, ENTIRE_PRE);
            sb.append(ENTIRE_SUF);
        }
        return sb.toString();
    }

}
