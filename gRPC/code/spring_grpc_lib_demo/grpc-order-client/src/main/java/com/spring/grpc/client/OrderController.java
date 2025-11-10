package com.spring.grpc.client;

import com.grpc.order.lib.OrderInfo;
import com.grpc.order.lib.OrderRequest;
import com.grpc.order.lib.OrderStatusCode;
import com.grpc.order.lib.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/api")
public class OrderController {

    public OrderController(OrderGrpcClient client) {
        this.client = client;
    }
    private final OrderGrpcClient client;

    //@PostMapping("/order")
    @GetMapping("/order")
    //public OrderInfo submitOrder(@RequestBody OrderRequest orderRequest){
        public OrderInfo submitOrder(){

        ((OrderGrpcClientImpl)client).subscribe();
        User usr = User.newBuilder().setUserId("test1")
                .setUserFirstName("test1")
                .build();
        OrderInfo infoRequest = OrderInfo.newBuilder()
                .setId(new Random().nextLong(1000L))
                .setQuantity("3")
                .setItemName("test1")
                .setUserId(usr.getUserId())
                .setDescription("from controller")
                .build();

        OrderRequest orderRequest = OrderRequest.newBuilder()
                .setUser(usr)
                .setOrderInfo(infoRequest)
                .build();
        OrderInfo info = OrderInfo.newBuilder().setStatusCode(OrderStatusCode.RECEIVED)
                .setUserId(orderRequest.getUser().getUserId())
                .setItemName(orderRequest.getOrderInfo().getItemName())
                .setQuantity(orderRequest.getOrderInfo().getQuantity())
                .build();
        client.submitOrder(orderRequest);

        return new ResponseEntity<>(info, HttpStatus.CREATED).getBody();
    }
}
