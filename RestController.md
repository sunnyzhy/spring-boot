## GetMapping
### 1. 参数为基本类型
- 示例 1，用 @RequestParam 获取参数，注解里的参数名必须跟 http 请求里的参数名一致
```java
@GetMapping(value = "/query")
public String query(@RequestParam("name") String userName, @RequestParam("age") Integer userAge) {
    System.out.println(userName + ":" + userAge);
    return userName + ":" + userAge;
}
```

- 示例 2，参数名必须跟 http 请求里的参数名一致
```java
@GetMapping(value = "/query")
public String query(String name, Integer age) {
    System.out.println(name + ":" + age);
    return name + ":" + age;
}
```

调用

```bash
# curl "http://localhost:8100/query?name=aa&age=15"

# curl http://localhost:8100/query?name=aa\&age=15
```

注意，在 linux 中  & 代表命令在后台执行，所以，需要做以下处理:

1. 使用 "" 把请求体包起来
   ```bash
   # curl "http://localhost:8100/query?name=aa&age=15"
   ```

2. 使用 \ 转义
   ```bash
   # curl http://localhost:8100/query?name=aa\&age=15
   ```

### 2. 参数为数组
- 示例 1，用 @RequestParam 获取参数，注解里的参数名必须跟 http 请求里的参数名一致
```java
@GetMapping(value = "/query")
public String query(@RequestParam("names") String[] nameList) {
    System.out.println(StringUtils.join(nameList, ";"));
    return StringUtils.join(nameList, ";");
}
```

- 示例 2，参数名必须跟 http 请求里的参数名一致
```java
@GetMapping(value = "/query")
public String query(String[] names) {
    System.out.println(StringUtils.join(names, ";"));
    return StringUtils.join(names, ";");
}
```

调用

```bash
# curl http://20.0.0.106:8100/query?names=aa,bb,cc
```

### 3. 参数为简单对象
对象里的属性名必须跟 http 请求里的参数名一致

```java
@GetMapping(value = "/query")
public String query(User user) {
    System.out.println(user.getName() + ":" + user.getAge());
    return user.getName() + ":" + user.getAge();
}

@Data
public class User {
    private String name;
    private Integer age;
}
```

调用

```bash
# curl "http://localhost:8100/query?name=aa&age=15"

# curl http://localhost:8100/query?name=aa\&age=15
```
