# pom.xml
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

# application.yml
```
spring:
  redis:
    database: 0
    timeout: 60000
    cluster:
      nodes:
        - 127.0.0.1:6300
        - 127.0.0.1:6301
        - 127.0.0.1:6302
        - 127.0.0.1:6303
        - 127.0.0.1:6304
        - 127.0.0.1:6305
    jedis:
      pool:
        max-active: 300
        max-wait: -1
        max-idle: 100
        min-idle: 20
````

# 单元测试
## 示例代码
```java
@Autowired
private RedisTemplate<String, Object> redisTemplate;

@Test
public void redisCluster() {
    String key = "name";
    redisTemplate.opsForValue().set(key, "zhy");
    String value = redisTemplate.opsForValue().get(key).toString();
    System.out.println(value);
    key = "age";
    redisTemplate.opsForValue().set(key, 21);
    value = redisTemplate.opsForValue().get(key).toString();
    System.out.println(value);
}
```

输出：
```
zhy
21
```

## 用命令行查看
```
>redis-cli -h 127.0.0.1 -c -p 6300

127.0.0.1:6300> get name
-> Redirected to slot [5798] located at 127.0.0.1:6301
"\"zhy\""

127.0.0.1:6301> get age
-> Redirected to slot [741] located at 127.0.0.1:6300
"21"
```
