///usr/bin/env jbang "$0" "$@" ; exit $?

//JAVA 23

/*
This is the jbang example with the use of having helper 
in another package add the package support and java class
use the // sources with the pacakge and add import statement
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

import com.jayway.jsonpath.JsonPath;

import support.apihelper;

@Command(name = "demoappcli", mixinStandardHelpOptions = true, version = "demo-app-cli v1.0.0",
         description = "demo app cli example with picocli")
public class demoappwithpckg implements Callable<Integer>{

    @Option(names = {"-r", "--random-message"}, description = "when this flag is passed will print a random message")
    private boolean randomMsg = false;

    @Option(names = { "-h", "--help" }, usageHelp = true, description = "display a help message")
    private boolean helpOption = false;

    private final static String RESPONSE_FORMAT = """
    { "message": "%s" }
    """;

    private final String JSON_PATH_RULE="$.message";

    private JsonPath jsonPath = JsonPath.compile(JSON_PATH_RULE);

    @Override
    public Integer call() throws Exception {
       
        String result = "";
        if (randomMsg){
            try{
             apihelper apiAccess = new apihelper();
             result = apiAccess.accessAPIForMessage();
             // parse json string for the path
             String apiResponseValue = jsonPath.read(result);
 
             result = String.format(RESPONSE_FORMAT,apiResponseValue);
 
            }catch (Exception e){
              result = String.format(RESPONSE_FORMAT, "Err","Exception occurred accessing API endpoint");
              e.printStackTrace();

            }
           System.out.printf("%s",result);
        }
        return 0;
    }

    // this example implements Callable, so parsing, error handling and handling user
    // requests for usage help or version help can be done with one line of code.
    public static void main(String... args) {
        int exitCode = new CommandLine(new demoappwithpckg()).execute(args);
        System.exit(exitCode);
    }
    

}
