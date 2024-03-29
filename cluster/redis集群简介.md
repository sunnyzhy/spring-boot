# Redis容错机制
节点之间会定时的互相发送 ping 命令，测试节点的健康状态，当节点接受到 ping 命令后，会返回一个 pong 字符串。

投票机制：如果一个节点 A 给节点 B 发送 ping 没有得到 pong 返回，会通知其他节点再次给 B 发送 ping ，如果集群中有超过一半的节点收不 B 节点的 pong。那么就认为 B 节点挂了。一般会为每个节点提供一个备份节点，如果挂掉会切换到备份节点。

# Redis集群存储原理
Redis 对于每个存放的 key 会进行 hash 操作，生成一个 [0 - 16384] 的 hash 值（先进行 crc 算法再对 16384 取余）。

集群的情况下，就是把 [0 - 16384] 的区间进行拆分，放到不同的 redis 中。每个 Redis 集群理论上最多可以有 16384 个节点。

# Redis持久化
Snapshotting：定时的将 Redis 内存中的数据保存到硬盘中。

AOF：将所有的 command 操作保存到 aof 中，AOP 的同步频率很高，数据即使丢失，粒度也很小，但会在性能上造成影响。

# Redis集群搭建所需要的环境
1. **Redis 集群至少需要 3 个节点**，因为投票容错机制要求超过半数节点认为某个节点挂了该节点才是挂了，所以 2 个节点无法构成集群。

2. **要保证集群的高可用，需要每个节点都有从节点，也就是备份节点，所以 Redis 集群至少需要 6 台服务器。**

3. **Redis5.0 以下版本搭建集群，必须安装Ruby。**

# Redis 模式
## 单节点模式
单节点模式是最简单的，适用于平时做测试、写 demo 需要用到缓存的场景，现实生产环境中基本不会使用单节点模式。

## 主从模式（master/slaver）

- **主从模式特点**

   - 主从模式的一个作用是备份数据，这样当一个节点损坏（指不可恢复的硬件损坏）时，因为数据有备份，可以方便恢复。

   - 另一个作用是负载均衡，所有客户端都访问一个节点肯定会影响 Redis 工作效率，有了主从以后，查询操作就可以通过查询从节点来完成。

   1. 一个 Master 可以有多个 Slave。

   2. 默认配置下，master 节点可以进行读和写，slave 节点只能进行读操作，写操作被禁止。

   3. 不要修改配置让 slave 节点支持写操作，没有意义，原因一，写入的数据不会被同步到其他节点；原因二，当 master 节点修改同一条数据后，slave 节点的数据会被覆盖掉
   
   4. slave 节点挂了不影响其他 slave 节点的读和 master 节点的读写，重新启动后会将数据从 master 节点同步过来
   
   5. master 节点挂了以后，不影响 slave 节点的读，Redis 将不再提供写服务，master 节点启动后 Redis 将重新对外提供写服务
   
   6. master 节点挂了以后，不会从 slave 节点里重新选一个 master (主从模式的缺点）

- **主从节点的缺点**

master 节点挂了以后，redis 就不能对外提供写服务了，因为剩下的 slave 不能成为 master。

这个缺点影响是很大的，尤其是对生产环境来说，所以有了下面的 sentinel 模式。

## sentinel模式
sentinel 的中文含义是哨兵、守卫。在主从模式中，当 master 节点挂了以后，slave 节点不能主动选举一个 master 节点出来，那么就安排一个或多个 sentinel 来做这件事，当 sentinel 发现 master 节点挂了以后，sentinel 就会从 slave 中重新选举一个 master。

sentinel 模式的特点：

- sentinel 模式是建立在主从模式的基础上，如果只有一个主节点，sentinel 就没有任何意义

- 当 master 节点挂了以后，sentinel 会在 slave 中选择一个节点做为 master，并修改它们的配置文件，其他 slave 的配置文件也会被修改，比如 slaveof 属性会指向新的 master

- 当 master 节点重新启动后，它将不再是 master 而是做为 slave 接收新的 master 节点的同步数据

- 因为 sentinel 也是一个可能会挂掉的进程，所以 sentinel 也会启动多个形成一个 sentinel 集群

- 当主从模式配置密码时，sentinel 也会同步将配置信息修改到配置文件中

- 一个 sentinel 或 sentinel 集群可以管理多个主从 Redis

- sentinel 最好不要和 Redis 部署在同一台机器上，不然 Redis 服务器挂了以后，sentinel 也跟着挂了

- sentinel 监控的 Redis 集群都会定义一个 master 名字，这个名字代表 Redis 集群的主节点

- 当使用 sentinel 模式的时候，客户端就不要直接连接 Redis 节点，而是连接 sentinel 的 ip 和 port，由 sentinel 来提供具体的可提供服务的 Redis 实现，这样当 master 节点挂掉以后，sentinel 就会感知并将新的 master 节点提供给客户端

- sentinel 模式基本可以满足一般生产的需求，具备高可用性。但是当数据量过大时，主从模式或者 sentinel 模式就不能满足需求了，这个时候需要对存储的数据进行分片，将数据存储到多个 Redis 实例中，所以有了下面的 cluster 模式。

## cluster模式
redis3.0 之后版本支持 redis-cluster 集群，Redis-Cluster采用无中心结构，每个节点保存数据和整个集群状态,每个节点都和其他所有节点连接。

cluster 的出现是为了解决单机 Redis 容量有限的问题，将 Redis 的数据根据一定的规则分配到多台机器。

cluster 模式的特点：

- 所有的 redis 节点彼此互联(PING - PONG 机制),内部使用二进制协议优化传输速度和带宽

- 节点的 fail 是通过集群中超过半数的节点检测失效时才生效

- 客户端与 redis 节点直连，不需要中间 proxy 层。客户端不需要连接集群所有节点，连接集群中任何一个可用节点即可

- redis-cluster 把所有的物理节点映射到 [0 - 16383] slot 上（不一定是平均分配）,cluster 负责维护 node <-> slot <-> value

- Redis 集群预分好 16384 个桶，当需要在 Redis 集群中放置一个 key-value 时，根据 CRC16(key) mod 16384 的值，决定将一个 key 放到哪个桶中。

这种模式适合数据量巨大的缓存要求，当数据量不是很大使用 sentinel 即可。

### redis cluster节点分配
比如有 A、B、C 三个节点，它们可以是一台机器上的三个端口，也可以是三台不同的服务器。采用哈希槽 (hash slot) 的方式来分配 16384 个 slot 的话，它们三个节点分别承担的 slot 区间是：
```
节点 A 覆盖0－5460

节点 B 覆盖5461－10922

节点 C 覆盖10923－16383
```

- 获取数据

如果存入一个值，按照 redis cluster 哈希槽的算法： CRC16('key')384 = 6782。 那么就会把这个 key 的存储分配到 B 上了。同样，当我连接 A、B、C 任何一个节点想获取 'key' 这个 key 时，就会根据算法跳转到 B 节点上获取数据。

- 新增一个主节点
 
新增一个节点 D，redis cluster 会从各个节点的前面各取一部分 slot 到 D 上：
```
节点 A 覆盖1365-5460

节点 B 覆盖6827-10922

节点 C 覆盖12288-16383

节点 D 覆盖0-1364,5461-6826,10923-12287
```

同样删除一个节点也是类似，移动完成后就可以删除这个节点了。

### Redis Cluster主从模式
redis cluster 为了保证数据的高可用性，加入了主从模式，一个主节点对应一个或多个从节点，主节点提供数据存取，从节点则是从主节点拉取数据备份，当这个主节点挂掉后，就会有这个从节点选取一个来充当主节点，从而保证集群不会挂掉。

上面那个例子里, 集群有 A、B、C 三个主节点, 如果这 3 个节点都没有加入从节点，如果 B 挂掉了，我们就无法访问整个集群了。A 和 C 的 slot 也无法访问。

所以我们在集群建立的时候，一定要为每个主节点都添加从节点, 比如集群包含主节点 A、B、C, 以及从节点 A1、B1、C1, 那么即使 B 挂掉系统也可以继续正确工作。

B1 节点替代了 B 节点，所以 Redis 集群将会选择 B1 节点作为新的主节点，集群将会继续正确地提供服务。当 B 重新开启后，它就会变成 B1 的从节点。

**不过需要注意，如果节点 B 和 B1 同时挂了，Redis 集群就无法继续正确地提供服务了。**

### redis 集群的搭建

集群中至少应该有奇数个节点，所以至少有三个节点，每个节点至少有一个备份节点，所以下面使用 6 节点（主节点、备份节点由 redis-cluster 集群确定）。
