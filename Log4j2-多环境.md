# Log4j2 多环境

## 前言

Log4j2 的初始化发生在 JVM 启动过程中，远早于 Spring Boot 容器的启动。加载顺序如下：

1. ```JVM``` 启动
2. ```Log4j2``` 自动配置 - 查找并加载 ```log4j2.xml```、```log4j2-test.xml``` 等
3. ```Spring Boot``` 启动
4. ```Spring Profile``` 激活 - 此时环境变量才被解析

***总结：***

- ***Log4j2 总是在 Spring Boot 启动之前加载，这是由 JVM 的类加载顺序决定的，是无法改变的。***
- ***多环境配置文件尽量不要包含 ```log4j2-test.xml```，这是因为 ```log4j2-test.xml``` 是 log4j2 默认的配置文件，如果包含了 ```log4j2-test.xml``` 的话，log4j2 自始至终都会加载```log4j2-test.xml```，这明显不符合多环境启动的预期。***

## JVM 命令行

- ```-Dspring.profiles.active=dev```: 设置环境变量(```dev/test/prod```)，也可以通过 ```application.yml``` 配置实现
- ```-Dlog4j2.debug=true```: 开启 Log4j2 调试日志

## 正确配置 Log4j2 多环境

### 通过公共模块实现

#### 在公共模块添加多环境配置文件

- log4j2-env-dev.xml
- log4j2-env-test.xml
- log4j2-env-prod.xml

```xml
<!-- log4j2-env-dev.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<configuration status="WARN">
    <properties>
        <!-- 定义日志文件的存储路径 -->
        <property name="LOG_PATH" value="./log"/>
        <!-- 文件的日志格式 -->
        <property name="LOG_PATTERN" value="%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{50} - %msg%n"/>
        <!-- 控制台的日志格式和颜色渲染 -->
        <property name="CONSOLE_LOG_PATTERN"
                  value="%style{%d{yyyy-MM-dd HH:mm:ss.SSS}}{bright,green} %highlight{${LOG_LEVEL_PATTERN:-%5p}} %style{%pid}{magenta} %style{---}{bright,blue} %style{[%15.15t]}{bright,yellow} %style{%-40.40logger{39}}{cyan} %style{:}{red} %m%n%throwable"/>
    </properties>

    <appenders>
        <!-- 输出到控制台 -->
        <console name="console" target="SYSTEM_OUT">
            <!-- 输出到控制台的日志格式，一定要启用ansi，否则颜色渲染无效 -->
            <patternLayout pattern="${CONSOLE_LOG_PATTERN}" disableAnsi="false" noConsoleNoAnsi="false"/>
        </console>

        <rollingFile name="file" fileName="${LOG_PATH}/app.dev.log"
                     filePattern="${LOG_PATH}/app.dev.%d{yyyyMMdd}.%i.log.gz">
            <patternLayout pattern="${LOG_PATTERN}"/>
            <policies>
                <sizeBasedTriggeringPolicy size="10MB"/>
            </policies>
            <defaultRolloverStrategy max="5">
                <delete basePath="${LOG_PATH}/" maxDepth="1">
                    <ifFileName glob="*.log.gz" />
                    <ifLastModified age="2d" />
                </delete>
            </defaultRolloverStrategy>
        </rollingFile>
    </appenders>

    <loggers>
        <root level="INFO">
            <appender-ref ref="console"/>
            <appender-ref ref="file"/>
        </root>
    </loggers>
</configuration>
```

```xml
<!-- log4j2-env-test.xml -->
<!-- 略-->
        <rollingFile name="file" fileName="${LOG_PATH}/app.test.log"
                     filePattern="${LOG_PATH}/app.test.%d{yyyyMMdd}.%i.log.gz">
<!-- 略-->
```

```xml
<!-- log4j2-env-prod.xml -->
<!-- 略-->
        <rollingFile name="file" fileName="${LOG_PATH}/app.prod.log"
                     filePattern="${LOG_PATH}/app.prod.%d{yyyyMMdd}.%i.log.gz">
<!-- 略-->
```

#### 方案一：使用系统属性/环境变量直接指定

```bash
# 方法1: 启动时直接指定
java -Dlog4j.configurationFile=classpath:log4j2-env-prod.xml -jar app.jar

# 方法2: 使用环境变量
export LOG4J_CONFIG_FILE=classpath:log4j2-env-dev.xml
java -jar app.jar

# 方法3: 在 Dockerfile 或启动脚本中设置
#!/bin/bash
ENV=${SPRING_PROFILES_ACTIVE:-dev}
java -Dlog4j.configurationFile=classpath:log4j2-env-${ENV}.xml -jar app.jar
```

#### 方案二：通过 Spring 中转 spring.profiles.active 为系统参数

实现 EnvironmentPostProcessor，中转 ```spring.profiles.active``` 为系统参数：

```java
public class ApplicationEnvironmentListener implements ApplicationListener<ApplicationEnvironmentPreparedEvent> {
    @Override
    public void onApplicationEvent(ApplicationEnvironmentPreparedEvent event) {
        ConfigurableEnvironment environment = event.getEnvironment();

        log4j2Handler(environment);
    }

    /**
     * log4j2处理器
     *
     * @param environment
     */
    private void log4j2Handler(ConfigurableEnvironment environment) {
        try {
            // 读取 spring.profiles.active（从 application.yml 或命令行参数）
            String activeProfile = environment.getProperty("spring.profiles.active", "dev");
            // 禁用 Log4j2 懒加载和默认配置，避免重复初始化
            System.setProperty("log4j2.disableLazyInit", "true");
            System.setProperty("log4j2.disableDefaultConfiguration", "true");
            // 获取 Log4j2 上下文（false 表示不创建新上下文，使用当前应用上下文）
            LoggerContext context = (LoggerContext) LogManager.getContext(false);
            String configPath = "log4j2-env-" + activeProfile + ".xml";
            URI configUri = new ClassPathResource(configPath).getURI();
            // 设置配置路径并启动 Log4j2 上下文
            context.setConfigLocation(configUri);
            context.start();
        } catch (Exception e) {
            throw new RuntimeException("log4j2 初始化失败", e);
        }
    }
}
```

在 ```src/main/resources/META-INF/spring.factories``` 中注册 ```ApplicationListener```：

```
org.springframework.context.ApplicationListener=com.zhy.listener.ApplicationEnvironmentListener
```

### 通过 SpringProfile 标签实现

```xml
<!-- log4j2.xml 或 log4j2-spring.xml -->
<?xml version="1.0" encoding="UTF-8"?>
<Configuration status="WARN" monitorInterval="30">
    <!-- 全局属性定义 -->
    <Properties>
        <Property name="LOG_EXCEPTION_CONVERSION_WORD">%xwEx</Property>
        <Property name="LOG_LEVEL_PATTERN">%5p</Property>
        <Property name="LOG_DATEFORMAT_PATTERN">yyyy-MM-dd'T'HH:mm:ss.SSSXXX</Property>
        <Property name="CONSOLE_LOG_PATTERN">%clr{%d{${sys:LOG_DATEFORMAT_PATTERN}}}{faint} %clr{${sys:LOG_LEVEL_PATTERN}} %clr{%pid}{magenta} %clr{--- %esb{${sys:APPLICATION_NAME:-}}%esb{${sys:APPLICATION_GROUP:-}}[%15.15t] ${sys:LOG_CORRELATION_PATTERN:-}}{faint}%clr{%-40.40c{1.}}{cyan} %clr{:}{faint} %m%n${sys:LOG_EXCEPTION_CONVERSION_WORD}</Property>
        <Property name="FILE_LOG_PATTERN">%d{${sys:LOG_DATEFORMAT_PATTERN}} ${sys:LOG_LEVEL_PATTERN} %pid --- %esb{${sys:APPLICATION_NAME:-}}%esb{${sys:APPLICATION_GROUP:-}}[%t] ${sys:LOG_CORRELATION_PATTERN:-}%-40.40c{1.} : %m%n${sys:LOG_EXCEPTION_CONVERSION_WORD}</Property>
        <Property name="LOG_PATH">./logs</Property>
    </Properties>

    <Appenders>
        <!-- 1. 开发环境（dev）：滚动文件 + 详细输出到控制台 -->
        <SpringProfile name="dev">
            <Console name="Console" target="SYSTEM_OUT">
                <PatternLayout pattern="${CONSOLE_LOG_PATTERN}" />
            </Console>
            <RollingFile name="RollingFile"
                         fileName="${LOG_PATH}/app-dev.log"
                         filePattern="${LOG_PATH}/app-dev-%d{yyyy-MM-dd}.log">
                <PatternLayout pattern="${FILE_LOG_PATTERN}"/>
                <Policies>
                    <TimeBasedTriggeringPolicy/>
                </Policies>
            </RollingFile>
        </SpringProfile>

        <!-- 2. 生产环境（prod）：滚动文件 + 控制台只输出错误 -->
        <SpringProfile name="prod">
            <Console name="Console" target="SYSTEM_OUT">
                <PatternLayout pattern="${CONSOLE_LOG_PATTERN}" />
                <!-- 生产环境控制台只打印WARN及以上级别 -->
                <ThresholdFilter level="WARN" onMatch="ACCEPT" onMismatch="DENY"/>
            </Console>
            <RollingFile name="RollingFile"
                         fileName="${LOG_PATH}/app-prod.log"
                         filePattern="${LOG_PATH}/app-prod-%d{yyyy-MM-dd}-%i.log.gz">
                <PatternLayout pattern="${FILE_LOG_PATTERN}"/>
                <Policies>
                    <TimeBasedTriggeringPolicy interval="1" modulate="true"/>
                    <SizeBasedTriggeringPolicy size="100 MB"/>
                </Policies>
                <DefaultRolloverStrategy max="10"/>
            </RollingFile>
        </SpringProfile>

        <!-- 3. 测试环境（test）：滚动文件 + 输出到文件 -->
        <SpringProfile name="test">
            <Console name="Console" target="SYSTEM_OUT">
                <PatternLayout pattern="${CONSOLE_LOG_PATTERN}" />
            </Console>
            <RollingFile name="RollingFile"
                         fileName="${LOG_PATH}/app-test.log"
                         filePattern="${LOG_PATH}/app-test-%d{yyyy-MM-dd}.log">
                <PatternLayout pattern="${FILE_LOG_PATTERN}"/>
                <Policies>
                    <TimeBasedTriggeringPolicy/>
                </Policies>
            </RollingFile>
        </SpringProfile>
    </Appenders>

    <Loggers>
        <!-- 第三方框架日志级别控制 -->
        <Logger name="org.springframework" level="INFO" additivity="false"/>
        <!-- 根日志配置 -->
        <Root level="INFO">
            <!-- 根据激活的Profile，自动引用对应的Appender -->
            <SpringProfile name="dev">
                <AppenderRef ref="Console"/>
                <AppenderRef ref="RollingFile"/>
            </SpringProfile>
            <SpringProfile name="prod">
                <AppenderRef ref="Console"/>
                <AppenderRef ref="RollingFile"/>
            </SpringProfile>
            <SpringProfile name="test">
                <AppenderRef ref="Console"/>
                <AppenderRef ref="RollingFile"/>
            </SpringProfile>
        </Root>
    </Loggers>
</Configuration>
```

应用：

1. 激活Profile：通过启动参数（如 ```-Dspring.profiles.active=prod```）、环境变量或在 ```application.properties``` 中设置 ```spring.profiles.active``` 来指定环境。
2. 启动应用：启动你的 Spring Boot 应用。Log4j2 会读取 ```log4j2.xml/log4j2-spring.xml``` 并根据激活的 Profile 应用对应的日志配置。
