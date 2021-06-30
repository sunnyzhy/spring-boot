# Supplier

## Supplier 接口

```java
@FunctionalInterface
public interface Supplier<T> {

    T get();
}
```

1. Supplier\<T\> 接口只包含一个无参的方法 T get()，返回一个泛型数据。
2. 业务方法的参数与 Supplier\<T\> 接口无关。
3. 业务方法的返回值类型必须跟 Supplier\<T\> 接口里指定的泛型一致。

## 示例

### 普通用法

```java
@Test
void contextLoads() {
    String value = doSupplier(() -> "Supplier<String> called");
    System.out.println(value);

    int num = 5;
    value = doSupplier(() -> {
        switch (num) {
            case 1:
                return "the arg is one";
            default:
                return "the arg is not one";
        }
    });
    System.out.println(value);

    value = doSupplier(() -> add(1, 2));
    System.out.println(value);

    Random random = new Random();
    int[] arr = new int[10];
    for (int i = 0; i < arr.length; i++) {
        arr[i] = random.nextInt(100);
    }
    value = doSupplier(() -> max(arr));
    System.out.println(value);
}

private String doSupplier(Supplier<String> supply) {
    return supply.get();
}

/**
 * 两个参数相加
 *
 * @param x
 * @param y
 * @return
 */
private String add(int x, int y) {
    return x + " + " + y + " = " + (x + y);
}

/**
 * 求数组元素的最大值
 *
 * @param arr
 * @return
 */
private String max(int[] arr) {
    StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("the array is: ");
    int max = arr[0];
    for (int i = 0; i < arr.length; i++) {
        if (arr[i] > max) {
            max = arr[i];
        }
        stringBuilder.append(arr[i]);
        if (i < arr.length - 1) {
            stringBuilder.append(", ");
        }
    }
    stringBuilder.append("\r\n");
    stringBuilder.append("the max number is " + max);
    return stringBuilder.toString();
}
```

输出:

```
Supplier<String> called
the arg is not one
1 + 2 = 3
the array is: 2, 62, 90, 2, 79, 71, 63, 78, 58, 7
the max number is 90
```

### 创建对象
```java
@Test
void instance() {
    // Cat::new 等价于 () -> new Cat()
    // Supplier<Cat> supplier = () -> new Cat();
    Supplier<Cat> supplier = Cat::new;
    
    Cat cat1 = supplier.get();
    System.out.println("cat1's name is: " + cat1.getName());
    
    Cat cat2 = supplier.get();
    System.out.println("cat2's name is: " + cat2.getName());
    
    cat2.setName("Jim");
    System.out.println("cat1's name is: " + cat1.getName());
    System.out.println("cat2's name is: " + cat2.getName());

    System.out.println("cat1's hash code is: " + cat1.hashCode());
    System.out.println("cat2's hash code is: " + cat2.hashCode());
}

@Data
class Cat {
    private String name = "Tom";
}
```

输出:

```
cat1's name is: Tom
cat2's name is: Tom
cat1's name is: Tom
cat2's name is: Jim
cat1's hash code is: 84333
cat2's hash code is: 74537
```

**注：每次调用 get 方法都会创建一个新的对象，所以两个对象打印的 hashcode 是不一样的！**
