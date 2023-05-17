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

```java
@Test
void test() throws IOException, InterruptedException {
    // 安装浏览器驱动
    WebDriverManager.edgedriver().setup();

    EdgeOptions options = new EdgeOptions();
    // 设置后台静默模式启动浏览器
    options.addArguments("--headless");
    options.addArguments("--disable-gpu");
    // 创建driver对象
    WebDriver driver = new EdgeDriver(options);
    // 访问url地址
    driver.get("https://www.baidu.com");

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
    // 等待页面加载完成
    Thread.sleep(5000);
    // 设置窗口大小
    driver.manage().window().setSize(new Dimension(width.intValue(), height.intValue()));

    // 生成快照
    File scrFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
    FileUtils.copyFile(scrFile, new File("./image.png"));

    // 关闭驱动
    driver.quit();
}
```
