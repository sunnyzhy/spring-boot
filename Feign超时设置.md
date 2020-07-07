# Feign的依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

# 开启feign注解
```java
@EnableFeignClients
```

## openfeign 2.x
yml文件配置
```yml
feign:
  client:
    config:
      service-name:           # 服务名，配置default为所有服务
        connectTimeout: 1000
        readTimeout: 12000
```

## openfeign 1.x
1. yml文件配置

```yml
feign:
  connect-timeout: 30000
  read-timeout: 30000 # feign的超时时间，单位：毫秒
  max-attempts: 3 # feign的重试次数
```

2.自定义配置

```java
@Configuration
public class FeignConfigure {
    @Value("${feign.connect-timeout:30000}")
    public long connectTimeout;
    @Value("${feign.read-timeout:30000}")
    public long readTimeout;
    @Value("${feign.max-attempts:3}")
    public int maxAttempts;

    @Bean
    public Request.Options options() {
        return new Request.Options(connectTimeout, TimeUnit.MILLISECONDS, readTimeout, TimeUnit.MILLISECONDS, true);
    }

    @Bean
    public Retryer feignRetryer() {
        Retryer retryer = new Retryer.Default(100, 1000, maxAttempts);
        return retryer;
    }
}
```
