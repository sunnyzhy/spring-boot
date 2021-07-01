# Function

## Function 接口

```java
@FunctionalInterface
public interface Function<T, R> {

    R apply(T t);

    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    static <T> Function<T, T> identity() {
        return t -> t;
    }
}
```

符合签名 R apply(T t) 的方法，即参数为泛型，返回值也为泛型的方法，都可以使用 Function\<T, R\> 描述。

方法:
- R apply(T t), 参数为泛型，返回值也为泛型
- default \<V\> Function\<V, R\> compose(Function<? super V, ? extends T> before), 默认方法，先调用 before，再调用当前 function
- default \<V\> Function<T, V> andThen(Function<? super R, ? extends V> after), 默认方法，先调用当前 function，再调用 after
- static \<T\> Function<T, T> identity(), 静态方法，直接返回泛型参数

## 示例

### 系统应用

```java
@Test
void system() {
    Map<String, List<Integer>> map = new HashMap<>();
    String key = "id";
    List<Integer> list = map.get(key);
    if (list == null) {
        list = new ArrayList<>();
        map.put(key, list);
    }
    list.add(1);
    System.out.println(list);

    map.remove(key);
    // 上述代码可以简写为以下形式
    list = map.computeIfAbsent(key, x -> new ArrayList<>());
    list.add(1);
    System.out.println(list);

    List<Integer> numList = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        numList.add(i + 1);
    }
    // stream#map
    List<String> strList = numList.stream().map(x -> "num: " + x).collect(Collectors.toList());
    System.out.println(strList);
}
```

输出:

```
[1]
[1]
[num: 1, num: 2, num: 3, num: 4, num: 5]
```

### 普通应用
#### apply
```java
@Test
void applyTest() {
    Function<String, Integer> function = x -> Integer.parseInt(x);
    Integer x = function.apply("10");
    System.out.println(x);
}
```

输出:

```
10
```

#### apply
```java
@Test
void andThenTest() {
    Function<Integer, Integer> function = x -> {
        System.out.println("x * 10");
        return x * 10;
    };
    // 先执行 x * 10，再执行 x + 10
    function = function.andThen(x -> {
        System.out.println("x + 10");
        return x + 10;
    });
    Integer x = function.apply(5);
    System.out.println(x);
}
```

输出:

```
x * 10
x + 10
60
```

#### compose
```java
@Test
void composeTest() {
    Function<Integer, Integer> function = x -> {
        System.out.println("x * 10");
        return x * 10;
    };
    // 先执行 x + 10，再执行 x * 10
    function = function.compose(x -> {
        System.out.println("x + 10");
        return x + 10;
    });
    Integer x = function.apply(5);
    System.out.println(x);
}
```

输出:

```
x + 10
x * 10
150
```

#### identity
```java
@Test
void identityTest() {
    Function<Integer, Integer> function = Function.identity();
    Integer x = function.apply(5);
    System.out.println(x);
}
```

输出:

```
5
```

#### nomarl
```java
@Test
void normal() {
    Function<Integer, Integer> function = x -> multi(x);
    function = function.compose(x -> sub(x));
    function = function.andThen(x -> add(x));
    Integer value = function.apply(20);
    System.out.println(value);
}

/**
 * 乘法
 *
 * @param x
 * @return
 */
private Integer multi(Integer x) {
    Integer value = x * 2;
    System.out.println(x + " * 2 = " + value);
    return value;
}

/**
 * 减法
 *
 * @param x
 * @return
 */
private Integer sub(Integer x) {
    Integer value = x - 10;
    System.out.println(x + " - 10 = " + value);
    return value;
}

/**
 * 加法
 *
 * @param x
 * @return
 */
private Integer add(Integer x) {
    Integer value = x + 5;
    System.out.println(x + " + 5 = " + value);
    return value;
}
```

输出:

```
20 - 10 = 10
10 * 2 = 20
20 + 5 = 25
25
```

### 创建对象

```java
@Test
void instance() {
    // Function<String, Cat> function = x -> new Cat(x);
    Function<String, Cat> function = Cat::new;
    Cat cat1 = function.apply("Jim");
    System.out.println("cat1's name is: " + cat1.getName());
    Cat cat2 = function.apply("Tom");
    System.out.println("cat2's name is: " + cat2.getName());
    cat2.setName("Jhon");
    System.out.println("cat1's name is: " + cat1.getName());
    System.out.println("cat2's name is: " + cat2.getName());

    System.out.println("cat1's hash code is: " + cat1.hashCode());
    System.out.println("cat2's hash code is: " + cat2.hashCode());
}

@Data
class Cat {
    private String name;

    public Cat(String name) {
        this.name = name;
    }
}
```

输出:

```
cat1's name is: Jim
cat2's name is: Tom
cat1's name is: Jim
cat2's name is: Jhon
cat1's hash code is: 74537
cat2's hash code is: 2308088
```

**注：每次调用 apply 方法都会创建一个新的对象，所以两个对象打印的 hashcode 是不一样的！**
