## REST API with Javalin and JBang

In this blog we will create a REST API using Javalin and running it using JBang.

Pre-requisites:
 - JBang CLI installed

### What is Javalin?
 - Javalin is a very lightweight web framework for Java
 - Javalin comes with a Jetty embedded server
 - Very easy to build REST API

For more details refer the [documentation](https://javalin.io/documentation)

### JBang:
 - With JBang we can run Java class as a script.
 - With the JBang here there is no specific Java project structure created by Maven or Gradle.
 - Like Shell script she bang, we do have specify JBang in the Java file.
 - The dependency are provided at the top of the file within the java comment style.

### Java code
 - With the Javalin library we initally configure the app
 - In this class the example of GET and POST request is demonstrated
 - Also we read a json file and serve the data in JSON format

```java
///usr/bin/env jbang "$0" "$0" : exit $?

//DEPS io.javalin:javalin:5.5.0
//DEPS org.projectlombok:lombok:1.18.26
//DEPS org.slf4j:slf4j-simple:2.0.7
//DEPS commons-io:commons-io:2.11.0
//DEPS com.fasterxml.jackson.core:jackson-databind:2.15.1

import io.javalin.Javalin;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;

import org.apache.commons.io.IOExceptionList;
import org.apache.commons.io.IOUtils;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
public class RESTApi {

    static String fileName ="data.json";
    static List<Items> items = new ArrayList<>();
    
    public static void main(String[] args){
        loadItems();
        var app = Javalin.create(config -> {
            config.plugins
            .enableCors(cors -> cors.add(host -> host.allowHost("localhost")));
        })
        .get("/data",ctx -> ctx.json(readDataFromFile(fileName)))
        .get("/hello/{name}",ctx -> ctx.result("Welcome "+ctx.pathParam("name")))
        .get("/items",ctx-> ctx.json(getItems()))
        .post("/item", ctx -> {
                    ctx.status(200);
                    addItem(ctx.bodyAsClass(Items.class));})
        .start(7070);
    }

    private static Items addItem(Items item){
        log.info("input data {}",item);
        if(item != null){
            items.add(item);
        }
        return item;
    }

    private static List<Items> getItems(){
        return items;
    }

    private static List<Items> loadItems(){
        Items computer = new Items("Computer",2);
        Items keyBoard = new Items("KeyBoard",4);
        Items mouse = new Items("Mouse",4);
        items.add(computer);
        items.add(keyBoard);
        items.add(mouse);
        return items;        
    }

    public static String readDataFromFile(String fileName){
        Map<String, Object> valueMap = new HashMap<>();
        String data = null;
        try{
            ObjectMapper mapper = new ObjectMapper();
            valueMap = mapper.readValue(Paths.get(fileName).toFile(),
                                 new TypeReference<Map<String,Object>>(){});
            data = new ObjectMapper().writeValueAsString(valueMap);
        }catch(Exception ex){
            ex.printStackTrace();
        }
        return data;
    }
}

@Data
@NoArgsConstructor
@AllArgsConstructor
class Items{
    String product = "";
    int quantity =0;
}
```
### Sample data json file

```json
{
    "companyName" : "xyzABC",
    "address" : {
        "addressLine1": "Street",
        "addressLine2": "Apt Number",
        "city" : "City",
        "zipCode" : "123456"
    },
    "departments": [
        {"name":"Accounts",
         "employees":[
            {"name":"employee1","id":"1"},
            {"name":"employee2","id":"2"}
         ]
        }
    ] 
}
```

### Output

```
$ curl http://localhost:7070/hello/Javalin
Welcome Javalin
```

- List the loaded items
```
$ curl http://localhost:7070/items
[{"product":"Computer","quantity":2},{"product":"KeyBoard","quantity":4},{"product":"Mouse","quantity":4}]
```

- List of added item above

```
$ curl http://localhost:7070/items
[{"product":"Computer","quantity":2},{"product":"KeyBoard","quantity":4},{"product":"Mouse","quantity":4},{"product":"Phone","quantity":2}]
```

- Add item

```
$ curl -X POST -d '{"product":"Phone","quantity":"2"}' http://localhost:7070/item
```

- Javalin startup logs
![image](https://github.com/thirumurthis/Learnings/assets/6425536/8e486b66-50cf-4c0d-b437-725c9e798212)
