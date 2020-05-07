### 野火im


#### server
 * 技术
   + Flyway
   + mqtt
   + shiro
   + hazelcast
 * TODO
   + [ ] maven 打包项目配置
   
 * broker配置
   + 将distribution下的log4j.log拷贝到broker config下
     - 考虑换成log4j2
     
 * broker启动
   + 启动参数：参考distribution项目下的sh
   ```
     -- mac
     -Dwildfirechat.path=/Users/playcrab/data/project/bmn/survey/server/broker/
     -Dlog4j.configuration=file:/Users/playcrab/data/project/bmn/survey/server/broker/config/log4j.properties
     -Dhazelcast.configuration=file:/Users/playcrab/data/project/bmn/survey/server/broker/config/hazelcast.xml
     
     -- windows
     -Dwildfirechat.path=E:/project/bmn/2020-survey/server/broker
     -Dlog4j.configurationFile=config/log4j2-test.xml
     -Dlogging.path=logs
    ```
 * h2db web 界面启动
   + 获取：h2-version.jar
   
   + 执行命令：
     - 需要把h2-version.jar放到broker目录下，再此目录下执行启动命令，由于jdbc url 使用的相对路径
     - java -cp h2-1.4.197.jar org.h2.tools.Console
   + 密码大写: SA
   + jdbc url: jdbc:h2:./h2db/wfchat;AUTO_SERVER=TRUE;MODE=MySQL
