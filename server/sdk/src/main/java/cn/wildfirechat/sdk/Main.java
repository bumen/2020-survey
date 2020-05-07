package cn.wildfirechat.sdk;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.wildfirechat.common.ErrorCode;
import cn.wildfirechat.pojos.BroadMessageResult;
import cn.wildfirechat.pojos.Conversation;
import cn.wildfirechat.pojos.LoginUserInfo;
import cn.wildfirechat.pojos.MessagePayload;
import cn.wildfirechat.pojos.MultiMessageResult;
import cn.wildfirechat.pojos.OutputCreateGroupResult;
import cn.wildfirechat.pojos.OutputGetIMTokenData;
import cn.wildfirechat.pojos.OutputGroupIds;
import cn.wildfirechat.pojos.OutputGroupMemberList;
import cn.wildfirechat.pojos.PojoGroupInfo;
import cn.wildfirechat.pojos.PojoGroupMember;
import cn.wildfirechat.pojos.SendMessageResult;
import cn.wildfirechat.pojos.UpdateUserInfo;
import cn.wildfirechat.proto.ProtoConstants;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.ImAdminHttpUtils;

import com.google.gson.Gson;

public class Main {
    public static void main(String[] args) throws Exception {
        //admin使用的是18080端口，超级管理接口，理论上不能对外开放端口，也不能让非内部服务知悉密钥。
        testAdmin();

        //Robot和Channel都是使用的80端口，第三方可以创建或者为第三方创建，第三方可以使用robot或者channel与IM系统进行对接。
        // testRobot();
        //testChannel();
    }


    static void testAdmin() throws Exception {
        //初始化服务API
        ImAdminHttpUtils.init("http://localhost:18080", "123456");

        testUser();
        // testUserRelation();
        // testGroup();
        // testChatroom();
        // testMessage();
        // testGeneralApi();

        // testUnion();

        //testArenaMessage();

        System.out.println("Congratulation, all admin test case passed!!!!!!!");
    }

    static void testWorldMessage() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setTarget("section1");
        conversation.setType(ProtoConstants.ConversationType.ConversationType_WORLD);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent("hello world");

        IMResult<SendMessageResult> resultSendMessage = ImMessageAdmin
            .sendMessage("user1", "10001", conversation, payload);
        if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("send message success");
        } else {
            System.out.println("send message failure");
            System.exit(-1);
        }
    }

    static void testArenaMessage() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setTarget("10001_001");
        conversation.setType(ProtoConstants.ConversationType.ConversationType_ARENA);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent("hello world arena");

        IMResult<SendMessageResult> resultSendMessage = ImMessageAdmin
            .sendMessage("user1", "10001", conversation, payload);
        if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("send message success");
        } else {
            System.out.println("send message failure");
            System.exit(-1);
        }

    }

    static void testBattleMessage() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setTarget("battle_1");
        conversation.setType(ProtoConstants.ConversationType.ConversationType_BATTLE);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent("hello world battle");

        IMResult<SendMessageResult> resultSendMessage = ImMessageAdmin
            .sendMessage("user1", "10001", conversation, payload);
        if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("send message success");
        } else {
            System.out.println("send message failure");
            System.exit(-1);
        }
    }

    //***********************************************
    //****  用户相关的API
    //***********************************************
    static void testUser() throws Exception {
        LoginUserInfo userInfo = new LoginUserInfo();
        userInfo.setUserId("userId1");
        userInfo.setName("user1");
        userInfo.setPortrait("p1");
        userInfo.setClientId("3333333");
        userInfo.setSection("10001");
        userInfo.setArenaId("10001_001");

        IMResult<OutputGetIMTokenData> resultCreateUser = ImUserAdmin.login(userInfo);
        if (resultCreateUser != null && resultCreateUser.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("Create user " +  new Gson().toJson(resultCreateUser) + " success");
        } else {
            System.out.println("Create user failure: " +  new Gson().toJson(resultCreateUser));
            System.exit(-1);
        }

        UpdateUserInfo updateUserInfo = new UpdateUserInfo();
        updateUserInfo.setUserId(userInfo.getUserId());
        updateUserInfo.setName(userInfo.getName());
        updateUserInfo.setPortrait("p2");
        updateUserInfo.setArenaId("10001_002");
        ImUserAdmin.updateUser(updateUserInfo);

        IMResult<Void> voidResult = ImUserAdmin.joinBattle(userInfo.getUserId(), "battle_1");
        if (voidResult != null && voidResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("join battle ok");
        } else {
            System.out.println("join battle fail");
        }

        voidResult = ImUserAdmin.quitBattle(userInfo.getUserId());

        if (voidResult != null && voidResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("quit battle ok");
        } else {
            System.out.println("quit battle fail");
        }
    }

    static void testUnion() throws Exception {
        PojoGroupInfo groupInfo = new PojoGroupInfo();
        groupInfo.setTarget_id("unionId2");
        groupInfo.setOwner("user1");
        groupInfo.setName("test_group");
        groupInfo.setExtra("hello extra");
        groupInfo.setPortrait("http://portrait");

        // IMResult<OutputCreateGroupResult> resultCreateGroup = ImUnionAdmin
        //     .createUnion("user1", groupInfo);
        // if (resultCreateGroup != null && resultCreateGroup.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
        //     System.out.println("create group success");
        // } else {
        //     System.out.println("create group failure");
        //     System.exit(-1);
        // }

        IMResult<Void> r = ImUnionAdmin.addUnionMembers("user1", "unionId2", "user2");
        if (r != null && r.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("union add ok");
        } else {
            System.out.println("union add fail");
        }

        r = ImUnionAdmin.quitUnion("user2", "unionId2");
        if (r != null && r.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("union quit ok");
        } else {
            System.out.println("union quit fail");
        }

        r = ImUnionAdmin.dismissUnion("user1", "unionId2");
        if (r != null && r.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("union dismiss ok");
        } else {
            System.out.println("union dismiss fail");
        }
    }

    //***********************************************
    //****  用户关系相关的API
    //***********************************************

    //***********************************************
    //****  群组相关功能
    //***********************************************
    static void testGroup() throws Exception {

        IMResult<Void> voidIMResult1 = ImGroupAdmin.dismissGroup("user1", "groupId1", null, null);

        PojoGroupInfo groupInfo = new PojoGroupInfo();
        groupInfo.setTarget_id("groupId1");
        groupInfo.setOwner("user1");
        groupInfo.setName("test_group");
        groupInfo.setExtra("hello extra");
        groupInfo.setPortrait("http://portrait");
        List<PojoGroupMember> members = new ArrayList<>();
        PojoGroupMember member1 = new PojoGroupMember();
        member1.setMember_id(groupInfo.getOwner());
        members.add(member1);

        PojoGroupMember member2 = new PojoGroupMember();
        member2.setMember_id("user2");
        members.add(member2);

        PojoGroupMember member3 = new PojoGroupMember();
        member3.setMember_id("user3");
        members.add(member3);

        IMResult<OutputCreateGroupResult> resultCreateGroup = ImGroupAdmin
            .createGroup(groupInfo.getOwner(), groupInfo, members, null, null);
        if (resultCreateGroup != null && resultCreateGroup.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("create group success");
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }

        IMResult<PojoGroupInfo> resultGetGroupInfo = ImGroupAdmin.getGroupInfo(groupInfo.getTarget_id());
        if (resultGetGroupInfo != null && resultGetGroupInfo.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            if (groupInfo.getExtra().equals(resultGetGroupInfo.getResult().getExtra())
                && groupInfo.getName().equals(resultGetGroupInfo.getResult().getName())
                && groupInfo.getOwner().equals(resultGetGroupInfo.getResult().getOwner())) {
                System.out.println("get group success");
            } else {
                System.out.println("group info is not expected");
                System.exit(-1);
            }
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }

        IMResult<Void> voidIMResult = ImGroupAdmin
            .transferGroup(groupInfo.getOwner(), groupInfo.getTarget_id(), "user2", null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("transfer success");
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }

        voidIMResult = ImGroupAdmin
            .modifyGroupInfo(groupInfo.getOwner(), groupInfo.getTarget_id(), ProtoConstants.ModifyGroupInfoType.Modify_Group_Name,"HelloWorld", null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("transfer success");
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }


        resultGetGroupInfo = ImGroupAdmin.getGroupInfo(groupInfo.getTarget_id());
        if (resultGetGroupInfo != null && resultGetGroupInfo.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            if ("user2".equals(resultGetGroupInfo.getResult().getOwner())) {
                groupInfo.setOwner("user2");
            } else {
                System.out.println("group info is not expected");
                System.exit(-1);
            }
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }

        IMResult<OutputGroupMemberList> resultGetMembers = ImGroupAdmin.getGroupMembers(groupInfo.getTarget_id());
        if (resultGetMembers != null && resultGetMembers.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("get group member success");
        } else {
            System.out.println("create group failure");
            System.exit(-1);
        }

        voidIMResult = ImGroupAdmin
            .addGroupMembers("user1", groupInfo.getTarget_id(), Arrays.asList("use4", "user5"), null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("add group member success");
        } else {
            System.out.println("add group member failure");
            System.exit(-1);
        }

        voidIMResult = ImGroupAdmin
            .kickoffGroupMembers("user1", groupInfo.getTarget_id(), Arrays.asList("user3"), null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("kickoff group member success");
        } else {
            System.out.println("kickoff group member failure");
            System.exit(-1);
        }

        voidIMResult = ImGroupAdmin
            .setGroupManager("user1", groupInfo.getTarget_id(), Arrays.asList("user4", "user5"), true, null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("set group manager success");
        } else {
            System.out.println("set group manager failure");
            System.exit(-1);
        }

        voidIMResult = ImGroupAdmin
            .setGroupManager("user1", groupInfo.getTarget_id(), Arrays.asList("user4", "user5"), false, null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("cancel group manager success");
        } else {
            System.out.println("cancel group manager failure");
            System.exit(-1);
        }


        voidIMResult = ImGroupAdmin.quitGroup("user4", groupInfo.getTarget_id(), null, null);
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("quit group success");
        } else {
            System.out.println("quit group failure");
            System.exit(-1);
        }

        IMResult<OutputGroupIds> groupIdsIMResult = ImGroupAdmin.getUserGroups("user1");
        if (groupIdsIMResult != null && groupIdsIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            if (groupIdsIMResult.getResult().getGroupIds().contains(groupInfo.getTarget_id())) {
                System.out.println("get user groups success");
            } else {
                System.out.println("get user groups failure");
                System.exit(-1);
            }
        } else {
            System.out.println("get user groups failure");
            System.exit(-1);
        }

    }

    //***********************************************
    //****  消息相关功能
    //***********************************************
    static void testMessage() throws Exception {
        Conversation conversation = new Conversation();
        conversation.setTarget("section1");
        conversation.setType(ProtoConstants.ConversationType.ConversationType_WORLD);
        MessagePayload payload = new MessagePayload();
        payload.setType(1);
        payload.setSearchableContent("hello world");

        IMResult<SendMessageResult> resultSendMessage = ImMessageAdmin
            .sendMessage("user1", "", conversation, payload);
        if (resultSendMessage != null && resultSendMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("send message success");
        } else {
            System.out.println("send message failure");
            System.exit(-1);
        }


        IMResult<Void> voidIMResult = ImMessageAdmin
            .recallMessage("user1", resultSendMessage.getResult().getMessageUid());
        if (voidIMResult != null && voidIMResult.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("recall message success");
        } else {
            System.out.println("recall message failure");
            System.exit(-1);
        }


        IMResult<BroadMessageResult> resultBroadcastMessage = ImMessageAdmin
            .broadcastMessage("user1", 0, payload);
        if (resultBroadcastMessage != null && resultBroadcastMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("broad message success, send message to " + resultBroadcastMessage.getResult().getCount() + " users");
        } else {
            System.out.println("broad message failure");
            System.exit(-1);
        }

        List<String> multicastReceivers = Arrays.asList("user2", "user3", "user4");
        IMResult<MultiMessageResult> resultMulticastMessage = ImMessageAdmin
            .multicastMessage("user1", multicastReceivers, 0, payload);
        if (resultMulticastMessage != null && resultMulticastMessage.getErrorCode() == ErrorCode.ERROR_CODE_SUCCESS) {
            System.out.println("multi message success, messageid is " + resultMulticastMessage.getResult().getMessageUid());
        } else {
            System.out.println("multi message failure");
            System.exit(-1);
        }
    }



}
