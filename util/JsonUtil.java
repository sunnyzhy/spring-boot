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
      * 将字节数组转为具体类型的简单对象
      *
      * @param buffer
      * @param clazz
      * @param <T>
      * @return
      */
     public static <T> T toObject(byte[] buffer, Class<T> clazz) {
         T t = toObject(buffer, Charset.forName("utf-8"), clazz);
         return t;
     }
    
     /**
      * 将字节数组转为具体类型的简单对象
      * @param buffer
      * @param charset
      * @param clazz
      * @return
      * @param <T>
      */
     public static <T> T toObject(byte[] buffer, Charset charset, Class<T> clazz) {
         if (buffer == null || buffer.length == 0) {
             return null;
         }
         try {
             String s = new String(buffer, charset);
             return objectMapper.readValue(s, clazz);
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
            return new byte[]{};
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
     * 将Object转为成员是简单对象的List
     * 如果成员是复杂对象的请使用 T toObject(Object object, TypeReference<T> typeReference)
     *
     * @param object
     * @param clazz
     * @param <T>
     * @return
     */
     public static <T> List<T> toList(Object object, Class<T> clazz) {
         if (object == null) {
             return new ArrayList<>();
         }
         try {
             CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, clazz);
             return objectMapper.convertValue(object, listType);
         } catch (Exception e) {
             log.warn(e.getMessage(), e);
             return new ArrayList<>();
         }
     }
    
    /**
     * 将Object转为成员是简单对象的Collection
     * 如果成员是复杂对象的请使用 T toObject(Object object, TypeReference<T> typeReference)
     *
     * @param object
     * @param collectionClass
     * @param elementClass
     * @param <T>
     * @return
     */
     public static <T> Collection<T> toCollection(Object object, Class<? extends Collection> collectionClass, Class<T> elementClass) {
         if (object == null) {
             return new ArrayList<>();
         }
         try {
             CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(collectionClass, elementClass);
             return objectMapper.convertValue(object, listType);
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
     * 需要由内到外逐层创建完整的类
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
     * 需要由内到外逐层创建完整的类
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
}
