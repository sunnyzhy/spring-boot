# eureka 集群

## 前言

- 准备两台物理机
    |物理机IP|物理机HostName|
    |--|--|
    |192.168.5.10|node-1|
    |192.168.5.11|node-2|

## 配置 hosts

```192.168.5.10```:

```bash
# vim /etc/hosts
192.168.5.10 node-1
192.168.5.11 node-2
```

```192.168.5.11```:

```bash
# vim /etc/hosts
192.168.5.10 node-1
192.168.5.11 node-2
```

## eureka 服务端配置

```192.168.5.10```:

```yml
eureka:
    client:
        fetchRegistry: false
        registerWithEureka: false
        serviceUrl:
            defaultZone: http://eureka:eureka@192.168.5.10:8080/eureka/,http://eureka:eureka@192.168.5.11:8080/eureka/
    instance:
        hostname: node-1
        perferIpAddress: true
    server:
        enable-self-preservation: false
        eviction-interval-timer-in-ms: 5000
server:
    port: 8080
spring:
    application:
        name: eureka-server
    security:
        user:
            name: eureka
            password: eureka
```

```192.168.5.11```:

```yml
eureka:
    client:
        fetchRegistry: false
        registerWithEureka: false
        serviceUrl:
            defaultZone: http://eureka:eureka@192.168.5.10:8080/eureka/,http://eureka:eureka@192.168.5.11:8080/eureka/
    instance:
        hostname: node-2
        perferIpAddress: true
    server:
        enable-self-preservation: false
        eviction-interval-timer-in-ms: 5000
server:
    port: 8080
spring:
    application:
        name: eureka-server
    security:
        user:
            name: eureka
            password: eureka
```

## eureka 客户端配置

```192.168.5.10```:

```yml
eureka:
    client:
        fetch-registry: true
        serviceUrl:
            defaultZone: http://eureka:eureka@192.168.5.10:8080/eureka/,http://eureka:eureka@192.168.5.11:8080/eureka/
    instance:
        prefer-ip-address: true

server:
    port: 8081

spring:
    application:
        name: eureka-client-1
```

```192.168.5.11```:

```yml
eureka:
    client:
        fetch-registry: true
        serviceUrl:
            defaultZone: http://eureka:eureka@192.168.5.10:8080/eureka/,http://eureka:eureka@192.168.5.11:8080/eureka/
    instance:
        prefer-ip-address: true

server:
    port: 8081

spring:
    application:
        name: eureka-client-1
```

## 查看集群

### DS Replicas
- 192.168.5.10
- 192.168.5.11

### Instances currently registered with Eureka

|Application|AMIs|Availability|Zones|Status|
|--|--|--|--|--|
|EUREKA-CLIENT-1|n/a(2)|(2)|UP(2) - node-1:eureka-client-1:8081 , node-2:eureka-client-1:8081|

### General Info
|Name|Value|
|--|--|
|registered-replicas|http://192.168.5.10:8080/eureka/, http://192.168.5.11:8080/eureka/|
|unavailable-replicas|http://192.168.5.10:8080/eureka/, http://192.168.5.11:8080/eureka/|
|available-replicas||
