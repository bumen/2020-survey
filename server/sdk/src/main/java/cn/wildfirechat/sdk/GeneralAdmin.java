package cn.wildfirechat.sdk;

import cn.wildfirechat.common.APIPath;
import cn.wildfirechat.pojos.*;
import cn.wildfirechat.sdk.model.IMResult;
import cn.wildfirechat.sdk.utilities.ImAdminHttpUtils;

public class GeneralAdmin {
    public static IMResult<SystemSettingPojo> getSystemSetting(int id) throws Exception {
        String path = APIPath.Get_System_Setting;
        SystemSettingPojo input = new SystemSettingPojo();
        input.id = id;
        return ImAdminHttpUtils.httpJsonPost(path, input, SystemSettingPojo.class);
    }

    public static IMResult<Void> setSystemSetting(int id, String value, String desc) throws Exception {
        String path = APIPath.Put_System_Setting;
        SystemSettingPojo input = new SystemSettingPojo();
        input.id = id;
        input.value = value;
        input.desc = desc;
        return ImAdminHttpUtils.httpJsonPost(path, input, Void.class);
    }

    public static IMResult<OutputCreateChannel> createChannel(InputCreateChannel inputCreateChannel) throws Exception {
        String path = APIPath.Create_Channel;
        return ImAdminHttpUtils.httpJsonPost(path, inputCreateChannel, OutputCreateChannel.class);
    }

}
