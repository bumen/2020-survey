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
   + [ ] 使用hazelcast 实现缓存，类似redis
   + [ ] 数据存储
   + [ ] im 通信消息
     - mqtt 协议
     - 如何保证qos
   + [ ] 如何与服务端sdk对接
 
 
