# 删除文件夹
## FileUtil 类
```java
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;

/**
 * @author zhy
 * @date 2019/6/17 10:43
 **/
@Component
public class FileUtil {
    public void deleteDirectory(String fileName) throws IOException {
        File file = new File(fileName);
        FileUtils.deleteDirectory(file);
    }
}
```

## 单元测试
```java
    @Autowired
    private FileUtil fileUtil;

    @Test
    public void deletedeleteDirectory() throws IOException {
        fileUtil.deleteDirectory("D:\\xx\\yy");
    }
```

# 压缩 zip 文件
## 添加 maven 依赖
```xml
		<dependency>
			<groupId>net.lingala.zip4j</groupId>
			<artifactId>zip4j</artifactId>
			<version>1.3.3</version>
		</dependency>
```

## ZipCompressUtil 类
```java
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * @author zhy
 * @date 2019/6/17 10:43
 **/
@Component
public class ZipCompressUtil {
    public void zipFile(String sourceFileName, String targetFileName) throws ZipException {
        // 生成的压缩文件
        ZipFile zipFile = new ZipFile(targetFileName);
        ZipParameters parameters = new ZipParameters();
        // 压缩方式
        parameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
        // 压缩级别
        parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
        // 要打包的文件夹
        File sourceFile = new File(sourceFileName);
        if (sourceFile.isDirectory()) {
            zipFile.addFolder(sourceFile, parameters);
        } else {
            zipFile.addFile(sourceFile, parameters);
        }
    }
}
```

## 单元测试
```java
    @Autowired
    private ZipCompressUtil zipCompressUtil;

    @Test
    public void zipFolder() throws ZipException {
        zipCompressUtil.zipFile("D:\\xx\\yy", "D:\\xx\\yy.zip");
    }
    
    @Test
    public void zipFile() throws ZipException {
        zipCompressUtil.zipFile("D:\\xx\\yy.log", "D:\\xx\\yy.zip");
    }
```

# 在多线程中使用 MultipartFile 进行异步操作报错 --- 系统找不到指定的文件

前端传递过来的文件会存储到临时文件夹中：

```
C:\Users\xxx\AppData\Local\Temp\tomcat.6131519677783180826.8056\work\Tomcat\localhost\ROOT
```

但是子线程异步执行的时候，由于主线程结束，导致临时文件被清空，所以会报错： 

```
java.io.FileNotFoundException: C:\Users\xxx\AppData\Local\Temp\tomcat.6131519677783180826.8056\work\Tomcat\localhost\ROOT\upload_85d787c3_6037_4ea2_a7f9_54ac3a19b461_00000011.tmp (系统找不到指定的文件。)
```

需要转换为流来进行操作：

```java
@PostMapping("/import")
public String import(@RequestParam("file") MultipartFile file) throws IOException {
    fileService.doImportThread(file.getInputStream());
}
```


