# split
```java
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
