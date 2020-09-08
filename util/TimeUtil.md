```java
public class TimeUtil {
    Integer day = 24 * 60 * 60 * 1000;
    Integer hour = 60 * 60 * 1000;
    Integer minute = 60 * 1000;
    Integer second = 1000;

    /**
     * 计算两个时间段之间相差 d天h时m分s秒S毫秒
     *
     * @param time
     * @return
     */
    public String diffTime(Long time) {
        if (time.toString().length() != 13) {
            return "";
        }
        Long currentTime = System.currentTimeMillis();
        Long diff = currentTime - time; // diff是当前时间戳减去参数时间戳的差，单位是毫秒
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(diff / day); // diff除以day，取整，得到天数
        stringBuilder.append("天 ");
        Long mod = diff % day; // diff除以day，取余，得到除天数以外的时、分、秒、毫秒的总和
        stringBuilder.append(mod / hour); // mod除以hour，取整，得到小时数
        stringBuilder.append("时 ");
        mod = mod % hour; // mod除以hour，取余，得到除天数、小时数以外的分、秒、毫秒的总和
        stringBuilder.append(mod / minute); // mod除以minute，取整，得到分钟数
        stringBuilder.append("分 ");
        mod = mod % minute; // mod除以minute，取余，得到除天数、小时数、分钟数以外的秒、毫秒的总和
        stringBuilder.append(mod / second); // mod除以second，取整，得到秒数
        stringBuilder.append("秒 ");
        mod = mod % second; // mod除以second，取余，得到毫秒数
        stringBuilder.append(mod); // 毫秒数
        stringBuilder.append("毫秒");
        return stringBuilder.toString();
    }
}
```
