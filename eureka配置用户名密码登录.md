# eureka-server(eureka注册中心)
## 添加依赖
```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>
<dependency> 
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

## 配置用户名和密码
```
security.user.name=your_username
security.user.password=your_password
```

# eureka-client
## 添加依赖
```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```

## 连接eureka注册中心
```
eureka.client.serviceUrl.defaultZone=http://${security.user.name}:${security.user.password}@127.0.0.1:${server.port}/eureka/
```
