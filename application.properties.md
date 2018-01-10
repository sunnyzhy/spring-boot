# eureka server
```
eureka.instance.hostname=localhost
eureka.client.registerWithEureka=false
eureka.client.fetchRegistry=false
eureka.client.serviceUrl.defaultZone=http://${eureka.instance.hostname}:8080/eureka/
```

# eureka client
```
eureka.client.serviceUrl.defaultZone=http://192.168.x.x:8080/eureka/
```

# druid
```
spring.datasource.druid.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.druid.url=jdbc:mysql://192.168.x.x:3306/dbName?useSSL=false
spring.datasource.druid.username=root
spring.datasource.druid.password=root
```

# mybatis
```
#mybatis自动生成的实体类
mybatis.type-aliases-package=com.xxx.xxx.model
#mybatis自动生成的sql映射文件
mybatis.mapper-locations=classpath:mapping/*.xml
```

# redis
```
#redis的数据库索引号[0-15]
spring.redis.database=0
spring.redis.host=192.168.x.x
spring.redis.port=6379
spring.redis.password=
spring.redis.timeout=3000
```

# mail
```
spring.mail.host=smtp.exmail.qq.com
spring.mail.username=userName
spring.mail.password=password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true
spring.mail.properties.mail.smtp.starttls.required=true
spring.mail.senders=xxx@xxx.com
```

# log
```
#打印sql语句、参数、受影响的行数，dao包里存放的是mybatis自动生成的mapper接口
logging.level.com.xxx.dao=debug
#日志目录及文件名
logging.file=log/xx.log
```
