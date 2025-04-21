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
