# queue 批量获取且不阻塞
```java
private BlockingQueue<T> queue = new LinkedBlockingQueue<>();
private int maxElements = 1000;

@Async
public void produce() {
    queue.put(T);
}

@Async
public void consume() {
    while (true) {
        List<T> list = new ArrayList<>();
        T one = queue.take();
        list.add(one);
        queue.drainTo(list, maxElements - 1);
    }
}
```
