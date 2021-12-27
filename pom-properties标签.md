# properties

[maven 官网](https://maven.apache.org/pom.html 'maven')

```xml
<project>
  ...
  <properties>
    <maven.compiler.source>1.7</maven.compiler.source>
    <maven.compiler.target>1.7</maven.compiler.target>
    <!-- Following project.-properties are reserved for Maven in will become elements in a future POM definition. -->
    <!-- Don't start your own properties properties with project. -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding> 
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
  </properties>
  ...
</project>
```

一般来说，我们使用 占位符 ```${x}``` 的形式来引用 maven 属性。

## env.X

属性前缀带 env 的，将指向系统的环境变量，例如: ```${env.PATH}``` 代表系统环境变量中的 PATH 路径。

## project.x

例如 ```${project.version}``` 表示:

```xml
<project>
    <version>1.0</version>
</project>
```

## settings.x

settings 代表 maven 的 settings 配置文件 ```/conf/settings.xml```

例如 ```${settings.localRepository}``` 表示:

```xml
<settings>
    <localRepository>/path/to/local/repo</localRepository>
</settings>
```

## Java系统属性

例如: ```${java.version}```

## x

在 properties 标签中可以自定义标签名，并使用占位符引用。例如 ```${spring-cloud.version}``` 表示:

```xml
<properties>
    <spring-cloud.version>2020.0.5</spring-cloud.version>
</properties>
```

## maven.x

```xml
<properties>
    <!-- 跳过测试 -->
    <maven.test.skip>true</maven.test.skip>

    <!-- 安装的时候，跳过该模块，即该模块不会安装到仓库 -->
    <maven.install.skip>true</maven.install.skip>
</properties>
```
