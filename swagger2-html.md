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
