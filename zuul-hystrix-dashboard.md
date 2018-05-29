# 添加依赖
```
<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-zuul</artifactId>
</dependency>

<dependency>
	<groupId>org.springframework.cloud</groupId>
	<artifactId>spring-cloud-starter-netflix-hystrix-dashboard</artifactId>
	<version>1.4.4.RELEASE</version>
</dependency>
```

# 添加 ServletRegistrationBean
```
@Configuration
public class HystrixBean {
    /**
     * 因为springboot的默认路径不是 "/hystrix.stream"，所以需要添加 ServletRegistrationBean
     * @return
     */
    @Bean
    public ServletRegistrationBean getServlet() {
        HystrixMetricsStreamServlet streamServlet = new HystrixMetricsStreamServlet();
        ServletRegistrationBean registrationBean = new ServletRegistrationBean(streamServlet);
        registrationBean.setLoadOnStartup(1);
        registrationBean.addUrlMappings("/hystrix.stream");
        registrationBean.setName("HystrixMetricsStreamServlet");
        return registrationBean;
    }
}
```

# 启用Zuul 和 Hystrix Dashboard
```
@EnableZuulProxy
@EnableHystrixDashboard
```

# 访问hystrix

http://localhost:9011/hystrix


1. 在浏览器中会看到以下提示:
```
Cluster via Turbine (default cluster): http://turbine-hostname:port/turbine.stream 
Cluster via Turbine (custom cluster): http://turbine-hostname:port/turbine.stream?cluster=[clusterName]
Single Hystrix App: http://hystrix-app:port/hystrix.stream 
```

2. 输入 http://localhost:9011/hystrix.stream , 之后点击 Monitor Stream

3. 如果没有请求会先显示 Loading ... ，访问 http://localhost:9011/hystrix.stream 也会不断地显示ping

4. 请求具体的服务 http://localhost:9011/user/info ，就可以看到监控的界面了
