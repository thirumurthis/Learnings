
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

**`POST`** is the preferred method for creating a record while **`PUT`** is the preferred method for updating a record.
The **`PUT`** method is used here as an `upsert` (that is, an insert or update).
For demonstration reasons, the same code was executed multiple times and PUT will perform upsert.

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

### Search query string: `HTTP action : GET`

To run a search with a low-level client, in this case we can issue a GET request that will run against <index-name> index with the following URI: **`/<indexname>/_search.`**
  
Because the low-level API uses the Elasticsearch REST interface, we need to construct the REST query object like below, in this case it will look as 
```
 { "query" : {"query_string" : { "query": "<string-to-search>" } } }
```

##### Search using the id within documents
```
"{  "query": {  "terms": { "_id": [ "%s" ] }} }"
```

##### Search using the specific field within the documents
```
// if the document contains catalogItem and corresponding field
"{ "query": {  "match" : { "catalogItem.category_name" : "%s" } } }"
```

With reference to the `findCatalogItem` method:
After sending the request to ES, we will receive a result in a Response object, it  contains: 
   - return status 
   - entity that represents the JSON response

To get CatalogItem results, navigate the json structure to find the document and convert to the corresponding model (entity object).

We can use Kibana, postman, Rest Client (ARC) plugin to form the request.

##### Query to search using the specific field within the document

In `findCatalogItem ()` method use the search query as below:
```
{ "query" : { "match" : { "itemDescription" : "<string-to-search>" } } }
```

```java
  public void findCatalogItem(String text, RestClient client) {
	Request request = new Request("GET", 
	String.format("/%s/_search", "item_details_low_level"));
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

##### Direct url to fetch the document info using url document id
```js
http://localhost:9200/item_details_low_level/_doc/1
// This returns only the specific object as json object

//output:
{
_index: "item_details_low_level",  //Index-name in this case
_type: "_doc",
_id: "1",
_version: 2,
_seq_no: 2,
_primary_term: 1,
found: true,
_source: {
itemId: 1,  //document-id in this case
itemName: "product1",
itemDescription: "This is a product1"
}
}
```

### UPDATE the document: `HTTP action : POST`
   - Update the entire document
   - Update only the specific field
   
To update entire document:
```
Use the URI: /<index-name/_update/<document-id>
```
```
Use Json entity:
{ "doc" : "<the complete document info>" }
// in the below code, we are writing the value as json using jackson objectmapper
```
```java
public void updateCatalogItem(CatalogItem item1,RestClient client) {
	  Request request =  new Request("POST",String.format("/%s/_update/%d","item_details_low_level", 2));
	  
	  CatalogItem item= new CatalogItem();
	  item.setItemDescription("This is product2 - issued update");
	  item.setItemId(2);
	  item.setItemName("product2-update");
	  try {
	  ObjectMapper objectMapper = new ObjectMapper();
	    request.setJsonEntity("{ \"doc\" :" + objectMapper.writeValueAsString(item)+"}");
	      
	    Response response = client.performRequest(request);
	    //Below will print if the update response was success OK/200
	    System.out.println("update response: "+ response);
	  } catch (IOException ex) {
	    System.err.println("Could not post to ES "+ ex);
	  }    
}
```

To Update the specific field in the document:
```
To udpate specific field
{ doc : { itemDescription : "This is product2- updated content at field level" } }  -- will update the existing field

//This is used demostrated in the below code snippet
{ doc : { description : "This is product 2 - updated document and added field to doc } } -- will update the document with a new field 
```
```java
//invoke this below method from the main passing the description and the restclient reference

public void updateDescription(String desc, RestClient client) {
	Request request = new Request("POST", String.format("/%s/_update/%d", "items_detail_low_level",  2));
	try {
		request.setJsonEntity(String.format("{ \"doc\" : { \"description\" : \"%s\" }}",desc));
		Response response = client.performRequest(request);
		System.out.println("update response: "+ response);
	} catch (IOException ex) {
		System.out.println("Could not post to ES "+ ex);
	}
}
```
```js 
//output
{
_index: "items_detail_low_level",
_type: "_doc",
_id: "2",
_version: 5,
_seq_no: 6,
_primary_term: 1,
found: true,
_source: {
itemId: 2,
itemName: "product2-update",
itemDescription: "This is product2 - issued update", // we can also update this field description
description: "This is product 2 - updated document and added field to doc" // new field to the existing document id 2 is added
}
}
```

### DELETE the Document : `HTTP action : DELETE`
```
URI : http://localhost:9200/<index-name>/_doc/<document-id>
```
```java
public void deleteCatalogItem(Integer id,RestClient client) {
	  Request request = new Request("DELETE", String.format("/%s/_doc/%d", "items_detail_low_level",  id));
	  try {
	    Response response = client.performRequest(request);
	    System.out.println("delete response: "+ response);
	  } catch (IOException ex) {
	    System.err.println("Could not post to ES "+ex);
	  }
}
```

### Asynchronous call
To make an asynchronous call using the low-level client, use the `performRequestAsync` method instead of the `performRequest` method.
A response listener needs to be supplied for asynchronous call. 
The response listener needs to implement two methods: `onSuccess` and `onFailure`.

```java
public void createCatalogItemAsync(List<CatalogItem> items, RestClient client) {
		CountDownLatch latch = new CountDownLatch(items.size());
		ResponseListener listener = new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				latch.countDown();
			}
			@Override
			public void onFailure(Exception exception) {
				latch.countDown();
				System.out.println("Could not process ES request. "+ exception);
			}
		};

		items.stream().forEach(e-> {
			//HTTP actions  PUT and POST can be used to create documents to index, 
			// best practice to use POST but PUT can also be used
			Request request = new Request("PUT",String.format("/%s/_doc/%d","items_detail_low_level", e.getItemId()));
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				request.setJsonEntity(objectMapper.writeValueAsString(e));
				client.performRequestAsync(request, listener); // Listener passed within this method
			} catch (IOException ex) {
				System.err.println("Could not post to ES"+ ex);
			}
		});
		try {
			latch.await(); //wait for all the threads to finish
			System.out.println("Inserted all the records/documents to the index");
		} catch (InterruptedException e1) {
			System.out.println("interrupted."+e1);
		}
	}
```

### High-level REST API
   - High-level API is much easier to work with
   - This is build over the Low-level client
   - The dependencies needs to be updated with every release/version of ES.
   - Low-level API gives more control 
   
Maven dependencies to for High-level API
```xml
<dependency>
  <groupId>org.elasticsearch.client</groupId>
  <artifactId>elasticsearch-rest-high-level-client</artifactId>
  <version>7.4.2</version>
</dependency>
```

To create a document using high-level API, you need to use `IndexRequest` and initialize it with the name of the desired index.

Then set the ID on the request and add JSON as a source.

Calling the high-level client index API with the request synchronously will return the index response, which could then be used to see if a document was created or updated.

```java
//jackson 
public ObjectMapper getObjectMapper() {
		return new ObjectMapper();
	}
public void createCatalogItem(List<CatalogItem>items,RestHighLevelClient client) {
   items.stream().forEach(e-> {
	IndexRequest request = new IndexRequest("item_details_low_level");
	try {
		request.id(""+e.getItemId());
		request.source(getObjectMapper().writeValueAsString(e), XContentType.JSON);
		request.timeout(TimeValue.timeValueSeconds(10));
		IndexResponse indexResponse = client.index(request, RequestOptions.DEFAULT);
		if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
		  System.out.println("Document added to the ES index");
		} else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
		System.out.println("document updated "+indexResponse.getIndex()+" "+indexResponse.getVersion() );
		} 
	} catch (IOException ex) {
		ex.printStackTrace();
	}
   });
}
```

##### Search/find the document usign the SearchHits, we need to use `SimpleQueryStringBuilder`
Create a search request by passing an index and then use a search query builder to construct a full text search. 

The search response encapsulates the JSON navigation and allows to easy access to the resulting documents via the SearchHits array.

To search all indexes, create a SearchRequest without any parameters.

```java
public void findCatalogItem(String text, RestHighLevelClient client) {
	    try {
	        SearchRequest request = new SearchRequest("item_details_high_level"); 
	        SearchSourceBuilder scb = new SearchSourceBuilder();
	        SimpleQueryStringBuilder mcb = QueryBuilders.simpleQueryStringQuery(text);
	        scb.query(mcb); 
	        request.source(scb);
	         
	        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
	        SearchHits hits = response.getHits();
	        SearchHit[] searchHits = hits.getHits();
	        List<String> catalogItems =  Arrays.stream(searchHits).filter(Objects::nonNull)
	                  .map(e -> e.toString())
	                  .collect(Collectors.toList());
	        catalogItems.forEach(System.out::println);
	         
	    } catch (IOException ex) {
	        ex.printStackTrace();
	    }
	}
```
------
##### Parsing the response :
In order to parse the response, we can use the small logic.

Query ES using, /<index-name>/_search with "{ "query" : { "match" : { "itemDescription" : "%s" } } } " and substring with string **_\_source_** and  "}]}}".

[Reference Link](https://blogs.oracle.com/javamagazine/easy-searching-with-elasticsearch)

-------

# Query or Search for data in ES, using Kibana or REST API

### To query all the indices in the ES
```js
 GET _cat/indicies?v
```

### To query the Mapping info
```js
GET /<index-name-pattern>/_search?size=50&pretty=true
```

###  To Query the field (data type of text) matching the value
```js
GET /<index-name-pattern>/_search?size=50
{ 
 "query":{
    "bool" : {
        "must" : [ 
	 { "match" : {"severity" : "INFO" }}
	 ]
      }
  }
}

Note: the document has an field Severity and query fetches the records matching that value.
bool - here is used to include multiple field to fire match query

we have used must, other options are must_not, should.
```
### To Query and get only the specific field in the results
```js
GET /<index-name-pattern>/_search?size=50
{ 
 "_source" : ["severity","message"],
 "query":{
    "bool" : {
        "must" : [ 
	 { "match" : {"severity" : "INFO" }}
	 ]
      }
  }
}
```

##### Output of above query, note the `_source` contains only selected fields.
```js
{
  "took" : 4,
  "timed_out" : false,
  "_shards" : {
    "total" : 5,
    "successful" : 5,
    "skipped" : 0,
    "failed" : 0
  },
  "hits" : {
    "total" : 34,
    "max_score" : 0.026202373,
    "hits" : [
      {
        "_index" : "logstash-2020-01-03",
        "_type" : "container",
        "_id" : "12121212sdfsdfsd",
        "_score" : 0.026202373,
        "_source" : {
          "severity" : "INFO",
          "message" : "some content included in the index when creating this document. ...... "
        }
      },
      ...
      ...
```
