# elasticsearch 集群命令
## _cat
语法：
```
# curl localhost:9200/_cat
```

- /_cat/allocation
- /_cat/shards
- /_cat/shards/{index}
- /_cat/master
- /_cat/nodes
- /_cat/indices
- /_cat/indices/{index}
- /_cat/segments
- /_cat/segments/{index}
- /_cat/count
- /_cat/count/{index}
- /_cat/recovery
- /_cat/recovery/{index}
- /_cat/health
- /_cat/pending_tasks
- /_cat/aliases
- /_cat/aliases/{alias}
- /_cat/thread_pool
- /_cat/plugins
- /_cat/fielddata
- /_cat/fielddata/{fields}
- /_cat/nodeattrs
- /_cat/repositories
- /_cat/snapshots/{repository}

## verbose
v 参数，显示详细的信息：
```
# curl localhost:9200/_cat/master?v
id                     host      ip        node
yBet3cYzQbC68FRzLZDmFg 127.0.0.1 127.0.0.1 node-1
```

## help
help 参数，输出可以显示的列：
```
# curl localhost:9200/_cat/master?help
id   |   | node id
host | h | host name
ip   |   | ip address
node | n | node name
```

## headers
h 参数，指定输出的字段：
```
# curl localhost:9200/_cat/master?v
id                      host      ip        node
yBet3cYzQbC68FRzLZDmFg  127.0.0.1 127.0.0.1 node-1
 
# curl localhost:9200/_cat/master?h=ip,node
127.0.0.1 node-1
```
