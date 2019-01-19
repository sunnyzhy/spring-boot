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

# Config Server

# Config Client

# refresh
1. 在Request的Headers中添加**Content-Type: application/json**

2. 以**POST**方式发送

3. 调用**/actuator/refresh**
