# 下载 windows 版本的 Redis

官网下载地址：http://redis.io/download

**注意：官网只提供 linux 版本的下载。**

windows 版本的 redis 下载地址：https://github.com/MSOpenTech/redis/tags

```
写这份文档的时候，官网的 redis 版本是 5，而 github 的 redis 版本是 3.2（远低于官网的 linux 版本），所以只能借助 ruby 实现 redis 集群。
```

此示例的 redis 目录为：D:\redis\redis
```
D:\redis>dir
2019/09/24  16:01    <DIR>          .
2019/09/24  16:01    <DIR>          ..
2019/07/19  16:44    <DIR>          redis

D:\redis\redis>dir
2019/07/19  16:44    <DIR>          .
2019/07/19  16:44    <DIR>          ..
2019/07/19  16:44             2,648 dump.rdb
2016/07/16  15:03            12,618 Redis%20on%20Windows%20Release%20Notes.docx
2016/07/16  15:03            16,769 Redis%20on%20Windows.docx
2016/07/16  15:03           414,704 redis-benchmark.exe
2016/07/16  15:03         4,370,432 redis-benchmark.pdb
2016/07/16  15:03           265,712 redis-check-aof.exe
2016/07/16  15:03         3,518,464 redis-check-aof.pdb
2016/07/16  15:03           276,968 redis-check-dump.exe
2016/07/16  15:03         3,485,696 redis-check-dump.pdb
2016/07/16  15:03           490,984 redis-cli.exe
2016/07/16  15:03         4,517,888 redis-cli.pdb
2016/07/16  15:03         1,561,576 redis-server.exe
2016/07/16  15:03         6,909,952 redis-server.pdb
2016/07/16  15:03            43,929 redis.windows-service.conf
2016/07/16  15:03            43,927 redis.windows.conf
2016/07/16  15:03            14,265 Windows%20Service%20Documentation.docx
```

# 安装 Ruby 并配置环境

下载地址：https://rubyinstaller.org/downloads/

找到 "7-ZIP ARCHIVES"，下载免安装版的。

解压到 D:\ruby。

## 配置 Ruby 环境变量
1. 新建环境变量
```
"变量名"：RUBY_HOME

"变量值"：D:\ruby
```

2. 编辑系统变量Path
```
在"变量值"最后面添加%RUBY_HOME%\bin;
```

# 搭建 Redis 集群

1. 创建 Redis 集群目录

在 redis 安装的根目录下创建 6 个以端口命名的文件夹。

```
D:\redis>dir
2019/09/24  16:01    <DIR>          .
2019/09/24  16:01    <DIR>          ..
2019/09/24  15:41    <DIR>          6300
2019/09/24  15:43    <DIR>          6301
2019/09/24  15:43    <DIR>          6302
2019/09/24  15:44    <DIR>          6303
2019/09/24  15:44    <DIR>          6304
2019/09/24  15:44    <DIR>          6305
2019/07/19  16:44    <DIR>          redis
```

2. 将安装的 redis 文件夹中的 redis.windows.conf 以及 redis-server ，分别拷贝到新建的这 6 个文件夹中

3. 更改配置

将 6 个文件夹下的 redis.windows.conf 文件中以下属性进行修改：

```
port 6300

cluster-enabled yes

cluster-config-file nodes-6300.conf

cluster-node-timeout 15000

appendonly yes
```

4. 启动这 6 个 redis 服务

在 D:\redis 下新建一个 start-all.bat 批处理文件：
```bat
@echo off

%~d0

set ENV_HOME=%~dp06300
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6300" --loglevel verbose
net start "Redis Cluster 6300"
echo.

set ENV_HOME=%~dp06301
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6301" --loglevel verbose
net start "Redis Cluster 6301"
echo.

set ENV_HOME=%~dp06302
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6302" --loglevel verbose
net start "Redis Cluster 6302"
echo.

set ENV_HOME=%~dp06303
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6303" --loglevel verbose
net start "Redis Cluster 6303"
echo.

set ENV_HOME=%~dp06304
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6304" --loglevel verbose
net start "Redis Cluster 6304"
echo.

set ENV_HOME=%~dp06305
cd %ENV_HOME%
redis-server --service-install redis.windows.conf --service-name "Redis Cluster 6305" --loglevel verbose
net start "Redis Cluster 6305"
echo.

pause
```

附上停止和删除redis集群的批处理文件：

- **stop-all.bat**

```bat
@echo off

net stop "Redis Cluster 6300"

net stop "Redis Cluster 6301"

net stop "Redis Cluster 6302"

net stop "Redis Cluster 6303"

net stop "Redis Cluster 6304"

net stop "Redis Cluster 6305"

pause
```

- **remove-all.bat**

```bat
@echo off

sc delete "Redis Cluster 6300"
echo.

sc delete "Redis Cluster 6301"
echo.

sc delete "Redis Cluster 6302"
echo.

sc delete "Redis Cluster 6303"
echo.

sc delete "Redis Cluster 6304"
echo.

sc delete "Redis Cluster 6305"
echo.

pause
```

# 安装Redis的Ruby驱动

下载地址：https://rubygems.org/pages/download

下载后解压，解压到 D:\rubygems。

1. 执行setup.rb

```
D:\rubygems>ruby setup.rb
```

2. GEM 安装 Redis

```
D:\rubygems>cd D:\redis\redis

D:\redis\redis>gem install redis
```

# 下载集群脚本 redis-trib （重点、重点、重点）

redis 源码下载地址：http://download.redis.io/releases/

**下载对应版本的源码压缩文件。**

步骤：

1. 解压

2. 在 src 目录下找到 redis-trib.rb 文件

3. 把 redis-trib.rb 复制到 D:\redis\redis 目录下。

4. 执行集群命令：

```
D:\redis\redis>ruby redis-trib.rb create --replicas 1 127.0.0.1:6300 127.0.0.1:6301 127.0.0.1:6302 127.0.0.1:6303 127.0.0.1:6304 127.0.0.1:6305
>>> Creating cluster
Connecting to node 127.0.0.1:6300: OK
Connecting to node 127.0.0.1:6301: OK
Connecting to node 127.0.0.1:6302: OK
Connecting to node 127.0.0.1:6303: OK
Connecting to node 127.0.0.1:6304: OK
Connecting to node 127.0.0.1:6305: OK
>>> Performing hash slots allocation on 6 nodes...
Using 3 masters:
127.0.0.1:6300
127.0.0.1:6301
127.0.0.1:6302
Adding replica 127.0.0.1:6303 to 127.0.0.1:6300
Adding replica 127.0.0.1:6304 to 127.0.0.1:6301
Adding replica 127.0.0.1:6305 to 127.0.0.1:6302
M: 5170742460df936521e687ffc3add4d1f379fa0a 127.0.0.1:6300
   slots:0-5460 (5461 slots) master
M: d7abdd46cf39f6882f7fef6f72591c1faa3c500b 127.0.0.1:6301
   slots:5461-10922 (5462 slots) master
M: 9bed8d5d9346b13c93d5ee88d3350b65821cb043 127.0.0.1:6302
   slots:10923-16383 (5461 slots) master
S: 4fedfd721ddd7fd422a896a6223a0d0e85ddcde3 127.0.0.1:6303
   replicates 5170742460df936521e687ffc3add4d1f379fa0a
S: aec49c5b29a4219b501cb2d3e708ffb17c981c79 127.0.0.1:6304
   replicates d7abdd46cf39f6882f7fef6f72591c1faa3c500b
S: 03ededd8aee4a69995996b2183f31419e3578ad8 127.0.0.1:6305
   replicates 9bed8d5d9346b13c93d5ee88d3350b65821cb043
Can I set the above configuration? (type 'yes' to accept): yes
>>> Nodes configuration updated
>>> Assign a different config epoch to each node
>>> Sending CLUSTER MEET messages to join the cluster
Waiting for the cluster to join..
>>> Performing Cluster Check (using node 127.0.0.1:6300)
M: 5170742460df936521e687ffc3add4d1f379fa0a 127.0.0.1:6300
   slots:0-5460 (5461 slots) master
M: d7abdd46cf39f6882f7fef6f72591c1faa3c500b 127.0.0.1:6301
   slots:5461-10922 (5462 slots) master
M: 9bed8d5d9346b13c93d5ee88d3350b65821cb043 127.0.0.1:6302
   slots:10923-16383 (5461 slots) master
M: 4fedfd721ddd7fd422a896a6223a0d0e85ddcde3 127.0.0.1:6303
   slots: (0 slots) master
   replicates 5170742460df936521e687ffc3add4d1f379fa0a
M: aec49c5b29a4219b501cb2d3e708ffb17c981c79 127.0.0.1:6304
   slots: (0 slots) master
   replicates d7abdd46cf39f6882f7fef6f72591c1faa3c500b
M: 03ededd8aee4a69995996b2183f31419e3578ad8 127.0.0.1:6305
   slots: (0 slots) master
   replicates 9bed8d5d9346b13c93d5ee88d3350b65821cb043
[OK] All nodes agree about slots configuration.
>>> Check for open slots...
>>> Check slots coverage...
[OK] All 16384 slots covered.
```

--replicas 1：表示每个主数据库拥有从数据库个数为 1，master 节点不能少于 3 个

5. 集群测试

```
D:\redis\redis>redis-cli -h 127.0.0.1 -c -p 6300
127.0.0.1:6300> set name zhy
-> Redirected to slot [5798] located at 127.0.0.1:6301
OK
```
