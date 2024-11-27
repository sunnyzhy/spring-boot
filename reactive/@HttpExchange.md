```@HttpExchange``` 注解用于声明接口作为HTTP远程服务。在方法、类级别使用。

快捷注解简化不同的请求方式：

- GetExchange
- PostExchange
- PutExchange
- DeleteExchange
- 替代OpenFeign

```java
/**
 * 服务间调用时，根据上下文动态添加请求头的参数
 */
@Component
public class ReactiveHeaderFilter implements ExchangeFilterFunction {
    @Override
    public Mono<ClientResponse> filter(ClientRequest clientRequest, ExchangeFunction nextFilter) {
        return ReactiveRequestContextHolder.getRequest().flatMap(request->{
            HttpHeaders headers = request.getHeaders();
            ClientRequest modifiedRequest = ClientRequest
                    .from(clientRequest)
                    .header("userName", headers.getFirst("userName"))
                    .header("userId", headers.getFirst("userId"))
                    .build();
            return nextFilter.exchange(modifiedRequest);
        });
    }
}

@Configuration
public class WebClientConfiguration {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Resource
    ReactiveHeaderFilter reactiveHeaderFilter;

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
                .filter(reactiveHeaderFilter)
                // 或者使用ExchangeFilterFunction.ofRequestProcessor
//                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
//                    Mono<ClientRequest> clientRequestMono = ReactiveRequestContextHolder.getRequest().flatMap(request -> {
//                        HttpHeaders headers = request.getHeaders();
//                        ClientRequest clientRequest1 = ClientRequest.from(clientRequest)
//                                .header("userName", headers.getFirst("userName"))
//                                .header("userId", headers.getFirst("userId"))
//                                .build();
//                        return Mono.just(clientRequest1);
//                    });
//                    return clientRequestMono;
//                }))
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
                .filter(reactiveHeaderFilter)
                // 或者使用ExchangeFilterFunction.ofRequestProcessor
//                .filter(ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
//                    Mono<ClientRequest> clientRequestMono = ReactiveRequestContextHolder.getRequest().flatMap(request -> {
//                        HttpHeaders headers = request.getHeaders();
//                        ClientRequest clientRequest1 = ClientRequest.from(clientRequest)
//                                .header("userName", headers.getFirst("userName"))
//                                .header("userId", headers.getFirst("userId"))
//                                .build();
//                        return Mono.just(clientRequest1);
//                    });
//                    return clientRequestMono;
//                }))
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

Controller:

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

Test:

```
POST http://localhost:8080/a
Content-Type: application/json
userId: 1
userName: admin

{
  "id": 100,
  "name": "demo",
  "tags": ""
}
###
```
