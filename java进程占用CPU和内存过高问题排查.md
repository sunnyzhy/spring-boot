# java 进程占用 CPU 和内存过高问题排查

## linux

### java 进程占用 CPU 过高

使用 ```top``` 查看进程列表，找到 cpu 占用率最高的 java 进程，也可以通过 ```shift + p``` 进行排序:

```bash
# top
  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND                                          
 7691 root      25   5 3972448 639268   5068 S 107.3  2.0   3852:49 java                                             
 6660 root      25   5 4066472 661476   3052 S  33.9  2.0   1792:02 java                                             
 7391 root      25   5   21.5g   8.5g  32344 S   8.0 27.2   3064:59 java                                             
31842 root      25   5 4043832 651164   1828 S   7.6  2.0   1373:55 java                                             
32167 root      25   5 4276312 896072   1136 S   7.3  2.7   1685:34 java 
```

从查询结果中可知 ```7691``` 的 java 进程占用 cpu 最高。

使用 ```jstack``` 命令生成 Java 进程中所有线程的快照:

```bash
jstack -l 7691 >> /usr/local/7691.stack
```

使用 ```top -Hp pid``` 查看 java 进程中占用 cup 最高的线程，也可以通过 ```shift + p``` 进行排序:

```bash
# top -Hp  7691
  PID USER      PR  NI    VIRT    RES    SHR S %CPU %MEM     TIME+ COMMAND                                           
 7833 root      25   5 3972448 638740   5020 R 98.7  1.9   3611:11 java                                              
 7831 root      25   5 3972448 638740   5020 S  2.7  1.9  33:24.90 java                                              
 7703 root      25   5 3972448 638740   5020 S  0.7  1.9  12:51.73 java                                              
 7832 root      25   5 3972448 638740   5020 S  0.7  1.9  33:26.04 java                                              
 7265 root      25   5 3972448 638740   5020 S  0.7  1.9   0:04.86 java                                              
 7700 root      25   5 3972448 638740   5020 S  0.3  1.9  12:49.20 java                                              
 7701 root      25   5 3972448 638740   5020 S  0.3  1.9  12:52.61 java                                              
 7702 root      25   5 3972448 638740   5020 S  0.3  1.9  12:55.66 java                                              
 7704 root      25   5 3972448 638740   5020 S  0.3  1.9  10:45.85 java                                              
 7778 root      25   5 3972448 638740   5020 S  0.3  1.9   1:03.71 java    
```

从查询结果中可知 ```7833``` 的线程占用 cpu 最高，将十进制的 ```7833``` 转换为十六进制数:

```bash
# printf "%x\n" 7833
1e99
```

打开之前生成的 ```7691.stack``` 文件并在文件中搜索 ```1e99```:

```bash
# vim /usr/local/7691.stack
"XNIO-1 I/O-3" #74 prio=5 os_prio=0 tid=0x00007f9619283800 nid=0x1e99 runnable [0x00007f95f842a000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
        at sun.nio.ch.EPollArrayWrapper.poll(EPollArrayWrapper.java:269)
        at sun.nio.ch.EPollSelectorImpl.doSelect(EPollSelectorImpl.java:93)
        at sun.nio.ch.SelectorImpl.lockAndDoSelect(SelectorImpl.java:86)
        - locked <0x00000000f229fd58> (a sun.nio.ch.Util$3)
        - locked <0x00000000f229fd48> (a java.util.Collections$UnmodifiableSet)
        - locked <0x00000000f228ad60> (a sun.nio.ch.EPollSelectorImpl)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:97)
        at sun.nio.ch.SelectorImpl.select(SelectorImpl.java:101)
        at org.xnio.nio.WorkerThread.run(WorkerThread.java:532)

"XNIO-1 I/O-2" #73 prio=5 os_prio=0 tid=0x00007f9619282800 nid=0x1e98 runnable [0x00007f95f872d000]
   java.lang.Thread.State: RUNNABLE
        at sun.nio.ch.EPollArrayWrapper.epollWait(Native Method)
/1e99                       
```

根据定位到的问题，具体分析。

### java 进程占用内存过高

使用 ```top``` 查看进程列表，通过 ```shift + m``` 按内存占用率进行排序:

```bash
# top
  PID USER      PR  NI    VIRT    RES    SHR S  %CPU %MEM     TIME+ COMMAND
 7391 root      25   5   21.5g   8.5g  26184 S   5.6 27.2   3065:10 java                                             
23732 mysql     20   0 2986816   2.5g    580 S   0.3  7.9   1978:33 mysqld                                           
29193 root      25   5 7978168   1.7g    124 S   0.3  5.5  63:30.08 java                                             
19137 saftop    20   0 5211404   1.4g      0 S   2.7  4.6 619:19.17 java 
```

从查询结果中可知 ```7391``` 的 java 进程占用内存最高。

使用 ```jmap``` 命令生成堆转储快照:

```bash
jmap -dump:format=b,file=/usr/local/7391.hprof 7391
```

把 ```7391.hprof``` 文件下载到本地，然后使用 ```visualvm``` 或 ```MemoryAnalyzer``` 具体分析。

## windows

### java 进程占用 CPU 过高

打开 Windows 任务管理器，查看 ```进程``` 列表，找到 cpu 占用率最高的 java 进程。

从查询结果中可知 ```7691``` 的 java 进程占用 cpu 最高。

使用 ```jstack``` 命令生成 Java 进程中所有线程的快照:

```bash
jstack -l 7691 >> C:\7691.stack
```

使用 ```PsList``` 或 ```ProcessExplorer``` 查看 java 进程中占用 cup 最高的线程。

- ```PsList```
   - 下载: [PsList 官方网址](https://learn.microsoft.com/zh-cn/sysinternals/downloads/pslist 'PsList 官方网址')，下载完将其解压到 ```C:\Windows\System32``` 路径下即可使用。
   - 使用: 在 cmd 命令窗口中执行命令 ```pslist -dmx 7691``` 即可找出占用 cup 最高的线程。
- ```ProcessExplorer```
   - 下载: [ProcessExplorer 官方网址](https://learn.microsoft.com/zh-cn/sysinternals/downloads/process-explorer 'ProcessExplorer 官方网址')，下载完直接运行 ```procexp.exe``` 即可使用。
   - 使用: 找到 PID 为 7691 的进程，右键点击 ```Properties…``` 选项，再打开 ```Threads``` 列表即可找出占用 cup 最高的线程。

从查询结果中可知 ```7833``` 的线程占用 cpu 最高，将十进制的 ```7833``` 转换为十六进制数 ```1e99```

打开之前生成的 ```7691.stack``` 文件并在文件中搜索 ```1e99```，根据定位到的问题，具体分析。

### java 进程占用内存过高

打开 Windows 任务管理器，查看 ```进程``` 列表，找到内存占用率最高的 java 进程。

从查询结果中可知 ```7391``` 的 java 进程占用内存最高。

使用 ```jmap``` 命令生成堆转储快照:

```bash
jmap -dump:format=b,file=C:\7391.hprof 7391
```

然后使用 ```visualvm``` 或 ```MemoryAnalyzer``` 具体分析快照文件 ```C:\7391.hprof```。
