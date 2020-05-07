package cn.wildfirechat.sdk;

import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.InputAddGroupMember;
import cn.wildfirechat.pojos.InputCreateUnion;
import cn.wildfirechat.pojos.InputDismissGroup;
import cn.wildfirechat.pojos.InputQuitGroup;
import cn.wildfirechat.pojos.OutputCreateGroupResult;
import cn.wildfirechat.pojos.PojoGroupMember;
import cn.wildfirechat.pojos.PojoUnionInfo;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.ImAdminHttpUtils;

public class ImUnionAdmin {

    /**
     * 创建联盟频道
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     * @param userId 玩家
     * @param group_info 联盟信息
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<OutputCreateGroupResult> createUnion(String userId, PojoUnionInfo group_info) throws Exception {
        String path = APIPath.Union_Create;
        InputCreateUnion createGroup = new InputCreateUnion();
        createGroup.setGroup(group_info);
        createGroup.setOperator(userId);

        return ImAdminHttpUtils.httpJsonPost(path, createGroup, OutputCreateGroupResult.class);
    }

    /**
     * 解散联盟频道
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     * @param userId 玩家
     * @param groupId 联盟id
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<Void> dismissUnion(String userId, String groupId) throws Exception {
        String path = APIPath.Union_Dismiss;
        InputDismissGroup dismissGroup = new InputDismissGroup();
        dismissGroup.setOperator(userId);
        dismissGroup.setGroup_id(groupId);
        return ImAdminHttpUtils.httpJsonPost(path, dismissGroup, Void.class);
    }

    /**
     * 加入联盟频道
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     * @param userId 玩家
     * @param groupId 联盟id
     * @param memberId 联盟成员
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<Void> addUnionMembers(String userId, String groupId, String memberId) throws Exception {
        String path = APIPath.Union_Member_Add;
        InputAddGroupMember addGroupMember = new InputAddGroupMember();
        addGroupMember.setGroup_id(groupId);
        List<PojoGroupMember> members = new ArrayList<>();
        PojoGroupMember m = new PojoGroupMember();
        m.setMember_id(memberId);
        members.add(m);
        addGroupMember.setMembers(members);
        addGroupMember.setOperator(userId);
        return ImAdminHttpUtils.httpJsonPost(path, addGroupMember, Void.class);
    }
    /**
     * 退出联盟频道
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     * @param userId 玩家
     * @param groupId 联盟信息
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<Void> quitUnion(String userId, String groupId) throws Exception {
        String path = APIPath.Union_Member_Quit;
        InputQuitGroup quitGroup = new InputQuitGroup();
        quitGroup.setGroup_id(groupId);
        quitGroup.setOperator(userId);
        return ImAdminHttpUtils.httpJsonPost(path, quitGroup, Void.class);
    }

}
