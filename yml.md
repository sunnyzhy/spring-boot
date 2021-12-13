# 读取配置文件

## 配置项

```yml
entity:
  object: # 简单对象
    student: # Student
      name: aa
      age: 20
  list: # List 集合
    string: # List<String>
      - a
      - b
      - c
    students: # List<Student>
      - name: aa
        age: 20
      - name: bb
        age: 21
  map: # Map 集合
    string: # Map<String,String>
      name: aa
      age: 20
    students: # Map<String,Student>
      student1:
        name: aa
        age: 20
      student2:
        name: bb
        age: 21
```

```java
@Data
public class Student {
    private String name;
    private Integer age;
}
```

## 简单对象

### 示例 1

```java
@ConfigurationProperties(prefix = "entity.object")
@Component
@Data
public class EntityBean {
    public Student student;
}
```

用法:

```java
@Autowired
private EntityBean entity;

System.out.println(entity.student);
```

### 示例 2

```java
@Configuration
public class EntityBean {
    @Bean
    @ConfigurationProperties(prefix = "entity.object.student")
    public Student student(){
        return new Student();
    }
}
```

用法:

```java
@Autowired
private Student student;

System.out.println(student);
```

## List 集合

### 示例

```java
@ConfigurationProperties(prefix = "entity.list")
@Component
@Data
public class ListBean {
    public List<String> string;
    public List<Student> students;
}
```

用法:

```java
@Autowired
private ListBean list;

System.out.println(list.string);
System.out.println(list.students);
```

## Map 集合

### 示例

```java
@ConfigurationProperties(prefix = "entity.map")
@Component
@Data
public class MapBean {
    public Map<String,String> string;
    public Map<String,Student> students;
}
```

用法:

```java
@Autowired
private MapBean map;

System.out.println(map.string);
System.out.println(map.students);
```
