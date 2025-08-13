# springboot 整合 elasticsearch 集群

## 前言
- **本示例用的是 elasticsearch7.1.1**
- **不推荐使用 Spring Data Elasticsearch( https://spring.io/projects/spring-data-elasticsearch )，即 maven 仓库里的 spring-boot-starter-data-elasticsearch。因为 Spring Data Elasticsearch 所支持的 elasticsearch 版本比 elasticsearch 官网的最新版本要低得多。在写这篇笔记的时候，Spring Data Elasticsearch 稳定版(3.2.0)只支持到 Elasticsearch 6.8.1。**

## pom.xml

```xml
<properties>
    <java.version>1.8</java.version>
    <elasticsearch.version>7.1.1</elasticsearch.version>
</properties>

<dependency>
    <groupId>org.elasticsearch</groupId>
    <artifactId>elasticsearch</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
<dependency>
    <groupId>org.elasticsearch.client</groupId>
    <artifactId>elasticsearch-rest-high-level-client</artifactId>
    <version>${elasticsearch.version}</version>
</dependency>
```

## application.yml

```
elasticsearch:
  hosts:
    - 192.168.0.10:9200
    - 192.168.0.11:9200
    - 192.168.0.12:9200
````

## ElasticsearchConfig 类

```java
@Component
@ConfigurationProperties(prefix = "elasticsearch")
@Data
public class ElasticsearchConfig {
    private List<String> hosts = new ArrayList<>();

    @Bean
    public RestHighLevelClient restHighLevelClient() {
        List<HttpHost> hostList = new ArrayList<>();
        for (String host : hosts) {
            String[] hostInfo = host.split(":");
            if (hostInfo.length != 2) {
                continue;
            }
            HttpHost httpHost = new HttpHost(hostInfo[0],
                    Integer.parseInt(hostInfo[1]),
                    "http");
            hostList.add(httpHost);
        }
        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(hostList.toArray(new HttpHost[hostList.size()])));
        return client;
    }
}
```

## 单元测试

### 示例代码

```java
@Autowired
private RestHighLevelClient restHighLevelClient;
private String index = "twitter";

@Test
public void search() throws IOException {
    BoolQueryBuilder boolQuery = new BoolQueryBuilder();
    boolQuery.filter(QueryBuilders.matchAllQuery());
    SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
    sourceBuilder.query(boolQuery);
    sourceBuilder.timeout(new TimeValue(20, TimeUnit.SECONDS));
    sourceBuilder.from(0);
    sourceBuilder.size(10);
    SearchRequest request = new SearchRequest(index);
    request.source(sourceBuilder);
    SearchResponse response = restHighLevelClient.search(request, RequestOptions.DEFAULT);
    System.out.println(response.getHits().getHits().length);
}
```
