当不同的两个 config 都注册了同一个Bean，启动服务后，会报bean已被注册异常。

springboot 中，allowBeanDefinitionOverriding 默认为 false；spring 默认为 true。

需要在 application.properties 中配置 **spring.main.allow-bean-definition-overriding=true**
