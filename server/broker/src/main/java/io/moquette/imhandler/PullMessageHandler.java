/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package io.moquette.imhandler;

import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.proto.ProtoConstants.PullType;
import cn.wildfirechat.proto.WFCMessage;
import io.moquette.service.ArenaMessageService;
import io.moquette.service.BattleMessageService;
import io.moquette.service.WorldMessageService;
import io.moquette.spi.impl.Qos1PublishHandler;
import io.netty.buffer.ByteBuf;
import win.liyufan.im.IMTopic;

@Handler(value = IMTopic.PullMessageTopic)
public class PullMessageHandler extends IMHandler<WFCMessage.PullMessageRequest> {
    @Override
    public ErrorCode action(ByteBuf ackPayload, String clientID, String fromUser, String section, boolean isAdmin, WFCMessage.PullMessageRequest request, Qos1PublishHandler.IMCallback callback) {
        ErrorCode errorCode = ErrorCode.ERROR_CODE_SUCCESS;

        int pullType = request.getType();

        if (pullType == PullType.Pull_World) {
            WFCMessage.PullMessageResult result = WorldMessageService.INSTANCE.fetchMessage(clientID, section, request.getId());
            byte[] data = result.toByteArray();
            ackPayload.ensureWritable(data.length).writeBytes(data);

            LOG.info("PullMessageHandler#action world user:{} count:{} size:{}", fromUser, result.getMessageCount(), data.length);
        } else if(pullType == PullType.Pull_Arena) {
            WFCMessage.PullMessageResult result = ArenaMessageService.INSTANCE.fetchMessage(fromUser, clientID, section, request.getId());
            byte[] data = result.toByteArray();
            ackPayload.ensureWritable(data.length).writeBytes(data);

            LOG.info("PullMessageHandler#action arena user:{} count:{} size:{}", fromUser, result.getMessageCount(), data.length);
        }else if(pullType == PullType.Pull_Battle) {
            WFCMessage.PullMessageResult result = BattleMessageService.INSTANCE.fetchMessage(fromUser, clientID, section, request.getId());
            byte[] data = result.toByteArray();
            ackPayload.ensureWritable(data.length).writeBytes(data);

            LOG.info("PullMessageHandler#action battle user:{} count:{} size:{}", fromUser, result.getMessageCount(), data.length);
        } else if (pullType == ProtoConstants.PullType.Pull_ChatRoom && !m_messagesStore.checkUserClientInChatroom(fromUser, clientID, null)) {
            errorCode = ErrorCode.ERROR_CODE_NOT_IN_CHATROOM;
        } else {
            WFCMessage.PullMessageResult result = m_messagesStore.fetchMessage(fromUser, clientID, request.getId(), pullType);
            byte[] data = result.toByteArray();
            ackPayload.ensureWritable(data.length).writeBytes(data);

            LOG.info("PullMessageHandler#action user:{} count:{} size:{}", fromUser, result.getMessageCount(), data.length);
        }
        return errorCode;
    }
}
