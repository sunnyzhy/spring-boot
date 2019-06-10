# 前言

- Excel 2003及以下的版本，一张表最大支持65536行数据，256列；

- Excel 2007-2010版本，一张表最大支持1048576行数据，16384列；

```
如果数据量大于104w，就只能通过程序级分表操作的方式来实现。
```

# SXSSF
[POI SXSSF官网](http://poi.apache.org/apidocs/dev/org/apache/poi/xssf/streaming/SXSSFWorkbook.html "POI SXSSF官网")

大数据量可以使用 POI 的 SXSSF

