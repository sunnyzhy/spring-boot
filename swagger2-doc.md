# spring boot 生成 swagger2 文档

**以生成 html 文档为例。**

## 添加 swagger2markup 和 asciidoctor 插件依赖
```xml
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

## 生成 adoc
```bash
swagger2markup:convertSwagger2markup
```
运行 Plugins 下面的 swagger2markup 插件,就会在 src\docs\swagger\generated 目录下生成 adoc 文件

## 生成 html
```bash
asciidoctor:process-asciidoc
```
运行 Plugins 下面的 asciidoctor 插件,就会在 src\docs\swagger\html 目录下生成 html 文件
