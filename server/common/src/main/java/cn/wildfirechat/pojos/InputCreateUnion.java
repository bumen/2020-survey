/*
 * This file is part of the Wildfire Chat package.
 * (c) Heavyrain2012 <heavyrain.lee@gmail.com>
 *
 * For the full copyright and license information, please view the LICENSE
 * file that was distributed with this source code.
 */

package cn.wildfirechat.pojos;


import cn.wildfirechat.proto.WFCMessage;

import com.playcrab.util.StringUtils;

public class InputCreateUnion extends InputGroupBase {
    private PojoUnionInfo union;

    public boolean isValide() {
        return true;
    }

    public WFCMessage.CreateGroupRequest toProtoGroupRequest() {
        WFCMessage.Group.Builder groupBuilder = WFCMessage.Group.newBuilder();
        WFCMessage.GroupInfo.Builder groupInfoBuilder = WFCMessage.GroupInfo.newBuilder();
        if (!StringUtils.isNullOrEmpty(union.unionId)) {
            groupInfoBuilder.setTargetId(union.unionId);
        }

        if (!StringUtils.isNullOrEmpty(union.name)) {
            groupInfoBuilder.setName(union.getName());
        }

        if (!StringUtils.isNullOrEmpty(union.portrait)) {
            groupInfoBuilder.setPortrait(union.getPortrait());
        }
        if (!StringUtils.isNullOrEmpty(union.owner)) {
            groupInfoBuilder.setOwner(union.getOwner());
        }

        groupInfoBuilder.setType(0);

        groupBuilder.setGroupInfo(groupInfoBuilder);

        WFCMessage.GroupMember.Builder groupMemberBuilder = WFCMessage.GroupMember.newBuilder().setMemberId(groupInfoBuilder.getOwner());
        groupMemberBuilder.setType(0);
        groupBuilder.addMembers(groupMemberBuilder);

        WFCMessage.CreateGroupRequest.Builder createGroupReqBuilder = WFCMessage.CreateGroupRequest.newBuilder();
        createGroupReqBuilder.setGroup(groupBuilder);

        return createGroupReqBuilder.build();
    }

    public PojoUnionInfo getGroup() {
        return union;
    }

    public void setGroup(PojoUnionInfo union) {
        this.union = union;
    }
}
