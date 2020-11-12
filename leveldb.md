# 添加依赖
```xml
<dependency>
    <groupId>org.iq80.leveldb</groupId>
    <artifactId>leveldb</artifactId>
    <version>0.12</version>
</dependency>
<dependency>
    <groupId>org.iq80.leveldb</groupId>
    <artifactId>leveldb-api</artifactId>
    <version>0.12</version>
</dependency>
```

# model
```java
@Data
public class LevelDbItem {
    private String key;
}

@Data
public class LevelEntity extends LevelDbItem {
    private String name;
}
```

# LevelDbUtil
```java
public class LevelDbUtil {
    private static String path;
    private static Charset charset;

    public static void setPath(String path) {
        setPath(path, Charset.forName("utf-8"));
    }

    public static void setPath(String path, Charset charset) {
        LevelDbUtil.path = path;
        LevelDbUtil.charset = charset;
    }

    private static DB createDb() throws IOException {
        DBFactory factory = new Iq80DBFactory();
        File file = new File(path);
        return factory.open(file, new Options());
    }

    /**
     * 单个写入 db
     *
     * @param data
     * @param <T>
     * @throws Exception
     */
    public static <T extends LevelDbItem> void put(T data) throws Exception {
        DB db = createDb();
        db.put(data.getKey().getBytes(charset), JSONObject.toJSONString(data).getBytes(charset));
        db.close();
    }

    /**
     * 批量写入 db
     *
     * @param dataList
     * @param <T>
     * @throws Exception
     */
    public static <T extends LevelDbItem> void put(List<T> dataList) throws Exception {
        DB db = createDb();
        WriteBatch writeBatch = db.createWriteBatch();
        for (T data : dataList) {
            writeBatch.put(data.getKey().getBytes(charset), JSONObject.toJSONString(data).getBytes(charset));
        }
        db.write(writeBatch);
        writeBatch.close();
        db.close();
    }

    /**
     * 查询键对应的值
     *
     * @param key
     * @param clazz
     * @param <T>
     * @return
     * @throws Exception
     */
    public static <T> T get(String key, Class<T> clazz) throws Exception {
        DB db = createDb();
        byte[] value = db.get(key.getBytes(charset));
        db.close();
        return value == null ? null : JSONObject.parseObject(new String(value, charset), clazz);
    }

    /**
     * 清除目录里的所有文件
     *
     * @throws IOException
     */
    public static void clean() throws IOException {
        Iq80DBFactory factory = new Iq80DBFactory();
        File file = new File(path);
        factory.destroy(file, null);
    }
}
```

#  controller
```java
@RestController
@RequestMapping(value = "/level")
public class LevelDbController {
    private String path = "./data/leveldb";

    @PostConstruct
    public void init() {
        LevelDbUtil.setPath(path);
    }

    @GetMapping(value = "")
    public void leveldb() {
        try {
            List<LevelEntity> list = new ArrayList<>();
            for (int i = 1; i < 10; i++) {
                LevelEntity entity = new LevelEntity();
                entity.setKey(String.valueOf(i));
                entity.setName("name_" + i);
                list.add(entity);
            }
            LevelDbUtil.put(list);
            System.out.println("ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/batch")
    public void leveldbBatch() {
        int index = 1;
        try {
            for (int i = 1; i <= 10000; i++) {
                List<LevelEntity> list = new ArrayList<>();
                for (int j = 1; j <= 200; j++) {
                    LevelEntity entity = new LevelEntity();
                    entity.setKey(String.valueOf(index));
                    entity.setName("name_" + entity.getKey());
                    list.add(entity);
                    index++;
                }
                LevelDbUtil.put(list);
            }
            System.out.println("ok");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GetMapping(value = "/{id}")
    public LevelEntity getLeveldbValue(@PathVariable("id") String id) {
        LevelEntity levelEntity = null;
        try {
            levelEntity = LevelDbUtil.get(id, LevelEntity.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return levelEntity;
    }

    @GetMapping(value = "/clean")
    public void cleanLeveldb() {
        try {
            LevelDbUtil.clean();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
```

# 测试
1. 初始化数据
```
# curl -X GET http://localhost:9991/level
```

2. 查询
```
# curl -X GET http://localhost:9991/level/1001
{"key":"1001","name":"name_1001"}
```

3. 删除 db 目录
```
# curl -X GET http://localhost:9991/level/clean
```

