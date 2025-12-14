package com.spring.grpc.client;

import com.proto.app.OrderRequest;

public interface OrderGrpcClient {

    void submitOrder(OrderRequest orderRequest);

    void getOrderStatus(OrderRequest orderRequest);

    void subscribeForStatus();

    void updateOrderStatus(OrderRequest orderRequest);
}
