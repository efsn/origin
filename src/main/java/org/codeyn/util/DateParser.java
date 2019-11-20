package org.codeyn.util;

/**
 * 解析下面的日期格式
 * 2008年8月8号
 * 2008-08-08
 * 2008-8-8
 * 2008/08/08
 * 2008/8/8
 * 2008年8月
 * 2008年
 * 2008
 * 2008--
 * 20080808
 *
 * @author work
 */

import org.codeyn.util.yn.StrUtil;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class DateParser {

    private int year = -1, month = -1, day = -1;
    private String format;

    public DateParser(String date) throws Exception {
        parseDate(date);
    }

    private void parseDate(String date) {
        if (date == null || StrUtil.isNull(date)) {
            // throw new IllegalArgumentException("date为NULL, 或者空字符串."); //by xh
            // throw new
            // IllegalArgumentException(I18N.getString("com.esen.util.DateParser.1",
            // "date为NULL, 或者空字符串."));

        }
        String _date = date.replaceAll(" {1,}", "");
        // Pattern p =
        // Pattern.compile("(\\d{4})([\u4e00-\u9fa5]|[\\-/,，\\*\\\\]{1,}|n|)(\\d{2}|\\d{1})?([\u4e00-\u9fa5]|[\\-/,，\\*\\\\]{1,}|y|)(\\d{2}|\\d{1})?([\u4e00-\u9fa5]|[\\-/,，\\*\\\\]{1,}|r|)",
        // Pattern.CASE_INSENSITIVE);
        Pattern p = Pattern
                .compile("(\\d{4})([^\\d]{0,})(\\d{2}|\\d{1})?([^\\d]{0,})(\\d{2}|\\d{1})?([^\\d]{0,})");
        Matcher m = p.matcher(_date);
        StringBuffer formatBuffer = new StringBuffer(1024);
        if (m.find()) {
            String year = m.group(1);
            this.year = parseYear(year);
            formatBuffer.append("yyyy");
            formatBuffer.append(m.group(2));
            String month = m.group(3);
            this.month = parseMonth(month);
            formatBuffer.append("MM");
            formatBuffer.append(m.group(4));
            String day = m.group(5);
            this.day = parseDay(day);
            formatBuffer.append("dd");
            formatBuffer.append(m.group(6));
            this.format = formatBuffer.toString();
        } else {
            // throw new IllegalArgumentException("不是正确的时间格式."); //by xh
            // throw new
            // IllegalArgumentException(I18N.getString("com.esen.util.DateParser.2",
            // "不是正确的时间格式."));
        }
    }

    private int parseYear(String year) {
        return (year == null || year.length() == 0 ? Calendar.getInstance().YEAR
                : Integer.parseInt(year));
    }

    private int parseMonth(String month) {
        int _month = (month == null || month.length() == 0 ? -1 : Integer
                .parseInt(month));
        return _month >= 1 && _month <= 12 ? _month : -1;
    }

    private int parseDay(String day) {
        // TODO 考虑各个月份及闰年的情况.
        int _day = (day == null || day.length() == 0 ? -1 : Integer
                .parseInt(day));
        return _day >= 1 && _day <= 31 ? _day : -1;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public String getFormat() {
        return format;
    }

}
