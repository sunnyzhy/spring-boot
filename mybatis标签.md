# mybatis 标签

## 内容标签

### namespace

Mapper 的映射文件中 namespace 必须为接口的全名。

```xml
<mapper namespace="com.zhy.mapper.TUserMapper">

</mapper>
```

### CRUD 标签

```xml
<!-- 查询 -->
<select id="" parameterType="" resultType=""></select>

<!-- 添加 -->
<insert id="" parameterType=""></insert>

<!-- 修改 -->
<update id="" parameterType=""></update>

<!-- 删除 -->
<delete id="" parameterType=""></delete>
```

## 动态 SQL 标签

### if 标签

```xml
  <select id="selectByName" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <if test="name != null and name != ''">
      AND `name` LIKE CONCAT('%', #{name}, '%')
    </if>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE 1 = 1 AND `name` LIKE CONCAT('%', 'a', '%')
```

### where 标签

<where> 可以自动处理第一个 AND

```xml
  <select id="selectByName" resultMap="BaseResultMap">
    SELECT * FROM t_user
    <where>
      <if test="name != null and name != ''">
        AND `name` LIKE "%"#{name}"%"
      </if>
    </where>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE `name` LIKE "%"'a'"%"
```

### sql 标签

<sql> 可以把重复的 sql 提取出来，使用时用 <include> 引用即可，最终达到 sql 重用的目的。

```xml
  <sql id="whereCondition">
    <if test="name != null and name != ''">
      AND `name` LIKE CONCAT('%', #{name}, '%')
    </if>
  </sql>
  
  <select id="selectByName" resultMap="BaseResultMap">
    SELECT * FROM t_user
    <where>
      <include refid="whereCondition"></include>
    </where>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE `name` LIKE CONCAT('%', 'a', '%')
```

selectByName 也可以写成:

```xml
  <select id="selectByName" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <include refid="whereCondition"></include>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE 1 = 1 AND `name` LIKE CONCAT('%', 'a', '%')
```

### foreach 标签

#### 参数是整型数组

```java
List<TUser> selectList(@Param("idList") List<Integer> idList);
```

##### IN

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <if test="idList != null and idList.size > 0">
      AND id IN
      <foreach collection="idList" item="id" open="(" close=")" separator=",">
        #{id}
      </foreach>
    </if>
  </select>
```

也可以写成:

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <if test="idList != null and idList.size > 0">
      <foreach collection="idList" item="id" open="AND id IN(" close=")" separator=",">
        #{id}
      </foreach>
    </if>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE 1 = 1 AND id IN ( 1 , 2 )
```

##### OR

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <if test="idList != null and idList.size > 0">
      AND
      <foreach collection="idList" item="id" open="(" close=")" separator="OR">
        id = #{id}
      </foreach>
    </if>
  </select>
```

也可以写成:

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user WHERE 1 = 1
    <if test="idList != null and idList.size > 0">
      <foreach collection="idList" item="id" open="AND (" close=")" separator="OR">
        id = #{id}
      </foreach>
    </if>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE 1 = 1 AND ( id = 1 OR id = 2 )
```

#### 参数是对象数组

```java
List<TUser> selectList(@Param("userList") List<TUser> userList);
```

##### IN

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user
    <where>
      <if test="userList != null and userList.size > 0">
        id IN
        <foreach collection="userList" item="user" open="(" close=")" separator=",">
          #{user.id}
        </foreach>
      </if>
    </where>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE id IN ( 1 , 2 )
```

##### OR

```xml
  <select id="selectList" resultMap="BaseResultMap">
    SELECT * FROM t_user
    <where>
      <if test="userList != null and userList.size > 0">
        <foreach collection="userList" item="user" open="(" close=")" separator="OR">
          id = #{user.id}
        </foreach>
      </if>
    </where>
  </select>
```

sql 语句:

```sql
SELECT * FROM t_user WHERE ( id = 1 OR id = 2 )
```

参考:

https://www.cnblogs.com/sh086/p/8375791.html
