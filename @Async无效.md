在 Springboot 中使用 @Async，需要注意以下几点：

1. 在 SpringBootApplication 启动类上添加注解 @EnableAsync
2. 异步方法使用注解 @Async，返回值为 void 或者 Future，必须用 public 修饰
3. 异步方法和调用方法一定要写在不同的类中，如果写在同一个类中，是没有效果的。**【易出错】**
4. 没有经过 Spring 的代理类（如：@Service、@Component 等注解修饰的类）。因为 @Transactional 和 @Async 注解的实现都是基于 Spring 的 AOP ，而 AOP 的实现是基于动态代理模式实现的。如果调用方法的是对象本身而不是代理对象，因为没有经过 Spring 容器，所以注解无效。**【易出错】**
   - 异步方法必须通过代理机制来触发
   - @Async 注解方法的类对象必须是 Spring 容器管理的 bean 对象

解决方法如下：

- 注解的方法必须用 public 修饰
- 异步方法一定要从类的外部调用，类的内部调用是无效的
- 如果需要从类的内部调用，需要先获取其代理类，如：```ApplicationContext.getBean()```
