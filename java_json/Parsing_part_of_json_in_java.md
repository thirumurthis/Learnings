## Parsing part of the JSON using java without mapping to POJO 

- When working with JSON objects in Java, there where scenarios where I need only a part of the JSON. Also don't create dedicated POJO's to map without deserializing.
- For example, say if we are using third party API's which returns JSON repsonse and if our requirement is to only use part of that JSON.

- Say, below is the sample Json from the third-party API, and we only need to check the status value.

```json
{
  "app" : {
          "name" : "demo-app",
          "version" : "1.0",
          "description" : "simple app demo"
          },
   "request" : {
        "status" : "ACCEPTED"
     },   
   "details" : [
          {"endpoint" : "http://domain.com/api/v1/user"},
          {"trackId" : "1234554321" },
   ]   
}
```
  - To fetch the status we can represent the Json path like `request.status`. 
     -  In JsonPath library, the path can be represented `$.request.status`
     -  In Fasterxml library, the path can be represented `/request/status`

- In this blog have demonstrated how to parse part of the JSON using two types java Json parsing library
   - 1. Jayway Jsonpath - [Documentation](https://github.com/json-path/JsonPath)
   - 2. Fasterxml Jackson - [Documentation](https://github.com/FasterXML/jackson)

### 1. Using `Jayway Jsonpath` library to fetch part of the json value provided the path till the key property

#### Add dependency in `pom.xml`

 - The Jayway jsonpath requires slf4j jars

```xml
    <dependency>
      <groupId>com.jayway.jsonpath</groupId>
      <artifactId>json-path</artifactId>
      <version>2.7.0</version>
    </dependency>
  <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-api</artifactId>
      <version>1.7.36</version>
    </dependency>
    <dependency>
      <groupId>org.slf4j</groupId>
      <artifactId>slf4j-simple</artifactId>
      <version>1.7.30</version>
    </dependency>
```

#### Java code using the jsonpath library to fetch part of the json

 - We specify the path part of the command, JsonPath.compile(<path/to/specific/key/in/json>).
 - Using the  JsonPath.compile().read(<input json as string>), will return the value of the json specified in the path.
 - Additionally, have used BiFunction<> Lambda functions as example usage for using above two points.

```java
package org.example;

import com.jayway.jsonpath.JsonPath;

import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

public class JaywayJsonPathBasedJSONParser {


    public static void main(String ... args){

        String inputJson = getJsonString();
        // define the path the $ - represents the root
        // path is represented with `.`
        JsonPath jsonPath = JsonPath.compile("$.store.bicycle.color");
        // parse json string for the path
        String bicycleColor = jsonPath.read(inputJson);
        // print the parsed string
        System.out.println(bicycleColor);

        // additionally below is a representation just parsing hte 
        // in this case the path returns List<> which is json Array 
        // the list of book will be returned as List of map
        String bookJsonPathExpression = "$.store.book";
        List<Map<String,Object>> result = fetchMapUsingPath.apply(inputJson,bookJsonPathExpression);

        result.forEach( (item)->{
            item.forEach((key,value)->{System.out.println(key+" : "+value);});
            System.out.println("--------------------------");
        });
    }

    //Below is a Function definition to convert 
    static BiFunction<String, String , List<Map<String,Object>>> fetchMapUsingPath = (jsonString, pathExpression)->{
        JsonPath jsonPath = JsonPath.compile(pathExpression);
        return jsonPath.read(jsonString);
    } ;

    /**
     * The method to return the simple json as string
     * @return
     */
    public static String getJsonString(){
        String inputJson = """
                {
                    "store": {
                        "book": [
                            {
                                "category": "sci-fi",
                                "author": "Douglas Adams",
                                "title": "The long dark tea-time of the soul",
                                "price": 14.25
                            },
                            {
                                "category": "fiction",
                                "author": "Stella Gibbons",
                                "title": "Cold comfort farm",
                                "isbn" : "978-1476783000",
                                "price": 13.79
                            }
                        ],
                        "bicycle": {
                            "color": "blue",
                            "price": 39.99
                        }
                    },
                    "expensive": 10
                }
                """;
        return inputJson;

    }
}
```

#### Output:

```
blue
category : sci-fi
author : Douglas Adams
title : The long dark tea-time of the soul
price : 14.25
-----------------
category : fiction
author : Stella Gibbons
title : Cold comfort farm
isbn : 978-1476783000
price : 13.79
-----------------
```

### 2. Using `Fasterxml Jackson` library to parse part of the json using the provided json key as path.

#### Add below dependency in `pom.xml` to load the fasterxml jackson library

```xml
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-core</artifactId>
      <version>2.13.4</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.13.4</version>
    </dependency>
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-annotations</artifactId>
      <version>2.13.4</version>
    </dependency>
```

#### Java code uses the jackson library to fetch the part of JSON

  - Using ObjectMapper read the complete json as Jsonnode
  - Defining a JsonPointer with path to the JSON specified properties
  - With the returned JsonNode, use the at(<pass the JsonPointer) method to read the value

```java
package org.example;

import com.fasterxml.jackson.core.JsonPointer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

public class JacksonBasedJSONParser {

    public static void main(String ... args) {

        // json input string
        String inputJson = getJsonString();
        // jackson ObjectMapper object
        ObjectMapper objectMapper = new ObjectMapper();
        
        //path represented using / to fetch the color
        JsonPointer bicycleColorPointer = JsonPointer.compile("/store/bicycle/color");
        
        //path represented to fetch book list
        JsonPointer bookJsonPathPointer = JsonPointer.compile("/store/book");
        
        try {
            JsonNode completeJsonNode = objectMapper.readValue(inputJson,JsonNode.class);
            String bicycleColor = completeJsonNode.at(bicycleColorPointer).toString();
            //print bicycelcolor 
            System.out.println(bicycleColor);
            List<Map<String,Object>> bookList = objectMapper.readValue(completeJsonNode.at(bookJsonPathPointer).toString(),new TypeReference<>(){});
            bookList.forEach( (item)->{
                item.forEach((key,value)->{
                    System.out.println(key+" : "+value);
                });
                System.out.println("------------------------");
            });
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This method returns the json string sample
     * @return
     */
    public static String getJsonString(){
        String inputJson = """
                {
                    "store": {
                        "book": [
                            {
                                "category": "sci-fi",
                                "author": "Douglas Adams",
                                "title": "The long dark tea-time of the soul",
                                "price": 14.25
                            },
                            {
                                "category": "fiction",
                                "author": "Stella Gibbons",
                                "title": "Cold comfort farm",
                                "isbn" : "978-1476783000",
                                "price": 13.79
                            }
                        ],
                        "bicycle": {
                            "color": "blue",
                            "price": 39.99
                        }
                    },
                    "expensive": 10
                }
                """;
        return inputJson;
    }
}
```

#### Output:

```
"blue"
category : sci-fi
author : Douglas Adams
title : The long dark tea-time of the soul
price : 14.25
------------------------
category : fiction
author : Stella Gibbons
title : Cold comfort farm
isbn : 978-1476783000
price : 13.79
------------------------
```
