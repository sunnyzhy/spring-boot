# Maven-relativePath

## 1 parent.relativePath 引用

```xml
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.5.1</version>
        <relativePath/> <!-- lookup parent from repository -->
    </parent>
```

## 2 relativePath 定义

```xml
      <xs:element name="relativePath" minOccurs="0" type="xs:string" default="../pom.xml">
        <xs:annotation>
          <xs:documentation source="version">4.0.0</xs:documentation>
          <xs:documentation source="description">
            The relative path of the parent &lt;code&gt;pom.xml&lt;/code&gt; file within the check out.
            The default value is &lt;code&gt;../pom.xml&lt;/code&gt;.
            Maven looks for the parent pom first in the reactor of currently building projects, then in this location on
            the filesystem, then the local repository, and lastly in the remote repo.
            &lt;code&gt;relativePath&lt;/code&gt; allows you to select a different location,
            for example when your structure is flat, or deeper without an intermediate parent pom.
            However, the group ID, artifact ID and version are still required,
            and must match the file in the location given or it will revert to the repository for the POM.
            This feature is only for enhancing the development in a local checkout of that project.
          </xs:documentation>
        </xs:annotation>
      </xs:element>
```

The relative path of the parent <code>pom.xml</code> file within the check out.

检验父文件 pom.xml 的相对路径, 其流程:

1. The default value is <code>../pom.xml</code>.
   relativePath 的默认值是 ../pom.xml, 即上级目录中的 pom.xml
2. Maven looks for the parent pom first in the reactor of currently building projects
   读取配置项 parent, 从父模块里查找 pom.xml
3. then in this location on the filesystem
   读取 <relativePath > 标签里配置的路径里查找 pom.xml
   如果是空值，就取默认值 ../pom.xml, 即从上级目录中查找
4. then the local repository
   从本地仓库中查找
5. and lastly in the remote repo.
   从远程仓库中查找
