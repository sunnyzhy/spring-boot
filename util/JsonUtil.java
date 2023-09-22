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
     * 将JSON字符串转为指定对象
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T toObject(String json, Class<T> clazz) {
        if (StringUtils.isEmpty(json) || clazz == null) {
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
     * JSON字符串转换为Java泛型对象
     *
     * @param json
     * @param typeReference
     * @return
     */
    public static <T> T toObject(String json, TypeReference<T> typeReference) {
        if (StringUtils.isEmpty(json) || typeReference == null) {
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

    public static <T> T toObject(String json, Class<T> collectionClass, Class<?>... elementClass) {
        JavaType javaType = objectMapper.getTypeFactory().constructParametricType(collectionClass, elementClass);

        try {
            return objectMapper.readValue(json, javaType);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    /**
     * json流式解析文件（JsonParser）
     * @param file
     * @param clazz
     * @return
     * @param <T>
     */
    public static <T> T toObject(File file, Class<T> clazz) {
        try {
            JsonFactory jsonFactory = new MappingJsonFactory();
            JsonParser jsonParser = jsonFactory.createParser(file);
            T obj = objectMapper.readValue(jsonParser, clazz);
            return obj;
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }

    public static <T> byte[] toBytes(T t) {
        try {
            return objectMapper.writeValueAsBytes(t);
        } catch (JsonProcessingException e) {
            log.warn(e.getMessage(), e);
            return new byte[]{};
        }
    }

    public static <T> List<T> toList(String json, Class<? extends Collection> collectionClass, Class<T> elementClass) {
        CollectionType collectionType = objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);

        try {
            return objectMapper.readValue(json, collectionType);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
            return null;
        }
    }
}
```
