package com.spring.grpc.client;

import com.grpc.order.server.lib.OrderRequest;

public interface OrderGrpcClient {

    void submitOrder(OrderRequest orderRequest);

    void getOrderStatus(OrderRequest orderRequest);

    void subscribe();
}
