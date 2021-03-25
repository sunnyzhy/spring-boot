# spring boot 集成 swagger2-3.0.0
## 1. 添加 swagger2 依赖
```xml
<properties>
    <swagger2.version>3.0.0</swagger2.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-boot-starter</artifactId>
        <version>${swagger2.version}</version>
    </dependency>
</dependencies>
```

## 2. 添加 swagger2 bean
添加 swagger2 配置类，即 swagger2 bean。

![swagger-03](./images/swagger-03.png 'swagger-03')

**注意在 3.0.0 版本中，已经不推荐使用 EnableSwagger2WebFlux 和 EnableSwagger2WebMvc 注解了。**

```java
@EnableSwagger2
@Configuration
public class Swagger2Config {
    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.withClassAnnotation(RestController.class))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("SDK API")
                .description("SDK API")
                .version("1.0")
                .build();
    }
}
```

## 3. 浏览在线文档
![swagger-04](./images/swagger-04.png 'swagger-04')

**注意在 3.0.0 版本中，swagger-ui 的路径有变化。**

http://localhost:9011/swagger-ui/index.html
