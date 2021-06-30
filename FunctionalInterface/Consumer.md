# Consumer

## Consumer 接口

```java
@FunctionalInterface
public interface Consumer<T> {

    void accept(T t);

    default Consumer<T> andThen(Consumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> { accept(t); after.accept(t); };
    }
}
```

1. Consumer\<T\> 接口只有两个方法: 一个抽象方法，一个默认方法。其中，抽象方法接收一个泛型参数，无返回值；默认方法的参数和返回值均为 Consumer\<T\>。
2. 只要符合签名 void accept(T t) 的业务方法，都可以使用 Consumer\<T\> 描述。

## 示例

### 普通应用

```java
@Test
void normal() {
    Consumer<Integer> square = x -> System.out.println(x + "^2 = " + x * x);
    square.accept(3);

    Consumer<Integer> multi = x -> multi(x);
    multi.accept(3);

    Consumer<Integer> consumer1 = x -> System.out.println("x = " + x);
    Consumer<Integer> consumer2 = x -> {
        System.out.println("x + x = " + (x + x));
    };
    Consumer<Integer> consumer3 = x -> System.out.println("x * x = " + x * x);
    consumer1
            .andThen(consumer2)
            .andThen(consumer3)
            .accept(3);
}

private void multi(int x) {
    System.out.println(x + " * 2 = " + x * 2);
}
```

输出:

```
3^2 = 9
3 * 2 = 6
x = 3
x + x = 6
x * x = 9
```

### 异步回调

#### 自定义回调函数

```java
/**
 * 定义回调接口
 */
interface PrintEventHandler {
    void doHandle(Boolean value);
}

@Test
void async() {
    print(new PrintEventHandler() {
        @Override
        public void doHandle(Boolean value) {
            // 拿到回调结果这后，do something
            // ...
            System.out.println("sub thread call back: " + value);
        }
    });

    // 也可以简写成以下形式
//    print(x -> {
//        // 拿到回调结果这后，do something
//        // ...
//        System.out.println("sub thread call back: " + x);
//    });

    int i = 1;
    i += 10;
    System.out.println("main thread: i = " + i);
    try {
        Thread.sleep(60000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

/**
 * 打印任务
 *
 * @param printEventHandler
 */
private void print(PrintEventHandler printEventHandler) {
    Thread thread = new Thread(() -> {
        System.out.println("sub thread execute begin");
        Boolean value = false;
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                Thread.sleep(1000);
            }
            value = true;
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            value = false;
        }
        if (printEventHandler != null) {
            printEventHandler.doHandle(value);
        }
        System.out.println("sub thread execute complete");
    });
    thread.start();
}
```

输出:

```
main thread: i = 11
sub thread execute begin
0
1
2
3
4
5
6
7
8
9
sub thread call back: true
sub thread execute complete
```

#### 使用 Consumer\<T\> 实现回调

```java
@Test
void async() {
    print(x -> {
        // 拿到回调结果这后，do something
        // ...
        System.out.println("sub thread call back: " + x);
    });

    int i = 1;
    i += 10;
    System.out.println("main thread: i = " + i);

    try {
        Thread.sleep(60000);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }
}

private void print(Consumer<Boolean> consumer) {
    Thread thread = new Thread(() -> {
        System.out.println("sub thread execute begin");
        try {
            for (int i = 0; i < 10; i++) {
                System.out.println(i);
                Thread.sleep(1000);
            }
            consumer.accept(true);
        } catch (InterruptedException ex) {
            ex.printStackTrace();
            consumer.accept(false);
        }
        System.out.println("sub thread execute complete");
    });
    thread.start();
}
```

输出:

```
main thread: i = 11
sub thread execute begin
0
1
2
3
4
5
6
7
8
9
sub thread call back: true
sub thread execute complete
```

**注: Consumer\<T\> 实现回调比自定义回调函数的代码简洁一些。**
