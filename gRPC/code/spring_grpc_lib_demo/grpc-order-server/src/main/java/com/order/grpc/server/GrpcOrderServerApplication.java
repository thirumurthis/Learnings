package com.order.grpc.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class GrpcOrderServerApplication {

	public static void main(String[] args) {
		SpringApplication.run(GrpcOrderServerApplication.class, args);
	}

}
