```java
public class ListUtil {
    public static <T, U extends Comparable<? super U>> List<T> distinct(List<T> sourceList, Function<? super T, ? extends U>... keyExtractor) {
        if (sourceList == null) {
            return new ArrayList<>();
        }
        if (keyExtractor.length == 0) {
            return sourceList;
        }
        Comparator<T> comparator = Comparator.comparing(keyExtractor[0]);
        for (int i = 1; i < keyExtractor.length; i++) {
            comparator = comparator.thenComparing(keyExtractor[i]);
        }
        Set<T> set = new TreeSet<>(comparator);
        set.addAll(sourceList);
        return new ArrayList(set);
    }
}
```
