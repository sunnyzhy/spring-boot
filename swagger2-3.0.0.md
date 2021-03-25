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

http://localhost:9011/swagger-ui/index.html

# 生成html文档
## 添加swagger2markup和asciidoctor插件
```
<plugin>
	<groupId>io.github.swagger2markup</groupId>
	<artifactId>swagger2markup-maven-plugin</artifactId>
	<version>1.3.7</version>
	<configuration>
		<swaggerInput>http://localhost:9011/v2/api-docs</swaggerInput>
		<outputDir>src/docs/swagger/generated</outputDir>
		<config>
			<!--设置输出文件的语言：ASCIIDOC, MARKDOWN, CONFLUENCE_MARKUP-->
			<swagger2markup.markupLanguage>ASCIIDOC</swagger2markup.markupLanguage>
			<!--设置目录的展现方式-->
			<swagger2markup.pathsGroupedBy>TAGS</swagger2markup.pathsGroupedBy>
		</config>
	</configuration>
</plugin>
<plugin>
	<groupId>org.asciidoctor</groupId>
	<artifactId>asciidoctor-maven-plugin</artifactId>
	<version>1.5.6</version>
	<configuration>
		<sourceDirectory>src/docs/swagger/generated</sourceDirectory>
		<outputDirectory>src/docs/swagger/html</outputDirectory>
		<backend>html</backend>
		<sourceHighlighter>coderay</sourceHighlighter>
		<attributes>
			<toc>left</toc>
		</attributes>
	</configuration>
</plugin>
```

## 生成adoc
```
swagger2markup:convertSwagger2markup
```
运行Plugins下面的swagger2markup插件,就会在 src\docs\swagger\generated 目录下生成adoc文件

## 生成html
```
asciidoctor:process-asciidoc
```
运行Plugins下面的asciidoctor插件,就会在 src\docs\swagger\html 目录下生成html文件
