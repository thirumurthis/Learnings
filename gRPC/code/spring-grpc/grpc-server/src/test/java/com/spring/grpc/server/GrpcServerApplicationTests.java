package com.spring.grpc.server;

import com.spring.grpc.hello.HelloReply;
import com.spring.grpc.hello.HelloRequest;
import com.spring.grpc.hello.SimpleGrpc;
import io.grpc.ManagedChannel;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.grpc.client.GrpcChannelFactory;
import org.springframework.grpc.test.AutoConfigureInProcessTransport;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertThrows;

//@Nested
@SpringBootTest(properties = "spring.grpc.client.default-channel.address=0.0.0.0:9090")
@AutoConfigureInProcessTransport
class GrpcServerApplicationTests {

	//@Test
	void contextLoads() {
	}

    @Test
    void servesResponseToClient(@Autowired GrpcChannelFactory channels) {
        assertThatResponseIsServedToChannel(channels.createChannel("0.0.0.0:0"));
    }

    private void assertThatResponseIsServedToChannel(ManagedChannel clientChannel) {
        SimpleGrpc.SimpleBlockingStub client = SimpleGrpc.newBlockingStub(clientChannel);
        HelloReply response = client.sayHello(HelloRequest.newBuilder().setName("Alien").build());
        assertThat(response.getMessage()).isEqualTo("Hello ==> Alien");
    }

}
