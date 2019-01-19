# 为什么要统一管理微服务配置
对于Spring Boot应用，我们可以将配置内容写入application.yml，设置多个profile，也可以用多个application-{profile}.properties文件配置，并在启动时指定spring.profiles.active={profile}来加载不同环境下的配置。
在Spring Cloud微服务架构中，这种方式未必适用，微服务架构对配置管理有着更高的要求，如：

- 集中管理：成百上千（可能没这么多）个微服务需要集中管理配置，否则维护困难、容易出错；

- 运行期动态调整：某些参数需要在应用运行时动态调整（如连接池大小、熔断阈值等），并且调整时不停止服务；

- 自动更新配置：微服务能够在配置发生变化是自动更新配置。

以上这些要求，传统方式是无法实现的，所以有必要借助一个通用的配置管理机制，通常使用配置服务器来管理配置。

# Sping Cloud Config简介
Spring Cloud Config分为Config Server和Config Client两部分，为分布式系统外部化配置提供了支持。 Spring Cloud Config非常适合Spring应用程序，也能与其他编程语言编写的应用组合使用。
微服务在启动时，通过Config Client请求Config Server以获取配置内容，同时会缓存这些内容。

# 新建github仓库与配置文件
1. 新建springcloud-config仓库

2. 新建default配置文件

```
cloud-config-default.properties
version=default-1.0
```

3. 新建dev配置文件

```
cloud-config-dev.properties
version=dev-1.0
```

4. 新建pro配置文件

```
cloud-config-pro.properties
version=pro-1.0
```

5. 新建test配置文件

```
cloud-config-test.properties
version=test-1.0
```

# Springboot版本2.1.2

# Config Server
## pom.xml
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
```

## application.yml
```
server:
  port: 8090
spring:
  application:
    name: springcloud-config-server
  cloud:
    config:
      server:
        git:
          uri: https://github.com/sunnyzhy/springcloud-config.git
          username: sunnyzhy
          password: ******
eureka:
  client:
    service-url:
      defaultZone: http://localhost:9010/eureka/
```

## 启动类添加@EnableConfigServer注解
```java
@EnableConfigServer
@EnableDiscoveryClient
```

## Config Server文件映射
映射{application}-{profile}.properties文件
```
/{application}/{profile}/[{label}]
/{label}/{application}-{profile}.properties
/{application}-{profile}.properties
/{label}/{application}-{profile}.yml
/{application}-{profile}.yml
```
- {application}通常使用微服务名称，对应Git仓库中文件名的前缀；

- {profile}对应{application}-后面的dev、pro、test等；

- {label}对应Git仓库的分支名，默认为master。

## 启动服务
访问http://localhost:8090/cloud-config/dev ,显示内容如下:
```
{"name":"cloud-config","profiles":["dev"],"label":null,"version":"a938ff4d417c8c5113155b3baaebcb3073f21dd3","state":null,"propertySources":[{"name":"https://github.com/sunnyzhy/springcloud-config.git/cloud-config-dev.properties","source":{"version":"dev-1.0"}},{"name":"https://github.com/sunnyzhy/springcloud-config.git/cloud-config.properties","source":{"version":"default-1.0"}}]}
```

# Config Client
## pom.xml
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
```

## bootstrap.yml
```
eureka:
  client:
    service-url:
      defaultZone: http://localhost:9010/eureka/
spring:
  cloud:
    config:
      #github仓库里的profile名
      profile: dev
      discovery:
        enabled: true
        #eureka注册中心的configServer服务名
        service-id: springcloud-config-server
management:
  endpoints:
    web:
      exposure:
        #include可以是用逗号分割的关键字:refresh,health,info，也可以是"*"
        include: "*"
```

## application.yml
**微服务的spring.application.name属性值决定了Git仓库中配置文件的的文件名前缀。**
```
server:
  port: 8091
spring:
  application:
    #github仓库里的application名
    name: cloud-config
```

## 启动类添加注解
```java
@EnableDiscoveryClient
```

## Config
**@RefreshScope注解用于刷新配置**
```java
@Configuration
@RefreshScope
@Data
public class MyConfig {
    @Value("${version}")
    private String version;
}
```

## Controller
```java
@RestController
public class ConfigController {
    @Autowired
    private MyConfig config;

    @RequestMapping("/get")
    public String get() {
        return config.getVersion();
    }
}
```

## 启动服务
1. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.0
```

2. 在git仓库中修改version的值为dev-1.1

用REST Client发送**post**请求http://localhost:8091/actuator/refresh ,显示如下内容
```
["config.client.version","version"]
```

3. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.1
```

**刷新的时候，要注意以下几点：**
1. 在Request的Headers中添加**Content-Type: application/json**

2. 用**POST**请求

3. 调用/actuator/refresh

4. 虽然服务没有重启，但是我们要一个服务一个服务地发送post请求才能获取到更新之后的配置，这显然是不可取的。此时就需要采用消息队列的发布订阅模式，让所的为服务来订阅这个事件，当这个事件发生改变时，就可以通知所有微服务去更新它们的内存中的配置信息，**Bus消息总线**就能解决这个问题。
