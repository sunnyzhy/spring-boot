Spring MVC:

```java
@Service
public class TestService {
    @Async
    public void test(String header) {
        System.out.println("start...");
        try {
            Thread.sleep(10000);
            System.out.println(header);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end...");
    }
}

@RestController
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public String async(ServerHttpRequest request) {
        String userName = request.getHeaders().getFirst("userName");
        testService.test(userName);
        return "ok";
    }
}
```

Web Flux:

```java
@Service
public class TestService {
    public Mono<Void> test(String header) {
        System.out.println("start...");
        try {
            Thread.sleep(10000);
            System.out.println(header);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        System.out.println("end...");
        return Mono.empty();
    }
}

@RestController
public class TestController {
    @Autowired
    private TestService testService;

    @GetMapping("/test")
    public Mono<String> async() {
        return ReactiveRequestContextHolder.getRequest().flatMap(request -> {
            String header = request.getHeaders().getFirst("userName");
            Mono.fromRunnable(() -> testService.test(header))
                    .subscribeOn(Schedulers.boundedElastic())
                    .subscribe();
            return Mono.just("ok");
        });
    }
}
```
