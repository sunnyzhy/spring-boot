# FTP 传输文件

springboot 整合 FTP 客户端实现文件传输。

## 1. 添加依赖项

```xml
        <dependency>
            <groupId>commons-net</groupId>
            <artifactId>commons-net</artifactId>
        </dependency>

        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
        </dependency>

        <dependency>
            <groupId>cn.hutool</groupId>
            <artifactId>hutool-all</artifactId>
        </dependency>
```

## 2. FTP 配置信息

```yml
ftp:
    host: 192.168.204.107
    port: 21
    username: admin
    password: admin
    initialSize: 5
    encoding: UTF-8
    bufferSize: 8192
    isopen: true
    retryCount: 5
```

## 3. 加载 FTP 配置信息

```java
import lombok.Data;

import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "ftp")
public class FtpProperties {

    private String  host;
    private int  port;
    private String  username;
    private String  password;
    private Integer initialSize = 0;
    private String  encoding = "UTF-8";
    private Integer bufferSize = 4096;
    private Integer retryCount = 3;
    private int connectTimeout = 30000;
    private boolean passiveMode = false;
}
```

## 4. FtpClient 池工厂

```java
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import java.io.IOException;

@Slf4j
public class FtpClientFactory extends BasePooledObjectFactory<FTPClient> {
    private FtpProperties ftpProperties;

    public FtpClientFactory(FtpProperties ftpProperties) {
        this.ftpProperties = ftpProperties;
    }

    @Override
    public FTPClient create() throws Exception {
        final FTPClient ftpClient = new FTPClient();
        ftpClient.setControlEncoding(ftpProperties.getEncoding());
        ftpClient.setConnectTimeout(ftpProperties.getConnectTimeout());
        try {
            // 连接ftp服务器
            ftpClient.connect(ftpProperties.getHost(), ftpProperties.getPort());
            // 登录ftp服务器
            ftpClient.login(ftpProperties.getUsername(), ftpProperties.getPassword());
        } catch (IOException e) {
            throw e;
        }
        // 是否成功登录服务器
        final int replyCode = ftpClient.getReplyCode();
        if (!FTPReply.isPositiveCompletion(replyCode)) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
            throw new Exception(String.format("Login failed for user [%s], reply code is: [%d]",
                    ftpProperties.getUsername(), replyCode));
        }
        ftpClient.setBufferSize(ftpProperties.getBufferSize());
        //设置模式
        if (ftpProperties.isPassiveMode()) {
            ftpClient.enterLocalPassiveMode();
        }
        return ftpClient;
    }

    @Override
    public PooledObject<FTPClient> wrap(FTPClient obj) {
        return new DefaultPooledObject<>(obj);
    }

    /**
     * 销毁FtpClient对象
     */
    @Override
    public void destroyObject(PooledObject<FTPClient> ftpPooled) {
        if (ftpPooled == null) {
            return;
        }
        FTPClient ftpClient = ftpPooled.getObject();

        try {
            if (ftpClient.isConnected()) {
                ftpClient.logout();
            }
        } catch (IOException io) {
            log.error("ftp client logout failed...{}", io);
        } finally {
            try {
                ftpClient.disconnect();
            } catch (IOException io) {
                log.error("close ftp client failed...{}", io);
            }
        }
    }

    /**
     * 验证FtpClient对象是否还可用
     */
    @Override
    public boolean validateObject(PooledObject<FTPClient> ftpPooled) {
        try {
            FTPClient ftpClient = ftpPooled.getObject();
            return ftpClient.sendNoOp();
        } catch (IOException e) {
            log.error("Failed to validate client: {}", e);
        }
        return false;
    }
}
```

## 5. FtpClient 池

```java
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.pool2.BaseObjectPool;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@Slf4j
public class FtpClientPool extends BaseObjectPool<FTPClient> {
    private static final int DEFAULT_POOL_SIZE = 4;

    private final BlockingQueue<FTPClient> ftpBlockingQueue;
    private final FtpClientFactory ftpClientFactory;

    /**
     * 初始化连接池，需要注入一个工厂来提供FTPClient实例
     *
     * @param ftpClientFactory ftp工厂
     * @throws Exception
     */
    public FtpClientPool(FtpClientFactory ftpClientFactory) throws Exception {
        this(DEFAULT_POOL_SIZE, ftpClientFactory);
    }

    public FtpClientPool(int poolSize, FtpClientFactory factory) throws Exception {
        this.ftpClientFactory = factory;
        ftpBlockingQueue = new ArrayBlockingQueue<>(poolSize);
        initPool(poolSize);
    }

    /**
     * 初始化连接池，需要注入一个工厂来提供FTPClient实例
     *
     * @param maxPoolSize 最大连接数
     * @throws Exception
     */
    private void initPool(int maxPoolSize) throws Exception {
        for (int i = 0; i < maxPoolSize; i++) {
            // 往池中添加对象
            addObject();
        }
    }

    /**
     * 获取连接
     *
     * @return
     * @throws Exception
     */
    @Override
    public FTPClient borrowObject() throws Exception {
        FTPClient client = ftpBlockingQueue.take();
        if (ObjectUtils.isEmpty(client)) {
            client = ftpClientFactory.create();
            // 放入连接池
            returnObject(client);
            // 验证对象是否有效  这里通过实践验证 如果长时间不校验是否存活，则这里会报通道已断开等错误
        } else if (!ftpClientFactory.validateObject(ftpClientFactory.wrap(client))) {
            // 对无效的对象进行处理
            invalidateObject(client);
            // 创建新的对象
            client = ftpClientFactory.create();
            // 将新的对象放入连接池
            returnObject(client);
        }
        return client;
    }

    @Override
    public void returnObject(FTPClient client) {
        try {
            if (client != null && !ftpBlockingQueue.offer(client, 3, TimeUnit.SECONDS)) {
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(client));
            }
        } catch (InterruptedException e) {
            log.error("return ftp client interrupted ...{}", e);
        }
    }

    @Override
    public void invalidateObject(FTPClient client) {
        try {
            if (client.isConnected()) {
                client.logout();
            }
        } catch (IOException io) {
            log.error("ftp client logout failed...{}", io);
        } finally {
            try {
                client.disconnect();
            } catch (IOException io) {
                log.error("close ftp client failed...{}", io);
            }
            ftpBlockingQueue.remove(client);
        }
    }

    /**
     * 增加一个新的链接，超时失效
     */
    @Override
    public void addObject() throws Exception {
        // 插入对象到队列
        ftpBlockingQueue.offer(ftpClientFactory.create(), 3, TimeUnit.SECONDS);
    }

    /**
     * 关闭连接池
     */
    @Override
    public void close() {
        try {
            while (ftpBlockingQueue.iterator().hasNext()) {
                FTPClient client = ftpBlockingQueue.take();
                ftpClientFactory.destroyObject(ftpClientFactory.wrap(client));
            }
        } catch (Exception e) {
            log.error("close ftp client ftpBlockingQueue failed...{}", e);
        }
    }

    public BlockingQueue<FTPClient> getFtpBlockingQueue() {
        return ftpBlockingQueue;
    }
}
```

## 6. FtpClient 抽象类定义常用函数

```java
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;

import java.io.File;
import java.util.List;

public abstract class AbstractFtpClient {
    /**
     * 打开指定目录
     *
     * @param directory directory
     * @return 是否打开目录
     */
    public abstract boolean cd(String directory) throws Exception;

    /**
     * 打开上级目录
     *
     * @return 是否打开目录
     */
    public boolean toParent() throws Exception {
        return cd("..");
    }

    /**
     * 远程当前目录（工作目录）
     *
     * @return 远程当前目录
     */
    public abstract String pwd() throws Exception;

    /**
     * 在当前远程目录（工作目录）下创建新的目录
     *
     * @param dir 目录名
     * @return 是否创建成功
     */
    public abstract boolean mkdir(String dir) throws Exception;

    /**
     * 文件或目录是否存在
     *
     * @param path 目录
     * @return 是否存在
     */
    public boolean exist(String path) throws Exception {
        final String fileName = FileUtil.getName(path);
        final String dir = StrUtil.removeSuffix(path, fileName);
        final List<String> names = ls(dir);
        return containsIgnoreCase(names, fileName);
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历
     *
     * @param path 需要遍历的目录
     * @return 文件和目录列表
     */
    public abstract List<String> ls(String path) throws Exception;

    /**
     * 删除指定目录下的指定文件
     *
     * @param path 目录路径
     * @return 是否存在
     */
    public abstract boolean delFile(String path) throws Exception;

    /**
     * 删除文件夹及其文件夹下的所有文件
     *
     * @param dirPath 文件夹路径
     * @return boolean 是否删除成功
     */
    public abstract boolean delDir(String dirPath) throws Exception;

    /**
     * 创建指定文件夹及其父目录，从根目录开始创建，创建完成后回到默认的工作目录
     *
     * @param dir 文件夹路径，绝对路径
     */
    public void mkDirs(String dir) throws Exception {
        final String[] dirs = StrUtil.trim(dir).split("[\\\\/]+");

        final String now = pwd();
        if (dirs.length > 0 && StrUtil.isEmpty(dirs[0])) {
            //首位为空，表示以/开头
            this.cd(StrUtil.SLASH);
        }
        for (int i = 0; i < dirs.length; i++) {
            if (StrUtil.isNotEmpty(dirs[i])) {
                if (false == cd(dirs[i])) {
                    //目录不存在时创建
                    mkdir(dirs[i]);
                    cd(dirs[i]);
                }
            }
        }
        // 切换回工作目录
        cd(now);
    }

    /**
     * 将本地文件上传到目标服务器，目标文件名为destPath，若destPath为目录，则目标文件名将与srcFilePath文件名相同。覆盖模式
     *
     * @param srcFilePath 本地文件路径
     * @param destFile    目标文件
     * @return 是否成功
     */
    public abstract boolean upload(String srcFilePath, File destFile) throws Exception;

    /**
     * 下载文件
     *
     * @param path    文件路径
     * @param outFile 输出文件或目录
     */
    public abstract void download(String path, File outFile) throws Exception;

    /**
     * 是否包含指定字符串，忽略大小写
     *
     * @param names      文件或目录名列表
     * @param nameToFind 要查找的文件或目录名
     * @return 是否包含
     */
    private static boolean containsIgnoreCase(List<String> names, String nameToFind) {
        if (CollUtil.isEmpty(names)) {
            return false;
        }
        if (StrUtil.isEmpty(nameToFind)) {
            return false;
        }
        for (String name : names) {
            if (nameToFind.equalsIgnoreCase(name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 关闭连接
     */
    public abstract void close() throws Exception;
}
```

## 7. FtpClient 类实现常用函数

```java
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.springframework.util.Assert;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class FtpClientImpl extends AbstractFtpClient {
    private FTPClient client;
    private FtpClientPool ftpClientPool;

    public FtpClientImpl(FtpClientPool ftpClientPool, FTPClient client) {
        this.ftpClientPool = ftpClientPool;
        this.client = client;
    }

    public FtpClientImpl(FtpClientPool ftpClientPool) throws Exception {
        this.ftpClientPool = ftpClientPool;
        this.client = ftpClientPool.borrowObject();
    }

    /**
     * 执行完操作是否返回当前目录
     */
    private boolean backToPwd;

    /**
     * 设置执行完操作是否返回当前目录
     *
     * @param backToPwd 执行完操作是否返回当前目录
     * @return this
     */
    public FtpClientImpl setBackToPwd(boolean backToPwd) {
        this.backToPwd = backToPwd;
        return this;
    }

    /**
     * 改变目录
     *
     * @param directory 目录
     * @return 是否成功
     */
    @Override
    public boolean cd(String directory) throws Exception {
        if (StrUtil.isBlank(directory)) {
            return false;
        }

        boolean flag = false;
        try {
            flag = client.changeWorkingDirectory(directory);
            return flag;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 远程当前目录
     *
     * @return 远程当前目录
     */
    @Override
    public String pwd() throws Exception {
        try {
            return client.printWorkingDirectory();
        } catch (IOException e) {
            throw e;
        }
    }

    @Override
    public List<String> ls(String path) throws Exception {
        final FTPFile[] ftpFiles = lsFiles(path);

        final List<String> fileNames = new ArrayList<>();
        for (FTPFile ftpFile : ftpFiles) {
            fileNames.add(ftpFile.getName());
        }
        return fileNames;
    }

    /**
     * 遍历某个目录下所有文件和目录，不会递归遍历
     *
     * @param path 目录
     * @return 文件或目录列表
     */
    public FTPFile[] lsFiles(String path) throws Exception {
        String pwd = null;
        if (StrUtil.isNotBlank(path)) {
            pwd = pwd();
            cd(path);
        }

        try {
            FTPFile[] ftpFiles = this.client.listFiles();
            return ftpFiles;
        } catch (IOException e) {
            throw e;
        } finally {
            // 回到原目录
            cd(pwd);
        }
    }

    @Override
    public boolean mkdir(String dir) throws Exception {
        try {
            boolean flag = this.client.makeDirectory(dir);
            return flag;
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 判断ftp服务器文件是否存在
     *
     * @param path 文件路径
     * @return 是否存在
     */
    public boolean existFile(String path) throws Exception {
        FTPFile[] ftpFileArr = null;
        try {
            ftpFileArr = client.listFiles(path);
        } catch (IOException e) {
            throw e;
        }
        if (ArrayUtil.isNotEmpty(ftpFileArr)) {
            return true;
        }
        return false;
    }

    @Override
    public boolean delFile(String path) throws Exception {
        final String pwd = pwd();
        final String fileName = FileUtil.getName(path);
        final String dir = StrUtil.removeSuffix(path, fileName);
        cd(dir);
        boolean isSuccess = false;
        try {
            isSuccess = client.deleteFile(fileName);
        } catch (IOException e) {
            throw e;
        } finally {
            // 回到原目录
            cd(pwd);
        }
        return isSuccess;
    }

    @Override
    public boolean delDir(String dirPath) throws Exception {
        FTPFile[] dirs = null;
        try {
            dirs = client.listFiles(dirPath);
        } catch (IOException e) {
            throw e;
        }
        String name;
        String childPath;
        for (FTPFile ftpFile : dirs) {
            name = ftpFile.getName();
            childPath = StrUtil.format("{}/{}", dirPath, name);
            if (ftpFile.isDirectory()) {
                // 上级和本级目录除外
                if (false == name.equals(".") && false == name.equals("..")) {
                    delDir(childPath);
                }
            } else {
                delFile(childPath);
            }
        }

        // 删除空目录
        try {
            return this.client.removeDirectory(dirPath);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. path为null或""上传到当前路径
     * 2. path为相对路径则相对于当前路径的子路径
     * 3. path为绝对路径则上传到此路径
     * </pre>
     *
     * @param path 服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param file 文件
     * @return 是否上传成功
     */
    @Override
    public boolean upload(String path, File file) throws Exception {
        Assert.notNull(file, "file to upload is null !");
        return upload(path, file.getName(), file);
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. path为null或""上传到当前路径
     * 2. path为相对路径则相对于当前路径的子路径
     * 3. path为绝对路径则上传到此路径
     * </pre>
     *
     * @param file     文件
     * @param path     服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param fileName 自定义在服务端保存的文件名
     * @return 是否上传成功
     */
    public boolean upload(String path, String fileName, File file) throws Exception {
        try (InputStream in = FileUtil.getInputStream(file)) {
            return upload(path, fileName, in);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 上传文件到指定目录，可选：
     *
     * <pre>
     * 1. path为null或""上传到当前路径
     * 2. path为相对路径则相对于当前路径的子路径
     * 3. path为绝对路径则上传到此路径
     * </pre>
     *
     * @param path       服务端路径，可以为{@code null} 或者相对路径或绝对路径
     * @param fileName   文件名
     * @param fileStream 文件流
     * @return 是否上传成功
     */
    public boolean upload(String path, String fileName, InputStream fileStream) throws Exception {
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
        } catch (IOException e) {
            throw e;
        }

        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }

        if (StrUtil.isNotBlank(path)) {
            mkDirs(path);
            boolean isOk = cd(path);
            if (false == isOk) {
                return false;
            }
        }

        try {
            return client.storeFile(fileName, fileStream);
        } catch (IOException e) {
            throw e;
        } finally {
            if (this.backToPwd) {
                cd(pwd);
            }
        }
    }

    /**
     * 下载文件
     *
     * @param path    文件路径
     * @param outFile 输出文件或目录
     */
    @Override
    public void download(String path, File outFile) throws Exception {
        final String fileName = FileUtil.getName(path);
        final String dir = StrUtil.removeSuffix(path, fileName);
        download(dir, fileName, outFile);
    }

    /**
     * 下载文件
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param outFile  输出文件或目录
     */
    public void download(String path, String fileName, File outFile) throws Exception {
        if (outFile.isDirectory()) {
            outFile = new File(outFile, fileName);
        }
        if (false == outFile.exists()) {
            FileUtil.touch(outFile);
        }
        try (OutputStream out = FileUtil.getOutputStream(outFile)) {
            download(path, fileName, out);
        } catch (IOException e) {
            throw e;
        }
    }

    /**
     * 下载文件到输出流
     *
     * @param path     文件路径
     * @param fileName 文件名
     * @param out      输出位置
     */
    public void download(String path, String fileName, OutputStream out) throws Exception {
        String pwd = null;
        if (this.backToPwd) {
            pwd = pwd();
        }
        cd(path);
        try {
            client.setFileType(FTPClient.BINARY_FILE_TYPE);
            client.retrieveFile(fileName, out);
        } catch (IOException e) {
            throw e;
        } finally {
            if (backToPwd) {
                cd(pwd);
            }
        }
    }

    @Override
    public void close() {
        ftpClientPool.returnObject(client);
    }

    public void setClient(FTPClient client) {
        this.client = client;
    }
}
```

## 8. FtpClient 对外提供 API

```java
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.tomcat.util.collections.SynchronizedStack;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.InputStream;

@Slf4j
public class FtpClient {
    @Autowired
    FtpClientPool ftpClientPool;

    private SynchronizedStack<FtpClientImpl> ftpClientCache;

    public FtpClient() {
        this.ftpClientCache = new SynchronizedStack<>();
    }

    private FtpClientImpl getFtpClient() {
        FtpClientImpl ftpClient = null;
        try {
            ftpClient = ftpClientCache.pop();
            FTPClient client = ftpClientPool.borrowObject();
            if (ObjectUtil.isEmpty(ftpClient)) {
                ftpClient = new FtpClientImpl(ftpClientPool, client);
            } else {
                ftpClient.setClient(client);
            }
            //使用完需要还原到原来目录
            ftpClient.setBackToPwd(Boolean.TRUE);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return ftpClient;
    }

    public boolean upload(String path, String fileName, File file) {
        FtpClientImpl ftpClient = getFtpClient();
        if (ftpClient == null) {
            return false;
        }
        boolean value = false;
        try {
            value = ftpClient.upload(path, fileName, file);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ftpClient.close();
            ftpClientCache.push(ftpClient);
        }
        return value;
    }

    public boolean upload(String path, String fileName, InputStream fileStream) {
        FtpClientImpl ftpClient = getFtpClient();
        if (ftpClient == null) {
            return false;
        }
        boolean value = false;
        try {
            value = ftpClient.upload(path, fileName, fileStream);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ftpClient.close();
            ftpClientCache.push(ftpClient);
            IoUtil.close(fileStream);
        }
        return value;
    }

    public boolean delFile(String path) {
        FtpClientImpl ftpClient = getFtpClient();
        if (ftpClient == null) {
            return false;
        }
        boolean value = false;
        try {
            value = ftpClient.delFile(path);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        } finally {
            ftpClient.close();
            ftpClientCache.push(ftpClient);
        }
        return value;
    }
}
```

## 9. 心跳线程检测无效的 FTPClient 并从连接池里移除

```java
import com.saftop.configure.ApplicationConfig;
import com.saftop.constant.ApplicationStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.net.ftp.FTPClient;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PostConstruct;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

@Slf4j
public class FtpClientKeepAlive {

    private KeepAliveThread keepAliveThread;

    @Autowired
    private FtpClientPool ftpClientPool;

    private final String THREAD_NAME = "ftp-client-alive-thread";

    @PostConstruct
    public void init() {
        // 启动心跳检测线程
        if (keepAliveThread == null) {
            keepAliveThread = new KeepAliveThread();
            Thread thread = new Thread(keepAliveThread, THREAD_NAME);
            thread.start();
        }
    }

    class KeepAliveThread implements Runnable {
        @Override
        public void run() {
            FTPClient ftpClient = null;
            while (true) {
                try {
                    BlockingQueue<FTPClient> pool = ftpClientPool.getFtpBlockingQueue();
                    if (pool != null && pool.size() > 0) {
                        Iterator<FTPClient> it = pool.iterator();
                        while (it.hasNext()) {
                            ftpClient = it.next();
                            boolean result = ftpClient.sendNoOp();
                            if (!result) {
                                ftpClientPool.invalidateObject(ftpClient);
                            }
                        }
                    }
                } catch (Exception e) {
                    ftpClientPool.invalidateObject(ftpClient);
                    log.error(e.getMessage(), e);
                }
                // 每30s发送一次心跳
                try {
                    Thread.sleep(1000 * 30);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
            }

        }
    }
}
```

## 10. 自动装配

```java
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(FtpProperties.class)
@Slf4j
public class FtpConfig {
    @Autowired
    FtpProperties ftpProperties;

    /**
     * 客户端工厂
     *
     * @return
     */
    @Bean
    public FtpClientFactory ftpClientFactory() {
        return new FtpClientFactory(ftpProperties);
    }

    /**
     * 连接池
     *
     * @param ftpClientFactory
     * @return
     * @throws Exception
     */
    @Bean
    public FtpClientPool ftpClientPool(FtpClientFactory ftpClientFactory) throws Exception {
        return new FtpClientPool(ftpClientFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public FtpClient ftpClient() {
        return new FtpClient();
    }

    /**
     * 检测ftp是否在活着
     */
    @Bean
    @ConditionalOnBean(FtpClientPool.class)
    public FtpClientKeepAlive ftpClientKeepAlive() {
        return new FtpClientKeepAlive();
    }
}
```

## 11. 单元测试

```java
@Autowired
private FtpClient ftpClient;

@Test
void ftpTest() {
    boolean value = ftpClient.upload("/aa/bb", "cc.jpg", new File("D:\\file\\201632116262812100.jpg"));
    System.out.println(value);
    boolean value = ftpClient.delFile("/aa/bb/cc.jpg");
    System.out.println(value);
}
```

参考: ```https://blog.csdn.net/u011837804/article/details/107048606```
