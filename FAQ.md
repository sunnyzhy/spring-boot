# FAQ

## 1 PathVariable annotation was empty on param 0

- 原因
   远程调用Feign的时候报错，PathVariable注解为空。

- 解决方法
    @PathVariable注释要写明value
    ```java
    @PathVariable(value = "id") Integer id
    ```

## 2 @Value注解报Could not resolve placeholder错误

- 原因
    存在多个properties配置文件，即除了application.properties之外，还有自定义的xxx.properties。

    Spring容器采用反射扫描的发现机制，在探测到Spring容器中有一个org.springframework.beans.factory.config.PropertyPlaceholderConfigurer的Bean就会停止对剩余PropertyPlaceholderConfigurer的扫描，所以根据加载的顺序，配置的第二个property-placeholder就被没有被spring加载，所以在使用@Value注入的时候占位符就解析不了。

- 解决方法
    合并properties配置文件:

    ```xml
        <bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
            <property name="locations">
                <list>
                    <value>classpath:application.properties</value>
                    <value>classpath:nnn/xxx.properties</value>
                </list>
            </property>
        </bean>
    ```

## 3 Cannot determine embedded database driver class for database type NONE

- 原因
    配置文件中缺少DataSource配置

- 解决方法
    在springboot的启动类上禁止自动初始化DataSource:

    ```java
    @SpringBootApplication(exclude = {DataSourceAutoConfiguration.class})
    ```

## 4 spring boot 单元测试的时候报 BeanCreationNotAllowedException: Error creating bean with name 'eurekaAutoServiceRegistration'

[github解决方案](https://github.com/spring-cloud/spring-cloud-netflix/issues/1952 "github解决方案")

```
The workaround works when running the application, but not when running tests disappointed.
```

## 5 The field file exceeds its maximum permitted size of 1048576 bytes.

- 原因
    Spring Boot 内置的 tomcat 限制了请求的文件大小。

- 解决方法
    Spring Boot1.4 之后的版本配置:

    ```
    spring.http.multipart.maxFileSize = 10Mb
    spring.http.multipart.maxRequestSize = 100Mb
    ```

    Spring Boot2.0 之后的版本配置:

    ```
    spring.servlet.multipart.max-file-size = 10MB  
    spring.servlet.multipart.max-request-size = 100MB
    ```

**如果不限制文件上传的大小，就把两个值都设置为-1。**

## 6 post 请求，后台已执行，但是返回 404

解决方法:

- 用 @Controller 注解修饰的 controller ，需加上 ```@ResponseBody``` 注解
- 用 ```@RestController``` 注解修饰 controller

## 7 RestController 报错 404

SpringBoot 启动时候，启动类从当前包开始扫描子级包，所以如果 Controller 不是启动类所在包的子级包，是不会被扫描进 IOC 容器并进行自动配置的。此时，需要将 Controller 所在的包修改为启动类的子级包。

例如，Controller 所在的包是 ```package com.example.controller;```，而主启动类所在的包是 ```package com.example.demo;```，由于 Controller 所在的包不是启动类所在包的子级包，所以前端访问 Controller 接口的时候就会抛出 404 的异常。

解决方法:

把 Controller 所在的包修改为 ```package com.example.demo.controller;``` 使其成为  ```package com.example.demo;``` 的子级包。

## 8 Eureka 在有虚拟网卡的情况下获取正确的IP

### 解决方法

```yml
spring:
  cloud:
    inetutils:
      ignored-interfaces: ## 忽略网卡
      - VMware.*
```

### 源码分析

#### 8.1 EurekaClientAutoConfiguration

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\netflix\eureka\EurekaClientAutoConfiguration.class

@Bean
@ConditionalOnMissingBean(
    value = {EurekaInstanceConfig.class},
    search = SearchStrategy.CURRENT
)
public EurekaInstanceConfigBean eurekaInstanceConfigBean(InetUtils inetUtils, ManagementMetadataProvider managementMetadataProvider) {
    // ...
    EurekaInstanceConfigBean instance = new EurekaInstanceConfigBean(inetUtils);
    // ...
    return instance;
}
```

#### 8.2 EurekaInstanceConfigBean

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\netflix\eureka\EurekaInstanceConfigBean.class

public EurekaInstanceConfigBean(InetUtils inetUtils) {
    // ...
    this.hostInfo = this.inetUtils.findFirstNonLoopbackHostInfo();
    // ...
}
```

#### 8.3 findFirstNonLoopbackHostInfo()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

public InetUtils.HostInfo findFirstNonLoopbackHostInfo() {
    InetAddress address = this.findFirstNonLoopbackAddress();
    // ...
}
```

#### 8.4 findFirstNonLoopbackAddress()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

public InetAddress findFirstNonLoopbackAddress() {
    // ...

    try {
        // ...
        while(true) {
            NetworkInterface ifc;
            do {
                // ...
            } while(this.ignoreInterface(ifc.getDisplayName()));

            // ...
        }
    } catch (IOException var8) {
        this.log.error("Cannot get first non-loopback address", var8);
    }

    // ...
}
```

#### 8.5 ignoreInterface()

```java
// 源码: spring-cloud-netflix-eureka-client-3.0.3.jar\org\springframework\cloud\commons\util\InetUtils.class

boolean ignoreInterface(String interfaceName) {
    // 从配置文件里读取需要忽略的网卡
    Iterator var2 = this.properties.getIgnoredInterfaces().iterator();

    String regex;
    do {
        if (!var2.hasNext()) {
            return false;
        }

        regex = (String)var2.next();
    } while(!interfaceName.matches(regex));

    this.log.trace("Ignoring interface: " + interfaceName);
    return true;
}
```

## 9 单元测试

### 9.1 Unable to find a @SpringBootConfiguration, you need to use @ContextConfiguration or @SpringBootTest(classes=...) with your test

1. 没有写启动类
2. 虽然写了启动类，但是启动类的包名和单元测试的包名不同。比如启动类的包名是 ```org.springframework.cloud.pattern```，单元测试的包名是 ```org.springframework.cloud```。解决方法有两种：
   1. 把启动类的包名和单元测试的包名修改为同一个
   2. 在 ```@SpringBootTest``` 注解里指定 ```启动类```，即 ```@SpringBootTest(classes = 启动类.class)```

### 9.2 Failed to load ApplicationContext

检查 application.yml 或 application.properties 的配置项是否有错误或不完整的地方。

## 10 为什么在业务模块的 pom#properties 加上 ＜log4j2.version＞ 配置就可以更新 log4j2 的版本

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
3. 在 ```spring-boot-dependencies-2.5.7.pom``` 里搜索 ```log4j2.version```
   ```xml
       <properties>
           // ...
           <log4j2.version>2.14.1</log4j2.version>
           // ...
       </properties>
   ```
4. 发现 spring-boot 2.5.7 默认使用的 log4j2 的版本号是 2.14.1, 所以，我们只需要在业务模块的 pom#properties 里显式地配置 ```<properties><log4j2.version>2.17.0</log4j2.version></properties>```，就可以覆盖 spring-boot 默认的 log4j2 的版本号，从而实现版本的更新。

## 11 Druid 异常 discard long time none received connection.

### 11.1 分析 testConnectionInternal 源码

```java
// 源码: \com\alibaba\druid\1.2.8\druid-1.2.8.jar!\com\alibaba\druid\pool\DruidAbstractDataSource.class

public abstract class DruidAbstractDataSource extends WrapperAdapter implements DruidAbstractDataSourceMBean, DataSource, DataSourceProxy, Serializable {
    // ...
    
    public DruidAbstractDataSource(boolean lockFair) {
        // ...
        this.timeBetweenEvictionRunsMillis = 60000L;
        // ...
    }

    protected boolean testConnectionInternal(DruidConnectionHolder holder, Connection conn) {
        // ...

                valid = this.validConnectionChecker.isValidConnection(conn, this.validationQuery, this.validationQueryTimeout);
                
                // ...
                
                if (valid && this.isMySql) {
                    long lastPacketReceivedTimeMs = MySqlUtils.getLastPacketReceivedTimeMs(conn);
                    if (lastPacketReceivedTimeMs > 0L) {
                        long mysqlIdleMillis = currentTimeMillis - lastPacketReceivedTimeMs;
                        if (lastPacketReceivedTimeMs > 0L && mysqlIdleMillis >= this.timeBetweenEvictionRunsMillis) {
                            this.discardConnection(holder);
                            String errorMsg = "discard long time none received connection. , jdbcUrl : " + this.jdbcUrl + ", version : " + VERSION.getVersionNumber() + ", lastPacketReceivedIdleMillis : " + mysqlIdleMillis;
                            LOG.warn(errorMsg);
                            boolean var13 = false;
                            return var13;
                        }
                    }
                }
        // ...
    }
    
    // ...
}
```

```MySqlUtils.getLastPacketReceivedTimeMs(conn)``` 是获取上一次使用的时间，mysqlIdleMillis 是计算出来的空闲时间，timeBetweenEvictionRunsMillis 是常量 60 秒。如果连接空闲了 60 秒以上，就调用 ```this.discardConnection(holder)``` 丢弃这个旧连接，并打印日志 ```LOG.warn(errorMsg)```。

### 11.2 分析 isValidConnection 源码, 即上述方法中 valid 的赋值过程

```java
// 源码: .\com\alibaba\druid\1.2.8\druid-1.2.8.jar!\com\alibaba\druid\pool\vendor\MySqlValidConnectionChecker.class

public class MySqlValidConnectionChecker extends ValidConnectionCheckerAdapter implements ValidConnectionChecker, Serializable {
    // ...
    public static final String DEFAULT_VALIDATION_QUERY = "SELECT 1";
    private boolean usePingMethod = false;
    
    public MySqlValidConnectionChecker() {
        // ...

        this.configFromProperties(System.getProperties());
    }
    
    // 把属性 "druid.mysql.usePingMethod" 的值赋值给 usePingMethod
    public void configFromProperties(Properties properties) {
        if (properties != null) {
            String property = properties.getProperty("druid.mysql.usePingMethod");
            if ("true".equals(property)) {
                this.setUsePingMethod(true);
            } else if ("false".equals(property)) {
                this.setUsePingMethod(false);
            }

        }
    }
    
    public void setUsePingMethod(boolean usePingMethod) {
        this.usePingMethod = usePingMethod;
    }
    
    // ...
    
    public boolean isValidConnection(Connection conn, String validateQuery, int validationQueryTimeout) throws Exception {
        if (conn.isClosed()) { // 如果数据库连接关闭，就返回 false
            return false;
        } else {
            if (this.usePingMethod) { // 使用 ping 的方式检查数据库连接，如果有异常，就抛出异常；否则，总是返回 true
                // ...

                    try {
                        this.ping.invoke(conn, true, validationQueryTimeout * 1000);
                        return true;
                    } catch (InvocationTargetException var11) {
                        Throwable cause = var11.getCause();
                        if (cause instanceof SQLException) {
                            throw (SQLException)cause;
                        }

                        throw var11;
                    }
                }
            }

            // 使用数据库查询 "SELECT 1" 的方式检查数据库连接，如果查询能返回结果，就返回 true；否则，返回 false
            String query = validateQuery;
            if (validateQuery == null || validateQuery.isEmpty()) {
                query = "SELECT 1";
            }

            Statement stmt = null;
            ResultSet rs = null;

            boolean var7;
            try {
                stmt = conn.createStatement();
                if (validationQueryTimeout > 0) {
                    stmt.setQueryTimeout(validationQueryTimeout);
                }

                rs = stmt.executeQuery(query);
                var7 = true;
            } finally {
                JdbcUtils.close(rs);
                JdbcUtils.close(stmt);
            }

            return var7;
        }
    }
}
```

### 11.3 解决方法, 禁用 PingMethod

- 在```启动参数```中添加如下参数:

   ```-Ddruid.mysql.usePingMethod=false```

- 在 Spring Boot 项目的```启动类```中添加如下代码:

   ```java
    System.setProperty("druid.mysql.usePingMethod","false");
   ```

## 12 spring-cloud-starter-gateway 异常

### 12.1 java.net.SocketException: Too many open files

网关的异常日志:

```
feign.RetryableException: Too many open files executing POST http://xxx
	at feign.FeignException.errorExecuting(FeignException.java:249) ~[feign-core-10.12.jar!/:?]
	at feign.SynchronousMethodHandler.executeAndDecode(SynchronousMethodHandler.java:129) ~[feign-core-10.12.jar!/:?]
	at feign.SynchronousMethodHandler.invoke(SynchronousMethodHandler.java:89) ~[feign-core-10.12.jar!/:?]
	at feign.ReflectiveFeign$FeignInvocationHandler.invoke(ReflectiveFeign.java:100) ~[feign-core-10.12.jar!/:?]
	at com.sun.proxy.$Proxy202.feign方法(Unknown Source) ~[?:?]
	at feign 调用目标服务接口时异常的详细堆栈信息 ~[classes!/:2.11.2.6214]
	at sun.reflect.GeneratedMethodAccessor199.invoke(Unknown Source) ~[?:?]
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:1.8.0_202]
	at java.lang.reflect.Method.invoke(Method.java:498) ~[?:1.8.0_202]
... ...
Caused by: java.net.SocketException: Too many open files
	at java.net.Socket.createImpl(Socket.java:460) ~[?:1.8.0_202]
	at java.net.Socket.getImpl(Socket.java:520) ~[?:1.8.0_202]
	at java.net.Socket.setSoTimeout(Socket.java:1141) ~[?:1.8.0_202]
	at okhttp3.internal.connection.RealConnection.connectSocket(RealConnection.kt:293) ~[okhttp-4.9.3.jar!/:?]
... ...
```

- 原因
   打开文件数（包含文件和 Socket 连接数）超过 Linux 设置的最大值
- 解决方法
    1. 使用 ```ps -ef | grep 目标服务的名称``` 查看 feign 调用的目标服务的进程 id, 即 pid
    2. 使用 ```lsof -p pid | wc -l```  查看目标进程打开的文件数
    3. 使用 ```cat /proc/pid/limits```  查看目标进程能打开的最大文件数限制:
	    ```bash
	    Limit                     Soft Limit           Hard Limit           Units     
	    Max cpu time              unlimited            unlimited            seconds   
	    Max file size             unlimited            unlimited            bytes     
	    Max data size             unlimited            unlimited            bytes     
	    Max stack size            8388608              unlimited            bytes     
	    Max core file size        0                    unlimited            bytes     
	    Max resident set          unlimited            unlimited            bytes     
	    Max processes             95695                95695                processes 
	    Max open files            65536                65536                files     
	    Max locked memory         65536                65536                bytes     
	    Max address space         unlimited            unlimited            bytes     
	    Max file locks            unlimited            unlimited            locks     
	    Max pending signals       95695                95695                signals   
	    Max msgqueue size         819200               819200               bytes     
	    Max nice priority         0                    0                    
	    Max realtime priority     0                    0                    
	    Max realtime timeout      unlimited            unlimited            us        
	    ```

	    ***Max open files 为 65536***

    4. 使用 ```ulimit -a``` 查看 Linux 最大文件数限制 ，如果出现 ```ulimit -a``` 查看的 ```open files``` 和 ```cat /proc/pid/limits``` 查看的 ```open files``` 不一致的情况，那就有可能是在程序运行中修改过系统 ```open files``` 的大小，而运行中的程序只会以启动时的 ```open files``` 大小为准。
	    ```bash
	    # ulimit -a
	    core file size          (blocks, -c) 0
	    data seg size           (kbytes, -d) unlimited
	    scheduling priority             (-e) 0
	    file size               (blocks, -f) unlimited
	    pending signals                 (-i) 95695
	    max locked memory       (kbytes, -l) 64
	    max memory size         (kbytes, -m) unlimited
	    open files                      (-n) 65536
	    pipe size            (512 bytes, -p) 8
	    POSIX message queues     (bytes, -q) 819200
	    real-time priority              (-r) 0
	    stack size              (kbytes, -s) 8192
	    cpu time               (seconds, -t) unlimited
	    max user processes              (-u) 95695
	    virtual memory          (kbytes, -v) unlimited
	    file locks                      (-x) unlimited
	    ```

	    ***open files 为 65536***

    5. 修改 Linux 最大文件数限制
	    ```bash
	    # vim /etc/security/limits.conf
	    * soft nofile 655360
	    * hard nofile 655360
	    ```
    6. 如果程序运行一段时间之后，又出现 ```Too many open files``` 异常，就用以下方法分析是什么句柄占用最多:
	    ```bash
	    # lsof -p pid> lsof.log
	    # cat lsof.log | awk '{print $8}' | sort | uniq -c | sort -rn | head -n 10
	    ```

## 13 The bean 'xxxService' could not be injected because it is a JDK dynamic proxy

***场景***

```java
public interface xxxInterface {
    void send(String message);
}

@Service
@Slf4j
public class xxxService implements xxxInterface {
    @Override
    public void send(String message) {
        // TODO
    }
    
    @Async
    public void threadStart() {
        // TODO
    }
}

@Component
@Slf4j
public class ApplicationRunnerImpl implements ApplicationRunner {
    @Autowired
    private xxxService xxx;

    @Override
    public void run(ApplicationArguments arg0) throws Exception {
        xxx.threadStart();
    }
}
```

***异常日志***

```
The bean 'xxxService' could not be injected because it is a JDK dynamic proxy

The bean is of type 'com.sun.proxy.$Proxy172' and implements:
	xxxInterface
	org.springframework.aop.SpringProxy
	org.springframework.aop.framework.Advised
	org.springframework.core.DecoratingProxy

Expected a bean of type 'xxxService' which implements:
	xxxInterface

Action:

Consider injecting the bean as one of its interfaces or forcing the use of CGLib-based proxies by setting proxyTargetClass=true on @EnableAsync and/or @EnableCaching.
```

***原因***

xxxService 是一个 JDK 动态代理，所以不能作为 bean 被注入:

1. xxxService 类实现了 xxxInterface 接口
2. xxxService 类里有一个配置了 ```@Async``` 注解的线程函数

***解决方法***

- 根据 ```Action``` 里的提示，可以在 ```@EnableAsync``` 注解上配置 ```proxyTargetClass=true```
   ```java
   @EnableAsync(proxyTargetClass = true)
   ```
- 不要在实现了接口的类里使用 ```@Async``` 注解，可以封装一个普通类，在普通类里定义一个多线程函数来调用 ```xxxService.threadStart()``` 方法
	```java
	@Service
	@Slf4j
	public class xxxService implements xxxInterface {
	    @Override
	    public void send(String message) {
		// TODO
	    }

	    public void threadStart() {
		// TODO
	    }
	}

	@Service
	public class CommonService {
	    @Autowired
	    private xxxService xxx;

	    @Async
	    public void threadStart() {
		xxx.threadStart();
	    }
	}

	@Component
	@Slf4j
	public class ApplicationRunnerImpl implements ApplicationRunner {
	    @Autowired
	    private CommonService commonService;

	    @Override
	    public void run(ApplicationArguments arg0) throws Exception {
		commonService.threadStart();
	    }
	}
	```

## 14 MultipartFile 对象报 FileNotFoundException

报错信息:

```
java.io.FileNotFoundException: C:\Users\Administrator\AppData\Local\Temp\tomcat.8702.692273666709752187\work\Tomcat\localhost\ROOT\upload_937d1ca3_fcf7_4c63_b104_7c8126932469_00000001.tmp (系统找不到指定的文件。)
```

***原因***

MultipartFile 文件会存储在临时文件夹中，如:

```
C:\Users\Administrator\AppData\Local\Temp\tomcat.8702.692273666709752187\work\Tomcat\localhost\ROOT\
```

- 如果是单线程解析文件，是不会有问题的

- 如果是多线程解析文件，主线程一旦结束，临时文件就会被清空；这时候子线程再读取 MultipartFile 文件，就会报错 FileNotFound
   
   ***类似于主线程结束之后，子线程就读取不到 http 上下文了。***

***解决方法***

由于主线程结束，临时文件被清空，导致多线程无法读取临时文件而报错 FileNotFound。具体做法如下:

1. 在主线程中获取文件流对象:

   ```java
   InputStream inputStream = file.getInputStream();
   ```

   或者

   ```java
   List<InputStream> inputStreamList = new ArrayList<>();
   for (int i = 0; i < 5; i++) {
       inputStreamList.add(file.getInputStream());
   }
   ```

2. 把文件流对象以参数的形式传递给子线程:

   ```java
   doMethodThread(inputStream);
   ```

   或者

   ```java
   doMethodThread(List<InputStream>);
   ```

## 15 读取 Excel文件，报错 Cleaning up unclosed ZipFile for archive

使用文件流读取 excel 的时候，一定要在末尾加上关闭流的操作。

## 16 NoClassDefFoundError

原因：

- 没有添加相应的依赖
- 存在相应的依赖，但是子依赖与父依赖的版本不一致（这种情况的概率要大一些）
   ![NoClassDefFound](./images/NoClassDefFound.png 'NoClassDefFound')

解决：

打开 ***```父模块```*** 的 ```spring-boot-dependencies```，搜索相应的依赖：

- 如果存在，就在 ***```子模块```*** 里指定相应依赖的版本:
   ```xml
    <properties>
        <selenium.version>4.9.1</selenium.version>
    </properties>
   ```
- 如果不存在，就在 ***```子模块```*** 里添加相应依赖:
   ```xml
        <dependency>
            <groupId>org.seleniumhq.selenium</groupId>
            <artifactId>selenium-java</artifactId>
            <version>4.9.1</version>
        </dependency>
   ```

## 17 JDK21 报错 java.lang.NoSuchFieldError: Class com.sun.tools.javac.tree.JCTree$JCImport does not have member field 'com.sun.tools.javac.tree.JCTree qualid'

原因：

- lombok 版本兼容性的问题

解决：

- 更新 lombok 版本
   ```xml
   <dependency>
       <groupId>org.projectlombok</groupId>
       <artifactId>lombok</artifactId>
       <version>1.18.30</version>
   </dependency>
   ```
