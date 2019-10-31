在 Springboot 中使用 @Async，需要注意以下三点：

1. 在 SpringBootApplication 启动类上添加注解 @EnableAsync

2. 异步方法使用注解 @Async，返回值为 void 或者 Future，必须用 public 修饰

3. 异步方法和调用方法一定要写在不同的类中，如果写在同一个类中，是没有效果的
