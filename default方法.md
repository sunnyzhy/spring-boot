# default 方法

## 为什么要有 default 方法

引入 default 方法，是为了在不破坏 java 现有实现架构的情况下往接口里增加新方法。在优化接口的同时，避免跟现有的实现架构出现不兼容的问题。

接口一经发布，后期要想在接口中增加方法而不修改现有的实现类，这是不可能做到的。

为了解决上述问题，引入了一个新的概念，即虚拟扩展方法（Virtual extension methods），通常也称之为 defender 方法，它目前可以添加到接口中，为声明的方法提供默认的实现。

简单地说，现在接口可以有非抽象方法了。default 方法带来的好处是：往接口里新增一个 default 方法，而不破坏现有的实现架构。

## default 方法

default 修饰的方法***只能在接口中使用***，在接口中被 default 标记的方法为普通方法，***可以有方法体***。

## default 方法应用

### 实现类会继承接口中的 default 方法

```java
interface DefaultInterface {
    default void method() {
        System.out.println("这是一个 default 方法");
    }
}

class A implements DefaultInterface {

}
```


```java
@Test
void test1() {
    A a = new A();
    a.method();
}
```

输出：

```
这是一个 default 方法
```

### 如果一个类同时实现两个接口而这接口中都有相同的 default 方法

此时，该类必须重写接口中的 default 方法。因为类在继承接口中的 default 方法时，不知道应该继承哪一个接口中的 default 方法。

```java
interface DefaultInterfaceA {
    default void method() {
        System.out.println("这是一个 default 方法");
    }
}

interface DefaultInterfaceB {
    default void method() {
        System.out.println("这还是一个 default 方法");
    }
}

class A implements DefaultInterfaceA, DefaultInterfaceB {

    @Override
    public void method() {
        System.out.println("重写 default 方法");
    }
}
```

```java
@Test
void test1() {
    A a = new A();
    a.method();
}
```

输出：

```
重写 default 方法
```

### 子类继承父类，父类中的方法签名和接口中的 default 方法签名一样

子类会继承父类的方法，而不是继承接口中的方法。

```java
interface DefaultInterfaceA {
    default void method() {
        System.out.println("这是一个 default 方法");
    }
}

class B {
    public void method() {
        System.out.println("这是一个普通方法");
    }
}

class A extends B implements DefaultInterfaceA {

}
```

```java
@Test
void test1() {
    A a = new A();
    a.method();
}
```

输出：

```
这是一个普通方法
```
