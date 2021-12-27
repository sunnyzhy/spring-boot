# 版本传递规则

以 spring-boot-2.5.7 和 lombok 为例。

## parent 继承

1. 默认版本 ```1.18.22```，参考 [添加依赖声明时避免重复添加](https://github.com/sunnyzhy/spring-boot/blob/master/pom-dependencyManagement%E6%A0%87%E7%AD%BE.md '添加依赖声明时避免重复添加') 查找 spring-boot 默认的 lombok 版本

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
   ```

2. 在 properties 里指定版本 1.18.20，此时 ```1.18.20``` 会覆盖 spring-boot 默认的 1.18.22

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>
   ```

3. 在 properties 里指定了版本 1.18.20，同时在 dependencyManagement 里也指定了版本 1.18.18，此时子模块使用的是 dependencyManagement 中声明的版本 ```1.18.18```

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.18</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

4. 在 properties 里指定了版本 1.18.20，同时在 dependencyManagement 里指定了版本 1.18.18 和 1.18.16， 此时子模块使用的是 dependencyManagement 中声明的版本 ```1.18.16(后声明的版本号会覆盖前面声明的版本号)```

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.18</version>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.16</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

5. 在 properties 里指定了版本 1.18.20，在 dependencies 里指定了版本 1.18.14，同时在 dependencyManagement 里也指定了版本 1.18.12，此时子模块使用的是 dependencyManagement 中声明的版本 ```1.18.12```

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.12</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.14</version>
        </dependency>
    </dependencies>
   ```

6. 在 properties 里指定了版本 1.18.20，在 dependencies 里指定了版本 1.18.14，同时在 dependencyManagement 里指定了版本 1.18.12 和 1.18.10，此时子模块使用的是 dependencyManagement 中声明的版本 ```1.18.10(后声明的版本号会覆盖前面声明的版本号)```

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.12</version>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.10</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.14</version>
        </dependency>
    </dependencies>
   ```

总结，parent 继承的方式，版本号传递的规则:

1. 如果没有在 properties 和 dependencyManagement 里指定版本号，就使用 spring-boot 默认的版本号
2. 如果在 properties 里指定了版本号，但没有在 dependencyManagement 里指定版本号，就使用 properties 里指定的版本号
3. 如果在 properties、 dependencyManagement、dependencies 里同时指定了版本号，就使用 dependencyManagement 里同时指定的版本号
4. 如果在 dependencyManagement 里同时指定了多个版本号，就按声明的顺序，```使用最后声明的版本号```
5. dependencyManagement 的优先级最高

## dependencyManagement 继承

1. 默认版本 ```1.18.22```，参考 [添加依赖声明时避免重复添加](https://github.com/sunnyzhy/spring-boot/blob/master/pom-dependencyManagement%E6%A0%87%E7%AD%BE.md '添加依赖声明时避免重复添加') 查找 spring-boot 默认的 lombok 版本

   ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.7</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

2. 在 properties 里指定版本 1.18.20，此时子模块使用的是 dependencyManagement 中默认的版本 ```1.18.22```

   ```xml
    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.7</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

3. 在 properties 里指定版本 1.18.20，同时在 dependencyManagement 里也指定了版本 1.18.12，此时子模块使用的是 dependencyManagement 中默认的版本 ```1.18.12```

   ```xml
    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.7</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.12</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

4. 在 properties 里指定版本 1.18.20，同时在 dependencyManagement 里指定了版本 1.18.10 和 1.18.12，此时子模块使用的是 dependencyManagement 中默认的版本 ```1.18.10(先声明的版本号优先)```

   ```xml
    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.7</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.10</version>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.12</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

5. 在 properties 里指定版本 1.18.20，在 dependencies 里指定了版本 1.18.14，同时在 dependencyManagement 里指定了版本 1.18.10 和 1.18.12，此时子模块使用的是 dependencyManagement 中默认的版本 ```1.18.10(先声明的版本号优先)```

   ```xml
    <properties>
        <lombok.version>1.18.20</lombok.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-dependencies</artifactId>
                <version>2.5.7</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.10</version>
            </dependency>

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.12</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>1.18.14</version>
        </dependency>
    </dependencies>
   ```

总结，dependencyManagement 继承的方式，版本号传递的规则:

1. 如果在 dependencyManagement 里只导入了 spring-boot-dependencies，就使用 spring-boot 默认的版本号
2. 如果在 properties、 dependencyManagement、dependencies 里同时指定了版本号，就使用 dependencyManagement 里同时指定的版本号
3. 如果在 dependencyManagement 里同时指定了多个版本号，就按声明的顺序，```使用最先声明的版本号```
4. dependencyManagement 的优先级最高
