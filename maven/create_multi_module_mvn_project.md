### Below is steps to create a multi module maven project using maven CLI.


#### Step 1:
  - First create a folder where the project to be created.

#### Step 2:
  - Make sure the maven is installed, and the env variables are set.
  - issue below command, to create the parent module.

```
> mvn archetype:generate -DgroupId=com.kafka.demo -DartifactId=kafka-parent
```

#### Step 3:
  - Navigate to the kafka-parent created part of the above command and edit the pom.xml file
  - Add the packaging tag with pom.xml

```
  <modelVersion>4.0.0</modelVersion>

  <groupId>com.kafka.demo</groupId>
  <artifactId>kafka-parent</artifactId>
  <version>1.0-SNAPSHOT</version>
  
  <!-- add below tag -->
  <packaging>pom</packaging>
  
  <name>kafka-parent</name>

  <url>http://www.example.com</url>

```

#### Step 4:
  - Since already navigated into the kafka-parent folder, we need to create the sub-modules using below command

```
> mvn archetype:generate -DgroupId=com.kafka.demo -DartifactId=kafka-ex1
```

  - Above command will create an sub module named kafka-ex1.

Refer the gRPC folder to see an spring example of multi-module maven project.