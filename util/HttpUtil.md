```java
@Configuration
public class JacksonConfig {
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }
}

@Configuration
public class RestTemplateConfig {
    @Autowired
    private ObjectMapper objectMapper;

    @Bean
    @LoadBalanced
    /**
     * 带有负载均衡的restTemplate，适用于微服务集群内(url含有服务名)
     */
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        return crateRestTemplate(factory);
    }

    /**
     * 普通功能的restTemplate，url含有域名、主机名和端口
     *
     * 不能用于微服务集群内
     * @param factory
     * @return
     */
    @Bean
    public RestTemplate rest(ClientHttpRequestFactory factory) {
        return crateRestTemplate(factory);
    }

    private RestTemplate crateRestTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        List<HttpMessageConverter<?>> list = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> httpMessageConverter : list) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(StandardCharsets.UTF_8);
                break;
            }
        }
        // 设置自定义的 ObjectMapper，防止反序列化失败（如：反序列化 Date 数据）
        MappingJackson2HttpMessageConverter messageConverter = new MappingJackson2HttpMessageConverter();
        messageConverter.setPrettyPrint(false);
        messageConverter.setObjectMapper(objectMapper);
        list.removeIf(item -> item.getClass().getName().equals(MappingJackson2HttpMessageConverter.class.getName()));
        list.add(messageConverter);
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);
        factory.setReadTimeout(30000);
        return factory;
    }
}

@Component
@Slf4j
public class HttpUtil {
    private final RestTemplate restTemplate;
    private final RestTemplate rest;

    public HttpUtil(RestTemplate restTemplate, RestTemplate rest) {
        this.restTemplate = restTemplate;
        this.rest = rest;
    }

    public <T> T get(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, ParameterizedTypeReference<T> typeRef) {
        return get(rest, url, headers, params, null, typeRef);
    }

    public <T> T get(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, ParameterizedTypeReference<T> typeRef) {
        return get(rest, url, headers, params, uriVariables, typeRef);
    }

    public <T, R> R post(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.POST, headers, params, null, body, typeRef);
    }

    public <T, R> R post(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.POST, headers, params, uriVariables, body, typeRef);
    }

    public <T, R> R put(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.PUT, headers, params, null, body, typeRef);
    }

    public <T, R> R put(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.PUT, headers, params, uriVariables, body, typeRef);
    }

    public <T, R> R delete(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.DELETE, headers, params, null, body, typeRef);
    }

    public <T, R> R delete(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(rest, url, HttpMethod.DELETE, headers, params, uriVariables, body, typeRef);
    }

    public <T> T getLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, ParameterizedTypeReference<T> typeRef) {
        return get(restTemplate, url, headers, params, null, typeRef);
    }

    public <T> T getLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, ParameterizedTypeReference<T> typeRef) {
        return get(restTemplate, url, headers, params, uriVariables, typeRef);
    }

    public <T, R> R postLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.POST, headers, params, null, body, typeRef);
    }

    public <T, R> R postLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.POST, headers, params, uriVariables, body, typeRef);
    }

    public <T, R> R putLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.PUT, headers, params, null, body, typeRef);
    }

    public <T, R> R putLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.PUT, headers, params, uriVariables, body, typeRef);
    }

    public <T, R> R deleteLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.DELETE, headers, params, null, body, typeRef);
    }

    public <T, R> R deleteLb(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return post(restTemplate, url, HttpMethod.DELETE, headers, params, uriVariables, body, typeRef);
    }

    private <T> T get(RestTemplate restTemplate, String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, ParameterizedTypeReference<T> typeRef) {
        return execute(url, headers, params, (uri, httpHeaders) -> {
            HttpEntity<?> httpEntity = new HttpEntity<>(httpHeaders);
            if (uriVariables == null) {
                return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, typeRef);
            } else {
                return restTemplate.exchange(uri.toString(), HttpMethod.GET, httpEntity, typeRef, uriVariables);
            }
        });
    }

    private <T, R> R post(RestTemplate restTemplate, String url, HttpMethod method, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, Map<String, ?> uriVariables, T body, ParameterizedTypeReference<R> typeRef) {
        return execute(url, headers, params, (uri, httpHeaders) -> {
            HttpEntity<?> httpEntity = body == null ? new HttpEntity<>(httpHeaders) : new HttpEntity<>(body, httpHeaders);
            if (uriVariables == null) {
                return restTemplate.exchange(uri, method, httpEntity, typeRef);
            } else {
                return restTemplate.exchange(uri.toString(), method, httpEntity, typeRef, uriVariables);
            }
        });
    }

    private <T> T execute(String url, MultiValueMap<String, String> headers, MultiValueMap<String, String> params, BiFunction<URI, HttpHeaders, ResponseEntity<T>> function) {
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        if (headers != null && !headers.isEmpty()) {
            httpHeaders.addAll(headers);
        }
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);
        if (params != null && !params.isEmpty()) {
            builder.queryParams(params);
        }
        URI uri = builder.build().encode().toUri();
        ResponseEntity<T> responseEntity = function.apply(uri, httpHeaders);
        return responseEntity.getBody();
    }
}
```
