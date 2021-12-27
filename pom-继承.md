# 继承

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
