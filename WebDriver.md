# WebDriver

## 添加依赖

```xml
<properties>
    <selenium.version>4.9.1</selenium.version>
    <selenium-htmlunit.version>2.52.0</selenium-htmlunit.version>
    <httpclient5.version>5.2.1</httpclient5.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.apache.httpcomponents.client5</groupId>
        <artifactId>httpclient5</artifactId>
    </dependency>
    <dependency>
        <groupId>org.seleniumhq.selenium</groupId>
        <artifactId>selenium-java</artifactId>
    </dependency>
    <dependency>
        <groupId>io.github.bonigarcia</groupId>
        <artifactId>webdrivermanager</artifactId>
        <version>5.3.2</version>
    </dependency>
</dependencies>
```

***由于 ```spring-boot-dependencies``` 里已经集成了 ```selenium-java``` 和 ```httpclient5``` ，所以只需在 ```<properties>...</properties>``` 里指定版本号即可。***

## 示例代码

```selenium wait``` 官网： ```https://www.selenium.dev/documentation/webdriver/waits/```

```java
public class WebDriverUtil {
    /**
     *
     * @param url URL
     * @param timeout 浏览器页面加载的超时时间，单位是秒
     * @param interval 浏览器页面在加载的过程中，监测指定的html元素的时间间隔，单位是秒
     * @param id 浏览器页面在加载的过程中，监测指定的html元素(id)
     * @param name 浏览器页面在加载的过程中，监测指定的html元素(name)
     * @param className 浏览器页面在加载的过程中，监测指定的html元素(className)
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public static File browser(String url, long timeout, long interval, String id, String name, String className) throws InterruptedException, IOException {
        // 创建driver对象
        WebDriver driver = WebDriverHolder.driver;
        
        // 访问url地址
        driver.get(url);
        
        // 获取可滑动页面的高度
        JavascriptExecutor driver_js = ((JavascriptExecutor) driver);
        Long height = (Long) driver_js.executeScript("return document.body.parentNode.scrollHeight");
        Long width = (Long) driver_js.executeScript("return document.body.parentNode.scrollWidth");
        // 如果高度大于1080，就滚动触发动态加载
        if (height > 1080) {
            for (int i = 1; i < height / 1000 + 1; i++) {
                driver_js.executeScript("window.scrollTo(0," + i * 1000 + ")");
                Thread.sleep(200);
            }
        }
        
        // 设置窗口大小
        driver.manage().window().setSize(new Dimension(width.intValue(), height.intValue()));
        
        // 页面加载的超时配置
        Wait<WebDriver> wait = new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(timeout))
                .pollingEvery(Duration.ofSeconds(interval))
                .ignoring(NoSuchElementException.class);
        if (StringUtils.isNotEmpty(id)) {
            wait.until(x -> x.findElement(By.id(id)));
        } else if (StringUtils.isNotEmpty(name)) {
            wait.until(x -> x.findElement(By.name(name)));
        } else if (StringUtils.isNotEmpty(className)) {
            wait.until(x -> x.findElement(By.className(className)));
        }
        
        // 生成快照（存储于 Temp 目录）
        File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        
        // 把快照转存到当前目录
        File file = new File("./" + scrFile.getName());
        FileUtils.copyFile(scrFile, file);
        
        return file;
    }

    /**
     * 关闭驱动
     */
    public static void quit() {
        WebDriverHolder.driver.quit();
    }

    private static class WebDriverHolder {
        private static WebDriver driver;

        static {
            // 安装浏览器驱动
            WebDriverManager.edgedriver().setup();
            
            EdgeOptions options = new EdgeOptions();
            // 设置后台静默模式启动浏览器
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            driver = new EdgeDriver(options);
        }
    }
}

@Test
void browser() throws InterruptedException, IOException {
    File file = WebDriverUtil.browser("https://www.baidu.com", 30, 1, "wrapper", null, null);
    System.out.println(file);
}
```

## 浏览器和驱动

selenium 官网： ```https://www.selenium.dev/documentation/webdriver/troubleshooting/errors/driver_location/```

### 浏览器和驱动

|浏览器|驱动的下载地址|
|--|--|
|```Microsoft Edge```|```https://msedgewebdriverstorage.z22.web.core.windows.net/```|
|```Firefox```|```https://github.com/mozilla/geckodriver/releases```|
|```Google```|```http://chromedriver.storage.googleapis.com/index.html```|

### 存储目录

- ```Microsoft Edge```
    |操作系统|驱动的存储目录|快照的存储目录|
    |--|--|--|
    |linux|```xxx```|```xxx```|
    |windows|```C:\Program Files (x86)\Microsoft\Edge\Application```|```C:\Users\用记名\AppData\Local\Temp```|
- ```Firefox```
    |操作系统|驱动的存储目录|快照的存储目录|
    |--|--|--|
    |linux|```xxx```|```xxx```|
    |windows|```C:\Program Files\Mozilla Firefox```|```C:\Users\用记名\AppData\Local\Temp```|
- ```Google```
    |操作系统|驱动的存储目录|快照的存储目录|
    |--|--|--|
    |linux|```xxx```|```xxx```|
    |windows|```C:\Program Files\Google\Chrome\Application```|```C:\Users\用记名\AppData\Local\Temp```|
    
