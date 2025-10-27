package com.order.grpc.server.service;

import com.grpc.order.server.lib.OrderInfo;
import com.grpc.order.server.lib.OrderRequest;
import com.grpc.order.server.lib.OrderStatusCode;
import com.grpc.order.server.lib.service.OrderProcessor;

import java.util.function.Consumer;

public class OrderProcessorImpl implements OrderProcessor {

    protected Consumer<OrderInfo> orderStatusConsumer;
    protected OrderInfo orderInfo;


    @Override
    public String getAppServerName() {
        return "order-server";
    }

    @Override
    public void onOrderStatusUpdate(Consumer<OrderInfo> statusConsumer) {
        this.orderStatusConsumer = statusConsumer;
    }

    @Override
    public void submitOrder(OrderRequest orderRequest) {

        orderInfo = OrderInfo.newBuilder()
                .setItemName(orderRequest.getOrderInfo().getItemName())
                .setQuantity(orderRequest.getOrderInfo().getQuantity())
                .setStatusCode(OrderStatusCode.RECEIVED)
                .build();
        orderStatusConsumer.accept(orderInfo);
    }
    
}
