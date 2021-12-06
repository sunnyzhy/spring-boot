# spring-cloud-starter-gateway

## 1 添加依赖项

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-gateway</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-reactor-resilience4j</artifactId>
</dependency>
```

## 2 简介

### 2.1 GatewayProperties

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\config\GatewayProperties.class

@ConfigurationProperties("spring.cloud.gateway")
@Validated
public class GatewayProperties {
    public static final String PREFIX = "spring.cloud.gateway";
    private final Log logger = LogFactory.getLog(this.getClass());
    @NotNull
    @Valid
    private List<RouteDefinition> routes = new ArrayList();
    private List<FilterDefinition> defaultFilters = new ArrayList();
    private List<MediaType> streamingMediaTypes;
    private boolean failOnRouteDefinitionError;
    
    // ...
}
```

gateWay 的主要功能之一是转发请求，常用的属性有:

|关键字|说明|
|--|--|
|routes|路由，```不能为空```|
|defaultFilters|全局过滤器|

### 2.2 RouteDefinition

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\route\RouteDefinition.class

@Validated
public class RouteDefinition {
    private String id;
    @NotEmpty
    @Valid
    private List<PredicateDefinition> predicates = new ArrayList();
    @Valid
    private List<FilterDefinition> filters = new ArrayList();
    @NotNull
    private URI uri;
    private Map<String, Object> metadata = new HashMap();
    private int order = 0;
    
    // ...
}
```

Route（路由）常用的属性有:

|关键字|说明|
|--|--|
|id|路由的唯一标识|
|uri|路由转发的目标地址，```不能为空```|
|predicates|转发规则，路由转发的判断条件，```不能为空```|
|filters|过滤器，是路由转发请求时所经过的过滤逻辑，可用于验证请求、响应内容|

### 2.3 PredicateDefinition

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\handler\predicate\PredicateDefinition.class

@Validated
public class PredicateDefinition {
    @NotNull
    private String name;
    private Map<String, String> args = new LinkedHashMap();
    
    // ...
}
```

Predicate（转发规则）由以下两部分组成:

|关键字|说明|
|--|--|
|name|具体 AbstractRoutePredicateFactory 的```前缀```，```不能为空```，比如引用的转发规则是 PathRoutePredicateFactory，那么只取 ```Path```，配置项就是 ```- Path=/api/uaa/**```|
|args|参数，k-v 形式的 map，具体的 key 请参照转发规则的 Config|

```
具体转发规则的工厂类在 org.springframework.cloud.gateway.handler.predicate 包里。
```

#### 2.3.1 PathRoutePredicateFactory 示例

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\handler\predicate\PathRoutePredicateFactory.class

public class PathRoutePredicateFactory extends AbstractRoutePredicateFactory<PathRoutePredicateFactory.Config> {
    // ...
    
    public static class Config {
        private List<String> patterns = new ArrayList();
        private boolean matchTrailingSlash = true;
        
        // ...
    }
}
```

简化配置:

```yml
predicates:
  - Path=/api/uaa/**
```

完整配置:

```yml
predicates:
  - name: Path
    args:
      - patterns:
          - /api/uaa/**
```

### 2.4 FilterDefinition

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\filter\FilterDefinition.class

@Validated
public class FilterDefinition {
    @NotNull
    private String name;
    private Map<String, String> args = new LinkedHashMap();
    
    // ...
}
```

Filter（过滤器）由以下两部分组成:

|关键字|说明|
|--|--|
|name|具体 AbstractGatewayFilterFactory 的```前缀```，```不能为空```，比如引用的转发规则是 StripPrefixGatewayFilterFactory，那么只取 ```StripPrefix```，配置项就是 ```- StripPrefix=2```|
|args|参数，key:value 形式，具体的 key 请参照过滤器具体的属性|

```
具体过滤器的工厂类在 org.springframework.cloud.gateway.filter.factory 包里。
```

#### 2.4.1 StripPrefixGatewayFilterFactory 示例

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\filter\factory\StripPrefixGatewayFilterFactory.class

public class StripPrefixGatewayFilterFactory extends AbstractGatewayFilterFactory<StripPrefixGatewayFilterFactory.Config> {
    // ...
    
    public static class Config {
        private int parts;

        // ...
    }
}
```

简化配置:

```yml
filters:
  - StripPrefix=2
```

完整配置:

```yml
filters:
  - name: StripPrefix
    args:
      parts: 2
```

#### 2.4.2 SpringCloudCircuitBreakerFilterFactory 示例

```
SpringCloudCircuitBreakerFilterFactory 一般用作全局熔断。
```

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\filter\factory\SpringCloudCircuitBreakerFilterFactory.class

public abstract class SpringCloudCircuitBreakerFilterFactory extends AbstractGatewayFilterFactory<SpringCloudCircuitBreakerFilterFactory.Config> {
    public static final String NAME = "CircuitBreaker";
    
    // ...

    public String name() {
        return "CircuitBreaker";
    }

    // ...

    public static class Config implements HasRouteId {
        private String name;
        private URI fallbackUri;
        private String routeId;
        private Set<String> statusCodes = new HashSet();

        // ...
    }
}
```

完整配置:

```yml
default-filters:
  - name: CircuitBreaker
    args:
      fallbackUri: forward:/fallback
```

/fallback 的 Controller:

```java
@RestController
@RequestMapping("/fallback")
public class FallbackController {
    @GetMapping
    public Mono<ResponseEntityVo> fallback() {
        return ResponseMonoUtil.error(HttpStatus.SERVICE_UNAVAILABLE);
    }
}
```

### 2.5 ShortcutType

在上述的示例中，有些配置既可以用简化配置也可以用完整配置，而有些配置只能用完整配置，这是由于每个 Factory(AbstractRoutePredicateFactory/AbstractGatewayFilterFactory) 默认配置的 ShortcutType 不同。

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\support\ShortcutConfigurable.class

public static enum ShortcutType {
    DEFAULT {
        public Map<String, Object> normalize(Map<String, String> args, ShortcutConfigurable shortcutConf, SpelExpressionParser parser, BeanFactory beanFactory) {
            Map<String, Object> map = new HashMap();
            int entryIdx = 0;

            for(Iterator var7 = args.entrySet().iterator(); var7.hasNext(); ++entryIdx) {
                Entry<String, String> entry = (Entry)var7.next();
                String key = ShortcutConfigurable.normalizeKey((String)entry.getKey(), entryIdx, shortcutConf, args);
                Object value = ShortcutConfigurable.getValue(parser, beanFactory, (String)entry.getValue());
                map.put(key, value);
            }

            return map;
        }
    },
    GATHER_LIST {
        public Map<String, Object> normalize(Map<String, String> args, ShortcutConfigurable shortcutConf, SpelExpressionParser parser, BeanFactory beanFactory) {
            Map<String, Object> map = new HashMap();
            List<String> fieldOrder = shortcutConf.shortcutFieldOrder();
            Assert.isTrue(fieldOrder != null && fieldOrder.size() == 1, "Shortcut Configuration Type GATHER_LIST must have shortcutFieldOrder of size 1");
            String fieldName = (String)fieldOrder.get(0);
            map.put(fieldName, args.values().stream().map((value) -> {
                return ShortcutConfigurable.getValue(parser, beanFactory, value);
            }).collect(Collectors.toList()));
            return map;
        }
    },
    GATHER_LIST_TAIL_FLAG {
        public Map<String, Object> normalize(Map<String, String> args, ShortcutConfigurable shortcutConf, SpelExpressionParser parser, BeanFactory beanFactory) {
            Map<String, Object> map = new HashMap();
            List<String> fieldOrder = shortcutConf.shortcutFieldOrder();
            Assert.isTrue(fieldOrder != null && fieldOrder.size() == 2, "Shortcut Configuration Type GATHER_LIST_HEAD must have shortcutFieldOrder of size 2");
            List<String> values = new ArrayList(args.values());
            if (!((List)values).isEmpty()) {
                int lastIdx = ((List)values).size() - 1;
                String lastValue = (String)((List)values).get(lastIdx);
                if (lastValue.equalsIgnoreCase("true") || lastValue.equalsIgnoreCase("false")) {
                    values = ((List)values).subList(0, lastIdx);
                    map.put(fieldOrder.get(1), ShortcutConfigurable.getValue(parser, beanFactory, lastValue));
                }
            }

            String fieldName = (String)fieldOrder.get(0);
            map.put(fieldName, ((List)values).stream().map((value) -> {
                return ShortcutConfigurable.getValue(parser, beanFactory, value);
            }).collect(Collectors.toList()));
            return map;
        }
    };

    // ...
}
```

说明:

1. DEFAULT: 按照每个工厂定义的 ```List<String> shortcutFieldOrder()``` 顺序依次赋值
2. GATHER_LIST: ```List<String> shortcutFieldOrder()``` 的列表只能有一个元素, 如果传递的参数有多个，就把多个参数合并成一个集合赋值给该元素
3. GATHER_LIST_TAIL_FLAG: ```List<String> shortcutFieldOrder()``` 的列表只能有两个元素，如果传递的多个参数中最后一个既不是 true 也不是 false 的话，就把最后一个参数也赋值给第一个元素; 否则，就把最后一个参数赋值给第二个元素，把其余的参数赋值给第一个元素

#### 2.5.1 DEFAULT 示例

SpringCloudCircuitBreakerFilterFactory 使用的是 ```ShortcutType.DEFAULT```

```java
public abstract class SpringCloudCircuitBreakerFilterFactory extends AbstractGatewayFilterFactory<SpringCloudCircuitBreakerFilterFactory.Config> {
    // ...

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("name");
    }
    
    // ...
}
```

如果配置项为:

```yml
default-filters:
  - CircuitBreaker=aaa,bbb
```

则映射结果为:

```
{name=aaa, _genkey_1=bbb}
```

所以，配置 CircuitBreaker 的时候需要指定明确的 k-v，如:

```yml
default-filters:
  - name: CircuitBreaker
    args:
      name: aaa
      fallbackUri: bbb
```

则映射结果为:

```
{fallbackUri=bbb, name=aaa}
```

#### 2.5.2 GATHER_LIST 示例

HostRoutePredicateFactory 使用的是 ```ShortcutType.GATHER_LIST```

```java
public class RemoteAddrRoutePredicateFactory extends AbstractRoutePredicateFactory<RemoteAddrRoutePredicateFactory.Config> {
    // ...
    
    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST;
    }

    public List<String> shortcutFieldOrder() {
        return Collections.singletonList("sources");
    }
    
    // ...
}
```

如果配置项为:

```yml
predicates:
  - RemoteAddr=192.168.0.1,192.168.0.2
```

则映射结果为:

```
{sources=[192.168.0.1, 192.168.0.2]}
```

#### 2.5.3 GATHER_LIST_TAIL_FLAG 示例

PathRoutePredicateFactory 使用的是 ```ShortcutType.GATHER_LIST_TAIL_FLAG```

```java
public class PathRoutePredicateFactory extends AbstractRoutePredicateFactory<PathRoutePredicateFactory.Config> {
    // ...
    
    public List<String> shortcutFieldOrder() {
        return Arrays.asList("patterns", "matchTrailingSlash");
    }

    public ShortcutType shortcutType() {
        return ShortcutType.GATHER_LIST_TAIL_FLAG;
    }
    
    // ...
}
```

如果配置项为:

```yml
predicates:
  - Path=/api/uaa/**,false,0011,true
```

则映射结果为:

```
{patterns=[/api/uaa/**, false, 0011], matchTrailingSlash=true}
```

## 3 自定义全局过滤器

### 3.1 配置文件

```yml
spring:
  application:
    name: spring-cloud-admin-gateway
  cloud:
    gateway:
      discovery:
        locator:
          # 开启从注册中心动态创建路由的功能，利用微服务名进行路由
          enabled: true
          lower-case-service-id: true
      routes:
        - id: uaa
          # 路由条件，如果匹配到 /api/uaa/**，就把请求转发到配置的 uri
          predicates:
            - Path=/api/uaa/**
          # 过滤条件，过滤掉 /api/uaa
          filters:
            - StripPrefix=2
          # lb = LoadBalancerClient
          # gateway 将使用 LoadBalancerClient 把 spring-cloud-admin-uaa 通过 eureka 解析为实际的主机和端口，并进行负载均衡
          uri: lb://spring-cloud-admin-uaa
      # 全局过滤
      default-filters:
        # 全局熔断 spring-cloud-starter-gateway:3.0.3 使用的是 CircuitBreaker GatewayFilter Factory，而不再是 Hystrix GatewayFilter Factory
        - name: CircuitBreaker
          args:
            fallbackUri: forward:/fallback
```

### 3.2 自定义全局过滤器

***需要实现 GlobalFilter, Ordered***

```java
@Component
public class GatewayFilterImpl implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest httpRequest = exchange.getRequest();
        String uri = httpRequest.getURI().getPath();

        // 从上下文中获取 token
        String token = httpRequest.getHeaders().getFirst("access-token");
        
        // TODO ...

        // 如果以上的验证都通过，就执行 chain 上的其他业务流程
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

## 4 自定义局部过滤器

### 4.1 配置文件

```yml
spring:
  application:
    name: spring-cloud-admin-gateway
  cloud:
    gateway:
      discovery:
        locator:
          # 开启从注册中心动态创建路由的功能，利用微服务名进行路由
          enabled: true
          lower-case-service-id: true
      routes:
        - id: uaa
          # 路由条件，如果匹配到 /api/uaa/**，就把请求转发到配置的 uri
          predicates:
            - Path=/api/uaa/**
          # 过滤条件，过滤掉 /api/uaa
          filters:
            - Local # 配置自定义的局部过滤器 LocalGatewayFilterFactory
            - StripPrefix=2
          # lb = LoadBalancerClient
          # gateway 将使用 LoadBalancerClient 把 spring-cloud-admin-uaa 通过 eureka 解析为实际的主机和端口，并进行负载均衡
          uri: lb://spring-cloud-admin-uaa
      # 全局过滤
      default-filters:
        # 全局熔断 spring-cloud-starter-gateway:3.0.3 使用的是 CircuitBreaker GatewayFilter Factory，而不再是 Hystrix GatewayFilter Factory
        - name: CircuitBreaker
          args:
            fallbackUri: forward:/fallback
```

### 4.2 自定义局部过滤器

***需要实现 GatewayFilter, Ordered***

```java
public class GatewayFilterImpl implements GatewayFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest httpRequest = exchange.getRequest();
        String uri = httpRequest.getURI().getPath();

        // 处理指定路由的逻辑
        // TODO ...

        // 如果以上的验证都通过，就执行 chain 上的其他业务流程
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
```

### 4.3 把局部过滤器加入到过滤器工厂

```java
@Component
public class LocalGatewayFilterFactory extends AbstractGatewayFilterFactory<Object> {
    @Override
    public GatewayFilter apply(Object config) {
        return new GatewayFilterImpl();
    }
}
```

## 5 GlobalFilter 和 GatewayFilter 的区别

### 5.1 联系

不管是 GlobalFilter 还是 GatewayFilter，他们都能够组成一个 filter 链来做拦截，而这个 filter 链是由 List<GatewayFilter> 集合组成的。其实底层通过适配器的方式，把 GlobalFilter 适配为 GatewayFilter 了。

源码:

```java
// .\org\springframework\cloud\spring-cloud-gateway-server\3.0.3\spring-cloud-gateway-server-3.0.3.jar!\org\springframework\cloud\gateway\handler\FilteringWebHandler.class

public class FilteringWebHandler implements WebHandler {
    protected static final Log logger = LogFactory.getLog(FilteringWebHandler.class);
    private final List<GatewayFilter> globalFilters;

    public FilteringWebHandler(List<GlobalFilter> globalFilters) {
        this.globalFilters = loadFilters(globalFilters);
    }

    private static List<GatewayFilter> loadFilters(List<GlobalFilter> filters) {
        return (List)filters.stream().map((filter) -> {
            FilteringWebHandler.GatewayFilterAdapter gatewayFilter = new FilteringWebHandler.GatewayFilterAdapter(filter);
            if (filter instanceof Ordered) {
                int order = ((Ordered)filter).getOrder();
                return new OrderedGatewayFilter(gatewayFilter, order);
            } else {
                return gatewayFilter;
            }
        }).collect(Collectors.toList());
    }
}
```

### 5.2 区别

- GlobalFilter: 对```所有路由```生效

- GatewayFilter: 对```指定路由```生效
