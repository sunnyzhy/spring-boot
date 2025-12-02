# Log4j2 Profile

## 一、配置根节点（Configuration）

所有 Log4j2 配置的顶层节点，控制全局行为，核心配置项如下：

|配置项|类型|说明|可选值/示例|
|--|--|--|--|
|status|String|Log4j2|自身日志级别（避免自身日志刷屏，建议 WARN）|```OFF/FATAL/ERROR/WARN/INFO/DEBUG```（默认 WARN）|
|monitorInterval|int|配置热更新间隔（单位：秒），无需重启应用即可生效|30（每 30 秒刷新一次）|
|name|String|配置名称（标识当前配置，多配置场景用）|log4j2-dev-config|
|schema/xmlns|String|XML 格式专用，指定 Log4j2 命名空间（确保标签生效）|```http://logging.apache.org/log4j/2.0/config```|
|packages|String|扫描自定义插件（如自定义 Lookup/Appender）的包路径|```com.yourpackage.log4j2.plugins```|

示例：

```xml
<Configuration status="WARN" monitorInterval="30" xmlns="http://logging.apache.org/log4j/2.0/config">
    <!-- 子组件配置 -->
</Configuration>
```

## 二、全局属性（Properties）

定义可复用的全局变量（如日志格式、文件大小、保留天数），通过 ```${属性名}``` 引用，减少重复配置。

|配置项|类型|说明|示例|
|--|--|--|--|
|自定义属性名|String|全局复用变量，可在任意组件中通过 ```${属性名}``` 引用|```LOG_PATTERN/MAX_FILE_SIZE/LOG_PATH```|

示例：

```xml
<Configuration>
    <Properties>
        <!-- 日志格式（复用） -->
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n</Property>
        <!-- 单个日志文件最大大小 -->
        <Property name="MAX_FILE_SIZE">100MB</Property>
        <!-- 日志保留的最大个数 -->
        <Property name="MAX_HISTORY">5</Property>
        <!-- 日志目录（默认值 + 系统参数 fallback） -->
        <Property name="LOG_PATH">${log-path:logs/}</Property>
    </Properties>
</Configuration>
```

## 三、核心组件配置项

1. Layout（日志格式）

控制日志输出的内容结构，常用 PatternLayout（自定义模板），核心配置项：

|配置项|类型|说明|示例|
|--|--|--|--|
|pattern|String|日志格式模板，支持占位符（如时间、线程名、级别）|```%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %msg%n```|
|charset|String|日志编码（默认 UTF-8）|```UTF-8/GBK```|
|alwaysWriteExceptions|boolean|是否强制输出异常堆栈（默认 true）|```true/false```|

示例：

```xml
<PatternLayout pattern="${LOG_PATTERN}" charset="UTF-8" alwaysWriteExceptions="true" />
```

2. Appender（输出目标）

定义日志输出到哪里，核心配置项按 Appender 类型分类：

（1）ConsoleAppender（控制台输出）

|配置项|类型|说明|可选值/示例|
|--|--|--|--|
|name|String|Appender 唯一标识（供 Logger 引用）|```Console/DevConsole```|
|target|String|输出目标（标准输出/标准错误）|```SYSTEM_OUT（黑字）/SYSTEM_ERR（红字）```|
|Layout|子组件|日志格式（必选）|```<PatternLayout ... />```|
|ThresholdFilter|子组件|级别过滤（仅输出指定级别及以上日志）|```<ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY" />```|

示例：

```xml
<Console name="Console" target="SYSTEM_OUT">
    <PatternLayout pattern="${LOG_PATTERN}" />
    <ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY" />
</Console>
```

（2）RollingFileAppender（滚动文件输出）

生产环境常用，支持按大小 / 时间分割日志，核心配置项：

|配置项|类型|说明|示例|
|--|--|--|--|
|name|String|Appender 唯一标识|```RollingFile/ProdFile```|
|fileName|String|当前活跃日志文件路径（支持系统参数/环境变量）|```${LOG_PATH}/app.log```|
|filePattern|String|分割后日志文件名模板（%d 时间、%i 序号）|```${LOG_PATH}/app-%d{yyyy-MM-dd}-%i.log```|
|Layout|子组件|日志格式（必选）|```<PatternLayout ... />```|
|Policies|子组件|滚动触发策略（时间/大小）|包含 ```<TimeBasedTriggeringPolicy>/<SizeBasedTriggeringPolicy>```|
|DefaultRolloverStrategy|子组件|滚动后文件保留策略|```<DefaultRolloverStrategy max="30" />```|
|append|boolean|是否追加到文件（默认 true，避免覆盖历史日志）|```true/false```|
|bufferedIO|boolean|是否缓冲输出（默认 true，提升性能|```true/false```|
|bufferSize|String|缓冲区大小（默认 8192 字节）|```8KB/16KB```|

（2.1）滚动策略子配置项：

（2.1.1）TimeBasedTriggeringPolicy（时间策略）：

|配置项|说明|示例|
|--|--|--|
|interval|分割间隔（单位：天，默认 1）|1（每天分割）|
|modulate|是否对齐自然时间（如 00:00 分割，默认 false）|true|

（2.1.2）SizeBasedTriggeringPolicy（大小策略）：

|配置项|说明|示例|
|--|--|--|
|size|单个文件最大大小（支持 ```KB/MB/GB```）|100MB|

示例：

```xml
<RollingFile name="RollingFile"
             fileName="${LOG_PATH}/app.log"
             filePattern="${LOG_PATH}/app-%d{yyyy-MM-dd}-%i.log"
             append="true"
             bufferedIO="true"
             bufferSize="8KB">
    <PatternLayout pattern="${LOG_PATTERN}" />
    <Policies>
        <TimeBasedTriggeringPolicy interval="1" modulate="true" />
        <SizeBasedTriggeringPolicy size="${MAX_FILE_SIZE}" />
    </Policies>
    <DefaultRolloverStrategy max="${MAX_HISTORY}" />
</RollingFile>
```

（2.2）保留策略(DefaultRolloverStrategy)的子配置项：

当触发日志滚动（按 ```时间/大小``` 分割）时，DefaultRolloverStrategy 主要做 3 件事：

- 重命名当前活跃日志文件（如 app.log → app-2025-12-02-1.log）；
- 按规则保留指定 ```数量/时间``` 的历史日志，删除 ```过期/超量``` 日志；
- 可选：将历史日志压缩归档（如 ```.gz/.zip```），节省磁盘空间。

|配置项|类型|说明|可选值/默认值|实战场景|
|--|--|--|--|--|
|max|int|保留的历史日志文件最大数量（默认 7），超过则删除最早的文件|正整数（如 ```30/90```）|生产环境建议 30-90（保留 1-3 个月）|
|min|int|保留的历史日志文件最小数量（默认 1），即使过期也不删除|正整数（如 5）|避免误删关键日志|
|fileIndex|String|日志文件序号排序规则（影响文件命名和删除逻辑）|max（默认，序号递增）/min（序号递减）|需配合 filePattern 中的 ```%i``` 使用|
|compressionLevel|```int/String```|日志压缩级别（仅开启压缩时生效）|0-9（数字，0 = 无压缩，9 = 最高压缩）```/DEFAULT/BEST_SPEED/BEST_COMPRESSION```|生产环境推荐 6（平衡速度和压缩比）|
|filePermissions|String|生成的历史日志文件权限（Linux 系统专属）|如 ```rw-r--r--```（八进制 644）|控制日志文件访问权限|
|dirPermissions|String|日志目录权限（Linux 系统专属）|如 ```rwxr-xr-x```（八进制 755）|确保应用能读写日志目录|
|delete|子组件|自定义删除策略（按文件大小、最后修改时间等删除旧日志）|```<Delete>``` 子标签配置|复杂日志清理规则（如保留 10GB 内日志）|
|renameEmptyFiles|boolean|是否重命名空的滚动文件（默认 false，空文件不生成）|```true/false```|避免生成大量空日志文件|
|stopCustomActionsOnError|boolean|自定义 ```删除/压缩``` 动作失败时是否停止滚动（默认 true）|```true/false```|建议 false（避免影响正常日志）|

关键补充：fileIndex 排序规则说明

- ```fileIndex="max"```（默认）：序号从 1 递增，最新的历史文件序号最大（如 app-2025-12-02-1.log → app-2025-12-02-2.log），删除时先删序号最小的文件；
- ```fileIndex="min"```：序号从指定最大值递减，最新的历史文件序号最小（如 app-2025-12-02-30.log → app-2025-12-02-29.log），删除时先删序号最大的文件。

注意：fileIndex 需与 RollingFileAppender 的 filePattern 配合，filePattern 必须包含 ```%i```（序号占位符），否则排序无效。

示例：按天分割日志，单个文件超过 100MB 额外分割，保留 30 个历史文件，自动压缩为 .gz 格式：

```xml
<RollingFile name="ProdRollingFile"
             fileName="${LOG_PATH}/app.log"  <!-- 当前活跃日志 -->
             filePattern="${LOG_PATH}/app-%d{yyyy-MM-dd}-%i.log.gz">  <!-- 滚动后文件名（含压缩后缀） -->
    <PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n" />
    <!-- 滚动触发策略：每天分割 + 100MB 分割 -->
    <Policies>
        <TimeBasedTriggeringPolicy interval="1" modulate="true" />
        <SizeBasedTriggeringPolicy size="100MB" />
    </Policies>
    <!-- 滚动后策略：保留 30 个文件，压缩级别 6 -->
    <DefaultRolloverStrategy max="30" compressionLevel="6" fileIndex="max">
        <!-- Linux 系统可选：设置文件权限 -->
        <filePermissions>rw-r--r--</filePermissions>
        <dirPermissions>rwxr-xr-x</dirPermissions>
    </DefaultRolloverStrategy>
</RollingFile>
```

示例：滚动文件序号从 30 递减，最新的历史文件序号最小（避免序号无限增大）：

```xml
<RollingFile name="ProdRollingFile"
             fileName="${LOG_PATH}/app.log"
             filePattern="${LOG_PATH}/app-%d{yyyy-MM-dd}-%i.log">
    <PatternLayout pattern="${LOG_PATTERN}" />
    <Policies>
        <TimeBasedTriggeringPolicy interval="1" />
    </Policies>
    <!-- 序号从 30 递减，保留 30 个文件 -->
    <DefaultRolloverStrategy max="30" min="5" fileIndex="min" />
</RollingFile>
```

（2.2.1）自定义删除策略（```<Delete>``` 子组件）

```<Delete>``` 子组件核心配置项：

|标签|说明|示例|
|--|--|--|
|basePath|搜索日志文件的根目录（必填）|	```${LOG_PATH}```（日志目录）|
|maxDepth|目录搜索深度（0 = 仅 basePath，1=basePath 下一级目录）|1（默认，仅当前日志目录）|
|IfLastModified|按文件最后修改时间过滤（age 支持 ```d/h/m/s```，如 ```30d=30天```）|```<IfLastModified age="30d" />```|
|IfAccumulatedFileSize|按累计文件大小过滤（支持 ```KB/MB/GB```）|```<IfAccumulatedFileSize exceeds="10GB" />```|
|IfFileName|按文件名 ```正则/glob``` 过滤（glob 支持通配符 ```*/?```）|<```IfFileName glob="*.log.gz" />```|
|IfFileSize|按单个文件大小过滤|```<IfFileSize exceeds="500MB" />```|

```IfLastModified``` 的 ```age``` 单位必须跟 ```filePattern``` 的最小单位保持一致。比如：

- 如果配置的 ```filePattern="${LOG_PATH}/run.%d{yyyyMMdd}.%i.log.gz"```，那么 ```IfLastModified``` 的 ```age``` 单位必须是 ```d```，如 ```<ifLastModified age="2d" />```
- 如果配置的 ```filePattern="${LOG_PATH}/run.%d{yyyyMMddhh}.%i.log.gz"```，那么 ```IfLastModified``` 的 ```age``` 单位必须是 ```h```，如 ```<ifLastModified age="2h" />```

逻辑运算符，支持 ```<And>/<Or>/<Not>``` 组合多条件：

```xml
<!-- 条件：（超过 30 天）且（单个文件大于 100MB） -->
<And>
    <IfLastModified age="30d" />
    <IfFileSize exceeds="100MB" />
</And>
```

```DefaultRolloverStrategy``` 的 ```<Delete>``` 子组件支持 按文件大小、最后修改时间、文件名正则等复杂规则删除旧日志。

示例：保留 30 天内日志 + 总大小不超过 10GB：

```xml
<DefaultRolloverStrategy max="999" compressionLevel="6">  <!-- max 设为较大值，由 Delete 策略控制删除 -->
    <Delete basePath="${LOG_PATH}" maxDepth="1">  <!-- basePath：日志目录，maxDepth：搜索深度（1=仅当前目录） -->
        <!-- 条件 1：删除 30 天前的日志文件 -->
        <IfLastModified age="30d" />
        <!-- 条件 2：删除后总大小仍超过 10GB 时，继续删除最早文件 -->
        <IfAccumulatedFileSize exceeds="10GB" />
        <!-- 仅匹配 .log.gz 后缀的日志文件（避免删除其他文件） -->
        <IfFileName glob="*.log.gz" />
    </Delete>
</DefaultRolloverStrategy>
```

示例：删除名称包含 error 的旧日志（保留 7 天），其他日志保留 30 天：

```xml
<DefaultRolloverStrategy max="999">
    <Delete basePath="${LOG_PATH}" maxDepth="1">
        <!-- 条件 1：文件名包含 error -->
        <IfFileName glob="*error*.log.gz" />
        <!-- 条件 2：超过 7 天 -->
        <IfLastModified age="7d" />
    </Delete>
    <!-- 其他日志按 max=999 + 30 天保留（需配合 TimeBasedTriggeringPolicy） -->
</DefaultRolloverStrategy>
```

（3）AsyncAppender（异步输出）

包装其他 Appender，异步输出日志（解耦业务线程，提升性能）：

|配置项|类型|说明|示例|
|--|--|--|--|
|name|String|Appender 唯一标识|```AsyncRollingFile```|
|AppenderRef|子组件|引用需要异步的 Appender（必选）|```<AppenderRef ref="RollingFile" />```|
|bufferSize|int|异步队列大小（默认 1024，高并发场景可增大）|```2048/4096```|
|blocking|boolean|队列满时是否阻塞业务线程（默认 true，避免日志丢失）|```true/false```|
|includeLocation|boolean|是否记录日志位置（```行号/类名```，默认 false，提升性能）|```true/false```|

示例：

```xml
<Async name="AsyncRollingFile" bufferSize="4096" blocking="true">
    <AppenderRef ref="RollingFile" />
</Async>
```

3. Logger（日志器）

控制日志输出规则（级别、输出目标），分为 ```Root Logger``` 和 ```自定义 Logger```：

（1）Root Logger（全局默认）

|配置项|类型|说明|示例|
|--|--|--|--|
|level|String|全局日志级别（默认 ERROR）|```DEBUG/INFO/WARN/ERROR```|
|AppenderRef|子组件|绑定的 Appender（可多个）|```<AppenderRef ref="Console" />```|

示例：

```xml
<Root level="INFO">
    <AppenderRef ref="Console" />
    <AppenderRef ref="AsyncRollingFile" />
</Root>
```

（2）自定义 Logger（按包 / 类精准控制）

|配置项|类型|说明|示例|
|--|--|--|--|
|name|String|包名或类全路径（唯一标识）|```com.yourpackage.biz/org.apache.ibatis```|
|level|String|该 Logger 的日志级别（优先级高于 Root）|```DEBUG/WARN```|
|additivity|boolean|是否向上传播日志到 Root（默认 true，可能导致重复输出）|false（禁止传播，推荐）|
|AppenderRef|子组件|绑定的 Appender（仅当前 Logger 生效）|```<AppenderRef ref="Console" />```|

示例：

```xml
<!-- 业务包日志：DEBUG 级别，仅输出到控制台 -->
<Logger name="com.yourpackage.biz" level="DEBUG" additivity="false">
    <AppenderRef ref="Console" />
</Logger>
<!-- 第三方组件日志：屏蔽调试日志，仅输出 WARN 及以上 -->
<Logger name="org.springframework" level="WARN" additivity="false">
    <AppenderRef ref="AsyncRollingFile" />
</Logger>
```

4. Filter（日志过滤）

细粒度控制日志输出，支持「级别过滤、正则过滤、匹配过滤」，常用 ThresholdFilter（级别过滤）：

|配置项|类型|说明|可选值/示例|
|--|--|--|--|
|level|String|过滤级别|```DEBUG/INFO/WARN```|
|onMatch|String|匹配级别时的行为|```ACCEPT（输出）/DENY（拒绝）/NEUTRAL（中立）```|
|onMismatch|String|不匹配级别时的行为|```ACCEPT/DENY/NEUTRAL```|

示例：

```xml
<!-- 仅输出 DEBUG 及以上级别日志 -->
<ThresholdFilter level="DEBUG" onMatch="ACCEPT" onMismatch="DENY" />
```

## 四、多环境配置项（Spring Boot 专属）

通过 SpringProfile 标签按 spring.profiles.active 动态加载配置，核心配置项：

|配置项|类型|说明|示例|
|--|--|--|--|
|name|String|环境名称（与 spring.profiles.active 对应，支持多个环境用逗号分隔）|```dev/test,prod/!dev（排除 dev）```|

示例：

```xml
<!-- 开发环境配置 -->
<SpringProfile name="dev">
    <Console name="DevConsole" target="SYSTEM_OUT">
        <PatternLayout pattern="${DEV_PATTERN}" />
    </Console>
</SpringProfile>
<!-- 生产环境配置 -->
<SpringProfile name="prod">
    <RollingFile name="ProdFile" ...>
        <!-- 生产环境配置 -->
    </RollingFile>
</SpringProfile>
```

## 五、性能优化配置项

|配置项|所属组件|说明|推荐值|
|--|--|--|--|
|bufferedIO|RollingFileAppender|启用缓冲输出（减少磁盘 IO）|true|
|bufferSize|RollingFileAppender|缓冲区大小（增大提升性能）|```8KB/16KB```|
|includeLocation|AsyncAppender|禁用日志位置记录（```行号/类名```，减少开销）|false|
|blocking|AsyncAppender|队列满时阻塞（避免日志丢失）|true|
|disableLazyInit|系统参数|禁用 Log4j2 懒加载（避免提前初始化）|```-Dlog4j2.disableLazyInit=true```|
|disableDefaultConfiguration|系统参数|禁用默认配置（避免冲突）|```-Dlog4j2.disableDefaultConfiguration=true```|

## 六、特殊功能配置项

1. 配置热更新

通过 monitorInterval（根节点配置项）实现，无需重启应用：

```xml
<Configuration monitorInterval="30"> <!-- 每 30 秒检查配置文件变更 -->
</Configuration>
```

2. 读取外部参数（系统参数/环境变量）

通过 ```${key}``` 读取系统参数，```${env:key}``` 读取环境变量，支持默认值：

|语法格式|说明|示例|
|--|--|--|
|```${key}```|读取系统参数（启动时通过 ```-Dkey=value``` 指定）|```${log-path:logs/}（默认 logs/）```|
|```${env:key}```|读取环境变量（容器化部署常用）|```${env:LOG_PATH:/data/logs}```|

示例：

```xml
<RollingFile fileName="${env:LOG_PATH:logs/}/app.log" ...>
</RollingFile>
```

3. 日志脱敏（PatternLayout 扩展）

通过 %replace 占位符实现敏感信息脱敏（如手机号、身份证）：

```xml
<PatternLayout pattern="%d{yyyy-MM-dd HH:mm:ss} [%t] %-5level %logger{36} - %replace(%msg){'(\d{11})','*******'}`%n" />
```

效果：日志中的 11 位手机号替换为 ```*******```。


## 七、常用配置项组合示例（生产环境）

```xml
<Configuration status="WARN" monitorInterval="30">
    <Properties>
        <Property name="LOG_PATTERN">%d{yyyy-MM-dd HH:mm:ss.SSS} [%t] %-5level %logger{36} - %msg%n</Property>
        <Property name="LOG_PATH">${env:LOG_PATH:/data/logs}</Property>
        <Property name="MAX_FILE_SIZE">100MB</Property>
        <Property name="MAX_HISTORY">30</Property>
    </Properties>
    <Appenders>
        <!-- 滚动文件 Appender -->
        <RollingFile name="ProdFile"
                     fileName="${LOG_PATH}/app.log"
                     filePattern="${LOG_PATH}/app-%d{yyyy-MM-dd}-%i.log"
                     append="true"
                     bufferedIO="true"
                     bufferSize="8KB">
            <PatternLayout pattern="${LOG_PATTERN}" />
            <Policies>
                <TimeBasedTriggeringPolicy interval="1" modulate="true" />
                <SizeBasedTriggeringPolicy size="${MAX_FILE_SIZE}" />
            </Policies>
            <DefaultRolloverStrategy max="${MAX_HISTORY}" />
        </RollingFile>
        <!-- 异步包装 -->
        <Async name="AsyncProdFile" bufferSize="4096" blocking="true" includeLocation="false">
            <AppenderRef ref="ProdFile" />
        </Async>
    </Appenders>
    <Loggers>
        <!-- 第三方组件日志过滤 -->
        <Logger name="org.springframework" level="WARN" additivity="false">
            <AppenderRef ref="AsyncProdFile" />
        </Logger>
        <Logger name="com.alibaba" level="WARN" additivity="false">
            <AppenderRef ref="AsyncProdFile" />
        </Logger>
        <!-- 根日志器 -->
        <Root level="INFO">
            <AppenderRef ref="AsyncProdFile" />
        </Root>
    </Loggers>
</Configuration>
```

## 八、配置项优先级说明

- 系统参数（-Dlog4j2.configurationFile）> 类路径配置文件（log4j2-spring.xml > log4j2.xml）；

- 自定义 Logger 级别 > Root Logger 级别；

- 命令行参数 > 环境变量 > 配置文件 > 全局属性默认值。

## 总结

Log4j2 配置项的核心是「围绕 Appender（输出）、Logger（过滤）、Layout（格式）」三大组件，结合多环境、性能优化需求灵活组合：

- 开发环境：ConsoleAppender + DEBUG 级别 + 简洁格式；
- 生产环境：RollingFileAppender + AsyncAppender + INFO 级别 + 滚动策略；
- 关键配置项：monitorInterval（热更新）、bufferSize（性能）、additivity（避免重复输出）、SpringProfile（多环境）。
