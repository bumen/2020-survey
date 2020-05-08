package com.xiaoleilu.loServer.action;

import cn.wildfirechat.log.Logs;
import io.moquette.server.Server;
import org.slf4j.Logger;

import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

/**
 * @date 2020-05-08
 * @author zhangyuqiang02@playcrab.com
 */
@Route("/shutdown")
@HttpMethod("POST")
public class ShutdownAction extends Action {
    private static final Logger  LOG = Logs.SERVER;

    @Override
    public boolean action(Request request, Response response) {
        LOG.info("ShutdownAction start shutdown...");

        Thread t = new Thread(()->{
            Server.getServer().stopServer();
        });
        t.setName("Shutdown-Thread");
        t.setDaemon(true);
        t.start();

        LOG.info("ShutdownAction finish");
        return true;
    }
}
