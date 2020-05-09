package com.playcrab.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

/**
 * @date 2020-04-30
 * @author zhangyuqiang02@playcrab.com
 */
public abstract class TimeUtils {

    public static final String HTTP_DATETIME_PATTERN = "EEE, dd MMM yyyy HH:mm:ss z";

    /**
     * 解析http头时间为unit时间戳
     * @param configTime http头部时间
     * @return 毫秒；失败返回0
     */
    public static long parseHttpDateTime(String configTime) {
        try {
            LocalDateTime date = LocalDateTime
                .parse(configTime, DateTimeFormatter.ofPattern(HTTP_DATETIME_PATTERN));
            return date.toInstant(getSystemZoneOffset()).toEpochMilli();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0L;
    }

    public static String formatHttpDateTime(long time) {
        LocalDateTime date = getLocalDateTimeFromMilli(time);
        return date.format(DateTimeFormatter.ofPattern(HTTP_DATETIME_PATTERN));
    }

    public static LocalDateTime getLocalDateTimeFromMilli(long time) {
        if (time <= 0L) {
            return LocalDateTime.now();
        }

        return LocalDateTime.ofInstant(Instant.ofEpochMilli(time),
            TimeZone.getDefault().toZoneId());
    }


    /**
     * 获取系统time-zone offset,主要用于LocalDateTime的时间戳转换
     * @return ZoneOffset
     */
    public static ZoneOffset getSystemZoneOffset() {
        return OffsetDateTime.now().getOffset();
    }

    public static long spendMs(long preTime) {
        return now() - preTime;
    }

    public static long now() {
        return Instant.now().toEpochMilli();
    }
}
