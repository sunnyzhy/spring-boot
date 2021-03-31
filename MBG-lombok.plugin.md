# mybatis-generator 自定义 lombok 插件
**注意:**
1. mybatis-generator-maven-plugin 的 classpath 只包含 mybatis generator 自己
2. 我们在业务工程中自定义的 PluginAdapter 插件不属于 mybatis-generator-maven-plugin 的 classpath
3. 可以通过添加依赖的形式，把自定义的 PluginAdapter 插件添加到 mybatis-generator-maven-plugin 的 classpath
4. 官方说明如下:
   ![mybatis-generator-plugin-01.png](./images/mybatis-generator-plugin-01.png 'mybatis-generator-plugin-01')
5. 数据库表和字段的 comment 要填写完整

## 1. 新建 mybatis-generator-lombok-plugin 插件工程
### 1.1 添加 mybatis-generator-core 依赖
```xml
<groupId>org.mybatis.generator</groupId>
<artifactId>mybatis-generator-lombok-plugin</artifactId>
<version>0.0.1</version>

<dependencies>
    <dependency>
        <groupId>org.mybatis.generator</groupId>
        <artifactId>mybatis-generator-core</artifactId>
        <version>1.3.6</version>
    </dependency>
</dependencies>
```

### 1.2 创建 lombok 插件继承 PluginAdapter
```java
package org.mybatis.generator.lombok.plugin;


import org.mybatis.generator.api.IntrospectedColumn;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;

import java.util.List;

public class LombokPlugin extends PluginAdapter {
    @Override
    public boolean validate(List<String> list) {
        return true;
    }

    public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
        if (lombokEnabled()) {
            // import lombok.Data;
            topLevelClass.addImportedType("lombok.Data");
            // 添加 @Data 注解
            topLevelClass.addAnnotation("@Data");
        }
        // 必须返回 true，否则不会生成表的实体类
        return true;
    }

    @Override
    public boolean modelSetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !lombokEnabled();
    }

    @Override
    public boolean modelGetterMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn, IntrospectedTable introspectedTable, ModelClassType modelClassType) {
        return !lombokEnabled();
    }

    private boolean lombokEnabled() {
        // 读取配置文件 generatorConfig.xml 里 property 的 name 为 lombokEnabled 的值
        String enabled = properties.getProperty("lombokEnabled");
        return (null == enabled || !Boolean.parseBoolean(enabled)) ? false : true;
    }
}
```

### 1.3 安装 mybatis-generator-lombok-plugin 插件到本地仓库

## 2. 在业务工程中使用 mybatis-generator-lombok-plugin 插件
### 2.1 添加依赖
```xml
<properties>
	<mybatis-plugin.version>1.3.6</mybatis-plugin.version>
</properties>

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
				<artifactId>mybatis-generator-lombok-plugin</artifactId>
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
  
	<!-- 自定义的 mybatis-generator-lombok 插件
		 1. 如果 lombokEnabled 值为 true，就生成 @Data 注解
		 2. 如果满足以下情况之一，则不会生成 @Data 注解:
			2.1. lombokEnabled 值为 false
			2.2. 名称为 lombokEnabled 的 property 不存在
			2.3. mybatis-generator-lombok 插件不存在 -->
	<plugin type="org.mybatis.generator.lombok.plugin.LombokPlugin">
		<property name="lombokEnabled" value="true"/>
	</plugin>
</context>
```

### 2.3 运行 mybatis-generator 插件，自动生成代码
