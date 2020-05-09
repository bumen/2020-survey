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

public class InputCreateChannel {
    private String owner;
    private String name;
    private String targetId;
    private String callback;
    private String portrait;
    private int auto;
    private String secret;
    private String desc;
    private int state;
    private String extra;
    private long updateDt;

    public WFCMessage.ChannelInfo toProtoChannelInfo() {
        WFCMessage.ChannelInfo.Builder builder = WFCMessage.ChannelInfo.newBuilder().setOwner(owner);
        if (!StringUtils.isNullOrEmpty(name))
            builder = builder.setName(name);
        if (!StringUtils.isNullOrEmpty(targetId))
            builder = builder.setTargetId(targetId);
        if (!StringUtils.isNullOrEmpty(callback))
            builder = builder.setCallback(callback);
        if (!StringUtils.isNullOrEmpty(portrait))
            builder = builder.setPortrait(portrait);
        builder = builder.setAutomatic(auto);
        if (!StringUtils.isNullOrEmpty(secret))
            builder = builder.setSecret(secret);
        if (!StringUtils.isNullOrEmpty(desc))
            builder = builder.setDesc(desc);
        builder = builder.setStatus(state);
        if (!StringUtils.isNullOrEmpty(extra))
            builder = builder.setExtra(extra);
        if (!StringUtils.isNullOrEmpty(name))
            builder = builder.setUpdateDt(updateDt);
        else
            builder = builder.setUpdateDt(System.currentTimeMillis());

        return builder.build();
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getCallback() {
        return callback;
    }

    public void setCallback(String callback) {
        this.callback = callback;
    }

    public String getPortrait() {
        return portrait;
    }

    public void setPortrait(String portrait) {
        this.portrait = portrait;
    }

    public int getAuto() {
        return auto;
    }

    public void setAuto(int auto) {
        this.auto = auto;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public long getUpdateDt() {
        return updateDt;
    }

    public void setUpdateDt(long updateDt) {
        this.updateDt = updateDt;
    }
}
