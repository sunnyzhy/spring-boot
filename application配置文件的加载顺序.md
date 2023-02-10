# application 配置文件的加载顺序

## 前言

1. 源码里配置文件的加载类 ```ConfigFileApplicationListener``` 已被标记为 ```@Deprecated```
2. 现行的配置文件加载类为 ```ConfigDataEnvironment```

## 解析 ConfigDataEnvironment

### 初始化 locations

```java
List<ConfigDataLocation> locations = new ArrayList();
locations.add(ConfigDataLocation.of("optional:classpath:/;optional:classpath:/config/"));
locations.add(ConfigDataLocation.of("optional:file:./;optional:file:./config/;optional:file:./config/*/"));
```

locations 分为两部分:

1. ```classpath```: 源码里的```resources``` 目录，也即打包之后 ```jar``` 文件内部的 ```BOOT-INF/classes/``` 目录
2. ```file```: ```jar``` 文件所在的目录，也即打包之后 ```jar``` 文件外部的目录

### 初始化 contributors

```java
private void addInitialImportContributors(List<ConfigDataEnvironmentContributor> initialContributors, ConfigDataLocation[] locations) {
	for(int i = locations.length - 1; i >= 0; --i) {
		initialContributors.add(this.createInitialImportContributor(locations[i]));
	}

}
```

把 ```file``` 置于 ```classpath``` 的前面，即在加载的时候先加载 ```file.location```， 再加载 ```classpath.location```

## 总结

application 配置文件的加载顺序为:

1. 先加载 ```file.location``` ，即先加载 ```jar``` 文件外部目录里的 ```application``` 配置文件
   优先级递增:
      - ```file:./```
      - ```file:./config/```
      - ```file:./config/*/```

2. 如果不存在 ```file.location```，再加载 ```classpath.location```，即 ```jar``` 文件内部 ```BOOT-INF/classes/``` 目录里的 ```application``` 配置文件
   优先级递增:
      - ```classpath:/```
      - ```classpath:/config/```

3. 同级目录里的 ```application.properties``` 优先级高于 ```application.yml```

### 示例一

最终加载的是 ```./config/x/application.yml```:

```bash
# tree
.
├── application.yml
├── conf-demo-0.0.1.jar
└── config
    ├── application.yml
    └── x
        └── application.yml
```

### 示例二

最终加载的是 ```BOOT-INF/classes/config/application.properties```:

```bash
BOOT-INF/classes/application.properties
BOOT-INF/classes/application.yml
BOOT-INF/classes/config/application.properties
BOOT-INF/classes/config/application.yml
```
