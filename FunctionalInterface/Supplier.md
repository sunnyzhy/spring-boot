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

```java
@Test
void contextLoads() {
    String value = doSupplier(() -> nullArg());
    System.out.println(value);

    value = doSupplier(() -> numToString(5));
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
 * 直接返回一个字符串
 * @return
 */
private String nullArg() {
    return "the arg is null";
}

/**
 * 数字转字符串
 * @param num
 * @return
 */
private String numToString(int num) {
    switch (num) {
        case 1:
            return "the arg is one";
        default:
            return "the arg is not one";
    }
}

/**
 * 两个参数相加
 * @param x
 * @param y
 * @return
 */
private String add(int x, int y) {
    return "x + y = " + (x + y);
}

/**
 * 求数组元素的最大值
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
