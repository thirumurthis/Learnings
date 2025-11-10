package com.grpc.order.lib.service;


import com.grpc.order.lib.OrderInfo;
import com.grpc.order.lib.OrderRequest;

import java.util.function.Consumer;

public interface OrderProcessor {

    String statusMessage = "Order {}";

    String getAppServerName();

    void onOrderStatusUpdate(Consumer<OrderInfo> statusConsumer);

    void submitOrder(OrderRequest orderRequest);

}
