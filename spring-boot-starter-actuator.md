# 开启Actuator
spring-boot-actuator模块提供Spring Boot所有的production-ready特性，启用该特性的最简单方式是添加**spring-boot-starter-actuator**依赖。 

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
</dependencies>
```

# 端点(Endpoints)
执行器端点（endpoints）可用于监控应用及与应用进行交互，Spring Boot包含很多内置的端点，你也可以添加自己的。例如，health端点提供了应用的基本健康信息。 
每个端点都可以启用或禁用。这控制着端点是否被创建，并且它的bean是否存在于应用程序上下文中。要远程访问端点，还必须通过JMX或HTTP进行暴露,大部分应用选择HTTP，端点的ID映射到一个带/actuator前缀的URL。例如，health端点默认映射到/actuator/health。

```
注意: 
Spring Boot 2.0的端点基础路径由"/"调整到"/actuator"下,如：/info调整为/actuator/info 
可以通过以下配置改为和旧版本一致:

management.endpoints.web.base-path=/
```

下面的端点都是可用的：

|ID|描述|默认启用|
|--|--|--|
|auditevents|显示当前应用程序的审计事件信息|Yes|
|beans|显示一个应用中所有Spring Beans的完整列表|Yes|
|conditions|显示配置类和自动配置类(configuration and auto-configuration classes)的状态及它们被应用或未被应用的原因|Yes|
|configprops|显示一个所有@ConfigurationProperties的集合列表|Yes|
|env|显示来自Spring的 ConfigurableEnvironment的属性|Yes|
|flyway|显示数据库迁移路径，如果有的话|Yes|
|health|显示应用的健康信息（当使用一个未认证连接访问时显示一个简单的’status’，使用认证连接访问则显示全部信息详情）|Yes|
|info|显示任意的应用信息|Yes|
|liquibase|展示任何Liquibase数据库迁移路径，如果有的话|Yes|
|metrics|展示当前应用的metrics信息|Yes|
|mappings|显示一个所有@RequestMapping路径的集合列表|Yes|
|scheduledtasks|显示应用程序中的计划任务|Yes|
|sessions|允许从Spring会话支持的会话存储中检索和删除(retrieval and deletion)用户会话。使用Spring Session对反应性Web应用程序的支持时不可用。|Yes|
|shutdown|允许应用以优雅的方式关闭（默认情况下不启用）|No|
|threaddump|执行一个线程dump|Yes|
```
注意 
Spring Boot 2.0的端点和之前的版本有较大不同,使用时需注意 
另外,端点的监控机制也有很大不同,启用了不代表可以直接访问,还需要将其暴露出来,传统的management.security管理已被标记为不推荐,现在一般使用单独启用并暴露
```

# 启用端点
默认情况下，除shutdown以外的所有端点均已启用。要配置单个端点的启用，请使用management.endpoint.<id>.enabled属性。以下示例启用shutdown端点：

```
management.endpoint.shutdown.enabled=true
```

另外可以通过management.endpoints.enabled-by-default来修改全局端口默认配置,以下示例启用info端点并禁用所有其他端点：

```
management.endpoints.enabled-by-default=false
management.endpoint.info.enabled=true
```

# 暴露端点
要更改公开哪些端点，请使用以下技术特定的include和exclude属性：

|Property|Default|
|--|--|
|management.endpoints.jmx.exposure.exclude|*|
|management.endpoints.jmx.exposure.include|*|
|management.endpoints.web.exposure.exclude|*|
|management.endpoints.web.exposure.include|info, health|

include属性列出了公开的端点的ID,exclude属性列出了不应该公开的端点的ID

exclude属性优先于include属性，包含和排除属性都可以使用端点ID列表进行配置。

```
注意，这里的优先级是指同一端点ID,同时出现在include属性表和exclude属性表里,exclude属性优先于include属性,即此端点没有暴露
```

例如，要停止通过JMX公开所有端点并仅公开health和info端点，请使用以下属性：

```
management.endpoints.jmx.exposure.include=health,info
```

*可以用来选择所有端点。例如，要通过HTTP公开除env和beans端点之外的所有内容，请使用以下属性：

```
management.endpoints.web.exposure.include=*
management.endpoints.web.exposure.exclude=env,beans
```

注意 
*在YAML中有特殊的含义，所以如果你想包含（或排除）所有的端点，一定要加引号，如下例所示：

```
management:
  endpoints:
    web:
      exposure:
        include: '*'
```

# 跨域支持
跨源资源共享（Cross-origin resource sharing,CORS）是W3C规范，允许您以灵活的方式指定授权哪种跨域请求。如果您使用Spring MVC或Spring WebFlux，则可以配置Actuator的Web端点来支持这些场景。

默认情况下，CORS支持处于禁用状态，只有在设置了management.endpoints.web.cors.allowed-origins属性后才能启用。以下配置允许来自example.com域的GET和POST调用：

```
management.endpoints.web.cors.allowed-origins=http://example.com
management.endpoints.web.cors.allowed-methods=GET,POST
```

# 健康信息
您可以使用健康信息来检查正在运行的应用程序的状态。当生产系统停机时，它经常被监控软件用来提醒某人。health端点公开的信息取决于management.endpoint.health.show-details属性，该属性可以使用以下值之一进行配置：

|Name|Description|
|--|--|
|never|细节永远不会显示。|
|when-authorized|详细信息仅向授权用户显示。授权角色可以使用management.endpoint.health.roles进行配置。|
|always|详细信息显示给所有用户。|

```
默认值为never。 

配置健康信息可见：

management.endpoint.health.show-details=always
```

# 示例
## 新建一个spring boot项目

## 添加依赖
```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
```

## 配置文件
```
spring:
  redis:
    database: 10
    host: 127.0.0.1
    jedis:
      pool:
        max-active: 20

management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
```

## 启动spring boot项目

## 访问http://localhost:8080/actuator/health
```json
{
	"status": "UP",
	"details": {
		"diskSpace": {
			"status": "UP",
			"details": {
				"total": 310318723072,
				"free": 13662003200,
				"threshold": 10485760
			}
		},
		"refreshScope": {
			"status": "UP"
		},
		"redis": {
			"status": "UP",
			"details": {
				"version": "3.2.100"
			}
		}
	}
}
```

## /actuator/info 显示版本信息
**actuator 默认读取 META-INF/build-info.properties**

所以需要先生成 build-info.properties:
1. 运行 Plugins -> spring-boot -> spring-boot:build-info

![build-info](./images/build-info.png ''build-info'')

2. 生成的 build-info.properties

![build-info](./images/build-info-2.png ''build-info'')

3. 访问 http://localhost:8080/actuator/info

```json
{
	"build": {
		"version": "1.0.0",
		"artifact": "framework",
		"name": "framework",
		"group": "com.zhy",
		"time": {
			"nano": 111000000,
			"epochSecond": 1604629406
		}
	}
}
```
