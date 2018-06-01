# 添加mapper依赖
```
<dependency>
	<groupId>tk.mybatis</groupId>
	<artifactId>mapper-spring-boot-starter</artifactId>
	<version>2.0.2</version>
</dependency>
```

# 添加mybatis-generator插件
```
<plugin>
	<groupId>org.mybatis.generator</groupId>
	<artifactId>mybatis-generator-maven-plugin</artifactId>
	<version>1.3.2</version>
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
			<version>3.4.0</version>
		</dependency>
	</dependencies>
</plugin>
```

# 代码生成器文档
- 配置文件的目录
```
src
|_main
|_ _resources
|_ _ ＿generator
|_ _ _ _generatorConfig.xml
```

- 配置文件的内容
```
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <properties resource="application.properties"/>

    <context id="Mysql" targetRuntime="MyBatis3Simple" defaultModelType="flat">
        <property name="beginningDelimiter" value="`"/>
        <property name="endingDelimiter" value="`"/>
        <plugin type="tk.mybatis.mapper.generator.MapperPlugin">
            <property name="mappers" value="tk.mybatis.mapper.common.Mapper"/>
        </plugin>

        <!-- 配置数据库连接 -->
        <jdbcConnection driverClass="com.mysql.jdbc.Driver"
                        connectionURL="jdbc:mysql://20.0.0.252/zhy?useSSL=false"
                        userId="root"
                        password="root">
        </jdbcConnection>

        <!-- 配置实体类 -->
        <javaModelGenerator targetPackage="com.zhy.user.model" targetProject="src/main/java"/>

        <!-- 配置Mapper接口对应的XML文件 -->
        <sqlMapGenerator targetPackage="mapper" targetProject="src/main/resources"/>

        <!-- 配置Mapper接口 -->
        <javaClientGenerator targetPackage="com.zhy.user.mapper" targetProject="src/main/java"
                             type="XMLMAPPER"/>

        <!--  配置自动要生成代码的表名
              通配符%匹配所有表  -->
        <table tableName="%">
            <generatedKey column="id" sqlStatement="Mysql" identity="true"/>
        </table>
    </context>
</generatorConfiguration>
```

# 生成代码
```
mybatis-generator:generate
```
运行Plugins下面的mybatis-generator插件,生成即可。
