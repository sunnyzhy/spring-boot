# 断言(assert)

## 启用断言

向 JVM 输入一个参数 ```-enableassertions```，也可以使用缩写 ```-ea```。

比如：```java -jar -ea xxx.jar```

## 使用断言

### java 中的 ```assert```

- ```assert [boolean表达式]```
   - 值为 true 时，程序从断言语句处继续执行;
   - 值为 false 时，程序从断言语句处停止执行。
   
   示例：
   
   ```java
    @Test
    void assertTest() {
        assertMethod(3);
    }

    @Test
    void assertTest2() {
        assertMethod(6);
    }

    void assertMethod(int i) {
        assert i < 5 ;
        System.out.println(i);
    }
   ```

   - ```assertTest()``` 输出：```3```
   - ```assertTest2()``` 输出：```java.lang.AssertionError```

- ```assert [boolean表达式 : message]```
   - 值为 true 时，程序从断言语句处继续执行;
   - 值为 false 时，程序从断言语句处停止执行，并拋出一个带有 message 信息的异常

   示例：
   
   ```java
    @Test
    void assertTest() {
        assertMethod(3);
    }

    @Test
    void assertTest2() {
        assertMethod(6);
    }

    void assertMethod(int i) {
        assert i < 5 : "i 必须小于 5";
        System.out.println(i);
    }
   ```
   
   - ```assertTest()``` 输出：```3```
   - ```assertTest2()``` 输出：```java.lang.AssertionError: i 必须小于 5```

### spring 中的 ```org.springframework.util.Assert```

```java
@Test
void assertTest() {
    assertMethod(null);
}

@Test
void assertTest2() {
    assertMethod(-1);
}

@Test
void assertTest3() {
    assertMethod(1);
}

void assertMethod(Integer i) {
    Assert.notNull(i, "i 不能为空");
    Assert.isTrue(i.intValue() > 0, "i 必须大于 0");
    System.out.println(i);
}
```

- ```assertTest()``` 输出：```java.lang.IllegalArgumentException: i 不能为空```
- ```assertTest2()``` 输出：```java.lang.IllegalArgumentException: i 必须大于 0```
- ```assertTest3()``` 输出：```1```
