# eureka-server(eureka注册中心)
## 1. 添加依赖
```xml
<dependency> 
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-netflix-eureka-server</artifactId>
</dependency>
```

## 2. 配置用户名和密码
```
spring.security.user.name=your_username
spring.security.user.password=your_password
```

# eureka-client
## 1. 添加依赖
```xml
<dependency>
   <groupId>org.springframework.cloud</groupId>
   <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
</dependency>
```

## 2. 连接eureka注册中心
```
eureka.client.serviceUrl.defaultZone=http://${security.user.name}:${security.user.password}@127.0.0.1:${server.port}/eureka/
```

# 解决security拦截部分url的问题
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    /**
     * 方法1
     * 
     * 忽略swagger相关的url，否则无法生成离线的接口文档
     * 如果只需要浏览在线的接口文档，则不需要忽略该项配置
     * @param web
     * @throws Exception
     */
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/swagger-ui.html")
                .antMatchers("/webjars/**")
                .antMatchers("/v2/**")
                .antMatchers("/swagger-resources/**");
    }

    /**
     * 方法2
     * 
     * 解决: SpringBoot项目访问任意接口都跳转到login登录接口或页面
     * @param http
     * @throws Exception
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .and().formLogin().permitAll()
                .and().logout().permitAll();
    }

}
```
