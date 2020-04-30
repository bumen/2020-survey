package cn.wildfirechat.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @date 2020-01-04
 * @author zhangyuqiang02@playcrab.com
 */
public class Logs {
    public static final Logger SERVER = LoggerFactory.getLogger("SERVER");
    public static final Logger HTTP = LoggerFactory.getLogger("HTTP");
    public static final Logger MQTT = LoggerFactory.getLogger("MQTT");
}
