package cn.wildfirechat.sdk;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.ImAdminHttpUtils;

public class ImUserAdmin {

    /**
     * 玩家获取登录im 认证token
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     *
     * @param user 玩家数据
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<OutputGetIMTokenData> login(LoginUserInfo user) throws Exception {
        String path = APIPath.Login_User;
        return ImAdminHttpUtils.httpJsonPost(path, user, OutputGetIMTokenData.class);
    }

    /**
     * 修改玩家信息
     * @param user 玩家数据
     * @return 成功失败
     * @throws Exception 连接异步
     */
    public static IMResult<Void> updateUser(UpdateUserInfo user) throws Exception {
        String path = APIPath.Modify_User;

        return ImAdminHttpUtils.httpJsonPost(path, user, Void.class);
    }

    /**
     * 玩家进入战场
     *
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     *
     * @param userId 玩家id
     * @param battleId 战场id
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<Void> joinBattle(String userId, String battleId) throws Exception {
        String path = APIPath.Battle_Member_Join;
        InputAddGroupMember addGroupMember = new InputAddGroupMember();
        addGroupMember.setGroup_id(battleId);
        addGroupMember.setOperator(userId);
        return ImAdminHttpUtils.httpJsonPost(path, addGroupMember, Void.class);
    }

    /**
     * 玩家退出战场
     * 正常返回：IMResult.code == ErrorCode.ERROR_CODE_SUCCESS
     * 失败返回：IMResult.code != ErrorCode.ERROR_CODE_SUCCESS
     * @param userId 玩家id
     * @return
     * @throws Exception 连接异常
     */
    public static IMResult<Void> quitBattle(String userId) throws Exception {
        String path = APIPath.Battle_Member_Quit;
        InputQuitGroup quitGroup = new InputQuitGroup();
        quitGroup.setOperator(userId);
        return ImAdminHttpUtils.httpJsonPost(path, quitGroup, Void.class);
    }
}
