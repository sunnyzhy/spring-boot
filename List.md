# 排序
## User类
```java
@Data
public class User {
    private int id;
    private String name;
}
```

## 单元测试
```java
    private String name = "user";
    private Random random = new Random();

    @Test
    public void listSort() {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            User user = new User();
            user.setId(random.nextInt(100));
            user.setName(name + user.getId());
            userList.add(user);
        }
        // 升序
        userList.sort((o1, o2) -> o1.getId() - o2.getId());
        // 降序
        userList.sort((o1, o2) -> o2.getId() - o1.getId());
        // 升序
        userList.sort(Comparator.comparing(User::getId));
    }
```

# 去重&交集
## User类
```java
@Data
public class User {
    private Integer id;
    private String name;
  
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        User user = (User) obj;
        return Objects.equals(id, user.id);
    }
}
```

## 去重
```
distinct()是基于hashCode()和equals()工作的，所以，如果使用distinct去重，就需要重写hashcode和equals方法。
```

```java
List<User> userList = Lists.newArrayList(
        new User(1, "a"),
        new User(1, "b"),
        new User(2, "b"),
        new User(1, "a"));
List<User> users = userList
        .parallelStream()
        .distinct()
        .collect(Collectors.toList());
```

## 交集
```java
List<User> userList1 = Lists.newArrayList(
        new User(1, "a"),
        new User(2, "b"),
        new User(3, "c"));
List<User> userList2 = Lists.newArrayList(
        new User(3, "c"),
        new User(4, "d"),
        new User(5, "f"));
List<User> intersectionList = userList1
        .parallelStream()
        .filter(item -> userList2
               .parallelStream()
               .anyMatch(x -> x.getId().equals(item.getId())))
       .collect(Collectors.toList());
```
