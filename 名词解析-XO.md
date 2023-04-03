# 名词解析 - 开发中的 VO、DTO、PO、BO、TO 等

## VO（View Object）视图对象

后端给前端

## DTO（Data Transfer Object） 数据传输对象

前端给后端

## PO（Persistent Object）持久对象

对应的就是数据库中的表结构，等同于Entity

## BO（Business Object）业务对象

BO 就是 PO 的组合

比如：PO 是一条交易记录，BO 是一个人全部的交易记录集合对象

## DO(Data Object / Domain Object)

DO 是在阿里巴巴的开发手册中定义，DO（Data Object）其实等同于 PO

另一种版本的 DO 是 DDD（Domain-Driven Design）领域驱动设计中的，DO（Domain Object），等同于上面的 BO

## TO（Transfer Object）

不同关系的应用程序之间传输的对象
