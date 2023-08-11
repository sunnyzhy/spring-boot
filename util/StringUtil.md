```java
public class StringUtil {
    /**
     * uuid
     * @return
     */
    public static String uuid() {
        String uuid = UUID.randomUUID().toString();
        return uuid;
    }

    /**
     * 不带分隔符的uuid
     * @return
     */
    public static String uuidTrim() {
        String uuidStr = uuid();
        uuidStr = uuidStr.replaceAll("-", "");
        return uuidStr;
    }

    /**
     * 去掉字符串首尾指定的字符
     * @param input
     * @param element
     * @return
     */
    public static String trim(String input, char element) {
        if (StringUtils.isEmpty(input)) {
            return input;
        }
        int i = -1;
        // 去掉首字符
        while ((i = input.indexOf(element)) == 0) {
            if (i == input.length() - 1) {
                break;
            }
            input = input.substring(i + 1);
        }
        // 去掉尾字符
        while ((i = input.lastIndexOf(element)) == input.length() - 1) {
            if (i == -1) {
                break;
            }
            input = input.substring(0, i);
        }
        return input;
    }

    /**
     * 分段读取字符串
     * @param input
     * @param partitionHandler
     */
    public static void partition(String input, PartitionHandler partitionHandler) {
        if (StringUtils.isEmpty(input)) {
            return;
        }
        int length = input.length();
        int size = 1024;
        int start = 0;
        int end = size;
        while (start < length) {
            if (end > length) {
                end = length;
            }
            String segment = input.substring(start, end);
            partitionHandler.execute(segment);
            start = end;
            end += size;
        }
    }

    /**
     * 字符串的分段处理接口
     */
    public interface PartitionHandler {
        void execute(String partition);
    }
}
```
