package io.moquette.service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import cn.wildfirechat.log.Logs;
import cn.wildfirechat.proto.ProtoConstants.PullType;
import cn.wildfirechat.proto.WFCMessage;
import io.moquette.connections.IConnectionsManager;
import io.moquette.persistence.MemoryMessagesStore;
import io.moquette.persistence.MemorySessionStore;
import io.moquette.persistence.MemorySessionStore.Session;
import io.moquette.server.Server;
import io.moquette.spi.IMessagesStore;
import io.moquette.spi.ISessionsStore;
import io.moquette.spi.impl.MessagesPublisher;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttQoS;
import org.slf4j.Logger;
import win.liyufan.im.IMTopic;
import win.liyufan.im.MessageBundle;

import static io.moquette.server.Constants.MAX_MESSAGE_QUEUE;

import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.util.StringUtil;
import com.playcrab.thread.NamedThreadFactory;
import com.playcrab.util.TimeUtils;

/**
 *
 * 区域频道
 *
 * @date 2020-04-28
 * @author zhangyuqiang02@playcrab.com
 */
public enum ArenaMessageService {

    INSTANCE;

    private static final Logger logger = Logs.MQTT;

    private Server mServer;
    private IMessagesStore messagesStore;
    private MessagesPublisher publisher;
    private IConnectionsManager connectionDescriptors;

    private volatile boolean started = false;
    private ExecutorService executor;

    public void init(Server server) {
        this.mServer = server;
        this.connectionDescriptors = server.getConnectionsManager();
        this.publisher = server.getProcessor().getMessagesPublisher();
        messagesStore = server.getStore().messagesStore();

        started = true;

        executor = new ThreadPoolExecutor(1, 1, 0L,
            TimeUnit.SECONDS, new ArrayBlockingQueue<>(1), new NamedThreadFactory(
            "IM-arena-executor", true));

        executor.execute(() -> {
            this.doSend();
        });

        Logs.SERVER.info("ArenaMessageService#init ok");
    }

    public void shutdown() {
        this.started = false;
        executor.shutdownNow();

        Logs.SERVER.info("ArenaMessageService#shutodnw ok");
    }

    /**
     * <arenaId, <seqId, mid>>
     */
    private ConcurrentHashMap<String, ConcurrentSkipListMap<Long, Long>> arenaMessages = new ConcurrentHashMap<>();

    /**
     * <section, <arenaId, seqId>>
     */
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Long>> notifyArenaSeq = new ConcurrentHashMap<>(
        1);


    /**
     * 发送区域频道
     * @param userId 用户
     * @param clientID 用户客户端id
     * @param section 区服
     * @param message 消息
     */
    public void sendMessage(String userId, String clientID, String section,
        WFCMessage.Message message) {
        // 保存消息
        message = messagesStore.storeMessage(userId, clientID, message);

        String arenaId = message.getConversation().getTarget();

        // 保存顺序最近消息
        long seqId = insertArenaMessages(arenaId, message.getMessageId());

        // 集群通知
        notifyClusterMessageSeq(message.getMessageId(), seqId);

        ConcurrentHashMap<String, Long> v = notifyArenaSeq.get(section);
        if (v == null) {
            // 通知用户，保存最小的
            notifyArenaSeq.putIfAbsent(section, new ConcurrentHashMap<>());
            v = notifyArenaSeq.get(section);
        }

        v.put(arenaId, seqId);
        // AtomicLong s = v.get(arenaId);
        // if (s == null) {
        //     v.putIfAbsent(arenaId, new AtomicLong(0));
        //     s = v.get(arenaId);
        // }
        //
        // s.compareAndSet(0, seqId);

        logger.debug("ArenaMessageService#sendMessage user:{} section:{} seq:{} ok", userId,
            section, seqId);
    }

    /**
     * 定时推送通知
     */
    public void doSend() {
        // 最快1/2秒推送一次
        final int tick = 500;
        while (started) {
            try {
                Iterator<Entry<String, ConcurrentHashMap<String, Long>>> it = notifyArenaSeq
                    .entrySet().iterator();
                while (it.hasNext()) {
                    Entry<String, ConcurrentHashMap<String, Long>> entry = it.next();

                    // 一次通知一个区服的所有区域
                    notifyOnlineArenaUser(entry.getKey(), entry.getValue());
                }
            } catch (Exception e) {
                logger.error("ArenaMessageService#doSend error", e);
            }

            try {
                Thread.sleep(tick);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }

        }
    }

    private final AtomicLong maxSeq = new AtomicLong(0);

    public long insertArenaMessages(String arenaId, long messageId) {
        // messageId是全局的，messageSeq是跟个人相关的，理论上messageId的增长数度远远大于seq。
        // 考虑到一种情况，当服务器发生变化，用户发生迁移后，messageSeq还需要保持有序。 要么把Seq持久化，要么在迁移后Seq取一个肯定比以前更大的数字（这个数字就是messageId）
        // 这里选择使用后面一种情况
        ConcurrentSkipListMap<Long, Long> maps = getArenaMessages(arenaId);

        long messageSeq = 0;
        Long v = Long.MIN_VALUE;
        while (v != null) {
            messageSeq = nextSeqId(maps, messageId);
            v = maps.putIfAbsent(messageSeq, messageId);
        }

        logger.debug("ArenaMessageService#insertMessages arenaId:{} seq:{} ok",
            arenaId, messageSeq);

        if (maps.size() > MAX_MESSAGE_QUEUE) {
            maps.remove(maps.firstKey());
        }

        messagesStore.getDatabaseStore().persistArenaMessage(arenaId, messageId, messageSeq);
        return messageSeq;
    }

    private long nextSeqId(ConcurrentSkipListMap<Long, Long> maps, long messageId) {
        long messageSeq = 0;

        Map.Entry<Long, Long> lastEntry = maps.lastEntry();
        if (lastEntry != null) {
            messageSeq = (lastEntry.getKey() + 1);
        }
        long maxPullSeq = maxSeq.get();
        if (maxPullSeq > messageSeq) {
            messageSeq = maxPullSeq + 1;
        }

        if (messageSeq == 0) {
            messageSeq = messageId;
        }

        return messageSeq;
    }

    /**
     * 一次拉取最大消息量
     */
    public static final int PULL_MAX_SIZE = 512 * 1024;

    /**
     * 拉取世界频道消息
     * @param clientId 拉取消息的客户端
     * @param section 所在区服
     * @param fromSeqId 拉取的seqId
     * @return 返回消息列表
     */
    public WFCMessage.PullMessageResult fetchMessage(String userId, String clientId,
        String section, long fromSeqId) {
        WFCMessage.PullMessageResult.Builder builder = WFCMessage.PullMessageResult.newBuilder();

        // 刷新拉取最大消息号
        long maxPullSeq = maxSeq.get();
        while (fromSeqId > maxPullSeq) {
            if (maxSeq.compareAndSet(maxPullSeq, fromSeqId)) {
                break;
            }
            maxPullSeq = maxSeq.get();
        }

        // 刷新session
        MemorySessionStore.Session session = mServer.getStore().sessionsStore()
            .getSession(clientId);
        session.refreshLastActiveTime();

        WFCMessage.User user = messagesStore.getUserInfo(userId);

        long head = fromSeqId, current = fromSeqId;

        // 获取拉取数据
        ConcurrentSkipListMap<Long, Long> maps = getArenaMessages(user.getCompany());

        // 获取消息
        HazelcastInstance hzInstance = mServer.getHazelcastInstance();
        IMap<Long, MessageBundle> mIMap = hzInstance.getMap(MemoryMessagesStore.MESSAGES_MAP);

        // 默认可以拉取历史消息
        boolean noRoaming = false;
        // 每次最多拉取3M
        int size = 0;
        long curTime = TimeUtils.now();

        while (true) {
            Map.Entry<Long, Long> entry = maps.higherEntry(current);
            if (entry == null) {
                break;
            }
            current = entry.getKey();
            MessageBundle bundle = mIMap.get(entry.getValue());
            if (bundle != null) {
                // 历史消息
                if (noRoaming && (
                    curTime - bundle.getMessage().getServerTimestamp()
                        > 5 * 60 * 1000)) {
                    continue;
                }

                // 消息过期
                if (bundle.getMessage().getContent().getExpireDuration() > 0) {
                    if (curTime < bundle.getMessage().getServerTimestamp() + bundle.getMessage()
                        .getContent().getExpireDuration()) {
                        continue;
                    }
                }

                // 数据量 3M
                size += bundle.getMessage().getSerializedSize();
                if (size >= PULL_MAX_SIZE) {
                    if (builder.getMessageCount() == 0) {
                        builder.addMessage(bundle.getMessage());
                    }
                    break;
                }
                builder.addMessage(bundle.getMessage());
            }
        }

        // 最后一条，表示如果没拉完，需要继续拉取
        Map.Entry<Long, Long> lastEntry = maps.lastEntry();
        if (lastEntry != null) {
            head = lastEntry.getKey();
        }

        builder.setCurrent(current);
        builder.setHead(head);
        return builder.build();
    }


    /**
     * 通知所有在线玩家拉取世界频道消息
     * @param section 区服
     * @param seqMap 需要拉取的最大顺序号
     */
    public void notifyOnlineArenaUser(String section,
        ConcurrentHashMap<String, Long> seqMap) {
        Map<String, MqttPublishMessage> arenaMsg = new HashMap<>(seqMap.size());

        // 创建消息
        Iterator<Entry<String, Long>> it = seqMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, Long> entry = it.next();
            it.remove();

            WFCMessage.NotifyMessage notifyMessage = WFCMessage.NotifyMessage
                .newBuilder()
                .setType(PullType.Pull_Arena)
                .setHead(entry.getValue())
                .build();

            ByteBuf payload = Unpooled.buffer();
            byte[] byteData = notifyMessage.toByteArray();
            payload.ensureWritable(byteData.length).writeBytes(byteData);
            MqttPublishMessage publishMsg = MessagesPublisher
                .notRetainedPublish(IMTopic.NotifyMessageTopic, MqttQoS.AT_MOST_ONCE,
                    payload);

            arenaMsg.put(entry.getKey(), publishMsg);

            logger.debug("ArenaMessageService#notifyOnlineUser section:{} arena:{} seq:{} start...",
                section, entry.getKey(), entry.getValue());
        }

        if (arenaMsg.isEmpty()) {
            return;
        }

        // 全服发送
        ISessionsStore sessionsStore = mServer.getStore().sessionsStore();
        int c = 0;
        Set<String> onlineClientIds = connectionDescriptors.getSectionClients(section);
        if (onlineClientIds == null) {
            return;
        }
        for (String cid : onlineClientIds) {
            try {
                boolean targetIsActive = this.connectionDescriptors.isConnected(cid);
                if (targetIsActive) {
                    Session session = sessionsStore.getSession(cid);
                    if (session == null) {
                        continue;
                    }

                    WFCMessage.User user = messagesStore.getUserInfo(session.getUsername());
                    if (user == null) {
                        continue;
                    }

                    String userArena = user.getCompany();
                    if (StringUtil.isNullOrEmpty(userArena)) {
                        continue;
                    }

                    MqttPublishMessage publishMsg = arenaMsg.get(userArena);
                    if (publishMsg == null) {
                        continue;
                    }

                    boolean sent = this.publisher.sendPublish(cid, publishMsg);

                    logger.debug("ArenaMessageService#notifyOnlineUser section:{} cid:{} finish:{}",
                        section,
                        cid, sent);
                } else {
                    logger.debug("ArenaMessageService#notifyOnlineUser section:{} cid:{} no online",
                        section,
                        cid);
                }
            } catch (Exception e) {
                logger.error("ArenaMessageService#notifyOnlineUser section:{} cid:{} error",
                    section,
                    cid, e);
            }

            c++;
        }

        logger.debug("ArenaMessageService#notifyOnlineUser section:{} online user size:{}", section,
            c);
    }


    public ConcurrentSkipListMap<Long, Long> getArenaMessages(String arenaId) {
        ConcurrentSkipListMap<Long, Long> maps = arenaMessages.get(arenaId);
        if (maps == null) {
            initArenaMessages(arenaId);
        }
        return arenaMessages.get(arenaId);
    }

    /**
     * 加载某区域历史消息
     * @param arenaId 区域id
     */
    public void initArenaMessages(String arenaId) {
        ConcurrentSkipListMap<Long, Long> maps = arenaMessages.get(arenaId);
        if (maps == null) {
            synchronized (this) {
                maps = arenaMessages.get(arenaId);
                if (maps == null) {
                    maps = messagesStore.getDatabaseStore().reloadArenaMessageMaps(arenaId);
                    arenaMessages.put(arenaId, maps);
                }
            }
        }
    }

    /**
     *  TODO 集群开发时实现
     */
    public void notifyClusterMessageSeq(long mid, long seq) {

    }

}