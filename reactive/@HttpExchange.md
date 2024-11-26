```@HttpExchange``` 注解用于声明接口作为HTTP远程服务。在方法、类级别使用。

快捷注解简化不同的请求方式：

- GetExchange
- PostExchange
- PutExchange
- DeleteExchange
- 替代OpenFeign

```java
@Configuration
public class WebClientConfiguration {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private <T> T buildWebClient(Class<T> clazz) {
        WebClient webClient = webClientBuilder()
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientDefaultCodecsConfigurer -> {
                            clientDefaultCodecsConfigurer.defaultCodecs()
                                    .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper(), MediaType.APPLICATION_JSON));
                            clientDefaultCodecsConfigurer.defaultCodecs()
                                    .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper(), MediaType.APPLICATION_JSON)); // 自定义Jackson反序列化时对时间的处理
                        })
                        .build())
                .defaultHeaders(headers ->
                {
                    headers.add("userId", "1");
                    headers.add("userName", "admin");
                })
                .build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();
        return proxyFactory.createClient(clazz);
    }

    private <T> T buildWebClient(String serverName, Class<T> clazz) {
        WebClient webClient = webClientBuilder()
                .baseUrl("http://" + serverName)
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(clientDefaultCodecsConfigurer -> {
                            clientDefaultCodecsConfigurer.defaultCodecs()
                                    .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper(), MediaType.APPLICATION_JSON));
                            clientDefaultCodecsConfigurer.defaultCodecs()
                                    .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper(), MediaType.APPLICATION_JSON)); // 自定义Jackson反序列化时对时间的处理
                        })
                        .build())
                .defaultHeaders(headers ->
                {
                    headers.add("userId", "1");
                    headers.add("userName", "admin");
                })
                .build();
        HttpServiceProxyFactory proxyFactory = HttpServiceProxyFactory
                .builder()
                .exchangeAdapter(WebClientAdapter.create(webClient))
                .build();
        return proxyFactory.createClient(clazz);
    }

    @Bean
    public AFeign aFeign() {
//        return buildWebClient("a-server", AFeign.class);
        return buildWebClient(AFeign.class);
    }

    @Bean
    public BFeign bFeign() {
//        return buildWebClient("b-server", BFeign.class);
        return buildWebClient( BFeign.class);
    }
}

@HttpExchange(url = "http://a-server")
public interface AFeign {
    @PostExchange(value = "/a")
    Mono<Response> add(@RequestBody Entity entity);
}

@HttpExchange(url = "http://b-server")
public interface BFeign {
    @GetExchange(value = "/b/{id}")
    Mono<Response<List<Entity>>> list(@PathVariable("id") Integer id, @RequestParam Map<String, Object> params);
}
```

单元测试:

```java
@Resource
ObjectMapper objectMapper;

@Resource
private AFeign aFeign;
@PostExchange(value = "/a")
public Mono<String> testA(@RequestBody Entity entity) {
    return aFeign.add(entity).flatMap(s->{
            try {
                return Mono.just(objectMapper.writeValueAsString(s));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }).doOnError(e-> System.out.println(e.getMessage()));
}

@Resource
private BFeign bFeign;
@GetExchange(value = "/b")
public Mono<String> testB() {
    Map<String, Object> params=new HashMap<>();
    params.put("userId", 1);
    params.put("userName", "admin");

    return bFeign.list(1, params)
            .flatMap(s->{
                try {
                    return Mono.just(objectMapper.writeValueAsString(s));
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }).doOnError(e-> System.out.println(e.getMessage()));
}
```
