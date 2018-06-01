  # 多参数&批量查询
  ## mapper.xml
  ```
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
  ```
  public interface DemoMapper extends Mapper<model_name> {
    List<model_name> selectByBatch(@Param("typeId") Integer typeId, @Param("ids") List<Integer> ids);
  }
  ```
  
  ## service
  ```
  @Autowired
  private DemoMapper demoMapper;
  
  Integer typeId = 1;
  List<Integer> ids = new ArrayList<>();
  ids.add(1);
  ids.add(2);
  ids.add(3);
  demoMapper.selectByBatch(typeId, ids);
  ```
