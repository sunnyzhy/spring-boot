# 自定义starter

## 创建一个maven工程（maven-archetype-quickstart）

- Spring官方Starter通常命名为spring-boot-starter-{name}如 spring-boot-starter-web

- Spring官方建议非官方Starter命名应遵循{name}-spring-boot-starter的格式, 如mybatis-spring-boot-starter

## 删除启动类（包含main方法的类）

## 引入依赖
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

## 创建proterties类来保存application.properties配置文件信息
```java
@ConfigurationProperties(prefix = "spring.person")
@Data
public class PersonProperties {
    private String name;
    private int age;
    private String sex = "M";
}
```

## 创建业务类
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

## 创建AutoConfiguration
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
- @ConditionalOnClass：当类路径classpath下有指定的类的情况下进行自动配置

- @ConditionalOnMissingBean: 当容器(Spring Context)中没有指定Bean的情况下进行自动配置

- @ConditionalOnProperty(prefix = “example.service”, value = “enabled”, matchIfMissing = true): 当配置文件中example.service.enabled=true时进行自动配置，如果没有设置此值就默认使用matchIfMissing对应的值

- @ConditionalOnMissingBean: 当Spring Context中不存在该Bean时

- @ConditionalOnBean: 当容器(Spring Context)中有指定的Bean的条件下

- @ConditionalOnMissingClass: 当类路径下没有指定的类的条件下

- @ConditionalOnExpression: 基于SpEL表达式作为判断条件

- @ConditionalOnJava: 基于JVM版本作为判断条件

- @ConditionalOnJndi: 在JNDI存在的条件下查找指定的位置

- @ConditionalOnNotWebApplication: 当前项目不是Web项目的条件下

- @ConditionalOnWebApplication: 当前项目是Web项目的条件下

- @ConditionalOnResource: 类路径下是否有指定的资源

- @ConditionalOnSingleCandidate: 当指定的Bean在容器中只有一个，或者在有多个Bean的情况下，用来指定首选的Bean

## 新建 spring.factories 文件
新建文件src/main/resources/META-INF/spring.factories
```
org.springframework.boot.autoconfigure.EnableAutoConfiguration=com.zhy.PersonAutoConfiguration
```

## 打包mvn clean install

## 创建一个Spring Boot工程并引入依赖
```xml
<dependency>
  <groupId>com.zhy</groupId>
  <artifactId>helloworld-spring-boot-starter</artifactId>
  <version>1.0-SNAPSHOT</version>
</dependency>
```

## 配置application.yml
```
spring:
  person:
    name: abc
    age: 20
```

## 单元测试
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
