# Maven scope

Maven 中使用 scope 来指定当前包的依赖范围和依赖的传递性。常见的可选值有: compile, provided, runtime, test, system, import 等。

|scope 取值|有效范围 (compile, runtime, test)|依赖传递|
|--|--|--|
|compile|all|是|
|provided|compile, test|否|
|runtime|runtime, test|是|
|test|test|否|
|system|compile, test|是|
|import|<dependencyManagement>|否|

## compile

compile 是默认的依赖有效范围，表示被依赖项目需要参与当前项目的编译、测试、运行，是一个比较强的依赖。打包的时候通常需要包含进去。

## provided

打包的时候可以不用包进去，别的设施 (Web Container) 会提供。事实上该依赖理论上可以参与编译、测试、运行等。相当于 compile，但是在打包阶段做了 exclude 的动作。

1. demo 的依赖声明, lombok 的 scope 是 provided:
  ```xml
  <dependencyManagement>
      <dependencies>
          <dependency>
              <groupId>org.projectlombok</groupId>
              <artifactId>lombok</artifactId>
              <version>${lombok.version}</version>
              <scope>provided</scope>
          </dependency>

          <dependency>
              <groupId>com.example</groupId>
              <artifactId>demo-common</artifactId>
              <version>0.0.1-SNAPSHOT</version>
          </dependency>
      </dependencies>
  </dependencyManagement>
  ```

2. 在 demo-common 里添加 lombok 依赖:
  ```xml
  <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
  </dependency>
  ```

3. 在 demo-service 里添加 common 依赖:
  ```xml
  <dependencies>
      <dependency>
          <groupId>com.example</groupId>
          <artifactId>demo-common</artifactId>
      </dependency>
  </dependencies>
  ```

![scope-provided](./images/maven-scope/provided.png 'scope-provided')

从上图可以地看到，虽然 demo-service 依赖于 demo-common， 但是因为 lombok 的 scope 是 provided, 所以 demo-service 并没有继承 lombok 依赖。

## runtime

表示被依赖项目无需参与项目的编译，不过后期的测试、运行，需要其参与。

1. demo 的依赖声明, mysql-connector-java 的 scope 是 runtime:
  ```xml
  <dependencyManagement>
      <dependencies>
          <dependency>
              <groupId>mysql</groupId>
              <artifactId>mysql-connector-java</artifactId>
              <version>${mysql.version}</version>
              <scope>runtime</scope>
          </dependency>

          <dependency>
              <groupId>com.example</groupId>
              <artifactId>demo-common</artifactId>
              <version>0.0.1-SNAPSHOT</version>
          </dependency>
      </dependencies>
  </dependencyManagement>
  ```

2. 在 demo-common 里添加 mysql-connector-java 依赖:
  ```xml
  <dependency>
      <groupId>mysql</groupId>
      <artifactId>mysql-connector-java</artifactId>
  </dependency>
  ```

3. 在 demo-service 里添加 common 依赖:
  ```xml
  <dependencies>
      <dependency>
          <groupId>com.example</groupId>
          <artifactId>demo-common</artifactId>
      </dependency>
  </dependencies>
  ```

![scope-runtime](./images/maven-scope/runtime.png 'scope-runtime')

## test

依赖项目仅仅参与测试相关的工作，包括测试代码的编译、执行。

1. demo 的依赖声明, spring-boot-starter-test 的 scope 是 test:
  ```xml
  <dependencyManagement>
      <dependencies>
          <dependency>
              <groupId>org.springframework.boot</groupId>
              <artifactId>spring-boot-starter-test</artifactId>
              <version>${spring-boot.version}</version>
              <scope>test</scope>
          </dependency>

          <dependency>
              <groupId>com.example</groupId>
              <artifactId>demo-common</artifactId>
              <version>0.0.1-SNAPSHOT</version>
          </dependency>
      </dependencies>
  </dependencyManagement>
  ```

2. 在 demo-common 里添加 spring-boot-starter-test 依赖:
  ```xml
  <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
  </dependency>
  ```

3. 在 demo-service 里添加 common 依赖:
  ```xml
  <dependencies>
      <dependency>
          <groupId>com.example</groupId>
          <artifactId>demo-common</artifactId>
      </dependency>
  </dependencies>
  ```

![scope-test](./images/maven-scope/test.png 'scope-test')

从上图可以地看到，虽然 demo-service 依赖于 demo-common， 但是因为 spring-boot-starter-test 的 scope 是 test, 所以 demo-service 并没有继承 spring-boot-starter-test 依赖。

## system

与 provided 相同，不过被依赖项不会从 maven 仓库获取，而是从本地文件系统获取，需要配合 systemPath 属性使用。

1. demo 的依赖声明, taobao-sdk-java-auto 的 scope 是 system:
  ```xml
  <dependencyManagement>
      <dependencies>
          <dependency>
              <groupId>taobao-sdk-java-auto_1479188381469-20190507</groupId>
              <artifactId>taobao-sdk-java-auto</artifactId>
              <version>${taobao-sdk.version}</version>
              <scope>system</scope>
              <systemPath>${basedir}/lib/taobao-sdk-java-auto_1479188381469-20190507.jar</systemPath>
          </dependency>

          <dependency>
              <groupId>com.example</groupId>
              <artifactId>demo-common</artifactId>
              <version>0.0.1-SNAPSHOT</version>
          </dependency>
      </dependencies>
  </dependencyManagement>
  ```

2. 在 demo-common 里添加 taobao-sdk-java-auto 依赖:
  ```xml
  <dependency>
      <groupId>taobao-sdk-java-auto_1479188381469-20190507</groupId>
      <artifactId>taobao-sdk-java-auto</artifactId>
  </dependency>
  ```

3. 在 demo-service 里添加 common 依赖:
  ```xml
  <dependencies>
      <dependency>
          <groupId>com.example</groupId>
          <artifactId>demo-common</artifactId>
      </dependency>
  </dependencies>
  ```

![scope-system](./images/maven-scope/system.png 'scope-system')

## import

maven 跟 java 一样，都是单继承，也就是说在子模块中只能出现一个 parent 标签; 但是，如果在父模块的 dependencyManagement 中预定义太多的依赖，会造成 pom 文件过长，不易维护; 此时就可以用 import 来分类管理依赖集合:

1. 将 dependency 分类，每一类依赖建立单独的 pom 文件
2. 在需要使用到这些依赖的子模块中，使用 dependencyManagement 管理依赖
3. <scope>import</scope> 只能定义在 dependencyManagement 里, 且仅用于 <type>pom</type> 的 dependency

一般，我们会将 springboot 添加到父模块的 <parent> 标签里:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>2.5.1</version>
    <relativePath/> <!-- lookup parent from repository -->
</parent>

<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```

我们也可以不使用 <parent> 标签，而使用 import 来将 springboot 添加到父模块里:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-dependencies</artifactId>
            <version>${spring-boot.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>${spring-cloud.version}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter</artifactId>
            <version>${spring-boot.version}</version>
        </dependency>
    </dependencies>
</dependencyManagement>
```
