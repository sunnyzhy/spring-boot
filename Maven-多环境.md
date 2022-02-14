# Maven - 多环境

Maven 环境一般分为 dev(开发), test(测试), prod(生产)。由于每个环境的数据库, redis 以及其他参数的配置项及运行模式（比如 redis 在一个环境是单机模式运行，在另一个环境是集群模式运行）是不同的，所以需要多个环境的配置项。

## 模式一（继承）

### 父模块 pom 配置

***profiles, resources 具有继承性。***

#### 配置 profile

profiles 跟 parent, dependencyManagement, build 平级。用于定义多环境。

```xml
<profiles>
    <!-- 开发环境 -->
    <profile>
        <id>dev</id>
        <properties>
            <environment>dev</environment>
        </properties>
        <activation>
            <!-- 当前项是否为默认的 profile, 默认值为 false -->
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>

    <!-- 测试环境 -->
    <profile>
        <id>test</id>
        <properties>
            <environment>test</environment>
        </properties>
    </profile>

    <!-- 生产环境 -->
    <profile>
        <id>prod</id>
        <properties>
            <environment>prod</environment>
        </properties>
    </profile>
</profiles>
```

#### 配置 resource

一般情况下，资源文件（如: xml, properties, json 等文件）都是放在 ```src/main/resources``` 目录里的，在使用 maven 打包时，maven 能把这些资源文件一起打包进 jar 或者 war 文件里。

有时候，一些资源文件会定义在 ```src/main/java``` 目录里，这时在使用 maven 打包时，就需要在 pom.xml 文件里配置 resource，以便把资源文件也打包进 jar 或者 war 文件里；否则，这些资源文件是不会被打包的。

```xml
<build>
    <resources>
        <resource>
            <!-- 配置类文件目录里需要包含的文件 -->
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.data</include>
            </includes>
        </resource>
        <resource>
            <!-- 配置资源文件目录里需要包含的文件 -->
            <directory>src/main/resources</directory>
            <includes>
                <include>application.yml</include>
                <include>application-${environment}.yml</include>
                <!-- 需要包含的其他文件，如: mapper.xml -->
                <!-- <include>**/*.xml</include> -->
            </includes>
            <!-- 是否用实际参数 -P 替换项目里的 ${environment} 变量(定义在 profile 里), 默认值为 false;
                 ${environment} 变量包含于编译环境(build#resource.include)和运行环境(application.yml#pring.profiles.active) -->
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```

### 子模块 yml 配置

#### application.yml

```yml
spring:
    profiles:
        active: @environment@
```

#### application-dev.yml

```yml
server:
    port: 6600

redis:
    host: 192.168.0.1
    port: 6379
    password: admin
```

#### application-test.yml

```yml
server:
    port: 6600

redis:
    host: 192.168.20.1
    port: 6375
    password: admin
```

#### application-prod.yml

```yml
server:
    port: 6600

redis:
    host: 192.168.10.1
    port: 6380
    password: admin
```

### 指定环境打包

#### 开发环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Pdev
```

classes 里的 yml 文件:

```
application.yml
application-dev.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: dev
```

#### 测试环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Ptest
```

classes 里的 yml 文件:

```
application.yml
application-test.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: test
```

#### 生产环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Pprod
```

classes 里的 yml 文件:

```
application.yml
application-prod.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: prod
```

## 模式二

### 子模块 pom 配置

#### 配置 profile

profiles 跟 parent, dependencyManagement, build 平级。用于定义多环境。

```xml
<profiles>
    <!-- 开发环境 -->
    <profile>
        <id>dev</id>
        <properties>
            <environment>dev</environment>
            <redis.host>192.168.0.1</redis.host>
            <redis.port>6379</redis.port>
            <redis.password>admin</redis.password>
        </properties>
        <activation>
            <!-- 当前项是否为默认的 profile, 默认值为 false -->
            <activeByDefault>true</activeByDefault>
        </activation>
    </profile>

    <!-- 测试环境 -->
    <profile>
        <id>test</id>
        <properties>
            <environment>test</environment>
            <redis.host>192.168.20.1</redis.host>
            <redis.port>6375</redis.port>
            <redis.password>admin</redis.password>
        </properties>
    </profile>

    <!-- 生产环境 -->
    <profile>
        <id>prod</id>
        <properties>
            <environment>prod</environment>
            <redis.host>192.168.10.1</redis.host>
            <redis.port>6380</redis.port>
            <redis.password>admin</redis.password>
        </properties>
    </profile>
</profiles>
```

#### 配置 resource

一般情况下，资源文件（如: xml, properties, json 等文件）都是放在 ```src/main/resources``` 目录里的，在使用 maven 打包时，maven 能把这些资源文件一起打包进 jar 或者 war 文件里。

有时候，一些资源文件会定义在 ```src/main/java``` 目录里，这时在使用 maven 打包时，就需要在 pom.xml 文件里配置 resource，以便把资源文件也打包进 jar 或者 war 文件里；否则，这些资源文件是不会被打包的。

```xml
<build>
    <resources>
        <resource>
            <!-- 配置类文件目录里需要包含的文件 -->
            <directory>src/main/java</directory>
            <includes>
                <include>**/*.data</include>
            </includes>
        </resource>
        <resource>
            <!-- 配置资源文件目录里需要包含的文件 -->
            <directory>src/main/resources</directory>
            <includes>
                <include>application.yml</include>
                <include>application-${environment}.yml</include>
                <!-- 需要包含的其他文件，如: mapper.xml -->
                <!-- <include>**/*.xml</include> -->
            </includes>
            <!-- 是否用实际参数 -P 替换项目里的 ${environment} 变量(定义在 profile 里), 默认值为 false;
                 ${environment} 变量包含于编译环境(build#resource.include)和运行环境(application.yml#pring.profiles.active) -->
            <filtering>true</filtering>
        </resource>
    </resources>
</build>
```

### 子模块 yml 配置

#### application.yml

```yml
spring:
    profiles:
        active: @environment@

server:
    port: 6600

redis:
    host: @redis.host@
    port: @redis.port@
    password: @redis.password@
```

### 指定环境打包

#### 开发环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Pdev
```

classes 里的 yml 文件:

```
application.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: dev

server:
    port: 6600

redis:
    host: 192.168.0.1
    port: 6379
    password: admin
```


#### 测试环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Ptest
```

classes 里的 yml 文件:

```
application.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: test

server:
    port: 6600

redis:
    host: 192.168.20.1
    port: 6375
    password: admin
```

#### 生产环境

```bash
mvn clean install -am -amd -Dmaven.test.skip=true -Pprod
```

classes 里的 yml 文件:

```
application.yml
```

application.yml 文件的内容:

```yml
spring:
    profiles:
        active: prod

server:
    port: 6600

redis:
    host: 192.168.10.1
    port: 6380
    password: admin
```

**注:**

- 当开发环境、测试环境与生产环境的 yml 文件配置项的差异非常大的时候（如: 开发环境下 redis 使用单机模式，而测试环境下 redis使用哨兵模式），可以采用模式一
- 当开发环境、测试环境与生产环境的 yml 文件配置项的差异不大的时候（如: 开发环境和测试环境下 redis 都使用哨兵模式，此时只是 redis.host 不同），可以采用模式二
