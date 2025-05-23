```
@Api：用在类上，说明该类的作用

@ApiOperation：用在方法上，说明方法的作用

@ApiImplicitParams：用在方法上包含一组参数说明

@ApiImplicitParam：用在 @ApiImplicitParams 注解中，指定一个请求参数的各个方面
    paramType：参数放在哪个地方
        · header --> 请求参数的获取：@RequestHeader
        · query -->请求参数的获取：@RequestParam
        · path（用于restful接口）--> 请求参数的获取：@PathVariable
        · body -->请求参数的获取：@RequestBody
        · form（不常用）
    name：参数名
    dataType：参数类型
    required：参数是否必须传
    value：参数的意思
    defaultValue：参数的默认值
    
@ApiResponses：用于表示一组响应

@ApiResponse：用在@ApiResponses中，一般用于表达一个错误的响应信息
    code：数字，例如400
    message：信息，例如"请求参数没填好"
    response：抛出异常的类
    
@ApiModel：描述一个Model的信息（这种一般用在post创建的时候，使用@RequestBody这样的场景，请求参数无法使用@ApiImplicitParam注解进行描述的时候）

@ApiModelProperty：描述一个model的属性
```

Swagger 和 SpringDoc 注解的对应关系：

***GET 请求，如果使用对象接收 Query 参数，那么在对象参数前需使用 ```@ParameterObject``` 注解，如：***

```java
@GetMapping
@Operation(summary = "使用对象接收 Query 参数")
public Response getList(@ParameterObject EntityForm form)
```

|Swagger|SpringDoc|
|--|--|
|@Api|@Tag|
|@ApiIgnore|@Parameter(hidden = true) or @Operation(hidden=false) or @Hidden|
|@ApiImplicitParam|@Parameter|
|@ApiImplicitParams|@Parameters|
|@ApiModel|@Schema|
|@ApiModelProperty(hidden = true)|@Schema(acc essMode = READ_ONLY)|
|@ApiModelProperty|@Schema|
|@ApiOperation(value = "foo", notes = "bar")|@Operation(summary = "foo", description = "bar")|
|@ApiParam|@Parameter|
|@ApiResponse(code = 404, message = "foo")|@ApiResponse(responseCode = "404", description = "foo")|
