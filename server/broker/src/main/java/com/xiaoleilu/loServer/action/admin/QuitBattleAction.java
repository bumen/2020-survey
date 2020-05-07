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
import cn.wildfirechat.pojos.InputQuitGroup;
import io.moquette.service.BattleMessageService;
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

@Route(APIPath.Battle_Member_Quit)
@HttpMethod("POST")
public class QuitBattleAction extends AdminAction {

    private static final Logger logger = Logs.HTTP;

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            InputQuitGroup gameUser = getRequestBody(request.getNettyRequest(),
                InputQuitGroup.class);

            if (gameUser == null) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("QuitBattleAction#action game user null error");

                return true;
            }

            String userId = gameUser.getOperator();
            if (StringUtil.isNullOrEmpty(userId)) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("QuitBattleAction#action game user id error");

                return true;
            }

            String battleId = BattleMessageService.INSTANCE.quitBattle(userId);

            response.setStatus(HttpResponseStatus.OK);
            response.setContent(RestResult.OK_JSON);

            logger.info("QuitBattleAction#action userId:{} battle:{} ok", userId, battleId);

            return true;
        }

        logger.warn("QuitBattleAction#action type fail");

        return true;
    }


}
