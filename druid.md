# 添加依赖
```
<dependency>
	<groupId>mysql</groupId>
	<artifactId>mysql-connector-java</artifactId>
	<version>8.0.11</version>
</dependency>
<dependency>
	<groupId>com.alibaba</groupId>
	<artifactId>druid-spring-boot-starter</artifactId>
	<version>1.1.10</version>
</dependency>
```

# 配置application.properties
```
#db
spring.datasource.url=jdbc:mysql://20.0.0.252:3306/zhy?characterEncodeing=utf-8&useSSL=false&serverTimezone=GMT
spring.datasource.username=root
spring.datasource.password=root

#druid
spring.datasource.druid.initial-size=1
spring.datasource.druid.min-idle=1
spring.datasource.druid.max-active=20
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.stat-view-servlet.allow=true
```

# 浏览
http://localhost:9013/druid
