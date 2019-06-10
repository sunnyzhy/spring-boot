# 前言

- Excel 2003及以下的版本，一张表最大支持65536行数据，256列；

- Excel 2007-2010版本，一张表最大支持1048576行数据，16384列；

```
如果数据量大于104w，就只能通过程序级分表操作的方式来实现。
```

# SXSSF
[POI SXSSF官网](http://poi.apache.org/apidocs/dev/org/apache/poi/xssf/streaming/SXSSFWorkbook.html "POI SXSSF官网")

大数据量可以使用 POI 的 SXSSF

# 示例
## 添加maven依赖
```xml
		<dependency>
			<groupId>org.apache.poi</groupId>
			<artifactId>poi-ooxml</artifactId>
			<version>4.1.0</version>
		</dependency>
```

## 定义User类
```java
@Data
public class User {
    private int id;
    private String name;
    private String gender;
    private String address;
}
```

## 测试代码
```java
    private String name = "user";
    private String[] gender = {"male", "female"};
    private String address = "address";
    private Random random = new Random();

    /**
     * 导出含有一个表单的工作簿
     * @throws IOException
     */
    @Test
    public void export() throws IOException {
        List<User> userList = initUserList(10000);
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        createSheet(workbook, "user", userList);
        close(workbook);
    }

    /**
     * 导出含有多个表单的工作簿
     * @throws IOException
     */
    @Test
    public void exportBySheet() throws IOException {
        List<User> userList = initUserList(30201);
        int pageSize = 10000;
        int totalCount = userList.size();
        int pageCount = 0;
        int m = totalCount % pageSize;
        if (m > 0) {
            pageCount = totalCount / pageSize + 1;
        } else {
            pageCount = totalCount / pageSize;
        }
        List<User> subList = new ArrayList<>();
        SXSSFWorkbook workbook = new SXSSFWorkbook();
        for (int i = 1; i <= pageCount; i++) {
            if (m == 0) {
                subList.addAll(userList.subList((i - 1) * pageSize, pageSize * (i)));
            } else {
                if (i == pageCount) {
                    subList.addAll(userList.subList((i - 1) * pageSize, totalCount));
                } else {
                    subList.addAll(userList.subList((i - 1) * pageSize, pageSize * (i)));
                }
            }
            createSheet(workbook, "user" + i, subList);
            subList.clear();
        }
        close(workbook);
    }

    /**
     * 初始化数据源
     * @param lenth
     * @return
     */
    private List<User> initUserList(int lenth) {
        List<User> userList = new ArrayList<>();
        for (int i = 1; i <= lenth; i++) {
            User user = new User();
            user.setId(i);
            user.setName(name + i);
            user.setGender(gender[random.nextInt(gender.length)]);
            user.setAddress(address + i);
            userList.add(user);
        }
        return userList;
    }

    /**
     * 生成表单
     * @param workbook
     * @param sheetName
     * @param userList
     */
    private void createSheet(SXSSFWorkbook workbook, String sheetName, List<User> userList) {
        SXSSFSheet sheet = workbook.createSheet(sheetName);
        sheet.createFreezePane(0, 1, 0, 1);
        int rowNum = 0;
        SXSSFRow row0 = sheet.createRow(rowNum++);
        row0.createCell(0).setCellValue("ID");
        row0.createCell(1).setCellValue("姓名");
        row0.createCell(2).setCellValue("性别");
        row0.createCell(3).setCellValue("地址");
        for (User user : userList) {
            SXSSFRow row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(user.getId());
            row.createCell(1).setCellValue(user.getName());
            row.createCell(2).setCellValue(user.getGender());
            row.createCell(3).setCellValue(user.getAddress());
        }
        sheet.setColumnWidth(0, 10 * 256);
        sheet.setColumnWidth(1, 10 * 256);
        sheet.setColumnWidth(2, 10 * 256);
        sheet.setColumnWidth(3, 30 * 256);
    }

    /**
     * 关闭工作簿
     * @param workbook
     * @throws IOException
     */
    private void close(SXSSFWorkbook workbook) throws IOException {
        String filePath = "./user.xlsx";
        FileOutputStream fileOutputStream = new FileOutputStream(filePath);
        workbook.write(fileOutputStream);
        fileOutputStream.flush();
        fileOutputStream.close();
        workbook.dispose();
        workbook.close();
    }
```
