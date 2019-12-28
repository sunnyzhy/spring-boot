# dependencyManagement 标签与 dependencies 标签下 dependency 的区别

- 所有声明在 dependencies 里的依赖都会自动引入，并默认被所有的子项目继承

- dependencies 即使在子项目中不写该依赖项，那么子项目仍然会从父项目中继承该依赖项（全部继承）

- dependencyManagement 只是声明依赖的版本号，该依赖不会引入，因此子项目需要显示声明所需要引入的依赖，若不声明则不引入

- 子项目声明了依赖且未声明版本号和 scope，则会继承父项目的版本号和 scope，否则覆盖

``` xml
<!-- 只是声明依赖，并不引入 -->
<dependencyManagement>
  <dependencies>
    // ...
  </dependencies>
</dependencyManagement>

<!-- 声明并引入依赖 -->
<dependencies>
  // ...
</dependencies>
```
