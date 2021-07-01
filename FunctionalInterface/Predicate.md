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
    Predicate<Integer> predicate1 = x -> x > 10;
    Predicate<Integer> predicate2 = x -> x < 20;
    Predicate<Integer> predicate3 = x -> x % 2 == 0;

    // and, or
    // x > 10 && x < 20 || x % 2 == 0
    Predicate<Integer> predicate = predicate1
            .and(predicate2)
            .or(predicate3);

    List<Integer> list = new ArrayList<>();
    list.add(2);
    list.add(10);
    list.add(11);
    list.add(12);
    list.add(20);
    list.add(21);

    List<Integer> list1 = new ArrayList<>();
    list1.addAll(list);

    List<Integer> list2 = new ArrayList<>();
    list2.addAll(list);

    List<Integer> list3 = new ArrayList<>();
    list3.addAll(list);


    // 筛选集合中满足条件的元素
    System.out.println("and()-or():" + list.stream().filter(predicate).collect(Collectors.toList()));
    // 移除集合中满足条件的元素
    list.removeIf(predicate);
    System.out.println("and()-or():" + list);

    // negate
    // 条件取非
    // !(x > 10 && x < 20 || x % 2 == 0)
    // 等价于
    // (x <= 10 || x >= 20) && x % 2 != 0
    predicate = predicate.negate();

    // 筛选集合中满足条件的元素
    System.out.println("negate():" + list1.stream().filter(predicate).collect(Collectors.toList()));
    // 移除集合中满足条件的元素
    list1.removeIf(predicate);
    System.out.println("negate():" + list1);

    // isEqual
    // 筛选集合中与设定数据相等的元素
    Predicate<Integer> predicateEqual = Predicate.isEqual(2);

    list2.add(2);
    list2.add(2);
    long count = list2.stream().filter(predicateEqual).count();
    System.out.println("isEqual(): " + count);

    // not
    // 等价于negate
    Predicate<Integer> predicateNot = predicate1
            .and(predicate2)
            .or(predicate3);
    predicateNot = Predicate.not(predicateNot);
    System.out.println("not(): " + list3.stream().filter(predicateNot).collect(Collectors.toList()));
}
```

输出:

```
and()-or():[2, 10, 11, 12, 20]
and()-or():[21]
negate():[21]
negate():[2, 10, 11, 12, 20]
isEqual(): 3
not(): [21]
```

### 普通应用

```java
@Test
void normal() {
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
