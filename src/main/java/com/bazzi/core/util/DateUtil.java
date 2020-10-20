package com.bazzi.core.util;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Slf4j
public final class DateUtil {
    public static final String YMD_FORMAT = "yyyy-MM-dd";
    public static final String FULL_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String DEFAULT_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 将日期格式化成字符串形式
     *
     * @param ldt    日期
     * @param format 字符串日期的格式
     * @return 字符串日期
     */
    public static String formatDate(LocalDateTime ldt, String format) {
        if (ldt == null || format == null || "".equals(format))
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
        if (strDate == null || "".equals(strDate) || format == null || "".equals(format))
            return null;
        try {
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
            return LocalDateTime.parse(strDate, dtf);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return null;
        }
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

}
