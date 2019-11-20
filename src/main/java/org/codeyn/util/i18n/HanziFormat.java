package org.codeyn.util.i18n;

/**
 * 该类将数字格式化处理为大写或者小写的汉字数字字符串。。
 */
public class HanziFormat {

    private final static String[] digit_s = {"○", "一", "二", "三", "四", "五", "六",
            "七", "八", "九"};

    private final static String[] digit_t = {"零", "壹", "贰", "叁", "肆", "伍", "陆",
            "柒", "捌", "玖"};

    private static final String[] unit_s = {"", "十", "百", "千", "万", "十", "百",
            "千", "亿", "十", "百", "千", "兆", "十", "百", "千"};

    private static final String[] unit_t = {"", "拾", "佰", "仟", "万", "拾", "佰",
            "仟", "亿", "拾", "佰", "仟", "兆", "拾", "佰", "仟"};

    private final static long[] mask = {1L, 10L, 100L, 1000L, 10000L, 100000L,
            1000000L, 10000000L, 100000000L, 1000000000L, 10000000000L,
            100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
            1000000000000000L, 10000000000000000L, 100000000000000000L,
            1000000000000000000L};

    /**
     * 将长整型数字转换为汉字大写数字字符串。 例如：HanziFormat.toHanZiTr(1253654) --> "壹佰贰拾伍万叁仟陆佰伍拾肆"
     *
     * @param x 需要转换的数字，该数字包含的位数不能超过15，否则会出现转换异常。
     * @return 数字转换后的汉字字符串。
     */
    public static String toHanZiTr(long x) {
        return toHanZi(x, digit_t, unit_t);
    }

    /**
     * 将长整型数字转换为汉字小写数字字符串。 例如：HanziFormat.toHanZiSi(1253654) --> "一百二十五万三千六百五十四"
     *
     * @param x 需要转换的数字，该数字包含的位数不能超过15，否则会出现转换异常。
     * @return 数字转换后的汉字字符串。
     */
    public static String toHanZiSi(long x) {
        String[] digit = digit_s;
        String[] unit = unit_s;

        if (x == 0) {
            return digit[0];
        }

        StringBuffer sb = new StringBuffer(40);

        if (x < 0) {
            x = -x;
            sb.append("负");
        }

        boolean lastzero = false;
        boolean hasvalue = false; // 亿、万进位前有数值标记
        int len = 0;
        long n;

        long xx = x;// 计算x的位数
        while (xx > 0) {
            len++;
            xx /= 10;
        }

        if (len > 16) {
            return "数值过大!";
        }

        for (int i = len - 1; i >= 0; i--) {

            n = (x % mask[i + 1]) / mask[i];

            if (n != 0) {
                if (lastzero) {
                    sb.append(digit[0]); // 若干零后若跟非零值，只显示一个零
                }

                if (!(n == 1 && (i % 4) == 1 && i == len - 1)) { // 十进位处于第一位不发壹音
                    sb.append(digit[(int) n]);
                }

                sb.append(unit[i]); // 非零值后加进位，个位为空
                hasvalue = true; // 置万进位前有值标记
            } else {
                if ((i % 8) == 0 || ((i % 8) == 4 && hasvalue)) { // 亿万之间必须有非零值方显示万
                    sb.append(unit[i]); // “亿”或“万”
                }
            }
            if (i % 8 == 0) {
                hasvalue = false; // 万进位前有值标记逢亿复位
            }
            lastzero = (n == 0) && (i % 4 != 0);
        }

        return sb.toString();
    }

    /**
     * 将数字转换为汉字
     */
    private static String toHanZi(long x, String[] digit, String[] unit) {

        if (x == 0) {
            return digit[0];
        }

        StringBuffer sb = new StringBuffer(40);

        if (x < 0) {
            x = -x;
            sb.append("负");
        }

        boolean lastzero = false;
        boolean hasvalue = false; // 亿、万进位前有数值标记
        int len = 0;
        long n;

        long xx = x;// 计算x的位数
        while (xx > 0) {
            len++;
            xx /= 10;
        }

        if (len > 16) {
            return "数值过大!";
        }

        for (int i = len - 1; i >= 0; i--) {

            n = (x % mask[i + 1]) / mask[i];

            if (n != 0) {
                if (lastzero) {
                    sb.append(digit[0]); // 若干零后若跟非零值，只显示一个零
                }

                sb.append(digit[(int) n]);
                sb.append(unit[i]); // 非零值后加进位，个位为空
                hasvalue = true; // 置万进位前有值标记
            } else {
                if ((i % 8) == 0 || ((i % 8) == 4 && hasvalue)) { // 亿万之间必须有非零值方显示万
                    sb.append(unit[i]); // “亿”或“万”
                }
            }
            if (i % 8 == 0) {
                hasvalue = false; // 万进位前有值标记逢亿复位
            }
            lastzero = (n == 0) && (i % 4 != 0);
        }

        return sb.toString();
    }

    /**
     * 见数字转换为繁体汉字表示的货币数字写法，会四舍五入到小数点后两位（也就是到分）。
     *
     * @param val 需要转换为繁体汉字表示的数字。
     * @return 转换好的繁体汉字货币数字字符串，包含货币单位：元、角、分等。
     */
    public static String toRMB(double val) {

        StringBuffer rmb = new StringBuffer(40);
        long fraction, integer;
        int jiao, fen;

        if (val > 99999999999999.999 || val < -99999999999999.999) {
            return "数值位数过大!";
        }
        // 四舍五入到分
        long temp = Math.round(val * 100);
        integer = temp / 100;
        rmb.append(toHanZiTr(integer));
        rmb.append("元");
        fraction = temp % 100;
        jiao = (int) fraction / 10;
        fen = (int) fraction % 10;
        if (jiao == 0 && fen == 0) {
            rmb.append("整");
        } else {
            rmb.append(digit_t[jiao]);
            if (jiao != 0) {
                rmb.append("角");
            }
            if (integer == 0 && jiao == 0) { // 零元后不写零几分
            }
            if (fen != 0) {
                rmb.append(digit_t[fen]);
                rmb.append("分");
            }
        }
        // 下一行可用于非正规金融场合，0.03只显示“叁分”而不是“零元叁分”
        // if( !integer ) return SignStr+TailStr;
        return rmb.toString();
    }
}
