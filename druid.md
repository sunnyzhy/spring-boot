# druid

## 添加依赖
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

## 配置application.properties
```
server.port=9013

#db
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://localhost:3306/my_db?characterEncodeing=utf-8&useSSL=false&serverTimezone=GMT
spring.datasource.username=root
spring.datasource.password=root

#druid
spring.datasource.druid.initial-size=1
spring.datasource.druid.min-idle=1
spring.datasource.druid.max-active=20
spring.datasource.druid.test-on-borrow=true
spring.datasource.druid.stat-view-servlet.allow=true
```

## 浏览
http://localhost:9013/druid

## druid 连接失败不停尝试重连

```java
public DataSource build(String url, String username, String password) throws Exception {
     Properties p =new Properties();
     p.put(DruidDataSourceFactory.PROP_INIT, "false");
     p.put(DruidDataSourceFactory.PROP_URL, url);
     p.put(DruidDataSourceFactory.PROP_USERNAME, username);
     p.put(DruidDataSourceFactory.PROP_PASSWORD, password);
     DruidDataSource dataSource = (DruidDataSource) DruidDataSourceFactory.createDataSource(p);
     dataSource.setBreakAfterAcquireFailure(true);
     dataSource.init();
     return dataSource;
}
```

注意以下几点:

1. ```init``` 设置为 ```false```
2. 调用 ```setBreakAfterAcquireFailure(true)``` 方法
3. 调用 ```init()``` 方法
