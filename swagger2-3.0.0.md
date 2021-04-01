# spring boot 集成 swagger2-3.0.0
## 1. 添加 swagger2 依赖
- 1.1. 排除 springfox-boot-starter 自带的 swagger-annotations 和 swagger-models 依赖

- 1.2. 添加 1.6.2 版本的 swagger-annotations 和 swagger-models 依赖

```xml
<dependency>
	<groupId>io.springfox</groupId>
	<artifactId>springfox-boot-starter</artifactId>
	<version>3.0.0</version>
	<exclusions>
		<exclusion>
			<groupId>io.swagger</groupId>
			<artifactId>swagger-annotations</artifactId>
		</exclusion>
		<exclusion>
			<groupId>io.swagger</groupId>
			<artifactId>swagger-models</artifactId>
		</exclusion>
	</exclusions>
</dependency>
<dependency>
	<groupId>io.swagger</groupId>
	<artifactId>swagger-annotations</artifactId>
	<version>1.6.2</version>
</dependency>
<dependency>
	<groupId>io.swagger</groupId>
	<artifactId>swagger-models</artifactId>
	<version>1.6.2</version>
</dependency>
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
