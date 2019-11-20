package org.codeyn.util;

import org.codeyn.util.i18n.I18N;

import java.util.Calendar;
import java.util.Date;

/**
 * 该对象与Timeago意思一样,而是显示一个时间距现在发生了多久(与微博,微信之类的显示格式一样),显示格式如下:
 * 间隔时间超过1一年:显示:xx年前,大约一年前
 * 间隔天数超过30天  :显示大约1个月前或xx月前
 * 其他时间精度类似,直到 xx分钟前
 * <p>
 * 使用示例:
 * <pre>
 *     String timeago = TimeAgo.foramt(System.currentTimeMillis() - 5000);
 *     结果为:5秒前
 *
 *   或者
 *
 *     timeago = TimeAgo.intervalFormat(5000);
 *     结果为:5秒前
 * </pre>
 *
 * @Date : Feb 18, 2014
 * @Author: wang
 */
public abstract class TimeAgo {

    /**
     * @return day
     */
    private static String getDay() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.DAY", "1天");
    }

    /**
     * @return days
     */
    private static String getDays(Object day) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.DAYS", "{0}天", day);
    }

    /**
     * @return hour
     */
    private static String getHour() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.HOUR", "大约1小时");
    }

    /**
     * @return hours
     */
    private static String getHours(Object hour) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.HOURS", "{0}小时", hour);
    }

    /**
     * @return minute
     */
    private static String getMinute() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.MINUTE", "大约1分钟");
    }

    /**
     * @return minutes
     */
    private static String getMinutes(Object minutes) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.MINUTES", "{0}分钟", minutes);
    }

    /**
     * @return month
     */
    private static String getMonth() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.MONTH", "大约1个月");
    }

    /**
     * @return months
     */
    private static String getMonths(Object months) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.MONTHS", "{0}月", months);
    }

    /**
     * @return seconds
     */
    private static String getSeconds(Object seconds) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.SECONDS", "不到1分钟", seconds);
    }

    /**
     * @return suffixAgo
     */
    private static String getSuffixAgo() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.AGO", "前");
    }

    /**
     * @return suffixFromNow
     */
    private static String getSuffixFromNow() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.SUFFIX_FROM_NOW", "从现在开始");
    }

    /**
     * @return year
     */
    private static String getYear() {
        return I18N.getString("UTIL.COMMON.TIMEAGO.YEAR", "大约1年");
    }

    /**
     * @return years
     */
    private static String getYears(Object years) {
        return I18N.getString("UTIL.COMMON.TIMEAGO.YEARS", "{0}年", years);
    }

    /**
     * Join time string with prefix and suffix. The prefix and suffix are only
     * joined with the time if they are non-null and non-empty
     *
     * @param prefix
     * @param time
     * @param suffix
     * @return non-null joined string
     */
    private static String join(final String prefix, final String time, final String suffix) {
        StringBuilder joined = new StringBuilder();
        if (prefix != null && prefix.length() > 0)
            joined.append(prefix);
        joined.append(time);
        if (suffix != null && suffix.length() > 0)
            joined.append(suffix);
        return joined.toString();
    }

    /**
     * Get time string for milliseconds distance
     *
     * @param distanceMillis
     * @param allowFuture
     * @return time string
     */
    private static String format(long distanceMillis, final boolean allowFuture) {
        final String suffix;
        if (allowFuture && distanceMillis < 0) {
            distanceMillis = Math.abs(distanceMillis);
            suffix = getSuffixFromNow();
        } else {
            suffix = getSuffixAgo();
        }

        final double seconds = distanceMillis / 1000;
        final double minutes = seconds / 60;
        final double hours = minutes / 60;
        final double days = hours / 24;
        final double years = days / 365;

        final String time;
        if (seconds < 45)
            time = getSeconds(seconds);
        else if (seconds < 90)
            time = getMinute();
        else if (minutes < 45)
            time = getMinutes(Math.round(minutes));
        else if (minutes < 90)
            time = getHour();
        else if (hours < 24)
            time = getHours(Math.round(hours));
        else if (hours < 48)
            time = getDay();
        else if (days < 30)
            time = getDays(Math.floor(days));
        else if (days < 60)
            time = getMonth();
        else if (days < 365)
            time = getMonths(Math.floor(days / 30));
        else if (years < 2)
            time = getYear();
        else
            time = getYears(Math.floor(years));

        return join(null, time, suffix);
    }

    /**
     * 根据java.util.Date 与当前时间 比较,显示"xx时间前"
     *
     * @param java.util.Date 时间
     * @return timeAgo格式
     */
    public static String format(final Date date) {
        return format(date.getTime());
    }

    /**
     * 根据java.util.Calendar 与当前时间 比较,显示"xx时间前"
     *
     * @param date java.util.Calendar 时间
     * @return timeAgo格式
     */
    public static String format(final Calendar date) {
        return format(date.getTime());
    }

    /**
     * 根据"时间的毫秒数" 与当前时间 比较,显示"xx时间前"
     *
     * @param dateInMillis
     * @return timeAgo格式
     */
    public static String format(final long dateInMillis) {
        return intervalFormat(System.currentTimeMillis() - dateInMillis);
    }

    /**
     * 根据"时间差距"的"毫秒数"来显示 "xx{时间}前"
     *
     * @param intervalInMillis
     * @return timeAgo格式
     */
    public static String intervalFormat(final long intervalInMillis) {
        return format(intervalInMillis, false);
    }

}
