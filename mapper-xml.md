 # Mybatis中#与$的区别
- **#{}** 是预编译处理，MyBatis在处理 **#{}** 时，它会将sql中的 **#{}** 替换为 **?** ，然后调用 PreparedStatement 的 set 方法来赋值

- **${}** 是字符串替换， MyBatis在处理 **${}** 时，它会将sql中的 **${}** 替换为变量的值

注意：
1. 使用 **${}** 会导致sql注入

2. 使用 **#{}** 可以很大程度上防止sql注入

3. 如果参数是字段名、表名等，就需要使用 **${}** ，例如

```sql
select * from ${t_name};

select * from user order by ${f_age};
```
 
 # 参数的用法
  ## mapper.xml
  ``` xml
  <select id="select" parameterType="java.util.Map" resultMap="BaseResultMap">
    select * from user where id in
    <foreach item="item" index="index" collection="ids" open="("
             separator="," close=")">
      #{item}
    </foreach>
    <if test="type != null and type != ''">
      AND `type` = #{type}
    </if>
  </select>
  ```
  
  ## mapper
  ``` java
  public interface UserMapper extends Mapper<User> {
    List<User> select(@Param("type") Integer type, @Param("ids") List<Integer> ids);
  }
  ```
  
# sql标签用法
```xml
<mapper namespace="com.zhy.mapper.UserMapper">
  <resultMap id="BaseResultMap" type="com.zhy.model.User">
    <id column="id" jdbcType="INTEGER" property="id"/>
    <result column="name" jdbcType="VARCHAR" property="name"/>
    <result column="age" jdbcType="INTEGER" property="age"/>
    <result column="address" jdbcType="VARCHAR" property="address"/>
  </resultMap>
	
  <sql id="condition">
    name = #{name} AND age = #{age}
    <if test="address != null">
        AND address LIKE '${address}%'
    </if>
  </sql>

  <select id="selectUser" parameterType="com.zhy.model.sql.UserSqlCondition" resultMap="BaseResultMap">
    SELECT * FROM user WHERE id >= (SELECT id FROM user WHERE
    <include refid="condition"/>
    LIMIT #{pageIndex},1) AND
    <include refid="condition"/>
    LIMIT #{pageSize}
  </select>

  <select id="selectUserCount" parameterType="com.zhy.model.sql.UserSqlCondition" resultType="java.lang.Integer">
    SELECT COUNT(*) FROM user WHERE
    <include refid="condition"/>
  </select>
</mapper>
```

# 分页查询、count、批量添加
## model
```java
@Data
public class UserSqlCondition {
    /***
     * 临时表名称
     */
    private String tname;

    /***
     * 临时表名称
     */
    private String tname1;

    /**
     * 部门id列表
     */
    private List<Integer> ids;

    /**
     * 姓名
     */
    private String name;

    /**
     * 分页的页号
     */
    private Integer pageIndex;

    /**
     * 每页显示的记录数
     */
    private Integer pageSize;

    /**
     * 最大的计数
     */
    private Integer maxCount;
}
```

## mapper.xml
一个标签中执行多条sql语句，需要做以下操作：

1. 在数据库的连接参数中加上 **allowMultiQueries=true**

2. 在标签下将多条sql用 **;** 隔开

```
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.type=com.alibaba.druid.pool.DruidDataSource
spring.datasource.url=jdbc:mysql://127.0.0.1:3306/user?characterEncodeing=utf-8&useSSL=false&serverTimezone=GMT&allowMultiQueries=true
spring.datasource.username=root
spring.datasource.password=root
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE mapper PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN" "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="com.zhy.mapper.UserMapper">
  <resultMap id="BaseResultMap" type="com.zhy.model.User">
    <id column="id" jdbcType="INTEGER" property="id" />
    <result column="username" jdbcType="VARCHAR" property="username" />
    <result column="password" jdbcType="VARCHAR" property="password" />
    <result column="name" jdbcType="VARCHAR" property="name" />
    <result column="gender" jdbcType="CHAR" property="gender" />
    <result column="type" jdbcType="INTEGER" property="type" />
    <result column="status" jdbcType="INTEGER" property="status" />
    <result column="birthday" jdbcType="VARCHAR" property="birthday" />
    <result column="address" jdbcType="VARCHAR" property="address" />
  </resultMap>

  <!-- 分页查询，有关联表查询时，避免使用IN，而用JOIN，必要时可以借助临时表 -->
  <select id="selectUser" parameterType="com.zhy.model.sql.UserSqlCondition" resultMap="BaseResultMap">
      DROP TEMPORARY TABLE IF EXISTS ${tname};
      CREATE TEMPORARY TABLE ${tname} (department_id INT NOT NULL);
      INSERT INTO ${tname} VALUES
      <foreach collection="ids" item="item" separator=",">
          (#{item})
      </foreach>;
      DROP TEMPORARY TABLE IF EXISTS ${tname1};
      CREATE TEMPORARY TABLE ${tname1} (department_id INT NOT NULL);
      INSERT INTO ${tname1} VALUES
      <foreach collection="ids" item="item" separator=",">
          (#{item})
      </foreach>;
      SELECT t1.* FROM user t1
      INNER JOIN department_user t2 ON t1.id = t2.user_id
      RIGHT JOIN ${tname1} t3  ON t2.department_id = t3.department_id
      WHERE t1.id >= (SELECT t1.id FROM user t1
      INNER JOIN department_user t2 ON t1.id = t2.user_id
      RIGHT JOIN ${tname} t3 ON t2.department_id = t3.department_id
      WHERE 1 = 1
      <if test="name != null and name != ''">
          AND t1.name LIKE '${name}%'
      </if>
      LIMIT #{pageIndex},1)
      <if test="name != null and name != ''">
          AND t1.name LIKE '${name}%'
      </if>
      LIMIT #{pageSize};
      DROP TEMPORARY TABLE IF EXISTS ${tname};
      DROP TEMPORARY TABLE IF EXISTS ${tname1};
  </select>
  
  <!-- 当数据量很大的时候，统计所有记录非常耗时，变通的做法是只统计一个最大计数的总数 -->
  <select id="selectUserCount" parameterType="com.zhy.model.sql.UserSqlCondition" resultType="java.lang.Integer">
	  DROP TEMPORARY TABLE IF EXISTS ${tname};
	  CREATE TEMPORARY TABLE ${tname} (department_id INT NOT NULL);
	  INSERT INTO ${tname} VALUES
	  <foreach collection="ids" item="item" separator=",">
		  (#{item})
	  </foreach>;
	  SELECT COUNT(*) FROM (
	  SELECT t1.id FROM user t1
	  INNER JOIN department_user t2 ON t1.id = t2.user_id
	  RIGHT JOIN ${tname} t3  ON t2.department_id = t3.department_id
	  WHERE 1 = 1
	  <if test="name != null and name != ''">
		  AND t1.name LIKE '${name}%'
	  </if>
	  LIMIT #{maxCount}) t;
	  DROP TEMPORARY TABLE IF EXISTS ${tname};
  </select>
  
  <!-- 批量添加 -->
  <insert id="batchInsert">
	INSERT INTO user(`username`,`password`,`name`,`gender`,`type`,`status`,`birthday`,`address`)
	VALUES
	<foreach collection="list" item="item" separator=",">
	  (#{item.username},#{item.password},#{item.name},
	  #{item.gender},#{item.type},#{item.status},
	  #{item.birthday},#{address.name})
	</foreach>
  </insert>
</mapper>
```

## mapper
```java
public interface UserMapper extends Mapper<User> {
    List<User> selectUser(UserSqlCondition sqlCondition);

    Integer selectUserCount(UserSqlCondition sqlCondition);

    void batchInsert(@Param("list") List<User> list);
}
```

# 批量添加并返回主键id
1. 升级Mybatis到3.3.1及以上版本，官方在这个版本中加入了批量新增返回主键id的功能

2. <insert>标签中添加 useGeneratedKeys="true" keyProperty="id"
	
3. 在Dao中不能使用@Param注解

4. Mapper.xml中使用list变量（parameterType="java.util.List"）接受Dao中的参数集合

```java
void batchInsertReturnId(List<User> list);
```

```xml
  <insert id="batchInsertReturnId" parameterType="java.util.List" useGeneratedKeys="true" keyProperty="id">
	INSERT INTO user(`username`,`password`,`name`,`gender`,`type`,`status`,`birthday`,`address`)
	VALUES
	<foreach collection="list" item="item" separator=",">
	  (#{item.username},#{item.password},#{item.name},
	  #{item.gender},#{item.type},#{item.status},
	  #{item.birthday},#{address.name})
	</foreach>
  </insert>
```

# Mybatis对整型参数值等于0的判断
mapper的配置文件
``` xml
		<if test="statusType != null and statusType != '' ">
			AND status_flag = #{statusType}
		</if>
```

当 statusType 的值为 **0** 的时候，如果其数据类型为 **Integer**，则判断的结果为 **null** ；如果其数据类型为 **int**，则判断的结果为 **''**。

- **解决方法**

``` xml
		<if test="statusType != null and statusType != ''  or statusType == 0">
			AND status_flag = #{statusType}
		</if>
```
