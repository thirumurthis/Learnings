package com.order.grpc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcOrderServerApp {

	public static void main(String[] args) {
		SpringApplication.run(GrpcOrderServerApp.class, args);
	}

}
