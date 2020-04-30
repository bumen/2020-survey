/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.xiaoleilu.loServer.action.admin;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.log.Logs;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.proto.WFCMessage;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

@Route(APIPath.Modify_User)
@HttpMethod("POST")
public class ModifyUserAction extends AdminAction {

    private static final Logger logger = Logs.HTTP;

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            InputOutputUserInfo gameUser = getRequestBody(request.getNettyRequest(),
                InputOutputUserInfo.class);

            if (gameUser == null || StringUtil.isNullOrEmpty(gameUser.getUserId())) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("ModifyUserAction#action game user id error");

                return true;
            }

            String userId = gameUser.getUserId();

            WFCMessage.User user = messagesStore.getUserInfo(userId);
            if (user == null) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult
                    .resultOf(ErrorCode.ERROR_CODE_NOT_EXIST);
                response.setContent(new Gson().toJson(result));

                logger.warn("ModifyUserAction#modify user:{} not exsit", userId);

                return true;
            }

            WFCMessage.User.Builder builder = user.toBuilder();
            builder.setName(gameUser.getName());
            builder.setPortrait(gameUser.getPortrait());

            try {
                messagesStore.addUserInfo(builder.build(), "");

                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.ok();
                response.setContent(new Gson().toJson(result));

                logger.info("ModifyUserAction#modify user:{} ok", userId);

                return true;
            } catch (Exception e) {
                logger.error("ModifyUserAction#modify user:{} error", userId, e);

                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult
                    .resultOf(ErrorCode.ERROR_CODE_NOT_EXIST);
                response.setContent(new Gson().toJson(result));

                return true;
            }
        }

        logger.warn("ModifyUserAction#action modify user fail");

        return true;
    }


}
