# List

## 排序
### User类
```java
@Data
public class User {
    private int id;
    private String name;
}
```

### 单元测试
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

## 去重&交集
### User类
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

### 去重
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

### 交集
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

## 分页
### User类
```java
@Data
public class User {
    private int id;
    private String name;
}
```

### 单元测试
```java
    @Test
    public void listPage() {
        List<User> userList = new ArrayList<>();
        for (int i = 0; i < 107; i++) {
            User user = new User();
            user.setId(i);
            user.setName(name + user.getId());
            userList.add(user);
        }
        int total = userList.size();
        Page page = initPage(6, 10, total);
        List<User> list = userList.subList(page.getFromIndex(), page.getToIndex());
        System.out.println(list);
    }

    private Page initPage(int pageIndex, int pageSize, int total) {
        Page page = new Page();
        page.setFromIndex((pageIndex - 1) * pageSize);
        page.setToIndex(total < pageIndex * pageSize ? total : pageIndex * pageSize);
        return page;
    }
```

### 删除list中所有null值
```java
@Test
public void filterList1() {
    List<Integer> list = Lists.newArrayList(null, 1, 2, null, 3, null);
    List<Integer> filterList = list.parallelStream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    System.out.println(filterList);
}

@Test
public void filterList2() {
    List<Integer> list = Lists.newArrayList(null, 1, 2, null, 3, null);
    list.removeIf(Objects::isNull);
    System.out.println(list);
}
```

### 对象转List
```java
public void list11() {
    Integer i = 10;
    List<Integer> listI = Arrays.asList(i);
    System.out.println(listI);
    Integer j = 11;
    List<Integer> listJ = Collections.singletonList(j);
    System.out.println(listJ);
}
```

## 初始化List
### 使用 List.add
```java
List<Integer> list = new ArrayList<>();
list.add(1);
list.add(2);
list.add(3);
```

### 使用 {{}} 双括号
```java
List<Integer> list = new ArrayList<Integer>() {{
    add(1);
    add(2);
    add(3);
}};
```

### 使用 Arrays.asList
```java
List<Integer> list = Arrays.asList(1, 2, 3);
// 不支持增、删元素
// list.add(4);
// list.remove(1);
```

### 使用 Stream
```java
List<Integer> list = Stream.of(1, 2, 3).collect(Collectors.toList());
```

## 删除集合元素

### 删除List元素
```java
private List<Integer> list = new ArrayList<>();

@Test
public void list() {
	for (int i = 0; i < 10; i++) {
		list.add(i);
	}
	System.out.println(list);
	for (int i = 0; i < 10; i++) {
		if (i % 3 == 0) {
			remove(i);
		}
	}
	System.out.println(list);
}

private void remove(Integer key) {
	Iterator<Integer> iterator = list.iterator();
	while (iterator.hasNext()) {
		Integer k = iterator.next();
		if (k.equals(key)) {
			iterator.remove();
		}
	}
}
```

输出:
```
[0, 1, 2, 3, 4, 5, 6, 7, 8, 9]
[1, 2, 4, 5, 7, 8]
```

### 删除Map元素
```java
private Map<Integer, String> map = new ConcurrentHashMap<>();

@Test
public void map() {
	for (int i = 0; i < 10; i++) {
		map.put(i, "map" + i);
	}
	System.out.println(map);
	for (int i = 0; i < 10; i++) {
		if (i % 3 == 0) {
			remove(i);
		}
	}
	System.out.println(map);
}

private void remove(Integer key) {
	Iterator<Integer> iterator = map.keySet().iterator();
	while (iterator.hasNext()) {
		Integer k = iterator.next();
		if (k.equals(key)) {
			iterator.remove();
		}
	}
}
```

输出:
```
{0=map0, 1=map1, 2=map2, 3=map3, 4=map4, 5=map5, 6=map6, 7=map7, 8=map8, 9=map9}
{1=map1, 2=map2, 4=map4, 5=map5, 7=map7, 8=map8}
```

## FAQ

### 迭代删除 ```Iterator.remove()``` 时异常 ```java.lang.IllegalStateException```

使用迭代删除的时候，一定要先使用 ```Iterator.next()``` 方法迭代出集合中的元素，然后才能调用 ```Iterator.remove()``` 方法删除迭代的元素，否则就会抛出导演 ```java.lang.IllegalStateException```
