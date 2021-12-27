# dependencyManagement 标签

## dependencyManagement 与 dependencies 的区别

- 所有声明在 dependencies 里的依赖都会自动引入，并默认被所有的子项目继承

- dependencies 即使在子项目中不写该依赖项，那么子项目仍然会从父项目中继承该依赖项（全部继承）

- dependencyManagement 只是声明依赖的版本号，该依赖不会引入，因此子项目需要显示声明所需要引入的依赖，若不声明则不引入

- 子项目声明了依赖且未声明版本号和 scope，则会继承父项目的版本号和 scope，否则覆盖

``` xml
<!-- 只是声明依赖，并不引入 -->
<dependencyManagement>
  <dependencies>
    // ...
  </dependencies>
</dependencyManagement>

<!-- 声明并引入依赖 -->
<dependencies>
  // ...
</dependencies>
```

## dependencyManagement 的作用

### 1 声明依赖

``` xml
<dependencyManagement>
  <dependencies>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt</artifactId>
        <version>${jjwt.version}</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

### 2 导入父模块

``` xml
<dependencyManagement>
  <dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-dependencies</artifactId>
        <version>${spring-cloud.version}</version>
        <type>pom</type>
        <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```

 ***必须添加 ```<type>pom</type>``` 和 ```<scope>import</scope>``` 标签。***

## 添加依赖声明时避免重复添加

***如果是已经包含在 spring-boot-dependencies 和 spring-cloud-dependencies 里的依赖，就不需要在父模块的 pom 里重复声明; 否则，就需要在父模块的 pom 里显式声明。***

### ```spring-boot-starter-web``` 依赖声明

1. ctrl + 左键，点击业务模块的 pom 文件中 ```<parent>```里的任意内容
   
   ```xml
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-parent</artifactId>
           <version>2.5.7</version>
           <relativePath/> <!-- lookup parent from repository -->
       </parent>
   ```
   
   跳转到 ```spring-boot-starter-parent-2.5.7.pom```

2. ctrl + 左键，点击 ```spring-boot-starter-parent-2.5.7.pom#<parent>```里的任意内容
   
   ```xml
       <parent>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-dependencies</artifactId>
           <version>2.5.7</version>
       </parent>
   ```
   
   跳转到 ```spring-boot-dependencies-2.5.7.pom```

3. 在 ```spring-boot-dependencies-2.5.7.pom``` 里搜索 ```spring-boot-starter-web```
   
   ```xml
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
           <version>2.5.7</version>
       </dependency>
   ```

4. 所以，不需要在业务模块的 pom 文件里声明 ```spring-boot-starter-web``` 依赖，直接在```子业务模块```里显式地引用即可。
   
   ```xml
       <dependency>
           <groupId>org.springframework.boot</groupId>
           <artifactId>spring-boot-starter-web</artifactId>
       </dependency>
   ```

5. 其它依赖，诸如: ```spring-boot-starter-security```, ```mysql-connector-java```, ```lombok```, ```commons-lang3```，```elasticsearch```, 都无须额外声明。

### ```spring-cloud-starter-gateway``` 依赖声明

1. ctrl + 左键，点击业务模块的 pom 文件中 ```<dependency>```里的任意内容
   
   ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2020.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    <dependencyManagement>
   ```
   
   跳转到 ```spring-cloud-dependencies-2020.0.5.pom```

2. 在 ```spring-cloud-dependencies-2020.0.5.pom```里搜索 ```spring-cloud-gateway```
   
   ```xml
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-gateway-dependencies</artifactId>
        <version>${spring-cloud-gateway.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
   ```

3. ctrl + 左键，点击 ```<dependency>```里的任意内容
   
   ```xml
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-gateway-dependencies</artifactId>
        <version>${spring-cloud-gateway.version}</version>
        <type>pom</type>
        <scope>import</scope>
      </dependency>
   ```
   
   跳转到 ```spring-cloud-gateway-dependencies-3.0.6.pom```

4. 在 ```spring-cloud-gateway-dependencies-3.0.6.pom``` 里搜索 ```spring-cloud-starter-gateway```
   
   ```xml
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
        <version>${project.version}</version>
      </dependency>
   ```

5. 所以，不需要在业务模块的 pom 文件里声明 ```spring-cloud-starter-gateway``` 依赖，直接在```子业务模块```里显示地引用即可。

   ```xml
      <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-gateway</artifactId>
      </dependency>
   ```

6. 其它依赖，诸如: ```spring-cloud-starter-netflix-eureka-client```, ```spring-cloud-starter-openfeign```，都无须额外声明。

## 更新依赖的版本号

### 更新 spring-boot 内部默认加载的依赖项的版本号

#### parent 继承

- 更新 log4j2 的版本号

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

- 更新 lombok 的版本号

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <properties>
        <lombok.version>1.18.22</lombok.version>
    </properties>
   ```

#### dependencyManagement 继承

- 更新 log4j2 的版本号，***log4j 依赖必须声明在 spring-boot-dependencies 依赖之前。***

   ```xml
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.apache.logging.log4j</groupId>
                <artifactId>log4j-bom</artifactId>
                <version>2.17.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

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

- 更新 lombok 的版本号
   
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

            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>1.18.22</version>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

### 更新 spring-cloud 内部默认加载的依赖项的版本号

#### parent 继承

- 更新 spring-cloud-starter-openfeign 的版本号，***spring-cloud-openfeign-dependencies 依赖必须声明在 spring-cloud-dependencies 依赖之前。***

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-openfeign-dependencies</artifactId>
                <version>3.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2020.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

- 更新 spring-cloud-starter-netflix-eureka-client(server) 的版本号，***spring-cloud-netflix-dependencies 依赖必须声明在 spring-cloud-dependencies 依赖之前。***

   ```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.7</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-netflix-dependencies</artifactId>
                <version>3.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2020.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

#### dependencyManagement 继承

- 更新 spring-cloud-starter-openfeign 的版本号，***spring-cloud-openfeign-dependencies 依赖必须声明在 spring-cloud-dependencies 依赖之前。***

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

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-openfeign-dependencies</artifactId>
                <version>3.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2020.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```

- 更新 spring-cloud-starter-netflix-eureka-client(server) 的版本号，***spring-cloud-netflix-dependencies 依赖必须声明在 spring-cloud-dependencies 依赖之前。***

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

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-netflix-dependencies</artifactId>
                <version>3.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>org.springframework.cloud</groupId>
                <artifactId>spring-cloud-dependencies</artifactId>
                <version>2020.0.5</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
   ```
