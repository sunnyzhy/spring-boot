# JsonUtil

## 初始化 ObjectMapper

```java
@Slf4j
public class JsonUtil {

    private static ObjectMapper objectMapper = new ObjectMapper();

    static {
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
}
```

## 简单对象转换

```java
/**
 * 将对象转为JSON字符串
 *
 * @param t
 * @return
 */
public static <T> String toString(T t) {
    if (t == null) {
        return null;
    }
    try {
        return t instanceof String ? (String) t : objectMapper.writeValueAsString(t);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将对象转为JSON字符串(换行)
 *
 * @param t
 * @return
 */
public static <T> String toStringPretty(T t) {
    if (t == null) {
        return null;
    }
    try {
        return t instanceof String ? (String) t
                : objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(t);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将JSON字符串转为简单对象
 *
 * @param json
 * @param clazz
 * @return
 */
public static <T> T toObject(String json, Class<T> clazz) {
    if (StringUtils.isEmpty(json)) {
        return null;
    }
    try {
        return clazz.equals(String.class) ? (T) json : objectMapper.readValue(json, clazz);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将Object对象转为具体类型的简单对象
 *
 * @param object
 * @param clazz
 * @param <T>
 * @return
 */
public static <T> T toObject(Object object, Class<T> clazz) {
    if (object == null) {
        return null;
    }
    try {
        return objectMapper.convertValue(object, clazz);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * Json流式解析文件（JsonParser）
 *
 * @param file
 * @param clazz
 * @param <T>
 * @return
 */
public static <T> T toObject(File file, Class<T> clazz) {
    if (file == null) {
        return null;
    }
    try {
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser jsonParser = jsonFactory.createParser(file);
        return objectMapper.readValue(jsonParser, clazz);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将对象转为字节流
 *
 * @param t
 * @param <T>
 * @return
 */
public static <T> byte[] toBytes(T t) {
    if (t == null) {
        return null;
    }
    try {
        return objectMapper.writeValueAsBytes(t);
    } catch (JsonProcessingException e) {
        log.warn(e.getMessage(), e);
        return new byte[]{};
    }
}

/**
 * 将JSON字符串转为成员是简单对象的List
 * 如果成员是复杂对象的请使用 T toObject(String json, TypeReference<T> typeReference)
 *
 * @param json
 * @param elementClass
 * @param <T>
 * @return
 */
public static <T> List<T> toList(String json, Class<T> elementClass) {
    if (StringUtils.isEmpty(json)) {
        return null;
    }
    try {
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, elementClass);
        return objectMapper.readValue(json, collectionType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return new ArrayList<>();
    }
}

/**
 * 将JSON字符串转为成员是简单对象的Collection
 * 如果成员是复杂对象的请使用 T toObject(String json, TypeReference<T> typeReference)
 *
 * @param json
 * @param collectionClass
 * @param elementClass
 * @param <T>
 * @return
 */
public static <T> Collection<T> toCollection(String json, Class<? extends Collection> collectionClass, Class<T> elementClass) {
    if (StringUtils.isEmpty(json)) {
        return null;
    }
    try {
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
        return objectMapper.readValue(json, collectionType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将List转为成员是简单对象的List
 * 如果成员是复杂对象的请使用 T toObject(Object object, TypeReference<T> typeReference)
 *
 * @param list
 * @param clazz
 * @param <T>
 * @return
 */
public static <T> List<T> toList(List list, Class<T> clazz) {
    if (list == null || list.isEmpty()) {
        return new ArrayList<>();
    }
    try {
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
        return objectMapper.convertValue(list, listType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return new ArrayList<>();
    }
}

/**
 * 将List转为成员是简单对象的Collection
 * 如果成员是复杂对象的请使用 T toObject(Object object, TypeReference<T> typeReference)
 *
 * @param list
 * @param collectionClass
 * @param elementClass
 * @param <T>
 * @return
 */
public static <T> Collection<T> toCollection(List list, Class<? extends Collection> collectionClass, Class<T> elementClass) {
    if (list == null || list.isEmpty()) {
        return null;
    }
    try {
        CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
        return objectMapper.convertValue(list, listType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将JSON字符串转为成员是简单对象的Map
 * 如果成员是复杂对象的请使用 T toObject(String json, TypeReference<T> typeReference)
 *
 * @param json
 * @param keyClazz
 * @param valueClazz
 * @param <K>
 * @param <V>
 * @return
 */
public static <K, V> Map<K, V> toMap(String json, Class<K> keyClazz, Class<V> valueClazz) {
    if (StringUtils.isEmpty(json)) {
        return new HashMap<>();
    }
    try {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClazz, valueClazz);
        return objectMapper.readValue(json, mapType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return new HashMap<>();
    }
}

/**
 * 将Object转为成员是简单对象的Map
 * 如果成员是复杂对象的请使用 T toObject(Object object, TypeReference<T> typeReference)
 *
 * @param object
 * @param keyClazz
 * @param valueClazz
 * @param <K>
 * @param <V>
 * @return
 */
public static <K, V> Map<K, V> toMap(Object object, Class<K> keyClazz, Class<V> valueClazz) {
    if (object == null) {
        return new HashMap<>();
    }
    try {
        MapType mapType = objectMapper.getTypeFactory().constructMapType(Map.class, keyClazz, valueClazz);
        return objectMapper.convertValue(object, mapType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return new HashMap<>();
    }
}
```

## 复杂对象转换

```java
/**
 * 将JSON字符串转为复杂对象(List/Map)
 *
 * @param json
 * @param typeReference
 * @return
 */
public static <T> T toObject(String json, TypeReference<T> typeReference) {
    if (StringUtils.isEmpty(json)) {
        return null;
    }
    try {
        return (T) (typeReference.getType().equals(String.class) ? (T) json
                : objectMapper.readValue(json, typeReference));
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将Object对象转为具体类型的复杂对象(List/Map)
 *
 * @param object
 * @param typeReference
 * @param <T>
 * @return
 */
public static <T> T toObject(Object object, TypeReference<T> typeReference) {
    if (object == null) {
        return null;
    }
    try {
        return objectMapper.convertValue(object, typeReference);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将JSON字符串转为复杂对象(List/Map)
 * 需要自己逐层构造 JavaType
 *
 * @param json
 * @param typeReference
 * @return
 */
public static <T> T toObject(String json, JavaType javaType) {
    if (StringUtils.isEmpty(json)) {
        return null;
    }
    try {
        return (T) (javaType.getClass().equals(String.class) ? (T) json
                : objectMapper.readValue(json, javaType));
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}

/**
 * 将Object对象转为具体类型的复杂对象(List/Map)
 * 需要自己逐层构造 JavaType
 *
 * @param object
 * @param javaType
 * @param <T>
 * @return
 */
public static <T> T toObject(Object object, JavaType javaType) {
    if (object == null) {
        return null;
    }
    try {
        return objectMapper.convertValue(object, javaType);
    } catch (Exception e) {
        log.warn(e.getMessage(), e);
        return null;
    }
}
```

## 单元测试

```java
@Data
public class User {
    private int id;
    private String name;
    private Address address;

    @Data
    public static class Address {
        private String country;
        private String province;
        private String city;
    }
}

@Data
public class User1 {
    private int id;
    private String name;
    private Address1 address;

    @Data
    public static class Address1 {
        private String country;
        private String province;
        private String city;
    }
}

@Data
public class JsonData<T> {
    private T data;
}
```

### 简单对象转换

```java
@Test
void json1() {
    {
        User user = new User();
        user.setId(1);
        user.setName("user");
        user.setAddress(new User.Address() {{
            setCountry("cn");
            setProvince("hb");
            setCity("wh");
        }});
        User1 user1 = JsonUtil.toObject(user, User1.class);
        System.out.println(user1);
    }

    {
        User[] list = new User[5];
        for (int i = 1; i <= list.length; i++) {
            User user = new User();
            user.setId(i);
            user.setName("user" + i);
            user.setAddress(new User.Address() {{
                setCountry("cn");
                setProvince("hb");
                setCity("wh");
            }});
            list[i - 1] = user;
        }
        User1[] list1 = JsonUtil.toObject(list, User1[].class);
        System.out.println(list1);
    }
}

@Test
void json2() {
    List<User> list = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
        User user = new User();
        user.setId(i);
        user.setName("user" + i);
        user.setAddress(new User.Address() {{
            setCountry("cn");
            setProvince("hb");
            setCity("wh");
        }});
        list.add(user);
    }
    List<User1> list1 = JsonUtil.toList(list, User1.class);
    System.out.println(list1);
}

@Test
void json3() {
    Map<Integer, User> map = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
        User user = new User();
        user.setId(i);
        user.setName("user" + i);
        user.setAddress(new User.Address() {{
            setCountry("cn");
            setProvince("hb");
            setCity("wh");
        }});
        map.put(i, user);
    }
    Map<Integer, User1> map1 = JsonUtil.toMap(map, Integer.class, User1.class);
    System.out.println(map1);
}
```

### 复杂对象转换

```java
@Test
void json4() {
    List<Object> list = new ArrayList<>();
    for (int i = 1; i <= 5; i++) {
        User user = new User();
        user.setId(i);
        user.setName("user" + i);
        user.setAddress(new User.Address() {{
            setCountry("cn");
            setProvince("hb");
            setCity("wh");
        }});
        list.add(user);
    }
    List<User1> list1 = JsonUtil.toObject(list, new TypeReference<List<User1>>() {
    });
    System.out.println(list1);
}

@Test
void json5() {
    Map<Object, Object> map = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
        User user = new User();
        user.setId(i);
        user.setName("user" + i);
        user.setAddress(new User.Address() {{
            setCountry("cn");
            setProvince("hb");
            setCity("wh");
        }});
        map.put(i, user);
    }
    Map<Integer, User1> map1 = JsonUtil.toObject(map, new TypeReference<Map<Integer, User1>>() {
    });
    System.out.println(map1);
}

@Test
void json6() {
    JsonData<Map<Object, List<Object>>> jsonData = new JsonData<>();
    Map<Object, List<Object>> map = new HashMap<>();
    for (int i = 1; i <= 5; i++) {
        List<Object> list = new ArrayList<>();
        for (int j = i; j <= 5; j++) {
            User user = new User();
            user.setId(i * j);
            user.setName("user" + user.getId());
            user.setAddress(new User.Address() {{
                setCountry("cn");
                setProvince("hb");
                setCity("wh");
            }});
            list.add(user);
        }
        map.put(i, list);
    }
    jsonData.setData(map);

    // 使用 JavaType 转换
    // 获取 Type 的工厂
    TypeFactory typeFactory = objectMapper.getTypeFactory();
    // 由内到外，一层层地创建对应的类型
    // 创建 List<User1> 类型
    JavaType listType = typeFactory.constructCollectionType(List.class, User1.class);
    // 创建 Integer 类型
    JavaType integerType = typeFactory.constructType(Integer.class);
    // 创建 Map<Integer, List<User1>> 类型
    JavaType mapType = typeFactory.constructMapType(Map.class, integerType, listType);
    // 创建Json<Map<Integer, List<User1>>> 类型
    JavaType lastType = typeFactory.constructParametricType(JsonData.class, mapType);
    JsonData<Map<Integer, List<User1>>> jsonData1 = JsonUtil.toObject(jsonData, lastType);
    System.out.println(jsonData1);

    // 使用 TypeReference<T> 转换
    JsonData<Map<Integer, List<User1>>> jsonData2 = JsonUtil.toObject(jsonData, new TypeReference<JsonData<Map<Integer, List<User1>>>>() {
    });
    System.out.println(jsonData2);
}
```
