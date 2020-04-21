## im 基于web-chat mac 开发部属
 * git给的文档真是垃圾，需要有强烈的大脑和有充足的时间来部属成功
 * 坑
   + nginx 配置
   + web-chat 配置
   + db2 没有web console
   + broker 与 web-chat ws通信不匹配
   + shiro 授权不成功
   
 * 功夫不负有心人
   + 再不需要手机客户端真实扫码实现web-chat登录
 
 * 需要的项目
   + im-app_server
   + server/broker
   + web-chat
   + 三个项目结合来实现im功能
   

### web-chat
 * web-chat是基于nodejs开发，所以需要安装nodejs
 * brew install node
   + 安装 nodejs
 * node -v 或npm -v 
   + nodejs 版本
  
 * 进行web-chat项目目录 
   + npm install 安装依赖
   + npm run dev 启动 nodejs dev 环境
   
 * 访问web-chat
   + http://localhost:3000
   + 此时会进入扫码界面
     - 代码js: web-chat/src/js/ui/pages/Login/index.js
     - 通过浏览器debug可以查到访问到哪个js
 * 此时部署成功，接下来配置web-chat
 * 打开src/js/config.js
   + USE_WSS 配置false
   
 * 修改web-chat/src/js/ui/pages/Login/index.js
   + 添加mobileLogin, scanPc, confirmPc 功能
      - 通过发送请求模似手机应用端扫码登录操作
 
 * web登录过程
   + 通过手机端应用扫码登录
     - 手机端登录通过接收短信码登录，由im-app_server处理
     - 此处我们没需要手机安装应用，直接通过web模拟手机应用请求登录
     - 扫码登录分两步
        1. 扫码：拿到二维码（由web生成的），更新web session状态为1
        2. 确认登录：拿到二维码，更新web session状态为2，
        web端定时轮询查看web session状态，如果为2则表示客户端允许web登录
  
   + web登录
     1. 如果未登录先生成登录二维码，等客户端扫码登录
     2. web轮询查看web session状态为2，则允许web登录，否则一直轮询
     3. 去im server 生成连接token，存cache
     4. web client 拿到userId, 和im server token 会路由到某台im server上
     5. webscoket 连接到指定im server上
     
 * nginx配置
   + 需要配置nginx实现js跨域访问和im server负载均衡
   + web.conf 自带配置基本上修改
     - upstream imserver_cluster
     ``` 
        # 配置im服务地址
        upstream imserver_cluster  {
            server 127.0.0.1:9000;
        }
     ```
     - listen 8080;  
     > 通过 80端转发到8080
     - server_name wildfirechat.cn web.wildfirechat.cn;
     > 配置wildfirechat.cn
     - location /route
     ``` 
        location /route{
                proxy_set_header  Host  $host;
                proxy_set_header  X-real-ip $remote_addr;
                proxy_set_header  X-Forwarded-For $proxy_add_x_forwarded_for;
                proxy_pass   http://imserver_cluster;
            }
     ```
     
 * mac 80端口转发
   + 需要做的是端口转发，也就是将监听的80端口转发到8080。
   + 通过域名访问80端口后，直接转发到8080, 然后nginx配置监听8080就可以
   1.创建idea.tomcat.forwarding文件：  
   `sudo vim /etc/pf.anchors/idea.tomcat.forwarding`
   2. 添加转发配置  
   `rdr pass on lo0 inet proto tcp from any to 127.0.0.1 port 80 -> 127.0.0.1 port 8080 `
   3. 创建pf-tomcat.conf文件  
   `sudo vim /etc/pf-tomcat.conf `
   4. 添加配置  
   ``` 
    rdr-anchor "forwarding"
    load anchor "forwarding" from "/etc/pf.anchors/idea.tomcat.forwarding"
   ```
   5. 启动端口转发功能  
   `sudo pfctl -ef /etc/pf-tomcat.conf`
   ```  
        // 启动成功
        pfctl: Use of -f option, could result in flushing of rules
        present in the main ruleset added by the system at startup.
        See /etc/pf.conf for further details.
        
        No ALTQ support in kernel
        ALTQ related functions disabled
        pfctl: pf already enabled
   ```
   6. 关闭端口转发功能
   `sudo pfctl -d `
     + 关闭所有
     `pfctl -F all -f /etc/pf.conf `
  
   
 * mac 域名解析
   + wildfirechat.cn
   + web.wildfirechat.cn
   + www.wildfirechat.cn
   + 配置/etc/hosts
      - sudo vi /etc/hosts
      ``` 
        127.0.0.1 www.wildfirechat.cn
        127.0.0.1 web.wildfirechat.cn
        127.0.0.1 app.wildfirechat.cn
        127.0.0.1 wildfirechat.cn
      ```
     
 * 服务端主要使用它来调试程序
     
### im-app_server
 * 是一个im客户端登录授权一个服务器
 * 通过 http请求访问im server 来提供一些登录认证，创建账号
 * 提提供接口
   + 发送应用登录短信码，（调试用不到）
   + 客户端登录，（在web界面模拟请求，所以不需要安装客户端应用）
   + 生成二维码，（web端首次需要登录认证授权，通过二维码实现）
   + 二维码登录，（web端轮询检查客户端授权是否成功，成功后则走登录im-server流程）
   + 客户端扫码，（在web界面模拟请求，所以不需要安装客户端应用）
   + 二维码确认，（在web界面模拟请求，所以不需要安装客户端应用，确认后web请求的二维码登录接口继续走登录im-server流程）
   
 * 是一个spring boot项目
   + 功能不复杂
   + 使用了shiro安全认证授权框架
   
 * 服务端主要使用它来调式web通信

 
### im server即broker
 * 启动参数
   + 参考distribution项目中的wildfirechat.sh 启动脚本
 ``` 
    -Dwildfirechat.path=/Users/playcrab/data/project/bmn/survey/server/broker/
    -Dlog4j.configuration=file:/Users/playcrab/data/project/bmn/survey/server/broker/config/log4j.properties
    -Dhazelcast.configuration=file:/Users/playcrab/data/project/bmn/survey/server/broker/config/hazelcast.xml
 ```
 * 从distribution项目中拷贝一个wildfirechat.conf配置到 broker/config目录
   + 因为开发时找的broker/config下的配置
 * 修改配置
   + server.ip 改为本机ip
   + host  改为本机ip
   + local_port 改为9000, 同时nginx配置也需要改为9000
     > 原始配置是80，如果机器没有对80有限制，则可以不改。一定要与nginx配置一致
     > 这个一个内部辅助类功能的接口
     
 * 修改NettyAcceptor
   + server/broker/src/main/java/io/moquette/server/netty/NettyAcceptor.java
   + 默认没有启动webscoket 连接处理，所以需要打开
      - 这个im 通信端口
   + 添加一个方法
   ``` 
    private void initializeWSSTransport(final NettyMQTTHandler handler, IConfig props)
            throws IOException {
            LOG.info("Configuring secure websocket MQTT transport");
            String sslPortProp = props.getProperty(WEB_SOCKET_PORT_PROPERTY_NAME, DISABLED_PORT_BIND);
            if (DISABLED_PORT_BIND.equals(sslPortProp)) {
                // Do nothing no SSL configured
                LOG.info("Property {} has been set to {}. Secure websocket MQTT will be disabled",
                    BrokerConstants.WEB_SOCKET_PORT_PROPERTY_NAME, DISABLED_PORT_BIND);
                return;
            }
            int sslPort = Integer.parseInt(sslPortProp);
            final MoquetteIdleTimeoutHandler timeoutHandler = new MoquetteIdleTimeoutHandler();
            String host = props.getProperty(BrokerConstants.HOST_PROPERTY_NAME);
            initFactory(host, sslPort, "No Secure websocket", new PipelineInitializer() {
    
                @Override
                void init(ChannelPipeline pipeline) throws Exception {
                    pipeline.addLast("httpEncoder", new HttpResponseEncoder());
                    pipeline.addLast("httpDecoder", new HttpRequestDecoder());
                    pipeline.addLast("aggregator", new HttpObjectAggregator(65536));
                    pipeline.addLast("webSocketHandler",
                        new WebSocketServerProtocolHandler("/", MQTT_SUBPROTOCOL_CSV_LIST));
                    pipeline.addLast("ws2bytebufDecoder", new WebSocketFrameToByteBufDecoder());
                    pipeline.addLast("bytebuf2wsEncoder", new ByteBufToWebSocketFrameEncoder());
                    pipeline.addFirst("idleStateHandler", new IdleStateHandler(nettyChannelTimeoutSeconds, 0, 0));
                    pipeline.addAfter("idleStateHandler", "idleEventHandler", timeoutHandler);
                    pipeline.addFirst("bytemetrics", new BytesMetricsHandler(m_bytesMetricsCollector));
                    pipeline.addLast("decoder", new MqttDecoder());
                    pipeline.addLast("encoder", MqttEncoder.INSTANCE);
                    pipeline.addLast("metrics", new MessageMetricsHandler(m_metricsCollector));
                    pipeline.addLast("messageLogger", new MQTTMessageLogger());
                    pipeline.addLast("handler", handler);
                }
            });
        }
   ```
   + 修改
   ``` 
     // 默认prefix为"/org/fusesource/mqtt"
     pipeline.addLast("webSocketHandler",
                            new WebSocketServerProtocolHandler("/org/fusesource/mqtt", MQTT_SUBPROTOCOL_CSV_LIST));
    // 修改后prefix为"/"             
    pipeline.addLast("webSocketHandler",
                            new WebSocketServerProtocolHandler("/", MQTT_SUBPROTOCOL_CSV_LIST));
   ```
      - 因为web client请求时ws://192.168.1.3:8083/
   
 
 * TODO
   + [x] 使用hazelcast 实现缓存，类似redis
   + [ ] 数据存储
   + [ ] im 通信消息
     - mqtt 协议
     - 如何保证qos
   + [ ] 如何与服务端sdk对接
   
   + [ ] 需要安全认证与数据加密
   + [ ] 需要分布式缓存
   + [ ] 需要集群部属
   + [ ] 登录过程
 
 
### MQTT 协议
 * 协议组成
   + 固定报头
   + 可变报头（部分控制报文包含）
   + 有效负载（部分控制报文包含）
   
 * 固定报头
   + 组成
      - 第1个字节：MQTT控制报文的类型 + 报文类型标志位
      - 后几个字节：剩余长度
   + MQTT控制报文类型
      - 0, 15 保留使用
      - 1，14 为不同的报文类型
      
   + 报文类型标志位
      - 只有PUBLISH有，其它类型都是保留使用
      
   + 剩余长度
      1. 是可变报头 + 有效负载的长度
      2. 最少1个字节，最多4个字节来表示长度
        > 这允许应用发送最大256MB(268,435,455)大小的控制报文。
        > 这个数值在报文中的表示是：0xFF,0xFF,0xFF,0x7F。
      3. 长度大于128时的多字节表示，最高位为表示是否是多字节长度
         - 0-7位表示的大小
      ``` 
      字节数	最小值	                            最大值
      1	    0 (0x00)	                        127 (0x7F)
      2	    128 (0x80, 0x01)	                16383 (0xFF, 0x7F)
      3	    16384 (0x80, 0x80, 0x01)	        2097151 (0xFF, 0xFF, 0x7F)
      4	    2097152 (0x80, 0x80, 0x80, 0x01)	268435455 (0xFF, 0xFF, 0xFF, 0x7F)
      ```
   
        
 * 可变报头
   + 组成
     - 报文标识符（不同报文类型相应才有）
     - 其它不同MQTT控制报文类型相应标识
     
   + 报文标识符
     - PUBLISH(Qos > 0)，SUBSCRIBE，UNSUBSCRIBE需要报文标识符
     - 占两个字节
     - 每次发送一个新的这些类型的报文时都必须分配一个当前未使用的报文标识符 
       > 当发送者处理完这个报文对应的确认后，这个报文标识符就释放可重用
       > 意思：一个标识符用完之后，之后其它消息还可以使用这个标识符
     - 重发报文时，它必须使用第一次消息相同的标识符
     - 所以确认消息，它必须使用相同的标识符
     - QoS等于0的PUBLISH报文不能包含报文标识符 
     - 客户端和服务端彼此独立地分配报文标识符
       > 可能同时，客户端发送标识符1234，服务器也向客户端发送标识符1234消息
       这是可以的，称为并发消息交换
       
 * 有效负载
   + 哪些MQTT报文类型需要有负载
     - CONNECT
     - PUBLISH（可选）
     - SUBSCRIBE
     - SUBACK
     - UNSUBSCRIBE
     
 * MATT报文类型CONNECT
   + 类型是1
   + 固定报头的中剩余长度值为
     - 可变报头长度（10字节）+ 有效负载长度
     
   + 可变报头
     - 长度10个字节
       > 主要看协议名是否采用标准的MQTT字母来标识
     - 内容顺序依次为
        1. 协议名
        2. 协议级别（可以自定义级别, 相当于协版本）
        3. 连接标志
        4. 保持连接 
        
   + 特殊说一下协议名
     - 1-2字节表示协议名长度
     - 3-6字节每个字节表示一个字母，组成协议名
     - 7字节就是协议级别，占一个字节不变，且在协议名后
     - 8字节就是连接标志，占一个字节不变，顺序固定
     - 9-10字节就是保持连接大小，占两个字不变，顺序固定
     - 所是如果是标准名称时，可变报头是10个字节
     - 如果自定义协议名，可能长度就变了
     
   ``` 
    	    说明	        7	6	5	4	3	2	1	0
    协议名
    byte 1	长度 MSB (0)	0	0	0	0	0	0	0	0
    byte 2	长度 LSB (4)	0	0	0	0	0	1	0	0
    byte 3	‘M’	        0	1	0	0	1	1	0	1
    byte 4	‘Q’	        0	1	0	1	0	0	0	1
    byte 5	‘T’	        0	1	0	1	0	1	0	0
    byte 6	‘T’	        0	1	0	1	0	1	0	0
   ```
   
   + 连接标志
     - 连接标志字节包含一些用于指定MQTT连接行为的参数。
     **它还指出有效载荷中的字段是否存在。**
     ``` 
     7        6        5          4     3 2       1            0
     username password willRetain willQos wilFlag cleanSession reserved
     ```
     - 0是保留使用，不能设置
       
   + 有效载荷 Payload
     - 包含一个或多个以长度为前缀的字段
     - 可变报头中的标志决定是否包含这些字段。如果包含的话，必须按这个顺序出现：客户端标识符，遗嘱主题，遗嘱消息，用户名，密码
        
   + 使用注意
     1. 在一个网络连接上，客户端只能发送一次CONNECT报文。
     服务端必须将客户端发送的第二个CONNECT报文当作协议违规处理并断开客户端的连接
     2. 如果协议名不正确服务端可以断开客户端的连接，也可以按照某些其它规范继续处理CONNECT报文
     3. 如果发现不支持的协议级别，服务端必须给发送一个返回码为0x01（不支持的协议级别）的CONNACK报文响应CONNECT报文，
     然后断开客户端的连接
     4. 服务端必须验证CONNECT控制报文的连接标志中保留标志位（第0位）是否为0，如果不为0必须断开客户端连接
