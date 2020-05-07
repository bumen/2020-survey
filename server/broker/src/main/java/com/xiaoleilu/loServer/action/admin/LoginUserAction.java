/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package com.xiaoleilu.loServer.action.admin;

import java.util.Base64;
import java.util.concurrent.Executor;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.log.Logs;
import cn.wildfirechat.pojos.InputGetToken;
import cn.wildfirechat.pojos.InputOutputUserInfo;
import cn.wildfirechat.pojos.OutputGetIMTokenData;
import cn.wildfirechat.proto.ProtoConstants.UserType;
import cn.wildfirechat.proto.WFCMessage;
import io.moquette.persistence.RPCCenter;
import io.moquette.persistence.TargetEntry;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import win.liyufan.im.IMTopic;

import com.google.gson.Gson;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

/**
 * 用户登录操作
 *  1. 如果用户不存在则创建用户
 *  2. 加入世界频道
 *  3. 加入区域频道
 *  4. 生成客户端登录Im-server token
 *
 */
@Route(APIPath.Login_User)
@HttpMethod("POST")
public class LoginUserAction extends AdminAction {

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
            if (gameUser == null) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("LoginUserAction#action game user null error");

                return true;
            }

            String userId = gameUser.getUserId();
            if (StringUtil.isNullOrEmpty(gameUser.getUserId())) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("LoginUserAction#action game user id:{} error", userId);
                return true;
            }
            // 客户端id
            String clientId = gameUser.getClientId();
            if (StringUtil.isNullOrEmpty(clientId)) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("LoginUserAction#action game user clientId:{} error", clientId);
                return true;
            }

            // 区服id
            String section = gameUser.getSection();
            if (StringUtil.isNullOrEmpty(section)) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("LoginUserAction#action game user section:{} error", section);
                return true;
            }

            String arenaId = gameUser.getArenaId();
            if (StringUtil.isNullOrEmpty(arenaId)) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("LoginUserAction#action game user arenaId:{} error", arenaId);
                return true;
            }

            logger.info("LoginUserAction#action section:{} user:{} client:{} login...", section,
                userId, clientId);

            WFCMessage.User user = messagesStore.getUserInfo(userId);
            if (user == null) {
                try {
                    // 1 创建用户
                    createUser(gameUser);
                    logger.error("LoginUserAction#createUser user:{} ok", userId);
                } catch (Exception e) {
                    response.setStatus(HttpResponseStatus.OK);
                    RestResult result = RestResult
                        .resultOf(ErrorCode.ERROR_CODE_SERVER_ERROR, e.getMessage());
                    response.setContent(new Gson().toJson(result));

                    logger.error("LoginUserAction#createUser user:{} error", userId, e);

                    return true;
                }
            }

            // 4. 获取token
            return getToken(userId, clientId, section, response);
        }

        logger.warn("LoginUserAction#action login user fail");

        return true;
    }

    /**
     * 如果用户不存在，则创建用户
     *
     * db保证创建一个
     *
     * @param inputCreateUser 用户
     */
    private void createUser(InputOutputUserInfo inputCreateUser)
        throws Exception {
        WFCMessage.User.Builder newUserBuilder = WFCMessage.User.newBuilder()
            .setUid(inputCreateUser.getUserId());

        // 名称
        if (inputCreateUser.getName() != null) {
            newUserBuilder.setName(inputCreateUser.getName());
        }
        if (inputCreateUser.getDisplayName() != null) {
            newUserBuilder.setDisplayName(
                StringUtil.isNullOrEmpty(inputCreateUser.getDisplayName()) ? inputCreateUser
                    .getName() : inputCreateUser.getDisplayName());
        }
        // 头像
        if (inputCreateUser.getPortrait() != null) {
            newUserBuilder.setPortrait(inputCreateUser.getPortrait());
        }

        // 区服
        newUserBuilder.setAddress(inputCreateUser.getSection());
        newUserBuilder.setCompany(inputCreateUser.getArenaId());

        // 类型
        newUserBuilder.setType(UserType.UserType_Normal);
        // 时间
        newUserBuilder.setUpdateDt(System.currentTimeMillis());

        messagesStore.addUserInfo(newUserBuilder.build(), "");
    }


    /**
     * 异步请求返回用户登录token
     * @param userId 用户user id
     * @param clientId 用户登录客户端id
     * @param response 响应
     * @return
     */
    private boolean getToken(String userId, String clientId, String section, Response response) {
        InputGetToken token = new InputGetToken(userId, clientId, section);
        String data = new Gson().toJson(token);

        RPCCenter.getInstance()
            .sendRequest(userId, clientId, "", IMTopic.GetTokenTopic, data.getBytes(),
                userId, TargetEntry.Type.TARGET_TYPE_USER, new RPCCenter.Callback() {
                    @Override
                    public void onSuccess(byte[] result) {
                        ErrorCode errorCode1 = ErrorCode.fromCode(result[0]);
                        if (errorCode1 == ErrorCode.ERROR_CODE_SUCCESS) {
                            //ba errorcode qudiao
                            byte[] data = new byte[result.length - 1];
                            for (int i = 0; i < data.length; i++) {
                                data[i] = result[i + 1];
                            }
                            String token = Base64.getEncoder().encodeToString(data);

                            sendResponse(response, null, new OutputGetIMTokenData(userId, token));
                        } else {
                            sendResponse(response, errorCode1, null);
                        }
                    }

                    @Override
                    public void onError(ErrorCode errorCode) {
                        sendResponse(response, errorCode, null);

                    }

                    @Override
                    public void onTimeout() {
                        sendResponse(response, ErrorCode.ERROR_CODE_TIMEOUT, null);
                    }

                    @Override
                    public Executor getResponseExecutor() {
                        return command -> {
                            ctx.executor().execute(command);
                        };
                    }
                }, true);
        return false;
    }
}
