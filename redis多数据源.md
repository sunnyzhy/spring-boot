# redis 多数据源
**使用 Lettuce Client，而非 Jedis Client。**

## 添加依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>

<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```

## 配置 redis 数据源
```yml
spring:
  # 默认数据源
  redis:
    database: 0
    host: 192.168.0.10
    port: 6379
    password: root
    timeout: 5000
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1
  # 自定义数据源
  custom-1:
    redis:
      database: 0
      host: 192.168.0.11
      port: 6379
      password: root
      timeout: 5000
      lettuce:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1
```

## 公共模块
### 1. 在公共模块里定义 redis 的属性配置类
```java
public class RedisProperty {
    // Redis数据库索引
    private Integer database = 0;
    // Redis服务器地址
    private String host = "127.0.0.1";
    // Redis服务器连接端口
    private Integer port = 6379;
    // Redis服务器连接密码（默认为空）
    private String password;
    // 连接池最大连接数（使用负值表示没有限制）
    private Integer maxActive = 8;
    // 连接池最大阻塞等待时间（使用负值表示没有限制）
    private Integer maxWait = -1;
    // 连接池中的最大空闲连接
    private Integer maxIdle = 8;
    // 连接池中的最小空闲连接
    private Integer minIdle = 0;
    // 连接超时时间（毫秒）
    private Long timeout = 3000l;

    public RedisProperty(Integer database, String host, Integer port, String password, Integer maxActive, Integer maxWait, Integer maxIdle, Integer minIdle, Long timeout) {
        this.database = database;
        this.host = host;
        this.port = port;
        this.password = password;
        this.maxActive = maxActive;
        this.maxWait = maxWait;
        this.maxIdle = maxIdle;
        this.minIdle = minIdle;
        this.timeout = timeout;
    }

    public Integer getDatabase() {
        return database;
    }

    public String getHost() {
        return host;
    }

    public Integer getPort() {
        return port;
    }

    public String getPassword() {
        return password;
    }

    public Integer getMaxActive() {
        return maxActive;
    }

    public Integer getMaxWait() {
        return maxWait;
    }

    public Integer getMaxIdle() {
        return maxIdle;
    }

    public Integer getMinIdle() {
        return minIdle;
    }

    public Long getTimeout() {
        return timeout;
    }
}
```

### 2. 在公共模块里定义创建 redis 实例的类
```java
/**
 * 用来创建 redis 实例，并非 RedisBean，需要在业务模块里创建 RedisBean
 */
public class RedisBean {
    private RedisProperty redisProperty;

    public RedisBean(RedisProperty redisProperty) {
        this.redisProperty = redisProperty;
    }

    public RedisTemplate<String, Object> generateRedisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 初始化 redis 连接
        RedisConnectionFactory connectionFactory = getConnectionFactory();
        redisTemplate.setConnectionFactory(connectionFactory);
        // 设置 key、value 的序列化方式
        setSerializer(redisTemplate);
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * 初始化 redis 连接
     *
     * @return
     */
    private RedisConnectionFactory getConnectionFactory() {
        // 初始化 redis 的基础连接配置
        RedisStandaloneConfiguration configuration = getRedisConfiguration();
        // 初始化 redis 的连接池配置
        GenericObjectPoolConfig poolConfig = getPoolConfig();
        // 使用 Lettuce Client
        LettuceClientConfiguration clientConfig = LettucePoolingClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(redisProperty.getTimeout()))
                .poolConfig(poolConfig)
                .build();
        LettuceConnectionFactory connectionFactory = new LettuceConnectionFactory(configuration, clientConfig);
        // 必须调用 afterPropertiesSet() 方法，否则 client、connectionProvider、reactiveConnectionProvider 都为空
        connectionFactory.afterPropertiesSet();
        return connectionFactory;
    }

    /**
     * 初始化 redis 的基础连接配置
     *
     * @return
     */
    private RedisStandaloneConfiguration getRedisConfiguration() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(redisProperty.getHost());
        configuration.setPort(redisProperty.getPort());
        configuration.setPassword(redisProperty.getPassword());
        configuration.setDatabase(redisProperty.getDatabase());
        return configuration;
    }

    /**
     * 初始化 redis 的连接池配置
     *
     * @return
     */
    private GenericObjectPoolConfig getPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMaxTotal(redisProperty.getMaxActive());
        poolConfig.setMaxWaitMillis(redisProperty.getMaxWait());
        poolConfig.setMaxIdle(redisProperty.getMaxIdle());
        poolConfig.setMinIdle(redisProperty.getMinIdle());
        return poolConfig;
    }

    /**
     * 设置 key、value 的序列化方式
     *
     * @param redisTemplate
     */
    private void setSerializer(RedisTemplate<String, Object> redisTemplate) {
        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);

        // 设置值value的序列化方式，防止value中出现类的声明
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);

        // 设置键key的序列化方式，防止key中出现类型的声明
        redisTemplate.setKeySerializer(new StringSerializer());
        redisTemplate.setHashKeySerializer(new StringSerializer());
    }
}
```

## 业务模块
### 1. 在业务模块里定义 RedisBean
```java
@Configuration
public class CustomRedisConfig1 {
    @Value("${spring.custom-1.redis.database:0}")
    private Integer database;
    @Value("${spring.custom-1.redis.host:127.0.0.1}")
    private String host;
    @Value("${spring.custom-1.redis.port:6379}")
    private Integer port;
    @Value("${spring.custom-1.redis.password}")
    private String password;
    @Value("${spring.custom-1.redis.lettuce.pool.max-active:8}")
    private Integer maxActive;
    @Value("${spring.custom-1.redis.lettuce.pool.max-wait:-1}")
    private Integer maxWait;
    @Value("${spring.custom-1.redis.lettuce.pool.max-idle:8}")
    private Integer maxIdle;
    @Value("${spring.custom-1.redis.lettuce.pool.min-idle:0}")
    private Integer minIdle;
    @Value("${spring.custom-1.redis.timeout:3000}")
    private Long timeout;

    @Bean(name = "custom-redis-1")
    public RedisTemplate<String, Object> redisTemplate1() {
        RedisProperty redisProperty = new RedisProperty(database, host, port, password, maxActive, maxWait, maxIdle, minIdle, timeout);
        RedisBean redisBean = new RedisBean(redisProperty);
        RedisTemplate<String, Object> redisTemplate = redisBean.generateRedisTemplate();
        return redisTemplate;
    }
}
```

### 2. 单元测试
```java
// 使用默认的数据源
@Autowired
private RedisTemplate<String, Object> redisTemplate;

// 使用自定义的数据源
@Resource(name = "custom-redis-1")
private RedisTemplate<String, Object> redisTemplate1;

@Test
void contextLoads() throws IOException {
    // 使用默认的数据源
    Object value = redisTemplate.opsForValue().get("a:b:c");
    System.out.println(value);

    // 使用自定义的数据源
    Object value1 = redisTemplate1.opsForValue().get("a:b:c");
    System.out.println(value1);
}
```
