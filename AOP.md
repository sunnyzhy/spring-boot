# AOP简单示例
## 定义接口
```java
/**
 * @author zhy
 * @date 2018/12/7 15:45
 **/
public interface UserDao {
    int addUser();

    void updateUser();

    void deleteUser();

    void findUser();
}
```

## 定义实现类
```java
/**
 * @author zhy
 * @date 2018/12/7 15:46
 **/
@Repository
public class UserDaoImp implements UserDao {
    @Override
    public int addUser() {
        System.out.println("add user ......");
        return 1;
    }

    @Override
    public void updateUser() {
        System.out.println("update user ......");
    }

    @Override
    public void deleteUser() {
        System.out.println("delete user ......");
    }

    @Override
    public void findUser() {
        System.out.println("find user ......");
    }
}
```

## 定义AOP的aspect类
```java
/**
 * @author zhy
 * @date 2018/12/7 15:47
 * 定义切面类，需要加上@Component、@Aspect这两个注解
 **/
@Component
@Aspect
public class UserAspect {
    /**
     * 定义切点
     */
    @Pointcut("execution(* com.zhy.aop.service.UserDao.*(..))")
    private void userPointcut(){}

    /**
     * 在切点方法之前执行
     */
    @Before(value = "userPointcut()")
    public void before(){
        System.out.println("前置通知....");
    }

    /**
     * 后置返回
     *      如果第一个参数为JoinPoint，则第二个参数为返回值的信息
     *      如果第一个参数不为JoinPoint，则第一个参数为returning中对应的参数
     * returning：限定了只有目标方法返回值与通知方法参数类型匹配时才能执行后置返回通知，否则不执行，
     *            参数为Object类型将匹配任何目标返回值
     */
    @AfterReturning(value = "userPointcut()",returning = "returnVal")
    public void afterReturning(Object returnVal){
        System.out.println("后置通知...."+returnVal);
    }


    /**
     * 环绕通知：
     *   注意:Spring AOP的环绕通知会影响到AfterThrowing通知的运行,不要同时使用
     *
     *   环绕通知非常强大，可以决定目标方法是否执行，什么时候执行，执行时是否需要替换方法参数，执行完毕是否需要替换返回值。
     *   环绕通知第一个参数必须是org.aspectj.lang.ProceedingJoinPoint类型
     */
    @Around(value = "userPointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        System.out.println("环绕通知前....");
        Object obj= (Object) joinPoint.proceed();
        System.out.println("环绕通知后....");
        return obj;
    }

    /**
     * 后置异常通知
     *  定义一个名字，该名字用于匹配通知实现方法的一个参数名，当目标方法抛出异常返回后，将把目标方法抛出的异常传给通知方法；
     *  throwing:限定了只有目标方法抛出的异常与通知方法相应参数异常类型时才能执行后置异常通知，否则不执行，
     *            对于throwing对应的通知方法参数为Throwable类型将匹配任何异常。
     * @param joinPoint
     * @param exception
     */
    @AfterThrowing(value = "userPointcut()",throwing = "exception")
    public void afterThrowable(JoinPoint joinPoint, Throwable exception){
        System.out.println("exception:"+exception.getMessage());
    }

    /**
     * 在切点方法之后执行,无论什么情况下都会执行的方法
     */
    @After(value = "userPointcut()")
    public void after(){
        System.out.println("最终通知....");
    }
}
```

## 测试类
```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class AopApplicationTests {
    @Autowired
    private UserDao userDao;

    @Test
    public void contextLoads() {
        userDao.addUser();
    }
}
```

```
环绕通知前....
前置通知....
add user ......
环绕通知后....
最终通知....
后置通知....1
```

# 定义切点
- **直接用execution定义匹配表达式**
```java
    @Before(value = "execution(* com.zhy.aop.service.UserDao.*(..))")
    public void before(){
        System.out.println("前置通知....");
    }
```

- **使用@Pointcut注解**

```java
    /**
     * 定义切点
     */
    @Pointcut("execution(* com.zhy.aop.service.UserDao.*(..))")
    private void userPointcut(){}

    /**
     * 在切点方法之前执行
     */
    @Before(value = "userPointcut()")
    public void before(){
        System.out.println("前置通知....");
    }
```

# 切点表达式
## 通配符
```- \*``` ：匹配任意数量的字符

```java
//匹配com.zhy.aop.service包及其子包中所有类的所有方法
within(com.zhy.aop.service..*)
//匹配以set开头，参数为int类型，任意返回值的方法
execution(* set*(int))
```

```- \..``` ：匹配方法定义中的任意数量的参数，此外还匹配类定义中的任意数量包

```java
//任意返回值，任意名称，任意参数的公共方法
execution(public * *(..))
//匹配com.zhy.aop.service包及其子包中所有类中的所有方法
within(com.zhy.aop.service..*)
```

```- \+``` ：只能放在类后面，表明本类及所有子类

```java
//匹配实现了DaoUser接口的所有子类的方法
within(com.zhy.aop.service.DaoUser+)
```

示例:

```
execution(* com.sample.service.impl..*.*(..))
```

解释如下：

|符号|含义|
|--|--|
|```execution()```|表达式的主体|
|第一个 ```*```|表示返回值的类型任意|
|```com.sample.service.impl```|AOP所切的服务的包名|
|包名后面的 ```..```|表示当前包及子包|
|第二个 ```*``` |表示类名，```*```即所有类。此处可以自定义|
|```.*(..)```|表示任何方法名，括号表示参数，两个点表示任何参数类型|

## 类型签名表达式within
- **语法**

**within(类路径)，主要用来限定类。**
```java
within(<type name>)
```

- **示例**

```java
//匹配com.zhy.aop.service包及其子包中所有类中的所有方法
@Pointcut("within(com.zhy.aop.service..*)")

//匹配UserDaoImpl类中所有方法
@Pointcut("within(com.zhy.aop.service.UserDaoImpl)")

//匹配UserDaoImpl类及其子类中所有方法
@Pointcut("within(com.zhy.aop.service.UserDaoImpl+)")

//匹配所有实现UserDao接口的类的所有方法
@Pointcut("within(com.zhy.aop.service.UserDao+)")
```

## 方法签名表达式execution
- **语法**

**execution(方法修饰符 返回类型 方法全限定名(参数))，主要用来匹配整个方法签名和返回值。**
```java
//scope ：方法作用域，如public,private,protect
//returnt-type：方法返回值类型
//fully-qualified-class-name：方法所在类的完全限定名称
//parameters 方法参数
execution(<scope> <return-type> <fully-qualified-class-name>.*(parameters))
```

- **示例**

```java
//匹配UserDaoImpl类中的所有方法
@Pointcut("execution(* com.zhy.aop.service.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中的所有公共的方法
@Pointcut("execution(public * com.zhy.aop.service.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中的所有公共方法并且返回值为int类型
@Pointcut("execution(public int com.zhy.aop.service.UserDaoImpl.*(..))")

//匹配UserDaoImpl类中第一个参数为int类型的所有公共的方法
@Pointcut("execution(public * com.zhy.aop.service.UserDaoImpl.*(int , ..))")
```

# 通知函数
## 前置通知 @Before
前置通知通过@Before注解进行标注，并可直接传入切点表达式的值，该通知在目标函数执行前执行，注意JoinPoint，是Spring提供的静态变量，通过joinPoint 参数，可以获取目标对象的信息,如类名称、方法参数、方法名称等，该参数是可选的。

## 后置通知 @AfterReturning 
通过@AfterReturning注解进行标注，该函数在目标函数执行完成后执行，并可以获取到目标函数最终的返回值returnVal，当目标函数没有返回值时，returnVal将返回null，必须通过returning = “returnVal”注明参数的名称而且必须与通知函数的参数名称相同。请注意，在任何通知中这些参数都是可选的，需要使用时直接填写即可，不需要使用时，可以不用声明出来。

## 异常通知 @AfterThrowing
该通知只有在异常时才会被触发，并由throwing来声明一个接收异常信息的变量，同样异常通知也用于Joinpoint参数，需要时加上即可。

## 最终通知 @After
该通知有点类似于finally代码块，只要应用了无论什么情况下都会执行。

## 环绕通知 @Around 
环绕通知既可以在目标方法前执行也可在目标方法之后执行，更重要的是环绕通知可以控制目标方法是否指向执行，但即使如此，我们应该尽量以最简单的方式满足需求，在仅需在目标方法前执行时，应该采用前置通知而非环绕通知。第一个参数必须是ProceedingJoinPoint，通过该对象的proceed()方法来执行目标函数，proceed()的返回值就是环绕通知的返回值。同样的，ProceedingJoinPoint对象也是可以获取目标对象的信息,如类名称、方法参数、方法名称等等。

## 通知传递参数
在Spring AOP中，除了execution和bean指示符不能传递参数给通知方法，其他指示符都可以将匹配的方法相应参数或对象自动传递给通知方法。获取到匹配的方法参数后通过”argNames”属性指定参数名。如下，需要注意的是args(指示符)、argNames的参数名与before()方法中参数名 必须保持一致即param。
```java
@Before(value="args(param)", argNames="param") //明确指定了    
public void before(int param) {    
    System.out.println("param:" + param);    
}  
```

当然也可以直接使用args指示符不带argNames声明参数，如下：
```java
@Before("execution(public * com.zhy..*.addUser(..)) && args(userId,..)")  
public void before(int userId) {  
    //调用addUser的方法时如果与addUser的参数匹配则会传递进来会传递进来
    System.out.println("userId:" + userId);  
}  
```
args(userId,..)该表达式会保证只匹配那些至少接收一个参数而且传入的类型必须与userId一致的方法，记住传递的参数可以简单类型或者对象，而且只有参数和目标方法也匹配时才有会有值传递进来。

# Aspect优先级
**在同一个切面中，通知函数将根据在类中的声明顺序执行；在不同的切面中，通知函数将根据优先级顺序执行，@Before注解的通知函数，最高优先级的先执行，@AfterReturning注解的通知函数，最高优先级的最后执行，order值越小，优先级越高。**

```java
@Component
@Aspect
public class UserAspectOne implements Ordered {
    @Pointcut("execution(* com.zhy.aop.service.UserDao.addUser(..))")
    private void userPointcut(){}

    @Before(value = "userPointcut()")
    public void beforeOne(){
        System.out.println("AspectOne..前置通知..执行顺序1");
    }

    @Before(value = "userPointcut()")
    public void beforeTwo(){
        System.out.println("AspectOne..前置通知..执行顺序2");
    }

    @AfterReturning(value = "userPointcut()",returning = "returnVal")
    public void afterReturningOne(Object returnVal){
        System.out.println("AspectOne..后置通知..执行顺序1.."+returnVal);
    }

    @AfterReturning(value = "userPointcut()",returning = "returnVal")
    public void afterReturningTwo(Object returnVal){
        System.out.println("AspectOne..后置通知..执行顺序2.."+returnVal);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

```java
@Component
@Aspect
public class UserAspectTwo implements Ordered {
    @Pointcut("execution(* com.zhy.aop.service.UserDao.addUser(..))")
    private void userPointcut(){}

    @Before(value = "userPointcut()")
    public void beforeOne(){
        System.out.println("AspectTwo..前置通知..执行顺序1");
    }

    @Before(value = "userPointcut()")
    public void beforeTwo(){
        System.out.println("AspectTwo..前置通知..执行顺序2");
    }

    @AfterReturning(value = "userPointcut()",returning = "returnVal")
    public void afterReturningOne(Object returnVal){
        System.out.println("AspectTwo..后置通知..执行顺序1.."+returnVal);
    }

    @AfterReturning(value = "userPointcut()",returning = "returnVal")
    public void afterReturningTwo(Object returnVal){
        System.out.println("AspectTwo..后置通知..执行顺序2.."+returnVal);
    }

    @Override
    public int getOrder() {
        return 2;
    }
}
```

```
AspectOne..前置通知..执行顺序1
AspectOne..前置通知..执行顺序2
AspectTwo..前置通知..执行顺序1
AspectTwo..前置通知..执行顺序2
add user ......
AspectTwo..后置通知..执行顺序1..1
AspectTwo..后置通知..执行顺序2..1
AspectOne..后置通知..执行顺序1..1
AspectOne..后置通知..执行顺序2..1
```
