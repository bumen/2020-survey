/*
 * Copyright (c) 2012-2017 The original author or authors
 * ------------------------------------------------------
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 * The Eclipse Public License is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * The Apache License v2.0 is available at
 * http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.moquette.spi.impl;

import io.moquette.server.ConnectionDescriptorStore;
import io.moquette.spi.ClientSession;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class PersistentQueueMessageSender {

    private static final Logger LOG = LoggerFactory.getLogger(PersistentQueueMessageSender.class);
    private final ConnectionDescriptorStore connectionDescriptorStore;

    PersistentQueueMessageSender(ConnectionDescriptorStore connectionDescriptorStore) {
        this.connectionDescriptorStore = connectionDescriptorStore;
    }

    /**
     * 不使用推送服务
     * @param sender
     * @param conversationType
     * @param target
     * @param line
     * @param messageHead
     * @param deviceId
     * @param pushContent
     * @param messageContentType
     * @param serverTime
     * @param senderName
     * @param targetName
     * @param unReceivedMsg
     * @param mentionType
     * @param isHiddenDetail
     * @param language
     */
    @Deprecated
    void sendPush(String sender, int conversationType, String target, int line, long messageHead, String deviceId, String pushContent, int messageContentType, long serverTime, String senderName, String targetName, int unReceivedMsg, int mentionType, boolean isHiddenDetail, String language) {
        LOG.info("Send push to {}, message from {}", deviceId, sender);
        // PushMessage pushMessage = new PushMessage(sender, conversationType, target, line, messageContentType, serverTime, senderName, targetName, unReceivedMsg, mentionType, isHiddenDetail, language);
        // pushMessage.pushContent = pushContent;
        // PushServer.getServer().pushMessage(pushMessage, deviceId, pushContent);
    }

    /**
     * 不使用推送服务
     * @param sender
     * @param target
     * @param deviceId
     * @param pushContent
     * @param pushContentType
     * @param serverTime
     * @param senderName
     * @param unReceivedMsg
     * @param language
     */
    @Deprecated
    void sendPush(String sender, String target, String deviceId, String pushContent, int pushContentType, long serverTime, String senderName, int unReceivedMsg, String language) {
        LOG.info("Send push to {}, message from {}", deviceId, sender);
        // PushMessage pushMessage = new PushMessage(sender, target, serverTime, senderName, unReceivedMsg, language, pushContentType);
        // pushMessage.pushContent = pushContent;
        // PushServer.getServer().pushMessage(pushMessage, deviceId, pushContent);
    }

    boolean sendPublish(ClientSession clientsession, MqttPublishMessage pubMessage) {
        String clientId = clientsession.clientID;
        return sendPublish(clientId, pubMessage);
    }

    boolean sendPublish(String clientId, MqttPublishMessage pubMessage) {
        final int messageId = pubMessage.variableHeader().packetId();
        final String topicName = pubMessage.variableHeader().topicName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending PUBLISH message. MessageId={}, CId={}, topic={}, qos={}, payload={}", messageId,
                clientId, topicName, DebugUtils.payload2Str(pubMessage.payload()));
        } else {
            LOG.info("Sending PUBLISH message. MessageId={}, CId={}, topic={}", messageId, clientId, topicName);
        }

        boolean messageDelivered = connectionDescriptorStore.sendMessage(pubMessage, messageId, clientId, null);

        if(!messageDelivered) {
            LOG.warn("PUBLISH message could not be delivered.  MessageId={}, CId={}, topic={}", messageId, clientId, topicName);
        }

        return messageDelivered;
    }
}
