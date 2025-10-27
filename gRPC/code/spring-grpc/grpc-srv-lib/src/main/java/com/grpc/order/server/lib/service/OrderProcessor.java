package com.grpc.order.server.lib.service;



import com.grpc.order.server.lib.OrderInfo;
import com.grpc.order.server.lib.OrderRequest;

import java.util.function.Consumer;

public interface OrderProcessor {

    String statusMessage = "Order {}";

    String getAppServerName();

    void onOrderStatusUpdate(Consumer<OrderInfo> statusConsumer);

    void submitOrder(OrderRequest orderRequest);

}
