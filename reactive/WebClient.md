```java
@Configuration
public class JacksonConfig {
    /**
     * jackson对象
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.setTimeZone(TimeZone.getTimeZone("GMT+8"));
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}

@Component
public class WebClientConfig {
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}

@Component
@Slf4j
public class WebClientUtil {
    @Resource
    private WebClient.Builder webClientBuilder;
    @Autowired
    private ObjectMapper objectMapper;

    public <T> Mono<T> reactiveCall(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference) {
        switch (webClientInfo.getMethod()) {
            case GET:
                return reactiveGet(webClientInfo, httpContextInfo, typeReference);
            case POST:
                return reactivePost(webClientInfo, httpContextInfo, typeReference);
            case PUT:
                return reactivePut(webClientInfo, httpContextInfo, typeReference);
            case DELETE:
                return reactiveDelete(webClientInfo, httpContextInfo, typeReference);
            default:
                return Mono.empty();
        }
    }

    private <T> Mono<T> reactiveGet(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference) {
        return reactiveCall(webClientInfo, httpContextInfo, typeReference, webClient ->
                webClient.method(HttpMethod.GET)
                        .uri(uriBuilder -> uriBuilder
                                .path(webClientInfo.getUri())
                                .queryParams(webClientInfo.getParams())
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
        );
    }

    private <T> Mono<T> reactivePost(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference) {
        return reactiveCall(webClientInfo, httpContextInfo, typeReference, webClient ->
                webClient.method(HttpMethod.POST)
                        .uri(webClientInfo.getUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(webClientInfo.getBody())
        );
    }

    private <T> Mono<T> reactivePut(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference) {
        return reactiveCall(webClientInfo, httpContextInfo, typeReference, webClient ->
                webClient.method(HttpMethod.PUT)
                        .uri(webClientInfo.getUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(webClientInfo.getBody())
        );
    }

    private <T> Mono<T> reactiveDelete(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference) {
        return reactiveCall(webClientInfo, httpContextInfo, typeReference, webClient ->
                webClient.method(HttpMethod.DELETE)
                        .uri(webClientInfo.getUri())
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(webClientInfo.getBody())
        );
    }

    private <T> Mono<T> reactiveCall(WebClientInfo webClientInfo, HttpContextInfo httpContextInfo, ParameterizedTypeReference<T> typeReference, Function<WebClient, WebClient.RequestHeadersSpec<?>> function) {
        try {
            WebClient webClient = webClientBuilder.baseUrl(webClientInfo.getServerName())
                    .exchangeStrategies(ExchangeStrategies.builder()
                            .codecs(clientDefaultCodecsConfigurer -> {
                                clientDefaultCodecsConfigurer.defaultCodecs()
                                        .jackson2JsonEncoder(new Jackson2JsonEncoder(objectMapper, MediaType.APPLICATION_JSON));
                                clientDefaultCodecsConfigurer.defaultCodecs()
                                        .jackson2JsonDecoder(new Jackson2JsonDecoder(objectMapper, MediaType.APPLICATION_JSON)); // 自定义Jackson反序列化时对时间的处理
                            })
                            .build())
                    .build();
            return function.apply(webClient)
                    .header(ConstantSet.USER_ID, httpContextInfo.getUserId())
                    .header(ConstantSet.USER_NAME, httpContextInfo.getUserName())
                    .retrieve()
                    .bodyToMono(typeReference)
                    .flatMap(resultSuccess -> {
                        if (resultSuccess == null) {
                            return Mono.empty();
                        }
                        return Mono.just(resultSuccess);
                    })
                    .onErrorResume(e -> {
                        log.error(e.getMessage(), e);
                        return Mono.empty();
                    });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            return Mono.empty();
        }
    }

}
```
