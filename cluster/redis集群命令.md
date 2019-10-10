# Redis 集群命令
## 集群
- cluster info ：打印集群的信息

- cluster nodes ：列出集群当前已知的所有节点（ node），以及这些节点的相关信息。

## 节点
- cluster meet <ip> <port> ：将 ip 和 port 所指定的节点添加到集群当中，让它成为集群的一份子。

- cluster forget <node_id> ：从集群中移除 node_id 指定的节点。

- cluster replicate <node_id> ：将当前节点设置为 node_id 指定的节点的从节点。

- cluster saveconfig ：将节点的配置文件保存到硬盘里面。
   
## 槽(slot)
- cluster addslots <slot> [slot ...] ：将一个或多个槽（ slot）指派（ assign）给当前节点。

- cluster delslots <slot> [slot ...] ：移除一个或多个槽对当前节点的指派。

- cluster flushslots ：移除指派给当前节点的所有槽，让当前节点变成一个没有指派任何槽的节点。

- cluster setslot <slot> node <node_id> ：将槽 slot 指派给 node_id 指定的节点，如果槽已经指派给另一个节点，那么先让另一个节点删除该槽>，然后再进行指派。

- cluster setslot <slot> migrating <node_id> ：将本节点的槽 slot 迁移到 node_id 指定的节点中。

- cluster setslot <slot> importing <node_id> ：从 node_id 指定的节点中导入槽 slot 到本节点。

- cluster setslot <slot> stable ：取消对槽 slot 的导入（ import）或者迁移（ migrate）。
   
## 键
- cluster keyslot <key> ：计算键 key 应该被放置在哪个槽上。

- cluster countkeysinslot <slot> ：返回槽 slot 目前包含的键值对数量。

- cluster getkeysinslot <slot> <count> ：返回 count 个 slot 槽中的键。 

## 命令示例
```
>redis-cli -h 127.0.0.1 -c -p 6302

127.0.0.1:6302> cluster nodes
49bb5dd2e2f4134d6881731a97be6b772a5900b7 127.0.0.1:6303 slave a9c82c53f61748fdfb3b4d5b16b6ae4c8c8094a9 0 1569401031266 4 connected
26fb33a135a6a492afda0643910b314de4814114 127.0.0.1:6301 slave c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 0 1569401032265 7 connected
a9c82c53f61748fdfb3b4d5b16b6ae4c8c8094a9 127.0.0.1:6300 master - 1569401026357 1569401024261 1 connected 0-5460
bc10406c29047e11cda32fbdbc3da574443f2ea0 127.0.0.1:6302 myself,master - 0 0 3 connected 10923-16383
f790bf6bfdd5915b88d761779a8fd36733982de4 127.0.0.1:6305 slave bc10406c29047e11cda32fbdbc3da574443f2ea0 0 1569401030265 6 connected
c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 127.0.0.1:6304 master - 0 1569401033272 7 connected 5461-10922

127.0.0.1:6302> set age 20
-> Redirected to slot [741] located at 127.0.0.1:6300
OK

127.0.0.1:6300> get age
"20"

127.0.0.1:6302> get age
-> Redirected to slot [741] located at 127.0.0.1:6300
"20"

127.0.0.1:6302> cluster keyslot age
(integer) 741
```
说明：

- -c：以集群方式连接 redis

- 主节点（有 slot 区间）：127.0.0.1:6300、127.0.0.1:6302、127.0.0.1:6304

- 从节点：127.0.0.1:6301、127.0.0.1:6303、127.0.0.1:6305

- myself：当前连接的 redis 节点

- 键 age 被放置在 741 的槽上，即主节点 127.0.0.1:6300


停止主节点 127.0.0.1:6300，再查看集群节点：
```
127.0.0.1:6302> cluster nodes
49bb5dd2e2f4134d6881731a97be6b772a5900b7 127.0.0.1:6303 master - 0 1569401200552 8 connected 0-5460
26fb33a135a6a492afda0643910b314de4814114 127.0.0.1:6301 slave c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 0 1569401202553 7 connected
a9c82c53f61748fdfb3b4d5b16b6ae4c8c8094a9 127.0.0.1:6300 master,fail - 1569401026357 1569401024261 1 connected
bc10406c29047e11cda32fbdbc3da574443f2ea0 127.0.0.1:6302 myself,master - 0 0 3 connected 10923-16383
f790bf6bfdd5915b88d761779a8fd36733982de4 127.0.0.1:6305 slave bc10406c29047e11cda32fbdbc3da574443f2ea0 0 1569401199554 6 connected
c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 127.0.0.1:6304 master - 0 1569401201552 7 connected 5461-10922
```

说明：
- 主节点 127.0.0.1:6300 处于 fail 状态，即离线状态

- 从节点 127.0.0.1:6303 被选为了主节点

停止从节点 127.0.0.1:6301、127.0.0.1:6305，再查看集群节点：
```
127.0.0.1:6302> cluster nodes
49bb5dd2e2f4134d6881731a97be6b772a5900b7 127.0.0.1:6303 master - 0 1569403648139 8 connected 0-5460
26fb33a135a6a492afda0643910b314de4814114 127.0.0.1:6301 slave,fail c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 1569403611942 1569403610936 7 connected
a9c82c53f61748fdfb3b4d5b16b6ae4c8c8094a9 127.0.0.1:6300 master,fail - 1569401026357 1569401024261 1 connected
bc10406c29047e11cda32fbdbc3da574443f2ea0 127.0.0.1:6302 myself,master - 0 0 3 connected 10923-16383
f790bf6bfdd5915b88d761779a8fd36733982de4 127.0.0.1:6305 slave,fail bc10406c29047e11cda32fbdbc3da574443f2ea0 1569403616243 1569403615945 6 connected
c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 127.0.0.1:6304 master - 0 1569403649137 7 connected 5461-10922
```

说明：
- 现在只剩主节点 127.0.0.1:6302、127.0.0.1:6303、127.0.0.1:6304

```
127.0.0.1:6302> set age 10
-> Redirected to slot [741] located at 127.0.0.1:6303
OK

127.0.0.1:6303> get age
"10"
```

停止主节点 127.0.0.1:6303，再查看集群节点：
```
127.0.0.1:6302> cluster nodes
49bb5dd2e2f4134d6881731a97be6b772a5900b7 127.0.0.1:6303 master,fail - 1569403902395 1569403899692 8 connected 0-5460
26fb33a135a6a492afda0643910b314de4814114 127.0.0.1:6301 slave,fail c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 1569403611942 1569403610936 7 connected
a9c82c53f61748fdfb3b4d5b16b6ae4c8c8094a9 127.0.0.1:6300 master,fail - 1569401026357 1569401024261 1 connected
bc10406c29047e11cda32fbdbc3da574443f2ea0 127.0.0.1:6302 myself,master - 0 0 3 connected 10923-16383
f790bf6bfdd5915b88d761779a8fd36733982de4 127.0.0.1:6305 slave,fail bc10406c29047e11cda32fbdbc3da574443f2ea0 1569403616243 1569403615945 6 connected
c4f8dfa1cd97d9d5ebf5ef12a54fc80cba1fdcbc 127.0.0.1:6304 master - 0 1569403928182 7 connected 5461-10922
```

说明：
- 现在只剩主节点 127.0.0.1:6302、127.0.0.1:6304

- 此时，redis 集群已经 down 掉了

```
127.0.0.1:6302> set age 12
(error) CLUSTERDOWN The cluster is down
```
