# redis 哨兵

## 前言

- 本示例为**一主一从一哨兵**
- 主节点**可读可写**，从节点**只读**
- 哨兵也是一台 redis 服务器，只是不提供数据相关的服务
- 哨兵的数量务必配置为**奇数**，因为哨兵数量为偶数的话，容易脑裂

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
nohup redis-server /etc/redis/sentinel.conf --sentinel > /root/sentinel.log 2>&1 &
```

查看日志：

```bash
tail -f /root/sentinel.log
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

    127.0.0.1:6379> get a1:b1:c1
    "x"

    127.0.0.1:6379> set a1:b1:c1 10
    (error) READONLY You can't write against a read only replica.
    ```
17. 查看 ```192.168.5.163``` 的状态
    ```bash
    127.0.0.1:6379> info replication
    role:master
    connected_slaves:1
    slave0:ip=192.168.5.164,port=6379,state=send_bulk,offset=0,lag=0
    ```

## 哨兵日志解析

格式中的 instance details 内容：

```
<instance-type> <name> <ip> <port> @ <master-name> <master-ip> <master-port>
```

- ```@``` 字符之后的内容用于指定主服务器

|参数|描述|
|--|--|
|```+reset-master <instance details>```|主服务器已被重置。|
|```+slave <instance details>```|一个新的从服务器已经被 Sentinel 识别并关联。|
|```+failover-state-reconf-slaves <instance details>```|故障转移状态切换到了 reconf-slaves 状态。|
|```+failover-detected <instance details>```|另一个 Sentinel 开始了一次故障转移操作，或者一个从服务器转换成了主服务器。|
|```+slave-reconf-sent <instance details>```|领头（leader）的 Sentinel 向实例发送了 SLAVEOF 命令，为实例设置新的主服务器。|
|```+slave-reconf-inprog <instance details>```|实例正在将自己设置为指定主服务器的从服务器，但相应的同步过程仍未完成。|
|```+slave-reconf-done <instance details>```|从服务器已经成功完成对新主服务器的同步。|
|```-dup-sentinel <instance details>```|对给定主服务器进行监视的一个或多个 Sentinel 已经因为重复出现而被移除 —— 当 Sentinel 实例重启的时候，就会出现这种情况。|
|```+sentinel <instance details>```|一个监视给定主服务器的新 Sentinel 已经被识别并添加。|
|```+sdown <instance details>```|给定的实例现在处于主观下线状态。|
|```-sdown <instance details>```|给定的实例已经不再处于主观下线状态。|
|```+odown <instance details>```|给定的实例现在处于客观下线状态。|
|```-odown <instance details>```|给定的实例已经不再处于客观下线状态。|
|```+new-epoch <instance details>```|当前的纪元（epoch）已经被更新。|
|```+try-failover <instance details>```|一个新的故障迁移操作正在执行中，等待被大多数 Sentinel 选中（waiting to be elected by the majority）。|
|```+elected-leader <instance details>```|赢得指定纪元的选举，可以进行故障迁移操作了。|
|```+failover-state-select-slave <instance details>```|故障转移操作现在处于 select-slave 状态 —— Sentinel 正在寻找可以升级为主服务器的从服务器。|
|```+no-good-slave <instance details>```|Sentinel 操作未能找到适合进行升级的从服务器。|Sentinel 会在一段时间之后再次尝试寻找合适的从服务器来进行升级，又或者直接放弃执行故障转移操作。|
|```+selected-slave <instance details>```|Sentinel 顺利找到适合进行升级的从服务器。|
|```+failover-state-send-slaveof-noone <instance details>```|Sentinel 正在将指定的从服务器升级为主服务器，等待升级功能完成。|
|```+failover-end-for-timeout <instance details>```|故障转移因为超时而中止，不过最终所有从服务器都会开始复制新的主服务器（slaves will eventually be configured to replicate with the new master anyway）。|
|```+failover-end <instance details>```|故障转移操作顺利完成。所有从服务器都开始复制新的主服务器了。|
|```+switch-master <master name> <oldip> <oldport> <newip> <newport>```|配置变更，主服务器的 IP 和地址已经改变。| 
|```+tilt```|进入 tilt 模式。|
|```-tilt```|退出 tilt 模式。|

示例：

```
# +sdown master mymaster 192.168.5.164 6379
# +odown master mymaster 192.168.5.164 6379 #quorum 1/1
# +new-epoch 2
# +try-failover master mymaster 192.168.5.164 6379
# +vote-for-leader 202acc3053b283fbfefefafefeb860373ff20cbb 2
# +elected-leader master mymaster 192.168.5.164 6379
# +failover-state-select-slave master mymaster 192.168.5.164 6379
# +selected-slave slave 192.168.5.163:6379 192.168.5.163 6379 @ mymaster 192.168.5.164 6379
* +failover-state-send-slaveof-noone slave 192.168.5.163:6379 192.168.5.163 6379 @ mymaster 192.168.5.164 6379
* +failover-state-wait-promotion slave 192.168.5.163:6379 192.168.5.163 6379 @ mymaster 192.168.5.164 6379
# +promoted-slave slave 192.168.5.163:6379 192.168.5.163 6379 @ mymaster 192.168.5.164 6379
# +failover-state-reconf-slaves master mymaster 192.168.5.164 6379
# +failover-end master mymaster 192.168.5.164 6379
# +switch-master mymaster 192.168.5.164 6379 192.168.5.163 6379
* +slave slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379
# +sdown slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379
# -sdown slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379
* +convert-to-slave slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379
```

- ```+sdown```：主观下线，有一个哨兵监测到主节点不可用
- ```+odown```：客观下线，其他哨兵去连接主节点以确定主节点是不是真的不可用了，当超过半数的哨兵认为主节点不可用，就会发起投票
- ```+switch-master mymaster 192.168.5.164 6379 192.168.5.163 6379```：切换主节点，哨兵发起投票的结果，推选端口为 6379 的 192.168.5.163 服务为主节点
- ```+slave slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379```：端口为 6379 的 192.168.5.164 从服务被哨兵识别并关联
- ```+sdown slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379```：端口为 6379 的 192.168.5.164 从服务处于主观下线状态
- ```-sdown slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379```：端口为 6379 的 192.168.5.164 从服务不再处于主观下线状态
- ```+convert-to-slave slave 192.168.5.164:6379 192.168.5.164 6379 @ mymaster 192.168.5.163 6379```：切换从节点(原主节点降为从节点)，端口为 6379 的 192.168.5.164 服务切换为从节点
