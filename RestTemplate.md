
```java
@Configuration
public class RestTemplateConfig {
    private Integer connectTimeout = 60000;
    private Integer readTimeout = 60000;

    /**
     * @LoadBalanced注意事项：
     * 1. 如果使用@LoadBalanced注解，在调用restTemplate的时候就只能使用服务名，不能使用ip地址
     * 2. 如果没有使用@LoadBalanced注解，在调用restTemplate的时候就只能使用ip地址，不能使用服务名
     * @param factory
     * @return
     */
    @Bean
//    @LoadBalanced
    public RestTemplate restTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        List<HttpMessageConverter<?>> list = restTemplate.getMessageConverters();
        for (HttpMessageConverter<?> httpMessageConverter : list) {
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                // 解决中文乱码问题
                ((StringHttpMessageConverter) httpMessageConverter).setDefaultCharset(Charset.forName("UTF-8"));
                break;
            }
        }
        return restTemplate;
    }

    @Bean
    public ClientHttpRequestFactory clientHttpRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return factory;
    }
}
```

单元测试：
```java
@Autowired
private RestTemplate restTemplate;

@Test
void contextLoads() {
    restTemplate.postForObject("http://20.0.0.106:9999/rest", "RestTemplate测试", String.class);
}
```
