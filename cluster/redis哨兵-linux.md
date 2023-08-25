# redis 哨兵

## 前言

|物理机IP|角色|
|--|--|
|192.168.5.163|master|
|192.168.5.164|slave|

## 搭建主从

### 修改主服务器配置

```bash
# vim /etc/redis/6379.conf
bind 0.0.0.0
requirepass "password"
```

### 修改从服务器配置

```bash
# vim /etc/redis/6379.conf
bind 0.0.0.0
masterauth "password"
requirepass "password"
replicaof 192.168.5.163 6379
```

- ```replicaof``` 配置项语法：```replicaof <master-ip> <master-port>```

### 启动主从服务器

启动主从服务器的命令相同：

```bash
service redis_6379 start
```

### 查看主从服务器的状态

主从服务器启动成功后，使用如下命令查看主从状态：

1. 查看主服务器状态
    ```bash
    127.0.0.1:6379> info replication
    # Replication
    role:master
    connected_slaves:1
    ```
2. 查看从服务器状态
    ```bash
    127.0.0.1:6379> info replication
    # Replication
    role:slave
    master_host:192.168.5.163
    master_port:6379
    ```

## 搭建哨兵

### 修改主服务器配置

```bash
# vim /etc/redis/sentinel.conf
sentinel monitor mymaster 192.168.5.163 6379 1
sentinel auth-pass mymaster password
```

- ```sentinel monitor``` 配置项语法：```sentinel monitor <master-name> <master-ip> <master-port> <quorum>```
   - ```quorum```：在哨兵配置中，quorum参数定义了在故障转移时至少需要多少个 Sentinel 实例同意执行故障转移操作。如果 quorum 设置为 3，则需要至少有 3 个哨兵实例同意执行故障转移操作。为了保证 quorum 参数的正确性，建议将 quorum 参数设置为奇数值。
- ```sentinel auth-pass``` 配置项语法：```sentinel auth-pass <master-name> <master-password>```

### 修改从服务器配置

```bash
# vim /etc/redis/sentinel.conf
sentinel monitor mymaster 192.168.5.163 6379 1
sentinel auth-pass mymaster password
```

- ```sentinel monitor``` 配置项语法：```sentinel monitor <master-name> <master-ip> <master-port> <quorum>```
- ```sentinel auth-pass``` 配置项语法：```sentinel auth-pass <master-name> <master-password>```

### 启动主从服务器的哨兵

启动主从服务器哨兵的命令相同：

```bash
# redis-server /etc/redis/sentinel.conf --sentinel &
```
