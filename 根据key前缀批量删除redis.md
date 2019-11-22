# RedisConfig
```java
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(factory);
        redisTemplate.afterPropertiesSet();
        setSerializer(redisTemplate);
        return redisTemplate;
    }

    private void setSerializer(RedisTemplate<String, Object> template) {
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jackson2JsonRedisSerializer.setObjectMapper(om);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(jackson2JsonRedisSerializer);
    }
}
```

# 单元测试
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

/**
 * 构造数据
 */
@Test
public void createData() {
    Random random = new Random();
    Map<String, Integer> map = new HashMap<>();
    for (int i = 1; i <= 12000; i++) {
        map.put("a:" + random.nextInt(3) + ":" + i, i);
        if (i % 2000 == 0) {
            redisTemplate.opsForValue().multiSet(map);
            map.clear();
        }
    }
}

/**
 * 根据key前缀批量删除
 */
@Test
public void batchDelete() {
    String key = "a:*";
    Set<String> keys = redisTemplate.keys(key);
    List<String> list = new ArrayList<>();
    for (String k : keys) {
        list.add(k);
        if (list.size() == 2000) {
            redisTemplate.delete(list);
            list.clear();
        }
    }
    if (!list.isEmpty()) {
        redisTemplate.delete(list);
        list.clear();
    }
}
```
