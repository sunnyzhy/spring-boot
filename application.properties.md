# application.properties

## eureka server

- pom.xml

   ```xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
		</dependency>
   ```

- application.properties

   ```
   server.port=9010
   
   spring.application.name=eureka-server
   
   #表示是否将自己注册到Eureka Server上，默认为true
   eureka.client.register-with-eureka=false
   
   #表示是否从Eureka Server上获取注册信息，默认为true
   eureka.client.fetch-registry=false
   
   eureka.client.service-url.defaultZone=http://localhost:9010/eureka/
   
   #控制台彩色输出
   spring.output.ansi.enabled=ALWAYS
   ```

- 注解

   ```java
   @EnableEurekaServer
   ```

## eureka client
- pom.xml

   ```xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
		</dependency>
   ```

- application.properties

   ```
   server.port=9011
   
   spring.application.name=eureka-client
   
   eureka.client.service-url.defaultZone=http://localhost:9010/eureka/
   ```

- 注解

   ```java
   @EnableDiscoveryClient
   ```

## zuul
- pom.xml

   ```xml
		<dependency>
			<groupId>org.springframework.cloud</groupId>
			<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
		</dependency>
   ```

- application.yml

   ```
	zuul:
	  host:
	    connect-timeout-millis: 60000
	    socket-timeout-millis: 60000
	  prefix: /api
	  routes:
	    #路由名称，随意，唯一即可(表示只要HTTP请求是 /grp开始的，就会forward到服务id为group_server的服务上面)
	    group:
		#路由的路径
		path: /grp/**
		#服务名称
		serviceId: group_server
	    user:
	      path: /usr/**
	      serviceId: user_server
   ```

- 注解

   ```java
   @EnableZuulProxy
   ```

## druid

```
spring.datasource.druid.driver-class-name=com.mysql.cj.jdbc.Driver

spring.datasource.druid.url=jdbc:mysql://192.168.x.x:3306/dbName?useSSL=false

spring.datasource.druid.username=root

spring.datasource.druid.password=root
```

## mybatis

```
#mybatis自动生成的实体类
mybatis.type-aliases-package=com.xxx.xxx.model

#mybatis自动生成的sql映射文件
mybatis.mapper-locations=classpath:mapping/*.xml

# mapper类所在的包
logging.level.com.xx.mapper=DEBUG
```

## redis

```
#redis的数据库索引号[0-15]
spring.redis.database=0

spring.redis.host=192.168.x.x

spring.redis.port=6379

spring.redis.password=

spring.redis.timeout=3000
```

## mail

```
spring.mail.host=smtp.exmail.qq.com

spring.mail.username=userName

spring.mail.password=password

spring.mail.properties.mail.smtp.auth=true

spring.mail.properties.mail.smtp.starttls.enable=true

spring.mail.properties.mail.smtp.starttls.required=true

spring.mail.senders=xxx@xxx.com
```

## log

```
#打印sql语句、参数、受影响的行数，dao包里存放的是mybatis自动生成的mapper接口
logging.level.com.xxx.dao=debug

#日志目录及文件名
logging.file=log/xx.log
```

## 其它

```
# 忽略虚拟网卡
spring:
  cloud:
    inetutils:
      ignoredInterfaces: VMware.*

# 引用依赖 Elasticsearch，在工程启动的时候，会自动检查 localhost/127.0.0.1:9200 的健康状态
# 如果是动态创建 Elasticsearch 数据源并且本地 127.0.0.1 没有搭建 Elasticsearch 服务的话，那么在工程启动的时候，检查 Elasticsearch 的健康状态会失败
# 此时，可以配置不检查 Elasticsearch 的健康状态
management:
  health:
    elasticsearch:
      enabled: false
```
