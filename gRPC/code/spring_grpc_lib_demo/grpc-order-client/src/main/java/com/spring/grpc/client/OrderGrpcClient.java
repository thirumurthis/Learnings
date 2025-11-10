package com.spring.grpc.client;

import com.grpc.order.lib.OrderRequest;

public interface OrderGrpcClient {

    void submitOrder(OrderRequest orderRequest);

    void getOrderStatus(OrderRequest orderRequest);

    void subscribeForStatus();

    void updateOrderStatus(OrderRequest orderRequest);
}
