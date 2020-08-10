# split
```java
@Test
public void split() {
	System.out.println(":ab:cd:ef::".split(":").length);    // 末尾分隔符全部忽略
	System.out.println(":ab:cd:ef::".split(":", -1).length);    // 不忽略任何一个分隔符
	System.out.println(StringUtils.split(":ab:cd:ef::", ":").length);   // 首尾的分隔符全部都忽略
	System.out.println(StringUtils.splitPreserveAllTokens(":ab:cd:ef::", ":").length);  // 不忽略任何一个分隔符
}
```

输出：

```
4
6
3
6
```

# padLeft
```java
private String padLeft(String text, Integer num) {
    Pattern pattern = Pattern.compile("%0?\\d*?d");
    Matcher matcher = pattern.matcher(text);
    if (matcher.find()) {
        String group = matcher.group(0);
        text = text.replace(group, String.format(group, num));
        while (matcher.find()) {
            group = matcher.group(0);
            text = text.replace(group, String.format(group, num));
        }
    }
    return text;
}

@Test
public void padLeft() {
    String text = padLeft("x%05dy%03d", 5);
    System.out.println(text);
    text = padLeft("x%5dy%3d", 5);
    System.out.println(text);
    text = padLeft("x%dy%d", 5);
    System.out.println(text);
}
```

输出：

```
x00005y005
x    5y  5
x5y5
```
