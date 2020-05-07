package cn.wildfirechat.sdk;

import java.util.List;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.BroadMessageData;
import cn.wildfirechat.pojos.BroadMessageResult;
import cn.wildfirechat.pojos.Conversation;
import cn.wildfirechat.pojos.MessagePayload;
import cn.wildfirechat.pojos.MultiMessageResult;
import cn.wildfirechat.pojos.MulticastMessageData;
import cn.wildfirechat.pojos.RecallMessageData;
import cn.wildfirechat.pojos.SendMessageData;
import cn.wildfirechat.pojos.SendMessageResult;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.ImAdminHttpUtils;

public class ImMessageAdmin {
    public static IMResult<SendMessageResult> sendMessage(String sender, String section, Conversation conversation, MessagePayload payload) throws Exception {
        String path = APIPath.Msg_Send;
        SendMessageData messageData = new SendMessageData();
        messageData.setSender(sender);
        messageData.setSection(section);
        messageData.setConv(conversation);
        messageData.setPayload(payload);
        return ImAdminHttpUtils.httpJsonPost(path, messageData, SendMessageResult.class);
    }

    public static IMResult<Void> recallMessage(String operator, long messageUid) throws Exception {
        String path = APIPath.Msg_Recall;
        RecallMessageData messageData = new RecallMessageData();
        messageData.setOperator(operator);
        messageData.setMessageUid(messageUid);
        return ImAdminHttpUtils.httpJsonPost(path, messageData, Void.class);
    }

    public static IMResult<BroadMessageResult> broadcastMessage(String sender, int line, MessagePayload payload) throws Exception {
        String path = APIPath.Msg_Broadcast;
        BroadMessageData messageData = new BroadMessageData();
        messageData.setSender(sender);
        messageData.setLine(line);
        messageData.setPayload(payload);
        return ImAdminHttpUtils.httpJsonPost(path, messageData, BroadMessageResult.class);
    }

    public static IMResult<MultiMessageResult> multicastMessage(String sender, List<String> receivers, int line, MessagePayload payload) throws Exception {
        String path = APIPath.Msg_Multicast;
        MulticastMessageData messageData = new MulticastMessageData();
        messageData.setSender(sender);
        messageData.setTargets(receivers);
        messageData.setLine(line);
        messageData.setPayload(payload);
        return ImAdminHttpUtils.httpJsonPost(path, messageData, MultiMessageResult.class);
    }
}
