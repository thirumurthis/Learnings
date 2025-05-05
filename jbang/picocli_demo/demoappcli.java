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
