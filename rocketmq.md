# 启动NameServer
用管理员身份运行cmd，进入到'MQ\bin'目录下
```
> start mqnamesrv.cmd
```

在弹出的窗口中显示以下内容，说明NameServer启动成功：
```
The Name Server boot success. serializeType=JSON
```

# 启动Broker
用管理员身份运行cmd，进入到'MQ\bin'目录下
```
> start mqbroker.cmd -n 127.0.0.1:9876 autoCreateTopicEnable=true
```

在弹出的窗口中显示以下内容，说明Broker启动成功：
```
The broker[zhy, 192.168.0.6:10911] boot success. serializeType=JSON and name server is 127.0.0.1:9876
```

# NameServer功能

- 接收broker的请求注册broker路由信息（包括master和slave）

- 接收client的请求根据某个topic获取所有到broker的路由信息

# NameServer原理

1. 每个broker启动的时候会向namesrv注册

2. Producer发送消息的时候根据topic获取路由到broker的信息

3. Consumer根据topic到namesrv获取topic的路由到broker的信息
