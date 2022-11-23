# Pravega-Flink-Sample
Sample Program using Pravega and Flink to analyze data(NY Taxi Records), inspired by [Pravega Samples](https://github.com/pravega/pravega-samples) 
and @wuchong's excellent work [flink-sql-training](http://wuchong.me/blog/2019/08/20/flink-sql-training/)

本文假定您已具备基础的 SQL 知识。

## Demo 组件
- Pravega 0.10.1
- Flink 1.13.1
- Zeppelin 0.10.1

本sample 程序是基于 Docker 进行的，因此你只需要安装了 Docker 即可。不需要依赖 Java、Scala 环境、或是IDE。
> **_注意:_** 使用 Docker 启动上述组件，请保证 Docker 内存大于 4G (推荐 6G)。

## 数据集
本sample所使用数据来自 [NY Taxi Records](http://www.nyc.gov/html/tlc/html/about/trip_record_data.shtml).
可以从以下链接下载:
```
https://s3.amazonaws.com/nyc-tlc/trip+data/yellow_tripdata_2018-01.csv
https://s3.amazonaws.com/nyc-tlc/misc/taxi+_zone_lookup.csv
```

## 准备工作
- `docker-compose up -d`以启动docker-compose环境
- 打开`localhost:18080`, 进入Zeppelin界面, Zeppelin首页, Import the note `flink_sql_demo_notebook`
- 执行`%flink.ssql show tables;`, 查看 Flink UI: `localhost:18081`是否工作
- 进入`datagen`目录并执行`mvn install`，在Flink UI中提交之前所生成的jar包文件`datagen-0.1-jar-with-dependencies.jar`, 将数据注入Pravega

### 实例：出租车公司希望实时分析得到当前里程大于20mile的行程数最多的区域（zone）,从而完成智能调度车辆

```sql
CREATE TABLE TaxiRide1 (
    rideId INT,
    vendorId INT,
    pickupTime TIMESTAMP(3),
    dropOffTime TIMESTAMP(3),
    passengerCount INT,
    tripDistance FLOAT,
    startLocationId INT,
    destLocationId INT,
    startLocationBorough STRING,
    startLocationZone STRING,
    startLocationServiceZone STRING,
    destLocationBorough STRING,
    destLocationZone STRING,
    destLocationServiceZone STRING
) with (
    'connector' = 'pravega',
    'controller-uri' = 'tcp://pravega:9090',
    'scope' = 'taxi',
    'scan.execution.type' = 'streaming',
    'scan.reader-group.name' = 'long-distance',
    'scan.streams' = 'trip',
    'format' = 'json'
);

SELECT startLocationZone, COUNT(*) AS zoneCnt
FROM TaxiRide1
WHERE tripDistance > 20
GROUP BY startLocationZone;
```