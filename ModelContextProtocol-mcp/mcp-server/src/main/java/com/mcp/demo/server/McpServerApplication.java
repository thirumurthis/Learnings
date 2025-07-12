package com.mcp.demo.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class McpServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(McpServerApplication.class, args);
	}

	/*
	@Bean
	public List<ToolCallback> carTools(CarService carService){
		return List.of(ToolCallbacks.from(carService));
	}
	 */
}
