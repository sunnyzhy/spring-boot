## 解决的问题
1. 解决 hash 的 key 中包含 16 进制的前缀问题

```
> hkeys aa:bb
1) "\xac\xed\x00\x05sr\x00\x11java.lang.Integer\x12\xe2\xa0\xa4\xf7\x81\x878\x02
\x00\x01I\x00\x05valuexr\x00\x10java.lang.Number\x86\xac\x95\x1d\x0b\x94\xe0\x8b
\x02\x00\x00xp\x00\x00\x00\x01"
```

2. 解决 key 和 value 中包含类型信息（如 java.lang.Integer 等）的前缀问题

## 添加 redis 依赖
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-redis</artifactId>
</dependency>
```

## 配置 RedisTemplate 的 Bean
```java
@Configuration
public class RedisTemplateConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);

        // 设置值 value 的序列化方式
        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);

        // 设置键 key 的序列化方式
        redisTemplate.setKeySerializer(new StringSerializer());
        redisTemplate.setHashKeySerializer(new StringSerializer());

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
}
```

## 配置 key 的序列化方式
```java
public class StringSerializer implements RedisSerializer<Object> {
    private final Charset charset;

    public StringSerializer() {
        this(StandardCharsets.UTF_8);
    }

    public StringSerializer(Charset charset) {
        this.charset = charset;
    }


    @Override
    public byte[] serialize(Object o) throws SerializationException {
        return o == null ? null : String.valueOf(o).getBytes(charset);
    }

    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        return bytes == null ? null : new String(bytes, charset);
    }
}
```

## 单元测试
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

@Test
void redis() {
	Map<Integer, User> map = new HashMap<>();
	User user = new User();
	user.setId(10);
	user.setName("小明");
	map.put(1, user);
	redisTemplate.opsForHash().putAll("aa:bb", map);
}
```
