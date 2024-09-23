# SpringCloudGateway 获取请求的参数

由于 Request 的 Body 只能读取一次，如果直接在过滤器中读取而不进行封装，就会导致后续服务无法获取到 Body 的数据。

通过 ```exchange.getRequest().getBody()``` 获取的 Body，其结果是一个 ```Flux<DataBuffer>```。也就是说请求的 Body 是一次性的，当再次请求 Body 的时候，Body 就读取不到了，因为 Flux 已经结束。

官网用于缓存请求 body 的过滤器：

```
https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#the-cacherequestbody-gatewayfilter-factory
```

添加依赖项：

```xml
<dependency>
    <groupId>org.synchronoss.cloud</groupId>
    <artifactId>nio-multipart-parser</artifactId>
    <version>1.1.0</version>
</dependency>
```

示例代码参考：

```
https://github.com/sunnyzhy/MySpring/tree/main/MySpring/admin-gateway
```
