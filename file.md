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
