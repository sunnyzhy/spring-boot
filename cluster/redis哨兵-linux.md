# redis 哨兵

## 前言

- ***本示例为一主一从一哨兵***
- ***哨兵的个数必须是单数***

|物理机IP|角色|
|--|--|
|192.168.5.163|master|
|192.168.5.164|slave|
|192.168.5.164|sentinel|

## 搭建主从

### 修改主服务器配置

```bash
# vim /etc/redis/6379.conf
bind 0.0.0.0
protected-mode no
masterauth "password"
requirepass "password"
```

### 修改从服务器配置

```bash
# vim /etc/redis/6379.conf
bind 0.0.0.0
protected-mode no
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

1. 查看主服务器的状态
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:1
    slave0:ip=192.168.5.164,port=6379,state=send_bulk,offset=0,lag=0
    ```
2. 查看从服务器的状态
    ```bash
    127.0.0.1:6379> info replication
    role:slave
    master_host:192.168.5.163
    master_port:6379
    ```

## 搭建哨兵

### 修改从服务器配置

```bash
# vim /etc/redis/sentinel.conf
sentinel monitor mymaster 192.168.5.163 6379 1
sentinel auth-pass mymaster password
```

- ```sentinel monitor``` 配置项语法：```sentinel monitor <master-name> <master-ip> <master-port> <quorum>```
   - ```quorum```：在哨兵配置中，quorum参数定义了在故障转移时至少需要多少个 Sentinel 实例同意执行故障转移操作。如果 quorum 设置为 3，则需要至少有 3 个哨兵实例同意执行故障转移操作。为了保证 quorum 参数的正确性，建议将 quorum 参数设置为奇数值。
- ```sentinel auth-pass``` 配置项语法：```sentinel auth-pass <master-name> <master-password>```

### 启动从服务器的哨兵

启动哨兵的命令相同：

```bash
nohup redis-server /etc/redis/sentinel.conf --sentinel &
```

查看主服务器 192.168.5.163 服务：

```bash
# ps -ef | grep redis
root      21089       1  0 15:34 ?        00:00:02 /usr/local/bin/redis-server 0.0.0.0:6379
```

查看从服务器 192.168.5.164 服务：

```bash
# ps -ef | grep redis
root      145033       1  0 15:34 ?        00:00:02 /usr/local/bin/redis-server 0.0.0.0:6379
root      145038  143886  0 15:35 pts/1    00:00:00 redis-server *:26379 [sentinel]
```

## 验证哨兵模式

1. 在 ```192.168.5.163``` 写入数据
    ```bash
    127.0.0.1:6379> set a1:b1:c1 10
    OK
    ```
2. 在 ```192.168.5.164``` 写入数据
    ```bash
    127.0.0.1:6379> get a1:b1:c1
    "10"
    
    127.0.0.1:6379> set a1:b1:c1 100
    (error) READONLY You can't write against a read only replica.
    ```
3. 停止 ```192.168.5.163``` 服务
    ```bash
    # kill -9 21089
    
    # rm -rf /var/run/redis_6379.pid
    ```
4. 查看 ```192.168.5.164``` 的状态，此时从服务器变成了主服务器
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:0
    
    127.0.0.1:6379> set a1:b1:c1 100
    OK
    ```
5. 查看 ```192.168.5.164``` 的配置文件，```replicaof``` 配置项已被自动移除
    ```bash
    # vim /etc/redis/6379.conf
    ```
6. 查看 ```192.168.5.164``` 的哨兵配置文件
    ```bash
    # vim /etc/redis/sentinel.conf
    sentinel monitor mymaster 192.168.5.164 6379 1
    sentinel known-replica mymaster 192.168.5.163 6379
    ```
7. 启动  ```192.168.5.163``` 服务
    ```bash
    # service redis_6379 start
    ```
8. 查看  ```192.168.5.163``` 的配置文件，```replicaof``` 配置项已被自动添加
    ```bash
    # vim /etc/redis/6379.conf
    replicaof 192.168.5.164 6379
    ```
9. 查看  ```192.168.5.163``` 的状态，此时主服务器变成了从服务器
    ```bash
    127.0.0.1:6379> info replication
    role:slave
    master_host:192.168.5.164
    master_port:6379

    127.0.0.1:6379> get a1:b1:c1
    "100"

    127.0.0.1:6379> set a1:b1:c1 x
    (error) READONLY You can't write against a read only replica.
    ```
10. 查看 ```192.168.5.164``` 的状态
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:1
    slave0:ip=192.168.5.163,port=6379,state=send_bulk,offset=0,lag=0
    ```
11. 停止 ```192.168.5.164``` 服务
    ```bash
    # kill -9 21089
    
    # rm -rf /var/run/redis_6379.pid
    ```
12. 查看 ```192.168.5.163``` 的状态，此时从服务器变成了主服务器
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:0
    
    127.0.0.1:6379> set a1:b1:c1 x
    OK
    ```
13. 查看 ```192.168.5.164``` 的哨兵配置文件
    ```bash
    # vim /etc/redis/sentinel.conf
    sentinel monitor mymaster 192.168.5.163 6379 1
    sentinel known-replica mymaster 192.168.5.164 6379
    ```
14. 启动  ```192.168.5.164``` 服务
    ```bash
    # service redis_6379 start
    ```
15. 查看  ```192.168.5.164``` 的配置文件，```replicaof``` 配置项已被自动添加
    ```bash
    # vim /etc/redis/6379.conf
    replicaof 192.168.5.163 6379
    ```
16. 查看 ```192.168.5.164``` 的状态
    ```bash
    127.0.0.1:6379> info replication
    role:slave
    master_host:192.168.5.163
    master_port:6379

    127.0.0.1:6379> get a1:b1
    "x"

    127.0.0.1:6379> set a1:b1 10
    (error) READONLY You can't write against a read only replica.
    ```
17. 查看 ```192.168.5.163``` 的状态
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:1
    slave0:ip=192.168.5.164,port=6379,state=send_bulk,offset=0,lag=0
    ```
