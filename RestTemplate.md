# Profile

## 基础用法

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

## 拦截器

### 1. 签名工具类

```java
/**
 * 签名工具类：生成参数签名
 */
public class SignUtils {

    // 生成签名
    public static String generateSign(
            String method,
            String url,
            Map<String, String> queryParams,
            Object bodyParams,
            String secret
    ) {
        // 1. 收集所有参数（URL查询参数 + 自定义queryParams + 请求体参数）
        MultiValueMap<String, String> allParams = new LinkedMultiValueMap<>();

        // 1.1 解析URL中的查询参数
        if (url != null && url.contains("?")) {
            Map<String, List<String>> urlQueryParams = UriComponentsBuilder.fromUriString(url)
                    .build()
                    .getQueryParams();
            urlQueryParams.forEach(allParams::addAll);
        }

        // 1.2 添加自定义queryParams（如RestTemplate的params参数）
        if (queryParams != null) {
            queryParams.forEach((k, v) -> allParams.add(k, v));
        }

        // 1.3 添加请求体参数（仅支持表单或简单JSON，复杂对象需自行序列化）
        if (bodyParams != null) {
            // 示例：假设body是MultiValueMap（表单参数），实际可扩展为JSON解析
            if (bodyParams instanceof MultiValueMap) {
                ((MultiValueMap<String, String>) bodyParams).forEach(allParams::addAll);
            } else {
                // 若为JSON对象，可先序列化为Map再添加（需引入Jackson等库）
                // Map<String, Object> jsonParams = objectMapper.convertValue(bodyParams, Map.class);
                // jsonParams.forEach((k, v) -> allParams.add(k, v.toString()));
            }
        }

        // 2. 过滤空值并按参数名ASCII升序排序
        List<Map.Entry<String, String>> sortedParams = allParams.entrySet().stream()
                .filter(entry -> !entry.getValue().isEmpty())
                .flatMap(entry -> entry.getValue().stream()
                        .map(value -> new AbstractMap.SimpleEntry<>(entry.getKey(), value)))
                .sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toList());

        // 3. 拼接参数为 key=value&key=value 格式
        StringBuilder paramStr = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams) {
            paramStr.append(entry.getKey()).append("=").append(entry.getValue()).append("&");
        }

        // 4. 追加时间戳、随机数、密钥
        long timestamp = System.currentTimeMillis();
        String nonce = RandomStringUtils.randomAlphanumeric(16); // 16位随机字符串
        paramStr.append("timestamp=").append(timestamp)
                .append("&nonce=").append(nonce)
                .append("&secret=").append(secret);

        // 5. MD5加密并转大写
        return DigestUtils.md5Hex(paramStr.toString().getBytes(StandardCharsets.UTF_8)).toUpperCase();
    }
}
```

### 2. 签名拦截器（拦截请求并添加签名）

```java
/**
 * RestTemplate 签名拦截器：自动为请求添加签名
 */
public class SignInterceptor implements ClientHttpRequestInterceptor {
    private static final Logger log = LoggerFactory.getLogger(SignInterceptor.class);
    private final String secret; // 签名密钥（通常从配置文件读取）

    public SignInterceptor(String secret) {
        this.secret = secret;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        // 1. 获取请求基本信息
        HttpMethod method = request.getMethod();
        String url = request.getURI().toString();
        HttpHeaders headers = request.getHeaders();

        // 2. 提取请求参数（query参数 + body参数）
        // 2.1 获取query参数（从URL中解析）
        Map<String, String> queryParams = new HashMap<>();
        // （如果是RestTemplate的exchange/getForObject等方法传入的params，需额外处理，此处简化）

        // 2.2 获取body参数（仅示例，实际根据Content-Type解析）
        Object bodyParams = null;
        String contentType = headers.getContentType() != null ? headers.getContentType().toString() : "";
        if (body != null && body.length > 0) {
            String bodyStr = new String(body, StandardCharsets.UTF_8);
            log.info("请求体: {}", bodyStr);
            // 示例：表单参数解析（application/x-www-form-urlencoded）
            if (contentType.contains("application/x-www-form-urlencoded")) {
                bodyParams = UriComponentsBuilder.newInstance()
                        .query(bodyStr)
                        .build()
                        .getQueryParams();
            }
            // 其他类型（如JSON）需自行解析为Map
        }

        // 3. 生成签名
        String sign = SignUtils.generateSign(
                method.name(),
                url,
                queryParams,
                bodyParams,
                secret
        );

        // 4. 添加签名到请求头
        HttpHeaders newHeaders = new HttpHeaders();
        newHeaders.putAll(headers);
        newHeaders.add("sign", sign);
        newHeaders.add("timestamp", String.valueOf(System.currentTimeMillis())); // 需与签名中的timestamp一致
        newHeaders.add("nonce", RandomStringUtils.randomAlphanumeric(16)); // 需与签名中的nonce一致

        // 5. 包装请求（替换头信息）
        HttpRequest wrappedRequest = new HttpRequestWrapper(request) {
            @Override
            public HttpHeaders getHeaders() {
                return newHeaders;
            }
        };

        // 6. 继续执行请求
        return execution.execute(wrappedRequest, body);
    }
}
```

### 3. 配置 RestTemplate 并注入拦截器

```java
@Configuration
public class RestTemplateConfig {

    // 签名密钥（实际应从配置文件读取）
    private static final String SIGN_SECRET = "your_secret_key";

    @Bean
    public RestTemplate restTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        // 添加签名拦截器
        restTemplate.setInterceptors(Collections.singletonList(new SignInterceptor(SIGN_SECRET)));
        return restTemplate;
    }
}
```

## FAQ

### 1. 在使用 RestTemplate 发送请求时，可能会遇到 % 被转义为 %25 的问题，导致请求失败。

重点：

- **HTTP 协议要求 URL 中的非 ASCII 字符必须编码**
- **RestTemplate 会将 url 中的 ```%``` 转为 ```%25```，其它特殊字符如: ```!@#``` 等，则不会转码****

如果已经对 url 进行了 encode，此时特殊符号被转为了 ```% + 数字或字母``` 的形式；RestTemplate 在发送请求时还会将 ```%``` 转为 ```%25```，相当于 url 被转码了两次。

解决方案:

‌创建 URI 对象‌，将字符串 URL 转换为 URI 对象，避免 RestTemplate 自动转码。

```java
String url = "http://example.com/path?query=value";
UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url);

## 必须 encode，否则 RestTemplate 发送请求时会丢失 ```#``` 后面的内容
URI uri = builder.build().encode().toUri();

## 不正确的调用方法，传入 String 类型的 url
url = uri.toString();
restTemplate.exchange(url, HttpMethod.GET, httpEntity, typeRef);

## 正确的调用方法，传入 URI 对象
restTemplate.exchange(uri, HttpMethod.GET, httpEntity, typeRef);
```

总结:

- 使用 RestTemplate 发送请求时，如果传入 String 类型的 url，则这个 url 中的 ```%``` 会被转为 ```%25```

- 如果 url 中不存在特殊符号，则没有问题；

- 如果 url 中存在特殊符号且没有调用 encode，那么 RestTemplate 执行后，实际请求的 url 会丢失 ```#``` 后面的内容（url 机制）；

- 如果 url 中存在特殊符号且调用了 encode，那么 RestTemplate 执行后，url 会被再次 encode，导致实际请求的 url 出错（```%``` 被转码）。
   ```java
   UriComponentsBuilder builder = UriComponentsBuilder
             .fromHttpUrl("http://localhost:8080/api")
             .queryParam("name", "张三");
   UriComponents encodedComponents = builder.encode().build(); // 此时，url 会被编码为 http://localhost:8080/api?name=%E5%BC%A0%E4%B8%89
   String encodedUrl = encodedComponents.toUriString();
   restTemplate.getForObject(encodedUrl, String.class); // 此时，url 会被编码为 http://localhost:8080/api?name=%25E5%25BC%25A0%25E4%25B8%2589
   ```

**因此，如果 url 中存在特殊符号，可以在 ```url.encode``` 之后，把 url 转为 URI 对象，再使用 RestTemplate 发送请求，这样就能避免 ```%``` 被转码的问题。**
