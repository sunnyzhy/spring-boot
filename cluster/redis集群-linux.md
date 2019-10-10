# 前言（重点、重点、重点）

- **本示例实现的是单机环境下的伪集群，redis.conf 配置：**

```
bind 127.0.0.1

port 6300

cluster-enabled yes

cluster-config-file nodes-6300.conf

cluster-node-timeout 15000

appendonly yes
```

- **生产环境下的集群，每个节点下的 redis.conf 配置都一样：**

```
bind 0.0.0.0

port 6379

cluster-enabled yes

cluster-config-file nodes-6379.conf

cluster-node-timeout 15000

appendonly yes
```

# 下载 linux 版本的 Redis

官网下载地址：http://redis.io

安装流程参考：[安装redis](https://github.com/sunnyzhy/spark/blob/master/%E5%AE%89%E8%A3%85redis.md "安装redis")

# 搭建 Redis 集群

1. 创建 Redis 集群目录

在 /usr/local/redis-cluster 目录下创建 6 个以端口命名的文件夹。

```
# cd /usr/local

# mkdir redis-cluster

# cd redis-cluster

# mkdir 6300

# mkdir 6301

# mkdir 6302

# mkdir 6303

# mkdir 6304

# mkdir 6305
```

2. 将 redis 安装目录中的 redis.conf 以及 redis-server ，分别拷贝到新建的这 6 个文件夹中

```
# cp /usr/local/redis/bin/redis-server /usr/local/redis-cluster/6300/redis-server

# cp /usr/local/redis/bin/redis.conf /usr/local/redis-cluster/6300/redis.conf
```

3. 更改配置

将 6 个文件夹下的 redis.conf 文件中的属性分别作如下修改：

```
port 6300

cluster-enabled yes

cluster-config-file nodes-6300.conf

cluster-node-timeout 15000

appendonly yes
```

```
port 6301

cluster-enabled yes

cluster-config-file nodes-6301.conf

cluster-node-timeout 15000

appendonly yes
```

```
port 6302

cluster-enabled yes

cluster-config-file nodes-6302.conf

cluster-node-timeout 15000

appendonly yes
```

```
port 6303

cluster-enabled yes

cluster-config-file nodes-6303.conf

cluster-node-timeout 15000

appendonly yes
```

```
port 6304

cluster-enabled yes

cluster-config-file nodes-6304.conf

cluster-node-timeout 15000

appendonly yes
```

```
port 6305

cluster-enabled yes

cluster-config-file nodes-6305.conf

cluster-node-timeout 15000

appendonly yes
```

4. 启动这 6 个 redis 服务

在 /usr/local/redis-cluster 下新建一个 start-all.sh 批处理文件：
```
# vim /usr/local/redis-cluster/start-all.sh
cd 6300
./redis-server redis.conf
cd ../
cd 6301
./redis-server redis.conf
cd ../
cd 6302
./redis-server redis.conf
cd ../
cd 6303
./redis-server redis.conf
cd ../
cd 6304
./redis-server redis.conf
cd ../
cd 6305
./redis-server redis.conf
cd ../

# chmod 777 /usr/local/redis-cluster/start-all.sh

# ./start-all.sh

# ps -ef | grep redis
root       3745      1  0 15:38 ?        00:00:05 /usr/local/redis/bin/redis-server 0.0.0.0:6379
root       4651      1  0 16:26 ?        00:00:00 ./6300/redis-server 127.0.0.1:6300 [cluster]
root       4653      1  0 16:26 ?        00:00:00 ./6301/redis-server 127.0.0.1:6301 [cluster]
root       4655      1  0 16:26 ?        00:00:00 ./6302/redis-server 127.0.0.1:6302 [cluster]
root       4657      1  0 16:26 ?        00:00:00 ./6303/redis-server 127.0.0.1:6303 [cluster]
root       4671      1  0 16:26 ?        00:00:00 ./6304/redis-server 127.0.0.1:6304 [cluster]
root       4673      1  0 16:26 ?        00:00:00 ./6305/redis-server 127.0.0.1:6305 [cluster]
root       4692   3200  0 16:26 pts/0    00:00:00 grep --color=auto redis
```

5. 执行集群命令

```
# redis-cli --cluster create 127.0.0.1:6300 127.0.0.1:6301 127.0.0.1:6302 127.0.0.1:6303 127.0.0.1:6304 127.0.0.1:6305 --cluster-replicas 1
>>> Performing hash slots allocation on 6 nodes...
Master[0] -> Slots 0 - 5460
Master[1] -> Slots 5461 - 10922
Master[2] -> Slots 10923 - 16383
Adding replica 127.0.0.1:6304 to 127.0.0.1:6300
Adding replica 127.0.0.1:6305 to 127.0.0.1:6301
Adding replica 127.0.0.1:6303 to 127.0.0.1:6302
>>> Trying to optimize slaves allocation for anti-affinity
[WARNING] Some slaves are in the same host as their master
M: 99a3f15d31691beb55afd29027eb778269fa69a4 127.0.0.1:6300
   slots:[0-5460] (5461 slots) master
M: af6c3c15bff8f692c7110c0683ca2f7c26ff5a21 127.0.0.1:6301
   slots:[5461-10922] (5462 slots) master
M: 221b9c73c6f7016b8649d658a58a260119f01d89 127.0.0.1:6302
   slots:[10923-16383] (5461 slots) master
S: de115d0b3021f72a136965d896f3bdad3631b4e2 127.0.0.1:6303
   replicates 99a3f15d31691beb55afd29027eb778269fa69a4
S: 1d9693b3ff845cec39621a3aadcb452998fafb8c 127.0.0.1:6304
   replicates af6c3c15bff8f692c7110c0683ca2f7c26ff5a21
S: c968cfc6f30e85afeed8065433fc4f728ce80b36 127.0.0.1:6305
   replicates 221b9c73c6f7016b8649d658a58a260119f01d89
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join
........
>>> Performing Cluster Check (using node 127.0.0.1:6300)
M: 99a3f15d31691beb55afd29027eb778269fa69a4 127.0.0.1:6300
   slots:[0-5460] (5461 slots) master
   1 additional replica(s)
M: af6c3c15bff8f692c7110c0683ca2f7c26ff5a21 127.0.0.1:6301
   slots:[5461-10922] (5462 slots) master
   1 additional replica(s)
S: 1d9693b3ff845cec39621a3aadcb452998fafb8c 127.0.0.1:6304
   slots: (0 slots) slave
   replicates af6c3c15bff8f692c7110c0683ca2f7c26ff5a21
S: c968cfc6f30e85afeed8065433fc4f728ce80b36 127.0.0.1:6305
   slots: (0 slots) slave
   replicates 221b9c73c6f7016b8649d658a58a260119f01d89
S: de115d0b3021f72a136965d896f3bdad3631b4e2 127.0.0.1:6303
   slots: (0 slots) slave
   replicates 99a3f15d31691beb55afd29027eb778269fa69a4
M: 221b9c73c6f7016b8649d658a58a260119f01d89 127.0.0.1:6302
   slots:[10923-16383] (5461 slots) master
   1 additional replica(s)
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

--cluster-replicas 1：表示每个主数据库拥有从数据库个数为 1，master 节点不能少于 3 个

6. 集群测试

```
# redis-cli -h 127.0.0.1 -c -p 6300

127.0.0.1:6300> set name zhy
-> Redirected to slot [5798] located at 127.0.0.1:6301
OK

127.0.0.1:6301> get name
"zhy"
```

执行 set 命令，可以看到集群生效，name 值被分配到 6301 节点上了。
