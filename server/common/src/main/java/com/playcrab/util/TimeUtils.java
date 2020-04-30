package com.playcrab.util;

import java.time.Instant;

/**
 * @date 2020-04-30
 * @author zhangyuqiang02@playcrab.com
 */
public abstract class TimeUtils {


    public static long now() {
        return Instant.now().toEpochMilli();
    }
}
