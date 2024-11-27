```java
@Configuration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public class ReactiveRequestContextFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(ServerHttpRequest.class, request));
    }
}

@Bean
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
public WebFilter reactiveRequestContextFilter() {
    return (exchange, chain) -> {
        ServerHttpRequest request = exchange.getRequest();
        return chain.filter(exchange)
                .contextWrite(ctx -> ctx.put(ServerHttpRequest.class, request));
    };
}

// 以上两种方法都可以利用WebFilter获取ServerHttpRequest并保存至上下文

public class ReactiveRequestContextHolder {
    public static Mono<ServerHttpRequest> getRequest() {
        return Mono.deferContextual(ctx ->
                ctx.hasKey(ServerHttpRequest.class) ? Mono.just(ctx.get(ServerHttpRequest.class)) : Mono.empty());
    }
}

@GetMapping(value = "/test")
public Mono<String> get() {
  return ReactiveRequestContextHolder.getRequest()
          .flatMap(request -> {
              HttpHeaders headers = request.getHeaders();
              return Mono.justOrEmpty(headers.getFirst("useruserName"));
          })
          .defaultIfEmpty("ok");
}
```
