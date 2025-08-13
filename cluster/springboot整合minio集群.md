# springboot 整合 minio 集群

## 前言

|服务器IP地址|服务名称|
|--|--|
|192.168.0.10|nginx|
|192.168.0.11|minio|
|192.168.0.12|minio|

## nginx 反向代理

1. 修改 nginx 目录所有者
   ```bash
   # chown -R root:root /usr/local/nginx
   ```

2. 修改 nginx 用户
   ```bash
   # vim /usr/local/nginx/conf/nginx.conf
   ```
   ```
   user root;
   ```
   user 必须跟 nginx 目录所有者一致，否则会出现无访问权限的问题。

3. 配置 minio 反向代理
   ```bash
   # vim /usr/local/nginx/conf/nginx.conf
   ```
   ```
   upstream minio {
        least_conn;
        server 192.168.0.11:9000;
        server 192.168.0.12:9000;
   }

   upstream minio_console {
        least_conn;
        server 192.168.0.11:9001;
        server 192.168.0.12:9001;
   }

    # 配置 Minio API 代理
    server {
       listen       9000;
       server_name  localhost;

       location / {
            proxy_pass http://minio;
            
            # Host 必须配置为 $http_host（不要配置成了 $host），关于两者的区别，请参考 nginx 仓库里的 《$http_host与$host.md》
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
       }
    }

    # 配置 Minio console 代理
    server {
       listen       9001;
       server_name  localhost;

       location / {
            proxy_pass http://minio_console;

            # Host 必须配置为 $http_host（不要配置成了 $host），关于两者的区别，请参考 nginx 仓库里的 《$http_host与$host.md》
            proxy_set_header Host $http_host;
            proxy_set_header X-Real-IP $remote_addr;
            proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
            proxy_set_header X-Forwarded-Proto $scheme;
            proxy_set_header X-NginX-Proxy true;

            # 关键：支持WebSocket（MinIO控制台依赖）
            proxy_http_version 1.1;
            proxy_set_header Upgrade $http_upgrade;
            proxy_set_header Connection "upgrade";  # 动态处理连接类型

            # 延长超时时间（避免WebSocket连接被过早断开）
            proxy_read_timeout 300s;  # 5分钟无数据不中断
            proxy_send_timeout 300s;
            proxy_connect_timeout 60s;
       }
    }
   ```

4. 重启 nginx 服务
   ```bash
   # systemctl restart nginx
   ```

## springboot 配置

```yml
minio:
    endpoint: http://192.168.0.10:9000
    access-key: admin
    secret-key: admin
```

如果同时满足以下两点，就说明 nginx 反向代理配置成功：

1. 后台调用 ```http://192.168.0.10:9000``` 的 API 跟调用 ```http://192.168.0.11/12:9000``` 的 API 是一样的效果
2. 前端访问 ```http://192.168.0.10:9001``` 跟访问 ```http://192.168.0.11/12:9001``` 是一样的效果
