# ConfigurationProperties 用法
比如配置文件 application.yml 里有以下配置项:

```yml
jwt:
  secret-key: zhy-123456
  expire-time: 15
  refresh-expire-time: 10
```

可以通过以下两种用法读取配置项的内容。

## 用法 1
通过 Component Scan 扫描到 JwtConfig。

```java
@Component
@ConfigurationProperties(prefix = "jwt")
@Data
public class JwtConfig {
    private String secretKey;
    private int expireTime;
    private int refreshExpireTime;
}
```

## 用法 2
通过 Spring 的 Java Configuration 特性装配 JwtConfig。

```java
@Data
public class JwtConfig {
    private String secretKey;
    private int expireTime;
    private int refreshExpireTime;
}

@Configuration
public class JwtBeanConfig {
    @Bean
    @ConfigurationProperties(prefix = "jwt")
    public JwtConfig jwtConfig() {
        return new JwtConfig();
    }
}
```
