# ThreadLocal

```java
ThreadLocal<String> localName = new ThreadLocal();

@Test
void thread() {
    for (int i = 0; i < 5; i++) {
        int finalI = i;
        new Thread(() -> {
            Thread thread = Thread.currentThread();
            thread.setName("Thread-" + finalI);
            String threadName = thread.getName();
            localName.set("aa" + finalI);
            String name = localName.get();
            System.out.println(threadName + " -> " + name);
            localName.remove();
        }).start();
    }
}
```

## ThreadLocal 与 Synchronized 的区别

ThreadLocal是与线程绑定的一个变量。

相同点：

- ThreadLocal 和 Synchonized 都用于解决多线程并发访问

区别：

- Synchronized 用于线程间的数据共享，而 ThreadLocal 则用于线程间的数据隔离
- Synchronized 是利用锁的机制，使变量或代码块在某一时该只能被一个线程访问，用于在多个线程间通信时能够获得数据共享；而 ThreadLocal 为每一个线程提供变量的副本，使得每个线程在某一时间访问到的并不是同一个对象，这样就隔离了多个线程对数据的数据共享
