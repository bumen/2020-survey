package io;

import java.util.UUID;

import org.slf4j.MDC;

/**
 * @date 2020-05-07
 * @author zhangyuqiang02@playcrab.com
 */
public class TraceRunnable implements Runnable {

    private final String userId;

    private final Runnable runnable;

    private static final String USER_ID_KEY = "MQTT-UserId";
    private static final String TRACE_ID_KEY = "MQTT-TraceId";

    public TraceRunnable(String userId, Runnable runnable) {
        this.userId = userId;
        this.runnable = runnable;
    }

    @Override
    public void run() {
        try {
            String traceId = UUID.randomUUID().toString().replaceAll("-", "");
            MDC.put(TRACE_ID_KEY, traceId);
            MDC.put(USER_ID_KEY, userId);
            runnable.run();
        } finally {
            MDC.remove(USER_ID_KEY);
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
