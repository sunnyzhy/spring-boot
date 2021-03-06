# Config Server
## pom.xml
```xml

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-config-server</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
            <version>2.0.0.RELEASE</version>
        </dependency>
```

## application.yml
```
server:
  port: 8090
spring:
  application:
    name: springcloud-config-server
  rabbitmq:
    host: 127.0.0.1
    username: admin
    password: admin
  cloud:
    config:
      server:
        git:
          uri: https://github.com/sunnyzhy/springcloud-config.git
          username: sunnyzhy
          password: ******
eureka:
  client:
    service-url:
      defaultZone: http://localhost:9010/eureka/
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

## 启动服务
访问 http://localhost:15672 ，可以看到交换机中多出一列

![](images/spring-cloud-config-1.png)

队列中也多出一列

![](images/spring-cloud-config-2.png)

# Config Client
## pom.xml
```xml
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-config</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-netflix-eureka-client</artifactId>
            <version>2.0.2.RELEASE</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-starter-bus-amqp</artifactId>
            <version>2.0.0.RELEASE</version>
        </dependency>
```

## application.yml
```
server:
  port: 8091
spring:
  application:
    name: cloud-config
  rabbitmq:
    host: 127.0.0.1
    username: admin
    password: admin
```

## 启动服务
开启两个客户端服务（端口分别为8091、8092），访问 http://localhost:15672 ，可以看到队列中多出两列

![](images/spring-cloud-config-3.png)

1. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.1
```

2. 访问http://localhost:8092/get ,显示如下内容

```
dev-1.1
```

3. 在git仓库中修改version的值为dev-1.2

4. 在**Config Server**端，用REST Client发送post请求http://localhost:8090/actuator/bus-refresh

5. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.2
```

6. 访问http://localhost:8092/get ,显示如下内容

```
dev-1.2
```

7. 此时并没有重启服务，获取的配置内容却已经自动刷新了。但是如果每次改完配置文件之后都需要手动去向配置服务发送 post 请求的话，那也是不太可取的。这个时候就可以用到 **GitHub 的 Webhooks**。

# Webhook
## 概述
Webhook是一种web回调或者http的push API，是向APP或者其他应用提供实时信息的一种方式。Webhook在数据产生时立即发送数据，也就是能实时收到数据。这一种不同于典型的API，需要用了实时性需要足够快的轮询。这无论是对生产还是对消费者都是高效的，唯一的缺点是初始建立困难。

Webhook有时也被称为反向API，因为他提供了API规则，你需要设计要使用的API。Webhook将向你的应用发起http请求，典型的是post请求，应用程序由请求驱动。

Github上对Webhook的描述
```
Webhooks allow external services to be notified when certain events happen. When the specified events happen, we’ll send a POST request to each of the URLs you provide. 
```

## 内网穿透
推荐使用natapp，[natapp教程](https://natapp.cn/article/natapp_newbie "https://natapp.cn/article/natapp_newbie")

## 配置
1. 配置natapp

隧道端口配置为8090

2. 配置webhook

![](images/spring-cloud-config-4.png)

3. 改进Config Server

- 新增自定义的Wrapper类

```java
public class CustometRequestWrapper extends HttpServletRequestWrapper {
    public CustometRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public ServletInputStream getInputStream() {
        byte[] bytes = new byte[0];
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);

        return new ServletInputStream() {
            @Override
            public boolean isFinished() {
                return byteArrayInputStream.read() == -1 ? true : false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {

            }

            @Override
            public int read() {
                return byteArrayInputStream.read();
            }
        };
    }
}
```

- 重写Filter

```java
@Component
public class ConfigFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)servletRequest;

        String url = new String(httpServletRequest.getRequestURI());

        //只过滤/actuator/bus-refresh请求
        if (!url.endsWith("/bus-refresh")) {
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        //使用HttpServletRequest包装原始请求达到修改post请求中body内容的目的
        CustometRequestWrapper requestWrapper = new CustometRequestWrapper(httpServletRequest);

        filterChain.doFilter(requestWrapper, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
```

4. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.2
```

5. 访问http://localhost:8092/get ,显示如下内容

```
dev-1.2
```

6. 在git仓库中修改version的值为dev-1.3，**注意查看webhook的Recent Deliveries，post响应是否成功。**

7. 访问http://localhost:8091/get ,显示如下内容

```
dev-1.3
```

8. 访问http://localhost:8092/get ,显示如下内容

```
dev-1.3
```
