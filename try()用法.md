# ```try()``` 用法

## 普通写法

-  需要在 ```finally``` 中手动关闭流

```java
@Test
void file() {
    FileInputStream inputStream = null;
    Scanner scanner = null;
    try {
        inputStream = new FileInputStream("./README.md");
        scanner = new Scanner(inputStream);
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            System.out.println(s);
        }
    } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
    } catch (IOException e) {
        throw new RuntimeException(e);
    } finally {
        if (scanner != null) {
            scanner.close();
        }
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
```

## ```try()``` 写法

- 凡是在 ```try()``` 的括号中声明的类都必须实现 ```java.io.Closeable``` 接口
- 在流程执行完成后，会自动关闭 try 中声明的流

```java
@Test
void file1() {
    try (FileInputStream inputStream = new FileInputStream("./README.md");
         Scanner scanner = new Scanner(inputStream)) {
        while (scanner.hasNext()) {
            String s = scanner.nextLine();
            System.out.println(s);
        }
    } catch (FileNotFoundException e) {
        throw new RuntimeException(e);
    } catch (IOException e) {
        throw new RuntimeException(e);
    }
}
```
