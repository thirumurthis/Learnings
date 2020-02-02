
##### Elastic Terminology with reference to Database.


| Elastic  |  Database relavent | Elastic Search descrption |
|---------|----------------|------------------|
| Data type| Data type | Applicable to document field. A type such as integer, text, keyword, date, range, geographical coordiantes, etc.|
| Document | Databae record | Json documents stored in Elastic search. Contains fields, and each field is of particular data type.|
| Type (*) | - | Deprecated in Es 7+. Previously, type was convinent way to group similar documents together.|
| Index  | Database table (preivously this was similar to Db, since type is deprecated. this is similar to **table**.| Index stores and manages documents of same type.|

**Node** - running instance of Elastic Search (ES).

**Cluster** - In Elastic Search single node is also a cluster. ES instance that search the same index.

**Shard/primary shard** - Part of an Index resides in one node of cluster.

**Replica/replica shard** - Addiational copy of shard stored on different node for high availablity.

##### ES Rest API to create index, and search documents
   - Low-Level Synchronous CRUD API
   - High-Level REST Client
   
#####  URL 
```
http://localhost:9200/<name-of-index>/_doc/<id-of-document>

Note: 
  _doc - indicates we are dealing with documents. (Before ES 7.0, we would have used type here.)
  
  Index name - needs to be in lowercase.
```

##### Sample Low-Level API code to create a document and query the index

To start we use the maven dependecies defined.
```
	<dependency>
			<groupId>org.elasticsearch.client</groupId>
			<artifactId>elasticsearch-rest-client</artifactId>
			<version>7.5.2</version>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-core</artifactId>
			<version>2.9.4</version>
		</dependency>
		<dependency>
			<groupId>com.fasterxml.jackson.core</groupId>
			<artifactId>jackson-databind</artifactId>
			<version>2.9.4</version>
		</dependency>
```

Use RestClient (thread-safe) to build the url. When you download the ES, navigate to run bin/eleasticsearch.bat. Once ES is started, use `http://localhost:9200` to see whether the instance is up.

```java
//Note: CatalogItem class created with id, name and description of item just an entity 
public static void main(String[] args) {
	try (RestClient restClient = RestClient.builder(new HttpHost("localhost",9200,"http")).build()){
			
        	EsCURDExample escurd = new EsCURDExample();
		List<CatalogItem> items = new ArrayList<CatalogItem>();
		CatalogItem catItem = new CatalogItem();
		catItem.setItemId(1);
		catItem.setItemName("product1");
		catItem.setItemDescription("This is a product1");
		items.add(catItem);
		catItem = new CatalogItem();
		catItem.setItemId(2);
		catItem.setItemName("product2");
		catItem.setItemDescription("This is a product2");
		items.add(catItem);
		//Creates and insert log
		escurd.createCatalogItem(items, restClient);
		
		//Search the data
		escurd.findCatalogItem("product1",restClient);		
	}
```      
```java      
//Method to create index and add documents
public void createCatalogItem(List<CatalogItem> items,RestClient client) {
	   items.stream().forEach(e-> {
	    Request request = new Request("PUT", String.format("/%s/_doc/%d","item_details_low_level",e.getItemId()));
	    try {
	    	ObjectMapper obj = new ObjectMapper(); //uses jackson jar
	        request.setJsonEntity(
	        obj.writeValueAsString(e));
	      
	        client.performRequest(request);
	      } catch (IOException ex) {
	        System.err.print(String.format("Could not post %s to ES - %s", e.toString(),ex.toString()");
	      }
	  });
	}
    }
```    
```java
// Method to search for the inserted document
// The search string is passed within the "text".

public void findCatalogItem(String text, RestClient client) {
   Request request = new Request("GET", String.format("/%s/_search", "item_details_low_level"));
   
   //Right now we used the query string like this.
   String SEARCH = "{ \"query\" : {\"query_string\" : { \"query\": \"%s\" } } }";	            
   request.setJsonEntity(String.format(SEARCH, text));
	 
   try {
	   Response response = client.performRequest(request);
	   if (response.getStatusLine().getStatusCode()==200) {
		   String responseBody = EntityUtils.toString(response.getEntity());
			  System.out.println(responseBody);
	       } 
	   } catch (IOException ex) {
	    	System.err.println(String.format("Could not post %s to ES",ex.toString());
	  }
}
```

Search query string:

To run a search with a low-level client, in this case we can issue a GET request that will run against <index-name> index with the following URI: **`/<indexname>/_search.`**
  
Because the low-level API uses the Elasticsearch REST interface, we need to construct the REST query object like below, in this case it will look as 
```
 { "query" : {"query_string" : { "query": "<string-to-search>" } } }
```
With reference to the `findCatalogItem` method:

After sending the request to ES, we will receive a result in a Response object, which  contains, 
   - return status 
   - entity that represents the JSON response

To get CatalogItem results, navigate the json structure to find the document and convert to the corresponding model (entity object).

We can use Kibana, postman, Rest Client (ARC) plugin to form the request.

##### query to check the corresponding field within the document

In `findCatalogItem ()` method use the search query as below:
```
{ "query" : { "match" : { "itemDescription" : "<string-to-search>" } } }
```

```java
  public void findCatalogItem(String text, RestClient client) {
	Request request = new Request("GET", 
	String.format("/%s/_search", "catalog_item_low_level"));
	String SEARCH = "{ \"query\" : { \"match\" : { \"itemDescription\" : \"%s\" } } }";
	request.setJsonEntity(String.format(SEARCH, text));
	try {
	  Response response = client.performRequest(request);
	  if (response.getStatusLine().getStatusCode()==200) {
	      String responseBody = EntityUtils.toString(response.getEntity());
	      System.out.println(responseBody);
	    } 
	 } catch (IOException ex) {
	   System.err.println("Could not post to ES"+ex.toString());
         }
   }
```
