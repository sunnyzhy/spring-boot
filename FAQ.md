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

## 8 Eureka 在有虚拟网卡的情况下获取正确的IP

### 解决方法

```yml
spring:
  cloud:
    inetutils:
      ignored-interfaces: ## 忽略网卡
      - VMware.*
```

### 源码分析

#### 8.1 EurekaClientAutoConfiguration

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\netflix\eureka\EurekaClientAutoConfiguration.class

@Bean
@ConditionalOnMissingBean(
    value = {EurekaInstanceConfig.class},
    search = SearchStrategy.CURRENT
)
public EurekaInstanceConfigBean eurekaInstanceConfigBean(InetUtils inetUtils, ManagementMetadataProvider managementMetadataProvider) {
    // ...
    EurekaInstanceConfigBean instance = new EurekaInstanceConfigBean(inetUtils);
    // ...
    return instance;
}
```

#### 8.2 EurekaInstanceConfigBean

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\netflix\eureka\EurekaInstanceConfigBean.class

public EurekaInstanceConfigBean(InetUtils inetUtils) {
    // ...
    this.hostInfo = this.inetUtils.findFirstNonLoopbackHostInfo();
    // ...
}
```

#### 8.3 findFirstNonLoopbackHostInfo()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

public InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
    InetAddress address = this.findFirstNonLoopbackAddress();
    // ...
}
```

#### 8.4 findFirstNonLoopbackAddress()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

public InetAddress findFirstNonLoopbackAddress() {
    // ...

    try {
        // ...
        while(true) {
            NetworkInterface ifc;
            do {
                // ...
            } while(this.ignoreInterface(ifc.getDisplayName()));

            // ...
        }
    } catch (IOException var8) {
        this.log.error("Cannot get first non-loopback address", var8);
    }

    // ...
}
```

#### 8.5 ignoreInterface()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

boolean ignoreInterface(String interfaceName) {
    // 从配置文件里读取需要忽略的网卡
    Iterator var2 = this.properties.getIgnoredInterfaces().iterator();

    String regex;
    do {
        if (!var2.hasNext()) {
            return false;
        }

        regex = (String)var2.next();
    } while(!interfaceName.matches(regex));

    this.log.trace("Ignoring interface: " + interfaceName);
    return true;
}
```

## 9 单元测试

### 9.1 Unable to find a @SpringBootConfiguration, you need to use @ContextConfiguration or @SpringBootTest(classes=...) with your test

一般常出现在 maven 创建的工程，需要在 @SpringBootTest 注解里指定```启动类```，即 @SpringBootTest(classes = 启动类.class)

比如启动类:

```java
@SpringBootApplication
public class DemoApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class,args);
    }
}
```

那么，需要在 @SpringBootTest 注解里指定 DemoApplication:

```java
@SpringBootTest(classes = DemoApplication.class)
public class DemoTests {

}
```

### 9.2 Failed to load ApplicationContext

检查 application.yml 或 application.properties 的配置项是否有错误或不完整的地方。

## 10 为什么在业务模块的 pom#properties 加上 ＜log4j2.version＞ 配置就可以更新 log4j2 的版本

1. ctrl + 左键，点击业务模块的 pom 文件中 ```<parent>```里的任意内容
   ```xml
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>2.5.7</version>
           <relativePath/> <!-- lookup parent from repository -->
       </parent>
   ```
   跳转到 ```spring-boot-starter-parent-2.5.7.pom```
2. ctrl + 左键，点击 ```spring-boot-starter-parent-2.5.7.pom#<parent>```里的任意内容
   ```xml
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-dependencies</artifactId>
           <version>2.5.7</version>
       </parent>
   ```
   跳转到 ```spring-boot-dependencies-2.5.7.pom```
3. 在 ```spring-boot-dependencies-2.5.7.pom``` 里搜索 ```log4j2.version```
   ```xml
       <properties>
           // ...
           <log4j2.version>2.14.1</log4j2.version>
           // ...
       </properties>
   ```
4. 发现 spring-boot 2.5.7 默认使用的 log4j2 的版本号是 2.14.1, 所以，我们只需要在业务模块的 pom#properties 里显式地配置 ```<properties><log4j2.version>2.17.0</log4j2.version></properties>```，就可以覆盖 spring-boot 默认的 log4j2 的版本号，从而实现版本的更新。
