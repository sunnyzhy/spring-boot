# Feign 接口继承

## 1 在公共模块里定义父接口

***在公共模块里把 Feign 公用的函数签名单独封装起来，给各个子业务模块共用。***

- 父接口是一个单纯的接口，不用添加任何注解
- 函数签名添加 ```@RequestMapping``` 注解

```java
public interface BaseFeign {
    @GetMapping(value = "/test")
    List<Test> get(TestTo entity);
}
```

## 2 在业务模块里定义子接口

***在业务模块里声明需要用到的 Feign 的函数签名，因为不同业务模块用到的 Feign 的函数签名可能不尽相同。***

- 子接口继承父接口
- 子接口添加 ```@FeignClient``` 注解

```java
@FeignClient(name = "test-service")
public interface SubFeign extends BaseFeign {
    @PostMapping(value = "/test")
    void add(@Valid @RequestBody TestTo entity);

    @DeleteMapping(value = "/test")
    void delete(@Valid @RequestBody TestTo entity);

    @PutMapping(value = "/test")
    void update(@Valid @RequestBody TestTo entity);
}
```

## 3 在业务类里注入子接口

```java
@Service
@Slf4j
public class BusinessService {
    private final SubFeign subFeign;

    public BusinessService(SubFeign subFeign) {
        this.subFeign = subFeign;
    }
}
```
