# Feign 异步调用上下文

## RequestContextHolder 方式

在主线程里提取上下文:

```java
RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
```

在线程池或子线程里设置上下文:

```java
@Async
public void doThread(RequestAttributes attributes) {
    RequestContextHolder.setRequestAttributes(attributes);
    // TODO Feign ...
    feign.call();
}
```

Feign 拦截器:

```java
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> requestHeaders = getRequestHeaders();
        for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
            requestTemplate.header(requestHeader.getKey(), requestHeader.getValue());
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> map = new LinkedHashMap<>();
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return map;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request == null) {
            return map;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return map;
        }
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}
```

## ThreadLocal 方式

RequestHeaderHandler 类:

```java
public class RequestHeaderHandler {
    public static final ThreadLocal<RequestAttributes> THREAD_LOCAL = new ThreadLocal<>();

    public static void setRequestAttributes(RequestAttributes requestAttributes) {
        THREAD_LOCAL.set(requestAttributes);
    }

    public static RequestAttributes getRequestAttributes() {
        return THREAD_LOCAL.get();
    }

    public static void remove() {
        THREAD_LOCAL.remove();
    }
}
```

在主线程里提取上下文:

```java
RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
```

在线程池或子线程里设置上下文:

```java
@Async
public void doThread(RequestAttributes attributes) {
    RequestHeaderHandler.setRequestAttributes(attributes);
    // TODO Feign ...
    feign.call();
}
```

Feign 拦截器:

```java
@Component
public class FeignInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate requestTemplate) {
        Map<String, String> requestHeaders = getRequestHeaders();
        for (Map.Entry<String, String> requestHeader : requestHeaders.entrySet()) {
            requestTemplate.header(requestHeader.getKey(), requestHeader.getValue());
        }
    }

    private Map<String, String> getRequestHeaders() {
        Map<String, String> map = new LinkedHashMap<>();
        RequestAttributes requestAttributes = RequestHeaderHandler.getRequestAttributes();
        if (requestAttributes == null) {
            return map;
        }
        HttpServletRequest request = ((ServletRequestAttributes) requestAttributes).getRequest();
        if (request == null) {
            return map;
        }
        Enumeration<String> headerNames = request.getHeaderNames();
        if (headerNames == null) {
            return map;
        }
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            map.put(key, value);
        }
        return map;
    }
}
```
