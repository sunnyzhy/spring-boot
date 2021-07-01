# Predicate

## Predicate 接口

```java
@FunctionalInterface
public interface Predicate<T> {

    boolean test(T t);

    default Predicate<T> and(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) && other.test(t);
    }

    default Predicate<T> negate() {
        return (t) -> !test(t);
    }

    default Predicate<T> or(Predicate<? super T> other) {
        Objects.requireNonNull(other);
        return (t) -> test(t) || other.test(t);
    }

    static <T> Predicate<T> isEqual(Object targetRef) {
        return (null == targetRef)
                ? Objects::isNull
                : object -> targetRef.equals(object);
    }

    @SuppressWarnings("unchecked")
    static <T> Predicate<T> not(Predicate<? super T> target) {
        Objects.requireNonNull(target);
        return (Predicate<T>)target.negate();
    }
}
```

符合签名 boolean test(T t) 的方法，即参数为泛型，返回值为 boolean 的方法，都可以使用 Predicate\<T\> 描述。

方法:
- boolean test(T t), 判断参数是否满足条件
- default Predicate\<T\> and(Predicate<? super T> other), 默认方法，逻辑与
- default Predicate\<T\> negate(), 默认方法，逻辑非
- default Predicate\<T\> or(Predicate<? super T> other), 默认方法，逻辑或
- static \<T\> Predicate\<T\> isEqual(Object targetRef), 静态方法，判断数据是否与参数相等
- static \<T\> Predicate\<T\> not(Predicate<? super T> target)，静态方法，逻辑非，内部调用的是 negate()

## 示例

### 系统应用

```java
@Test
void system() {
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
        list.add(i + 1);
    }

    // 打印数组
    System.out.println(list);
    // 筛选数组中的偶数
    System.out.println(list.stream().filter(x -> x % 2 == 0).collect(Collectors.toList()));
    // 移除数组中的偶数
    list.removeIf(x -> x % 2 == 0);
    System.out.println(list);
}
```

输出:

```
[1, 2, 3, 4, 5]
[2, 4]
[1, 3, 5]
```

### 普通应用

#### test

```java
@Test
void testTest() {
    Predicate<Integer> predicate1 = x -> x == 5;
    boolean value = predicate1.test(5);
    System.out.println(value);

    Predicate<Integer> predicate2 = x -> check(x);
    value = predicate2.test(5);
    System.out.println(value);
}

private boolean check(Integer x) {
    return x > 10;
}
```

输出:

```
true
false
```

#### and/or

```java
@Test
void andOrTest() {
    Predicate<Integer> predicate1 = x -> x > 10;
    Predicate<Integer> predicate2 = x -> x < 20;
    Predicate<Integer> predicate3 = x -> x % 2 == 0;

    // x > 10 && x < 20 || x % 2 == 0
    Predicate<Integer> predicate = predicate1
            .and(predicate2)
            .or(predicate3);

    List<Integer> list = getList();

    // 筛选集合中满足条件的元素
    System.out.println(list.stream().filter(predicate).collect(Collectors.toList()));
    // 移除集合中满足条件的元素
    list.removeIf(predicate);
    System.out.println(list);
}

private List<Integer> getList() {
    List<Integer> list = new ArrayList<>();
    list.add(2);
    list.add(10);
    list.add(11);
    list.add(12);
    list.add(20);
    list.add(21);
    return list;
}
```

输出:

```
[2, 10, 11, 12, 20]
[21]
```

#### negate

```java
@Test
void negateTest() {
    Predicate<Integer> predicate1 = x -> x > 10;
    Predicate<Integer> predicate2 = x -> x < 20;
    Predicate<Integer> predicate3 = x -> x % 2 == 0;

    // 条件取非
    // !(x > 10 && x < 20 || x % 2 == 0)
    // 等价于
    // (x <= 10 || x >= 20) && x % 2 != 0
    Predicate<Integer> predicate = predicate1
            .and(predicate2)
            .or(predicate3)
            .negate();

    List<Integer> list = getList();

    // 筛选集合中满足条件的元素
    System.out.println(list.stream().filter(predicate).collect(Collectors.toList()));
    // 移除集合中满足条件的元素
    list.removeIf(predicate);
    System.out.println(list);
}
```

输出:

```
[21]
[2, 10, 11, 12, 20]
```

#### isEqual

```java
@Test
void isEqualTest() {
    List<Integer> list = getList();
    list.add(2);
    list.add(2);

    // 筛选集合中与设定数据相等的元素
    Predicate<Integer> predicate = Predicate.isEqual(2);
    long count = list.stream().filter(predicate).count();
    System.out.println(count);
}
```

输出:

```
3
```

#### not

```java
@Test
void notTest() {
    Predicate<Integer> predicate1 = x -> x > 10;
    Predicate<Integer> predicate2 = x -> x < 20;
    Predicate<Integer> predicate3 = x -> x % 2 == 0;

    List<Integer> list = getList();

    // 等价于 negate
    Predicate<Integer> predicate = predicate1
            .and(predicate2)
            .or(predicate3);
    predicate = Predicate.not(predicate);

    System.out.println(list.stream().filter(predicate).collect(Collectors.toList()));
    // 移除集合中满足条件的元素
    list.removeIf(predicate);
    System.out.println(list);
}
```

输出:

```
[21]
[2, 10, 11, 12, 20]
```
