/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package cn.wildfirechat.server;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import cn.wildfirechat.log.Logs;
import io.TraceWrapperRunnable;
import org.slf4j.Logger;

public class ThreadPoolExecutorWrapper {
    private static final Logger LOG = Logs.MQTT;
    private final ScheduledExecutorService executor;
    private final int count;
    private final AtomicInteger runCounter;
    private final String name;

    public ThreadPoolExecutorWrapper(ScheduledExecutorService executor, int count, String name) {
        this.executor = executor;
        this.count = count;
        this.runCounter = new AtomicInteger();
        this.name = name;
    }

    public void execute(Runnable task) {
        executor.execute(new TraceWrapperRunnable(task));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
