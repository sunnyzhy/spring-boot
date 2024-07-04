# SpringCloudGateway 获取请求的参数

官网用于缓存请求 body 的过滤器：

```
https://docs.spring.io/spring-cloud-gateway/docs/3.1.4/reference/html/#the-cacherequestbody-gatewayfilter-factory
```

改进的代码如下：

```java
@Slf4j
public class SessionAccessFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();
        if (method.equals(HttpMethod.GET)) {
            return RequestParamUtil.readGetData(exchange, chain);
        } else {
            HttpHeaders headers = request.getHeaders();
            MediaType contentType = headers.getContentType();
            // 如果contentType为空，就说明requestBody为空，参数可能附带在url里
            // 一种常见的情况就是前端使用 post + params 的方式请求
            if (contentType == null) {
                return RequestParamUtil.readNoJsonData(exchange, chain);
            }
            if (MediaType.APPLICATION_JSON.isCompatibleWith(contentType)) {
                return RequestParamUtil.readJsonData(exchange, chain);
            }
            if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(contentType)) {
                return RequestParamUtil.readFormData(exchange, chain);
            }
            if (MediaType.MULTIPART_FORM_DATA.isCompatibleWith(contentType)) {
                return RequestParamUtil.readMultipartData(exchange, chain);
            }
        }

        return chain.filter(exchange);
    }
}

@Slf4j
public class RequestParamUtil {
    private final static List<HttpMessageReader<?>> defaultMessageReaders = HandlerStrategies.withDefaults().messageReaders();

    /**
     * ReadGetData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readGetData(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean check = checkSign(exchange, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, String> queryParams = request.getQueryParams();
                for (Map.Entry<String, String> entry : queryParams.toSingleValueMap().entrySet()) {
                    String value = entry.getValue();
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        if (!check) {
            return MonoUtil.setFailedRequest(exchange, BaseResponseVoUtil.error(RestCodeConstants.SIGN_ERROR_CODE.getCode(), RestCodeConstants.SIGN_ERROR_CODE.getName()), HttpStatus.FORBIDDEN);
        }
        return chain.filter(exchange);
    }

    /**
     * ReadJsonBody
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readJsonData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, new ParameterizedTypeReference<String>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                String cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
                // 过滤空值
                if (isEmpty(cachedBody)) {
                    return;
                }
                sortedMap.put("", cachedBody);
            }
        });
        return mono;
    }

    /**
     * ReadNoJsonData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readNoJsonData(ServerWebExchange exchange, GatewayFilterChain chain) {
        boolean check = checkSign(exchange, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {

            }
        });
        if (!check) {
            return MonoUtil.setFailedRequest(exchange, BaseResponseVoUtil.error(RestCodeConstants.SIGN_ERROR_CODE.getCode(), RestCodeConstants.SIGN_ERROR_CODE.getName()), HttpStatus.FORBIDDEN);
        }
        return chain.filter(exchange);
    }

    /**
     * ReadFormData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readFormData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, new ParameterizedTypeReference<MultiValueMap<String, String>>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, String> cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
                if (cachedBody == null) {
                    return;
                }
                for (Map.Entry<String, String> entry : cachedBody.toSingleValueMap().entrySet()) {
                    String value = entry.getValue();
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        return mono;
    }

    /**
     * ReadMultipartData
     *
     * @param exchange
     * @param chain
     * @return
     */
    public static Mono<Void> readMultipartData(ServerWebExchange exchange, GatewayFilterChain chain) {
        Mono<Void> mono = doReadData(exchange, chain, new ParameterizedTypeReference<MultiValueMap<String, Part>>() {
        }, new AnalyseData() {
            @Override
            public void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap) {
                MultiValueMap<String, Part> cachedBody = exchange.getAttribute(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR);
                if (cachedBody == null) {
                    return;
                }
                for (Map.Entry<String, Part> entry : cachedBody.toSingleValueMap().entrySet()) {
                    Part part = entry.getValue();
                    String value = null;
                    if (part instanceof FilePart) {
                        value = ((FilePart) part).filename();
                    } else {
                        value = ((FormFieldPart) part).value();
                    }
                    // 过滤空值
                    if (isEmpty(value)) {
                        continue;
                    }
                    sortedMap.put(entry.getKey(), value);
                }
            }
        });
        return mono;
    }

    private static <T> Mono<Void> doReadData(ServerWebExchange exchange, GatewayFilterChain chain, ParameterizedTypeReference<T> typeReference, AnalyseData analyseData) {
        Mono<Void> mono = ServerWebExchangeUtils.cacheRequestBodyAndRequest(exchange, (serverHttpRequest) -> {
            final ServerRequest serverRequest = ServerRequest
                    .create(exchange.mutate().request(serverHttpRequest).build(), defaultMessageReaders);
            return serverRequest.bodyToMono(typeReference)
                    .doOnNext(objectValue -> {
                        exchange.getAttributes().put(ServerWebExchangeUtils.CACHED_REQUEST_BODY_ATTR, objectValue);
                    }).then(Mono.defer(() -> {
                        boolean check = checkSign(exchange, analyseData);
                        if (!check) {
                            return MonoUtil.setFailedRequest(exchange, BaseResponseVoUtil.error(RestCodeConstants.SIGN_ERROR_CODE.getCode(), RestCodeConstants.SIGN_ERROR_CODE.getName()), HttpStatus.FORBIDDEN);
                        }
                        ServerHttpRequest cachedRequest = exchange
                                .getAttribute(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                        exchange.getAttributes().remove(CACHED_SERVER_HTTP_REQUEST_DECORATOR_ATTR);
                        return chain.filter(exchange.mutate().request(cachedRequest).build());
                    }));
        });

        return mono;
    }

    private static boolean checkSign(ServerWebExchange exchange, AnalyseData analyseData) {
        // TODO
        return true;
    }

    private static boolean isEmpty(String content) {
        return StringUtils.isEmpty(content) || content.equals("undefined") || content.equals("null");
    }

    interface AnalyseData {
        void execute(ServerHttpRequest request, SortedMap<String, String> sortedMap);
    }
}
```

参考：

```
https://www.lushihuan.com/archives/springcloudgateway%E8%8E%B7%E5%8F%96multipartform-data%E8%AF%B7%E6%B1%82%E7%B1%BB%E5%9E%8B%E5%8F%82%E6%95%B0%E7%9A%84%E5%8F%AF%E7%94%A8%E6%96%B9%E6%B3%95
```
