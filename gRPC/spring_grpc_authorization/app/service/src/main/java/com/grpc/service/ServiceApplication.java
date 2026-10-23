package com.grpc.service;

import com.app.service.grpc.AppRequest;
import com.app.service.grpc.AppResponse;
import com.app.service.grpc.AppServiceGrpc;
import io.grpc.stub.StreamObserver;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.grpc.server.GlobalServerInterceptor;
import org.springframework.grpc.server.security.AuthenticationProcessInterceptor;
import org.springframework.grpc.server.security.GrpcSecurity;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.ObjectPostProcessor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@SpringBootApplication
public class ServiceApplication {

	static void main(String[] args) {
		SpringApplication.run(ServiceApplication.class, args);
	}

	@Bean
	@GlobalServerInterceptor
	AuthenticationProcessInterceptor authenticationProcessInterceptor(GrpcSecurity security) throws  Exception{
		return  security.oauth2ResourceServer(a -> a.jwt(Customizer.withDefaults()))
				.authorizeRequests(a-> a.allRequests().authenticated())
				.build();
	}

}

@Service
class DefaultAppGrpcService extends AppServiceGrpc.AppServiceImplBase {

	@Override
	public void msg(AppRequest request, StreamObserver<AppResponse> respObserver){
		// to do something
		respObserver.onNext(
				AppResponse.newBuilder()
						.setMessage("Responding pong to "+ (
										Objects.requireNonNull(SecurityContextHolder.getContext()
                                                        .getAuthentication())
												.getName()+"!"
										))
						.build()
		);

		respObserver.onCompleted();
	}
}