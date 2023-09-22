# 解析超大json文件

```java
private JSONArray readValue(File file, String nodeName) {
    JSONArray array = new JSONArray();
    try {
        JsonFactory jsonFactory = new MappingJsonFactory();
        JsonParser jsonParser = jsonFactory.createParser(file);
        JsonToken current = jsonParser.nextToken();
        if (current != JsonToken.START_OBJECT) {
            return array;
        }
        nodeName = nodeName.toLowerCase();
        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.getCurrentName();
            if (StringUtils.isEmpty(fieldName)) {
                jsonParser.skipChildren();
                continue;
            }
            fieldName = fieldName.toLowerCase();
            if (!nodeName.equals(fieldName)) {
                jsonParser.skipChildren();
                continue;
            }
            current = jsonParser.nextToken();
            if (current != JsonToken.START_ARRAY) {
                jsonParser.skipChildren();
                continue;
            }
            while (jsonParser.nextToken() != JsonToken.END_ARRAY) {
                JsonNode node = jsonParser.readValueAsTree();
                JSONObject item = JSONObject.parseObject(node.toString());
                array.add(item);
            }
        }
    } catch (IOException e) {
        log.error(e.getMessage(), e);
    }
    return array;
}
```
