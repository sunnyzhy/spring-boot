# mybatis-generator 自定义 swagger2 插件
**注意:**
1. mybatis-generator-maven-plugin 的 classpath 只包含 mybatis generator 自己
2. 我们在业务工程中自定义的 PluginAdapter 插件不属于 mybatis-generator-maven-plugin 的 classpath
3. 可以通过添加依赖的形式，把自定义的 PluginAdapter 插件添加到 mybatis-generator-maven-plugin 的 classpath
4. 官方说明如下:
   ![mybatis-generator-plugin-01.png](./images/mybatis-generator-plugin-01.png 'mybatis-generator-plugin-01')
5. 数据库表和字段的 comment 要填写完整

## 1. 新建 mybatis-generator-swagger2-plugin 插件工程
### 1.1 添加 mybatis-generator-core 依赖
```xml
<groupId>org.mybatis.generator</groupId>
<artifactId>mybatis-generator-swagger2-plugin</artifactId>
<version>0.0.1</version>

<dependencies>
    <dependency>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-core</artifactId>
        <version>1.3.6</version>
    </dependency>
</dependencies>
```

### 1.2 创建 swagger2 插件继承 PluginAdapter
```java
package org.mybatis.generator.swagger2.plugin;

import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.Field;
import org.mybatis.generator.api.dom.java.TopLevelClass;

import java.util.List;

public class Swagger2Plugin extends PluginAdapter {
    public boolean validate(List<String> list) {
        return true;
    }

    @Override
    public boolean modelFieldGenerated(Field field, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        // 读取配置文件 generatorConfig.xml 里 property 的 name 为 apiModel 的值
        String apiModelAnnotationPackage = properties.getProperty("apiModel");
        if (apiModelAnnotationPackage != null) {
            apiModelAnnotationPackage = apiModelAnnotationPackage.trim();
            if (apiModelAnnotationPackage.length() > 0 && apiModelAnnotationPackage.equals("io.swagger.annotations.ApiModel")) {
                // import io.swagger.annotations.ApiModel;
                topLevelClass.addImportedType(apiModelAnnotationPackage);
                // 添加 @ApiModel 注解，description 是数据表的 comment
                String apiModelAnnotation = "@ApiModel(value=\"" + topLevelClass.getType().getShortName() + "\", description=\"" + introspectedTable.getRemarks() + "\")";
                if (!topLevelClass.getAnnotations().contains(apiModelAnnotation)) {
                    topLevelClass.addAnnotation(apiModelAnnotation);
                }
            }
        }

        // 读取配置文件 generatorConfig.xml 里 property 的 name 为 apiModelProperty 的值
        String apiModelPropertyAnnotationPackage = properties.getProperty("apiModelProperty");
        if (apiModelPropertyAnnotationPackage != null) {
            apiModelPropertyAnnotationPackage = apiModelPropertyAnnotationPackage.trim();
            if (apiModelPropertyAnnotationPackage.length() > 0 && apiModelPropertyAnnotationPackage.equals("io.swagger.annotations.ApiModelProperty")) {
                // import io.swagger.annotations.ApiModelProperty;
                topLevelClass.addImportedType(apiModelPropertyAnnotationPackage);
                // 添加 @ApiModelProperty 注解, value 是数据表字段的 comment
                field.addAnnotation("@ApiModelProperty(value=\"" + introspectedColumn.getRemarks() + "\")");
            }
        }

        return super.modelFieldGenerated(field, topLevelClass, introspectedColumn, introspectedTable, modelClassType);
    }
}
```

### 1.3 安装 mybatis-generator-swagger2-plugin 插件到本地仓库

## 2. 在业务工程中使用 mybatis-generator-swagger2-plugin 插件
### 2.1 添加依赖
```xml
<properties>
	<swagger2.version>3.0.0</swagger2.version>
	<swagger.version>1.6.2</swagger.version>
	<mybatis-plugin.version>1.3.6</mybatis-plugin.version>
</properties>

<dependencies>
    <dependency>
        <groupId>io.springfox</groupId>
        <artifactId>springfox-boot-starter</artifactId>
        <version>${swagger2.version}</version>
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
        <version>${swagger.version}</version>
    </dependency>
    <dependency>
        <groupId>io.swagger</groupId>
        <artifactId>swagger-models</artifactId>
        <version>${swagger.version}</version>
    </dependency>
</dependencies>

<plugins>
	<plugin>
		<groupId>org.mybatis.generator</groupId>
		<artifactId>mybatis-generator-maven-plugin</artifactId>
		<version>${mybatis-plugin.version}</version>
		<configuration>
			<configurationFile>${basedir}/src/main/resources/generator/generatorConfig.xml</configurationFile>
			<overwrite>true</overwrite>
			<verbose>true</verbose>
		</configuration>
		<dependencies>
			<dependency>
				<groupId>mysql</groupId>
				<artifactId>mysql-connector-java</artifactId>
				<version>5.1.38</version>
			</dependency>
			<dependency>
				<groupId>tk.mybatis</groupId>
				<artifactId>mapper</artifactId>
				<version>4.0.2</version>
			</dependency>
			<dependency>
				<groupId>org.mybatis.generator</groupId>
				<artifactId>mybatis-generator-swagger2-plugin</artifactId>
				<version>0.0.1</version>
			</dependency>
		</dependencies>
	</plugin>
</plugins>
```

### 2.2 配置 generatorConfig.xml
下面只列出了几个关键的配置:

```xml
<context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
	<!-- tk.mybatis 插件 -->
	<plugin type="tk.mybatis.mapper.generator.MapperPlugin">
		<property name="mappers" value="tk.mybatis.mapper.common.Mapper"/>
	</plugin>
  
	<!-- 自定义的 mybatis-generator-swagger2 插件 -->
	<plugin type="org.mybatis.generator.swagger2.plugin.Swagger2Plugin">
		<property name="apiModel" value="io.swagger.annotations.ApiModel"/>
		<property name="apiModelProperty" value="io.swagger.annotations.ApiModelProperty"/>
	</plugin>

	<jdbcConnection driverClass="com.mysql.jdbc.Driver"
					connectionURL="jdbc:mysql://127.0.0.1:3306/db_name?useSSL=false"
					userId="root"
					password="root">
		<!-- 设置 useInformationSchema 的值为 true，否则，IntrospectedTable 取到的表 comment 为空字符串 -->
		<property name="useInformationSchema" value="true" />
	</jdbcConnection>
</context>
```

### 2.3 运行 mybatis-generator 插件，自动生成代码
