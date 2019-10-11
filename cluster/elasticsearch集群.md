# 前言（重点、重点、重点）
## 本示例用的是 elasticsearch7.1.1

# 下载 elasticsearch

官网最新版本下载地址：https://www.elastic.co/cn/downloads/elasticsearch

官网历史版本下载地址：https://www.elastic.co/cn/downloads/past-releases#elasticsearch

下载后直接解压。

# 搭建 Redis 集群

有如下 3 个节点：

- 节点1 (192.168.0.10)

- 节点2 (192.168.0.11)

- 节点3 (192.168.0.12)

## elasticsearch.yml 配置说明

|参数|说明|
|--|--|
|cluster.name|集群名称，相同名称为一个集群|
|node.name|节点名称，集群模式下每个节点名称唯一|
|node.master|当前节点是否可以被选举为master节点，是：true、否：false|
|node.data|当前节点是否用于存储数据，是：true、否：false|
|path.data|索引数据存放的位置|
|path.logs|日志文件存放的位置|
|bootstrap.memory_lock|需求锁住物理内存，是：true、否：false|
|bootstrap.system_call_filter|SecComp检测，是：true、否：false|
|network.host|监听地址，用于访问该es|
|network.publish_host|可设置成内网ip，用于集群内各机器间通信|
|http.port|es对外提供的http端口，默认 9200|
|discovery.seed_hosts|es7.x 之后新增的配置，写入候选主节点的设备地址，在开启服务后可以被选为主节点|
|cluster.initial_master_nodes|es7.x 之后新增的配置，初始化一个新的集群时需要此配置来选举master|
|http.cors.enabled|是否支持跨域，是：true，在使用head插件时需要此配置|
|http.cors.allow-origin|"*" 表示支持所有域名|

## 修改节点 1 的 config/elasticsearch.yml

```
cluster.name: my-elasticsearch

node.name: node-1

path.data: ./path/to/data

path.logs: ./path/to/logs

network.host: 0.0.0.0

http.port: 9200

discovery.seed_hosts: ["192.168.0.10", "192.168.0.11", "192.168.0.12"]

cluster.initial_master_nodes: ["node-1"]

http.cors.enabled: true

http.cors.allow-origin: "*"
```

## 修改节点 2 的 config/elasticsearch.yml

```
cluster.name: my-elasticsearch

node.name: node-2

path.data: ./path/to/data

path.logs: ./path/to/logs

network.host: 0.0.0.0

http.port: 9200

discovery.seed_hosts: ["192.168.0.10", "192.168.0.11", "192.168.0.12"]

cluster.initial_master_nodes: ["node-1"]

http.cors.enabled: true

http.cors.allow-origin: "*"
```

## 修改节点 3 的 config/elasticsearch.yml

```
cluster.name: my-elasticsearch

node.name: node-3

path.data: ./path/to/data

path.logs: ./path/to/logs

network.host: 0.0.0.0

http.port: 9200

discovery.seed_hosts: ["192.168.0.10", "192.168.0.11", "192.168.0.12"]

cluster.initial_master_nodes: ["node-1"]

http.cors.enabled: true

http.cors.allow-origin: "*"
```

## 分别启动各个节点下的 elasticsearch 服务
```
# cd elasticsearch

# cd bin

# ./elasticsearch
```

## 查看集群状态

1. 查看集群状态

```
# curl 192.168.0.12:9200/_cat/nodes?v
ip         heap.percent ram.percent cpu load_1m load_5m load_15m node.role master name
192.168.0.11         24          81  7                           mdi       -      node-2
192.168.0.10         22          57  -1                          mdi       *      node-1
192.168.0.12         22          92  7                           mdi       -      node-3

# curl 192.168.0.12:9200/_cat/master?v
id                     host         ip           node
SD41P8qnSkmPsQ5cgiiQiQ 192.168.0.10 192.168.0.10 node-1
```
**node-1 是主节点**

2. 停止节点 1 的 elasticsearch 服务，再查看集群状态

```
# curl 192.168.0.12:9200/_cat/nodes?v
ip         heap.percent ram.percent cpu load_1m load_5m load_15m node.role master name
192.168.0.12         26          92  9                           mdi       -      node-3
192.168.0.11         19          81  6                           mdi       *      node-2

# curl 192.168.0.12:9200/_cat/master?v
id                     host         ip           node
rJHnTJC3ROO5ywoFSvwhEA 192.168.0.12 192.168.0.12 node-2
```
**node-2 被选举为主节点**

3. 停止节点 2 的 elasticsearch 服务

**集群中只剩一个节点（node-3）了，此时，整个集群不可用。**
