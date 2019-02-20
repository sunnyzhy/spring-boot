# 自定义starter

# 创建一个maven工程（maven-archetype-quickstart）

- Spring官方Starter通常命名为spring-boot-starter-{name}如 spring-boot-starter-web

- Spring官方建议非官方Starter命名应遵循{name}-spring-boot-starter的格式, 如mybatis-spring-boot-starter

# 引入依赖
```xml
  <groupId>com.zhy</groupId>
  <artifactId>helloworld-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
  
  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-autoconfigure</artifactId>
      <version>2.0.0.RELEASE</version>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-configuration-processor</artifactId>
      <version>2.0.0.RELEASE</version>
      <optional>true</optional>
    </dependency>
    <dependency>
      <groupId>org.projectlombok</groupId>
      <artifactId>lombok</artifactId>
      <version>1.18.6</version>
      <scope>provided</scope>
    </dependency>
  </dependencies>
```

# 创建proterties类来保存application.properties配置文件信息
```java
@ConfigurationProperties(prefix = "spring.person")
@Data
public class PersonProperties {
    private String name;
    private int age;
    private String sex = "M";
}
```

# 创建业务类
```java
public class PersonService {
    private PersonProperties properties;

    public PersonService(PersonProperties properties) {
        this.properties = properties;
    }

    public void sayHello(){
        System.out.println("Hello，my name is: " + properties.getName() + ", age is: " + properties.getAge() + "years"
                + ", sex is: " + properties.getSex());
    }
}
```

# 创建AutoConfiguration
```java
@Configuration
@EnableConfigurationProperties(PersonProperties.class)
@ConditionalOnClass(PersonService.class)
@ConditionalOnProperty(prefix = "spring.person", value = "enabled", matchIfMissing = true)
public class PersonAutoConfiguration {

    @Autowired
    private PersonProperties properties;

    @Bean
    @ConditionalOnMissingBean(PersonService.class)  // 当容器中没有指定Bean的情况下，自动配置PersonService类
    public PersonService personService(){
        PersonService personService = new PersonService(properties);
        return personService;
    }
}
```

# 新建 spring.factories 文件
新建文件src/main/resources/META-INF/spring.factories
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.zhy.PersonServiceAutoConfiguration
```

# 打包mvn clean install

# 创建一个Spring Boot工程并引入依赖
```xml
<dependency>
  <groupId>com.zhy</groupId>
  <artifactId>helloworld-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

# 配置application.yml
```
spring:
  person:
    name: abc
    age: 20
```

```java
@RunWith(SpringRunner.class)
@SpringBootTest
public class ComponentApplicationTests {

    @Autowired
    private PersonService personService;
    
    @Test
    public void contextLoads() {
        personService.sayHello();
    }
}
```
