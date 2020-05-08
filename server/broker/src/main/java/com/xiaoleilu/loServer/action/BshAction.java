package com.xiaoleilu.loServer.action;

import java.io.IOException;

import io.moquette.service.BshService;
import io.netty.handler.codec.http.HttpResponseStatus;

import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

/**
 * @date 2020-05-08
 * @author zhangyuqiang02@playcrab.com
 */
@Route("/bsh")
@HttpMethod({"GET","POST"})
public class BshAction extends IMAction {

    @Override
    public boolean action(Request request, Response response) {
        LOG.info("BshAction start...");

        try {
            BshService.INSTANCE.doGet(request, response);
        } catch (IOException e) {
            LOG.error("BshAction error", e);

            response.setStatus(HttpResponseStatus.OK);
            response.setContent("Script error");
        }

        LOG.info("BshAction finish");
        return true;
    }

}
