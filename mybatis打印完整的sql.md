# mybatis 打印完整的 sql

## mybatis-config.xml 方式

### 自定义 sql 拦截器

```java
package com.zhy.configure;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * @author zhy
 * @date 2025/8/14 16:35
 */
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SqlInterceptor implements Interceptor {
    private boolean showSql;

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        if (!showSql) {
            return invocation.proceed();
        }
        
        // 获取SQL相关对象
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        BoundSql boundSql = ms.getBoundSql(parameter);
        Configuration config = ms.getConfiguration();

        // 生成带参数的完整SQL
        String fullSql = getFullSql(config, boundSql);
        // 打印SQL信息
        log.info("===>\nSQL ID: {}\nFull SQL: {}", ms.getId(), fullSql);

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 执行原始SQL操作
        Object result = invocation.proceed();
        // 计算执行时间
        long costTime = System.currentTimeMillis() - startTime;
        // 打印SQL信息
        log.info("===>\nExecute Time: {} ms", costTime);

        return result;
    }

    /**
     * 将SQL中的?替换为实际参数值
     */
    private String getFullSql(Configuration config, BoundSql boundSql) {
        String sql = boundSql.getSql().replaceAll("\\s+", " "); // 格式化SQL（去除多余空格）
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if (parameterMappings.isEmpty() || parameterObject == null) {
            return sql; // 无参数直接返回
        }

        // 处理参数
        TypeHandlerRegistry typeHandlerRegistry = config.getTypeHandlerRegistry();
        MetaObject metaObject = config.newMetaObject(parameterObject);

        for (ParameterMapping pm : parameterMappings) {
            String propertyName = pm.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                // 反射获取Bean属性, 通过Getter方法获取参数值
                Object value = metaObject.getValue(propertyName);
                sql = replacePlaceholder(sql, value);
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                // 检查动态SQL参数（如<if>标签中的附加参数）, 从boundSql中获取附加参数值
                Object value = boundSql.getAdditionalParameter(propertyName);
                sql = replacePlaceholder(sql, value);
            } else {
                // 当参数是基本类型或无法解析属性时, 直接返回参数值
                sql = replacePlaceholder(sql, parameterObject);
            }
        }

        return sql;
    }

    /**
     * 替换SQL中的第一个?为实际参数（处理特殊类型）
     */
    private String replacePlaceholder(String sql, Object value) {
        if (value == null) {
            return sql.replaceFirst("\\?", "NULL");
        }
        // 字符串类型添加单引号，特殊字符转义
        if (value instanceof String || value instanceof Character) {
            String strVal = value.toString();
            if (strVal.contains("$")) {
                strVal = strVal.replace("$", "\\$");
            }
            strVal = strVal.replace("'", "''");
            return sql.replaceFirst("\\?", "'" + strVal + "'");
        }
        // 日期类型格式化
        if (value instanceof Date) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return sql.replaceFirst("\\?", "'" + df.format(value) + "'");
        }
        // 其他类型直接转换为字符串
        return sql.replaceFirst("\\?", Matcher.quoteReplacement(value.toString()));
    }

    @Override
    public Object plugin(Object target) {
        // 包装目标对象，仅对Executor类型生效
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置文件传递参数（如是否开启打印）
        Object o = properties.get("print-sql");
        if (o != null) {
            try {
                this.showSql = Boolean.parseBoolean(o.toString());
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
```

### 注册自定义的拦截器

#### 在 resources 目录下创建 mybatis-config.xml

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <plugins>
        <plugin interceptor="com.zhy.configure.SqlInterceptor">
            <property name="print-sql" value="true"/>
        </plugin>
    </plugins>
</configuration>
```

#### 在 application.yml 中添加配置

```yml
mybatis:
    config-location: classpath:mybatis-config.xml
    mapper-locations: classpath:mapper/*.xml
    type-aliases-package: com.zhy.model
```

***注：只有通过 mybatis-config.xml 配置文件，setProperties 才会生效！***

## Spring Boot 自动注册

```java
package com.zhy.configure;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.regex.Matcher;

/**
 * @author zhy
 * @date 2025/8/14 16:35
 */
@ConditionalOnProperty(value = "mybatis.print-sql", havingValue = "true")
@Component
@Slf4j
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class})
})
public class SqlInterceptor implements Interceptor {
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        // 获取SQL相关对象
        MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
        Object parameter = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        BoundSql boundSql = ms.getBoundSql(parameter);
        Configuration config = ms.getConfiguration();

        // 生成带参数的完整SQL
        String fullSql = getFullSql(config, boundSql);

        // 记录开始时间
        long startTime = System.currentTimeMillis();
        // 执行原始SQL操作
        Object result = invocation.proceed();
        // 计算执行时间
        long costTime = System.currentTimeMillis() - startTime;

        // 打印SQL信息
        log.info("===>\nSQL ID: {}\nFull SQL: {}\nExecute Time: {} ms", ms.getId(), fullSql, costTime);

        return result;
    }

    /**
     * 将SQL中的?替换为实际参数值
     */
    private String getFullSql(Configuration config, BoundSql boundSql) {
        String sql = boundSql.getSql().replaceAll("\\s+", " "); // 格式化SQL（去除多余空格）
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();

        if (parameterMappings.isEmpty() || parameterObject == null) {
            return sql; // 无参数直接返回
        }

        // 处理参数
        TypeHandlerRegistry typeHandlerRegistry = config.getTypeHandlerRegistry();
        MetaObject metaObject = config.newMetaObject(parameterObject);

        for (ParameterMapping pm : parameterMappings) {
            String propertyName = pm.getProperty();
            if (metaObject.hasGetter(propertyName)) {
                // 反射获取Bean属性, 通过Getter方法获取参数值
                Object value = metaObject.getValue(propertyName);
                sql = replacePlaceholder(sql, value);
            } else if (boundSql.hasAdditionalParameter(propertyName)) {
                // 检查动态SQL参数（如<if>标签中的附加参数）, 从boundSql中获取附加参数值
                Object value = boundSql.getAdditionalParameter(propertyName);
                sql = replacePlaceholder(sql, value);
            } else {
                // 当参数是基本类型或无法解析属性时, 直接返回参数值
                sql = replacePlaceholder(sql, parameterObject);
            }
        }

        return sql;
    }

    /**
     * 替换SQL中的第一个?为实际参数（处理特殊类型）
     */
    private String replacePlaceholder(String sql, Object value) {
        if (value == null) {
            return sql.replaceFirst("\\?", "NULL");
        }
        // 字符串类型添加单引号，特殊字符转义
        if (value instanceof String || value instanceof Character) {
            String strVal = value.toString();
            if (strVal.contains("$")) {
                strVal = strVal.replace("$", "\\$");
            }
            strVal = strVal.replace("'", "''");
            return sql.replaceFirst("\\?", "'" + strVal + "'");
        }
        // 日期类型格式化
        if (value instanceof Date) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.DEFAULT, DateFormat.DEFAULT, Locale.CHINA);
            return sql.replaceFirst("\\?", "'" + df.format(value) + "'");
        }
        // 其他类型直接转换为字符串
        return sql.replaceFirst("\\?", Matcher.quoteReplacement(value.toString()));
    }

    @Override
    public Object plugin(Object target) {
        // 包装目标对象，仅对Executor类型生效
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // 可通过配置文件传递参数（如是否开启打印）
    }
}
```

在 application.yml 中添加配置:

```yml
mybatis:
    print-sql: true # true: 开启; false: 关闭; 配置项或值不存在: 关闭
```
