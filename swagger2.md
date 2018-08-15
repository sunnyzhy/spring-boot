# 添加Swagger2依赖
```
<dependency>
	<groupId>io.springfox</groupId>
	<artifactId>springfox-swagger2</artifactId>
	<version>2.8.0</version>
</dependency>

<dependency>
	<groupId>io.springfox</groupId>
	<artifactId>springfox-swagger-ui</artifactId>
	<version>2.8.0</version>
</dependency>
```

# 开启swagger2
在启动类加上添加注解
```
@EnableSwagger2
```

# 配置不显示basic-error-controler
```
@Configuration
public class SwaggerConfig {

    @Bean
    public Docket api() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .pathMapping("/")
                .select() // 选择那些路径和api会生成document
                .apis(RequestHandlerSelectors.any())// 对所有api进行监控
                //不显示错误的接口地址
                .paths(Predicates.not(PathSelectors.regex("/error.*")))//错误路径不监控
                .paths(PathSelectors.regex("/.*"))// 对根下所有路径进行监控
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder().title("接口文档")
                .contact(new Contact("zhy", "", "zhy@163.com"))
                .description("SWAGGER_2自动生成的接口文档")
                .termsOfServiceUrl("NO terms of service")
                .license("The Apache License, Version 2.0")
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .version("v1.0")
                .build();
    }
}
```

# 浏览在线文档

http://localhost:9011/swagger-ui.html


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
