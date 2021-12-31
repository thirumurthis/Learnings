#### Acessing the twitter api's. https://developer.twitter.com
  - with the account, first sign up.
  - Read through the options and create the developer account.
  - Create a project/app with unique name, make a note of below keys.
     - API Key    (store this secretly)
     - API Key Secret (store this secretly)
     - Bearer token.
  - The app created will contain an APP ID as well, this can be obtained at any time from the developer account.

##### Accessign API from Postman (Using documentation version V2):
   - From the [documentation link](https://developer.twitter.com/en/docs/twitter-api/tweets/search/api-reference/get-tweets-search-recent), get the End-Point to search tweets.
   - The `id` parameter is required for this end-point.
   
Using latest version of Postman app, to access the twitter API end point with the Bearer token.
  - Open the Postman app, create a collection, and new request.
  - In `Authorization tab` select `OAuth2.0` and in choosed `Request header` option.
     - In the `Access Token` select `Available Token` and copy paste the Bearer token provided by the Twitter API project screen.
     - The url to access endpoint is `https://api.twitter.com/2/tweets/search/recent`. 
     - Select the Param, in the `key` input `query` and `value` provide `Microsoft News`.
     - clicking send will display a response in the screen. 
     - If we need to add more fields within the response, we can use `tweet.fields` param with the keys like `attachment`,etc. Check docs.
     
##### Accessing via Spring Boot application:
  - Note we can use **RestTemplate**, but currently this feature is in maintenance mode, which means no further development. We can use Spring `Reactive` **WebClient**.
  
  - `WebClient` provides Synchoronus, Asynchoronus and Streaming support.
    - Streaming, the client makes the initial request which is followed by mulitple response.
  
  - Twitter API, provides streaming option as well. 
  
  - To work with the `WebClient` we need to include the pom.xml with Spring reactive dependency.

```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-webflux</artifactId>
  </dependency>
```
   
#### Code Sample, using the Spring reactive WebFlux:
  - Mono object
  - Note: This class is just a test case 
```java
package com.demo.examples.hellospringboot;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;

import reactor.core.publisher.Mono;

@SpringBootTest
public class TwitterAPIAccessTest {

	@Value("${BEARER_TOKEN}")
	private String bearerToken;
	
	public final static String API_ENDPOINT = "https://api.twitter.com";
	public final static String API_ENDPOINT_PATH = "/2/tweets/search/recent";
	
	@Test
	public void simpleRestTemplateClient() {
		HttpHeaders httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization","Bearer "+this.bearerToken);
		
		String url = UriComponentsBuilder.fromHttpUrl(API_ENDPOINT+API_ENDPOINT_PATH)
				.queryParam("query", "Microsoft News")
				.build()
				.toUriString();
		
		HttpEntity<?> entity = new HttpEntity<Object>(httpHeaders);
		
		RestTemplate restTemplate = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET,entity,String.class);
		assertEquals(HttpStatus.OK, response.getStatusCode());
	}

	@Test
	public void simpleWebClient() {
		
		WebClient client = WebClient.create(API_ENDPOINT);
		
		//Mono represents a container, that is going to publish or emit a single item in the future
		// when the item is emitted we can use it.
		// in our case it would be a response entity of string
		Mono<ResponseEntity<String>> monoResponse = client.get() //fire a GET request
				                               .uri(API_ENDPOINT_PATH+"?query={query}","Microsoft News") //using a template here
				                               .header("Authorization","Bearer "+this.bearerToken)
                                               .retrieve()
                                               .toEntity(String.class);
		
		//To perform a synchronized access, using block method
		ResponseEntity<String> response = monoResponse.block();
		System.out.println(response.getBody());
		
		assertEquals(200,response.getStatusCodeValue());

		
	}
	
	@Test
	public void simpleWebClientAsync() throws InterruptedException {
		
		WebClient client = WebClient.create(API_ENDPOINT);
		
		//Mono represents a container, that is going to publish or emit a single item in the future
		// when the item is emitted we can use it.
		// in our case it would be a response entity of string
		Mono<ResponseEntity<String>> monoResponse = client.get() //fire a GET request
				                               .uri(API_ENDPOINT_PATH+"?query={query}","Microsoft News") //using a template here
				                               .header("Authorization","Bearer "+this.bearerToken)
                                               .retrieve()
                                               .toEntity(String.class);
		
		//To perform a Async access, using block method
		monoResponse.subscribe(response -> {
			System.out.println(response.getBody());
			assertEquals(200,response.getStatusCodeValue());	
		});
		
		System.out.println("Call invoked as async using webclient");
		Thread.sleep(5000);//sleep 5 seconds 

		
	}
}
```
#### Using Streaming API of Twitter
```java
package com.demo.examples.hellospringboot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@SpringBootTest
public class TwitterAPIStreamAccess {

	@Value("${BEARER_TOKEN}")
	private String bearerToken;
	
	public final static String API_ENDPOINT = "https://api.twitter.com";

	private final static String API_STREAM_EP_PATH = "/2/tweets/search/stream";
	private final static String API_STREAM_EP_RULES_PATH = API_STREAM_EP_PATH+"/rules";
	
	/*
	 * Autowiring WebClient.Builder -> which provides additional feature which we will be use
	 * There are few pre-configured codex provided, check the spring documentation for more info
	 */
	@Autowired
	private WebClient.Builder builder;
	
	@Test
	public void streamAPITest() throws IOException {
		
		WebClient client = builder
				           .baseUrl(API_ENDPOINT)
				           .defaultHeaders(httpHeaders -> httpHeaders.setBearerAuth(this.bearerToken))
				           .build();
		
		RuleList rules = new RuleList();
		rules.addRules("Microsoft News", "Microsoft Tag");
		
		ObjectMapper obj = new ObjectMapper();
		System.out.println("request "+obj.writeValueAsString(rules));
		//We need to send the rules to the end-point (stream/rules) as a post request
		// when there is a matching criteria, the twitter will send the tweets
		
		client.post()
		.uri(API_STREAM_EP_RULES_PATH)
		.bodyValue(rules)  //Using the bodyValue() spring will serialize the object to json and send in request body
		.retrieve()
		.toBodilessEntity()  //This condition makes sure that we are not worried about the response body itself
		.subscribe(response -> {
			// with this response we will be hitting the twitter stream endpoint with GET method.
			client.get()
			.uri(API_STREAM_EP_PATH)
			.retrieve()
			.bodyToFlux(String.class) //This is like Mono, but flux can handle multiple values
			.filter(body ->!body.isBlank()) // this is to handle blank body response. predicate filters it
			.subscribe(streamRespJson -> {
				System.out.println(streamRespJson);
			});
		});
		
		//Below is just for testing to keep the console open.
		// we can use thread.sleep, not recommended to be used in production
		System.in.read();
		/*
		 *  Note: 
		 *    Mono - use to emit the single value
		 *    Flux - use to emit multiple values.
		 */

	}
}
```
- Rules and RuleList
```java
package com.demo.examples.hellospringboot;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Getter
@Setter
//included for support serialization
@NoArgsConstructor
public class Rules {
	
	//a indicator to tag the values or filter 
	private String tag;
	//Values is the main query or condition to twitter when to send the tweet
	private String value;
	
	Rules(String value, String tag){
	  this.tag = tag;
	  this.value = value;
	}
}
```

```java
package com.demo.examples.hellospringboot;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
'{
 "add": [
   {"value": "cat has:media", "tag": "cats with media"},
   {"value": "cat has:media -grumpy", "tag": "happy cats with media"},
   {"value": "meme", "tag": "funny things"},
   {"value": "meme has:images"}
 ]
}'
*/
@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@Getter
@Setter
@NoArgsConstructor
public class RuleList{
	private List<Rules> add= new ArrayList<>();

	public void addRules(String value, String tag) {
		this.add.add(new Rules(value,tag));
	}	
}
```
