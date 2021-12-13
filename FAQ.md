# FAQ

## 1 PathVariable annotation was empty on param 0

- 原因

远程调用Feign的时候报错，PathVariable注解为空。

- 解决方法

@PathVariable注释要写明value
```java
@PathVariable(value = "id") Integer id
```

## 2 @Value注解报Could not resolve placeholder错误

- 原因

存在多个properties配置文件，即除了application.properties之外，还有自定义的xxx.properties。

Spring容器采用反射扫描的发现机制，在探测到Spring容器中有一个org.springframework.beans.factory.config.PropertyPlaceholderConfigurer的Bean就会停止对剩余PropertyPlaceholderConfigurer的扫描，所以根据加载的顺序，配置的第二个property-placeholder就被没有被spring加载，所以在使用@Value注入的时候占位符就解析不了。

- 解决方法

合并properties配置文件
```xml
    <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
        <property name="locations">
            <list>
                <value>classpath:application.properties</value>
                <value>classpath:nnn/xxx.properties</value>
            </list>
        </property>
    </bean>
```

## 3 Cannot determine embedded database driver class for database type NONE

- 原因

配置文件中缺少DataSource配置

- 解决方法

在springboot的启动类上禁止自动初始化DataSource
```java
@SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
```

## 4 spring boot 单元测试的时候报 BeanCreationNotAllowedException: Error creating bean with name 'eurekaAutoServiceRegistration'

[github解决方案](https://github.com/spring-cloud/spring-cloud-netflix/issues/1952 "github解决方案")

```
The workaround works when running the application, but not when running tests disappointed.
```

## 5 The field file exceeds its maximum permitted size of 1048576 bytes.

- 原因

Spring Boot 内置的 tomcat 限制了请求的文件大小。

- 解决方法

Spring Boot1.4 之后的版本配置:
```
spring.http.multipart.maxFileSize = 10Mb
spring.http.multipart.maxRequestSize = 100Mb
```

Spring Boot2.0 之后的版本配置:
```
spring.servlet.multipart.max-file-size = 10MB  
spring.servlet.multipart.max-request-size = 100MB
```

**如果不限制文件上传的大小，就把两个值都设置为-1。**

## 6 post 请求，后台已执行，但是返回 404

解决方法:

- 用 @Controller 注解修饰的 controller ，需加上 ```@ResponseBody``` 注解
- 用 ```@RestController``` 注解修饰 controller

## 7 RestController 报错 404

SpringBoot 启动时候，启动类从当前包开始扫描子级包，所以如果 Controller 不是启动类所在包的子级包，是不会被扫描进 IOC 容器并进行自动配置的。此时，需要将 Controller 所在的包修改为启动类的子级包。

例如，Controller 所在的包是 ```package com.example.controller;```，而主启动类所在的包是 ```package com.example.demo;```，由于 Controller 所在的包不是启动类所在包的子级包，所以前端访问 Controller 接口的时候就会抛出 404 的异常。

解决方法:

把 Controller 所在的包修改为 ```package com.example.demo.controller;``` 使其成为  ```package com.example.demo;``` 的子级包。
