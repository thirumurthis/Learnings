/*
In this blog I show the code on how to access the REST End-point using Java HTTPClient.

With reference to my previous [blog](https://thirumurthi.hashnode.dev/idea-of-building-spring-boot-app-and-deploying-in-heroku-platform) this blog shows the another approach to perform Integration test of the Stock App using Java *HttpClient*.

- Code example with setting up headers and processing the response.
*/
package com.stock.finance.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/*
 * Java 11 http client approach to perform integration test
 */
public class Java11HttpClient {
	
	@Test
	//@Disabled
	public void getEntitiesFromAPI() {
		
		//Create http client 
		HttpClient client = HttpClient.newHttpClient();
		
		// Create request - builder pattern
		HttpRequest request = HttpRequest.newBuilder().uri(URI.create("https://my-stock-boot-app.herokuapp.com/stock-app/about"))
				     .build();
		// Send request and receive response
		//Convert the response as a string, which can also be a About object - use BodyHandlers to convert
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
			assertEquals(true, response.body()!= null && response.body().contains("Stock"));
		} catch (IOException | InterruptedException e ) {
			e.printStackTrace();
			assertTrue(false);
		}
		
	}
	
	public void postEntitiesFromAPI() {
		
		//Create http client 
		HttpClient client = HttpClient.newHttpClient();
		
		// Create request - builder pattern
		HttpRequest request = HttpRequest.newBuilder()
				.header("Content-Type", "application/json")
				.uri(URI.create("https://my-stock-boot-app.herokuapp.com/stock-app/about")) //upper case
				.POST(BodyPublishers.ofString("{\"username\":\"user\",\"password\":\"password\"}"))
				.build();
		// Send request and receive response
		//Convert the response as a string, which can also be a About object - use BodyHandlers to convert
		try {
			HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
			assertEquals(200, response.statusCode());
			assertEquals(true, response.body()!= null && response.body().contains("Stock"));
		} catch (IOException | InterruptedException e ) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
}
