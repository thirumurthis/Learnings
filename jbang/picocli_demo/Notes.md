### The picocli usage with Jbang
-  Picocli is a one-file framework for creating Java command line applications. 
- In this demonstrated we using picocli framework to java code takes command line arguments and performs some logic.
- This blog will just demonstrate the usage of Picocli framework with Jbang, for more info refer the [picocli documentation](https://picocli.info/#_introduction).
- Basic understanding of Jbang will help to execut the code

#### Pre-requsites
- Jbang CLI installed 

#### Summary
- In code example we use Jbang to load the picocli dependency and use that framework to build the simple java application that takes command line arguments.
- When executing the java application a parameter needs to be passed which is considered as username it can be any random string.
- The application also takes an optional argument `-r` or `--random-message`, when this argument is passed the code will fetch some random message from internet.
- The application also takes an optional password argument `-p` or `--password` which is set to be interactive, that is no need to pass the password upon executing the code it will be prompting frome the user.
- The `-h` or `--help` argument will display the usage of the java application.
- The Jaway json path dependency used to extract the information from the API.


#### Code

 - The `demoappcli.java` is the entry point for the Jbang, for the `-r` option to connect to the API in the internet the code is placed under `support/` folder, which is referred in Jbang class with `//SOURCES suppor/*` and import the package `import support.apihelper`..
 
- To execuet the application we can use the Jbang cli by passing the java class name and command line arguments like below

```
$ jbang demoappcli.java testUser -r
```

- Output looks like below

```
{ "username": "testUser", "password-provided": "false", "message": "What do you call a computer mouse that swears a lot?" }
```

- When the required parameter is missing the output would be like below

```
$ jbang demoappcli.java -r
Missing required parameter: '<user-name>'
Usage: demoappcli [-hr] [-p] <user-name>
demo app cli example with picocli
      <user-name>        Input an user name .
  -h, --help             display a help message
  -p, --password         Passphrase
  -r, --random-message   flag will print a random message from internet
```

- The main java class 

```java
///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 23

/*
This is the jbang example with the use helper source class in another folder 
and demonstrate the using helper class as package in the class
*/

//SOURCES support/*

//DEPS info.picocli:picocli:4.7.7
//DEPS com.jayway.jsonpath:json-path:2.9.0
//DEPS org.slf4j:slf4j-api:2.0.17
//DEPS org.slf4j:slf4j-simple:2.0.17

import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import com.jayway.jsonpath.JsonPath;

import support.apihelper;

@Command(name = "demoappcli", mixinStandardHelpOptions = true, version = "demo-app-cli v1.0.0",
         description = "demo app cli example with picocli")
public class demoappcli implements Callable<Integer>{

    @Option(names = {"-r", "--random-message"}, description = "flag will print a random message from internet")
    private boolean randomMsg = false;

    @Parameters(paramLabel = "<user-name>", description = "Input an user name .")
    private String username = "User Name";

    @Option(names = {"-p", "--password"}, description = "Passphrase", interactive = true)
    char[] password;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpOption = false;

    private final static String RESPONSE_FORMAT_UNAME_PSWD_RANDMSG = """
    { "username": "%s", "password-provided": "%s", "message": "%s" }
    """;
    
    private final static String RESPONSE_FORMAT_UNAME="""
    { "username": "%s","password-provided": "%s" }        
    """;

    private final String JSON_PATH_RULE="$.message";

    private JsonPath jsonPath = JsonPath.compile(JSON_PATH_RULE);

    @Override
    public Integer call() throws Exception {
       
        String result = "";
        boolean passwordInput = false;
    
        if (password != null ){
        byte[] bytes = new byte[password.length];
        for (int i = 0; i < bytes.length; i++) { bytes[i] = (byte) password[i]; }
        if (bytes.length > 0){
            passwordInput = true;
          }
        }
        if (randomMsg){
            try{
             apihelper apiAccess = new apihelper();
             result = apiAccess.accessAPIForMessage();
             // parse json string for the path
             String apiResponseValue = jsonPath.read(result);
 
             result = String.format(RESPONSE_FORMAT_UNAME_PSWD_RANDMSG, username, passwordInput, apiResponseValue);
 
            }catch (Exception e){
              result = String.format(RESPONSE_FORMAT_UNAME_PSWD_RANDMSG, "Err","Err","Exception occurred accessing API endpoint");
              e.printStackTrace();

            }
           System.out.printf("%s",result);
        } else if( !randomMsg && username != null ) {
            result = String.format(RESPONSE_FORMAT_UNAME, username, passwordInput);
            System.out.printf("%s",result);
        }else{
            System.out.println("usage:");
            System.out.println("jbang demoappcli -r username");
            System.out.println("jbang demoappcli -h ");
        }
        return 0;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new demoappcli()).execute(args);
        System.exit(exitCode);
    }
}
```

- The support `apihelper.java` class

```java
package support;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.jayway.jsonpath.JsonPath;

public class apihelper {

    private final static String OUTPUT_FORMAT = """
        { "statusCode": "%s",
          "message": "%s"
        }
        """;

    private final String API_JSON_RULE="$.setup";

    private final static String API_URL="https://official-joke-api.appspot.com/random_joke";
    
    //"https://api.chucknorris.io/jokes/random"; //$.value

    public String accessAPIForMessage(){

        String output = "";
        // Create an HttpClient instance
        HttpClient client = HttpClient.newHttpClient();

        // Create a HttpRequest
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .GET()
                .build();

        try {
            // Send the request and get the response
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            //System.out.println(response.body());
            JsonPath jsonPath = JsonPath.compile(API_JSON_RULE);
            // parse json string for the path
            String apiResponseValue = jsonPath.read(response.body());

            output = String.format(OUTPUT_FORMAT, response.statusCode(),apiResponseValue);
    
        } catch (IOException | InterruptedException e) {
            output = String.format(OUTPUT_FORMAT, "Err","Exception occurred accessing API endpoint");
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return output;
    }   
}
```