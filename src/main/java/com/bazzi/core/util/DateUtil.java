package com.bazzi.core.util;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class DateUtil {
    public static final String YMD_FORMAT = "yyyy-MM-dd";
    public static final String FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private static final String SPACE_JOINER = " ";
    private static final String TIME_PATTERN = "[[HH][:mm][:ss][.SSS]]";

    /**
     * 将日期格式化成字符串形式
     *
     * @param ldt    日期
     * @param format 字符串日期的格式
     * @return 字符串日期
     */
    public static String formatDate(LocalDateTime ldt, String format) {
        if (ldt == null || format == null || format.isEmpty())
            return null;
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        return dtf.format(ldt);
    }

    /**
     * 将字符串日期转换成日期类型
     *
     * @param strDate 字符串日期
     * @param format  字符串日期的格式
     * @return 日期
     */
    public static LocalDateTime getDate(String strDate, String format) {
        if (strDate == null || strDate.isEmpty() || format == null || format.isEmpty())
            return null;
        return LocalDateTime.parse(strDate, buildDateTimeFormatter(format));
    }

    /**
     * 获取相距日期n天的日期
     *
     * @param ldt 日期
     * @param n   正数往后，负数往前
     * @return 计算后的日期
     */
    public static LocalDateTime getNextDay(LocalDateTime ldt, int n) {
        if (ldt == null) {
            return null;
        }
        return ldt.plusDays(n);
    }

    /**
     * 获取日期的开始时间，即yyyy-MM-dd 00:00:00.000
     *
     * @param ldt 日期
     * @return 该日期的开始时间
     */
    public static LocalDateTime startOfDay(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return ldt.toLocalDate().atStartOfDay();
    }

    /**
     * 获取日期的结束时间，即yyyy-MM-dd 23:59:59.999
     *
     * @param ldt 日期
     * @return 该日期的结束时间
     */
    public static LocalDateTime endOfDay(LocalDateTime ldt) {
        if (ldt == null) {
            return null;
        }
        return LocalDateTime.of(ldt.toLocalDate(), LocalTime.MAX);
    }

    /**
     * 将Date转为LocalDateTime
     *
     * @param date Date
     * @return LocalDateTime
     */
    public static LocalDateTime convertToLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * 将LocalDateTime转为Date
     *
     * @param ldt LocalDateTime
     * @return Date
     */
    public static Date convertToDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 两个日期之间的天数差，忽略时分秒
     *
     * @param d1 日期
     * @param d2 日期
     * @return 天数差
     */
    public static int dayDiff(Date d1, Date d2) {
        return dayDiff(convertToLocalDateTime(d1), convertToLocalDateTime(d2));
    }

    /**
     * 两个日期之间的天数差，忽略时分秒
     *
     * @param ldt1 日期
     * @param ldt2 日期
     * @return 天数差
     */
    public static int dayDiff(LocalDateTime ldt1, LocalDateTime ldt2) {
        return Math.abs((int) ChronoUnit.DAYS.between(ldt1.toLocalDate(), ldt2.toLocalDate()));
    }

    /**
     * 构建用于打印和解析日期时间对象的格式化程序 24小时制
     *
     * @param format 日期格式
     * @return DateTimeFormatter
     */
    private static DateTimeFormatter buildDateTimeFormatter(String format) {
        String curFormat;
        if (format == null || format.isEmpty())//缺省的日期格式
            curFormat = YMD_FORMAT + SPACE_JOINER + TIME_PATTERN;
        else if (!format.contains(SPACE_JOINER)) //不包含时分秒
            curFormat = format + SPACE_JOINER + TIME_PATTERN;
        else {
            String[] arr = format.split(SPACE_JOINER);
            if (arr[0].contains("H")) //时分秒在前
                curFormat = TIME_PATTERN + SPACE_JOINER + arr[1];
            else
                curFormat = arr[0] + SPACE_JOINER + TIME_PATTERN;
        }

        return new DateTimeFormatterBuilder().appendPattern(curFormat)
                .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)//缺省0，小时
                .parseDefaulting(ChronoField.MINUTE_OF_HOUR, 0)//缺省0，分钟
                .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)//缺省0，秒
                .toFormatter();
    }

}
