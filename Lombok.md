# Profile

## ```@EqualsAndHashCode(callSuper = true)``` 注解的使用

在 Java 开发中，Lombok 提供的 ```@EqualsAndHashCode``` 注解用于自动生成 ```equals()``` 和 ```hashCode()``` 方法。默认情况下，这些方法只基于当前类的字段生成，而忽略父类的字段。如果父类的字段对对象的相等性判断有影响，就需要设置 ```callSuper = true```，以确保父类的字段也参与比较和哈希计算。

定义一个父类 Parent：

```java
@Data
public class Parent {
    private String code; // 父类字段
}
```

定义一个子类 Child：

```java
@Data
public class Child extends Parent {
    private String name; // 子类字段
}
```

单元测试：

```java
Child child1 = new Child();
child1.setCode("1");
child1.setName("child");

Child child2 = new Child();
child2.setCode("2");
child2.setName("child");

System.out.println(child1.equals(child2));
```

### Child 类没有添加 ```@EqualsAndHashCode(callSuper = true)``` 注解

在这种情况下，```equals()``` 和 ```hashCode()``` 方法只会比较 Child 类的 name 字段，而忽略 Parent 类的 code 字段。这就可能导致父类对象和子类对象在作比较时被错误地认为相等。

单元测试输出:

```
true
```

### Child 类添加 ```@EqualsAndHashCode(callSuper = true)``` 注解

修改子类 Child：

```java
@Data
@EqualsAndHashCode(callSuper = true)
public class Child extends Parent {
    private String name; // 子类字段
}
```

在这种情况下，```equals()``` 和 ```hashCode()``` 方法除了比较 Child 类的 name 字段，还会比较 Parent 类的 code 字段。这样才能够正确地比较父类对象和子类对象。

单元测试输出:

```
false
```

### 配置文件 ```lombok.config```

在这种情况下，无需给子类 Child 添加 ```@EqualsAndHashCode(callSuper = true)``` 注解，能且只能在 ```src/main/java``` 目录下添加配置文件 ```lombok.config```：

```config
config.stopBubbling=true
lombok.equalsAndHashCode.callSuper=call
```

单元测试输出:

```
false
```
