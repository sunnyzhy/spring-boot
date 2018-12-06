  # 多参数&批量查询
  ## mapper.xml
  ``` xml
  <select id="selectByBatch" parameterType="java.util.Map" resultMap="BaseResultMap">
    select * from t_tablename where id in
    <foreach item="item" index="index" collection="ids" open="("
             separator="," close=")">
      #{item}
    </foreach>
    <if test="typeId != null and typeId != ''">
      AND type_id = #{typeId}
    </if>
  </select>
  ```
  
  ## mapper
  ``` java
  public interface DemoMapper extends Mapper<model_name> {
    List<model_name> selectByBatch(@Param("typeId") Integer typeId, @Param("ids") List<Integer> ids);
  }
  ```
  
  ## service
  ``` java
  @Autowired
  private DemoMapper demoMapper;
  
  Integer typeId = 1;
  List<Integer> ids = new ArrayList<>();
  ids.add(1);
  ids.add(2);
  ids.add(3);
  demoMapper.selectByBatch(typeId, ids);
  ```

# Mybatis对整型参数值等于0的判断
mapper的配置文件
``` xml
		<if test="statusType != null and statusType != '' ">
			AND status_flag = #{statusType}
		</if>
```

- **问题原因**

当 statusType 的值为 **0** 的时候，如果其数据类型为 **Integer**，则判断的结果为 **null** ；如果其数据类型为 **int**，则判断的结果为 **''**。

- **解决方法**

``` xml
		<if test="statusType != null and statusType != ''  or statusType == 0">
			AND status_flag = #{statusType}
		</if>
```
