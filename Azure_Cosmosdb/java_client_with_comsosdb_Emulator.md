### 1. Install the emulator.
  - Note: After installation faced an issue since the 8081 port was sued by McAfee.
  - From commnad prompt, navigate to the Cosmosdb Emulator exe, and start the executable in command line passing argument `/port=8085`, default it will start the emulator.

### 2. When connecting with Java, with maven pom.xml, make sure to inclde `<properties> <maven.target.version>11...` and also the maven plugin.

### 3. Add the SLF4J dependencies and apache commons.

### 4. During client implementation in java faced certifcate authentication issue, followed the instruction provided in this [link](https://docs.microsoft.com/en-us/answers/questions/557456/cosmos-db-emulator-connection-fails-with-certifica.html)
   - Check documentation to set the certifcate locally, [Link](https://docs.microsoft.com/en-us/azure/cosmos-db/local-emulator-export-ssl-certificates)
      - From windows certificate manager select personal, serach for CosmosDbEmulatoreCertificate. Open and copy as file. (don't include private key)
      - Move the .cer file to diffrent location, this will be used to create keystore
      - use command `keytool -importcert -alias cosmosdbemu -file documentemulatorcert.cer -keystore cosmosemulator.keystore`, when prompted for password provide it.
      - Since we don't want the jacerts (java) to be updated with this keystore, we created a seprate keystore.
      - In order to include it witin the jvm conttext use `System.properties("javax.net.trustStore","/path/to/truststore");`

### 4. Note `id` in the document is mandatory, without an id in the document cosmosdb throws exception.

### 5. Use `container.close` without this the jvm will be running, simply the client session will be on.

#### Terminology:
  - Database
  - Container
  - Item (row)
     - Partition key

#### All the queries are executed via rest api call

#### IndexPolicy can also be defined, from java. By default, CosmosDb performs indexing.
```java

/*
 //Sample indexs list, which is needs to be indexed
 
 String indexes = new String[] {
    "modelId/*", // This is the partition key
		"/make/*", 
		"/company/*",
    }
*/
// Pass the indexs that is requrired for the document and create the indexpolicy
protected IndexingPolicy getIndexingPolicy(String[] indexes) {
		IndexingPolicy indexingPolicy = new IndexingPolicy();
		
		List<ExcludedPath> excludedPaths = new ArrayList<>();
		excludedPaths.add(new ExcludedPath("/*"));

		List<IncludedPath> includedPaths = new ArrayList<>();
		for(String ind : indexes) {
			includedPaths.add(new IncludedPath(ind));
		}
		indexingPolicy.setIncludedPaths(includedPaths);
		indexingPolicy.setExcludedPaths(excludedPaths);
		return indexingPolicy;
	}
```

##### The indexpolicy is set in the `CosmosContainerProperties`
```java
      CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName,partitionKeyOfContainer);
      containerProperties.setIndexingPolicy(indexingPolicy); //policy returned by above method
      containerProperties.setDefaultTimeToLiveInSeconds(24*10*3600); //ttl seconds 10 days
      // pass this part of the container creation
      
      CosmosDatabaseResponse res = client.createDatabaseIfNotExists(databaseName);
      CosmosDatabase database = client.getDatabase(res.getProperties().getId()); 
      CosmosContainerResponse containerResponse = database.createContainerIfNotExists(containerProperties); //passing the index policy
      CosmosContainer container = database.getContainer(containerResponse.getProperties().getId());
```

- Full java program
```java
package cosmosdbdemo;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosClient;
import com.azure.cosmos.CosmosClientBuilder;
import com.azure.cosmos.CosmosContainer;
import com.azure.cosmos.CosmosDatabase;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosDatabaseResponse;
import com.azure.cosmos.models.CosmosItemRequestOptions;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.FeedResponse;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.SqlParameter;
import com.azure.cosmos.models.SqlQuerySpec;


public class SimpleMainCosmoDemo {


	private CosmosClient client;

	private final String databaseName = "vehicle_db";
	private final String containerName = "cars";
	private final String partionKey = "/modelId";

	public static String MASTER_KEY =
			System.getProperty("ACCOUNT_KEY", 
					StringUtils.defaultString(StringUtils.trimToNull(
							System.getenv().get("ACCOUNT_KEY")),
							"**********************"));// ********* = token generated in the emulator, check the quick start of data explorer

	public static String HOST =
			System.getProperty("ACCOUNT_HOST",
					StringUtils.defaultString(StringUtils.trimToNull(
							System.getenv().get("ACCOUNT_HOST")),
							"https://localhost:8085"));
	private CosmosDatabase database;
	private CosmosContainer container;

	//protected static CosmosQueryRequestOptions defaultOptions = new CosmosQueryRequestOptions();
	//protected static CosmosItemRequestOptions defaultRequestOptions = new CosmosItemRequestOptions();


	public static void main(String[] args) {
		
		System.setProperty ("javax.net.ssl.trustStore", "C:\\user\\learn\\cosmosdb\\cert\\cosmosemulator.keystore");
		System.setProperty ("javax.net.ssl.trustStorePassword", "123456");

		boolean toRead = true;
		System.out.println("STARTED...");
		List<Car> cars = new ArrayList<>();
		SimpleMainCosmoDemo demo = new SimpleMainCosmoDemo();
		cars.add(demo.createCar("100","MD001", "Corola","Toyota", "2000"));
		cars.add(demo.createCar("101","MD002", "Ultima","Nissan", "1990"));
		cars.add(demo.createCar("102","MD004", "Sonata","Hyundai", "1995"));
		cars.add(demo.createCar("103","MD007", "Civic","Honda", "1997"));
		cars.add(demo.createCar("104","MD007", "Accord","Honda", "1991"));
		cars.add(demo.createCar("105","MD009", "Versa","Nissan", "2007"));
		cars.add(demo.createCar("106","MD010", "Impala","Ford", "1999"));

		try {
			demo.createClient();
			demo.validateDatabase();
			if(!toRead) {
			 demo.createItemOrRow(cars);
			}else {
			 System.out.println("Calling QueryItems () method");
			 demo.queryItems();
			 System.out.println("Calling QueryUsingSqlSpec () method");
			 demo.queryUsingSqlSpec("MD007", "Honda");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("COMPLETED...");
		demo.close();
	}
	
	private void close() {
		if(client !=null) {
			client.close();
		}
	}

	private Car createCar(String id, String modelId,String make, String company, String year) {
		Car car = new Car();
		car.setId(id);
		car.setModelId(modelId);
		car.setMake(make);
		car.setCompany(company);
		car.setYear(year);
		return car;
	}
	private void createItemOrRow(List<Car> Cars) throws Exception {
		double totalRequestCharge = 0;
		for (Car car : Cars) {

			//  <CreateItem>
			//  Create item using container that we created using sync client
			CosmosItemRequestOptions cosmosItemRequestOptions = new CosmosItemRequestOptions();
			CosmosItemResponse<Car> item = container.createItem(car, cosmosItemRequestOptions);
			//  </CreateItem>

			//  Get request charge and other properties like latency, and diagnostics strings, etc.
			System.out.println(String.format("Created item with request charge of %.2f within" +" duration %s",item.getRequestCharge(), item.getDuration()));
			totalRequestCharge += item.getRequestCharge();
		}
		System.out.println(String.format("Created %d items with total request " + "charge of %.2f", Cars.size(),totalRequestCharge));
	}

	   private void queryItems() {
		   CosmosQueryRequestOptions queryOptions = new CosmosQueryRequestOptions();

	        Iterator<FeedResponse<Car>> feedResponseIterator = container.queryItems(
	            "SELECT * FROM c WHERE c.modelId IN ('MD001', 'MD002', 'MD004')", queryOptions,Car.class).iterableByPage().iterator();

	        feedResponseIterator.forEachRemaining(cosmosItemPropertiesFeedResponse -> {
	            System.out.println("Got a page of query result with " +
	                cosmosItemPropertiesFeedResponse.getResults().size() + " items(s)"
	                + " and request charge of " + cosmosItemPropertiesFeedResponse.getRequestCharge());

	            System.out.println("Item Ids ");
               cosmosItemPropertiesFeedResponse
	                .getResults()
	                .stream()
	                .forEach(itm -> System.out.println(itm.toString()));
	        });
	        //  </QueryItems>
	    }
	   
	   public void queryUsingSqlSpec(String modelId, String company) {
		   
	    	CosmosQueryRequestOptions options = new CosmosQueryRequestOptions();
	        options.setPartitionKey(new PartitionKey(modelId));
	        List<SqlParameter> paramList = new ArrayList<SqlParameter>();
	        paramList.add(new SqlParameter("@company", company));
	        SqlQuerySpec querySpec = new SqlQuerySpec("SELECT * FROM c WHERE c.company = @company", paramList);
	        
	        Iterator<FeedResponse<Car>> iter = container.queryItems(querySpec, options,Car.class).iterableByPage().iterator();
	    	if(iter.hasNext()) {
	        	FeedResponse<Car> res = iter.next();
	        	List<Car> resultList = res.getResults();
	        	while(iter.hasNext()) {
	        		res = iter.next();
	            	resultList.addAll(res.getResults()); 
	            }
	        	resultList.stream().forEach(itm -> System.out.println(itm.toString()));
	    	}
	   }
	public void validateDatabase() {
		if(client != null) {
			CosmosDatabaseResponse res = client.createDatabaseIfNotExists(databaseName);
			database = client.getDatabase(res.getProperties().getId()); 
			CosmosContainerProperties containerProperties = new CosmosContainerProperties(containerName, partionKey);
			//Note: Cosmos db perform auto indexing based on the documents.
			// in case we need to override we can cuse beliw
			//containerProperties.setIndexingPolicy(indexPolicy);
			//containerProperties.setDefaultTimeToLiveInSeconds(10*24*3600); //10 days in seconds

			CosmosContainerResponse contRes = database.createContainerIfNotExists(containerProperties);
			container =  database.getContainer(contRes.getProperties().getId());
		}
	}

	public void createClient() throws Exception {

		ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();
		throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(10);//throttle request 
		throttlingRetryOptions.setMaxRetryWaitTime(Duration.ofSeconds(30));//wait to retry

		if(client == null) {
			System.out.println("Client blank, will create client with host and key as "+HOST+" - "+MASTER_KEY);
		}
		try {
			client = new CosmosClientBuilder().endpoint(HOST).key(MASTER_KEY)
					.throttlingRetryOptions(throttlingRetryOptions)
					.preferredRegions(Arrays.asList("WEST US"))
					.contentResponseOnWriteEnabled(true)
					.consistencyLevel(ConsistencyLevel.EVENTUAL)
					.buildClient(); // applying EVENTUAL consistency for less latency and higher performance
			if(client != null) {
				System.out.println("Client created");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new Exception(e);
		}
	}

	public void deleteContainer(String dbName, String containerName) {
		if (client != null) {
			CosmosDatabaseResponse res = client.createDatabaseIfNotExists(dbName);
			CosmosDatabase database = client.getDatabase(res.getProperties().getId()); 
			database.getContainer(containerName).delete();
		}

	}

}

class Car{
	private String id;
	private String modelId;
	private String make;
	private String company;
	private String year;
	
	public String toString() {
		return this.id+", "+this.company+", "+this.make+", "+this.year;
		
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getModelId() {
		return modelId;
	}

	public void setModelId(String modelId) {
		this.modelId = modelId;
	}

	public String getMake() {
		return make;
	}

	public void setMake(String make) {
		this.make = make;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getYear() {
		return year;
	}

	public void setYear(String year) {
		this.year = year;
	}
}

```

- pom.xml
```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<groupId>cosmosdbdemo</groupId>
	<artifactId>cosmosdbdemo</artifactId>
	<version>0.0.1-SNAPSHOT</version>

	<properties>
		<maven.compiler.target>11</maven.compiler.target>
		<maven.compiler.source>11</maven.compiler.source>
	</properties>

	<dependencies>
		<dependency>
			<groupId>com.azure</groupId>
			<artifactId>azure-cosmos</artifactId>
			<version>4.20.0</version>
			<!-- <exclusions> <exclusion> <groupId>com.fasterxml.jackson.core</groupId> 
				<artifactId>jackson-core</artifactId> </exclusion> <exclusion> <groupId>com.fasterxml.jackson.core</groupId> 
				<artifactId>jackson-databind</artifactId> </exclusion> <exclusion> <groupId>com.fasterxml.jackson.core</groupId> 
				<artifactId>jackson-annotations</artifactId> </exclusion> </exclusions> -->
		</dependency>
		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.9</version>
		</dependency>
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-api</artifactId>
			<version>1.7.32</version>
		</dependency>
		<dependency>
			<groupId>org.slf4j</groupId>
			<artifactId>slf4j-simple</artifactId>
			<version>1.7.32</version>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<version>3.8.0</version>
				<configuration>
					<release>11</release>
					<fork>true</fork>
					<executable>C:\Program Files\AdoptOpenJDK\jdk-11.0.10.9-hotspot\bin\javac</executable> <!-- I was getting no compiler error when using mvn install -->
          <!-- Above is not standard, a generic way to include the javac executable needs to be identified. -->
				</configuration>
			</plugin>
		</plugins>
	</build>


</project>
```
