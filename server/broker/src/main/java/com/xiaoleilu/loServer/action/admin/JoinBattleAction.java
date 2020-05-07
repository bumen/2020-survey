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
import cn.wildfirechat.pojos.InputAddGroupMember;
import io.moquette.service.BattleMessageService;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.hazelcast.util.StringUtil;
import com.xiaoleilu.loServer.RestResult;
import com.xiaoleilu.loServer.annotation.HttpMethod;
import com.xiaoleilu.loServer.annotation.Route;
import com.xiaoleilu.loServer.handler.Request;
import com.xiaoleilu.loServer.handler.Response;

@Route(APIPath.Battle_Member_Join)
@HttpMethod("POST")
public class JoinBattleAction extends AdminAction {

    private static final Logger logger = Logs.HTTP;

    @Override
    public boolean isTransactionAction() {
        return true;
    }

    @Override
    public boolean action(Request request, Response response) {
        if (request.getNettyRequest() instanceof FullHttpRequest) {
            InputAddGroupMember gameUser = getRequestBody(request.getNettyRequest(),
                InputAddGroupMember.class);

            if (gameUser == null) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("JoinBattleAction#action game user null error");
                return true;
            }

            String userId = gameUser.getOperator();
            String battleId = gameUser.getGroup_id();
            if (StringUtil.isNullOrEmpty(userId) || StringUtil.isNullOrEmpty(battleId)) {
                response.setStatus(HttpResponseStatus.OK);
                RestResult result = RestResult.resultOf(ErrorCode.INVALID_PARAMETER);
                response.setContent(new Gson().toJson(result));

                logger.warn("JoinBattleAction#action game user id error");
                return true;
            }

            BattleMessageService.INSTANCE.joinBattle(userId, battleId);

            response.setStatus(HttpResponseStatus.OK);
            response.setContent(RestResult.OK_JSON);

            logger.info("JoinBattleAction#action userId:{} battle:{} ok", userId, battleId);
            return true;
        }

        logger.warn("JoinBattleAction#action type fail");

        return true;
    }


}
