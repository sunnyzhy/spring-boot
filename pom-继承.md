# 继承

## 版本传递规则

以 log4j2 为例。

1. 默认版本 2.14.1，参考 [添加依赖声明时避免重复添加](https://github.com/sunnyzhy/spring-boot/blob/master/pom-dependencyManagement%E6%A0%87%E7%AD%BE.md '添加依赖声明时避免重复添加') 查找 spring-boot 默认的 log4j2 版本

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
   ```

2. 在 properties 里指定版本 2.17.0，此时 2.17.0 会覆盖 spring-boot 默认的 2.14.1

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <log4j2.version>2.17.0</log4j2.version>
    </properties>
   ```

3. 在 properties 里指定了版本 2.17.0，同时在 dependencyManagement 里也指定了版本 2.16.0， 此时使用的是 dependencyManagement 中声明的版本 2.16.0

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <log4j2.version>2.17.0</log4j2.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-to-slf4j</artifactId>
                <version>2.16.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

4. 在 properties 里指定了版本 2.17.0，同时在 dependencyManagement 里指定了版本 2.16.0 和 2.15.0， 此时使用的是 dependencyManagement 中声明的版本 2.15.0(后声明的版本号会覆盖前面声明的版本号)

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <log4j2.version>2.17.0</log4j2.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-to-slf4j</artifactId>
                <version>2.16.0</version>
            </dependency>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-to-slf4j</artifactId>
                <version>2.15.0</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

## 单继承

父模块

```xml
    <groupId>com.example</groupId>
    <artifactId>parent</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <packaging>pom</packaging>
```

- parent 继承方式

   ```xml
    <parent>
        <groupId>com.example</groupId>
        <artifactId>parent</artifactId>
        <version>0.0.1-SNAPSHOT</version>
    </parent>
   ```

- dependencyManagement 继承方式

   ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>parent</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

## 多继承

父模块 A

```xml
    <groupId>com.example</groupId>
    <artifactId>parent-a</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <packaging>pom</packaging>
```

父模块 B

```xml
    <groupId>com.example</groupId>
    <artifactId>parent-b</artifactId>
    <version>0.0.1-SNAPSHOT</version>

    <packaging>pom</packaging>
```

- 继承方式

   ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>parent-a</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
          
            <dependency>
                <groupId>com.example</groupId>
                <artifactId>parent-b</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```
