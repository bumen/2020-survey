package io;

import org.slf4j.MDC;

/**
 * @date 2020-05-07
 * @author zhangyuqiang02@playcrab.com
 */
public class TraceWrapperRunnable implements Runnable {

    private final String userId;
    private final String traceId;

    private final Runnable runnable;

    private static final String USER_ID_KEY = "MQTT-UserId";
    private static final String TRACE_ID_KEY = "MQTT-TraceId";

    public TraceWrapperRunnable(Runnable runnable) {
        this.userId = MDC.get(USER_ID_KEY);
        this.traceId = MDC.get(TRACE_ID_KEY);
        this.runnable = runnable;

    }

    @Override
    public void run() {
        try {
            MDC.put(TRACE_ID_KEY, traceId);
            MDC.put(USER_ID_KEY, userId);
            runnable.run();
        } finally {
            MDC.remove(USER_ID_KEY);
            MDC.remove(TRACE_ID_KEY);
        }
    }
}
