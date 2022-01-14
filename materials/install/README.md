# Prepare environment (Pravega 0.10 + Flink 1.13 with Scala 2.12 + Pravega Flink Connector r0.10)

# Download Packages
>  
>  wget https://github.com/pravega/pravega/releases/download/v0.10.1/pravega-0.10.1.tgz  
> wget https://dlcdn.apache.org/flink/flink-1.13.3/flink-1.13.3-bin-scala_2.12.tgz  
> wget https://repo1.maven.org/maven2/io/pravega/pravega-connectors-flink-1.13_2.12/0.10.1/pravega-connectors-flink-1.13_2.12-0.10.1.jar  
> wget https://artifacts.opensearch.org/releases/bundle/opensearch/1.0.0/opensearch-1.0.0-linux-x64.tar.gz  
> wget https://artifacts.opensearch.org/releases/bundle/opensearch-dashboards/1.0.0/opensearch-dashboards-1.0.0-linux-x64.tar.gz  
>  
> tar xzvf pravega-0.10.1.tgz  
> tar xzvf pravega-connectors-flink-1.13_2.12-0.10.1.jar  
> tar xzvf opensearch-1.0.0-linux-x64.tar.gz  
> tar xzvf opensearch-dashboards-1.0.0-linux-x64.tar.gz  

# Start services

## Pravega
Start Standalone
> cd /home/ur/Course/materials/install/pravega-0.10.1/ 
> ./bin/pravega-standalone 

Verify service works
> ./bin/pravega-cli  
> scope create test  
> stream create test/test  
> stream append test/test 4  
> stream read test/test  
> <Ctl+C>  

## Flink
Start Standalone
> cd /home/ur/Course/materials/install/flink-1.13.3  
> ./bin/start-cluster.sh  

Verify service works
> open http://127.0.0.1:8081/ on guest and http://127.0.0.1:18081/ on host  
