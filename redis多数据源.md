# 添加依赖
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<dependency>
  <groupId>redis.clients</groupId>
  <artifactId>jedis</artifactId>
</dependency>
```

# 配置redis数据源
```
spring:
  redis:
    database: 1
    host: 192.168.0.1
    ssl: false
    timeout: 0
    jedis:
      pool:
        max-active: 20
        max-idle: 8
        min-idle: 0
        max-wait: -1
  redis2:
    database: 0
    host: 192.168.0.2
```

# 定义父类
```java
@EnableCaching
@Configuration
public class RedisConfig {
    @Value("${spring.redis.jedis.pool.max-active}")
    private int maxActive;

    @Value("${spring.redis.jedis.pool.max-wait}")
    private int maxWait;

    @Value("${spring.redis.jedis.pool.max-idle}")
    private int maxIdle;

    @Value("${spring.redis.jedis.pool.min-idle}")
    private int minIdle;

    @Value("${spring.redis.ssl}")
    private boolean ssl;

    @Value("${spring.redis.timeout}")
    private long timeout;

    public RedisConnectionFactory createRedisConnectionFactory(int database, String host) {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxWaitMillis(maxWait);
        poolConfig.setMinIdle(maxIdle);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(false);
        poolConfig.setTestWhileIdle(true);
        JedisClientConfiguration jedisClientConfiguration = null;

        if (ssl){
            jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().
                    poolConfig(poolConfig).and().
                    readTimeout(Duration.ofMillis(timeout)).useSsl()
                    .build();
        }else {
            jedisClientConfiguration = JedisClientConfiguration.builder().usePooling().
                    poolConfig(poolConfig).and().
                    readTimeout(Duration.ofMillis(timeout)).build();
        }
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();

        redisStandaloneConfiguration.setDatabase(database);
        redisStandaloneConfiguration.setHostName(host);
        RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration);
        return redisConnectionFactory;
    }

    /**
     * 设置RedisTemplate的序列化方式
     *
     * @param redisTemplate
     */
    public void setSerializer(RedisTemplate redisTemplate) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        //设置键（key）的序列化方式
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        //设置值（value）的序列化方式
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        redisTemplate.afterPropertiesSet();
    }
}
```

# 定义数据源1
```java
@Configuration
@EnableCaching
public class DefaultRedisConfig extends RedisConfig {

    @Value("${spring.redis.database}")
    private int database;

    @Value("${spring.redis.host}")
    private String host;

    @Primary
    @Bean
    public RedisConnectionFactory defaultRedisConnectionFactory() {
        return createRedisConnectionFactory(database, host);
    }

    /**
     * 配置redisTemplate 注入方式使用@Resource(name="") 方式注入
     *
     * @return
     */
    @Bean(name = "defaultRedisTemplate")
    public RedisTemplate defaultRedisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(defaultRedisConnectionFactory());
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }
}
```

# 定义数据源2
```java
@Configuration
@EnableCaching
public class CacheRedisConfig extends RedisConfig {

    @Value("${spring.redis2.database}")
    private int database;

    @Value("${spring.redis2.host}")
    private String host;

    @Bean
    public RedisConnectionFactory cacheRedisConnectionFactory() {
        return createRedisConnectionFactory(database, host);
    }

    /**
     * 配置redisTemplate 注入方式使用@Resource(name="") 方式注入
     *
     * @return
     */
    @Bean(name = "cacheRedisTemplate")
    public RedisTemplate cacheRedisTemplate() {
        RedisTemplate template = new RedisTemplate();
        template.setConnectionFactory(cacheRedisConnectionFactory());
        setSerializer(template);
        template.afterPropertiesSet();
        return template;
    }
}
```

# 使用多数据源
```java
@Resource(name = "defaultRedisTemplate")
private RedisTemplate<String, Object> defaultRedisTemplate;

@Resource(name = "cacheRedisTemplate")
private RedisTemplate<String, Object> cacheRedisTemplate;
```
