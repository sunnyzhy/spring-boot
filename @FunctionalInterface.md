# 函数式接口的定义

在java8中，满足下面任意一个条件的接口都是函数式接口：

1. 被@FunctionalInterface注释的接口，满足@FunctionalInterface注释的约束；

2. 没有被@FunctionalInterface注释的接口，但是满足@FunctionalInterface注释的约束。

 

@FunctionalInterface注释的约束：

1. 接口有且只能有个一个抽象方法，只有方法定义，没有方法体；

2. 在接口中覆写Object类中的public方法，不算是函数式接口的方法。

# 定义函数式接口
```java
/**
 * 接口一
 */
@FunctionalInterface
public interface MyFunctionalInterface<P, T> {
    T getInstance(P p);
    
    @Override
    String toString();
    
    @Override
    boolean equals(Object obj);
    
    /**
     * 在接口中编写default修饰的方法时，必须有方法体
     */
    default void show() {
        System.out.println("default method execute.");
    }
}

/**
 * 接口二
 */
@FunctionalInterface
public interface MyFunctionalInterface<P, T> {
    T getInstance(P p);
    
    default void show() {
        System.out.println("default method execute.");
    }
}

/**
 * 接口三
 */
public interface MyFunctionalInterface<P, T> {
    T getInstance(P p);
    
    default void show() {
        System.out.println("default method execute.");
    }
}
```

# 定义实体类
```java
@Data
public class Student {
    private String name;

    public Student(String name) {
        this.name = name;
    }

    public String method(String name){
        return "student:" + name;
    }

    public static String staticMethod(String name){
        return "static student:" + name;
    }
}
```

# 单元测试
```java
    @Test
    public void functionalInterfaceTest() {
        /**
         * 1、lambda表达式
         * 这种形式最为直观，lambda表达式，接收一个String类型的参数，返回一个Student类型的对象。
         */
        MyFunctionalInterface<String, Student> student = name -> new Student(name);
        Student stu = student.getInstance("Tom");
        System.out.println(stu);

        /**
         * 2、构造方法引用
         * 构造函数的结构：接收输入参数，然后返回一个对象。
         */
        MyFunctionalInterface<String, Student> student1 = Student::new;
        Student stu1 = student1.getInstance("Tom2");
        System.out.println(stu1);

        MyFunctionalInterface<String, String> student3 = name -> "student:" + name;
        String message = student3.getInstance("Tom3");
        System.out.println(message);

        /**
         * 调用对象的方法
         */
        MyFunctionalInterface<String, String> student4 = stu1::method;
        String message2 = student4.getInstance("Tom4");
        System.out.println(message2);

        /**
         * 调用类的静态方法
         */
        MyFunctionalInterface<String, String> student5 = Student::staticMethod;
        String message3 = student5.getInstance("Tom5");
        System.out.println(message3);
        
        /**
         * 调用接口的默认方法
         */
        student5.show();
    }

// 输出
Student(name=Tom)
Student(name=Tom2)
student:Tom3
student:Tom4
static student:Tom5
default method execute.
```
