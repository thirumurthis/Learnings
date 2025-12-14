package com.spring.grpc.client;


import com.proto.app.OrderResponse;
import com.proto.app.OrderServiceGrpc;
import com.proto.app.OrderStatus;
import com.proto.app.OrderStatusCode;
import com.spring.grpc.client.data.OrderKey;
import com.spring.grpc.client.data.OrderRequest;
import jakarta.annotation.Nonnull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class OrderController {

    private static Logger logger = LoggerFactory.getLogger(OrderController.class);

    private final OrderServiceGrpc.OrderServiceBlockingStub clientBlockingStub;
    OrderController(OrderServiceGrpc.OrderServiceBlockingStub clientBlockingStub) {
        this.clientBlockingStub = clientBlockingStub;
    }

    @PostMapping(path = "/order",
    consumes = MediaType.APPLICATION_JSON_VALUE,
    produces = MediaType.APPLICATION_JSON_VALUE)
    public OrderResponse submitOrder(@RequestBody OrderRequest orderRequest){

        logger.info("order request received...");
        if(orderRequest.getUserName() == null){
            OrderResponse response = OrderResponse
                    .newBuilder()
                    .build();
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST)
                    .getBody();
        }
        com.proto.app.OrderRequest req = com.proto.app.OrderRequest
                .newBuilder()
                .setDescription(orderRequest.getDescription()==null?"":orderRequest.getDescription())
                .setQuantity(orderRequest.getQuantity())
                .setItemName(orderRequest.getItemName()==null?"":orderRequest.getItemName())
                .setStatus(getStatusCode(orderRequest.getOrderStatus()==null?"RECEIVED":orderRequest.getOrderStatus()))
                .setUserName(orderRequest.getUserName())
                .setUserType(orderRequest.getUserType()==null?"by_user":orderRequest.getUserType())
                .build();


       OrderResponse response = clientBlockingStub.createOrderByUser(req);

        return new ResponseEntity<>(response, HttpStatus.CREATED).getBody();
    }

    @GetMapping("/status")
    public ResponseEntity<StreamingResponseBody> updateStatus(
            @RequestParam(name="userName") Optional<String> userName ,
            @RequestParam(name="orderId") Optional<Long> orderId){

        com.proto.app.OrderKey orderSearchKey = com.proto.app.OrderKey.newBuilder()
                .setUserName(userName.orElse(""))
                .setOrderId(orderId.orElse(0L))
                .build();

        StreamingResponseBody responseBody = statusResponse -> {
             Iterator<OrderStatus> statuses = clientBlockingStub.getOrderStatus(orderSearchKey);
             try {
                 while(statuses.hasNext()) {
                     OrderStatus status = statuses.next();
                     statusResponse.write(status.toString().getBytes(StandardCharsets.UTF_8));
                     statusResponse.flush();
                 }
             }catch (IOException e){
                 logger.error("Error exception ",e);
             }
        };
        return  ResponseEntity.ok()
                .header(HttpHeaders.CACHE_CONTROL,"no-cache")
                .body(responseBody);

    }

    private static com.proto.app.OrderStatusCode getStatusCode(String orderStatus ){
        return switch (orderStatus){
            case AppConstants.CREATED -> OrderStatusCode.valueOf(AppConstants.CREATED);
            case AppConstants.RECEIVED -> OrderStatusCode.valueOf(AppConstants.RECEIVED);
            case AppConstants.INPROGRESS -> OrderStatusCode.valueOf(AppConstants.INPROGRESS);
            case AppConstants.DELIVERED -> OrderStatusCode.valueOf(AppConstants.DELIVERED);
            case AppConstants.COMPLETED -> OrderStatusCode.valueOf(AppConstants.COMPLETED);
            case AppConstants.CANCELLED -> OrderStatusCode.valueOf(AppConstants.CANCELLED);
            case AppConstants.DELETED -> OrderStatusCode.valueOf(AppConstants.DELETED);
            case AppConstants.EDITED -> OrderStatusCode.valueOf(AppConstants.EDITED);
            default -> OrderStatusCode.valueOf(AppConstants.NOSTATUS);
        };
    }
}
